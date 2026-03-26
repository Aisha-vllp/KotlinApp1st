package com.flag.bozi

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.widget.TextView

class TileView(context: Context) : TextView(context) {
    var value: Int = 0
        set(v) {
            field = v
            text = if (v == 0) "" else v.toString()
            setBackgroundColor(getColorByValue(v))
        }

    init {
        textSize = 24f
        setPadding(16, 16, 16, 16)
        setTextColor(Color.BLACK)
        gravity = Gravity.CENTER
        setBackgroundColor(Color.LTGRAY)
    }

    private fun getColorByValue(value: Int): Int {
        return when (value) {
            0 -> Color.parseColor("#CCC0B3")
            2 -> Color.parseColor("#EEE4DA")
            4 -> Color.parseColor("#EDE0C8")
            8 -> Color.parseColor("#F2B179")
            16 -> Color.parseColor("#F59563")
            32 -> Color.parseColor("#F67C5F")
            64 -> Color.parseColor("#F65E3B")
            128 -> Color.parseColor("#EDCF72")
            256 -> Color.parseColor("#EDCC61")
            512 -> Color.parseColor("#EDC850")
            1024 -> Color.parseColor("#EDC53F")
            2048 -> Color.parseColor("#EDC22E")
            else -> Color.BLACK
        }
    }
}
