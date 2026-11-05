# পই পই হিসাব – Salary & Expense Tracker

Kotlin + Jetpack Compose diye banano ekta simple Android app.

## Features
- Mash onujayi hisab (◀ ▶ diye mash change)
- Monthly beton set kore ek tap e income hishebe add
- Income (beton, bonus, freelance) ar khoros (khabar, bhara, jatayat, bill, etc.) entry
- Nijer icche moto notun category lekha jay, porer bar chip hishebe dekha jay
- Balance, mot income, mot khoros, "income er koto % khoros hoyeche"
- Category onujayi khoros breakdown (progress bar shoho)
- Entry delete kora
- Bam-er niche watermark
- Shob data phone e save thake (SharedPreferences), internet lage na

## Run korbe kibhabe
1. Android Studio (Koala ba notun) install koro
2. File > Open diye `hisab` folder ta select koro
3. Gradle sync hoye gele phone/emulator e Run chapo
   (minSdk 26 = Android 8.0+)

## Nam, watermark, icon kothay bodlabe
- App er nam: `app/src/main/AndroidManifest.xml` er `android:label` ar `HisabScreen.kt` er `APP_TITLE`
- Watermark: `HisabScreen.kt` er upore `WATERMARK_TEXT`
- Icon: app folder e right click > New > Image Asset, tarpor manifest e
  `android:icon="@mipmap/ic_launcher"` ar `android:roundIcon="@mipmap/ic_launcher_round"` add koro

## File structure
- `Models.kt` – Transaction model, category list, taka format
- `TxStore.kt` – data save/load
- `HisabViewModel.kt` – hisab-er logic
- `HisabScreen.kt` – shob UI (summary, list, dialog, watermark)
- `MainActivity.kt` – app entry point

## Pore add korte paro
Edit entry, budget limit, CSV export, dark mode, Room database, PIN lock.
