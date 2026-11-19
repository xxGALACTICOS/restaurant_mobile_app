package com.example.restarant_mobile_app.order.DAOs

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Transaction
import com.example.restarant_mobile_app.order.Entities.Order
import com.example.restarant_mobile_app.order.Entities.OrderItem
import com.example.restarant_mobile_app.order.Entities.OrderWithItems

@Dao
interface OrderDAO {

    @Insert
    abstract suspend fun insertOrder(order: Order): Long

    @Insert
    abstract suspend fun insertOrderItems(items: List<OrderItem>)

    // Create Order and its items in one transaction
    @Transaction
    open suspend fun createOrder(
        order: Order,
        items: List<OrderItem>
    ): Long {
        val orderId = insertOrder(order)
        insertOrderItems(
            items.map { it.copy(orderId = orderId.toInt()) }
        )

        return orderId
    }

    @Query("SELECT * FROM orders ORDER BY orderDate DESC")
    abstract suspend fun getAllOrders(): List<Order>

    @Query("SELECT * FROM orders WHERE userId = :userId ORDER BY orderDate DESC")
    abstract suspend fun getOrdersByUserId(userId: Long): List<Order>


    @Query("SELECT * FROM orders WHERE orderId = :orderId")
    abstract suspend fun getOrderById(orderId: Int): Order?


    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    abstract suspend fun getOrderItems(orderId: Int): List<OrderItem>

    // Get orders with their items by user ID
    @Transaction
    @Query("SELECT * FROM orders WHERE userId = :userId ORDER BY orderDate DESC")
    abstract suspend fun getOrdersWithItemsByUserId(userId: Long): List<OrderWithItems>

    // Get all orders with their items (for Admin)
    @Transaction
    @Query("SELECT * FROM orders ORDER BY orderDate DESC")
    abstract suspend fun getAllOrdersWithItems(): List<OrderWithItems>


    @Query("UPDATE orders SET status = :status WHERE orderId = :orderId")
    abstract suspend fun updateOrderStatus(
        orderId: Int,
        status: String
    )

    // Delete an order
    @Query("DELETE FROM orders WHERE orderId = :orderId")
    abstract suspend fun deleteOrder(orderId: Int)
}
