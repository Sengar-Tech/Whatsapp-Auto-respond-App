package com.yourfirm.autoreply.util

/**
 * Normalizes any Indian phone number string to a bare 10-digit form.
 *
 * Input examples that all produce "9876543210":
 *   +91 98765-43210
 *   0091 9876543210
 *   09876543210
 *   9876543210
 *   91 9876543210
 *
 * Only the last 10 digits are used for comparison — this sidesteps
 * any country-code ambiguity when matching against the whitelist.
 */
object PhoneNormalizer {

    fun normalize(raw: String): String {
        // Strip all non-digit characters (spaces, dashes, plus, brackets, dots)
        var cleaned = raw.replace(Regex("[^\\d]"), "")

        when {
            cleaned.startsWith("0091") && cleaned.length > 4  -> cleaned = cleaned.removePrefix("0091")
            cleaned.startsWith("91")  && cleaned.length == 12 -> cleaned = cleaned.removePrefix("91")
            cleaned.startsWith("0")   && cleaned.length == 11 -> cleaned = cleaned.removePrefix("0")
        }

        // Return the last 10 digits (or whatever we have if shorter)
        return if (cleaned.length >= 10) cleaned.takeLast(10) else cleaned
    }

    /** Returns true if the string (after stripping non-digits) looks like a phone number. */
    fun looksLikePhoneNumber(text: String): Boolean {
        val digits = text.replace(Regex("[^\\d]"), "")
        return digits.length in 7..15
    }

    /** Prepend country code for Meta API — e.g. "9876543210" → "919876543210" */
    fun toApiFormat(normalized: String, countryCode: String = "91"): String {
        return "$countryCode$normalized"
    }
}
