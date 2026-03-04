package com.nuda.nudaclient.presentation.navigation

import android.content.Intent
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.nuda.nudaclient.R
import com.nuda.nudaclient.data.remote.RetrofitClient.productsService
import com.nuda.nudaclient.data.remote.dto.common.Product
import com.nuda.nudaclient.databinding.FragmentHomeBinding
import com.nuda.nudaclient.databinding.FragmentMypageBinding
import com.nuda.nudaclient.extensions.executeWithHandler
import com.nuda.nudaclient.presentation.navigation.adapter.HomeRankingAdapter
import com.nuda.nudaclient.presentation.product.ProductRankingActivity


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var homeRankingAdapter: HomeRankingAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupNavigation()

        // 전체 랭킹 ViewPager 설정
        setRankingViewPager()
    }

    // 뷰 파괴 (프래그먼트 객체는 유지)
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    // 화면 이동 네비게이션 설정
    private fun setupNavigation() {
        // 전체 상품 랭킹 화면으로 이동
        binding.tvGoToRanking.setOnClickListener {
            startActivity(Intent(requireContext(), ProductRankingActivity::class.java))
        }
    }

    // 전체 랭킹 ViewPager 설정
    private fun setRankingViewPager() {
        val viewPager2 = binding.viewPager2

        homeRankingAdapter = HomeRankingAdapter() // 어댑터 생섣
        loadProductRanking() // 랭킹 상품 목록 로드

        // 어댑터 연결
        viewPager2.adapter = homeRankingAdapter
        // 가로 스크롤 설정
        viewPager2.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        // 양 옆 카드 미리보기
        viewPager2.offscreenPageLimit = 3

        // 카드 간격
        val pageMargin = resources.getDimensionPixelOffset(R.dimen.pageMargin) // 카드 사이 간격
        val pageOffset = resources.getDimensionPixelOffset(R.dimen.pageOffset) // 양 옆 미리보기 카드 노출량

        viewPager2.setPageTransformer { page, position ->
            val absPos = Math.abs(position)

            // 양옆 카드 크기 축소
            val scale = 1f - absPos * 0.15f  // 0.85f까지 줄어듦
            page.scaleX = scale
            page.scaleY = scale

            // 블러 효과 (API 31 이상)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (absPos > 0f) {
                    val blur = absPos * 8f  // 블러 강도
                    page.setRenderEffect(
                        RenderEffect.createBlurEffect(blur, blur, Shader.TileMode.CLAMP)
                    )
                } else {
                    page.setRenderEffect(null)  // 현재 카드 블러 제거
                }
            } else {
                // API 31 미만은 alpha로 대체
                page.alpha = 1f - absPos * 0.4f
            }
        }

    }

    // 전체 상품 랭킹 조회 API 호출 및 상품 목록 저장 (10위까지)
    private fun loadProductRanking() {
        productsService.getAllProductRanking(
            sort = "DEFAULT", // 전체 순위로 호출
            cursor = null
        ).executeWithHandler(
            context = requireContext(),
            onSuccess = { body ->
                if (body.success == true) {
                    body.data?.let { data ->
                        homeRankingAdapter.submitList(data.content.take(10)) // 어댑터에 랭킹 아이템 10개 추가
                    }
                }
            }
        )

    }


}