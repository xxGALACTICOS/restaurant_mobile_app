package com.example.restarant_mobile_app.cart.Entities

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    tableName = "cart",
    indices = [
        Index(value = ["userId"], unique = true)
    ]
)
data class Cart(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val userId: Long
)
