package com.blocknos.call

import android.telecom.Call
import android.telecom.CallScreeningService
import com.blocknos.data.AppDatabase
import com.blocknos.data.BlockRulesRepository
import com.blocknos.data.BlockedCallLogEntity
import com.blocknos.data.DefaultSpamChecker

class SpamCallScreeningService : CallScreeningService() {

    private val db by lazy { AppDatabase.getInstance(applicationContext) }

    private val repository by lazy {
        BlockRulesRepository(db.blockedRuleDao(), DefaultSpamChecker())
    }

    override fun onScreenCall(callDetails: Call.Details) {
        val handle = callDetails.handle ?: return
        val number = handle.schemeSpecificPart ?: return

        val decision = repository.evaluateNumber(number)

        val responseBuilder = CallResponse.Builder()

        if (decision.shouldBlock) {
            responseBuilder
                .setDisallowCall(true)
                .setRejectCall(true)
                .setSkipCallLog(false)
                .setSkipNotification(false)

            db.blockedRuleDao().insertCallLog(
                BlockedCallLogEntity(
                    phoneNumber = number,
                    reason = decision.reason ?: "Unknown"
                )
            )
        }

        respondToCall(callDetails, responseBuilder.build())
    }
}

