package com.moneymanager.app.repository

import com.moneymanager.app.data.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields
import kotlin.math.abs

/**
 * Single source of truth for everything the spec's "Reports to Generate" and
 * "Budget Estimation & Fixing" sections ask for. UI layers (ViewModels) call
 * these functions; they never touch the DAOs directly.
 */
class MoneyRepository(
    private val txnDao: TransactionDao,
    private val budgetDao: BudgetDao,
    private val cardDao: CreditCardDao,
    private val balanceDao: OpeningBalanceDao
) {
    // ---------- Writes ----------

    suspend fun addTransaction(transaction: Transaction, reviewThreshold: Double = 5000.0): Long {
        val flagged = transaction.amount >= reviewThreshold
        return txnDao.insert(transaction.copy(flaggedForReview = flagged))
    }

    suspend fun updateTransaction(transaction: Transaction) = txnDao.update(transaction)
    suspend fun deleteTransaction(transaction: Transaction) = txnDao.delete(transaction)

    suspend fun setBudget(categoryKey: String, yearMonth: YearMonth, limit: Double) =
        budgetDao.upsert(Budget(categoryKey, yearMonth.toString(), limit))

    fun observeBudgets(yearMonth: YearMonth): Flow<List<Budget>> =
        budgetDao.getBudgetsForMonth(yearMonth.toString())

    // ---------- Reads / streams ----------

    fun observeTransactions(start: LocalDate, end: LocalDate): Flow<List<Transaction>> =
        txnDao.getTransactionsBetween(start.toEpochDay(), end.toEpochDay())

    // ---------- Reports ----------

    /** Works for Weekly / Monthly / Quarterly / Yearly — just pass the right date range. */
    suspend fun buildPeriodSummary(start: LocalDate, end: LocalDate): PeriodSummary {
        val s = start.toEpochDay(); val e = end.toEpochDay()
        val income = txnDao.getTotal(TransactionType.INCOME, s, e)
        val expense = txnDao.getTotal(TransactionType.EXPENSE, s, e)
        val net = income - expense
        val rate = if (income > 0) (net / income) * 100 else 0.0
        val byCategory = txnDao.getCategoryTotals(TransactionType.EXPENSE, s, e)
        val byMode = txnDao.getPaymentModeTotals(TransactionType.EXPENSE, s, e)
        return PeriodSummary(
            totalIncome = income,
            totalExpense = expense,
            netSavings = net,
            savingsRatePercent = rate,
            byCategory = byCategory,
            byPaymentMode = byMode,
            topExpenseCategories = byCategory.take(5)
        )
    }

    /** Percentage change vs. the previous period of equal length — used for the ↑/↓ comparison rows. */
    suspend fun compareToPreviousPeriod(start: LocalDate, end: LocalDate): Double {
        val lengthDays = end.toEpochDay() - start.toEpochDay() + 1
        val prevEnd = start.minusDays(1)
        val prevStart = prevEnd.minusDays(lengthDays - 1)
        val current = buildPeriodSummary(start, end).totalExpense
        val previous = buildPeriodSummary(prevStart, prevEnd).totalExpense
        return if (previous > 0) ((current - previous) / previous) * 100 else 0.0
    }

    suspend fun weeklyReport(anyDateInWeek: LocalDate): PeriodSummary {
        val wf = WeekFields.ISO
        val start = anyDateInWeek.with(wf.dayOfWeek(), 1L)
        val end = start.plusDays(6)
        return buildPeriodSummary(start, end)
    }

    suspend fun monthlyReport(month: YearMonth): PeriodSummary =
        buildPeriodSummary(month.atDay(1), month.atEndOfMonth())

    suspend fun quarterlyReport(endMonth: YearMonth): List<PeriodSummary> =
        (2 downTo 0).map { offset -> monthlyReport(endMonth.minusMonths(offset.toLong())) }

    suspend fun yearlyReport(year: Int): PeriodSummary =
        buildPeriodSummary(LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31))

    // ---------- Budgets ----------

    /** Rule from spec: baseline = average of last 3 months' actual spend per category. */
    suspend fun suggestedBaselineBudget(categoryKey: String, referenceMonth: YearMonth): Double {
        val totals = (1..3).map { i ->
            val m = referenceMonth.minusMonths(i.toLong())
            txnDao.getCategoryTotals(TransactionType.EXPENSE, m.atDay(1).toEpochDay(), m.atEndOfMonth().toEpochDay())
                .firstOrNull { it.categoryKey == categoryKey }?.total ?: 0.0
        }
        return totals.average()
    }

    /**
     * Builds the Budget vs Actual table, including the >10% over-budget flag from the spec.
     * For a live-updating UI, prefer collecting `observeBudgets()` in the ViewModel and
     * calling `computeBudgetStatus` directly — this suspend variant is for one-off exports.
     */
    suspend fun budgetVsActual(month: YearMonth): List<BudgetStatus> {
        val actuals = txnDao.getCategoryTotals(
            TransactionType.EXPENSE, month.atDay(1).toEpochDay(), month.atEndOfMonth().toEpochDay()
        ).associateBy { it.categoryKey }
        val budgets = budgetDao.getBudgetsForMonth(month.toString()).first()
        return computeBudgetStatus(budgets, actuals)
    }

    fun computeBudgetStatus(budgets: List<Budget>, actuals: Map<String, CategoryTotal>): List<BudgetStatus> =
        budgets.map { b ->
            val actual = actuals[b.categoryKey]?.total ?: 0.0
            val variance = b.limit - actual
            val variancePct = if (b.limit > 0) (variance / b.limit) * 100 else 0.0
            BudgetStatus(
                categoryKey = b.categoryKey,
                budget = b.limit,
                actual = actual,
                variance = variance,
                variancePercent = variancePct,
                isOverBudget = variancePct < -10.0 // "runs over by more than 10%" rule
            )
        }

    /** Categories under budget with room to spare — used to suggest where to trim from. */
    fun suggestTrimSource(statuses: List<BudgetStatus>): BudgetStatus? =
        statuses.filter { !it.isOverBudget && it.variance > 0 }.maxByOrNull { it.variance }

    // ---------- Balances (running balance per payment mode) ----------

    suspend fun closingBalance(mode: PaymentMode, month: YearMonth): Double {
        val opening = balanceDao.getForMonth(month.toString()).firstOrNull { it.paymentMode == mode }?.amount ?: 0.0
        val s = month.atDay(1).toEpochDay(); val e = month.atEndOfMonth().toEpochDay()
        val inflow = txnDao.getIncomeForMode(mode, s, e)
        val outflow = txnDao.getOutflowForMode(mode, s, e)
        return opening + inflow - outflow
    }

    suspend fun rollBalancesForward(fromMonth: YearMonth) {
        val toMonth = fromMonth.plusMonths(1)
        PaymentMode.entries.forEach { mode ->
            val closing = closingBalance(mode, fromMonth)
            balanceDao.upsert(OpeningBalance(mode, toMonth.toString(), closing))
        }
    }

    // ---------- Alerts ----------

    suspend fun highValueTransactionsThisMonth(month: YearMonth, threshold: Double = 5000.0): List<Transaction> =
        txnDao.getTransactionsAboveThreshold(threshold, month.atDay(1).toEpochDay(), month.atEndOfMonth().toEpochDay())

    /** Flags an "unusual spending spike": a category running >40% above its 3-month baseline. */
    suspend fun spendingSpikeAlerts(month: YearMonth): List<String> {
        val actuals = txnDao.getCategoryTotals(TransactionType.EXPENSE, month.atDay(1).toEpochDay(), month.atEndOfMonth().toEpochDay())
        return actuals.mapNotNull { current ->
            val baseline = suggestedBaselineBudget(current.categoryKey, month)
            if (baseline > 0 && current.total > baseline * 1.4) current.categoryKey else null
        }
    }
}
