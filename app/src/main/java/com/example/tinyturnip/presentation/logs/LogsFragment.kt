package com.example.tinyturnip.presentation.logs

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.example.tinyturnip.R
import com.example.tinyturnip.TinyTurnipApp
import com.example.tinyturnip.data.local.entity.LogEntity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class LogsFragment : Fragment(R.layout.fragment_logs) {

    private val app get() = requireActivity().application as TinyTurnipApp

    private val adapter = LogsAdapter(::onLogClicked)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val emptyTextView = view.findViewById<TextView>(R.id.emptyTextView)
        view.findViewById<RecyclerView>(R.id.logsRecyclerView).adapter = adapter
        view.findViewById<View>(R.id.addLogButton).setOnClickListener {
            startActivity(Intent(requireContext(), ManualLogActivity::class.java))
        }

        val items = combine(app.logRepository.getAll(), app.actRepository.getAll(), LogListItem::build)
            .flowOn(Dispatchers.Default)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                items.collect { list ->
                    adapter.submitList(list)
                    emptyTextView.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }

    private fun onLogClicked(log: LogEntity) {
        MaterialAlertDialogBuilder(requireContext())
            .setItems(arrayOf(getString(R.string.log_delete))) { _, _ -> deleteLog(log) }
            .show()
    }

    private fun deleteLog(log: LogEntity) {
        viewLifecycleOwner.lifecycleScope.launch {
            if (!app.logRepository.delete(log.id)) {
                MaterialAlertDialogBuilder(requireContext())
                    .setMessage(R.string.log_delete_impossible)
                    .setPositiveButton(R.string.ok, null)
                    .show()
            }
        }
    }
}
