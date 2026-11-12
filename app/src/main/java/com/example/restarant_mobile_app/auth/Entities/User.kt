package com.example.restarant_mobile_app.auth.Entities

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName="users")
class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0
)