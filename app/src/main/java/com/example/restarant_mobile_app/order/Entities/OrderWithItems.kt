package com.example.restarant_mobile_app.order.Entities

import androidx.room3.Embedded
import androidx.room3.Relation

data class OrderWithItems(
    @Embedded val order: Order,
    @Relation(
        parentColumns = ["orderId"],
        entityColumns = ["orderId"]
    )
    val items: List<OrderItem>
)
