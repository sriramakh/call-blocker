package com.blocknos.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_contacts")
data class BlockedContactEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val contactName: String?,
    val phoneNumber: String
)

