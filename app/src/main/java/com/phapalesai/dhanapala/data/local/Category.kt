package com.phapalesai.dhanapala.data.local

object Category {
    const val FOOD = "Food"
    const val FUEL = "Fuel"
    const val SHOPPING = "Shopping"
    const val BILLS = "Bills"
    const val ENTERTAINMENT = "Entertainment"
    const val TRAVEL = "Travel"
    const val UPI = "UPI"
    const val CASH_WITHDRAWAL = "Cash Withdrawal"
    const val SALARY = "Salary"
    const val REFUND = "Refund"
    const val TRANSFER = "Transfer"
    const val OTHER = "Other"
    const val UNCATEGORIZED = "Anonymous Expenses"

    val ALL = listOf(
        FOOD, FUEL, SHOPPING, BILLS, ENTERTAINMENT, TRAVEL, UPI,
        CASH_WITHDRAWAL, SALARY, REFUND, TRANSFER, OTHER, UNCATEGORIZED
    )
}
