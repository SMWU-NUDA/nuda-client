package com.nuda.nudaclient.presentation.product

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.nuda.nudaclient.R
import com.nuda.nudaclient.data.remote.RetrofitClient.productsService
import com.nuda.nudaclient.data.remote.dto.common.Product
import com.nuda.nudaclient.databinding.ActivityProductRankingBinding
import com.nuda.nudaclient.extensions.executeWithHandler
import com.nuda.nudaclient.extensions.setInfiniteScrollListener
import com.nuda.nudaclient.presentation.common.activity.BaseActivity
import com.nuda.nudaclient.presentation.common.fragment.SortBottomSheet
import com.nuda.nudaclient.presentation.product.adapter.ProductAdapter

class ProductRankingActivity : BaseActivity() {
    
    // TODO 필터링 선택 시 목록 초기화 안됨 에러
    // TODO 상품 카드 클릭 시 해당 상품 상세페이지로 이동 로직
    // TODO 상품 랭킹 텍스트 늘어나면 잘리는 에러 수정 필요
    

    private lateinit var binding: ActivityProductRankingBinding
    private var selectedSortTypeIdx = 0 // 필터링 기본값 인덱스 0, 이후 선택 필터 인덱스 전달로 상태 유지
    private var selectedSortType: String = "DEFAULT" // 선택된 필터링 저장

    private lateinit var productAdapter : ProductAdapter

    private var currentCursor: String? = null // 다음 페이지 요청에 쓸 커서
    private var isLoading = false // 현재 로딩 중인지 체크

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

        // 리사이클러뷰 설정
        setupRecyclerView()
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
                sortTypes = listOf("DEFAULT", "REVIEW_COUNT_DESC", "RATING_DESC", "RATING_ASC", "LIKE_COUNT_DESC"),
                selectedIndex = selectedSortTypeIdx
            ){ sortType ->
                when (sortType) {
                    "DEFAULT" -> {
                        selectedSortTypeIdx = 0
                        BtnFilter.text = "기본순"
                    }
                    "REVIEW_COUNT_DESC" -> {
                        selectedSortTypeIdx = 1
                        BtnFilter.text = "리뷰 많은 순"
                    }
                    "RATING_DESC" -> {
                        selectedSortTypeIdx = 2
                        BtnFilter.text = "별점 높은 순"
                    }
                    "RATING_ASC" -> {
                        selectedSortTypeIdx = 3
                        BtnFilter.text = "별점 낮은 순"
                    }
                    "LIKE_COUNT_DESC" -> {
                        selectedSortTypeIdx = 4
                        BtnFilter.text = "찜 많은 순"
                    }
                }
                selectedSortType = sortType // 선택된 필터링 항목 저장
                currentCursor = null // cursor 초기화 (필터링 된 목록으로 로드하기 위해)

                loadAllProductRanking() // 상품 목록 로드

            }.show(supportFragmentManager, "SortBottomSheet")

        }
    }

    // 리사이클러뷰 설정
    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(
            showRank = true // 순위 있음 (전체 랭킹)
        ) { productId ->
            val intent = Intent(this, ProductDetailActivity::class.java)
            intent.putExtra("PRODUCT_ID", productId)
            startActivity(intent)
        }

        binding.rvAllRanking.apply {
            adapter = productAdapter
            layoutManager = LinearLayoutManager(this@ProductRankingActivity)
        }

        loadAllProductRanking() // 첫 로드
        setScrollListner()  // 무한 스크롤 설정
    }

    // 무힌 스크롤 리스너 설정
    private fun setScrollListner() {
        binding.rvAllRanking.setInfiniteScrollListener {
            if (!isLoading // 로딩 중이 아니고
                && currentCursor != null) { // 다음 페이지가 있으면
                loadAllProductRanking() // 다음 페이지 로드
            }
        }
    }

    // 전체 상품 랭킹 조회 API 호출 및 응답 저장
    private fun loadAllProductRanking() {
        if (isLoading) return // 로딩 중이면 리턴
        isLoading = true // 로딩 시작

        productsService.getAllProductRanking(
            sort = selectedSortType,
            cursor = currentCursor
        ).executeWithHandler(
            context = this,
            onSuccess = { body ->
                if (body.success == true) {
                    body.data?.let { data ->
                        if (currentCursor == null) { // 첫 로드이거나 필터 변경 후 첫 로드인 경우
                            productAdapter.submitList(data.content)
                        } else { // 첫 로드가 아닌 경우
                            productAdapter.appendItems(data.content) // 무한 스크롤 추가
                        }

                        // 다음 커서 업데이트
                        currentCursor = if (data.hasNext) { // 다음 페이지가 있으면
                            "${data.nextCursor.sortValue}_${data.nextCursor.id}" // "{sortValue}_{id}"
                        } else { // 마지막 페이지면
                            null
                        }
                    }
                }
                // 로딩 종료
                isLoading = false
            },
            onError = {
                isLoading = false
            }
        )
    }

}