package com.beyond5g.polaris

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "test")
data class Test(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val phonenumber: String,
    val timestamp: Long,
    val cell_info_id: Int
)
