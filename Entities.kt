package com.moneymanager.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * One row per transaction. Mirrors the "Transaction Fields to Capture" table in the spec:
 * Date, Type, Category, Sub-category, Payment Mode, Amount, Note, Recurring.
 */
@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val type: TransactionType,
    /** Stores IncomeCategory.name or ExpenseCategory.name depending on `type`. */
    val categoryKey: String,
    val subCategory: String? = null,
    val paymentMode: PaymentMode,
    val amount: Double,
    val note: String = "",
    val isRecurring: Boolean = false,
    /** Only meaningful when paymentMode == CREDIT_CARD. */
    val creditCardId: Long? = null,
    /** Flags the "single transaction above threshold" review rule from the spec. */
    val flaggedForReview: Boolean = false
)

/** Lets the user track more than one credit card, each with its own due date/cycle. */
@Entity(tableName = "credit_cards")
data class CreditCard(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cardName: String,
    val statementDay: Int,   // day of month the statement is generated
    val dueDay: Int,         // day of month payment is due
    val creditLimit: Double? = null
)

/**
 * Fixed monthly budget per category, per the "Budget Estimation & Fixing" section.
 * yearMonth is stored as "yyyy-MM" so each month can have its own fixed limits.
 */
@Entity(tableName = "budgets", primaryKeys = ["categoryKey", "yearMonth"])
data class Budget(
    val categoryKey: String,
    val yearMonth: String,
    val limit: Double
)

/** User-set per-payment-mode opening balances (cash in hand, wallet balance, etc). */
@Entity(tableName = "opening_balances", primaryKeys = ["paymentMode", "yearMonth"])
data class OpeningBalance(
    val paymentMode: PaymentMode,
    val yearMonth: String,
    val amount: Double
)
