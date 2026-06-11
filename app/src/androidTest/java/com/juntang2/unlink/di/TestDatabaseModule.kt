package com.juntang2.unlink.di

import android.content.Context
import androidx.room.Room
import com.juntang2.unlink.data.local.db.UnLinkDatabase
import com.juntang2.unlink.data.local.db.HistoryDao
import com.juntang2.unlink.data.local.preferences.SettingsDataStore
import com.juntang2.unlink.data.di.DatabaseModule
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DatabaseModule::class]
)
object TestDatabaseModule {
    
    @Provides
    @Singleton
    fun provideInMemoryDatabase(@ApplicationContext context: Context): UnLinkDatabase {
        return Room.inMemoryDatabaseBuilder(
            context,
            UnLinkDatabase::class.java
        ).allowMainThreadQueries()
         .build()
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
