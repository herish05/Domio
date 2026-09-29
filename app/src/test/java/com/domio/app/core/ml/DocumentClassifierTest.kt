package com.domio.app.core.ml

import org.junit.Assert.assertEquals
import org.junit.Test

class DocumentClassifierTest {
    @Test
    fun classifyDocument_detectsInsurancePolicy() {
        val text = """
            Insurance Policy
            Policy Number: POL-778899
            Premium Amount: ₹ 2499.00
            Coverage: health care
        """.trimIndent()

        assertEquals("Insurance", DocumentClassifier.classifyDocument(text))
    }

    @Test
    fun classifyDocument_detectsBankStatement() {
        val text = """
            Account Statement
            Account No: 123456789012
            IFSC: HDFC0001234
            Customer Name: Rahul Sharma
        """.trimIndent()

        assertEquals("Bank Statement", DocumentClassifier.classifyDocument(text))
    }

    @Test
    fun classifyDocument_usesGenericCategoryWhenUnknown() {
        val text = """
            Service Agreement
            Reference: SA-1001
            Company: Acme Workspaces
        """.trimIndent()

        assertEquals("General Document", DocumentClassifier.classifyDocument(text))
    }
}
