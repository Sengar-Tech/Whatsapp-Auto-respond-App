package com.yourfirm.autoreply.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.yourfirm.autoreply.service.ForegroundService
import com.yourfirm.autoreply.util.PrefsManager

/**
 * Restarts the foreground service after the phone reboots.
 * Only starts if the user had the service enabled before the reboot.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val prefs = PrefsManager(context)
            if (prefs.isServiceEnabled()) {
                ForegroundService.start(context)
            }
        }
    }
}
