package com.education.financetrackerrublik.ui.adapter

import com.education.financetrackerrublik.data.model.TransactionWithCategory
import java.util.Date

sealed class TransactionListItem {
    data class DateHeader(
        val date: Date,
        val totalIncome: Double,
        val totalExpense: Double
    ) : TransactionListItem()

    data class TransactionItem(
        val transaction: TransactionWithCategory
    ) : TransactionListItem()
} 