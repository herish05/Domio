package com.domio.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.domio.app.data.local.dao.AssetDao
import com.domio.app.data.local.dao.DocumentDao
import com.domio.app.data.local.dao.ReminderDao
import com.domio.app.data.local.entity.AssetDocumentCrossRef
import com.domio.app.data.local.entity.AssetEntity
import com.domio.app.data.local.entity.DocumentEntity
import com.domio.app.data.local.entity.ReminderEntity

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE assets ADD COLUMN subCategory TEXT")
        db.execSQL("ALTER TABLE assets ADD COLUMN barcode TEXT")
        db.execSQL("ALTER TABLE assets ADD COLUMN room TEXT")
        db.execSQL("ALTER TABLE assets ADD COLUMN condition TEXT")
        db.execSQL("ALTER TABLE assets ADD COLUMN notes TEXT")
        db.execSQL("ALTER TABLE assets ADD COLUMN currency TEXT NOT NULL DEFAULT 'INR'")
        db.execSQL("ALTER TABLE assets ADD COLUMN sellerStore TEXT")
        db.execSQL("ALTER TABLE assets ADD COLUMN receiptInfo TEXT")
        db.execSQL("ALTER TABLE assets ADD COLUMN warrantyProvider TEXT")
        db.execSQL("ALTER TABLE assets ADD COLUMN warrantyDurationMonths INTEGER")
        db.execSQL("ALTER TABLE assets ADD COLUMN maintenanceCost REAL")
        db.execSQL("ALTER TABLE assets ADD COLUMN repairCost REAL")
        db.execSQL("ALTER TABLE assets ADD COLUMN estimatedCurrentValue REAL")
        db.execSQL("ALTER TABLE assets ADD COLUMN isArchived INTEGER NOT NULL DEFAULT 0")
    }
}

@Database(
    entities = [
        AssetEntity::class,
        DocumentEntity::class,
        ReminderEntity::class,
        AssetDocumentCrossRef::class
    ],
    version = 3,
    exportSchema = true
)
abstract class DomioDatabase : RoomDatabase() {

    abstract fun assetDao(): AssetDao

    abstract fun documentDao(): DocumentDao

    abstract fun reminderDao(): ReminderDao
}