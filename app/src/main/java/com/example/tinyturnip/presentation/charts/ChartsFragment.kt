package com.example.tinyturnip.presentation.charts

import android.app.DatePickerDialog
import android.graphics.Typeface
import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.tinyturnip.R
import com.example.tinyturnip.TinyTurnipApp
import com.example.tinyturnip.domain.charts.ChartResult
import androidx.core.content.ContextCompat
import com.example.tinyturnip.presentation.common.ActColors
import com.example.tinyturnip.presentation.common.SwipeLayout
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ChartsFragment : Fragment(R.layout.fragment_charts) {

    private val app get() = requireActivity().application as TinyTurnipApp

    private val pieChartBuilder by lazy {
        PieChartBuilder(
            colorFor = { ActColors.colorFor(requireContext(), it.id) },
            labelFor = { act, minutes -> getString(R.string.charts_legend_item, act.name, minutes.toHours(), minutes) },
        )
    }

    private lateinit var dateTextView: TextView
    private lateinit var summaryTextView: TextView
    private lateinit var pieChartView: PieChartView
    private lateinit var pieChartLegendView: PieChartLegendView
    private lateinit var emptyTextView: TextView

    private val date: Calendar = Calendar.getInstance()
    private var loadJob: Job? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dateTextView = view.findViewById(R.id.dateTextView)
        summaryTextView = view.findViewById(R.id.summaryTextView)
        pieChartView = view.findViewById(R.id.pieChartView)
        pieChartLegendView = view.findViewById(R.id.pieChartLegendView)
        emptyTextView = view.findViewById(R.id.emptyTextView)

        savedInstanceState?.let { date.timeInMillis = it.getLong(KEY_DATE) }
        dateTextView.setOnClickListener { openDatePicker() }
        view.findViewById<SwipeLayout>(R.id.swipeLayout).apply {
            onSwipeRight = { shiftDate(-1) }
            onSwipeLeft = { shiftDate(1) }
        }
        updateDate()
    }

    private fun shiftDate(days: Int) {
        date.add(Calendar.DAY_OF_MONTH, days)
        updateDate()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong(KEY_DATE, date.timeInMillis)
    }

    private fun openDatePicker() {
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                date.set(year, month, dayOfMonth)
                updateDate()
            },
            date.get(Calendar.YEAR),
            date.get(Calendar.MONTH),
            date.get(Calendar.DAY_OF_MONTH),
        ).show()
    }

    private fun updateDate() {
        dateTextView.text = SimpleDateFormat(DATE_PATTERN, Locale.getDefault()).format(date.time)
        setDateViewStyle()
        loadChart()
    }

    /** Today's date is highlighted, like in the KOM main screen. */
    private fun setDateViewStyle() {
        val today = Calendar.getInstance()
        val isToday = date.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            date.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
        val color = if (isToday) R.color.charts_date_today else R.color.charts_date_default
        dateTextView.setTextColor(ContextCompat.getColor(requireContext(), color))
        dateTextView.setTypeface(null, if (isToday) Typeface.BOLD else Typeface.NORMAL)
    }

    private fun loadChart() {
        // ChartService expects a 1-based month.
        val day = date.get(Calendar.DAY_OF_MONTH)
        val month = date.get(Calendar.MONTH) + 1
        val year = date.get(Calendar.YEAR)
        loadJob?.cancel()
        loadJob = viewLifecycleOwner.lifecycleScope.launch {
            val result = app.chartService.calculate(day, month, year)
            render(result)
        }
    }

    private fun render(result: ChartResult) {
        val data = (result as? ChartResult.Success)?.data
        summaryTextView.visibility = if (data == null) View.GONE else View.VISIBLE
        summaryTextView.text = data?.let {
            getString(
                R.string.charts_summary,
                DateFormat.getTimeFormat(requireContext()).format(it.firstStartTime),
                it.spanMinutes.toHours(),
                it.spanMinutes,
                it.totalMinutes.toHours(),
                it.totalMinutes,
            )
        }

        val slices = data?.let { pieChartBuilder.build(it.minutesByAct) }.orEmpty()
        val chartVisibility = if (slices.isEmpty()) View.GONE else View.VISIBLE
        pieChartView.setSlices(slices)
        pieChartView.setCenterText(data?.let { getString(R.string.charts_pie_center, it.totalMinutes.toHours()) })
        pieChartView.visibility = chartVisibility
        pieChartLegendView.setSlices(slices)
        pieChartLegendView.visibility = chartVisibility
        emptyTextView.visibility = if (slices.isEmpty()) View.VISIBLE else View.GONE
        emptyTextView.text = if (result is ChartResult.Error) result.message else getString(R.string.charts_empty)
    }

    /** Rounding to one decimal is done by the `%.1f` placeholders in the string resources. */
    private fun Int.toHours(): Double = this / MINUTES_IN_HOUR

    private companion object {
        const val MINUTES_IN_HOUR = 60.0
        const val KEY_DATE = "date"
        const val DATE_PATTERN = "dd.MM.yyyy (EE)"
    }
}
