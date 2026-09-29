package com.domio.app.core.ml

import android.content.Context
import android.net.Uri
import android.util.Log
import com.domio.app.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import com.google.gson.Gson
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

class TextRecognizerHelper(private val context: Context) {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    
    private data class GeminiDocumentResult(
        val title: String?,
        val type: String?,
        val issuer: String?,
        val documentNumber: String?,
        val issueDateMillis: Long?,
        val expiryDateMillis: Long?,
        val amount: Double?,
        val dynamicFields: Map<String, String>?
    )

    private val generativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = BuildConfig.GEMINI_API_KEY,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
            }
        )
    }

    suspend fun analyzeDocument(uri: Uri): SmartParsedDocument {
        var rawText = ""
        try {
            val image = InputImage.fromFilePath(context, uri)
            val result = recognizer.process(image).await()
            rawText = result.text ?: ""
        } catch (e: Exception) {
            Log.e("TextRecognizerHelper", "ML Kit OCR failed", e)
        }

        val genericType = DocumentClassifier.classifyDocument(rawText)

        // 1. Specialized Smart Local On-Device Parser for Indian & Global Documents
        val fallback = parseLocally(rawText, uri)

        // 2. If Gemini API key is missing or default placeholder, return refined local fallback
        if (BuildConfig.GEMINI_API_KEY.isBlank() || BuildConfig.GEMINI_API_KEY == "AIzaSy_YOUR_API_KEY_HERE") {
            Log.d("TextRecognizerHelper", "Using specialized on-device ML Kit OCR parser")
            return fallback.copy(
                extractedType = fallback.extractedType ?: genericType
            )
        }

        // 3. Try Gemini AI refinement with safety fallback
        return try {
            val prompt = """
                You are an expert AI document parser for Indian and Global documents (Aadhaar, PAN, Vehicle RC, Voter ID, Passport, Utility Bills, Receipts, Bank Statements, Insurance, Warranties).
                
                Analyze the provided OCR text and extract all relevant fields into a clean JSON object:
                {
                    "title": "A short, precise document title (e.g. 'Aadhaar Card', 'PAN Card', 'Delhi Electricity Bill', 'Amazon Receipt', 'Vehicle RC')",
                    "type": "The document category ('ID Document', 'Insurance', 'Invoice', 'Warranty', 'Receipt / Bill', 'Vehicle Document', 'Utility Bill', 'Other')",
                    "issuer": "The issuing authority or company name (e.g., 'Government of India (UIDAI)', 'Income Tax Dept', 'BSES Rajdhani', 'Amazon')",
                    "documentNumber": "The primary unique identification / document / policy / invoice number",
                    "issueDateMillis": null,
                    "expiryDateMillis": null,
                    "amount": null,
                    "dynamicFields": {
                        "Field Name 1": "Value 1",
                        "Field Name 2": "Value 2"
                    }
                }

                Rules:
                - Extract ALL specific key-value fields present in the text into 'dynamicFields' (e.g. 'Holder Name', 'Aadhaar Number', 'PAN Number', 'Chassis Number', 'Engine Number', 'Consumer Number', 'Account Number', 'IFSC Code', 'Due Date').
                - Do NOT include empty or boilerplate lines (like 'To,', 'Signature', 'Page 1').
                - Return ONLY valid JSON.

                OCR Text:
                $rawText
            """.trimIndent()

            val aiResponse = generativeModel.generateContent(prompt)
            val rawJson = aiResponse.text ?: return fallback
            val jsonText = rawJson.replace("```json", "").replace("```", "").trim()
            
            val geminiResult = Gson().fromJson(jsonText, GeminiDocumentResult::class.java)

            // Clean title if Gemini returns boilerplate or "To,"
            val cleanTitle = geminiResult.title?.takeIf { 
                it.isNotBlank() && !it.equals("To,", true) && !it.startsWith("API Error", true) 
            } ?: fallback.extractedTitle

            val normalizedType = (geminiResult.type?.takeIf { it.isNotBlank() }
                ?: fallback.extractedType
                ?: genericType)

            SmartParsedDocument(
                fileUri = uri.toString(),
                extractedAmount = geminiResult.amount ?: fallback.extractedAmount,
                extractedDate = geminiResult.issueDateMillis ?: fallback.extractedDate,
                extractedExpiryDate = geminiResult.expiryDateMillis ?: fallback.extractedExpiryDate,
                extractedIssuer = geminiResult.issuer?.takeIf { it.isNotBlank() } ?: fallback.extractedIssuer,
                extractedDocumentNumber = geminiResult.documentNumber?.takeIf { it.isNotBlank() } ?: fallback.extractedDocumentNumber,
                extractedTitle = cleanTitle,
                extractedType = normalizedType,
                extractedDynamicFields = geminiResult.dynamicFields?.takeIf { it.isNotEmpty() } ?: fallback.extractedDynamicFields
            )
        } catch (e: Exception) {
            Log.w("DOMIO_GEMINI_WARN", "Gemini AI failed, using ML Kit local fallback: ${e.message}")
            fallback
        }
    }

    private fun parseLocally(text: String, uri: Uri): SmartParsedDocument {
        val rawLines = text.lines().map { it.trim() }.filter { it.isNotBlank() }
        val lowerText = text.lowercase()

        // Filter boilerplate words from line headers
        val cleanLines = rawLines.filter { line ->
            val l = line.lowercase()
            !l.startsWith("to,") && !l.equals("to") && !l.startsWith("from,") && !l.contains("enrolment no") && !l.contains("enrollment no")
        }

        // -------------------------------------------------------------
        // 1. AADHAAR CARD
        // -------------------------------------------------------------
        if (lowerText.contains("aadhaar") || lowerText.contains("aadhar") || lowerText.contains("unique identification") || lowerText.contains("uidai") || (lowerText.contains("government of india") && lowerText.contains("dob"))) {
            val aadhaarRegex = Regex("""\b([2-9]\d{3}\s?\d{4}\s?\d{4})\b""")
            val aadhaarMatch = aadhaarRegex.find(text)
            val formattedAadhaar = aadhaarMatch?.groupValues?.get(1)?.let { rawNum ->
                val digits = rawNum.replace(" ", "")
                if (digits.length == 12) "${digits.substring(0, 4)} ${digits.substring(4, 8)} ${digits.substring(8, 12)}" else rawNum
            }

            val holderName = cleanLines.firstOrNull { line ->
                val l = line.lowercase()
                !l.contains("government") && !l.contains("india") && !l.contains("dob") && !l.contains("male") && !l.contains("female") && !l.contains("address") && !l.contains("unique") && line.length >= 3 && line.all { char -> char.isLetter() || char.isWhitespace() }
            } ?: "Aadhaar Card Holder"

            val dobRegex = Regex("""(?:DOB|Date of Birth|YOB|Year of Birth)\s*:?\s*(\d{2}/\d{2}/\d{4}|\d{4})""", RegexOption.IGNORE_CASE)
            val dobMatch = dobRegex.find(text)
            val dobStr = dobMatch?.groupValues?.get(1)

            val gender = when {
                lowerText.contains("female") -> "Female"
                lowerText.contains("male") -> "Male"
                else -> null
            }

            val dynamicMap = mutableMapOf<String, String>()
            if (holderName.isNotBlank()) dynamicMap["Holder Name"] = holderName
            if (formattedAadhaar != null) dynamicMap["Aadhaar Number"] = formattedAadhaar
            if (dobStr != null) dynamicMap["Date of Birth"] = dobStr
            if (gender != null) dynamicMap["Gender"] = gender

            return SmartParsedDocument(
                fileUri = uri.toString(),
                extractedTitle = "Aadhaar Card",
                extractedType = "ID Document",
                extractedIssuer = "Government of India (UIDAI)",
                extractedDocumentNumber = formattedAadhaar,
                extractedDate = System.currentTimeMillis(),
                extractedExpiryDate = null,
                extractedAmount = null,
                extractedDynamicFields = if (dynamicMap.isNotEmpty()) dynamicMap else null
            )
        }

        // -------------------------------------------------------------
        // 2. PAN CARD
        // -------------------------------------------------------------
        if (lowerText.contains("income tax") || lowerText.contains("permanent account number") || lowerText.contains("pan card")) {
            val panRegex = Regex("""\b([A-Z]{5}[0-9]{4}[A-Z]{1})\b""")
            val panMatch = panRegex.find(text)
            val panNumber = panMatch?.groupValues?.get(1)

            val holderName = cleanLines.firstOrNull { line ->
                val l = line.lowercase()
                !l.contains("income") && !l.contains("tax") && !l.contains("department") && !l.contains("india") && line.length >= 3
            } ?: "PAN Card Holder"

            val dynamicMap = mutableMapOf<String, String>()
            if (holderName.isNotBlank()) dynamicMap["Holder Name"] = holderName
            if (panNumber != null) dynamicMap["PAN Number"] = panNumber

            return SmartParsedDocument(
                fileUri = uri.toString(),
                extractedTitle = "PAN Card",
                extractedType = "ID Document",
                extractedIssuer = "Income Tax Department, Govt of India",
                extractedDocumentNumber = panNumber,
                extractedDate = System.currentTimeMillis(),
                extractedExpiryDate = null,
                extractedAmount = null,
                extractedDynamicFields = if (dynamicMap.isNotEmpty()) dynamicMap else null
            )
        }

        // -------------------------------------------------------------
        // 3. DRIVING LICENCE (DL)
        // -------------------------------------------------------------
        if (lowerText.contains("driving licence") || lowerText.contains("driving license") || lowerText.contains("dl no") || lowerText.contains("licence no")) {
            val dlRegex = Regex("""\b([A-Z]{2}[-\s]?\d{2}[-\s]?\d{11}|[A-Z]{2}\d{13})\b""")
            val dlMatch = dlRegex.find(text)
            val dlNumber = dlMatch?.groupValues?.get(1)

            val holderName = cleanLines.firstOrNull { line ->
                val l = line.lowercase()
                !l.contains("licence") && !l.contains("driving") && !l.contains("transport") && !l.contains("india") && !l.contains("union") && line.length >= 3
            } ?: "Licence Holder"

            val dynamicMap = mutableMapOf<String, String>()
            if (holderName.isNotBlank()) dynamicMap["Holder Name"] = holderName
            if (dlNumber != null) dynamicMap["DL Number"] = dlNumber

            return SmartParsedDocument(
                fileUri = uri.toString(),
                extractedTitle = "Driving Licence",
                extractedType = "ID Document",
                extractedIssuer = "Transport Department, Govt of India",
                extractedDocumentNumber = dlNumber,
                extractedDate = System.currentTimeMillis(),
                extractedExpiryDate = null,
                extractedAmount = null,
                extractedDynamicFields = if (dynamicMap.isNotEmpty()) dynamicMap else null
            )
        }

        // -------------------------------------------------------------
        // 4. VOTER ID (EPIC)
        // -------------------------------------------------------------
        if (lowerText.contains("election commission") || lowerText.contains("elector photo") || lowerText.contains("voter id") || lowerText.contains("epic no")) {
            val epicRegex = Regex("""\b([A-Z]{3}[0-9]{7})\b""")
            val epicMatch = epicRegex.find(text)
            val epicNumber = epicMatch?.groupValues?.get(1)

            val holderName = cleanLines.firstOrNull { line ->
                val l = line.lowercase()
                !l.contains("election") && !l.contains("commission") && !l.contains("india") && !l.contains("elector") && line.length >= 3
            } ?: "Voter Name"

            val dynamicMap = mutableMapOf<String, String>()
            if (holderName.isNotBlank()) dynamicMap["Elector Name"] = holderName
            if (epicNumber != null) dynamicMap["Voter ID (EPIC) No"] = epicNumber

            return SmartParsedDocument(
                fileUri = uri.toString(),
                extractedTitle = "Voter ID Card",
                extractedType = "ID Document",
                extractedIssuer = "Election Commission of India",
                extractedDocumentNumber = epicNumber,
                extractedDate = System.currentTimeMillis(),
                extractedExpiryDate = null,
                extractedAmount = null,
                extractedDynamicFields = if (dynamicMap.isNotEmpty()) dynamicMap else null
            )
        }

        // -------------------------------------------------------------
        // 5. PASSPORT
        // -------------------------------------------------------------
        if (lowerText.contains("republic of india") && (lowerText.contains("passport") || lowerText.contains("code ind"))) {
            val passportRegex = Regex("""\b([A-Z][0-9]{7})\b""")
            val passportMatch = passportRegex.find(text)
            val passportNumber = passportMatch?.groupValues?.get(1)

            val dynamicMap = mutableMapOf<String, String>()
            if (passportNumber != null) dynamicMap["Passport Number"] = passportNumber
            dynamicMap["Country Code"] = "IND"

            return SmartParsedDocument(
                fileUri = uri.toString(),
                extractedTitle = "Indian Passport",
                extractedType = "ID Document",
                extractedIssuer = "Ministry of External Affairs, India",
                extractedDocumentNumber = passportNumber,
                extractedDate = System.currentTimeMillis(),
                extractedExpiryDate = null,
                extractedAmount = null,
                extractedDynamicFields = if (dynamicMap.isNotEmpty()) dynamicMap else null
            )
        }

        // -------------------------------------------------------------
        // 6. VEHICLE REGISTRATION CERTIFICATE (RC)
        // -------------------------------------------------------------
        if (lowerText.contains("registration certificate") || lowerText.contains("transport department") || lowerText.contains("chassis no") || lowerText.contains("engine no")) {
            val regRegex = Regex("""\b([A-Z]{2}[-\s]?\d{2}[-\s]?[A-Z]{1,2}[-\s]?\d{4})\b""")
            val regMatch = regRegex.find(text)
            val regNumber = regMatch?.groupValues?.get(1)

            val chassisRegex = Regex("""(?:chassis|vin)\s*#?\s*:?\s*([A-Z0-9]{10,20})""", RegexOption.IGNORE_CASE)
            val chassisNo = chassisRegex.find(text)?.groupValues?.get(1)

            val engineRegex = Regex("""(?:engine)\s*#?\s*:?\s*([A-Z0-9]{6,20})""", RegexOption.IGNORE_CASE)
            val engineNo = engineRegex.find(text)?.groupValues?.get(1)

            val dynamicMap = mutableMapOf<String, String>()
            if (regNumber != null) dynamicMap["Registration No"] = regNumber
            if (chassisNo != null) dynamicMap["Chassis Number"] = chassisNo
            if (engineNo != null) dynamicMap["Engine Number"] = engineNo

            return SmartParsedDocument(
                fileUri = uri.toString(),
                extractedTitle = "Vehicle RC (${regNumber ?: "Vehicle"})",
                extractedType = "Vehicle Document",
                extractedIssuer = "Transport Department, RTO",
                extractedDocumentNumber = regNumber,
                extractedDate = System.currentTimeMillis(),
                extractedExpiryDate = null,
                extractedAmount = null,
                extractedDynamicFields = if (dynamicMap.isNotEmpty()) dynamicMap else null
            )
        }

        // -------------------------------------------------------------
        // 4. UTILITY BILL (Electricity, Water, Gas)
        // -------------------------------------------------------------
        if (lowerText.contains("electricity") || lowerText.contains("utility bill") || lowerText.contains("consumer no") || lowerText.contains("ca no") || lowerText.contains("kwh")) {
            val consumerRegex = Regex("""(?:consumer|ca|account|kno)\s*#?\s*:?\s*(\d{6,16})""", RegexOption.IGNORE_CASE)
            val consumerNo = consumerRegex.find(text)?.groupValues?.get(1)

            val amountRegex = Regex("""(?:total|amount|net payable|paid|₹|\$|rs\.?)\s*:?\s*₹?\s*\$?([\d,]+\.\d{2})""", RegexOption.IGNORE_CASE)
            val amountMatch = amountRegex.find(text)
            val parsedAmount = amountMatch?.groupValues?.get(1)?.replace(",", "")?.toDoubleOrNull()

            val providerName = cleanLines.firstOrNull()?.take(30) ?: "Electricity Board"

            val dynamicMap = mutableMapOf<String, String>()
            if (consumerNo != null) dynamicMap["Consumer Number"] = consumerNo
            if (parsedAmount != null) dynamicMap["Bill Amount"] = "₹$parsedAmount"

            return SmartParsedDocument(
                fileUri = uri.toString(),
                extractedTitle = "$providerName Utility Bill",
                extractedType = "Utility Bill",
                extractedIssuer = providerName,
                extractedDocumentNumber = consumerNo,
                extractedDate = System.currentTimeMillis(),
                extractedExpiryDate = null,
                extractedAmount = parsedAmount,
                extractedDynamicFields = if (dynamicMap.isNotEmpty()) dynamicMap else null
            )
        }

        // -------------------------------------------------------------
        // 5. STORE RECEIPT / PURCHASE INVOICE
        // -------------------------------------------------------------
        val isReceipt = lowerText.contains("receipt") || lowerText.contains("invoice") || lowerText.contains("total") || lowerText.contains("bill") || lowerText.contains("tax invoice")
        val amountRegex = Regex("""(?:total|amount|paid|grand total|net amount|₹|\$|rs\.?|inr)\s*:?\s*₹?\s*\$?([\d,]+\.\d{2})""", RegexOption.IGNORE_CASE)
        val amountMatch = amountRegex.find(text)
        val parsedAmount = amountMatch?.groupValues?.get(1)?.replace(",", "")?.toDoubleOrNull()

        val storeName = cleanLines.firstOrNull { line ->
            val l = line.lowercase()
            !l.contains("invoice") && !l.contains("receipt") && !l.contains("tax") && !l.contains("date") && line.length >= 3
        } ?: "Store"

        if (isReceipt) {
            val invoiceNumRegex = Regex("""(?:INVOICE|REC|BILL|REF)\s*#?\s*:?\s*([A-Z0-9\-\/]{4,20})""", RegexOption.IGNORE_CASE)
            val invoiceNum = invoiceNumRegex.find(text)?.groupValues?.get(1)

            val dynamicMap = mutableMapOf<String, String>()
            if (storeName.isNotBlank()) dynamicMap["Store Name"] = storeName
            if (parsedAmount != null) dynamicMap["Total Amount"] = "₹$parsedAmount"

            return SmartParsedDocument(
                fileUri = uri.toString(),
                extractedTitle = "$storeName Receipt",
                extractedType = "Receipt / Bill",
                extractedIssuer = storeName,
                extractedDocumentNumber = invoiceNum,
                extractedDate = System.currentTimeMillis(),
                extractedExpiryDate = null,
                extractedAmount = parsedAmount,
                extractedDynamicFields = if (dynamicMap.isNotEmpty()) dynamicMap else null
            )
        }

        // -------------------------------------------------------------
        // 6. GENERAL FALLBACK PARSER
        // -------------------------------------------------------------
        val firstTitleLine = cleanLines.firstOrNull()?.take(40) ?: "Scanned Document"

        val type = when {
            lowerText.contains("warranty") || lowerText.contains("guarantee") -> "Warranty Card"
            lowerText.contains("insurance") || lowerText.contains("policy") -> "Insurance Policy"
            lowerText.contains("license") || lowerText.contains("passport") -> "ID Document"
            else -> "General Document"
        }

        val docNumRegex = Regex("""(?:NO\.?|NUMBER|POLICY|ID)\s*:?\s*([A-Z0-9\-\/]{4,20})""", RegexOption.IGNORE_CASE)
        val docNum = docNumRegex.find(text)?.groupValues?.get(1)

        val dynamicMap = mutableMapOf<String, String>()
        cleanLines.take(4).forEachIndexed { index, line ->
            dynamicMap["Key Details ${index + 1}"] = line
        }

        return SmartParsedDocument(
            fileUri = uri.toString(),
            extractedTitle = firstTitleLine,
            extractedType = type,
            extractedIssuer = cleanLines.firstOrNull()?.take(30) ?: "Issuer",
            extractedDocumentNumber = docNum,
            extractedDate = System.currentTimeMillis(),
            extractedExpiryDate = null,
            extractedAmount = parsedAmount,
            extractedDynamicFields = if (dynamicMap.isNotEmpty()) dynamicMap else null
        )
    }
}
