package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType {
    INCOME, EXPENSE
}

data class Transaction(
    val id: Long = 0,
    val amount: Double,
    val description: String,
    val category: String,
    val type: TransactionType,
    val timestamp: Long
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val encryptedAmount: String,
    val encryptedDescription: String,
    val encryptedCategory: String,
    val transactionType: String, // "INCOME" or "EXPENSE"
    val timestamp: Long
)
