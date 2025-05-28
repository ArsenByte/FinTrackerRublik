package com.education.financetrackerrublik.ui.categories

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.education.financetrackerrublik.data.AppDatabase
import com.education.financetrackerrublik.data.model.Category
import com.education.financetrackerrublik.data.model.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ManageCategoriesViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val categoryDao = database.categoryDao()
    private val transactionDao = database.transactionDao()

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    private val _canDelete = MutableLiveData<Boolean>()
    val canDelete: LiveData<Boolean> = _canDelete

    fun loadCategories(type: TransactionType) {
        viewModelScope.launch(Dispatchers.IO) {
            val categories = categoryDao.getCategoriesByType(type)
            _categories.postValue(categories)
        }
    }

    fun checkCanDeleteCategory(category: Category) {
        viewModelScope.launch(Dispatchers.IO) {
            val transactionsCount = transactionDao.getTransactionsCountByCategory(category.id)
            _canDelete.postValue(transactionsCount == 0)
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch(Dispatchers.IO) {
            categoryDao.deleteCategory(category)
            loadCategories(category.type)
        }
    }

    fun addCategory(name: String, iconResId: Int, type: TransactionType) {
        viewModelScope.launch(Dispatchers.IO) {
            val category = Category(
                name = name,
                iconResId = iconResId,
                type = type
            )
            categoryDao.insertCategory(category)
            loadCategories(type)
        }
    }

    fun updateCategory(category: Category, newName: String, newIconResId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedCategory = category.copy(
                name = newName,
                iconResId = newIconResId
            )
            categoryDao.updateCategory(updatedCategory)
            loadCategories(category.type)
        }
    }
} 