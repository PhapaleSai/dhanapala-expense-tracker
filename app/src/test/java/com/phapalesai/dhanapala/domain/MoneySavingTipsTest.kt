package com.phapalesai.dhanapala.domain

import com.phapalesai.dhanapala.data.local.Category
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class MoneySavingTipsTest {

    @Test
    fun `food category gives a different tip pool than general`() {
        val generalTip = MoneySavingTips.random(recentCategory = null, random = Random(1))
        val foodTip = MoneySavingTips.random(recentCategory = Category.FOOD, random = Random(1))
        assertNotEquals(generalTip, foodTip)
    }

    @Test
    fun `each known category returns a non-blank tip`() {
        val categories = listOf(
            null, Category.FOOD, Category.FUEL, Category.SHOPPING, Category.BILLS,
            Category.ENTERTAINMENT, Category.TRAVEL, Category.CASH_WITHDRAWAL, Category.OTHER
        )
        categories.forEach { category ->
            assertTrue(MoneySavingTips.random(recentCategory = category).isNotBlank())
        }
    }

    @Test
    fun `unrecognized category falls back to general pool`() {
        val tip = MoneySavingTips.random(recentCategory = "Some Unknown Category", random = Random(5))
        assertTrue(tip.isNotBlank())
    }
}
