package com.example.eventia.di

import com.example.eventia.data.repository.AuthRepositoryImpl
import com.example.eventia.data.repository.EventRepositoryImpl
import com.example.eventia.data.repository.SessionRepositoryImpl
import com.example.eventia.domain.repository.AuthRepository
import com.example.eventia.domain.repository.EventRepository
import com.example.eventia.domain.repository.SessionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSessionRepository(impl: SessionRepositoryImpl): SessionRepository

    @Binds
    @Singleton
    abstract fun bindEventRepository(impl: EventRepositoryImpl): EventRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
}
