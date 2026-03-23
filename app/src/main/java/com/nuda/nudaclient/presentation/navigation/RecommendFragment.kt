package com.nuda.nudaclient.presentation.navigation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.nuda.nudaclient.R
import com.nuda.nudaclient.data.remote.RetrofitClient.membersService
import com.nuda.nudaclient.data.remote.RetrofitClient.productsService
import com.nuda.nudaclient.data.remote.dto.common.Product
import com.nuda.nudaclient.databinding.FragmentRecommendBinding
import com.nuda.nudaclient.extensions.executeWithHandler
import com.nuda.nudaclient.extensions.setInfiniteScrollListener
import com.nuda.nudaclient.presentation.common.activity.BaseActivity
import com.nuda.nudaclient.presentation.common.fragment.SortBottomSheet
import com.nuda.nudaclient.presentation.product.ProductDetailActivity
import com.nuda.nudaclient.presentation.product.adapter.ProductAdapter


class RecommendFragment : Fragment() {

    private var _binding: FragmentRecommendBinding? = null
    private val binding get() = _binding!!

    private var selectedSortTypeIdx = 0 // 필터링 기본값 인덱스 0
    private var selectedSortType: String = "DEFAULT" // 선택된 필터링 저장

    private lateinit var productAdapter: ProductAdapter

    private var currentCursor: Int? = null // 다음 페이지 요청에 쓸 커서
    private var isLoading = false // 현재 로딩 중인지 체크

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

        setFilterButton()// 필터링 버튼 설정
        loadKeywords() // 키워드 조회
        setupRecyclerView() // 리사이클러뷰 설정
    }

    override fun onStart() {
        super.onStart()

        setToolbar() // 툴바 설정
    }

    // 툴바 설정
    private fun setToolbar() {
        val base = activity as? BaseActivity
        Log.d("API_DEBUG", "baseActivity: $base")  // null인지 확인

        base?.setToolbarTitle("맞춤 제품 추천") // 타이틀 설정
        base?.setToolbarBackBtn(false) // 뒤로가기 버튼 숨김
        base?.setToolbarButtons() // 툴바 버튼들 설정
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

                            // 사용자 닉네임 설정
                            binding.tvNickname.text = data.me.nickname
                            binding.tvNickname2.text = data.me.nickname
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
                sortTypes = listOf("DEFAULT", "IRRITATION_LEVEL", "SCENT", "ABSORBENCY", "ADHESION"),
                selectedIndex = selectedSortTypeIdx
            ){ sortType ->
                when (sortType) {
                    "DEFAULT" -> {
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
                    "ABSORBENCY" -> {
                        selectedSortTypeIdx = 3
                        BtnFilter.text = "흡수력"
                    }
                    "ADHESION" -> {
                        selectedSortTypeIdx = 4
                        BtnFilter.text = "접착력"
                    }
                }
                selectedSortType = sortType // 선택된 필터링 항목 저장
                currentCursor = null // cursor 초기화 (필터링 된 목록으로 로드하기 위해)

                loadKeywordRanking() // 상품 목록 로드
            }.show(parentFragmentManager, "SortBottomSheet")

        }
    }

    // 리사이클러뷰 설정
    private fun setupRecyclerView() {
        // 어댑터 생성
        productAdapter = ProductAdapter(
            showRank = true // 순위 있음
        ) { productId, thumbnail -> // 상품 카드 클릭 시 해당 상품으로 이동
            val intent = Intent(requireContext(), ProductDetailActivity::class.java)
            intent.putExtra("PRODUCT_ID", productId)
            startActivity(intent)
        }
        // 어댑터 연결
        binding.rvRecommendRanking.apply {
            adapter = productAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
        
        setScrollListner() // 무한 스크롤 설정
        loadKeywordRanking() // 첫 로드
    }

    // 무힌 스크롤 리스너 설정
    private fun setScrollListner() {
        binding.rvRecommendRanking.setInfiniteScrollListener {
            if (!isLoading // 로딩 중이 아니고
                && currentCursor != null) { // 다음 페이지가 있으면
                loadKeywordRanking() // 다음 페이지 로드
            }
        }
    }

    // 키워드별 맞춤 상품 랭킹 조회 API 요청 및 응답 저장
    private fun loadKeywordRanking() {
        if (isLoading) return // 로딩 중이면 리턴
        isLoading = true // 로딩 시작

        productsService.getPersonalProductRanking(
            keyword = selectedSortType,
            cursor = currentCursor
        ).executeWithHandler(
            context = requireContext(),
            onSuccess = { body ->
                if (body.success == true) {
                    body.data?.let { data ->
                        if (currentCursor == null) { // 첫 로드이거나 필터 변경 후 첫 로드인 경우
                            productAdapter.submitList(data.content)
                            setTop3Product(data.content.take(3)) // 상위 3개 제품 데이터 바인딩
                        } else { // 첫 로드가 아닌 경우
                            productAdapter.appendItems(data.content) // 무한 스크롤 추가
                        }

                        // 다음 커서 업데이트
                        currentCursor = if (data.hasNext) { // 다음 페이지가 있으면
                            data.nextCursor
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

    // 상위 3개 상품 설정
    private fun setTop3Product(products: List<Product>) {
        bindTop3(products) // 데이터 바인딩
        setTop3ClickListeners() // 카드 클릭 리스너 설정
    }

    // 상위 3개 상품 데이터 바인딩
    private fun bindTop3(products: List<Product>) {
        products[0].let { product -> // 1위 상품
            binding.tvBrandRank1.text = product.brandName
            binding.tvProductNameRank1.text = product.productName
            Glide.with(this)
                .load(product.thumbnailImg)
                .placeholder(R.drawable.image_product2)
                .error(R.drawable.image_product)
                .centerCrop()
                .into(binding.ivProductRank1)
        }
        products[1].let { product -> // 2위 상품
            binding.tvBrandRank2.text = product.brandName
            binding.tvProductNameRank2.text = product.productName
            Glide.with(this)
                .load(product.thumbnailImg)
                .placeholder(R.drawable.image_product2)
                .error(R.drawable.image_product)
                .centerCrop()
                .into(binding.ivProductRank2)
        }
        products[2].let { product -> // 3위 상품
            binding.tvBrandRank3.text = product.brandName
            binding.tvProductNameRank3.text = product.productName
            Glide.with(this)
                .load(product.thumbnailImg)
                .placeholder(R.drawable.image_product2)
                .error(R.drawable.image_product)
                .centerCrop()
                .into(binding.ivProductRank3)
        }
    }

    // 상위 3개 상품 카드 클릭 설정
    private fun setTop3ClickListeners() {
        val cards = listOf(binding.cvRank1, binding.cvRank2, binding.cvRank3)

        cards.forEachIndexed { index, card ->
            card.setOnClickListener {
                // 모든 카드 elevation 낮추고
                cards.forEach { it.cardElevation = 4f }
                // 클릭한 카드만 높임
                card.cardElevation = 10f
            }
        }
    }

}