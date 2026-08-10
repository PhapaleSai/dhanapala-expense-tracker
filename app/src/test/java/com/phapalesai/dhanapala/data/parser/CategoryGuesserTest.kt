package com.phapalesai.dhanapala.data.parser

import com.phapalesai.dhanapala.data.local.Category
import com.phapalesai.dhanapala.data.local.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Test

class CategoryGuesserTest {

    @Test
    fun `recognizable brand names map to their category`() {
        assertEquals(Category.FOOD, CategoryGuesser.guess("Rs 250 paid to SWIGGY via UPI", TransactionType.DEBIT))
        assertEquals(Category.FUEL, CategoryGuesser.guess("Rs 800 debited at HPCL petrol pump", TransactionType.DEBIT))
        assertEquals(Category.BILLS, CategoryGuesser.guess("Rs 450 debited for electricity bill payment", TransactionType.DEBIT))
        assertEquals(Category.SHOPPING, CategoryGuesser.guess("Rs 1200 paid to AMAZON", TransactionType.DEBIT))
        assertEquals(Category.TRAVEL, CategoryGuesser.guess("Rs 180 paid for UBER ride", TransactionType.DEBIT))
    }

    @Test
    fun `opaque VPA with no brand name falls back to UPI, not a wrong guess`() {
        // Real bank SMS format (JSBL): the receiving VPA is a randomly
        // generated handle with zero brand signal — there is nothing in the
        // text to categorize beyond "it was a UPI transaction."
        val body = "A/c no. XX9476 is debited for Rs.5.00 on 09-08-2026 22:10:24 and CR to VPA " +
            "q345368832@ybl (UPI Ref no 127660747053) If not done by you, call 02024404521/22/23. JSBLPune"
        assertEquals(Category.UPI, CategoryGuesser.guess(body, TransactionType.DEBIT))
    }

    @Test
    fun `salary and refund take priority over generic upi keyword`() {
        assertEquals(Category.SALARY, CategoryGuesser.guess("Rs 45000 credited as SALARY via UPI", TransactionType.CREDIT))
        assertEquals(Category.REFUND, CategoryGuesser.guess("Rs 200 refunded via UPI", TransactionType.CREDIT))
    }

    @Test
    fun `unrecognized debit with no keywords at all is anonymous`() {
        assertEquals(
            Category.UNCATEGORIZED,
            CategoryGuesser.guess("Rs 500 debited from A/c XX1234", TransactionType.DEBIT)
        )
    }
}
