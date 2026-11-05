package com.example.hisab

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.Instant
import java.time.ZoneId

private val IncomeGreen = Color(0xFF0B6E4F)
private val ExpenseRed = Color(0xFFC0392B)

// App er nam ar watermark er lekha ekhane bodlano jabe
private const val APP_TITLE = "পই পই হিসাব"
private const val WATERMARK_TEXT = "neyon neyon neyon :)"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HisabApp(vm: HisabViewModel = viewModel()) {
    var showAdd by remember { mutableStateOf(false) }
    var showSalary by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(APP_TITLE) },
                    actions = { TextButton(onClick = { showSalary = true }) { Text("বেতন সেটআপ") } }
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { showAdd = true },
                    text = { Text("+ নতুন এন্ট্রি") },
                    icon = {}
                )
            }
        ) { padding ->
            val list = vm.monthTransactions
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { MonthSwitcher(vm) }
                item { SummaryCard(vm) }
                item { CategoryBreakdown(vm) }
                item {
                    Text(
                        "লেনদেনের তালিকা",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (list.isEmpty()) {
                    item {
                        Text(
                            "এই মাসে কোনো এন্ট্রি নেই। নিচের বাটনে চাপ দিয়ে যোগ করুন।",
                            color = Color.Gray
                        )
                    }
                } else {
                    items(list, key = { it.id }) { tx ->
                        TxRow(tx, onDelete = { vm.deleteTransaction(tx.id) })
                    }
                }
            }
        }

        // Watermark: bam-er niche
        Text(
            text = WATERMARK_TEXT,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 12.dp, bottom = 12.dp),
            color = Color.Gray.copy(alpha = 0.55f),
            style = MaterialTheme.typography.labelSmall
        )
    }

    if (showAdd) {
        AddTransactionDialog(vm = vm, onDismiss = { showAdd = false })
    }
    if (showSalary) {
        SalaryDialog(vm = vm, onDismiss = { showSalary = false })
    }
}

@Composable
private fun MonthSwitcher(vm: HisabViewModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TextButton(onClick = { vm.previousMonth() }) { Text("◀") }
        Text(
            "${vm.month.monthValue} / ${vm.month.year}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        TextButton(onClick = { vm.nextMonth() }) { Text("▶") }
    }
}

