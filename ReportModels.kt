package com.moneymanager.app.data

/** Row shape returned by @Query aggregations — Room maps these automatically. */
data class CategoryTotal(val categoryKey: String, val total: Double)

data class PaymentModeTotal(val paymentMode: PaymentMode, val total: Double)

data class DayTotal(val epochDay: Long, val income: Double, val expense: Double)

/** Built in the repository layer (not a direct query result). */
data class PeriodSummary(
    val totalIncome: Double,
    val totalExpense: Double,
    val netSavings: Double,
    val savingsRatePercent: Double,
    val byCategory: List<CategoryTotal>,
    val byPaymentMode: List<PaymentModeTotal>,
    val topExpenseCategories: List<CategoryTotal>
)

data class BudgetStatus(
    val categoryKey: String,
    val budget: Double,
    val actual: Double,
    val variance: Double,
    val variancePercent: Double,
    val isOverBudget: Boolean
)
