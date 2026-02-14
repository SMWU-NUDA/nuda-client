package com.nuda.nudaclient.presentation.navigation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nuda.nudaclient.R

class WishlistFragment : Fragment() {

    // TODO 찜한 브랜드 조회 API 연동 및 데이터 바인딩
    // TODO 찜한 상품 조회 API 연동 및 데이터 바인딩
    // TODO 성분 즐겨찾기 조회 API 연동 및 데이터 바인딩


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_wishlist, container, false)
    }

}