package com.moneymanager.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneymanager.app.data.*
import com.moneymanager.app.repository.MoneyRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

class TransactionViewModel(private val repo: MoneyRepository) : ViewModel() {

    private val currentMonth = MutableStateFlow(YearMonth.now())

    val transactionsThisMonth: StateFlow<List<Transaction>> = currentMonth
        .flatMapLatest { ym -> repo.observeTransactions(ym.atDay(1), ym.atEndOfMonth()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setMonth(month: YearMonth) { currentMonth.value = month }

    fun addTransaction(
        date: LocalDate,
        type: TransactionType,
        categoryKey: String,
        subCategory: String?,
        paymentMode: PaymentMode,
        amount: Double,
        note: String,
        isRecurring: Boolean,
        creditCardId: Long? = null
    ) {
        viewModelScope.launch {
            repo.addTransaction(
                Transaction(
                    date = date, type = type, categoryKey = categoryKey, subCategory = subCategory,
                    paymentMode = paymentMode, amount = amount, note = note,
                    isRecurring = isRecurring, creditCardId = creditCardId
                )
            )
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch { repo.deleteTransaction(transaction) }
    }
}
