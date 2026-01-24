package com.nuda.nudaclient.utils

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.nuda.nudaclient.R

object CustomToast {
    fun show(rootView: View, message: String) {
        Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT).apply {
            // 글자 색
            setTextColor(ContextCompat.getColor(context, R.color.gray2))
            // 배경 색
            setBackgroundTint(ContextCompat.getColor(context, R.color.white_bg))
            // 글자 크기 및 폰트
            view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).apply {
                textSize = 14f
                textAlignment = View.TEXT_ALIGNMENT_CENTER
                typeface = resources.getFont(R.font.pretendard_medium)
            }

            // 스낵바를 글자 길이에 맞게 중앙 정렬
            val params = view.layoutParams as FrameLayout.LayoutParams
            params.width = FrameLayout.LayoutParams.WRAP_CONTENT
            params.gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
            params.bottomMargin = 50
            view.layoutParams = params

            show()
        }
    }
}