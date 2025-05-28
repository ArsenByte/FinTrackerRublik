package com.education.financetrackerrublik.data.dao

import androidx.room.*
import com.education.financetrackerrublik.data.model.Category
import com.education.financetrackerrublik.data.model.TransactionType

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): List<Category>

    @Query("SELECT * FROM categories WHERE type = :type ORDER BY name ASC")
    fun getCategoriesByType(type: TransactionType): List<Category>

    @Query("SELECT * FROM categories WHERE id = :categoryId")
    suspend fun getCategoryById(categoryId: Long): Category?

    @Insert
    fun insertCategory(category: Category): Long

    @Update
    fun updateCategory(category: Category)

    @Delete
    fun deleteCategory(category: Category)

    @Query("SELECT EXISTS(SELECT 1 FROM transactions WHERE categoryId = :categoryId)")
    fun isCategoryUsed(categoryId: Long): Boolean
} 