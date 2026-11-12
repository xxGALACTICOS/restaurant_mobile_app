package com.example.restarant_mobile_app

import android.content.Context
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.AndroidSQLiteDriver
import com.example.restarant_mobile_app.cart.DAOs.CartDAO
import com.example.restarant_mobile_app.cart.DAOs.CartItemsDAO
import com.example.restarant_mobile_app.cart.Entities.Cart
import com.example.restarant_mobile_app.cart.Entities.CartItems
import com.example.restarant_mobile_app.products.DAOs.ProductDAO
import com.example.restarant_mobile_app.products.Entities.Product

/**
 * App database.
 *
 * Only entities that currently have real fields are registered.
 * TODO: add User / Category / Order / OrderItems here once their fields exist.
 */
@Database(entities = [Cart::class, CartItems::class, Product::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun cartDao(): CartDAO
    abstract fun cartItemsDao(): CartItemsDAO
    abstract fun productDao(): ProductDAO

    /**
     * Inserts the sample products the first time the app runs and returns
     * all products. Used by the cart screen for now; the menu screen can
     * reuse it later.
     */
    suspend fun seedSampleProductsIfEmpty(): List<Product> {
        val dao = productDao()
        val existing = dao.getAllProducts()
        if (existing.isNotEmpty()) return existing

        dao.insertProducts(
            listOf(
                Product(name = "Classic Burger", price = 8.99, imageRes = R.drawable.img_burger),
                Product(name = "Pepperoni Pizza", price = 12.50, imageRes = R.drawable.img_pizza),
                Product(name = "Fresh Salad", price = 6.75, imageRes = R.drawable.img_salad),
                Product(name = "French Fries", price = 3.50, imageRes = R.drawable.img_fries),
                Product(name = "Iced Coffee", price = 3.99, imageRes = R.drawable.img_coffee),
                Product(name = "Chocolate Cake", price = 5.25, imageRes = R.drawable.img_cake)
            )
        )
        return dao.getAllProducts()
    }

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder<AppDatabase>(context, name = "restaurant.db")
                    .setDriver(AndroidSQLiteDriver())
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
