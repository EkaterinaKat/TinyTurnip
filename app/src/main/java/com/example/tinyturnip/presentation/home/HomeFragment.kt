package com.example.tinyturnip.presentation.home

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.tinyturnip.R
import com.example.tinyturnip.TinyTurnipApp
import com.example.tinyturnip.data.local.entity.ActEntity
import com.example.tinyturnip.data.local.entity.LogType
import com.example.tinyturnip.presentation.common.ActColors
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class HomeFragment : Fragment(R.layout.fragment_home) {

    private val app get() = requireActivity().application as TinyTurnipApp

    private lateinit var stateTextView: TextView
    private lateinit var detailsTextView: TextView
    private lateinit var itemsContainer: LinearLayout
    private lateinit var stopButton: View

    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        stateTextView = view.findViewById(R.id.stateTextView)
        detailsTextView = view.findViewById(R.id.detailsTextView)
        itemsContainer = view.findViewById(R.id.itemsContainer)

        stopButton = view.findViewById(R.id.stopButton)
        stopButton.setOnClickListener { onStopClicked() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { app.actRepository.getAll().collect(::renderActs) }
                launch {
                    // Re-render every tick so the elapsed time stays current.
                    val tickMs = TICK_UNIT.toMillis(1)
                    while (true) {
                        refreshState()
                        delay(tickMs - System.currentTimeMillis() % tickMs)
                    }
                }
            }
        }
    }

    private fun renderActs(acts: List<ActEntity>) {
        itemsContainer.removeAllViews()
        val inflater = layoutInflater
        acts.forEach { act ->
            val item = inflater.inflate(R.layout.item_home_act, itemsContainer, false) as MaterialButton
            item.text = act.name
            item.backgroundTintList = ColorStateList.valueOf(ActColors.colorFor(requireContext(), act.id))
            item.setOnClickListener { onActClicked(act) }
            itemsContainer.addView(item)
        }
    }

    private suspend fun refreshState() {
        val latest = app.logRepository.getLatest()
        val now = System.currentTimeMillis()
        val today = dateFormat.format(Date(now))

        // Disabled button is drawn grey, enabled one red (see stop_button_background).
        stopButton.isEnabled = latest?.logType == LogType.ACT_START

        if (latest == null || latest.logType == LogType.FINISH) {
            stateTextView.text = ""
            detailsTextView.text = today
            return
        }

        val act = latest.actId?.let { app.actRepository.getById(it) }
        val elapsed = TICK_UNIT.convert(now - latest.timestamp, TimeUnit.MILLISECONDS)
        stateTextView.text = act?.name.orEmpty()
        detailsTextView.text = getString(
            R.string.home_details_since,
            today,
            timeFormat.format(Date(latest.timestamp)),
            elapsed,
        )
    }

    private fun onActClicked(act: ActEntity) {
        viewLifecycleOwner.lifecycleScope.launch {
            app.logRepository.createActStart(act.id)
            refreshState()
        }
    }

    private fun onStopClicked() {
        viewLifecycleOwner.lifecycleScope.launch {
            val latest = app.logRepository.getLatest()
            if (latest?.logType != LogType.ACT_START) return@launch
            app.logRepository.createFinish()
            refreshState()
        }
    }

    private companion object {
        val TICK_UNIT = TimeUnit.MINUTES
    }
}
