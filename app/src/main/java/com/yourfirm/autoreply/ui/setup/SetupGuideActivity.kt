package com.yourfirm.autoreply.ui.setup

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.yourfirm.autoreply.App
import com.yourfirm.autoreply.MainActivity
import com.yourfirm.autoreply.databinding.ActivitySetupGuideBinding

/**
 * Shown exactly once on first launch.
 * Detects the phone brand and shows the specific battery settings the user must
 * configure to prevent the OS from killing the NotificationListenerService.
 *
 * After "Done" is tapped: sets first_launch=false in prefs and opens MainActivity.
 */
class SetupGuideActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySetupGuideBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupGuideBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val steps   = stepsForThisDevice()
        val adapter = SetupStepAdapter(steps)

        binding.stepsRecycler.apply {
            this.adapter  = adapter
            layoutManager = LinearLayoutManager(this@SetupGuideActivity)
        }

        binding.doneButton.setOnClickListener {
            (application as App).prefs.setFirstLaunchDone()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    // ── Brand detection ───────────────────────────────────────────────────────

    private fun stepsForThisDevice(): List<String> {
        val manufacturer = Build.MANUFACTURER.lowercase()
        val model        = Build.MODEL.lowercase()

        return when {
            manufacturer.contains("xiaomi") ||
            manufacturer.contains("redmi")  ||
            model.contains("poco")          -> xiaomiSteps()

            manufacturer.contains("motorola") ||
            manufacturer.contains("moto")     -> motorolaSteps()

            manufacturer.contains("samsung")  -> samsungSteps()

            manufacturer.contains("oppo")   ||
            manufacturer.contains("realme") ||
            manufacturer.contains("vivo")   -> oppoSteps()

            else -> genericSteps()
        }
    }

    // ── Step lists ────────────────────────────────────────────────────────────

    private fun xiaomiSteps() = listOf(
        "Settings → Apps → Manage apps → AutoReply → Battery Saver → No restrictions",
        "Settings → Apps → Manage apps → AutoReply → Autostart → Toggle ON",
        "Settings → Battery & Performance → Choose apps → AutoReply → No restrictions",
        "Settings → Additional Settings → Developer Options → MIUI Optimization → Toggle OFF\n(Enable Developer Options first: tap MIUI version 7 times in About Phone)",
        "Open Recents → long press AutoReply card → tap the Lock icon to prevent memory cleaner from killing it"
    )

    private fun motorolaSteps() = listOf(
        "Settings → Battery → Battery Optimization → All Apps → AutoReply → Don't optimize",
        "Settings → Apps → AutoReply → Battery → Unrestricted"
    )

    private fun samsungSteps() = listOf(
        "Settings → Battery → Background usage limits → Never sleeping apps → tap + → add AutoReply",
        "Settings → Apps → AutoReply → Battery → Unrestricted",
        "(Optional) Settings → Battery → Adaptive battery → Toggle OFF for most aggressive fix"
    )

    private fun oppoSteps() = listOf(
        "Settings → Battery → App Quick Freeze → find AutoReply → tap Exclude",
        "Settings → Apps → AutoReply → Battery → Allow background activity → ON",
        "Phone Manager → App battery saver → AutoReply → No restrictions",
        "If you see a notification 'AutoReply is using battery in background' → tap Allow"
    )

    private fun genericSteps() = listOf(
        "Settings → Battery → Battery Optimization → All Apps → AutoReply → Don't optimize",
        "Settings → Apps → AutoReply → Battery → Unrestricted"
    )
}
