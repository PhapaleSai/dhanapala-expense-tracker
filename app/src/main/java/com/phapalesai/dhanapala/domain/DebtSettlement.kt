package com.phapalesai.dhanapala.domain

/** Classic greedy debt-settlement: match the biggest creditor with the biggest debtor, repeat. Minimizes payment count. */
object DebtSettlement {

    data class Payment(val from: String, val to: String, val amount: Double)

    fun settle(balances: Map<String, Double>): List<Payment> {
        val creditors = balances.filterValues { it > 0.01 }.toMutableMap()
        val debtors = balances.filterValues { it < -0.01 }.mapValues { -it.value }.toMutableMap()
        val payments = mutableListOf<Payment>()

        while (creditors.isNotEmpty() && debtors.isNotEmpty()) {
            val (creditor, creditAmount) = creditors.maxByOrNull { it.value }!!
            val (debtor, debtAmount) = debtors.maxByOrNull { it.value }!!
            val settledAmount = minOf(creditAmount, debtAmount)
            payments.add(Payment(from = debtor, to = creditor, amount = settledAmount))

            val remainingCredit = creditAmount - settledAmount
            val remainingDebt = debtAmount - settledAmount
            if (remainingCredit <= 0.01) creditors.remove(creditor) else creditors[creditor] = remainingCredit
            if (remainingDebt <= 0.01) debtors.remove(debtor) else debtors[debtor] = remainingDebt
        }
        return payments
    }
}
