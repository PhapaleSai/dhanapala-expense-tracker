package com.phapalesai.dhanapala.domain

import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class MoneySavingTipsTest {

    @Test
    fun `food delivery context gives a different tip than general context`() {
        val generalTip = MoneySavingTips.random(isFoodDeliveryContext = false, random = Random(1))
        val deliveryTip = MoneySavingTips.random(isFoodDeliveryContext = true, random = Random(1))
        assertNotEquals(generalTip, deliveryTip)
    }

    @Test
    fun `tips are never blank`() {
        repeat(20) {
            assertTrue(MoneySavingTips.random(isFoodDeliveryContext = it % 2 == 0).isNotBlank())
        }
    }
}
