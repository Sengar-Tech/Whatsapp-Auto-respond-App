package com.yourfirm.autoreply.util

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Thin wrapper around EncryptedSharedPreferences.
 *
 * AES256_GCM is the strongest key scheme available in the security-crypto library.
 * The token is never logged — see error handling in NotificationService.
 *
 * Full implementation detail: Phase 3.
 * Stub here exposes only what Phase 1 and Phase 2 need (isFirstLaunch, isServiceEnabled).
 */
class PrefsManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "autoreply_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // ── Access Token (Meta API) ───────────────────────────────────────────────
    fun getAccessToken(): String = prefs.getString("access_token", "") ?: ""
    fun setAccessToken(token: String) = prefs.edit().putString("access_token", token).apply()

    // ── Phone Number ID (Meta API) ────────────────────────────────────────────
    fun getPhoneNumberId(): String = prefs.getString("phone_number_id", "") ?: ""
    fun setPhoneNumberId(id: String) = prefs.edit().putString("phone_number_id", id).apply()

    // ── Template settings ─────────────────────────────────────────────────────
    fun getTemplateName(): String = prefs.getString("template_name", "") ?: ""
    fun setTemplateName(name: String) = prefs.edit().putString("template_name", name).apply()

    fun getTemplateText(): String = prefs.getString("template_text", "") ?: ""
    fun setTemplateText(text: String) = prefs.edit().putString("template_text", text).apply()

    fun getTemplateImageUri(): String = prefs.getString("template_image_uri", "") ?: ""
    fun setTemplateImageUri(uri: String) = prefs.edit().putString("template_image_uri", uri).apply()

    fun getTemplateUrl(): String = prefs.getString("template_url", "") ?: ""
    fun setTemplateUrl(url: String) = prefs.edit().putString("template_url", url).apply()

    // ── Service state ─────────────────────────────────────────────────────────
    fun isServiceEnabled(): Boolean = prefs.getBoolean("service_enabled", false)
    fun setServiceEnabled(enabled: Boolean) = prefs.edit().putBoolean("service_enabled", enabled).apply()

    // ── First launch flag ─────────────────────────────────────────────────────
    fun isFirstLaunch(): Boolean = prefs.getBoolean("first_launch", true)
    fun setFirstLaunchDone() = prefs.edit().putBoolean("first_launch", false).apply()

    // ── Country code — prepended when sending to Meta API ────────────────────
    fun getCountryCode(): String = prefs.getString("country_code", "91") ?: "91"
    fun setCountryCode(code: String) = prefs.edit().putString("country_code", code).apply()

    // ── Service health timestamp ───────────────────────────────────────────────
    fun getLastServiceActivity(): Long = prefs.getLong("last_service_activity", 0L)
    fun setLastServiceActivity(ts: Long) = prefs.edit().putLong("last_service_activity", ts).apply()
}
