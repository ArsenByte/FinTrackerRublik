package com.education.financetrackerrublik.data.dao

import androidx.room.*
import com.education.financetrackerrublik.data.model.Transaction
import com.education.financetrackerrublik.data.model.TransactionType
import java.util.Date
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun getTransactionsByDateRange(startDate: Date, endDate: Date): List<Transaction>

    @Query("SELECT * FROM transactions WHERE date >= :today ORDER BY date DESC")
    fun getTodayTransactions(today: Date): List<Transaction>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = :type AND date >= :startDate AND date <= :endDate")
    fun getTotalByType(type: TransactionType, startDate: Date, endDate: Date): Double?

    @Query("SELECT COUNT(*) FROM transactions WHERE categoryId = :categoryId")
    suspend fun getTransactionsCountByCategory(categoryId: Long): Int

    @Insert
    suspend fun insertTransaction(transaction: Transaction): Long

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)
} 