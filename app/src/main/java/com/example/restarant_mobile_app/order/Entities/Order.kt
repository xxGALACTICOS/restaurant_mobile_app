package com.example.restarant_mobile_app.order.Entities

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(tableName = "order", indices = [Index("userId")])
data class Order(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val total: Double = 0.0,
    val status: String = "pending",
    val date: Long = System.currentTimeMillis()
)
