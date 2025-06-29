package com.beyond5g.polaris

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TestDao {
//    @Insert
//    suspend fun insert(test: Test): Long
//
//    @Query("SELECT * FROM test WHERE id = :id")
//    suspend fun getById(id: Int): Test?
//
//    @Query("SELECT * FROM test")
//    suspend fun getAll(): List<Test>
//
//    // Add this for debugging
//    @Query("SELECT COUNT(*) FROM test")
//    suspend fun getCount(): Int
}