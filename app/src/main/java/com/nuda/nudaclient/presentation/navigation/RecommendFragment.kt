package com.nuda.nudaclient.presentation.navigation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nuda.nudaclient.R



class RecommendFragment : Fragment() {

    // TODO feat: 맞춤 제품 추천 툴바 설정 (타이틀, 뒤로가기, 버튼들)
    // TODO feat: 맞춤 상품 추천 UI 구현 완료 (키워드 필터링은 사용자 별 개수 차이가 있을 수 있음)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_recommend, container, false)
    }

}