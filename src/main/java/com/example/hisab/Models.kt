package com.example.hisab

import java.util.Locale
import java.util.UUID

enum class TxType { INCOME, EXPENSE }

data class Transaction(
    val id: String = UUID.randomUUID().toString(),
    val type: TxType,
    val amount: Double,
    val category: String,
    val note: String,
    val dateMillis: Long
)

const val SALARY_CATEGORY = "বেতন"

val incomeCategories = listOf(SALARY_CATEGORY, "বোনাস", "ফ্রিল্যান্স", "অন্যান্য আয়")

val expenseCategories = listOf(
    "খাবার", "বাসা ভাড়া", "যাতায়াত", "বিল", "চিকিৎসা",
    "শিক্ষা", "কেনাকাটা", "বিনোদন", "সঞ্চয়", "অন্যান্য"
)

fun formatMoney(value: Double): String {
    val negative = value < 0
    val abs = kotlin.math.abs(value)
    val text = if (abs % 1.0 == 0.0) {
        String.format(Locale.US, "%,.0f", abs)
    } else {
        String.format(Locale.US, "%,.2f", abs)
    }
    return (if (negative) "-" else "") + "৳" + text
}
