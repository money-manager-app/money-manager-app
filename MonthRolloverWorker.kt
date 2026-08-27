package com.moneymanager.app.util

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.moneymanager.app.data.AppDatabase
import com.moneymanager.app.repository.MoneyRepository
import java.time.YearMonth

class MonthRolloverWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val db = AppDatabase.getInstance(applicationContext)
        val repo = MoneyRepository(db.transactionDao(), db.budgetDao(), db.creditCardDao(), db.openingBalanceDao())
        repo.rollBalancesForward(YearMonth.now().minusMonths(1))
        return Result.success()
    }
}
