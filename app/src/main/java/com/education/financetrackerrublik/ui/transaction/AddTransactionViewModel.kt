package com.education.financetrackerrublik.ui.transaction

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.education.financetrackerrublik.data.AppDatabase
import com.education.financetrackerrublik.data.model.Category
import com.education.financetrackerrublik.data.model.Transaction
import com.education.financetrackerrublik.data.model.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

class AddTransactionViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val transactionDao = database.transactionDao()
    private val categoryDao = database.categoryDao()

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    private val _transactionSaved = MutableLiveData<Boolean>()
    val transactionSaved: LiveData<Boolean> = _transactionSaved

    fun loadCategories(type: TransactionType) {
        viewModelScope.launch(Dispatchers.IO) {
            val categories = categoryDao.getCategoriesByType(type)
            _categories.postValue(categories)
        }
    }

    fun saveTransaction(
        amount: Double,
        type: TransactionType,
        categoryId: Long,
        description: String,
        date: Date
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val transaction = Transaction(
                amount = amount,
                type = type,
                categoryId = categoryId,
                description = description,
                date = date
            )
            transactionDao.insertTransaction(transaction)
            _transactionSaved.postValue(true)
        }
    }
} 