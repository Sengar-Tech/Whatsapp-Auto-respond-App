package com.yourfirm.autoreply.service

import android.app.Notification
import android.provider.ContactsContract
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.yourfirm.autoreply.App
import com.yourfirm.autoreply.api.MetaApiClient
import com.yourfirm.autoreply.db.entity.CooldownEntry
import com.yourfirm.autoreply.db.entity.LogEntry
import com.yourfirm.autoreply.util.ImageUploader
import com.yourfirm.autoreply.util.PhoneNormalizer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Core listener — Android binds this automatically when notification access is granted.
 *
 * Processing order for every notification:
 *  1. Is it from WhatsApp?          → No  → return
 *  2. Is it a group chat?           → Yes → return
 *  3. Can we extract a sender?      → No  → return
 *  4. Is sender in the whitelist?   → Yes → return (these are known contacts, skip)
 *  5. Did we reply within 24 hours? → Yes → return (cooldown)
 *  6. Are API credentials set?      → No  → return
 *  7. Upload image → send template → record cooldown + log
 */
class NotificationService : NotificationListenerService() {

    // SupervisorJob: if one coroutine fails, others keep running
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // ── Notification callback ─────────────────────────────────────────────────

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (!isWhatsApp(sbn.packageName)) return
        if (isGroupChat(sbn)) return

        val senderNormalized = extractSender(sbn) ?: return

        // Update health timestamp so dashboard can detect if we're alive
        (application as App).prefs.setLastServiceActivity(System.currentTimeMillis())

        scope.launch {
            processNotification(senderNormalized)
        }
    }

    // ── Filtering helpers ─────────────────────────────────────────────────────

    private fun isWhatsApp(packageName: String): Boolean =
        packageName == "com.whatsapp" || packageName == "com.whatsapp.w4b"

    /**
     * Group chats: the notification text contains "SenderName: message".
     * Personal chats: the title IS the sender (no colon pattern in text from different sender).
     * We also skip if the title is not a phone number and the text has a colon — group pattern.
     */
    private fun isGroupChat(sbn: StatusBarNotification): Boolean {
        val extras = sbn.notification.extras
        val text  = extras.getString(Notification.EXTRA_TEXT) ?: ""
        val title = extras.getString(Notification.EXTRA_TITLE) ?: ""

        // Primary: check the group conversation flag (API 26+)
        if (extras.getBoolean(Notification.EXTRA_IS_GROUP_CONVERSATION, false)) return true

        // Secondary: colon pattern in text when title is not a phone number
        return text.contains(": ") && !PhoneNormalizer.looksLikePhoneNumber(title)
    }

    // ── Sender extraction ─────────────────────────────────────────────────────

    /**
     * WhatsApp puts the sender's contact name (or raw number for unsaved) in EXTRA_TITLE.
     * If it looks like a phone number → normalize it directly.
     * If it's a contact name → reverse-lookup in ContactsContract to get the number.
     * Returns null if we can't determine a phone number (skip the notification).
     */
    private fun extractSender(sbn: StatusBarNotification): String? {
        val extras = sbn.notification.extras
        val title  = extras.getString(Notification.EXTRA_TITLE)?.trim() ?: return null
        if (title.isBlank()) return null

        return if (PhoneNormalizer.looksLikePhoneNumber(title)) {
            PhoneNormalizer.normalize(title)
        } else {
            resolveContactNumber(title)
        }
    }

    /**
     * Query ContactsContract by display name to retrieve a phone number.
     * Returns null if the contact has no number or isn't found.
     *
     * Note: READ_CONTACTS permission is required (declared in manifest).
     */
    private fun resolveContactNumber(contactName: String): String? {
        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} = ?",
            arrayOf(contactName),
            null
        )
        return cursor?.use {
            if (it.moveToFirst()) {
                PhoneNormalizer.normalize(it.getString(0))
            } else null
        }
    }

    // ── Core logic ────────────────────────────────────────────────────────────

    private suspend fun processNotification(senderNormalized: String) {
        val app   = application as App
        val db    = app.database
        val prefs = app.prefs

        // Step 1: whitelist check — these are the user's known contacts, skip them
        if (db.whitelistDao().exists(senderNormalized)) return

        // Step 2: cooldown — don't spam the same sender more than once per 24 hours
        val lastReply = db.cooldownDao().getLastReply(senderNormalized)
        if (lastReply != null) {
            val hoursSince = (System.currentTimeMillis() - lastReply.timestamp) / 3_600_000L
            if (hoursSince < 24) return
        }

        // Step 3: credentials check
        val token        = prefs.getAccessToken()
        val phoneNumberId = prefs.getPhoneNumberId()
        val templateName = prefs.getTemplateName()
        val imageUri     = prefs.getTemplateImageUri()

        if (token.isBlank() || phoneNumberId.isBlank() || templateName.isBlank()) return
        if (imageUri.isBlank()) return

        // Step 4: upload image + send template
        try {
            val mediaId = ImageUploader.upload(
                context       = applicationContext,
                imageUriString = imageUri,
                phoneNumberId  = phoneNumberId,
                accessToken    = token
            )

            val toNumber = PhoneNormalizer.toApiFormat(senderNormalized, prefs.getCountryCode())
            val url      = prefs.getTemplateUrl().ifBlank { null }

            MetaApiClient.send(
                phoneNumberId = phoneNumberId,
                accessToken   = token,
                toNumber      = toNumber,
                templateName  = templateName,
                mediaId       = mediaId,
                urlButton     = url
            )

            // Record so we don't reply again within 24 hours
            db.cooldownDao().upsert(CooldownEntry(senderNormalized, System.currentTimeMillis()))

            db.logDao().insert(
                LogEntry(
                    sender    = senderNormalized,
                    timestamp = System.currentTimeMillis(),
                    status    = "sent"
                )
            )

        } catch (e: Exception) {
            // Log failure — do NOT retry (risk of duplicate sends)
            db.logDao().insert(
                LogEntry(
                    sender    = senderNormalized,
                    timestamp = System.currentTimeMillis(),
                    status    = "failed: ${e.message?.take(120)}"
                )
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
