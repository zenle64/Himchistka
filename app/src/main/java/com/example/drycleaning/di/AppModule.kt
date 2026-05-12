package com.example.drycleaning.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Модуль Hilt для общих зависимостей приложения */
@Module
@InstallIn(SingletonComponent::class)
object AppModule
