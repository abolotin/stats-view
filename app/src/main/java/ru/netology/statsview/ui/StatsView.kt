package ru.netology.statsview.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
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
    enum class Mode {
        PARALLEL,
        SEQUENTIAL,
        BIDIRECTIONAL
    }
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
    var mode: Mode = Mode.PARALLEL

    var data: List<Float> = emptyList()
        set(value) {
            field = value
            update()
        }
    private var progress = 0F
    var valueAnimator: ValueAnimator? = null

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
            mode = Mode.entries.toTypedArray()[getInt(R.styleable.StatsView_mode, 0)]
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

        val max = data.maxOrNull() ?: 0F
        var percent = if (max == 0F) 0F else data.sum() / (max * 4F);

        val startColor: Int? =
            when(mode) {
                Mode.PARALLEL -> parallelDrawMode(canvas, max)
                Mode.BIDIRECTIONAL -> bidirectionalDrawMode(canvas, max)
                Mode.SEQUENTIAL -> sequentialDrawMode(canvas, max)
                else -> throw IllegalStateException()
            }

        if (progress == 1F) {
            startColor?.let {
                canvas.drawPoint(center.x, center.y - radius, paint.apply { color = it })
            }
        }

        canvas.drawText(
            "%.2f%%".format(percent * 100F),
            center.x,
            center.y + textPaint.textSize / 4,
            textPaint
        )
    }

    private fun parallelDrawMode(canvas: Canvas, max: Float): Int? {
        if (max == 0F) return null;

        var startAngle = -90F
        val angleProgress = 360F * progress
        var startColor: Int? = null

        data.forEachIndexed { index, datum ->
            paint.color = colors.getOrElse(index) { generateRandomColor() }
            if (startColor == null) startColor = paint.color
            val angle = datum / max * 90F;
            canvas.drawArc(oval, startAngle + angleProgress, angle * progress, false, paint)
            startAngle += angle
        }

        return startColor
    }

    private fun sequentialDrawMode(canvas: Canvas, max: Float): Int? {
        if (max == 0F) return null;

        var startAngle = -90F
        var startColor: Int? = null

        data.forEachIndexed { index, datum ->
            if ((index * 0.25F) > progress) return@forEachIndexed
            paint.color = colors.getOrElse(index) { generateRandomColor() }
            if (startColor == null) startColor = paint.color
            var angle = datum / max * 90F;
            val partProgress = (index+1) * 0.25F
            if (partProgress > progress) angle *= (progress - 0.25F*index)/0.25F;
            canvas.drawArc(oval, startAngle, angle, false, paint)
            startAngle += 90F
        }

        return startColor
    }

    private fun bidirectionalDrawMode(canvas: Canvas, max: Float): Int? {
        if (max == 0F) return null;

        var startAngle = -45F
        val angleProgress = 360F * progress
        var startColor: Int? = null

        data.forEachIndexed { index, datum ->
            paint.color = colors.getOrElse(index) { generateRandomColor() }
            if (startColor == null) startColor = paint.color
            val angle = (datum / max * 90F) * progress;
            canvas.drawArc(oval, startAngle - angle/2F, angle, false, paint)
            startAngle += 90F
        }

        return startColor
    }

    private fun update() {
        valueAnimator?.let {
            it.removeAllListeners()
            it.cancel()
        }

        progress = 0F

        valueAnimator = ValueAnimator.ofFloat(0F, 1F).apply {
            addUpdateListener { anim ->
                progress = anim.animatedValue as Float
                invalidate()
            }
            duration = 2000
            interpolator = LinearInterpolator()
        }.also {
            it.start()
        }
    }
}