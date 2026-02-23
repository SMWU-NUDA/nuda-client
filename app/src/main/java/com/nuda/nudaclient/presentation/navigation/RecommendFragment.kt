package com.nuda.nudaclient.presentation.navigation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nuda.nudaclient.R
import com.nuda.nudaclient.data.remote.RetrofitClient.membersService
import com.nuda.nudaclient.databinding.FragmentRecommendBinding
import com.nuda.nudaclient.databinding.FragmentWishlistBinding
import com.nuda.nudaclient.extensions.executeWithHandler
import com.nuda.nudaclient.presentation.common.activity.BaseActivity
import com.nuda.nudaclient.presentation.common.fragment.SortBottomSheet


class RecommendFragment : Fragment() {

    // TODO feat: 맞춤 상품 추천 UI 구현 완료 (키워드 필터링은 사용자 별 개수 차이가 있을 수 있음)

    private var _binding: FragmentRecommendBinding? = null
    private val binding get() = _binding!!

    private var selectedSortTypeIdx = 0 // 필터링 기본값 인덱스 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRecommendBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 툴바 설정
         setToolbar()

        // 필터링 버튼 설정
        setFilterButton()

        // 키워드 조회
        loadKeywords()

    }
    // 툴바 설정
    private fun setToolbar() {
        (activity as? BaseActivity)?.setToolbarTitle("맞춤 제품 추천") // 타이틀 설정
        (activity as? BaseActivity)?.setBackButton() // 뒤로가기 버튼 설정
        (activity as? BaseActivity)?.setToolbarButtons() // 툴바 버튼들 설정
    }

    // 키워드 조회 API 호출 및 키워드 설정
    private fun loadKeywords() {
        membersService.getKeywords()
            .executeWithHandler(
                context = requireContext(),
                onSuccess = { body ->
                    if (body.success == true) {
                        body.data?.let { data ->
                            binding.tvIrritationLevel.text = when (data.irritationLevel) {
                                "NONE" -> "민감도 낮음"
                                "SOMETIMES" -> "민감도 보통"
                                "OFTEN" -> "민감도 높음"
                                else -> "UNKNOWN"
                            }
                            binding.tvScent.text = when (data.scent) {
                                "NONE" -> "무향"
                                "MILD" -> "보통 향"
                                "STRONG" -> "강한 향"
                                else -> "UNKNOWN"
                            }
                            binding.tvAdhesion.text = when (data.adhesion) {
                                "WEAK" -> "접착력 무관"
                                "NORMAL" -> "접착력 보통"
                                "STRONG" -> "접착력 중시"
                                else -> "UNKNOWN"
                            }
                            binding.tvThickness.text = when (data.thickness) {
                                "THIN" -> "약한 흡수력"
                                "NORMAL" -> "보통 흡수력"
                                "THICK" -> "높은 흡수력"
                                else -> "UNKNOWN"
                            }
                        }
                    }
                }
            )
    }


    // 필터링 버튼 클릭 이벤트 -> BottomSheetDialog 프래그먼트 호출
    private fun setFilterButton() {
        val BtnFilter = binding.btnFilter

        BtnFilter.setOnClickListener {
            SortBottomSheet.newInstance(
                options = listOf("전체", "민감도", "향", "흡수력", "접착력"),
                sortTypes = listOf("ALL", "IRRITATION_LEVEL", "SCENT", "THICKNESS", "ADHESION"),
                selectedIndex = selectedSortTypeIdx
            ){ sortType ->
                when (sortType) {
                    "ALL" -> {
                        selectedSortTypeIdx = 0
                        BtnFilter.text = "전체"
                    }
                    "IRRITATION_LEVEL" -> {
                        selectedSortTypeIdx = 1
                        BtnFilter.text = "민감도"
                    }
                    "SCENT" -> {
                        selectedSortTypeIdx = 2
                        BtnFilter.text = "향"
                    }
                    "THICKNESS" -> {
                        selectedSortTypeIdx = 3
                        BtnFilter.text = "흡수력"
                    }
                    "ADHESION" -> {
                        selectedSortTypeIdx = 4
                        BtnFilter.text = "접착력"
                    }
                }
            }.show(parentFragmentManager, "SortBottomSheet")

        }
    }


}