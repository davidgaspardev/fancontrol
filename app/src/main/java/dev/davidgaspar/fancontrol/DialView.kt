package dev.davidgaspar.fancontrol

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.content.withStyledAttributes
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private enum class FanSpeed(val label: Int) {
    OFF(R.string.fan_off),
    LOW(R.string.fan_low),
    MEDIUM(R.string.fan_medium),
    HIGH(R.string.fan_high);

    fun next() = when (this) {
        OFF -> LOW
        LOW -> MEDIUM
        MEDIUM -> HIGH
        HIGH -> OFF
    }
}

private const val RADIUS_OFFSET_LABEL = 35
private const val RADIUS_OFFSET_INDICATOR = -35

class DialView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var radius = 0.0f
    private var fanSpeed = FanSpeed.OFF
    private var fanSpeedLowColor = 0
    private var fanSpeedMediumColor = 0
    private var fanSpeedHighColor = 0

    private var pointPosition: PointF = PointF(0.0f, 0.0f)
    private var paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
        typeface = Typeface.create("", Typeface.BOLD)
    }

    init {
        isClickable = true

        context.withStyledAttributes(attrs, R.styleable.DialView) {
            fanSpeedLowColor = getColor(R.styleable.DialView_fanColor1, 0)
            fanSpeedMediumColor = getColor(R.styleable.DialView_fanColor2, 0)
            fanSpeedHighColor = getColor(R.styleable.DialView_fanColor3, 0)
        }
    }

    private fun PointF.goToCenter() {
        x = (width / 2).toFloat()
        y = (height / 2).toFloat()
    }

    private fun PointF.computeXYForSpeed(pos: FanSpeed, radius: Float) {
        goToCenter()

        // Angles are in radians.
        val startAngle = Math.PI * (9 / 8.0)
        val angle = startAngle + pos.ordinal * (Math.PI / 4)

        x += (radius * cos(angle)).toFloat()
        y += (radius * sin(angle)).toFloat()
    }

    override fun performClick(): Boolean {
        if (super.performClick()) return true

        fanSpeed = fanSpeed.next()
        contentDescription = resources.getString(fanSpeed.label)

        invalidate()
        return true
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        radius = (min(width, height) / 2.0 * 0.75).toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        paint.color = when (fanSpeed) {
            FanSpeed.OFF -> Color.GRAY
            FanSpeed.LOW -> fanSpeedLowColor
            FanSpeed.MEDIUM -> fanSpeedMediumColor
            FanSpeed.HIGH -> fanSpeedHighColor
        }
        canvas.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), radius, paint)

        val markerRadius = radius + RADIUS_OFFSET_INDICATOR
        paint.color = Color.BLACK
        pointPosition.computeXYForSpeed(fanSpeed, markerRadius)
        canvas.drawCircle(pointPosition.x, pointPosition.y, (radius / 12), paint)

        val labelRadius = radius + RADIUS_OFFSET_LABEL
        for (i in FanSpeed.entries) {
            paint.color = if (i == fanSpeed) Color.BLACK else (0x32000000).toInt()
            pointPosition.computeXYForSpeed(i, labelRadius)
            val label = resources.getString(i.label)
            canvas.drawText(label, pointPosition.x, pointPosition.y, paint)

            // Create marker disabled
            if (i !== fanSpeed) {
                paint.color = (0x32000000).toInt()
                pointPosition.computeXYForSpeed(i, markerRadius)
                canvas.drawCircle(pointPosition.x, pointPosition.y, (radius / 12), paint)
            }
        }

//        showAxes(canvas)
    }

    private fun showAxes(canvas: Canvas) {
        paint.color = Color.RED

        // X axes
        canvas.drawLine(
            (0).toFloat(),
            (height / 2).toFloat(),
            width.toFloat(),
            (height / 2).toFloat(),
            paint
        )

        // Y axes
        canvas.drawLine(
            (width / 2).toFloat(),
            (0).toFloat(),
            (width / 2).toFloat(),
            height.toFloat(),
            paint
        )

        canvas.drawLine(
            0.toFloat(),
            0.toFloat(),
            width.toFloat(),
            height.toFloat(),
            paint
        )

        canvas.drawLine(
            width.toFloat(),
            0.toFloat(),
            0.toFloat(),
            height.toFloat(),
            paint
        )
    }
}