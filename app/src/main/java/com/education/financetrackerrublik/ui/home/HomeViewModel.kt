package com.education.financetrackerrublik.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.education.financetrackerrublik.data.AppDatabase
import com.education.financetrackerrublik.data.model.TransactionType
import com.education.financetrackerrublik.data.model.TransactionWithCategory
import com.education.financetrackerrublik.data.model.Transaction
import com.education.financetrackerrublik.data.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val transactionDao = database.transactionDao()
    private val categoryDao = database.categoryDao()
    private val transactionRepository = TransactionRepository(database.transactionDao())

    private val _todayTransactions = MutableLiveData<List<TransactionWithCategory>>()
    val todayTransactions: LiveData<List<TransactionWithCategory>> = _todayTransactions

    private val _monthlyIncome = MutableLiveData<Double>()
    val monthlyIncome: LiveData<Double> = _monthlyIncome

    private val _monthlyExpense = MutableLiveData<Double>()
    val monthlyExpense: LiveData<Double> = _monthlyExpense

    val transactions = transactionRepository.getAllTransactions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {
            val startOfDay = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time

            val startOfMonth = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time

            val endOfMonth = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.time

            loadTodayTransactions(startOfDay)
            loadMonthlyStatistics(startOfMonth, endOfMonth)
        }
    }

    private suspend fun loadTodayTransactions(startOfDay: Date) {
        val transactions = transactionDao.getTodayTransactions(startOfDay)
        val transactionsWithCategory = transactions.map { transaction ->
            val category = categoryDao.getCategoryById(transaction.categoryId)
            category?.let { TransactionWithCategory(transaction, it) }
        }.filterNotNull()
        _todayTransactions.postValue(transactionsWithCategory)
    }

    private suspend fun loadMonthlyStatistics(startOfMonth: Date, endOfMonth: Date) {
        val income = transactionDao.getTotalByType(
            TransactionType.INCOME,
            startOfMonth,
            endOfMonth
        ) ?: 0.0
        val expense = transactionDao.getTotalByType(
            TransactionType.EXPENSE,
            startOfMonth,
            endOfMonth
        ) ?: 0.0

        _monthlyIncome.postValue(income)
        _monthlyExpense.postValue(expense)
    }

    fun deleteTransaction(transactionWithCategory: TransactionWithCategory) {
        viewModelScope.launch(Dispatchers.IO) {
            transactionDao.deleteTransaction(transactionWithCategory.transaction)
            loadData() // Перезагружаем данные после удаления
        }
    }
} 