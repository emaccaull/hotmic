package io.github.emaccaull.hotmic.level

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.annotation.FloatRange
import kotlin.math.abs
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

    private var history = ArrayDeque<Float>(WINDOW_SIZE)

    /**
     * The level rendered in the peak meter. Values outside the range are clamped to the range.
     */
    @FloatRange(from = Dbfs.MIN.toDouble(), to = Dbfs.MAX.toDouble())
    var level: Float = Dbfs.MIN
        set(value) {
            val avg = averageMicLevel(value)
            val clamped = max(min(avg, Dbfs.MAX), Dbfs.MIN)
            if (abs(clamped - field) >= EPSILON) {
                field = clamped
                invalidate()
            }
        }

    private fun averageMicLevel(nextValue: Float): Float {
        if (history.size == WINDOW_SIZE) {
            history.removeFirst()
        }
        history.addLast(nextValue)
        return history.average().toFloat()
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.BLACK
        strokeWidth = 2f
    }

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.GREEN
    }

    override fun onDraw(canvas: Canvas) {
        val height = measuredHeight.toFloat()
        val width = measuredWidth.toFloat()
        // Level
        //   Normalize level to find percentage of height. Also note that y is positive in the down
        //   direction.
        val levelLine = (1 - ((level - Dbfs.MIN) / (Dbfs.MAX - Dbfs.MIN))) * height
        canvas.drawRect(0f, levelLine, width, height, bgPaint)
        // Border
        canvas.drawLine(0f, 0f, 0f, height, paint)
        canvas.drawLine(0f, 0f, width, 0f, paint)
        canvas.drawLine(0f, height, width, height, paint)
        canvas.drawLine(width, 0f, width, height, paint)
        // Labels
    }

    companion object {
        private const val EPSILON = 0.00001f
        private const val WINDOW_SIZE = 5
    }
}
