package com.sravanthi.kairo.core.data.di

import com.sravanthi.kairo.core.data.repository.DoseRepository
import com.sravanthi.kairo.core.data.repository.MedicationRepository
import com.sravanthi.kairo.core.data.repository.OfflineFirstDoseRepository
import com.sravanthi.kairo.core.data.repository.OfflineFirstMedicationRepository
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

    companion object {
        /** The device clock, injected so time-dependent logic can be tested with a fixed clock. */
        @Provides
        fun provideClock(): Clock = Clock.systemDefaultZone()
    }
}
