package com.sravanthi.kairo.core.data.di

import com.sravanthi.kairo.core.data.repository.MedicationRepository
import com.sravanthi.kairo.core.data.repository.OfflineFirstMedicationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DataModule {

    @Binds
    abstract fun bindMedicationRepository(
        repository: OfflineFirstMedicationRepository,
    ): MedicationRepository
}
