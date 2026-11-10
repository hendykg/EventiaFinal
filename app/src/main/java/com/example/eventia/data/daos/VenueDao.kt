package com.example.eventia.data.daos

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import com.example.eventia.data.entities.VenueEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VenueDao {

    // Caché de lugares (RF-13)
    @Query("SELECT * FROM venues")
    fun getCachedVenues(): Flow<List<VenueEntity>>

    // Detalle de un lugar por id
    @Query("SELECT * FROM venues WHERE id = :venueId")
    fun getVenueById(venueId: Int): Flow<VenueEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVenues(venues: List<VenueEntity>)
}
