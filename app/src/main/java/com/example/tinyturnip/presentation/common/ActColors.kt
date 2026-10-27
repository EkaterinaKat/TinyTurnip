package com.example.tinyturnip.presentation.common

import android.content.Context
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.example.tinyturnip.R

/** Maps an act to a stable pastel color using the last decimal digit of its id. */
object ActColors {

    private val palette = intArrayOf(
        R.color.act_pastel_0,
        R.color.act_pastel_1,
        R.color.act_pastel_2,
        R.color.act_pastel_3,
        R.color.act_pastel_4,
        R.color.act_pastel_5,
        R.color.act_pastel_6,
        R.color.act_pastel_7,
        R.color.act_pastel_8,
        R.color.act_pastel_9,
    )

    @ColorRes
    fun colorResFor(id: Long): Int = palette[Math.floorMod(id, 10L).toInt() % palette.size]

    @ColorInt
    fun colorFor(context: Context, id: Long): Int =
        ContextCompat.getColor(context, colorResFor(id))
}
