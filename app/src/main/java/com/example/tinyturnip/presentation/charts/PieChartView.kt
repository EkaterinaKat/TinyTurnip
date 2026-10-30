package com.example.tinyturnip.presentation.charts

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.example.tinyturnip.R
import com.google.android.material.color.MaterialColors
import kotlin.math.min

/**
 * Draws ready-made [PieSlice]s as a ring centred in the view with an optional text in the hole;
 * all calculations live in [PieChartBuilder].
 */
class PieChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private val slicePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = resources.getDimension(R.dimen.charts_pie_center_text_size)
        color = MaterialColors.getColor(this@PieChartView, com.google.android.material.R.attr.colorOnSurface)
    }

    /** Middle line of the ring, so the stroke spreads evenly inwards and outwards. */
    private val oval = RectF()

    private var slices: List<PieSlice> = emptyList()
    private var centerText: String? = null

    fun setSlices(slices: List<PieSlice>) {
        this.slices = slices
        invalidate()
    }

    fun setCenterText(text: String?) {
        centerText = text
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val contentWidth = w - paddingLeft - paddingRight
        val contentHeight = h - paddingTop - paddingBottom
        val radius = min(contentWidth, contentHeight) / 2f
        val ringWidth = radius * RING_WIDTH_RATIO
        val centerX = paddingLeft + contentWidth / 2f
        val centerY = paddingTop + contentHeight / 2f
        val middleRadius = radius - ringWidth / 2f
        slicePaint.strokeWidth = ringWidth
        oval.set(centerX - middleRadius, centerY - middleRadius, centerX + middleRadius, centerY + middleRadius)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        slices.forEach { slice ->
            slicePaint.color = slice.color
            canvas.drawArc(oval, slice.startAngle, slice.sweepAngle, false, slicePaint)
        }
        centerText?.let { text ->
            val baseline = oval.centerY() - (textPaint.descent() + textPaint.ascent()) / 2f
            canvas.drawText(text, oval.centerX(), baseline, textPaint)
        }
    }

    private companion object {
        /** Ring thickness as a share of the outer radius. */
        const val RING_WIDTH_RATIO = 0.35f
    }
}
