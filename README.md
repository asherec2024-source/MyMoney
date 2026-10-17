# MyMoney — Native Android development branch

This branch replaces the HTML/WebView prototype with a **Kotlin + Jetpack Compose** interface backed by a **Room/SQLite** database.

## Implemented in this initial native milestone
- Clean first launch: no preloaded finance records
- Accounts: create, edit, delete (bank, cash wallet, credit card)
- Payment methods: create, edit, delete (user-defined)
- Transactions: create, edit, delete, linked to an account and optional payment method
- Basic dashboard and category expense summaries derived from stored transactions
- Local offline database; no Internet permission or WebView

## Not yet implemented
- Full transfer and credit-card settlement accounting
- Shared credit limit across cards
- Automatic background EMI processing
- EMIs, budgets, goals, reminders, backup/import and advanced reports

This branch is **not production-ready**. It must pass GitHub Actions and be tested on a device before merging. Existing WebView localStorage data is **not migrated** automatically.

## Build
Run `gradle assembleDebug` using Gradle 8.7, Java 17 and Android SDK 35, or use GitHub Actions.
