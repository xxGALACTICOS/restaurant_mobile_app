package com.example.restarant_mobile_app.order.Entities

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    tableName = "order_items",
    foreignKeys = [
        ForeignKey(
            entity = Order::class,
            parentColumns = ["orderId"],
            childColumns = ["orderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("orderId")]
)
data class OrderItem(
    @PrimaryKey(autoGenerate = true)
    val itemId: Int = 0,

    val orderId: Int = 0,

    val foodId: Int,

    val foodName: String,

    val price: Double,

    val quantity: Int,

    val selectedExtra: String = ""
)
