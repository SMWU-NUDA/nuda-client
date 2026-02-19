package com.nuda.nudaclient.utils

import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.nuda.nudaclient.R
import com.nuda.nudaclient.data.remote.dto.ingredients.IngredientsGetSummaryResponse

// 성분 막대그래프 유틸 함수들

// 구성요소 별 막대그래프 생성
fun setupBarGraph(
    container: LinearLayout, // 구성요소 막대그래프 컨테이너 (표지, 흡수체 ..)
    totalCount: Int, // 구성요소 별 총 성분 개수
    riskCounts: IngredientsGetSummaryResponse.RiskCounts) { // 구성요소 위험도 별 개수

    container.removeAllViews() // 기존 뷰 제거

    val total = totalCount
    if (total == 0) {
        return // 데이터 없으면 리턴
    }

    val context = container.context

    // 위험 (빨간색)
    if (riskCounts.danger > 0) {
        addColorBar(container, riskCounts.danger, ContextCompat.getColor(context, R.color.riskLevel_red))
    }
    // 주의 (노란색)
    if (riskCounts.warn > 0 ) {
        addColorBar(container,riskCounts.warn, ContextCompat.getColor(context, R.color.riskLevel_yellow))
    }
    // 안전 (민트색)
    if (riskCounts.safe > 0) {
        addColorBar(container, riskCounts.safe, ContextCompat.getColor(context, R.color.riskLevel_mint))
    }
    // 등급 없음 (회색)
    if (riskCounts.unknown > 0) {
        addColorBar(container,riskCounts.unknown, ContextCompat.getColor(context, R.color.gray4))
    }
}

// 위험도 별 막대그래프 생성
fun addColorBar(container: LinearLayout, count: Int, color: Int) {
    val view = View(container.context) // 뷰 생성
    val params = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        0,
        count.toFloat()
    )

    view.layoutParams = params
    view.setBackgroundColor(color) // 배경색 설정
    container.addView(view) // 컨테이너에 뷰 추가
}
