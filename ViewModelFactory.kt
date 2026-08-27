package com.moneymanager.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.moneymanager.app.repository.MoneyRepository

class ViewModelFactory(private val repo: MoneyRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when (modelClass) {
        TransactionViewModel::class.java -> TransactionViewModel(repo) as T
        ReportViewModel::class.java -> ReportViewModel(repo) as T
        else -> throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
    }
}
