package com.moneymanager.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.moneymanager.app.data.AppDatabase
import com.moneymanager.app.navigation.MoneyManagerNavHost
import com.moneymanager.app.repository.MoneyRepository
import com.moneymanager.app.ui.theme.MoneyManagerTheme
import com.moneymanager.app.viewmodel.ReportViewModel
import com.moneymanager.app.viewmodel.TransactionViewModel
import com.moneymanager.app.viewmodel.ViewModelFactory

class MainActivity : ComponentActivity() {

    private val repository: MoneyRepository by lazy {
        val db = AppDatabase.getInstance(applicationContext)
        MoneyRepository(db.transactionDao(), db.budgetDao(), db.creditCardDao(), db.openingBalanceDao())
    }

    private val factory by lazy { ViewModelFactory(repository) }
    private val transactionViewModel: TransactionViewModel by viewModels { factory }
    private val reportViewModel: ReportViewModel by viewModels { factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MoneyManagerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MoneyManagerNavHost(transactionViewModel, reportViewModel)
                }
            }
        }
    }
}
