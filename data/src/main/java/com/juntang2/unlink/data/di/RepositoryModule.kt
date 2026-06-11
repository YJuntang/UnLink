package com.juntang2.unlink.data.di

import com.juntang2.unlink.core.repository.HistoryRepository
import com.juntang2.unlink.core.repository.SettingsRepository
import com.juntang2.unlink.data.repository.HistoryRepositoryImpl
import com.juntang2.unlink.data.repository.SettingsRepositoryImpl
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
    abstract fun bindHistoryRepository(
        historyRepositoryImpl: HistoryRepositoryImpl
    ): HistoryRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        settingsRepositoryImpl: SettingsRepositoryImpl
    ): SettingsRepository
}
