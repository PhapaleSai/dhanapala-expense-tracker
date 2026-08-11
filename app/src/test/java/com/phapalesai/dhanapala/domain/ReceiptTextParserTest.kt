package com.phapalesai.dhanapala.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReceiptTextParserTest {

    @Test
    fun `picks the amount on the total line over subtotal`() {
        val text = """
            Café Coffee Day
            Cappuccino          120.00
            Sandwich             80.00
            Subtotal            200.00
            Tax                  10.00
            Total                210.00
            Thank you!
        """.trimIndent()
        assertEquals(210.0, ReceiptTextParser.extractTotal(text)!!, 0.01)
    }

    @Test
    fun `falls back to a currency-prefixed amount when there is no total line`() {
        val text = "Payment received: Rs. 450.50\nThanks for visiting"
        assertEquals(450.50, ReceiptTextParser.extractTotal(text)!!, 0.01)
    }

    @Test
    fun `falls back to the largest number when nothing else matches`() {
        val text = "Item A 10\nItem B 25\nItem C 999"
        assertEquals(999.0, ReceiptTextParser.extractTotal(text)!!, 0.01)
    }

    @Test
    fun `returns null for text with no numbers at all`() {
        assertNull(ReceiptTextParser.extractTotal("Thank you for shopping with us!"))
    }
}
