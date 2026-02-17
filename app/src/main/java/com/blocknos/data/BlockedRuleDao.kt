package com.blocknos.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BlockedRuleDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertBlockedNumber(entity: BlockedNumberEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertBlockedPrefix(entity: BlockedPrefixEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertBlockedContact(entity: BlockedContactEntity)

    @Query("SELECT phoneNumber FROM blocked_numbers")
    fun getBlockedNumbers(): List<String>

    @Query("SELECT prefix FROM blocked_prefixes")
    fun getBlockedPrefixes(): List<String>

    @Query("SELECT phoneNumber FROM blocked_contacts")
    fun getBlockedContactNumbers(): List<String>

    @Query("SELECT * FROM blocked_numbers ORDER BY id DESC")
    fun getAllBlockedNumbers(): LiveData<List<BlockedNumberEntity>>

    @Query("SELECT * FROM blocked_prefixes ORDER BY id DESC")
    fun getAllBlockedPrefixes(): LiveData<List<BlockedPrefixEntity>>

    @Query("SELECT * FROM blocked_contacts ORDER BY id DESC")
    fun getAllBlockedContacts(): LiveData<List<BlockedContactEntity>>

    @Delete
    fun deleteBlockedNumber(entity: BlockedNumberEntity)

    @Delete
    fun deleteBlockedPrefix(entity: BlockedPrefixEntity)

    @Delete
    fun deleteBlockedContact(entity: BlockedContactEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCallLog(entity: BlockedCallLogEntity)

    @Query("SELECT * FROM blocked_call_log ORDER BY timestamp DESC")
    fun getCallLog(): LiveData<List<BlockedCallLogEntity>>

    @Query("DELETE FROM blocked_call_log")
    fun clearCallLog()
}

