package com.nuda.nudaclient.extensions

import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import com.nuda.nudaclient.R

// 1. 확장 함수 정의
fun EditText.setupValidation(
    validationText : TextView,
    validator : (String) -> Boolean,
    onValidationChanged : (Boolean) -> Unit, // 유효성 검사 상태 콜백 추가 (유효성 검사 결과 변경 시 호출)

    // 동적 메시지 파라미터 추가
    emptyMessage : String? = null, // 입력창 비어있을 때 메세지
    validMessage : String? = null, // 유효할 때 메세지
    invalidMessage : String? = null // 유효하지 않을 때 메세지
    ) {


    // 2. 텍스트 변경 감지
    // 실시간 EditText 입력 변경 반영 (EditText의 입력이 변경될 때마다 호출)
    this.doAfterTextChanged { text -> // text : EditText에 입력된 텍스트
        // 3. 입력 텍스트 String으로 변환
        val input = text.toString() // text는 Editable? 타입이므로 문자열로 변환해줘야 함

        // 4. EditText 입력 상태에 따라 텍스트 색상 변경 및 유효성 상태 변경 (콜백 호출)
        when{
            // 입력이 없을 때
            input.isEmpty() -> { // 기본 텍스트 : 회색
                emptyMessage?.let { validationText.text = it } // let은 null이 아닐 때만 실행
                validationText.setTextColor(ContextCompat.getColor(context, R.color.gray3))
                onValidationChanged(false) // 유효성 검사 상태 변경 콜백 호출
            }
            // 입력이 유효성 검사 조건을 통과했을 때
            validator(input) -> { // input 넣었을 때 유효성 검사 만족하면 true 반환
                validMessage?.let { validationText.text = it }
                validationText.setTextColor(ContextCompat.getColor(context, R.color.green))  // 유효성 검사 통과 : 초록색
                onValidationChanged(true)
            }
            else -> { // 유효성 검사 실패하면 false 반환
                invalidMessage?.let { validationText.text = it }
                validationText.setTextColor(ContextCompat.getColor(context, R.color.red)) // 유효성 검사 통과 실패 : 빨간색
                onValidationChanged(false)
            }
        }
    }
}