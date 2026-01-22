package com.nuda.nudaclient.utils

import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.nuda.nudaclient.R

object CustomToast {
    fun show(rootView: View, message: String) {
        Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT).apply {
            // 글자 색
            setTextColor(ContextCompat.getColor(context, R.color.gray2))
            // 글자 크기 및 폰트
            view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).apply {
                textSize = 10f
                textAlignment = View.TEXT_ALIGNMENT_CENTER
                typeface = resources.getFont(R.font.pretendard_medium)
            }
            show()
        }
    }
}