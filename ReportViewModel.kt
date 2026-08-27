package com.moneymanager.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneymanager.app.data.BudgetStatus
import com.moneymanager.app.data.CategoryTotal
import com.moneymanager.app.data.PeriodSummary
import com.moneymanager.app.repository.MoneyRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

class ReportViewModel(private val repo: MoneyRepository) : ViewModel() {

    private val _month = MutableStateFlow(YearMonth.now())
    val month: StateFlow<YearMonth> = _month

    private val _monthlySummary = MutableStateFlow<PeriodSummary?>(null)
    val monthlySummary: StateFlow<PeriodSummary?> = _monthlySummary

    private val _weeklySummary = MutableStateFlow<PeriodSummary?>(null)
    val weeklySummary: StateFlow<PeriodSummary?> = _weeklySummary

    private val _quarterlyTrend = MutableStateFlow<List<PeriodSummary>>(emptyList())
    val quarterlyTrend: StateFlow<List<PeriodSummary>> = _quarterlyTrend

    private val _vsPreviousPercent = MutableStateFlow(0.0)
    val vsPreviousPercent: StateFlow<Double> = _vsPreviousPercent

    private val _budgetStatuses = MutableStateFlow<List<BudgetStatus>>(emptyList())
    val budgetStatuses: StateFlow<List<BudgetStatus>> = _budgetStatuses

    private val _spikeAlerts = MutableStateFlow<List<String>>(emptyList())
    val spikeAlerts: StateFlow<List<String>> = _spikeAlerts

    init { refresh() }

    fun setMonth(newMonth: YearMonth) {
        _month.value = newMonth
        refresh()
    }

    fun refresh() {
        val ym = _month.value
        viewModelScope.launch {
            _monthlySummary.value = repo.monthlyReport(ym)
            _weeklySummary.value = repo.weeklyReport(LocalDate.now())
            _quarterlyTrend.value = repo.quarterlyReport(ym)
            _vsPreviousPercent.value = repo.compareToPreviousPeriod(ym.atDay(1), ym.atEndOfMonth())
            _budgetStatuses.value = repo.budgetVsActual(ym)
            _spikeAlerts.value = repo.spendingSpikeAlerts(ym)
        }
    }

    fun setBudget(categoryKey: String, limit: Double) {
        viewModelScope.launch {
            repo.setBudget(categoryKey, _month.value, limit)
            refresh()
        }
    }

    /** Category with the most unused budget — for the "trim from here" suggestion. */
    fun trimSuggestion(): BudgetStatus? = repo.suggestTrimSource(_budgetStatuses.value)
}
