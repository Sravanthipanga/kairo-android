package com.sravanthi.kairo.core.data.repository

import com.sravanthi.kairo.core.database.dao.SuggestionDismissalDao
import com.sravanthi.kairo.core.database.model.SuggestionDismissalEntity
import com.sravanthi.kairo.core.database.model.asExternalModel
import com.sravanthi.kairo.core.model.SuggestionDismissal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalTime
import javax.inject.Inject

internal class OfflineFirstSuggestionDismissalRepository @Inject constructor(
    private val dao: SuggestionDismissalDao,
) : SuggestionDismissalRepository {

    override fun observeDismissals(): Flow<List<SuggestionDismissal>> =
        dao.observeAll().map { list -> list.map { it.asExternalModel() } }

    override suspend fun dismiss(medicationId: Long, reminderTime: LocalTime, at: Instant) =
        dao.upsert(SuggestionDismissalEntity(medicationId, reminderTime, at))
}
