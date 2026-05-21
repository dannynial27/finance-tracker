package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TransactionViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = TransactionRepository(database.transactionDao())

    val transactions: StateFlow<List<Transaction>> = repository.allTransactions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val sharedPrefs = application.getSharedPreferences("finance_tracker_prefs", Context.MODE_PRIVATE)

    val isUserAuthenticated = MutableStateFlow(!sharedPrefs.getBoolean("biometric_enabled", false))
    val isBiometricEnabled = MutableStateFlow(sharedPrefs.getBoolean("biometric_enabled", false))
    val isDarkMode = MutableStateFlow(sharedPrefs.getBoolean("dark_mode", true)) // Default to dark mode for luxury visual vibe

    fun setAuthenticated(auth: Boolean) {
        isUserAuthenticated.value = auth
    }

    fun setBiometricEnabled(enabled: Boolean) {
        isBiometricEnabled.value = enabled
        sharedPrefs.edit().putBoolean("biometric_enabled", enabled).apply()
        if (!enabled) {
            isUserAuthenticated.value = true
        }
    }

    fun setDarkMode(enabled: Boolean) {
        isDarkMode.value = enabled
        sharedPrefs.edit().putBoolean("dark_mode", enabled).apply()
    }

    fun addTransaction(amount: Double, description: String, category: String, type: TransactionType) {
        viewModelScope.launch {
            repository.insert(
                Transaction(
                    amount = amount,
                    description = description,
                    category = category,
                    type = type,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            repository.delete(id)
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    fun generateCsvContent(): String {
        val list = transactions.value
        val sb = StringBuilder()
        sb.append("ID,Date,Type,Category,Description,Amount (USD)\n")
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        for (tx in list) {
            val dateStr = sdf.format(java.util.Date(tx.timestamp))
            val descSafe = tx.description.replace("\"", "\"\"")
            val catSafe = tx.category.replace("\"", "\"\"")
            sb.append("${tx.id},\"$dateStr\",${tx.type.name},\"$catSafe\",\"$descSafe\",${tx.amount}\n")
        }
        return sb.toString()
    }
}
