package com.sravanthi.kairo.core.data.di

import com.sravanthi.kairo.core.data.repository.DataStoreUserPreferencesRepository
import com.sravanthi.kairo.core.data.repository.DoseRepository
import com.sravanthi.kairo.core.data.repository.MedicationRepository
import com.sravanthi.kairo.core.data.repository.OfflineFirstDoseRepository
import com.sravanthi.kairo.core.data.repository.OfflineFirstMedicationRepository
import com.sravanthi.kairo.core.data.repository.OfflineFirstSuggestionDismissalRepository
import com.sravanthi.kairo.core.data.repository.SuggestionDismissalRepository
import com.sravanthi.kairo.core.data.repository.UserPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DataModule {

    @Binds
    abstract fun bindMedicationRepository(
        repository: OfflineFirstMedicationRepository,
    ): MedicationRepository

    @Binds
    abstract fun bindDoseRepository(
        repository: OfflineFirstDoseRepository,
    ): DoseRepository

    @Binds
    abstract fun bindSuggestionDismissalRepository(
        repository: OfflineFirstSuggestionDismissalRepository,
    ): SuggestionDismissalRepository

    @Binds
    abstract fun bindUserPreferencesRepository(
        repository: DataStoreUserPreferencesRepository,
    ): UserPreferencesRepository

    companion object {
        /** The device clock, injected so time-dependent logic can be tested with a fixed clock. */
        @Provides
        fun provideClock(): Clock = Clock.systemDefaultZone()
    }
}
