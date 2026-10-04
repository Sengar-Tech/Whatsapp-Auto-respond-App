package com.yourfirm.autoreply.ui.template

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.yourfirm.autoreply.App
import com.yourfirm.autoreply.R
import com.yourfirm.autoreply.databinding.FragmentTemplateBinding
import java.io.File

/**
 * Lets the user:
 *  1. Pick a PNG from their gallery (stored in app's private files dir so the URI stays valid)
 *  2. View the approved template text (reference only — not sent dynamically)
 *  3. Enter the optional URL for the template button
 *  4. Save all three to EncryptedSharedPreferences
 */
class TemplateFragment : Fragment() {

    private var _binding: FragmentTemplateBinding? = null
    private val binding get() = _binding!!

    private var selectedImageUri: Uri? = null

    // Must be registered before onStart — fragment field initializer is the safe place
    private val imagePicker = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.imagePreview.setImageURI(it)
            binding.imagePreview.visibility = View.VISIBLE
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTemplateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = (requireActivity().application as App).prefs

        // Load saved values
        binding.templateText.setText(prefs.getTemplateText())
        binding.templateUrl.setText(prefs.getTemplateUrl())

        val existingUri = prefs.getTemplateImageUri()
        if (existingUri.isNotBlank()) {
            try {
                binding.imagePreview.setImageURI(Uri.parse(existingUri))
                binding.imagePreview.visibility = View.VISIBLE
            } catch (_: Exception) {
                // URI may be invalid after reinstall — user will re-select
            }
        }

        binding.selectImageButton.setOnClickListener {
            imagePicker.launch("image/png")
        }

        binding.saveButton.setOnClickListener {
            val text = binding.templateText.text.toString().trim()
            val url  = binding.templateUrl.text.toString().trim()

            if (text.isBlank()) {
                Toast.makeText(requireContext(), getString(R.string.template_text_empty), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Persist selected image to app-private storage so the URI survives app restarts
            selectedImageUri?.let { uri ->
                val savedUri = saveImageToPrivateStorage(uri)
                prefs.setTemplateImageUri(savedUri.toString())
            }

            prefs.setTemplateText(text)
            prefs.setTemplateUrl(url)

            Toast.makeText(requireContext(), getString(R.string.template_saved), Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Copies the picked image from the content URI into app's [filesDir].
     * The [filesDir] path is stable across app updates (but not reinstalls).
     */
    private fun saveImageToPrivateStorage(uri: Uri): Uri {
        val inputStream = requireContext().contentResolver.openInputStream(uri)!!
        val file        = File(requireContext().filesDir, "template_image.png")
        file.outputStream().use { out -> inputStream.use { it.copyTo(out) } }
        return Uri.fromFile(file)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
