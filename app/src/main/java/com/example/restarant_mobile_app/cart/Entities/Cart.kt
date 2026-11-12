package com.example.restarant_mobile_app.cart.Entities

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "cart")
class Cart(
    @PrimaryKey(autoGenerate = true) val id: Long = 0
)