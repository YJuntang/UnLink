package com.juntang2.unlink.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [HistoryEntity::class], version = 1, exportSchema = false)
abstract class UnLinkDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
}
