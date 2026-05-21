package com.example.data

import com.example.security.CryptographyManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TransactionRepository(private val transactionDao: TransactionDao) {

    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactionsFlow().map { entities ->
        entities.map { entity -> decryptEntity(entity) }
    }

    suspend fun insert(transaction: Transaction) {
        val encryptedEntity = encryptDomain(transaction)
        transactionDao.insert(encryptedEntity)
    }

    suspend fun delete(id: Long) {
        transactionDao.deleteById(id)
    }

    suspend fun clearAll() {
        transactionDao.clearAll()
    }

    private fun encryptDomain(transaction: Transaction): TransactionEntity {
        val encryptedAmount = CryptographyManager.encrypt(transaction.amount.toString())
        val encryptedDescription = CryptographyManager.encrypt(transaction.description)
        val encryptedCategory = CryptographyManager.encrypt(transaction.category)
        return TransactionEntity(
            id = transaction.id,
            encryptedAmount = encryptedAmount,
            encryptedDescription = encryptedDescription,
            encryptedCategory = encryptedCategory,
            transactionType = transaction.type.name,
            timestamp = transaction.timestamp
        )
    }

    private fun decryptEntity(entity: TransactionEntity): Transaction {
        val amountStr = CryptographyManager.decrypt(entity.encryptedAmount)
        val amount = amountStr.toDoubleOrNull() ?: 0.0
        val description = CryptographyManager.decrypt(entity.encryptedDescription)
        val category = CryptographyManager.decrypt(entity.encryptedCategory)
        
        return Transaction(
            id = entity.id,
            amount = amount,
            description = if (description == "Decryption Error" && entity.encryptedDescription.isEmpty()) "" else description,
            category = if (category == "Decryption Error" && entity.encryptedCategory.isEmpty()) "" else category,
            type = try {
                TransactionType.valueOf(entity.transactionType)
            } catch (e: Exception) {
                TransactionType.EXPENSE
            },
            timestamp = entity.timestamp
        )
    }
}
