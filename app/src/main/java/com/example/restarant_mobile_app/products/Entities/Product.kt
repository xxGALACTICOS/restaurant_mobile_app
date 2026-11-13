package com.example.restarant_mobile_app.products.Entities

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val price: Double,
    // Name of the image file inside res/drawable, WITHOUT the extension (e.g. "burger")
    val imageName: String,
    // Links the product to a category (the category teammate's table). Add a ForeignKey later if needed.
    val categoryId: Int = 0,
    // Comma-separated options the customer can choose from, e.g. "Original,Spicy"
    val extras: String = "Original,Spicy"
) {
    // Not a database column (it's a function, not a property)
    fun extraOptions(): List<String> =
        extras.split(",").map { it.trim() }.filter { it.isNotEmpty() }
}