package com.juntang2.unlink.data.di

import android.content.Context
import androidx.room.Room
import com.juntang2.unlink.data.local.db.HistoryDao
import com.juntang2.unlink.data.local.db.UnLinkDatabase
import com.juntang2.unlink.data.local.preferences.SettingsDataStore
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
    fun provideDatabase(@ApplicationContext context: Context): UnLinkDatabase {
        return Room.databaseBuilder(
            context,
            UnLinkDatabase::class.java,
            "unlink_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideHistoryDao(database: UnLinkDatabase): HistoryDao {
        return database.historyDao()
    }

    @Provides
    @Singleton
    fun provideSettingsDataStore(@ApplicationContext context: Context): SettingsDataStore {
        return SettingsDataStore(context)
    }
}
