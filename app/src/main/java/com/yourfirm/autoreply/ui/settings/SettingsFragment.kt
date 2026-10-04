package com.yourfirm.autoreply.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.yourfirm.autoreply.App
import com.yourfirm.autoreply.R
import com.yourfirm.autoreply.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = (requireActivity().application as App).prefs

        // Load existing values — token field uses password input type so it's masked
        binding.accessToken.setText(prefs.getAccessToken())
        binding.phoneNumberId.setText(prefs.getPhoneNumberId())
        binding.templateName.setText(prefs.getTemplateName())
        binding.countryCode.setText(prefs.getCountryCode())

        binding.saveButton.setOnClickListener {
            val token      = binding.accessToken.text.toString().trim()
            val phoneId    = binding.phoneNumberId.text.toString().trim()
            val tmplName   = binding.templateName.text.toString().trim()
            val countryCode = binding.countryCode.text.toString().trim().ifBlank { "91" }

            prefs.setAccessToken(token)
            prefs.setPhoneNumberId(phoneId)
            prefs.setTemplateName(tmplName)
            prefs.setCountryCode(countryCode)

            Toast.makeText(requireContext(), getString(R.string.settings_saved), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
