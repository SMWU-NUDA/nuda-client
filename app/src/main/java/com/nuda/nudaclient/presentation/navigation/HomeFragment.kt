package com.nuda.nudaclient.presentation.navigation

import android.content.Intent
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.nuda.nudaclient.R
import com.nuda.nudaclient.data.remote.RetrofitClient.productsService
import com.nuda.nudaclient.data.remote.dto.common.Product
import com.nuda.nudaclient.databinding.FragmentHomeBinding
import com.nuda.nudaclient.extensions.executeWithHandler
import com.nuda.nudaclient.extensions.setInfiniteScrollListener
import com.nuda.nudaclient.presentation.navigation.adapter.HomeKeywordRankingAdapter
import com.nuda.nudaclient.presentation.navigation.adapter.HomeRankingAdapter
import com.nuda.nudaclient.presentation.product.ProductDetailActivity
import com.nuda.nudaclient.presentation.product.ProductRankingActivity
import com.nuda.nudaclient.presentation.search.SearchActivity
import com.nuda.nudaclient.presentation.shopping.ShoppingCartActivity


class HomeFragment : Fragment() {

    private val TAG = "HomeFragment"

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var homeRankingAdapter: HomeRankingAdapter
    private lateinit var irritationAdapter: HomeKeywordRankingAdapter
    private lateinit var scentAdapter: HomeKeywordRankingAdapter
    private lateinit var adhesionAdapter: HomeKeywordRankingAdapter
    private lateinit var absorptionAdapter: HomeKeywordRankingAdapter

    private var isIrritationLoading = false
    private var isScentLoading = false
    private var isAdhesionLoading = false
    private var isAbsorptionLoading = false


    private var currentIrritationLevelCursor: Int? = null // 다음 페이지 요청에 쓸 커서
    private var currentScentCursor: Int? = null
    private var currentAdhesionCursor: Int? = null
    private var currentAbsorbencyCursor: Int? = null


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
        setupButtons()

