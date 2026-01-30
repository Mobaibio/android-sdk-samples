package com.example.biometricsdkexample.biometric

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.annotation.Px
import kotlin.math.min
import androidx.core.graphics.toColorInt

class CornerFrameOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    data class Insets(
        @Px var left: Float = 0f,
        @Px var top: Float = 0f,
        @Px var right: Float = 0f,
        @Px var bottom: Float = 0f
    )

    data class CornerStyle(
        @Px var lineWidth: Float = 6f,
        @Px var cornerLength: Float = 42f,
        @Px var cornerRadius: Float = 14f,
        var inset: Insets = Insets(),

        // ✅ ORTADAKİ FRAME BOYUTU
        @Px var frameWidth: Float = 0f,   // 0 => tüm alan
        @Px var frameHeight: Float = 0f,  // 0 => tüm alan

        // ✅ İstersen merkezi biraz kaydır (ekrandaki gibi biraz yukarı almak için)
        @Px var centerOffsetX: Float = 0f,
        @Px var centerOffsetY: Float = 0f
    )

    enum class Corner { TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT }
    enum class CornerState { NEUTRAL, PASS, FAIL }

    var style: CornerStyle = CornerStyle()
        set(value) {
            field = value
            updatePaints()
            rebuildPaths()
            invalidate()
        }

    private val states: MutableMap<Corner, CornerState> = Corner.values()
        .associateWith { CornerState.NEUTRAL }
        .toMutableMap()

    private val paintTL = newPaint()
    private val paintTR = newPaint()
    private val paintBL = newPaint()
    private val paintBR = newPaint()

    private val pathTL = Path()
    private val pathTR = Path()
    private val pathBL = Path()
    private val pathBR = Path()

    init {
        isClickable = false
        isFocusable = false
        setWillNotDraw(false)
        updatePaints()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        rebuildPaths()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawPath(pathTL, paintTL)
        canvas.drawPath(pathTR, paintTR)
        canvas.drawPath(pathBL, paintBL)
        canvas.drawPath(pathBR, paintBR)
    }

    fun resetNeutral() {
        Corner.values().forEach { states[it] = CornerState.NEUTRAL }
        applyColors()
        invalidate()
    }

    fun setStates(newStates: Map<Corner, CornerState>) {
        for ((k, v) in newStates) states[k] = v
        applyColors()
        invalidate()
    }

    private fun newPaint(): Paint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

    private fun colorFor(state: CornerState): Int = when (state) {
        CornerState.NEUTRAL -> "#F0F9FD".toColorInt()
        CornerState.PASS -> "#9BCF88".toColorInt()
        CornerState.FAIL -> "#FF8888".toColorInt()
    }

    private fun updatePaints() {
        val w = style.lineWidth
        listOf(paintTL, paintTR, paintBL, paintBR).forEach { it.strokeWidth = w }
        applyColors()
    }

    private fun applyColors() {
        paintTL.color = colorFor(states[Corner.TOP_LEFT] ?: CornerState.NEUTRAL)
        paintTR.color = colorFor(states[Corner.TOP_RIGHT] ?: CornerState.NEUTRAL)
        paintBL.color = colorFor(states[Corner.BOTTOM_LEFT] ?: CornerState.NEUTRAL)
        paintBR.color = colorFor(states[Corner.BOTTOM_RIGHT] ?: CornerState.NEUTRAL)
    }

    private fun rebuildPaths() {
        if (width == 0 || height == 0) return

        // inset sonrası çizilebilir alan
        val available = RectF(
            style.inset.left,
            style.inset.top,
            width.toFloat() - style.inset.right,
            height.toFloat() - style.inset.bottom
        )

        val cx = available.centerX() + style.centerOffsetX
        val cy = available.centerY() + style.centerOffsetY

        // frame boyutu verilmemişse available’ı kullan
        val fw = if (style.frameWidth > 0f) min(style.frameWidth, available.width()) else available.width()
        val fh = if (style.frameHeight > 0f) min(style.frameHeight, available.height()) else available.height()

        val r = RectF(
            cx - fw / 2f,
            cy - fh / 2f,
            cx + fw / 2f,
            cy + fh / 2f
        )

        val L = style.cornerLength
        val rad = min(style.cornerRadius, L)

        pathTL.set(smoothCornerPath(r.left,  r.top,    +1f, +1f, L, rad))
        pathTR.set(smoothCornerPath(r.right, r.top,    -1f, +1f, L, rad))
        pathBL.set(smoothCornerPath(r.left,  r.bottom, +1f, -1f, L, rad))
        pathBR.set(smoothCornerPath(r.right, r.bottom, -1f, -1f, L, rad))
    }

    private fun smoothCornerPath(
        cornerX: Float,
        cornerY: Float,
        hx: Float,
        vy: Float,
        length: Float,
        radius: Float
    ): Path {
        val path = Path()

        val hEndX = cornerX + hx * length
        val hEndY = cornerY

        val vEndX = cornerX
        val vEndY = cornerY + vy * length

        val hInnerX = cornerX + hx * radius
        val hInnerY = cornerY

        val vInnerX = cornerX
        val vInnerY = cornerY + vy * radius

        path.moveTo(hEndX, hEndY)
        path.lineTo(hInnerX, hInnerY)

        if (radius > 0f) {
            path.quadTo(cornerX, cornerY, vInnerX, vInnerY)
        } else {
            path.lineTo(cornerX, cornerY)
        }

        path.lineTo(vEndX, vEndY)
        return path
    }

    private fun Path.set(other: Path) {
        reset()
        addPath(other)
    }
}

// dp helper
fun Float.dp(context: Context): Float = this * context.resources.displayMetrics.density
