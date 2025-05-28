package com.education.financetrackerrublik.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Date,
    val type: TransactionType,
    val amount: Double,
    val description: String,
    val categoryId: Long
)

enum class TransactionType {
    INCOME, EXPENSE
} 