        setRankingViewPager() // 전체 랭킹 ViewPager 설정
        setKeywordRankingRecyclerView() // 키워드별 상품 목록 리사이클러뷰 설정
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
            Log.d("API_DEBUG", "[$TAG] 제품 랭킹으로 화면 이동")
        }
    }

    // 전체 버튼 설정
    private fun setupButtons() {
        binding.ivBtnSearch.setOnClickListener {
            startActivity(Intent(requireContext(), SearchActivity::class.java))
            Log.d("API_DEBUG", "[$TAG] 검색 화면으로 이동")
        }
        binding.ivBtnCart.setOnClickListener {
            startActivity(Intent(requireContext(), ShoppingCartActivity::class.java))
            Log.d("API_DEBUG", "[$TAG] 장바구니 화면으로 이동")
        }
    }

    // 전체 랭킹 ViewPager 설정
    private fun setRankingViewPager() {
        val viewPager2 = binding.viewPager2

        // 어댑터 생섣
        homeRankingAdapter = HomeRankingAdapter() { productId -> // 상품 카드 클릭 시 상품 상세페이지로 이동
            val intent = Intent(requireContext(), ProductDetailActivity::class.java)
            intent.putExtra("PRODUCT_ID", productId)
            startActivity(intent)

            Log.d("API_DEBUG", "[$TAG] 상품 상세로 화면 이동")
        }

        // 랭킹 상품 목록 로드
        loadProductRanking()

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

        // 무한 루프: 양 끝 도달 시 반대쪽으로 조용히 점프
        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val itemCount = homeRankingAdapter.itemCount
                if (itemCount <= 2) return

                when (position) {
                    0 -> viewPager2.setCurrentItem(itemCount - 2, false) // 가짜 10위 → 진짜 10위
                    itemCount - 1 -> viewPager2.setCurrentItem(1, false) // 가짜 1위 → 진짜 1위
                }
            }
        })

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
                        // 상위 10개 제품 저장 후 리스트에 추가
                        setupInfiniteLoop(data.content.take(10))
                    }
                }
            }
        )
    }

    // 무한 루프용 리스트 구성 및 초기 위치 설정
    private fun setupInfiniteLoop(products: List<Product>) {
        if (products.isEmpty()) return

        // 실제 랭킹 번호를 index+1로 고정해서 RankingItem에 담음
        val ranked = products.mapIndexed { index, product ->
            HomeRankingAdapter.RankingItem(product, rank = index + 1)
        }

        // [10위, 1위, 2위, ..., 10위, 1위] 구조로 앞뒤 복제
        val loopList = listOf(ranked.last()) + ranked + listOf(ranked.first())

        homeRankingAdapter.submitList(loopList)
        binding.viewPager2.post {
            binding.viewPager2.setCurrentItem(1, false) // 초기 위치를 진짜 1위(index 1)로 설정
        }
    }

    private fun setKeywordRankingRecyclerView() {
        // 어댑터 생성 함수
        fun makeAdapter() = HomeKeywordRankingAdapter { productId ->
            val intent = Intent(requireContext(), ProductDetailActivity::class.java)
            intent.putExtra("PRODUCT_ID", productId)
            startActivity(intent)

            Log.d("API_DEBUG", "[$TAG] 상품 상세로 화면 이동")
        }

        // 어댑터 생성
        irritationAdapter = makeAdapter()
        scentAdapter = makeAdapter()
        adhesionAdapter = makeAdapter()
        absorptionAdapter = makeAdapter()


        binding.rvIrritationLevel.apply {
            adapter = irritationAdapter
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false)
        }

        binding.rvScent.apply {
            adapter = scentAdapter
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false)
        }

        binding.rvAdhesion.apply {
            adapter = adhesionAdapter
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false)
        }
        binding.rvAbsorption.apply {
            adapter = absorptionAdapter
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false)
        }

        setScrollListner() // 무한 스크롤 설정

        // 상품 목록 로드
        loadKeywordProductRanking("IRRITATION_LEVEL")
        loadKeywordProductRanking("SCENT")
        loadKeywordProductRanking("ADHESION")
        loadKeywordProductRanking("ABSORBENCY")
    }

    // 무힌 스크롤 리스너 설정
    private fun setScrollListner() {
        binding.rvIrritationLevel.setInfiniteScrollListener { // 민감도
            if (!getLoadingState("IRRITATION_LEVEL") // 로딩 중이 아니고
                && currentIrritationLevelCursor != null) { // 다음 페이지가 있으면
                loadKeywordProductRanking("IRRITATION_LEVEL") // 다음 페이지 로드
            }
        }
        binding.rvScent.setInfiniteScrollListener { // 향
            if (!getLoadingState("SCENT") // 로딩 중이 아니고
                && currentScentCursor != null) { // 다음 페이지가 있으면
                loadKeywordProductRanking("SCENT") // 다음 페이지 로드
            }
        }
        binding.rvAdhesion.setInfiniteScrollListener { // 접착력
            if (!getLoadingState("ADHESION") // 로딩 중이 아니고
                && currentAdhesionCursor != null) { // 다음 페이지가 있으면
                loadKeywordProductRanking("ADHESION") // 다음 페이지 로드
            }
        }
        binding.rvAbsorption.setInfiniteScrollListener { // 흡수력
            if (!getLoadingState("ABSORBENCY") // 로딩 중이 아니고
                && currentAbsorbencyCursor != null) { // 다음 페이지가 있으면
                loadKeywordProductRanking("ABSORBENCY") // 다음 페이지 로드
            }
        }
    }

    // 키워드별 로딩 상태 리턴
    private fun getLoadingState(keyword: String): Boolean {
        return when (keyword) {
            "IRRITATION_LEVEL" -> isIrritationLoading
            "SCENT" -> isScentLoading
            "ADHESION" -> isAdhesionLoading
            "ABSORBENCY" -> isAbsorptionLoading
            else -> false
        }
    }

    // 키워드별 로딩 상태 반영
    private fun setLoadingState(keyword: String, loading: Boolean) {
        when (keyword) {
            "IRRITATION_LEVEL" -> isIrritationLoading = loading
            "SCENT" -> isScentLoading = loading
            "ADHESION" -> isAdhesionLoading = loading
            "ABSORBENCY" -> isAbsorptionLoading = loading
        }
    }

    // 키워드별 어댑터, 커서 반환
    private fun getAdapterAndCursor(keyword: String): Pair<HomeKeywordRankingAdapter, Int?> {
        return when (keyword) {
            "IRRITATION_LEVEL" -> irritationAdapter to currentIrritationLevelCursor
            "SCENT" -> scentAdapter to currentScentCursor
            "ADHESION" -> adhesionAdapter to currentAdhesionCursor
            "ABSORBENCY" -> absorptionAdapter to currentAbsorbencyCursor
            else -> throw IllegalArgumentException("Unknown keyword: $keyword")
        }
    }

    // 커서 업데이트
    private fun updateCursor(keyword: String, cursor: Int?) {
        when (keyword) {
            "IRRITATION_LEVEL" -> currentIrritationLevelCursor = cursor
            "SCENT" -> currentScentCursor = cursor
            "ADHESION" -> currentAdhesionCursor = cursor
            "ABSORBENCY" -> currentAbsorbencyCursor = cursor
        }
    }

    // 키워드별 상품 목록 로드
    private fun loadKeywordProductRanking(keyword: String) {
        if (getLoadingState(keyword)) return // 키워드 별 로딩 체크 후 로딩 중이라면 리턴
        setLoadingState(keyword, true) // 로딩 시작

        val (adapter, cursor) = getAdapterAndCursor(keyword)

        productsService.getGlobalProductRanking(
            keyword = keyword,
            cursor = cursor
        ).executeWithHandler(
            context = requireContext(),
            onSuccess = { body ->
                if (body.success == true) {
                    body.data?.let { data ->
                        if (cursor == null) { // 첫 로드이거나 필터 변경 후 첫 로드인 경우
                            adapter.submitList(data.content)
                        } else { // 첫 로드가 아닌 경우
                            adapter.appendItems(data.content) // 무한 스크롤 추가
                        }

                        // 다음 커서 업데이트
                        updateCursor(keyword,
                            if (data.hasNext) data.nextCursor else null)
                    }
                }
                // 로딩 종료
                setLoadingState(keyword, false)
            },
            onError = {
                setLoadingState(keyword, false)
            }
        )
    }

}