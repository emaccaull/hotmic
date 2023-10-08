package io.github.emaccaull.hotmic.level

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Shader
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

    private var nightMode: Int = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

    /**
     * The level rendered in the peak meter. Values outside the range are clamped to the range.
     */
    @FloatRange(from = Dbfs.MIN.toDouble(), to = Dbfs.MAX.toDouble())
    var level: Float = Dbfs.MIN
        set(value) {
            val clamped = max(min(value, Dbfs.MAX), Dbfs.MIN)
            if (abs(clamped - field) >= EPSILON) {
                field = clamped
                invalidate()
            }
        }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = if (nightMode == Configuration.UI_MODE_NIGHT_YES) Color.WHITE else Color.BLACK
        strokeWidth = 2f
    }

    private val gradient = LinearGradient(
        0f,
        1f,
        0f,
        0f,
        intArrayOf(Color.GREEN, Color.YELLOW, Color.RED),
        floatArrayOf(0f, 0.7f, 0.98f),
        Shader.TileMode.CLAMP
    )
    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader = gradient
    }
    private val matrix = Matrix()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        matrix.setScale(1f, measuredHeight.toFloat())
        gradient.setLocalMatrix(matrix)
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
    }
}