@Composable
private fun SummaryCard(vm: HisabViewModel) {
    val income = vm.totalIncome
    val expense = vm.totalExpense
    val balance = vm.balance
    val spentPercent = if (income > 0) (expense / income * 100).toInt() else 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("বর্তমান ব্যালেন্স", style = MaterialTheme.typography.labelLarge)
            Text(
                formatMoney(balance),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = if (balance >= 0) IncomeGreen else ExpenseRed
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("মোট আয়", style = MaterialTheme.typography.labelMedium)
                    Text(formatMoney(income), color = IncomeGreen, fontWeight = FontWeight.SemiBold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("মোট খরচ", style = MaterialTheme.typography.labelMedium)
                    Text(formatMoney(expense), color = ExpenseRed, fontWeight = FontWeight.SemiBold)
                }
            }
            if (income > 0) {
                LinearProgressIndicator(
                    progress = { (expense / income).toFloat().coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth(),
                    color = if (expense > income) ExpenseRed else IncomeGreen
                )
                Text("আয়ের $spentPercent% খরচ হয়েছে", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun CategoryBreakdown(vm: HisabViewModel) {
    val data = vm.expenseByCategory
    if (data.isEmpty()) return
    val total = vm.totalExpense

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                "কোথায় কত খরচ",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            data.forEach { (cat, amount) ->
                Column {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(cat)
                        Text(formatMoney(amount))
                    }
                    LinearProgressIndicator(
                        progress = { (amount / total).toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        color = ExpenseRed
                    )
                }
            }
        }
    }
}

@Composable
private fun TxRow(tx: Transaction, onDelete: () -> Unit) {
    val day = Instant.ofEpochMilli(tx.dateMillis).atZone(ZoneId.systemDefault()).dayOfMonth
    val isIncome = tx.type == TxType.INCOME

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(tx.category, fontWeight = FontWeight.SemiBold)
                val sub = if (tx.note.isBlank()) "তারিখ: $day" else "তারিখ: $day • ${tx.note}"
                Text(sub, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Text(
                (if (isIncome) "+" else "-") + formatMoney(tx.amount),
                color = if (isIncome) IncomeGreen else ExpenseRed,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onDelete) { Text("মুছুন") }
        }
    }
}

@Composable
private fun AddTransactionDialog(vm: HisabViewModel, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var type by remember { mutableStateOf(TxType.EXPENSE) }
    var amountText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(expenseCategories.first()) }
    var customCategory by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var dayText by remember { mutableStateOf(java.time.LocalDate.now().dayOfMonth.toString()) }

    val defaults = if (type == TxType.EXPENSE) expenseCategories else incomeCategories
    // Age nijer lekha category gulo o chip hishebe dekhabe
    val saved = vm.transactions
        .filter { it.type == type }
        .map { it.category }
        .distinct()
        .filter { it !in defaults }
    val categories = defaults + saved

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("নতুন এন্ট্রি") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = type == TxType.EXPENSE,
                        onClick = {
                            type = TxType.EXPENSE
                            category = expenseCategories.first()
                            customCategory = ""
                        },
                        label = { Text("খরচ") }
                    )
                    FilterChip(
                        selected = type == TxType.INCOME,
                        onClick = {
                            type = TxType.INCOME
                            category = incomeCategories.first()
                            customCategory = ""
                        },
                        label = { Text("আয়") }
                    )
                }
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("টাকার পরিমাণ") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                Text("ক্যাটাগরি", style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = customCategory.isBlank() && category == cat,
                            onClick = {
                                category = cat
                                customCategory = ""
                            },
                            label = { Text(cat) }
                        )
                    }
                }
                OutlinedTextField(
                    value = customCategory,
                    onValueChange = { customCategory = it },
                    label = { Text("অথবা নিজের ক্যাটাগরি লিখুন") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("নোট (ঐচ্ছিক)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dayText,
                    onValueChange = { dayText = it.filter { c -> c.isDigit() }.take(2) },
                    label = { Text("মাসের কত তারিখ") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(160.dp)
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val amount = amountText.toDoubleOrNull()
                if (amount == null || amount <= 0.0) {
                    Toast.makeText(context, "সঠিক টাকার পরিমাণ লিখুন", Toast.LENGTH_SHORT).show()
                } else {
                    val day = dayText.toIntOrNull() ?: 1
                    // Nijer lekha thakle seta, na hole chip e select kora ta
                    val finalCategory = customCategory.trim().ifBlank { category }
                    vm.addTransaction(type, amount, finalCategory, note, day)
                    onDismiss()
                }
            }) { Text("সংরক্ষণ") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("বাতিল") } }
    )
}

@Composable
private fun SalaryDialog(vm: HisabViewModel, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var salaryText by remember {
        mutableStateOf(if (vm.defaultSalary > 0) vm.defaultSalary.toLong().toString() else "")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("মাসিক বেতন") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("একবার বেতন সেট করে রাখলে প্রতি মাসে এক ট্যাপেই আয় হিসেবে যোগ করতে পারবেন।")
                OutlinedTextField(
                    value = salaryText,
                    onValueChange = { salaryText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("বেতনের পরিমাণ") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedButton(
                    onClick = {
                        val value = salaryText.toDoubleOrNull()
                        if (value == null || value <= 0.0) {
                            Toast.makeText(context, "সঠিক বেতন লিখুন", Toast.LENGTH_SHORT).show()
                        } else {
                            vm.saveDefaultSalary(value)
                            val added = vm.addSalaryToMonth()
                            val msg = if (added) "এই মাসে বেতন যোগ হয়েছে" else "এই মাসে বেতন আগেই যোগ করা আছে"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            onDismiss()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("সেভ করে এই মাসে যোগ করুন") }
            }
        },
        confirmButton = {
            Button(onClick = {
                val value = salaryText.toDoubleOrNull()
                if (value == null || value <= 0.0) {
                    Toast.makeText(context, "সঠিক বেতন লিখুন", Toast.LENGTH_SHORT).show()
                } else {
                    vm.saveDefaultSalary(value)
                    onDismiss()
                }
            }) { Text("শুধু সেভ") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("বাতিল") } }
    )
}
