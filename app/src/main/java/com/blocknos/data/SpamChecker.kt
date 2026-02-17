package com.blocknos.data

interface SpamChecker {
    /**
     * Returns true if the given E.164 formatted phone number is known spam
     * according to a public spam database.
     *
     * In a production app this would call out to a maintained spam data source
     * or a periodically synced local database.
     */
    fun isSpam(numberE164: String): Boolean
}

class DefaultSpamChecker : SpamChecker {

    // Placeholder spam prefixes / numbers. Replace with real public data source.
    private val spamPrefixes = listOf(
        "+1408",
        "+1877",
        "+1888"
    )

    private val spamNumbers = setOf(
        "+14085550123"
    )

    override fun isSpam(numberE164: String): Boolean {
        if (spamNumbers.contains(numberE164)) return true
        return spamPrefixes.any { prefix -> numberE164.startsWith(prefix) }
    }
}

