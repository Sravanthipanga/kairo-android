package com.sravanthi.kairo.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.sravanthi.kairo.core.database.model.SuggestionDismissalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SuggestionDismissalDao {

    @Query("SELECT * FROM suggestion_dismissals")
    fun observeAll(): Flow<List<SuggestionDismissalEntity>>

    @Upsert
    suspend fun upsert(dismissal: SuggestionDismissalEntity)
}
