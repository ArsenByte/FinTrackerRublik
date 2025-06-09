package com.education.financetrackerrublik.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.education.financetrackerrublik.data.AppDatabase
import com.education.financetrackerrublik.data.model.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val transactionDao = database.transactionDao()
    private val numberFormat = NumberFormat.getCurrencyInstance(Locale("ru", "RU"))

    private val _totalIncome = MutableLiveData<String>()
    val totalIncome: LiveData<String> = _totalIncome

    private val _totalExpense = MutableLiveData<String>()
    val totalExpense: LiveData<String> = _totalExpense

    init {
        loadTotalAmounts()
    }

    fun loadTotalAmounts() {
        viewModelScope.launch(Dispatchers.IO) {
            val startDate = Calendar.getInstance().apply {
                set(1970, Calendar.JANUARY, 1, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }.time

            val endDate = Calendar.getInstance().apply {
                set(2100, Calendar.DECEMBER, 31, 23, 59, 59)
                set(Calendar.MILLISECOND, 999)
            }.time

            val income = transactionDao.getTotalByType(TransactionType.INCOME, startDate, endDate) ?: 0.0
            val expense = transactionDao.getTotalByType(TransactionType.EXPENSE, startDate, endDate) ?: 0.0

            _totalIncome.postValue(numberFormat.format(income))
            _totalExpense.postValue(numberFormat.format(expense))
        }
    }
} 