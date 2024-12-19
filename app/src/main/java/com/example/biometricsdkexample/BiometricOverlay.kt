package com.example.biometricsdkexample

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.View

class BiometricOverlay(context: Context): View(context) {

 private val transparentBox = Paint().apply {
     color = Color.BLACK
     alpha = 100
 }

    var ovalColor = BorderColor.YELLOW
        set(value) {
            field = value
            ovalPaint.color = value.color
            invalidate()
        }
    private val ovalPaint = Paint().apply {
        color = ovalColor.color
        strokeWidth = 12f
        style = Paint.Style.STROKE
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val ovalRectWith = height * 0.35
        val ovalRectHeight = height * 0.45
        val ovalRectLeft = (width - ovalRectWith) / 2
        val ovalRectTop = (height - ovalRectHeight) / 2
        val ovalRectRight = ovalRectLeft + ovalRectWith
        val ovalRectBottom = ovalRectTop + ovalRectHeight

        val ovalRect = RectF().apply {
            left = ovalRectLeft.toFloat()
            top = ovalRectTop.toFloat()
            right = ovalRectRight.toFloat()
            bottom = ovalRectBottom.toFloat()
        }

        canvas.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), transparentBox)
        canvas.drawOval(ovalRect, ovalPaint)
    }

    /**
     * Colors that can be used for the oval border
     */
    enum class BorderColor(val color: Int) {
        YELLOW(Color.parseColor("#FFC94F")),
        GREEN(Color.parseColor("#1BF305")),
    }
}