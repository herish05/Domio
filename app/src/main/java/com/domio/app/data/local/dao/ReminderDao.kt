package com.domio.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.domio.app.data.local.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {

    @Insert
    suspend fun insert(reminder: ReminderEntity)

    @Update
    suspend fun update(reminder: ReminderEntity)

    @Delete
    suspend fun delete(reminder: ReminderEntity)

    @Query(
        """
        SELECT * FROM reminders
        WHERE isCompleted = 0
        ORDER BY dueDate ASC
        """
    )
    fun observePending(): Flow<List<ReminderEntity>>

    @Query(
        """
        SELECT * FROM reminders
        WHERE id = :id
        LIMIT 1
        """
    )
    suspend fun getById(id: String): ReminderEntity?

    @Query(
        """
        UPDATE reminders
        SET isCompleted = 1
        WHERE id = :id
        """
    )
    suspend fun markCompleted(id: String)
}