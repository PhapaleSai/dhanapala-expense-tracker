package com.phapalesai.dhanapala.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class BudgetNotificationDeciderTest {

    @Test
    fun `below 80 percent never notifies`() {
        val tier = BudgetNotificationDecider.decide(50.0, notified80 = false, notifiedExceeded = false)
        assertEquals(BudgetNotifyTier.NONE, tier)
    }

    @Test
    fun `crossing 80 percent notifies once`() {
        val tier = BudgetNotificationDecider.decide(82.0, notified80 = false, notifiedExceeded = false)
        assertEquals(BudgetNotifyTier.EIGHTY_PERCENT, tier)
    }

    @Test
    fun `already notified 80 percent does not notify again while under 100`() {
        val tier = BudgetNotificationDecider.decide(95.0, notified80 = true, notifiedExceeded = false)
        assertEquals(BudgetNotifyTier.NONE, tier)
    }

    @Test
    fun `crossing 100 percent notifies exceeded even if 80 percent already fired`() {
        val tier = BudgetNotificationDecider.decide(105.0, notified80 = true, notifiedExceeded = false)
        assertEquals(BudgetNotifyTier.EXCEEDED, tier)
    }

    @Test
    fun `already notified exceeded does not notify again`() {
        val tier = BudgetNotificationDecider.decide(150.0, notified80 = true, notifiedExceeded = true)
        assertEquals(BudgetNotifyTier.NONE, tier)
    }
}
