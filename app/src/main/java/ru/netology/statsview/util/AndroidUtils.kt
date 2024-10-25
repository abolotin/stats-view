package ru.netology.statsview.util

import android.content.Context
import kotlin.math.ceil

object AndroidUtils {
    fun dp(context: Context, value: Int): Int =
        ceil(value * context.resources.displayMetrics.density).toInt()
}