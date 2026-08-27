package com.moneymanager.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert
    suspend fun insert(transaction: Transaction): Long

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    @Query("SELECT * FROM transactions WHERE date BETWEEN :startEpochDay AND :endEpochDay ORDER BY date DESC, id DESC")
    fun getTransactionsBetween(startEpochDay: Long, endEpochDay: Long): Flow<List<Transaction>>

    @Query("""
        SELECT categoryKey, SUM(amount) as total FROM transactions
        WHERE type = :type AND date BETWEEN :startEpochDay AND :endEpochDay
        GROUP BY categoryKey ORDER BY total DESC
    """)
    suspend fun getCategoryTotals(type: TransactionType, startEpochDay: Long, endEpochDay: Long): List<CategoryTotal>

    @Query("""
        SELECT paymentMode, SUM(amount) as total FROM transactions
        WHERE type = :type AND date BETWEEN :startEpochDay AND :endEpochDay
        GROUP BY paymentMode
    """)
    suspend fun getPaymentModeTotals(type: TransactionType, startEpochDay: Long, endEpochDay: Long): List<PaymentModeTotal>

    @Query("SELECT COALESCE(SUM(amount),0) FROM transactions WHERE type = :type AND date BETWEEN :startEpochDay AND :endEpochDay")
    suspend fun getTotal(type: TransactionType, startEpochDay: Long, endEpochDay: Long): Double

    @Query("""
        SELECT date as epochDay,
               COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END),0) as income,
               COALESCE(SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END),0) as expense
        FROM transactions WHERE date BETWEEN :startEpochDay AND :endEpochDay
        GROUP BY date ORDER BY date ASC
    """)
    suspend fun getDailyTotals(startEpochDay: Long, endEpochDay: Long): List<DayTotal>

    @Query("SELECT * FROM transactions WHERE amount >= :threshold AND date BETWEEN :startEpochDay AND :endEpochDay ORDER BY date DESC")
    suspend fun getTransactionsAboveThreshold(threshold: Double, startEpochDay: Long, endEpochDay: Long): List<Transaction>

    @Query("SELECT COALESCE(SUM(amount),0) FROM transactions WHERE paymentMode = :mode AND type = 'INCOME' AND date BETWEEN :startEpochDay AND :endEpochDay")
    suspend fun getIncomeForMode(mode: PaymentMode, startEpochDay: Long, endEpochDay: Long): Double

    @Query("SELECT COALESCE(SUM(amount),0) FROM transactions WHERE paymentMode = :mode AND type != 'INCOME' AND date BETWEEN :startEpochDay AND :endEpochDay")
    suspend fun getOutflowForMode(mode: PaymentMode, startEpochDay: Long, endEpochDay: Long): Double
}

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets WHERE yearMonth = :yearMonth")
    fun getBudgetsForMonth(yearMonth: String): Flow<List<Budget>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(budget: Budget)

    @Query("DELETE FROM budgets WHERE categoryKey = :categoryKey AND yearMonth = :yearMonth")
    suspend fun delete(categoryKey: String, yearMonth: String)
}

@Dao
interface CreditCardDao {
    @Query("SELECT * FROM credit_cards ORDER BY dueDay ASC")
    fun getAll(): Flow<List<CreditCard>>

    @Insert
    suspend fun insert(card: CreditCard): Long

    @Update
    suspend fun update(card: CreditCard)

    @Delete
    suspend fun delete(card: CreditCard)
}

@Dao
interface OpeningBalanceDao {
    @Query("SELECT * FROM opening_balances WHERE yearMonth = :yearMonth")
    suspend fun getForMonth(yearMonth: String): List<OpeningBalance>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(balance: OpeningBalance)
}
