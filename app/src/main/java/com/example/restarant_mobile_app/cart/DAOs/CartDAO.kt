package com.example.restarant_mobile_app.cart.DAOs

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import com.example.restarant_mobile_app.cart.Entities.Cart

@Dao
interface CartDAO {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCart(cart: Cart): Long

    @Query("""
        SELECT * FROM cart
        WHERE userId = :userId
        LIMIT 1
    """)
    suspend fun getCartByUserId(userId: Long): Cart?
}
