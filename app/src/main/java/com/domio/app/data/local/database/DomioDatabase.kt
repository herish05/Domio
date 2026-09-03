package com.domio.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.domio.app.data.local.dao.AssetDao
import com.domio.app.data.local.dao.DocumentDao
import com.domio.app.data.local.dao.ReminderDao
import com.domio.app.data.local.entity.AssetDocumentCrossRef
import com.domio.app.data.local.entity.AssetEntity
import com.domio.app.data.local.entity.DocumentEntity
import com.domio.app.data.local.entity.ReminderEntity

@Database(
    entities = [
        AssetEntity::class,
        DocumentEntity::class,
        ReminderEntity::class,
        AssetDocumentCrossRef::class
    ],
    version = 2,
    exportSchema = true
)
abstract class DomioDatabase : RoomDatabase() {

    abstract fun assetDao(): AssetDao

    abstract fun documentDao(): DocumentDao

    abstract fun reminderDao(): ReminderDao
}