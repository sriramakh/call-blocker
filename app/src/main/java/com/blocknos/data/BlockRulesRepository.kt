package com.blocknos.data

data class BlockDecision(
    val shouldBlock: Boolean,
    val reason: String? = null
)

class BlockRulesRepository(
    private val dao: BlockedRuleDao,
    private val spamChecker: SpamChecker
) {

    fun evaluateNumber(e164Number: String): BlockDecision {
        val normalized = normalize(e164Number)

        if (dao.getBlockedNumbers().any { normalize(it) == normalized }) {
            return BlockDecision(true, "Blocked number")
        }

        if (dao.getBlockedContactNumbers().any { normalize(it) == normalized }) {
            return BlockDecision(true, "Blocked contact")
        }

        if (dao.getBlockedPrefixes().any { prefix ->
                normalized.startsWith(normalizePrefix(prefix))
            }) {
            return BlockDecision(true, "Blocked prefix")
        }

        if (spamChecker.isSpam(normalized)) {
            return BlockDecision(true, "Public spam database")
        }

        return BlockDecision(false, null)
    }

    private fun normalize(e164: String): String =
        e164.filterNot { it == ' ' }

    private fun normalizePrefix(prefix: String): String =
        prefix.filterNot { it == ' ' }
}

