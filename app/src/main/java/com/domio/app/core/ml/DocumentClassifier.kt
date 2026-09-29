package com.domio.app.core.ml

object DocumentClassifier {
    fun classifyDocument(text: String): String {
        val lower = text.lowercase()

        return when {
            lower.contains("insurance") || lower.contains("policy number") || lower.contains("premium amount") -> "Insurance"
            lower.contains("account statement") || lower.contains("bank") || lower.contains("ifsc") || lower.contains("account no") -> "Bank Statement"
            lower.contains("electricity") || lower.contains("utility bill") || lower.contains("consumer no") || lower.contains("gas bill") || lower.contains("water bill") -> "Utility Bill"
            lower.contains("receipt") || lower.contains("invoice") || lower.contains("tax invoice") || lower.contains("total amount") -> "Receipt / Bill"
            lower.contains("aadhaar") || lower.contains("aadhar") || lower.contains("pan card") || lower.contains("driving licence") || lower.contains("driving license") || lower.contains("passport") || lower.contains("voter id") || lower.contains("election commission") -> "ID Document"
            lower.contains("registration certificate") || lower.contains("transport department") || lower.contains("chassis no") || lower.contains("engine no") || lower.contains("vehicle") -> "Vehicle Document"
            lower.contains("warranty") || lower.contains("guarantee") -> "Warranty"
            else -> "General Document"
        }
    }
}
