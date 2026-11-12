package com.example.restarant_mobile_app.products.DAOs

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import com.example.restarant_mobile_app.products.Entities.Product

@Dao
interface ProductDAO {

    @Insert
    suspend fun insertProducts(products: List<Product>)

    @Query("SELECT * FROM products")
    suspend fun getAllProducts(): List<Product>
}
