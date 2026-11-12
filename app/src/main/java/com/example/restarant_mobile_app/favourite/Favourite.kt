package com.example.restarant_mobile_app.favourite

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    tableName = "favourites",
    indices = [Index(value = ["userId", "productId"], unique = true)]
)
data class Favourite(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val productId: Long,
    val date: Long = System.currentTimeMillis()
)
