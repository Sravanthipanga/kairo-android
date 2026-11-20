package com.sravanthi.kairo.core.testing

import com.sravanthi.kairo.core.data.repository.SuggestionDismissalRepository
import com.sravanthi.kairo.core.model.SuggestionDismissal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.time.Instant
import java.time.LocalTime

class FakeSuggestionDismissalRepository : SuggestionDismissalRepository {
    private val dismissals = MutableStateFlow<List<SuggestionDismissal>>(emptyList())

    val current: List<SuggestionDismissal> get() = dismissals.value

    override fun observeDismissals(): Flow<List<SuggestionDismissal>> = dismissals

    override suspend fun dismiss(medicationId: Long, reminderTime: LocalTime, at: Instant) {
        dismissals.update { list ->
            list.filterNot { it.medicationId == medicationId && it.reminderTime == reminderTime } +
                SuggestionDismissal(medicationId, reminderTime, at)
        }
    }
}
