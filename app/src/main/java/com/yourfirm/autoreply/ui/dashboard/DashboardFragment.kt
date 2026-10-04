package com.yourfirm.autoreply.ui.dashboard

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.yourfirm.autoreply.App
import com.yourfirm.autoreply.databinding.FragmentDashboardBinding
import com.yourfirm.autoreply.service.ForegroundService
import kotlinx.coroutines.launch
import java.util.Calendar

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val app   = requireActivity().application as App
        val prefs = app.prefs
        val db    = app.database

        // Initialise toggle state from prefs
        binding.serviceToggle.isChecked = prefs.isServiceEnabled()
        updateStatusIndicator(prefs.isServiceEnabled())

        binding.serviceToggle.setOnCheckedChangeListener { _, isChecked ->
            prefs.setServiceEnabled(isChecked)
            if (isChecked) {
                if (!isNotificationListenerEnabled()) {
                    // Redirect user to grant permission — toggle won't start service yet
                    startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                    binding.serviceToggle.isChecked = false
                    prefs.setServiceEnabled(false)
                } else {
                    ForegroundService.start(requireContext())
                    updateStatusIndicator(true)
                }
            } else {
                ForegroundService.stop(requireContext())
                updateStatusIndicator(false)
            }
        }

        // Today's reply count
        lifecycleScope.launch {
            val since = todayMidnightMs()
            val count = db.logDao().countSentSince(since)
            binding.repliesCount.text = "$count replies sent today"
        }
    }

    override fun onResume() {
        super.onResume()
        checkPermissionBanner()
        checkServiceHealthBanner()
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun updateStatusIndicator(running: Boolean) {
        binding.statusDot.setBackgroundColor(
            if (running) Color.parseColor("#4CAF50") else Color.parseColor("#F44336")
        )
        binding.statusText.text = if (running) getString(com.yourfirm.autoreply.R.string.service_running)
                                  else         getString(com.yourfirm.autoreply.R.string.service_stopped)
    }

    private fun checkPermissionBanner() {
        if (!isNotificationListenerEnabled()) {
            binding.permissionWarning.visibility = View.VISIBLE
            binding.permissionWarning.setOnClickListener {
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            }
        } else {
            binding.permissionWarning.visibility = View.GONE
        }
    }

    private fun checkServiceHealthBanner() {
        val prefs = (requireActivity().application as App).prefs
        if (!prefs.isServiceEnabled()) {
            binding.serviceWarning.visibility = View.GONE
            return
        }
        val lastActivity  = prefs.getLastServiceActivity()
        val minutesSince  = (System.currentTimeMillis() - lastActivity) / 60_000L
        if (minutesSince > 30 && lastActivity > 0) {
            binding.serviceWarning.visibility = View.VISIBLE
            binding.serviceWarning.setOnClickListener {
                ForegroundService.start(requireContext())
            }
        } else {
            binding.serviceWarning.visibility = View.GONE
        }
    }

    private fun isNotificationListenerEnabled(): Boolean {
        val flat = Settings.Secure.getString(
            requireContext().contentResolver,
            "enabled_notification_listeners"
        )
        return flat?.contains(requireContext().packageName) == true
    }

    private fun todayMidnightMs(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
