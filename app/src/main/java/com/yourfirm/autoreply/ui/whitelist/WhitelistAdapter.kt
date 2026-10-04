package com.yourfirm.autoreply.ui.whitelist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yourfirm.autoreply.databinding.ItemWhitelistBinding
import com.yourfirm.autoreply.db.entity.WhitelistEntry

/**
 * ListAdapter uses DiffUtil to calculate minimal updates to the RecyclerView —
 * only changed rows are redrawn, not the whole list.
 */
class WhitelistAdapter(
    private val onDelete: (WhitelistEntry) -> Unit
) : ListAdapter<WhitelistEntry, WhitelistAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemWhitelistBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemWhitelistBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(entry: WhitelistEntry) {
            binding.numberText.text = entry.normalizedNumber
            if (entry.displayLabel.isNotBlank()) {
                binding.labelText.text = entry.displayLabel
                binding.labelText.visibility = android.view.View.VISIBLE
            } else {
                binding.labelText.visibility = android.view.View.GONE
            }
            binding.deleteButton.setOnClickListener { onDelete(entry) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<WhitelistEntry>() {
            override fun areItemsTheSame(a: WhitelistEntry, b: WhitelistEntry) =
                a.normalizedNumber == b.normalizedNumber
            override fun areContentsTheSame(a: WhitelistEntry, b: WhitelistEntry) = a == b
        }
    }
}
