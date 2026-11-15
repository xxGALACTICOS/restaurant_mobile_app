package com.example.restarant_mobile_app

import android.content.Context
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase
import com.example.restarant_mobile_app.auth.DAOs.UsersDAO
import com.example.restarant_mobile_app.auth.Entities.User
import com.example.restarant_mobile_app.cart.DAOs.CartDAO
import com.example.restarant_mobile_app.cart.DAOs.CartItemsDAO
import com.example.restarant_mobile_app.cart.Entities.Cart
import com.example.restarant_mobile_app.cart.Entities.CartItems
import com.example.restarant_mobile_app.favourite.FavDao
import com.example.restarant_mobile_app.favourite.Favourite
import com.example.restarant_mobile_app.order.DAOs.OrderDAO
import com.example.restarant_mobile_app.order.DAOs.OrderItemsDAO
import com.example.restarant_mobile_app.order.Entities.Order
import com.example.restarant_mobile_app.products.DAOs.ProductDAO
import com.example.restarant_mobile_app.products.Entities.Product

@Database(entities = [User::class, Cart::class, CartItems::class, Order::class, Product::class, Favourite::class], version = 5)
abstract class RestaurantDatabase : RoomDatabase() {
    abstract fun userDao(): UsersDAO
    abstract fun productsDao(): ProductDAO
    abstract fun cartDao(): CartDAO
    abstract fun cartItemDao(): CartItemsDAO
    abstract fun orderDao(): OrderDAO
    abstract fun orderItemsDao(): OrderItemsDAO
    abstract fun favouritesDao(): FavDao

    companion object{
        @Volatile
        private var INSTANCE: RestaurantDatabase? = null
        fun getInstance(context: Context) : RestaurantDatabase {
            if (INSTANCE == null) {
                synchronized(RestaurantDatabase::class) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        RestaurantDatabase::class.java,
                        "res_db"
                    )
                        .fallbackToDestructiveMigration(dropAllTables = true)
                        .build()
                }
            }
            return INSTANCE!!
        }
    }


}