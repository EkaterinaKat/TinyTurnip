package com.example.tinyturnip.presentation.charts

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.TextViewCompat
import com.example.tinyturnip.R

/** Lists [PieSlice]s top to bottom as a color marker followed by the slice label. */
class PieChartLegendView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr) {

    private val textAppearanceRes = TypedValue().also {
        context.theme.resolveAttribute(com.google.android.material.R.attr.textAppearanceBodyMedium, it, true)
    }.resourceId

    init {
        orientation = VERTICAL
    }

    fun setSlices(slices: List<PieSlice>) {
        removeAllViews()
        slices.forEach { addView(createItem(it)) }
    }

    private fun createItem(slice: PieSlice): TextView {
        val markerSize = resources.getDimensionPixelSize(R.dimen.charts_legend_marker_size)
        val marker = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(slice.color)
            setSize(markerSize, markerSize)
        }
        return TextView(context).apply {
            TextViewCompat.setTextAppearance(this, textAppearanceRes)
            text = slice.label
            setCompoundDrawablesRelativeWithIntrinsicBounds(marker, null, null, null)
            compoundDrawablePadding = resources.getDimensionPixelSize(R.dimen.charts_legend_marker_padding)
            val spacing = resources.getDimensionPixelSize(R.dimen.charts_legend_item_spacing)
            setPadding(0, spacing, 0, spacing)
        }
    }
}
