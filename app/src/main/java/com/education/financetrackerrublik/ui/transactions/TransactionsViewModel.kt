package com.education.financetrackerrublik.ui.transactions

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.education.financetrackerrublik.data.AppDatabase
import com.education.financetrackerrublik.data.model.TransactionType
import com.education.financetrackerrublik.data.model.TransactionWithCategory
import com.education.financetrackerrublik.ui.adapter.TransactionListItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class TransactionsViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val transactionDao = database.transactionDao()
    private val categoryDao = database.categoryDao()

    private val _transactions = MutableLiveData<List<TransactionListItem>>()
    val transactions: LiveData<List<TransactionListItem>> = _transactions

    private var currentType: TransactionType? = null
    private var startDate: Date = Calendar.getInstance().apply {
        add(Calendar.YEAR, -10)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.time
    private var endDate: Date = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }.time

    fun loadTransactions() {
        viewModelScope.launch(Dispatchers.IO) {
            val transactions = transactionDao.getTransactionsByDateRange(startDate, endDate)
                .let { if (currentType != null) it.filter { t -> t.type == currentType } else it }

            // Группируем транзакции по дате (без времени)
            val groupedTransactions = transactions.groupBy { transaction ->
                Calendar.getInstance().apply {
                    time = transaction.date
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time
            }

            // Создаем список элементов с заголовками и транзакциями
            val items = mutableListOf<TransactionListItem>()
            
            groupedTransactions.entries
                .sortedByDescending { it.key }
                .forEach { (date, transactionsForDate) ->
                    // Считаем общие суммы для дня
                    val totalIncome = transactionsForDate
                        .filter { it.type == TransactionType.INCOME }
                        .sumOf { it.amount }
                    val totalExpense = transactionsForDate
                        .filter { it.type == TransactionType.EXPENSE }
                        .sumOf { it.amount }

                    // Добавляем заголовок дня
                    items.add(TransactionListItem.DateHeader(date, totalIncome, totalExpense))

                    // Добавляем транзакции за день
                    transactionsForDate
                        .sortedByDescending { it.date }
                        .forEach { transaction ->
                            val category = categoryDao.getCategoryById(transaction.categoryId)
                            category?.let {
                                items.add(TransactionListItem.TransactionItem(
                                    TransactionWithCategory(transaction, it)
                                ))
                            }
                        }
                }

            _transactions.postValue(items)
        }
    }

    fun setTransactionType(type: TransactionType?) {
        currentType = type
        loadTransactions()
    }

    fun setDateRange(start: Date, end: Date) {
        startDate = start
        endDate = end
        loadTransactions()
    }

    fun deleteTransaction(transaction: TransactionWithCategory) {
        viewModelScope.launch(Dispatchers.IO) {
            transactionDao.deleteTransaction(transaction.transaction)
            loadTransactions()
        }
    }
} 