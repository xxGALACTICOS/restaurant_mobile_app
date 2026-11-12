package com.example.restarant_mobile_app.order.Entities

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "order")
class Order(
    @PrimaryKey(autoGenerate = true) val id: Long = 0
)