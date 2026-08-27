package com.moneymanager.app.data

/** The six payment modes tracked by the spec. */
enum class PaymentMode(val label: String) {
    CASH("Cash"),
    CREDIT_CARD("Credit Card"),
    DEBIT_CARD("Debit Card"),
    WALLET("Wallet"),
    UPI("UPI"),
    NET_BANKING("Net Banking")
}

enum class TransactionType(val label: String) {
    INCOME("Income"),
    EXPENSE("Expense"),
    TRANSFER("Transfer")
}

enum class IncomeCategory(val label: String) {
    SALARY("Salary"),
    BUSINESS_FREELANCE("Business/Freelance Income"),
    INTEREST("Interest (Savings/FD/RD)"),
    DIVIDENDS("Dividends/Investment Returns"),
    RENTAL("Rental Income"),
    GIFTS_CASHBACK("Gifts/Cashback/Refunds"),
    OTHER("Other Income")
}

/** Grouped as in the spec: group name -> list of categories in that group. */
enum class ExpenseCategory(val label: String, val group: ExpenseGroup) {
    GROCERIES("Groceries", ExpenseGroup.ESSENTIALS),
    RENT_EMI("Rent/EMI", ExpenseGroup.ESSENTIALS),
    ELECTRICITY_WATER_GAS("Utilities", ExpenseGroup.ESSENTIALS),
    MOBILE_INTERNET("Mobile/Internet", ExpenseGroup.ESSENTIALS),

    FUEL("Fuel", ExpenseGroup.TRANSPORT),
    PUBLIC_TRANSPORT("Public Transport", ExpenseGroup.TRANSPORT),
    CAB_AUTO("Cab/Auto", ExpenseGroup.TRANSPORT),
    VEHICLE_MAINTENANCE("Vehicle Maintenance", ExpenseGroup.TRANSPORT),

    RESTAURANTS("Restaurants", ExpenseGroup.FOOD_DINING),
    FOOD_DELIVERY("Food Delivery", ExpenseGroup.FOOD_DINING),
    TEA_SNACKS("Tea/Snacks", ExpenseGroup.FOOD_DINING),

    MEDICAL("Medical", ExpenseGroup.HEALTH),
    PHARMACY("Pharmacy", ExpenseGroup.HEALTH),
    INSURANCE_PREMIUMS("Insurance Premiums", ExpenseGroup.HEALTH),
    FITNESS("Fitness", ExpenseGroup.HEALTH),

    CLOTHING("Clothing", ExpenseGroup.SHOPPING),
    ELECTRONICS("Electronics", ExpenseGroup.SHOPPING),
    PERSONAL_CARE("Personal Care", ExpenseGroup.SHOPPING),

    MOVIES("Movies", ExpenseGroup.ENTERTAINMENT),
    SUBSCRIPTIONS("Subscriptions (OTT, Music)", ExpenseGroup.ENTERTAINMENT),
    HOBBIES("Hobbies", ExpenseGroup.ENTERTAINMENT),

    EDUCATION_FEES("Education Fees", ExpenseGroup.EDUCATION),
    BOOKS_COURSES("Books/Courses", ExpenseGroup.EDUCATION),

    SIP_FD_STOCKS_GOLD("Investments/Savings (SIP/FD/Stocks/Gold)", ExpenseGroup.INVESTMENTS),
    PF_CONTRIBUTIONS("PF Contributions", ExpenseGroup.INVESTMENTS),

    CREDIT_CARD_BILL("Credit Card Bill Payment", ExpenseGroup.DEBT),
    LOAN_EMI("Loan EMI", ExpenseGroup.DEBT),

    GIFTS_GIVEN("Gifts Given", ExpenseGroup.FAMILY_SOCIAL),
    FESTIVALS("Festivals", ExpenseGroup.FAMILY_SOCIAL),
    DONATIONS("Donations", ExpenseGroup.FAMILY_SOCIAL),

    MISCELLANEOUS("Miscellaneous", ExpenseGroup.MISC)
}

enum class ExpenseGroup(val label: String) {
    ESSENTIALS("Essentials"),
    TRANSPORT("Transport"),
    FOOD_DINING("Food & Dining"),
    HEALTH("Health"),
    SHOPPING("Shopping"),
    ENTERTAINMENT("Entertainment"),
    EDUCATION("Education"),
    INVESTMENTS("Investments/Savings"),
    DEBT("Debt Repayment"),
    FAMILY_SOCIAL("Family/Social"),
    MISC("Miscellaneous")
}

/** 50/30/20 bucket used for the budgeting sanity check in the spec. */
enum class BudgetBucket { NEEDS, WANTS, SAVINGS_INVESTMENTS }

fun ExpenseGroup.toBudgetBucket(): BudgetBucket = when (this) {
    ExpenseGroup.ESSENTIALS, ExpenseGroup.HEALTH, ExpenseGroup.DEBT -> BudgetBucket.NEEDS
    ExpenseGroup.TRANSPORT, ExpenseGroup.FOOD_DINING, ExpenseGroup.SHOPPING,
    ExpenseGroup.ENTERTAINMENT, ExpenseGroup.FAMILY_SOCIAL, ExpenseGroup.MISC -> BudgetBucket.WANTS
    ExpenseGroup.EDUCATION -> BudgetBucket.WANTS
    ExpenseGroup.INVESTMENTS -> BudgetBucket.SAVINGS_INVESTMENTS
}
