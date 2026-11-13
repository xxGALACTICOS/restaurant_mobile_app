package com.example.restarant_mobile_app.category.DAOs
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import androidx.room3.Insert
import androidx.room3.Update
import androidx.room3.Delete
import androidx.room3.Query
import com.example.restarant_mobile_app.category.Entities.Category
import androidx.room3.Dao
@Dao
interface CategoryDAO {
    @Insert
    suspend fun insertCategory(category: Category)

    @Update
    suspend fun updateCategory(category: Category)

    @Delete
    suspend fun deleteCategory(category: Category)

    @Query("SELECT * FROM categories")
    suspend fun getAllCategories(): List<Category>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: Int): Category?
}