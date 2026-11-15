package com.example.restarant_mobile_app.auth.Entities

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val address: String,
    val password: String,
    val email: String,
    val blocked: Boolean = false
)
