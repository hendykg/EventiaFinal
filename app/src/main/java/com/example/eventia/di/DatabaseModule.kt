package com.example.eventia.di

import android.content.Context
import androidx.room3.Room
import com.example.eventia.data.DataStoreManager
import com.example.eventia.data.EventiaDatabase
import com.example.eventia.data.daos.EventDao
import com.example.eventia.data.daos.VenueDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): EventiaDatabase =
        Room.databaseBuilder(context, EventiaDatabase::class.java, "eventia_db")
            .build()

    @Provides
    fun provideEventDao(database: EventiaDatabase): EventDao = database.eventDao()

    @Provides
    fun provideVenueDao(database: EventiaDatabase): VenueDao = database.venueDao()

    @Provides
    @Singleton
    fun provideDataStoreManager(@ApplicationContext context: Context): DataStoreManager =
        DataStoreManager(context)
}
