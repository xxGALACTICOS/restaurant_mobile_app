package com.example.restarant_mobile_app.cart.DAOs

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Update
import com.example.restarant_mobile_app.cart.Entities.CartItems

@Dao
interface CartItemsDAO {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCartItem(cartItem: CartItems): Long

    @Query("SELECT * FROM cart_items WHERE cartId = :cartId")
    suspend fun getCartItems(cartId: Int): List<CartItems>

    @Query("""
        UPDATE cart_items
        SET quantity = quantity + 1
        WHERE cartId = :cartId AND productId = :productId
    """)
    suspend fun increaseQuantity(cartId: Int, productId: Int)

    @Query("""
        UPDATE cart_items
        SET quantity = quantity - 1
        WHERE cartId = :cartId
        AND productId = :productId
        AND quantity > 1
    """)
    suspend fun decreaseQuantity(cartId: Int, productId: Int)

    @Delete
    suspend fun deleteCartItem(cartItem: CartItems)

    @Query("DELETE FROM cart_items WHERE cartId = :cartId")
    suspend fun clearCart(cartId: Int)
}