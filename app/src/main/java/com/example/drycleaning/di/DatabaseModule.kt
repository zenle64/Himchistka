package com.example.drycleaning.di

import android.content.Context
import androidx.room.Room
import com.example.drycleaning.data.database.AppDatabase
import com.example.drycleaning.data.database.dao.ClientDao
import com.example.drycleaning.data.database.dao.InventoryDao
import com.example.drycleaning.data.database.dao.OrderDao
import com.example.drycleaning.data.database.dao.ServicePriceDao
import com.example.drycleaning.data.database.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Модуль Hilt для предоставления базы данных и DAO */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "drycleaning_db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao()

    @Provides
    fun provideClientDao(db: AppDatabase): ClientDao = db.clientDao()

    @Provides
    fun provideOrderDao(db: AppDatabase): OrderDao = db.orderDao()

    @Provides
    fun provideInventoryDao(db: AppDatabase): InventoryDao = db.inventoryDao()

    @Provides
    fun provideServicePriceDao(db: AppDatabase): ServicePriceDao = db.servicePriceDao()
}
