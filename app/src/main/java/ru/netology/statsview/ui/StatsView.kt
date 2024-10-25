package ru.netology.statsview.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import ru.netology.statsview.R
import ru.netology.statsview.util.AndroidUtils
import kotlin.math.min
import kotlin.random.Random

class StatsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(
    context,
    attrs,
    defStyleAttr,
    defStyleRes
) {
    private var radius = 0F
    private var center = PointF()
    private var lineWidth = AndroidUtils.dp(context, 15)
    private var textSize = AndroidUtils.dp(context, 20).toFloat()
    private var colors = emptyList<Int>()
    private var colorCircle = 0
    private val paint = Paint(
        Paint.ANTI_ALIAS_FLAG,
    ).apply {
        strokeWidth = lineWidth.toFloat()
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        style = Paint.Style.STROKE
    }
    private val textPaint = Paint(
        Paint.ANTI_ALIAS_FLAG,
    ).apply {
        textSize = this@StatsView.textSize
        textAlign = Paint.Align.CENTER
        style = Paint.Style.FILL
    }
    var oval = RectF()

    var data: List<Float> = emptyList()
        set(value) {
            field = value
            invalidate()
        }

    init {
        context.withStyledAttributes(attrs, R.styleable.StatsView) {
            lineWidth = getDimension(R.styleable.StatsView_lineWidth, lineWidth.toFloat()).toInt()
            textSize = getDimension(R.styleable.StatsView_textSize, textSize)
            colors = listOf(
                getColor(R.styleable.StatsView_color1, generateRandomColor()),
                getColor(R.styleable.StatsView_color2, generateRandomColor()),
                getColor(R.styleable.StatsView_color3, generateRandomColor()),
                getColor(R.styleable.StatsView_color4, generateRandomColor()),
            )
            colorCircle = getColor(R.styleable.StatsView_colorCircle, generateRandomColor())
        }
    }

    private fun generateRandomColor() = Random.nextInt(0xFF000000.toInt(), 0xFFFFFFFF.toInt())

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        radius = min(w, h) / 2F - lineWidth
        center = PointF(w / 2F, h / 2F)
        oval = RectF(
            center.x - radius,
            center.y - radius,
            center.x + radius,
            center.y + radius
        )
    }

    override fun onDraw(canvas: Canvas) {
        if (data.isEmpty()) return

        canvas.drawCircle(center.x, center.y, radius, paint.apply { color = colorCircle })

        var startAngle = -90F

        var startColor: Int? = null
        val max = data.maxOrNull() ?: 0F
        var percent = if (max == 0F) 0F else data.sum() / (max * 4F);
        data.forEachIndexed { index, datum ->
            val angle = datum/max * 90F;
            paint.color = colors.getOrElse(index) { generateRandomColor() }
            if (startColor == null) {
                startColor = paint.color
            }
            canvas.drawArc(oval, startAngle, angle, false, paint)
            startAngle += angle
        }
        startColor?.let {
            canvas.drawPoint(center.x, center.y - radius, paint.apply { color = it })
        }

        canvas.drawText(
            "%.2f%%".format(percent * 100F),
            center.x,
            center.y + textPaint.textSize / 4,
            textPaint
        )
    }
}