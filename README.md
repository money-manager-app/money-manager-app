# Personal Money Manager — Android App

A native Android app (Kotlin + Jetpack Compose + Room) built from your
`Personal_Money_Manager` spec and report. All data is stored **locally on
the device** (no server/backend included) — nothing is uploaded anywhere.

## What's implemented

| Spec section | Status |
|---|---|
| 6 payment modes (Cash, Credit Card, Debit Card, Wallet, UPI, Net Banking) | ✅ full enum + tracked per transaction |
| Transaction fields (date, type, category, sub-category, mode, amount, note, recurring) | ✅ `Transaction` entity + Add Transaction screen |
| Income categories | ✅ all 7 from spec |
| Expense categories (30, grouped into 11 groups) | ✅ all included, matches your PDF's category list |
| Weekly / Monthly / Quarterly reports | ✅ `MoneyRepository` + Reports screen (tabs) |
| Yearly report | ✅ `repo.yearlyReport()` (wire up a screen tab the same way as the others) |
| Category & payment-mode % breakdown | ✅ Dashboard + Reports |
| Top 5 expense categories | ✅ Dashboard |
| vs. previous period ↑/↓ % | ✅ Dashboard |
| Budget fixing (3-month baseline average) | ✅ `suggestedBaselineBudget()` |
| Budget vs Actual table + >10% over-budget flag | ✅ Budget screen |
| "Which category to trim from" suggestion | ✅ `suggestTrimSource()` |
| 50/30/20 sanity check | ✅ `toBudgetBucket()` mapping — bucket the category totals and compare to income in a small UI addition |
| High-value transaction flag (₹5,000 default) | ✅ `flaggedForReview` on every transaction |
| Credit card due-date tracking | ✅ `CreditCard` entity — add a "Cards" screen to list them with reminders |
| Running balance per payment mode / month rollover | ✅ `OpeningBalance` entity + `MonthRolloverWorker` |
| Unusual spending spike alert | ✅ `spendingSpikeAlerts()` (>40% over 3-month baseline) |
| Charts (pie/bar) | ⚠️ Vico chart library is added as a dependency; the current screens use simple progress-bar breakdowns. Swap in `Vico` `Chart` composables in `DashboardScreen`/`ReportsScreen` for pie/bar visuals if you want them. |
| Excel/CSV import of your existing data | ❌ Not built — say the word if you want an importer for your uploaded `.xlsx` file's format |

## Project structure

```
MoneyManagerApp/
├── app/
│   ├── build.gradle.kts          # dependencies (Compose, Room, Vico, WorkManager)
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/moneymanager/app/
│       │   ├── MainActivity.kt
│       │   ├── data/              # Room entities, DAOs, database, enums
│       │   ├── repository/        # MoneyRepository — all report/budget logic
│       │   ├── viewmodel/         # TransactionViewModel, ReportViewModel
│       │   ├── ui/screens/        # Dashboard, AddTransaction, Transactions, Reports, Budget
│       │   ├── ui/components/     # Reusable cards, bars, alert banners
│       │   ├── ui/theme/          # Colors, Material3 theme
│       │   ├── navigation/        # Bottom-nav + NavHost wiring
│       │   └── util/              # Month-end balance rollover (WorkManager)
│       └── res/                   # strings, themes, launcher icon, backup rules
├── build.gradle.kts, settings.gradle.kts, gradle.properties
└── gradle/wrapper/gradle-wrapper.properties
```

## How to build it

You'll need **Android Studio** (free, from developer.android.com/studio) —
this can't be compiled inside this chat sandbox since it requires the
Android SDK/emulator toolchain.

1. Unzip the project and open the folder in Android Studio (`File → Open`).
2. Let Gradle sync (it will download the Gradle wrapper + dependencies —
   first sync needs internet).
3. Generate a proper launcher icon: `Right-click app/res → New → Image Asset`
   and pick any icon/text you like (a placeholder vector icon is included
   so the project builds without this step, but you'll want a real one
   before publishing).
4. Run on an emulator or your phone: click the green ▶ Run button.

## Signing & publishing to the Play Store

1. **Create a signing key** (one-time):
   `Build → Generate Signed Bundle / APK → Android App Bundle`, then
   "Create new..." keystore. Store the `.jks` file and its passwords
   somewhere safe — losing it means you can never update the app again
   under the same listing.
2. **Build a release AAB**: same dialog, choose `release` build variant.
   This produces a `.aab` file (Play Store requires App Bundles, not APKs,
   for new apps).
3. **Google Play Console**: create an account (one-time $25 USD registration
   fee) at play.google.com/console, create a new app, fill in the store
   listing (screenshots, description, privacy policy — required even for
   an app with no backend, since it collects financial data locally),
   complete the Data Safety form (declare that financial data is stored
   locally and not shared), upload the `.aab` under
   `Release → Production → Create new release`.
4. **Content rating & app content questionnaires**: fill these in the
   Play Console; a finance-tracking app with no ads/analytics is
   straightforward here.
5. Submit for review. Google typically takes a few hours to a few days
   for a first review.

## Extending it further

- Add the **Yearly report tab** to `ReportsScreen.kt` the same way the
  other three tabs are wired (the repository method `yearlyReport()`
  already exists).
- Add a **Credit Cards screen** (list/add/edit `CreditCard` rows) and a
  notification via WorkManager a few days before each card's `dueDay`.
- Swap the progress-bar category breakdowns for real pie/bar charts using
  the already-added Vico library.
- If you want your existing Excel data imported on first launch, share
  the exact column layout of your workbook's transaction sheet and an
  importer can be added.

## Data & privacy

Everything is stored in a local Room (SQLite) database on the device.
There's no analytics or ad SDK included. If you add any (crash reporting,
etc.) before publishing, disclose it in the Play Console Data Safety form.
