package com.blocknos.ui

import android.app.Application
import android.provider.CallLog
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.blocknos.data.AppDatabase
import com.blocknos.data.BlockedCallLogEntity
import com.blocknos.data.BlockedContactEntity
import com.blocknos.data.BlockedNumberEntity
import com.blocknos.data.BlockedPrefixEntity

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).blockedRuleDao()

    val blockedNumbers: LiveData<List<BlockedNumberEntity>> = dao.getAllBlockedNumbers()
    val blockedPrefixes: LiveData<List<BlockedPrefixEntity>> = dao.getAllBlockedPrefixes()
    val blockedContacts: LiveData<List<BlockedContactEntity>> = dao.getAllBlockedContacts()
    val callLog: LiveData<List<BlockedCallLogEntity>> = dao.getCallLog()

    private val _deviceCallLog = MutableLiveData<List<DeviceCallLogEntry>>()
    val deviceCallLog: LiveData<List<DeviceCallLogEntry>> = _deviceCallLog

    private val _summary = MutableLiveData<String>()
    val summary: LiveData<String> = _summary

    fun addBlockedNumber(number: String): Boolean {
        if (number.isBlank()) return false
        dao.insertBlockedNumber(BlockedNumberEntity(phoneNumber = number.trim()))
        updateSummary()
        return true
    }

    fun addBlockedPrefix(prefix: String): Boolean {
        if (prefix.isBlank()) return false
        dao.insertBlockedPrefix(BlockedPrefixEntity(prefix = prefix.trim()))
        updateSummary()
        return true
    }

    fun addBlockedContact(name: String?, number: String): Boolean {
        if (number.isBlank()) return false
        dao.insertBlockedContact(
            BlockedContactEntity(
                contactName = name?.trim(),
                phoneNumber = number.trim()
            )
        )
        updateSummary()
        return true
    }

    fun deleteBlockedNumber(entity: BlockedNumberEntity) {
        dao.deleteBlockedNumber(entity)
        updateSummary()
    }

    fun deleteBlockedPrefix(entity: BlockedPrefixEntity) {
        dao.deleteBlockedPrefix(entity)
        updateSummary()
    }

    fun deleteBlockedContact(entity: BlockedContactEntity) {
        dao.deleteBlockedContact(entity)
        updateSummary()
    }

    fun clearCallLog() {
        dao.clearCallLog()
    }

    fun loadDeviceCallLog() {
        val entries = mutableListOf<DeviceCallLogEntry>()
        try {
            val resolver = getApplication<Application>().contentResolver
            val cursor = resolver.query(
                CallLog.Calls.CONTENT_URI,
                arrayOf(
                    CallLog.Calls.NUMBER,
                    CallLog.Calls.CACHED_NAME,
                    CallLog.Calls.TYPE,
                    CallLog.Calls.DATE,
                    CallLog.Calls.DURATION
                ),
                null, null,
                "${CallLog.Calls.DATE} DESC"
            )
            cursor?.use {
                val colNumber = it.getColumnIndex(CallLog.Calls.NUMBER)
                val colName = it.getColumnIndex(CallLog.Calls.CACHED_NAME)
                val colType = it.getColumnIndex(CallLog.Calls.TYPE)
                val colDate = it.getColumnIndex(CallLog.Calls.DATE)
                val colDuration = it.getColumnIndex(CallLog.Calls.DURATION)

                var count = 0
                while (it.moveToNext() && count < 50) {
                    val number = it.getString(colNumber) ?: continue
                    entries.add(
                        DeviceCallLogEntry(
                            number = number,
                            name = it.getString(colName),
                            type = it.getInt(colType),
                            date = it.getLong(colDate),
                            duration = it.getLong(colDuration)
                        )
                    )
                    count++
                }
            }
        } catch (_: SecurityException) {
            // Permission not granted yet
        }
        _deviceCallLog.postValue(entries)
    }

    fun updateSummary() {
        val numbers = dao.getBlockedNumbers().size
        val prefixes = dao.getBlockedPrefixes().size
        val contacts = dao.getBlockedContactNumbers().size

        val parts = mutableListOf<String>()
        if (numbers > 0) parts.add("$numbers number${if (numbers != 1) "s" else ""}")
        if (prefixes > 0) parts.add("$prefixes prefix${if (prefixes != 1) "es" else ""}")
        if (contacts > 0) parts.add("$contacts contact${if (contacts != 1) "s" else ""}")

        val text = if (parts.isEmpty()) {
            "No blocking rules configured"
        } else {
            parts.joinToString(" \u2022 ") + " blocked"
        }
        _summary.postValue(text)
    }
}

