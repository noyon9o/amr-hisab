package com.example.hisab

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Phone-er moddhei SharedPreferences e JSON hishebe shob data save kore. */
class TxStore(context: Context) {

    private val prefs = context.getSharedPreferences("hisab_store", Context.MODE_PRIVATE)

    var defaultSalary: Double
        get() = prefs.getFloat(KEY_SALARY, 0f).toDouble()
        set(value) {
            prefs.edit().putFloat(KEY_SALARY, value.toFloat()).apply()
        }

    fun load(): List<Transaction> {
        val raw = prefs.getString(KEY_TX, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                Transaction(
                    id = o.getString("id"),
                    type = TxType.valueOf(o.getString("type")),
                    amount = o.getDouble("amount"),
                    category = o.getString("category"),
                    note = o.optString("note", ""),
                    dateMillis = o.getLong("date")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun save(list: List<Transaction>) {
        val arr = JSONArray()
        list.forEach { t ->
            arr.put(
                JSONObject()
                    .put("id", t.id)
                    .put("type", t.type.name)
                    .put("amount", t.amount)
                    .put("category", t.category)
                    .put("note", t.note)
                    .put("date", t.dateMillis)
            )
        }
        prefs.edit().putString(KEY_TX, arr.toString()).apply()
    }

    companion object {
        private const val KEY_TX = "transactions"
        private const val KEY_SALARY = "default_salary"
    }
}
