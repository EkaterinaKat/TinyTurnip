package com.example.tinyturnip.presentation.acts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.tinyturnip.R
import com.example.tinyturnip.data.local.entity.ActEntity

class ActsAdapter(
    private val onActClick: (ActEntity) -> Unit,
) : ListAdapter<ActEntity, ActsAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_act, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val nameTextView: TextView = view.findViewById(R.id.nameTextView)
        private val descriptionTextView: TextView = view.findViewById(R.id.descriptionTextView)

        fun bind(act: ActEntity) {
            nameTextView.text = act.name
            descriptionTextView.text = act.description
            descriptionTextView.visibility = if (act.description.isBlank()) View.GONE else View.VISIBLE
            itemView.setOnClickListener { onActClick(act) }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<ActEntity>() {
        override fun areItemsTheSame(oldItem: ActEntity, newItem: ActEntity) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: ActEntity, newItem: ActEntity) = oldItem == newItem
    }
}
