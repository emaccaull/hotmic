package io.github.emaccaull.hotmic.level

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.annotation.FloatRange
import kotlin.math.max
import kotlin.math.min

/**
 * https://en.wikipedia.org/wiki/Peak_meter
 */
class PeakMeterView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    /**
     * The level rendered in the peak meter. Values outside the range are clamped to the range.
     */
    @FloatRange(from = RANGE_MIN.toDouble(), to = RANGE_MAX.toDouble())
    var level: Float = RANGE_MIN
        set(value) {
            field = max(min(value, RANGE_MAX), RANGE_MIN)
            invalidate()
        }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.BLACK
        strokeWidth = 2f
    }

    override fun onDraw(canvas: Canvas) {
        val height = measuredHeight.toFloat()
        val width = measuredWidth.toFloat()
        // Border
        canvas.drawLine(0f, 0f, 0f, height, paint)
        canvas.drawLine(0f, 0f, width, 0f, paint)
        canvas.drawLine(0f, height, width, height, paint)
        canvas.drawLine(width, 0f, width, height, paint)
        // Level
        //   Normalize level to find percentage of height. Also note that y is positive in the down
        //   direction.
        val levelLine = (1 - ((level - RANGE_MIN) / (RANGE_MAX - RANGE_MIN))) * height
        canvas.drawLine(0f, levelLine, width, levelLine, paint)
        // Labels
    }

    companion object {
        private const val RANGE_MIN = -32.0f
        private const val RANGE_MAX = 3.0f
    }
}
