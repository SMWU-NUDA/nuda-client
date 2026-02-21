package com.nuda.nudaclient.presentation.product

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.NestedScrollView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.nuda.nudaclient.R
import com.nuda.nudaclient.data.remote.RetrofitClient.ingredientsService
import com.nuda.nudaclient.data.remote.RetrofitClient.productsService
import com.nuda.nudaclient.data.remote.RetrofitClient.reviewsService
import com.nuda.nudaclient.data.remote.api.IngredientsService
import com.nuda.nudaclient.data.remote.dto.ingredients.IngredientsGetSummaryResponse
import com.nuda.nudaclient.databinding.ActivityProductDetailBinding
import com.nuda.nudaclient.extensions.executeWithHandler
import com.nuda.nudaclient.extensions.toFormattedPrice
import com.nuda.nudaclient.presentation.common.activity.BaseActivity
import com.nuda.nudaclient.presentation.ingredient.IngredientComponentActivity
import com.nuda.nudaclient.presentation.product.adapter.ProductImagesAdapter
import com.nuda.nudaclient.presentation.review.ReviewAllActivity
import com.nuda.nudaclient.utils.setupBarGraph

class ProductDetailActivity : BaseActivity() {
    // 상품 상세페이지로 이동할 때 Intent에 productId 담아서 전달 필요 !!!

    // TODO 리뷰 요약 조회 API 연동 및 데이터 바인딩
    // TODO 리뷰 좋아요 API 연동 및 기능 구현
    // TODO 탭 설정 추가 (상품 정보, 성분, 리뷰)


    private lateinit var binding: ActivityProductDetailBinding

    private lateinit var viewPagerProductImages: ViewPager2
    private lateinit var layoutIndicator: LinearLayout
    private lateinit var imageAdapter: ProductImagesAdapter

    // 탭 레이아웃 관련 변수
    private var productInfoSectionTop = 0 // 상품 정보 섹션 상단 위치 저장
    private var ingredientSectionTop = 0 // 성분 섹션 상단 위치 저장
    private var reviewSectionTop = 0 // 리뷰 섹션 상단 위치 저장
    private var isUserScrolling = true // 프로그래밍 스크롤 시 탭 변경 방지용
    private var stickyTabHeight = 0 // 고정 탭 높이

    private var productId: Int = -1
    private var brandId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Intent에서 productId 받기
        productId = intent.getIntExtra("PRODUCT_ID", -1)

        // ViewPager 객체 초기화
        viewPagerProductImages = binding.viewPagerProductImages

        // 인디케이터 초기화
        layoutIndicator = binding.llIndicator

        // 툴바 설정
        setToolbar()

        // 상품 상세페이지 정보 로드
        loadProductDetail()

        // 버튼 설정
        setButtons()

