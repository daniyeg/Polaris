package com.beyond5g.polaris

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.app.data.dao.TestDao
import com.example.app.data.entities.Test

@Database(entities = [Test::class], version = 1)
@TypeConverters(DateConverter::class) // for timestamp Date conversion
abstract class AppDatabase : RoomDatabase() {
    abstract fun testDao(): TestDao
}
