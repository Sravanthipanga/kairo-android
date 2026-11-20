package com.sravanthi.kairo.core.data.repository

import com.sravanthi.kairo.core.model.SuggestionDismissal
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.LocalTime

/** Remembers when the user said "not now" to an adaptive timing suggestion. */
interface SuggestionDismissalRepository {
    fun observeDismissals(): Flow<List<SuggestionDismissal>>
    suspend fun dismiss(medicationId: Long, reminderTime: LocalTime, at: Instant)
}
