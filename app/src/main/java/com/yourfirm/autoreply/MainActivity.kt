package com.yourfirm.autoreply

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.yourfirm.autoreply.databinding.ActivityMainBinding
import com.yourfirm.autoreply.ui.dashboard.DashboardFragment
import com.yourfirm.autoreply.ui.log.LogFragment
import com.yourfirm.autoreply.ui.settings.SettingsFragment
import com.yourfirm.autoreply.ui.setup.SetupGuideActivity
import com.yourfirm.autoreply.ui.template.TemplateFragment
import com.yourfirm.autoreply.ui.whitelist.WhitelistFragment

/**
 * Single-activity host for the bottom-navigation tabs.
 *
 * On first launch the user is redirected to [SetupGuideActivity] which walks
 * them through battery optimisation steps. After that they land here every time.
 *
 * Navigation flow:
 *   Bottom nav item tapped → replace the fragment container with the target fragment.
 *   We use replace() + addToBackStack(null) so the back button works naturally.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Route first-time users to the battery setup guide
        val prefs = (application as App).prefs
        if (prefs.isFirstLaunch()) {
            startActivity(Intent(this, SetupGuideActivity::class.java))
            finish()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Show Dashboard on first load (no savedInstanceState = fresh open)
        if (savedInstanceState == null) {
            showFragment(DashboardFragment())
        }

        // Bottom navigation — each item swaps the fragment in the container
        binding.bottomNav.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.nav_dashboard -> DashboardFragment()
                R.id.nav_whitelist -> WhitelistFragment()
                R.id.nav_template  -> TemplateFragment()
                R.id.nav_log       -> LogFragment()
                R.id.nav_settings  -> SettingsFragment()
                else -> return@setOnItemSelectedListener false
            }
            showFragment(fragment)
            true
        }
    }

    private fun showFragment(fragment: androidx.fragment.app.Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
