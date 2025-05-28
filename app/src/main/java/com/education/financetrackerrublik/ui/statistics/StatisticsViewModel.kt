package com.education.financetrackerrublik.ui.statistics

import android.app.Application
import android.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.education.financetrackerrublik.data.AppDatabase
import com.education.financetrackerrublik.data.model.Category
import com.education.financetrackerrublik.data.model.Transaction
import com.education.financetrackerrublik.data.model.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import kotlin.random.Random

class StatisticsViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val transactionDao = database.transactionDao()
    private val categoryDao = database.categoryDao()

    private val _statistics = MutableLiveData<List<CategoryStatistics>>()
    val statistics: LiveData<List<CategoryStatistics>> = _statistics

    private val colors = listOf(
        Color.rgb(244, 67, 54),   // Red
        Color.rgb(156, 39, 176),  // Purple
        Color.rgb(33, 150, 243),  // Blue
        Color.rgb(76, 175, 80),   // Green
        Color.rgb(255, 152, 0),   // Orange
        Color.rgb(121, 85, 72),   // Brown
        Color.rgb(63, 81, 181),   // Indigo
        Color.rgb(0, 150, 136),   // Teal
        Color.rgb(255, 87, 34),   // Deep Orange
        Color.rgb(96, 125, 139)   // Blue Grey
    )

    fun loadStatistics(type: TransactionType, period: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val (startDate, endDate) = getDateRange(period)
            val transactions = if (startDate != null && endDate != null) {
                transactionDao.getTransactionsByDateRange(startDate, endDate)
            } else {
                transactionDao.getAllTransactions().first()
            }

            val filteredTransactions = transactions.filter { it.type == type }
            val categories = categoryDao.getCategoriesByType(type)
            
            calculateStatistics(categories, filteredTransactions)
        }
    }

    private fun getDateRange(period: Int): Pair<Date?, Date?> {
        val endDate = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }

        val startDate = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        when (period) {
            0 -> startDate.add(Calendar.DAY_OF_YEAR, -7)  // Неделя
            1 -> startDate.add(Calendar.MONTH, -1)        // Месяц
            2 -> startDate.add(Calendar.YEAR, -1)         // Год
            3 -> return Pair(null, null)                  // Все время
        }

        return Pair(startDate.time, endDate.time)
    }

    private suspend fun calculateStatistics(categories: List<Category>, transactions: List<Transaction>) {
        val categoryAmounts = mutableMapOf<Long, Double>()
        var totalAmount = 0.0

        // Подсчитываем суммы по категориям
        transactions.forEach { transaction ->
            categoryAmounts[transaction.categoryId] = 
                (categoryAmounts[transaction.categoryId] ?: 0.0) + transaction.amount
            totalAmount += transaction.amount
        }

        // Формируем статистику
        val statistics = categories.mapIndexed { index, category ->
            val amount = categoryAmounts[category.id] ?: 0.0
            val percentage = if (totalAmount > 0) {
                (amount / totalAmount * 100).toFloat()
            } else {
                0f
            }
            CategoryStatistics(
                category = category,
                amount = amount,
                percentage = percentage,
                color = colors.getOrElse(index) { 
                    Color.rgb(
                        Random.nextInt(128, 256),
                        Random.nextInt(128, 256),
                        Random.nextInt(128, 256)
                    )
                }
            )
        }.sortedByDescending { it.amount }

        _statistics.postValue(statistics)
    }
} 