package com.blocknos.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_prefixes")
data class BlockedPrefixEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val prefix: String
)

