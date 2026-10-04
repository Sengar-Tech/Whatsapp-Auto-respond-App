package com.yourfirm.autoreply.ui.setup

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yourfirm.autoreply.databinding.ItemSetupStepBinding

class SetupStepAdapter(private val steps: List<String>) :
    RecyclerView.Adapter<SetupStepAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSetupStepBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position + 1, steps[position])
    }

    override fun getItemCount() = steps.size

    class ViewHolder(private val binding: ItemSetupStepBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(stepNumber: Int, instruction: String) {
            binding.stepNumber.text = stepNumber.toString()
            binding.stepText.text   = instruction
        }
    }
}
