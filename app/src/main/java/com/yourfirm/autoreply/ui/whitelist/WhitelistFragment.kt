package com.yourfirm.autoreply.ui.whitelist

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.yourfirm.autoreply.App
import com.yourfirm.autoreply.R
import com.yourfirm.autoreply.databinding.FragmentWhitelistBinding
import com.yourfirm.autoreply.db.entity.WhitelistEntry
import com.yourfirm.autoreply.util.PhoneNormalizer
import kotlinx.coroutines.launch

class WhitelistFragment : Fragment() {

    private var _binding: FragmentWhitelistBinding? = null
    private val binding get() = _binding!!

    private val db by lazy { (requireActivity().application as App).database }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentWhitelistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = WhitelistAdapter { entry ->
            lifecycleScope.launch { db.whitelistDao().delete(entry) }
        }

        binding.recyclerView.apply {
            this.adapter = adapter
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        }

        // Collect the Flow — updates the list every time the DB changes
        lifecycleScope.launch {
            db.whitelistDao().getAll().collect { list ->
                adapter.submitList(list)
                binding.emptyText.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        binding.fabAdd.setOnClickListener { showAddDialog() }
    }

    private fun showAddDialog() {
        val input = EditText(requireContext()).apply {
            hint     = getString(R.string.whitelist_hint)
            inputType = InputType.TYPE_CLASS_PHONE
            setPadding(48, 24, 48, 24)
        }

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.add_to_whitelist))
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val raw        = input.text.toString().trim()
                val normalized = PhoneNormalizer.normalize(raw)
                if (normalized.length == 10) {
                    lifecycleScope.launch {
                        db.whitelistDao().insert(WhitelistEntry(
                            normalizedNumber = normalized,
                            displayLabel     = raw
                        ))
                    }
                } else {
                    Toast.makeText(requireContext(), getString(R.string.invalid_number), Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
