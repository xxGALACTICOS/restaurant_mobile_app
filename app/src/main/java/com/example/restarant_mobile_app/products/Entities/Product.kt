package com.example.restarant_mobile_app.products.Entities

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "products")
class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0
)