package com.domio.app.data.local.database

import android.content.Context
import androidx.room.Room

object DatabaseProvider {

    @Volatile
    private var database: DomioDatabase? = null

    fun getDatabase(context: Context): DomioDatabase {

        return database ?: synchronized(this) {

            database ?: Room.databaseBuilder(
                context.applicationContext,
                DomioDatabase::class.java,
                "domio.db"
            )
                .addMigrations(MIGRATION_2_3)
                .fallbackToDestructiveMigration()
                .build()
                .also {
                    database = it
                }
        }
    }
}