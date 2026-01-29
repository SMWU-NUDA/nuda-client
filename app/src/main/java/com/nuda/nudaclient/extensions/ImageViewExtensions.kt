package com.nuda.nudaclient.extensions

import android.content.Context
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.EditText
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.nuda.nudaclient.R

fun ImageView.setupPasswordVisible(
    context: Context,
    editText: EditText) {
    var ispasswordVisible = false

    this.setOnClickListener {
        ispasswordVisible = !ispasswordVisible

        if(ispasswordVisible) { // true
            // 비밀번호 보이기
            editText.transformationMethod = HideReturnsTransformationMethod.getInstance()
            // 아이콘 색 변경
            this.setColorFilter(
                ContextCompat.getColor(context, R.color.gray2)
            )
        } else { // false
            // 비밀번호 숨기기
            editText.transformationMethod = PasswordTransformationMethod.getInstance()
            // 아이콘 색 변경 (원래대로)
            this.setColorFilter(
                ContextCompat.getColor(context,R.color.gray4)
            )
        }

        // 입력창 커서 맨 뒤로
        editText.setSelection(editText.text?.length ?: 0) // setSelection은 Int를 필요로 함
    }
}