package com.blocknos.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_call_log")
data class BlockedCallLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val phoneNumber: String,
    val reason: String,
    val timestamp: Long = System.currentTimeMillis()
)
