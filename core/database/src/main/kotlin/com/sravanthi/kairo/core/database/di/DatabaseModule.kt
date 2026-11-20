package com.sravanthi.kairo.core.database.di

import android.content.Context
import androidx.room.Room
import com.sravanthi.kairo.core.database.KairoDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): KairoDatabase =
        Room.databaseBuilder(context, KairoDatabase::class.java, "kairo.db").build()

    @Provides
    fun provideMedicationDao(database: KairoDatabase) = database.medicationDao()

    @Provides
    fun provideDoseEventDao(database: KairoDatabase) = database.doseEventDao()

    @Provides
    fun provideSuggestionDismissalDao(database: KairoDatabase) = database.suggestionDismissalDao()
}
