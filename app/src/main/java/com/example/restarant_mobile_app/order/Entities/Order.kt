package com.example.restarant_mobile_app.order.Entities

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import com.example.restarant_mobile_app.auth.Entities.User

@Entity(
    tableName = "orders",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId")]
)
data class Order(
    @PrimaryKey(autoGenerate = true)
    val orderId: Int = 0,

    val userId: Long = 0,

    val orderDate: Long = System.currentTimeMillis(),

    val totalPrice: Double,

    val status: String = "Pending"
)