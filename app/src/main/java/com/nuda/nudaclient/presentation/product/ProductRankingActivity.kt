package com.nuda.nudaclient.presentation.product

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.nuda.nudaclient.R
import com.nuda.nudaclient.databinding.ActivityProductRankingBinding
import com.nuda.nudaclient.presentation.common.activity.BaseActivity
import com.nuda.nudaclient.presentation.common.fragment.SortBottomSheet

class ProductRankingActivity : BaseActivity() {

    // TODO feat(products): 필터링 항목 클릭 시 폰트 변경 및 체크 아이콘 추가

    private lateinit var binding: ActivityProductRankingBinding
    private var selectedSortTypeIdx = 0 // 필터링 기본값 인덱스 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityProductRankingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 툴바 설정
        setToolbar()

        // 필터링 버튼 설정
        setFilterButton()


    }

    // 툴바 설정
    private fun setToolbar() {
        setToolbarTitle("제품 랭킹") // 타이틀
        setBackButton() // 뒤로가기 버튼
        setToolbarButtons() // 툴바 버튼 설정
    }

    // 필터링 버튼 클릭 이벤트 -> BottomSheetDialog 프래그먼트 호출
    private fun setFilterButton() {
        val BtnFilter = binding.btnFilter

        BtnFilter.setOnClickListener {
            SortBottomSheet.newInstance(
                options = listOf("기본순", "리뷰 많은 순", "별점 높은 순", "별점 낮은 순", "찜 많은 순"),
                sortTypes = listOf("DEFAULT", "REVIEW", "HIGH_RATING", "LOW_RATING", "WISH"),
                selectedIndex = selectedSortTypeIdx
            ){ sortType ->
                when (sortType) {
                    "DEFAULT" -> {
                        selectedSortTypeIdx = 0
                        BtnFilter.text = "기본순"
                    }
                    "REVIEW" -> {
                        selectedSortTypeIdx = 1
                        BtnFilter.text = "리뷰 많은 순"
                    }
                    "HIGH_RATING" -> {
                        selectedSortTypeIdx = 2
                        BtnFilter.text = "별점 높은 순"
                    }
                    "LOW_RATING" -> {
                        selectedSortTypeIdx = 3
                        BtnFilter.text = "별점 낮은 순"
                    }
                    "WISH" -> {
                        selectedSortTypeIdx = 4
                        BtnFilter.text = "찜 많은 순"
                    }
                }
            }.show(supportFragmentManager, "SortBottomSheet")

        }
    }


}