        // 탭 레이아웃 설정
        setupTabLayout()
        measureSectionPositions()
        setupScrollListener()

    }

    // 툴바 설정
    private fun setToolbar() {
        setToolbarTitle("") // 타이틀
        setBackButton() // 뒤로가기 버튼
        setToolbarButtons() // 툴바 버튼들 설정
    }

    // TabLayout 초기 설정
    private fun setupTabLayout() {
        // 탭 클릭 시 스크롤 이동하는 리스너 (탭 원본, sticky 공통 동작)
        fun onTabClick(position: Int) {
            if (!isUserScrolling) return

            val targetY = when (position) {
                0 -> productInfoSectionTop - stickyTabHeight
                1 -> ingredientSectionTop - stickyTabHeight
                2 -> reviewSectionTop - stickyTabHeight
                else -> 0
            }
            isUserScrolling = false
            binding.nestedScrollView.smoothScrollTo(0, targetY)
        }

        // 원본 탭 리스너
        binding.tabOriginal.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) { onTabClick(tab.position) }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // sticky 탭 리스너 (동일한 동작)
        binding.tabSticky.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) { onTabClick(tab.position) }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    // 섹션 위치 측정 및 저장
    private fun measureSectionPositions() {
        binding.root.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener { // 뷰 측적 및 배치된 후 시점 알려주는 리스너
            override fun onGlobalLayout() {
                // 리스너 즉시 제거 - onGlobalLayout은 레이아웃 변경마다 계속 호출되기 때문
                // 제거 안 하면 화면 회전, 키보드 등장 등 매 변경마다 불필요하게 재호출됨
                binding.root.viewTreeObserver.removeOnGlobalLayoutListener(this)

                stickyTabHeight = binding.tabOriginal.height

                productInfoSectionTop = getRelativeTop(binding.constraintProductInfo, binding.nestedScrollView)
                ingredientSectionTop = getRelativeTop(binding.line3, binding.nestedScrollView)
                reviewSectionTop = getRelativeTop(binding.line4, binding.nestedScrollView)
            }
        })
    }

    // 섹션 위치 가져오는 함수
    private fun getRelativeTop(view: View, scrollView: NestedScrollView): Int {
        var offset = 0
        var current: View = view

        // 목표 뷰부터 시작해서 NestedScrollView까지 올라가며 top 누적
        while (current != scrollView) {
            offset += current.top  // 현재 뷰의 부모 기준 top
            current = current.parent as View  // 한 단계 위 부모로 이동
        }
        return offset
    }

    // 스크롤 연동
    private fun setupScrollListener() {
        binding.nestedScrollView.setOnScrollChangeListener(
            NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->
                // scrollY: 현재 스크롤된 Y 픽셀값 (위에서 얼마나 내려왔는지)

                updateTabByScrollPosition(scrollY)

                // tab_original의 하단이 화면 밖으로 나갔으면 sticky 탭 표시
                // binding.tabOriginal.bottom: 뷰 자체 좌표 기준 하단 위치
                // scrollY가 그보다 크다 = 원본 탭이 화면 위로 사라졌다
                val tabOriginalTop = getRelativeTop(binding.tabOriginal, binding.nestedScrollView)
                if (scrollY > tabOriginalTop) {
                    binding.tabSticky.visibility = View.VISIBLE
                } else {
                    binding.tabSticky.visibility = View.GONE
                }

                // smoothScrollTo() 완료 후 플래그 복구
                // (스크롤 중 계속 호출되므로 스크롤 끝나면 자연스럽게 true로 돌아옴)
                if (!isUserScrolling) {
                    isUserScrolling = true
                }
            }
        )
    }

    // 스크롤 위치에 따라 탭 업데이트
    private fun updateTabByScrollPosition(scrollY: Int) {
        val tabIndex = when {
            scrollY < ingredientSectionTop - stickyTabHeight  -> 0
            scrollY < reviewSectionTop - stickyTabHeight -> 1
            else -> 2
        }

        // 이미 선택된 탭이면 아무것도 안 함 → select() 호출 자체를 막아서 리스너 발동 방지
        if (binding.tabOriginal.selectedTabPosition != tabIndex) {
            isUserScrolling = false // 코드가 탭을 바꾸는 것임을 표시 → 탭 리스너에서 스크롤 안 함
            binding.tabOriginal.getTabAt(tabIndex)?.select()
            binding.tabSticky.getTabAt(tabIndex)?.select()
            isUserScrolling = true  // 즉시 복구
        }
    }


    // 상품 상세페이지 정보 로드
    private fun loadProductDetail() {
        loadProductInfo()
        loadIngredientInfo()
        loadReviewInfo()
    }

    // 상품 정보 로드
    private fun loadProductInfo() {
        productsService.getProductInfo(productId) // 묵업 데이터 입력 (productId = 1)
            .executeWithHandler(
                context = this,
                onSuccess = { body ->
                    if (body.success == true) { // data non-null로 보장
                        body.data?.let { data ->
                            // 브랜드 아이디 값 저장
                            brandId = data.brandId

                            // 뷰에 데이터 바인딩
                            binding.tvBrand.text = data.brandName
                            binding.tvProductName.text = data.name
                            binding.tvRatingScore.text = data.averageRating.toString()
                            binding.tvReview.text = "리뷰 ${data.reviewCount}개"
                            binding.tvPrice.text = data.price.toFormattedPrice()

                            // 리뷰 개수 텍스트에 밑줄 플래그 추가
                            binding.tvReview.paintFlags = binding.tvReview.paintFlags or Paint.UNDERLINE_TEXT_FLAG

                            // 상품 이미지 리스트
                            setupViewPager(data.imageUrls)
                            setupIndicator(data.imageUrls.size)

//                            // 상품 정보 이미지 (크롤링 후 수정)
//                            data.content

                            // 찜하기
                            if (data.productLikedByMe) { // 상품 찜하기 여부
                                binding.ivProductLike.setImageResource(R.drawable.img_btn_heart_selected)
                            } else {
                                binding.ivProductLike.setImageResource(R.drawable.img_btn_heart_unselected)
                            }

                            if (data.brandLikedByMe) { // 브랜드 찜하기 여부
                                binding.ivBrandLike.setImageResource(R.drawable.img_heart2_selected)
                            } else {
                                binding.ivBrandLike.setImageResource(R.drawable.img_heart2_unselected)
                            }
                        }
                    }
                }
            )
    }

    // 성분 정보 로드
    private fun loadIngredientInfo() {
        ingredientsService.getIngredientSummary(productId)
            .executeWithHandler(
                context = this,
                onSuccess = { body ->
                    if (body.success == true) {
                        updateIngredientUI(body.data)
                    }
                }
            )
    }

    // 성분 정보 UI 업데이트
    private fun updateIngredientUI(data: IngredientsGetSummaryResponse?) {
        data?.let { data ->
            // 막대그래프 구성
            setupBarGraph( // 표지
                binding.llTopSheet,
                data.ingredientCounts.topSheet.count,
                data.ingredientCounts.topSheet.riskCounts)
            setupBarGraph( // 흡수체
                binding.llAbsorber,
                data.ingredientCounts.absorber.count,
                data.ingredientCounts.absorber.riskCounts)
            setupBarGraph( // 방수층
                binding.llBackSheet,
                data.ingredientCounts.backSheet.count,
                data.ingredientCounts.backSheet.riskCounts)
            setupBarGraph( // 접착제
                binding.llAdhesive,
                data.ingredientCounts.adhesive.count,
                data.ingredientCounts.adhesive.riskCounts)
            setupBarGraph( // 기타
                binding.llAdditive,
                data.ingredientCounts.additive.count,
                data.ingredientCounts.additive.riskCounts)

            // 구성요소 별 성분 개수 텍스트
            binding.tvTopSheetCount.text = "${data.ingredientCounts.topSheet.count}개"
            binding.tvAbsorberCount.text = "${data.ingredientCounts.absorber.count}개"
            binding.tvBackSheetCount.text = "${data.ingredientCounts.backSheet.count}개"
            binding.tvAdhesiveCount.text = "${data.ingredientCounts.adhesive.count}개"
            binding.tvAdditiveCount.text = "${data.ingredientCounts.additive.count}개"

            // 항목 별 개수 텍스트
            binding.tvIngredientsAllCount.text = "${data.totalCount}개"
            binding.tvIngredientsCautionCount.text = "${data.globalRiskCounts.warn}개"
            binding.tvIngredientsDangerCount.text = "${data.globalRiskCounts.danger}개"
            binding.tvIngredientsHighlightCount.text = "${data.myIngredientCounts.prefer}개"
        }
    }

    // 리뷰 정보 로드
    private fun loadReviewInfo() {

    }

    // 상품 이미지 리스트 ViewPager 설정
    private fun setupViewPager(imageUrls: List<String>) {
        // 어댑터 설정
        imageAdapter = ProductImagesAdapter(imageUrls)
        viewPagerProductImages.adapter = imageAdapter

        viewPagerProductImages.registerOnPageChangeCallback( // 이미지 스와이프할 때마다 알림
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    updateIndicator(position)
                }
            })
    }

    // 상품 이미지 리스트 인디케이터 점 생성
    private fun setupIndicator(imageCount: Int) {
        repeat(imageCount) { // 이미지 개수만큼 반복
            val indicator = View(this).apply { // View 객체 생성 및 설정
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(8, 0, 8, 0)
                }
                setBackgroundResource(R.drawable.indicator_unselected)
            }
            layoutIndicator.addView(indicator) // 인디케이터 추가
        }

        if (layoutIndicator.childCount > 0) { // 인디케이터가 하나 이상일 때)
            layoutIndicator.getChildAt(0)
                .setBackgroundResource(R.drawable.indicator_selected) // 첫 번째 인디케이터 선택됨으로 변경
        }
    }

    // 상품 이미지 스와이프 시, 인디케이터 업데이트
    private fun updateIndicator(position: Int) {
        // 전체 인디케이터 unselected 설정
        for (i in 0 until layoutIndicator.childCount) { // 인디케이터 개수만큼 반복
            layoutIndicator.getChildAt(i)
                .setBackgroundResource(R.drawable.indicator_unselected)
        }

        // 선택된 이미지 인디테이터 selected 설정
        if (position < layoutIndicator.childCount) { // 인디케이터 개수보다 작을 때
            layoutIndicator.getChildAt(position)
                .setBackgroundResource(R.drawable.indicator_selected)
        }
    }

    private fun setButtons() {
        setMoveToIngredientPage()
        setMoveToReviewPage()
        setLikeButtons()
    }

    // 상품 구성 성분 화면으로 이동
    private fun setMoveToIngredientPage() {
        binding.subtitleAndButton.setOnClickListener {
            val intent = Intent(this, IngredientComponentActivity::class.java)
            intent.putExtra("PRODUCT_ID", productId)
            startActivity(intent)
        }
    }

    // 전체 리뷰 화면으로 이동
    private fun setMoveToReviewPage() {
        binding.btnMoveToAllReviewsTop.setOnClickListener {
            val intent = Intent(this, ReviewAllActivity::class.java)
            intent.putExtra("PRODUCT_ID", productId)
            startActivity(intent)
        }
        binding.btnMoveToAllReviewsBottom.setOnClickListener {
            val intent = Intent(this, ReviewAllActivity::class.java)
            intent.putExtra("PRODUCT_ID", productId)
            startActivity(intent)
        }
    }

    // 찜하기 버튼 설정
    private fun setLikeButtons() {
        // 브랜드 찜하기 설정
        val btnBrandLike = binding.ivBrandLike
        btnBrandLike.setOnClickListener {
            productsService.createBrandLike(1) // brandId 임시 데이터. 이후 수정
                .executeWithHandler(
                    context = this,
                    onSuccess = { body ->
                        if (body.success == true) {
                            body.data?.let { data ->
                                if (data.liked) { // true : 좋아요
                                    btnBrandLike.setImageResource(R.drawable.img_heart2_selected)
                                } else { // false : 취소
                                    btnBrandLike.setImageResource(R.drawable.img_heart2_unselected)
                                }
                            }

                        }
                    }
                )
        }
        // 상품 찜하기 설정
        val btnProductLike = binding.ivProductLike
        btnProductLike.setOnClickListener {
            productsService.createProductLike(productId) // brandId 임시 데이터. 이후 수정
                .executeWithHandler(
                    context = this,
                    onSuccess = { body ->
                        if (body.success == true) {
                            body.data?.let { data ->
                                if (data.liked) { // true : 좋아요
                                    btnProductLike.setImageResource(R.drawable.img_btn_heart_selected)
                                } else { // false : 취소
                                    btnProductLike.setImageResource(R.drawable.img_btn_heart_unselected)
                                }
                            }

                        }
                    }
                )
        }
    }

    // 리뷰 카드의 좋아요 클릭 시 (좋아요 하거나 취소) -> 어댑터에 추가?? linearLayout도 어댑터 적용 되나. 이거 전체 리뷰에도 같이 써야할 것 같은디
    private fun setLikeReview(reviewId: Int) {
        reviewsService.likeReview(reviewId)
            .executeWithHandler(
                context = this,
                onSuccess = { body ->
                    if (body.success == true) {
                        body.data?.let { data ->
                            if (data.liked) { // true : 좋아요
                                // 리뷰 아이템 카드의 좋아요 하트 변경 (selected로)
                            } else { // false : 취소
                                // 리뷰 아이템 카드의 좋아요 하트 변경 (unselected로)
                            }
                        }
                    }
                }
            )

    }





}