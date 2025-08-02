package com.vibecoder.app.di

import android.content.Context
import com.vibecoder.app.data.dao.CustomerDao
import com.vibecoder.app.data.dao.FollowUpDao
import com.vibecoder.app.data.database.AppDatabase
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
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideCustomerDao(database: AppDatabase): CustomerDao {
        return database.customerDao()
    }

    @Provides
    fun provideFollowUpDao(database: AppDatabase): FollowUpDao {
        return database.followUpDao()
    }
}