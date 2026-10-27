package com.example.tinyturnip.presentation.logs

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TimePicker
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.tinyturnip.R
import com.example.tinyturnip.TinyTurnipApp
import com.example.tinyturnip.data.local.entity.ActEntity
import com.example.tinyturnip.data.local.entity.LogType
import com.example.tinyturnip.presentation.common.ActColors
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.util.Calendar

/** Creates a log with a manually chosen type, act and date/time. */
class ManualLogActivity : AppCompatActivity() {

    private val app get() = application as TinyTurnipApp

    private lateinit var typeSpinner: Spinner
    private lateinit var actsContainer: LinearLayout
    private lateinit var datePicker: DatePicker
    private lateinit var timePicker: TimePicker

    private var selectedActId: Long? = null

    private val selectedType: LogType get() = LogType.entries[typeSpinner.selectedItemPosition]

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_manual_log)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        findViewById<MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener { finish() }

        typeSpinner = findViewById(R.id.typeSpinner)
        actsContainer = findViewById(R.id.actsContainer)
        datePicker = findViewById(R.id.datePicker)
        timePicker = findViewById(R.id.timePicker)

        selectedActId = savedInstanceState?.getLong(KEY_SELECTED_ACT_ID, NO_ACT_ID)?.takeIf { it != NO_ACT_ID }

        typeSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            LogType.entries.map { getString(it.labelRes()) },
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        typeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) =
                updateActsEnabled()

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        // DatePicker is initialised with the current date by default, TimePicker needs it explicitly.
        // On recreation both restore their own state.
        timePicker.setIs24HourView(DateFormat.is24HourFormat(this))
        if (savedInstanceState == null) {
            val now = Calendar.getInstance()
            timePicker.hour = now.get(Calendar.HOUR_OF_DAY)
            timePicker.minute = now.get(Calendar.MINUTE)
        }

        findViewById<View>(R.id.saveButton).setOnClickListener { save() }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                app.actRepository.getAll().collect(::renderActs)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong(KEY_SELECTED_ACT_ID, selectedActId ?: NO_ACT_ID)
    }

    private fun renderActs(acts: List<ActEntity>) {
        if (acts.none { it.id == selectedActId }) selectedActId = null
        actsContainer.removeAllViews()
        acts.forEach { act ->
            val item = layoutInflater.inflate(R.layout.item_home_act, actsContainer, false) as MaterialButton
            item.text = act.name
            item.tag = act.id
            item.backgroundTintList = ColorStateList.valueOf(ActColors.colorFor(this, act.id))
            item.strokeColor = ColorStateList.valueOf(
                MaterialColors.getColor(item, androidx.appcompat.R.attr.colorPrimary)
            )
            item.setOnClickListener {
                selectedActId = act.id
                updateActsSelection()
            }
            actsContainer.addView(item)
        }
        updateActsSelection()
        updateActsEnabled()
    }

    private fun updateActsSelection() {
        val strokeWidth = resources.getDimensionPixelSize(R.dimen.manual_log_selected_stroke)
        actsContainer.children.forEach { item ->
            (item as MaterialButton).strokeWidth = if (item.tag == selectedActId) strokeWidth else 0
        }
    }

    /** Acts are available only for [LogType.ACT_START]. */
    private fun updateActsEnabled() {
        val enabled = selectedType == LogType.ACT_START
        actsContainer.alpha = if (enabled) 1f else DISABLED_ALPHA
        actsContainer.children.forEach { it.isEnabled = enabled }
    }

    private fun save() {
        val type = selectedType
        val actId = selectedActId
        if (type == LogType.ACT_START && actId == null) {
            showError(R.string.manual_log_act_required)
            return
        }
        val timestamp = Calendar.getInstance().apply {
            clear()
            set(datePicker.year, datePicker.month, datePicker.dayOfMonth, timePicker.hour, timePicker.minute)
        }.timeInMillis

        lifecycleScope.launch {
            if (app.logRepository.createManual(type, actId, timestamp)) {
                finish()
            } else {
                showError(R.string.manual_log_not_latest)
            }
        }
    }

    private fun showError(messageRes: Int) {
        MaterialAlertDialogBuilder(this)
            .setMessage(messageRes)
            .setPositiveButton(R.string.ok, null)
            .show()
    }

    private fun LogType.labelRes() = when (this) {
        LogType.ACT_START -> R.string.log_type_start
        LogType.FINISH -> R.string.log_type_finish
    }

    private companion object {
        const val KEY_SELECTED_ACT_ID = "selected_act_id"
        const val NO_ACT_ID = -1L
        const val DISABLED_ALPHA = 0.38f
    }
}
