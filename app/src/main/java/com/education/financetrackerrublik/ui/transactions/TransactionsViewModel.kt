package com.education.financetrackerrublik.ui.transactions

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.education.financetrackerrublik.data.AppDatabase
import com.education.financetrackerrublik.data.model.TransactionType
import com.education.financetrackerrublik.data.model.TransactionWithCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Date

class TransactionsViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val transactionDao = database.transactionDao()
    private val categoryDao = database.categoryDao()

    private val _transactions = MutableLiveData<List<TransactionWithCategory>>()
    val transactions: LiveData<List<TransactionWithCategory>> = _transactions

    private var startDate: Date? = null
    private var endDate: Date? = null
    private var selectedType: TransactionType? = null

    init {
        loadTransactions()
    }

    fun loadTransactions() {
        viewModelScope.launch(Dispatchers.IO) {
            val baseTransactions = if (startDate != null && endDate != null) {
                transactionDao.getTransactionsByDateRange(startDate!!, endDate!!)
            } else {
                transactionDao.getAllTransactions().first()
            }

            val filteredTransactions = baseTransactions.filter { transaction ->
                selectedType?.let { type -> type == transaction.type } ?: true
            }

            val transactionsWithCategory = filteredTransactions.mapNotNull { transaction ->
                val category = categoryDao.getCategoryById(transaction.categoryId)
                category?.let { TransactionWithCategory(transaction, it) }
            }
            _transactions.postValue(transactionsWithCategory)
        }
    }

    fun setDateRange(start: Date?, end: Date?) {
        startDate = start
        endDate = end
        loadTransactions()
    }

    fun setTransactionType(type: TransactionType?) {
        selectedType = type
        loadTransactions()
    }
} 