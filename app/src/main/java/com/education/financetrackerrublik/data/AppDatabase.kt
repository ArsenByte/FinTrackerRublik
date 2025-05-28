package com.education.financetrackerrublik.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.education.financetrackerrublik.R
import com.education.financetrackerrublik.data.dao.CategoryDao
import com.education.financetrackerrublik.data.dao.TransactionDao
import com.education.financetrackerrublik.data.model.Category
import com.education.financetrackerrublik.data.model.Transaction
import com.education.financetrackerrublik.data.model.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Transaction::class, Category::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "finance_tracker_db"
                )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        INSTANCE?.let { database ->
                            CoroutineScope(Dispatchers.IO).launch {
                                prepopulateCategories(database.categoryDao())
                            }
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun prepopulateCategories(categoryDao: CategoryDao) {
            val defaultCategories = listOf(
                Category(name = "Продукты", type = TransactionType.EXPENSE, iconResId = R.drawable.ic_food),
                Category(name = "Транспорт", type = TransactionType.EXPENSE, iconResId = R.drawable.ic_transport),
                Category(name = "Развлечения", type = TransactionType.EXPENSE, iconResId = R.drawable.ic_entertainment),
                Category(name = "Зарплата", type = TransactionType.INCOME, iconResId = R.drawable.ic_salary),
                Category(name = "Фриланс", type = TransactionType.INCOME, iconResId = R.drawable.ic_freelance)
            )

            defaultCategories.forEach { category ->
                categoryDao.insertCategory(category)
            }
        }
    }
} 