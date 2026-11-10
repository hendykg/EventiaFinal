package com.example.eventia.data

import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.example.eventia.data.daos.EventDao
import com.example.eventia.data.daos.VenueDao
import com.example.eventia.data.entities.EventEntity
import com.example.eventia.data.entities.VenueEntity

@Database(
    entities = [EventEntity::class, VenueEntity::class],
    version = 1,
    exportSchema = false
)
abstract class EventiaDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
    abstract fun venueDao(): VenueDao
}
