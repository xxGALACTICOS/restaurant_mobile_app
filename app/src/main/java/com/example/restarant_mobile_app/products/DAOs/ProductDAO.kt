package com.example.restarant_mobile_app.products.DAOs

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Update
import com.example.restarant_mobile_app.products.Entities.Product
import androidx.room3.Insert
import androidx.room3.Query


@Dao
interface ProductDAO {

    @Insert
    suspend fun insertProduct(product: Product)

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("SELECT * FROM products")
    suspend fun getAllProducts(): List<Product>

    @Query("SELECT * FROM products WHERE id = :id")
    fun getProductById(id: Int): Product
}