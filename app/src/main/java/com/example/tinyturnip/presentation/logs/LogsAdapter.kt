package com.example.tinyturnip.presentation.logs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.tinyturnip.R
import com.example.tinyturnip.data.local.entity.LogEntity
import com.example.tinyturnip.data.local.entity.LogType
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LogsAdapter(
    private val onLogClick: (LogEntity) -> Unit,
) : ListAdapter<LogListItem, RecyclerView.ViewHolder>(DiffCallback) {

    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is LogListItem.DateHeader -> VIEW_TYPE_DATE
        is LogListItem.Log -> VIEW_TYPE_LOG
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_DATE -> DateViewHolder(inflater.inflate(R.layout.item_log_date, parent, false))
            else -> LogViewHolder(inflater.inflate(R.layout.item_log, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is LogListItem.DateHeader -> (holder as DateViewHolder).bind(item)
            is LogListItem.Log -> (holder as LogViewHolder).bind(item)
        }
    }

    inner class DateViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val dateTextView: TextView = view.findViewById(R.id.dateTextView)

        fun bind(item: LogListItem.DateHeader) {
            dateTextView.text = dateFormat.format(Date(item.dayStart))
        }
    }

    inner class LogViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val cardView = view as MaterialCardView
        private val timeTextView: TextView = view.findViewById(R.id.timeTextView)
        private val actTextView: TextView = view.findViewById(R.id.actTextView)
        private val typeTextView: TextView = view.findViewById(R.id.typeTextView)

        fun bind(item: LogListItem.Log) {
            timeTextView.text = timeFormat.format(Date(item.log.timestamp))
            val backgroundColorRes = when (item.log.logType) {
                LogType.ACT_START -> {
                    actTextView.text = item.actName
                    typeTextView.setText(R.string.log_type_start)
                    R.color.log_tile_start_background
                }
                LogType.FINISH -> {
                    // FINISH doesn't belong to any act.
                    actTextView.text = null
                    typeTextView.setText(R.string.log_type_finish)
                    R.color.log_tile_finish_background
                }
            }
            cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.context, backgroundColorRes))
            itemView.setOnClickListener { onLogClick(item.log) }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<LogListItem>() {
        override fun areItemsTheSame(oldItem: LogListItem, newItem: LogListItem) = when {
            oldItem is LogListItem.DateHeader && newItem is LogListItem.DateHeader ->
                oldItem.dayStart == newItem.dayStart
            oldItem is LogListItem.Log && newItem is LogListItem.Log ->
                oldItem.log.id == newItem.log.id
            else -> false
        }

        override fun areContentsTheSame(oldItem: LogListItem, newItem: LogListItem) = oldItem == newItem
    }

    private companion object {
        const val VIEW_TYPE_DATE = 0
        const val VIEW_TYPE_LOG = 1
    }
}
