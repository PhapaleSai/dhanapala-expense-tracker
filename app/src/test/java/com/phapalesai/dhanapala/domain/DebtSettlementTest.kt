package com.phapalesai.dhanapala.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DebtSettlementTest {

    @Test
    fun `two people, one owes the other`() {
        val payments = DebtSettlement.settle(mapOf("A" to 100.0, "B" to -100.0))
        assertEquals(listOf(DebtSettlement.Payment("B", "A", 100.0)), payments)
    }

    @Test
    fun `everyone already settled produces no payments`() {
        val payments = DebtSettlement.settle(mapOf("A" to 0.0, "B" to 0.0))
        assertTrue(payments.isEmpty())
    }

    @Test
    fun `three people settle with at most n-1 payments`() {
        // A paid 300 for a trip split 3 ways (100 each): A is owed 200, B and C each owe 100.
        val payments = DebtSettlement.settle(mapOf("A" to 200.0, "B" to -100.0, "C" to -100.0))
        assertTrue(payments.size <= 2)
        val totalToA = payments.filter { it.to == "A" }.sumOf { it.amount }
        assertEquals(200.0, totalToA, 0.01)
    }

    @Test
    fun `every payment nets balances back to zero`() {
        val balances = mapOf("A" to 250.0, "B" to -100.0, "C" to -50.0, "D" to -100.0)
        val payments = DebtSettlement.settle(balances)
        val net = balances.keys.associateWith { person ->
            val paid = payments.filter { it.from == person }.sumOf { it.amount }
            val received = payments.filter { it.to == person }.sumOf { it.amount }
            balances.getValue(person) - received + paid
        }
        net.values.forEach { assertEquals(0.0, it, 0.01) }
    }
}
