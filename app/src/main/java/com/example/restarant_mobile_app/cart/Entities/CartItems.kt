package com.example.restarant_mobile_app.cart.Entities

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    tableName = "cart_items",
    foreignKeys = [
        ForeignKey(
            entity = Cart::class,
            parentColumns = ["id"],
            childColumns = ["cartId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(
            value = ["cartId", "productId","selectedExtra"],
            unique = true
        )
    ]
)
data class CartItems(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val productId: Int,

    val quantity: Int = 1,

    val cartId: Int,

    val selectedExtra: String
)
