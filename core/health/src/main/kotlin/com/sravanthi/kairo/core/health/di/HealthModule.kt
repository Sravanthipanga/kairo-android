package com.sravanthi.kairo.core.health.di

import com.sravanthi.kairo.core.health.HealthConnectDataRepository
import com.sravanthi.kairo.core.health.HealthDataRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class HealthModule {
    @Binds
    abstract fun bindHealthDataRepository(repository: HealthConnectDataRepository): HealthDataRepository
}
