package com.example.hisab

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId

class HisabViewModel(app: Application) : AndroidViewModel(app) {

    private val store = TxStore(app)
    private val zone: ZoneId = ZoneId.systemDefault()

    val transactions = mutableStateListOf<Transaction>().apply { addAll(store.load()) }

    var month by mutableStateOf(YearMonth.now())
        private set

    var defaultSalary by mutableStateOf(store.defaultSalary)
        private set

    // ---- Month navigation ----
    fun previousMonth() { month = month.minusMonths(1) }
    fun nextMonth() { month = month.plusMonths(1) }

    // ---- Derived values (current month) ----
    private fun monthOf(t: Transaction): YearMonth =
        YearMonth.from(Instant.ofEpochMilli(t.dateMillis).atZone(zone))

    val monthTransactions: List<Transaction>
        get() = transactions.filter { monthOf(it) == month }.sortedByDescending { it.dateMillis }

    val totalIncome: Double
        get() = monthTransactions.filter { it.type == TxType.INCOME }.sumOf { it.amount }

    val totalExpense: Double
        get() = monthTransactions.filter { it.type == TxType.EXPENSE }.sumOf { it.amount }

    val balance: Double get() = totalIncome - totalExpense

    /** Category onujayi khoros, beshi theke kom. */
    val expenseByCategory: List<Pair<String, Double>>
        get() = monthTransactions
            .filter { it.type == TxType.EXPENSE }
            .groupBy { it.category }
            .map { (cat, list) -> cat to list.sumOf { it.amount } }
            .sortedByDescending { it.second }

    // ---- Actions ----
    fun addTransaction(type: TxType, amount: Double, category: String, note: String, day: Int) {
        val safeDay = day.coerceIn(1, month.lengthOfMonth())
        val millis = month.atDay(safeDay).atStartOfDay(zone).toInstant().toEpochMilli()
        transactions.add(
            Transaction(type = type, amount = amount, category = category, note = note.trim(), dateMillis = millis)
        )
        store.save(transactions.toList())
    }

    fun deleteTransaction(id: String) {
        transactions.removeAll { it.id == id }
        store.save(transactions.toList())
    }

    fun saveDefaultSalary(value: Double) {
        defaultSalary = value
        store.defaultSalary = value
    }

    /** Ei mashe beton add kore. Age theke thakle false dey (double add hobe na). */
    fun addSalaryToMonth(): Boolean {
        val alreadyAdded = monthTransactions.any {
            it.type == TxType.INCOME && it.category == SALARY_CATEGORY
        }
        if (alreadyAdded || defaultSalary <= 0.0) return false
        addTransaction(TxType.INCOME, defaultSalary, SALARY_CATEGORY, "মাসিক বেতন", 1)
        return true
    }
}
