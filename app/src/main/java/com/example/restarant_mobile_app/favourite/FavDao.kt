package com.example.restarant_mobile_app.favourite

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import com.example.restarant_mobile_app.products.Entities.Product

@Dao
interface FavDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addFavourite(favourite: Favourite): Long

    @Query("DELETE FROM favourites WHERE userId = :userId AND productId = :productId")
    suspend fun removeFavourite(userId: Long, productId: Long)

    @Query("SELECT * FROM favourites WHERE userId = :userId ORDER BY date DESC")
    suspend fun getFavourites(userId: Long): List<Favourite>

    @Query(
        "SELECT products.* FROM favourites " +
        "INNER JOIN products ON products.id = favourites.productId " +
        "WHERE favourites.userId = :userId " +
        "ORDER BY favourites.date DESC"
    )
    suspend fun getFavouriteProducts(userId: Long): List<Product>

    @Query("SELECT EXISTS(SELECT 1 FROM favourites WHERE userId = :userId AND productId = :productId)")
    suspend fun isFavourite(userId: Long, productId: Long): Boolean
}
