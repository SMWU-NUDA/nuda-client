package com.nuda.nudaclient.presentation.navigation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.nuda.nudaclient.R
import com.nuda.nudaclient.data.remote.RetrofitClient.ingredientsService
import com.nuda.nudaclient.data.remote.RetrofitClient.productsService
import com.nuda.nudaclient.data.remote.dto.ingredients.IngredientsGetWishlistResponse
import com.nuda.nudaclient.data.remote.dto.products.ProductsGetBrandWishlist
import com.nuda.nudaclient.data.remote.dto.products.ProductsGetProductWishlist
import com.nuda.nudaclient.databinding.FragmentWishlistBinding
import com.nuda.nudaclient.databinding.ItemWishBrandBinding
import com.nuda.nudaclient.databinding.ItemWishIngredientBinding
import com.nuda.nudaclient.databinding.ItemWishProductBinding
import com.nuda.nudaclient.extensions.executeWithHandler
import com.nuda.nudaclient.presentation.product.ProductDetailActivity

class WishlistFragment : Fragment() {

    // TODO 찜한 상품 조회 API 연동 및 데이터 바인딩
    // TODO 찜한 브랜드 조회 API 연동 및 데이터 바인딩
    // TODO 성분 즐겨찾기 조회 API 연동 및 데이터 바인딩

    private var _binding: FragmentWishlistBinding? = null
    private val binding get() = _binding!!


    // 상태 관리 변수
    // 찜한 상품 관련
    private var productCursor: Int? = null
    private var IsLoadingProduct = false
    private var hasNextProduct = false
    // 찜한 브랜드 관련
    private var brandCursor: Int? = null
    private var IsLoadingBrand = false
    private var hasNextBrand = false
    // 즐겨찾는 성분 관련
    private var highlightCursor: Int? = null
    private var IsLoadinghighlight = false
    private var hasNexthighlight = false
    // 피할 성분 관련
    private var avoidCursor: Int? = null
    private var IsLoadingAvoid = false
    private var hasNextAvoid = false

    // 레이아웃 지정 및 뷰 생성
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWishlistBinding.inflate(inflater, container, false)
        return binding.root
    }

    // 뷰 데이터 바인딩, API 호출, 리사이클러뷰 설정
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 무한 스크롤 설정 (4개 모두 설정)
        setupInfiniteScrolls()
    }

    // 화면이 보일 때마다 실행 (탭 전환, 다른 앱 다녀 오기), 최초 생성 시 onViewCreated() 후 호출됨
    override fun onResume() {
        super.onResume()
        // 상태 초기화
        resetState()

        // 기존 뷰 제거
        clearData()

        // 데이터 로드
        loadInitialData()
    }

    // 뷰 파괴 (프래그먼트 객체는 유지)
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    // 상태 초기화
    private fun resetState() {
        // 찜한 상품 상태 초기화
        productCursor = null
        IsLoadingProduct = false
        hasNextProduct = false

        // 찜한 브랜드 상태 초기화
        brandCursor = null
        IsLoadingBrand = false
        hasNextBrand = false

        // 즐겨찾는 성분 상태 초기화
        highlightCursor = null
        IsLoadinghighlight = false
        hasNexthighlight = false

        // 피할 성분 상태 초기화
        avoidCursor = null
        IsLoadingAvoid = false
        hasNextAvoid = false
    }

    // 테스트용 include 제거
    private fun clearData() {
        binding.llWishProducts.removeAllViews()
        binding.llWishBrands.removeAllViews()
        binding.llHighlightIngredients.removeAllViews()
        binding.llAvoidIngredients.removeAllViews()
    }

    // 초기 데이터 로드
    private fun loadInitialData() {
        loadWishProducts()
        loadWishBrands()
        loadHighlightIngredients()
        loadAvoidIngredients()
    }

    // 무한 스크롤 설정
    private fun setupInfiniteScrolls() {
        setupProductScroll()
        setupBrandScroll()
        setupHighlightScroll()
        setupAvoidScroll()
    }

    /**
     * 무한 스크롤 설정
     */

    // 상품 무한 스크롤
    private fun setupProductScroll() {
        binding.llWishProducts.viewTreeObserver.addOnScrollChangedListener {
            val scrollView = binding.llWishProducts.parent as? HorizontalScrollView ?: return@addOnScrollChangedListener

            val scrollX = scrollView.scrollX // 현재 스크롤 위치
            val width = scrollView.width // 스크롤뷰의 보이는 너비
            val contentWidth = binding.llWishProducts.width // 전체 상품 리스트의 너비

            // 끝에서 200dp 전에 다음 데이터 로드 시작
            val thredshold = 200 * resources.displayMetrics.density

            if (scrollX + width >= contentWidth - thredshold) {
                if (hasNextProduct && !IsLoadingProduct) { // 다음 페이지가 있고 로딩 중이 아니라면
                    loadWishProducts() // 다음 페이지 상품 리스트 로드
                }
            }
        }
    }

    // 브랜드 무한 스크롤
    private fun setupBrandScroll() {
        binding.llWishBrands.viewTreeObserver.addOnScrollChangedListener {
            val scrollView = binding.llWishBrands.parent as? HorizontalScrollView ?: return@addOnScrollChangedListener

            val scrollX = scrollView.scrollX // 현재 스크롤 위치
            val width = scrollView.width // 스크롤뷰의 보이는 너비
            val contentWidth = binding.llWishBrands.width // 전체 상품 리스트의 너비

            // 끝에서 200dp 전에 다음 데이터 로드 시작
            val thredshold = 200 * resources.displayMetrics.density

            if (scrollX + width >= contentWidth - thredshold) {
                if (hasNextBrand && !IsLoadingBrand) {// 다음 페이지가 있고 로딩 중이 아니라면
                    loadWishBrands() // 다음 페이지 상품 리스트 로드
                }
            }
        }
    }

   // 관심 성분 무한 스크롤
    private fun setupHighlightScroll() {
        binding.llHighlightIngredients.viewTreeObserver.addOnScrollChangedListener {
            val scrollView = binding.llHighlightIngredients.parent as? HorizontalScrollView ?: return@addOnScrollChangedListener

            val scrollX = scrollView.scrollX // 현재 스크롤 위치
            val width = scrollView.width // 스크롤뷰의 보이는 너비
            val contentWidth = binding.llHighlightIngredients.width // 전체 상품 리스트의 너비

            // 끝에서 200dp 전에 다음 데이터 로드 시작
            val thredshold = 200 * resources.displayMetrics.density

            if (scrollX + width >= contentWidth - thredshold) {
                if (hasNexthighlight && !IsLoadinghighlight) { // 다음 페이지가 있고 로딩 중이 아니라면
                    loadHighlightIngredients() // 다음 페이지 상품 리스트 로드
                }
            }
        }
    }

   // 피할 성분 무한 스크롤
    private fun setupAvoidScroll() {
        binding.llAvoidIngredients.viewTreeObserver.addOnScrollChangedListener {
            val scrollView = binding.llAvoidIngredients.parent as? HorizontalScrollView ?: return@addOnScrollChangedListener

            val scrollX = scrollView.scrollX // 현재 스크롤 위치
            val width = scrollView.width // 스크롤뷰의 보이는 너비
            val contentWidth = binding.llAvoidIngredients.width // 전체 상품 리스트의 너비

            // 끝에서 200dp 전에 다음 데이터 로드 시작
            val thredshold = 200 * resources.displayMetrics.density

            if (scrollX + width >= contentWidth - thredshold) {
                if (hasNextAvoid && !IsLoadingAvoid) { // 다음 페이지가 있고 로딩 중이 아니라면
                    loadAvoidIngredients() // 다음 페이지 상품 리스트 로드
                }
            }
        }
    }


    /**
     * 데이터 로드 메소드 섹션
     * API 호출해서 데이터를 가져옴
     */

    // 찜한 상품 로드
    private fun loadWishProducts() {
        if (IsLoadingProduct) return // 이미 로딩 중이면 중복 요청 방지
        IsLoadingProduct = true

        productsService.getProductWishlist(cursor = productCursor)
            .executeWithHandler(
                context = requireContext(),
                onSuccess = { body ->
                    if (body.success == true) {
                        body.data?.let { data ->
                            // 다음 페이지 정보 저장
                            productCursor = data.nextCursor
                            hasNextProduct = data.hasNext

                            // 받아온 데이터를 UI에 표시
                            data.content.forEach { product ->
                                addProductItem(product)
                            }
                        }
                    }
                    IsLoadingProduct = false
                },
                onError = {
                    IsLoadingProduct = false
                    Log.d("API_DEBUG", "찜한 상품 로드 실패")
                }
            )

    }

    // 찜한 브랜드 로드
    private fun loadWishBrands() {
        if (IsLoadingBrand) return // 이미 로딩 중이면 중복 요청 방지
        IsLoadingBrand = true

        productsService.getBrandWishlist(cursor = brandCursor)
            .executeWithHandler(
                context = requireContext(),
                onSuccess = { body ->
                    if (body.success == true) {
                        body.data?.let { data ->
                            // 다음 페이지 정보 저장
                            brandCursor = data.nextCursor
                            hasNextBrand = data.hasNext

                            // 받아온 데이터 UI에 표시
                            data.content.forEach { brand ->
                                addBrandItem(brand)
                            }
                        }
                    }
                    IsLoadingBrand = false
                },
                onError = {
                    IsLoadingBrand = false
                    Log.e("API_DEBUG", "찜한 브랜드 로드 실패")
                }
            )
    }

    // 관심 성분 로드
    private fun loadHighlightIngredients() {
        if (IsLoadinghighlight) return // 이미 로딩 중이면 중복 요청 방지
        IsLoadinghighlight = true

        ingredientsService.getIngredientWishlist(cursor = highlightCursor, preference = true)
            .executeWithHandler(
                context = requireContext(),
                onSuccess = { body ->
                    if (body.success == true) {
                        body.data?.let { data ->
                            // 다음 페이지 정보 저장
                            highlightCursor = data.nextCursor
                            hasNexthighlight = data.hasNext

                            // 받아온 데이터 UI에 표시
                            data.content.forEach { highlightIngredient ->
                                addHighlightItem(highlightIngredient)
                            }
                        }
                    }
                    IsLoadinghighlight = false
                },
                onError = {
                    IsLoadinghighlight = false
                    Log.e("API_DEBUG", "관심 성분 로드 실패")
                }
            )
    }

    // 피할 성분 로드
    private fun loadAvoidIngredients() {
        if (IsLoadingAvoid) return // 이미 로딩 중이면 중복 요청 방지
        IsLoadingAvoid = true

        ingredientsService.getIngredientWishlist(cursor = avoidCursor, preference = false)
            .executeWithHandler(
                context = requireContext(),
                onSuccess = { body ->
                    if (body.success == true) {
                        body.data?.let { data ->
                            // 다음 페이지 정보 저장
                            avoidCursor = data.nextCursor
                            hasNextAvoid = data.hasNext

                            // 받아온 데이터 UI에 표시
                            data.content.forEach { avoidIngredient ->
                                addAvoidItem(avoidIngredient)
                            }
                        }
                    }
                    IsLoadingAvoid = false
                },
                onError = {
                    IsLoadingAvoid = false
                    Log.e("API_DEBUG", "피할 성분 로드 실패")
                }
            )
    }

    /**
     * UI 업데이트 및 아이템 추가 메소드 섹션
     * 받아온 데이터를 화면에 추가
     */

    // 상품 아이템 추가
    private fun addProductItem(product: ProductsGetProductWishlist.Content) {
        val itemBinding = ItemWishProductBinding.inflate(
            layoutInflater,
            binding.llWishProducts,
            false)

        // 상품 이미지 UI 업데이트
        Glide.with(this)
            .load(product.thumbnailImg)
            .placeholder(R.drawable.image_product2)
            .error(R.drawable.image_product)
            .into(itemBinding.ivProduct)

        itemBinding.tvBrand.text = product.brandName
        itemBinding.tvProductName.text = product.productName
        itemBinding.tvRatingAndReview.text = "${product.averageRating}(${product.reviewCount})"

        // 상품 아이템 클릭 이벤트
        itemBinding.root.setOnClickListener {
            // 상품 상세페이지로 이동하면서 productId 전달
            val intent = Intent(requireContext(), ProductDetailActivity::class.java)
            intent.putExtra("PRODUCT_ID", product.productId)
            startActivity(intent)
        }

        // root 뷰를 추가
        binding.llWishProducts.addView(itemBinding.root)
    }

    // 브랜드 아이템 추가
    private fun addBrandItem(brand: ProductsGetBrandWishlist.Content) {
        val itemBinding = ItemWishBrandBinding.inflate(
            layoutInflater,
            binding.llWishBrands,
            false)

        // 상품 이미지 UI 업데이트
        Glide.with(this)
            .load(brand.logoImg)
            .placeholder(R.drawable.image_product2)
            .error(R.drawable.image_brand)
            .into(itemBinding.ivBrand)

        itemBinding.tvBrand.text = brand.name

        // root 뷰를 추가
        binding.llWishBrands.addView(itemBinding.root)
    }

    // 관심 성분 아이템 추가
    private fun addHighlightItem(highlightIngredient: IngredientsGetWishlistResponse.Content) {
        val itemBinding = ItemWishIngredientBinding.inflate(
            layoutInflater,
            binding.llHighlightIngredients,
            false)

        // 위험도에 따른 원 아이콘 색 변경
        when(highlightIngredient.riskLevel) {
            "SAFE" -> {
                itemBinding.ivRiskLevel.setColorFilter(
                    ContextCompat.getColor(requireContext(), R.color.riskLevel_mint)
                )
            }
            "WARN" -> {
                itemBinding.ivRiskLevel.setColorFilter(
                    ContextCompat.getColor(requireContext(), R.color.riskLevel_yellow)
                )
            }"DANGER" -> {
                itemBinding.ivRiskLevel.setColorFilter(
                    ContextCompat.getColor(requireContext(), R.color.riskLevel_red)
                )
            }"UNKNOWN" -> {
                itemBinding.ivRiskLevel.setColorFilter(
                    ContextCompat.getColor(requireContext(), R.color.gray4)
                )
            }
        }

        itemBinding.tvIngredientName.text = highlightIngredient.name

        // 성분 구성요소 (레이어 층) 텍스트
        when (highlightIngredient.layerType) {
            "TOP_SHEET" -> itemBinding.tvComponent.setText(R.string.component_coverLayer)
            "ABSORBER" -> itemBinding.tvComponent.setText(R.string.component_absorbentCore)
            "BACK_SHEET" -> itemBinding.tvComponent.setText(R.string.component_waterproofLayer)
            "ADHESIVE" -> itemBinding.tvComponent.setText(R.string.component_adhesive)
            "ADDITIVE" -> itemBinding.tvComponent.setText(R.string.component_additionalParts)
        }

        // 성분 아이템 클릭 이벤트
        itemBinding.root.setOnClickListener {
            // 성분 상세페이지로 이동하면서 ingredientId 전달
//            val intent = Intent(requireContext(), ::class.java)
//            intent.putExtra("INGREDIENT_ID", highlightIngredient.ingredientId)
//            startActivity(intent)
        }
        // root 뷰를 추가
        binding.llHighlightIngredients.addView(itemBinding.root)
    }

    // 피할 성분 아이템 추가
    private fun addAvoidItem(avoidIngredient: IngredientsGetWishlistResponse.Content) {
        val itemBinding = ItemWishIngredientBinding.inflate(
            layoutInflater,
            binding.llAvoidIngredients,
            false)

        // 위험도에 따른 원 아이콘 색 변경
        when(avoidIngredient.riskLevel) {
            "SAFE" -> {
                itemBinding.ivRiskLevel.setColorFilter(
                    ContextCompat.getColor(requireContext(), R.color.riskLevel_mint)
                )
            }
            "WARN" -> {
                itemBinding.ivRiskLevel.setColorFilter(
                    ContextCompat.getColor(requireContext(), R.color.riskLevel_yellow)
                )
            }"DANGER" -> {
                itemBinding.ivRiskLevel.setColorFilter(
                    ContextCompat.getColor(requireContext(), R.color.riskLevel_red)
                )
            }"UNKNOWN" -> {
                itemBinding.ivRiskLevel.setColorFilter(
                    ContextCompat.getColor(requireContext(), R.color.gray4)
                )
            }
        }

        itemBinding.tvIngredientName.text = avoidIngredient.name

        // 성분 구성요소 (레이어 층) 텍스트
        when (avoidIngredient.layerType) {
            "TOP_SHEET" -> itemBinding.tvComponent.setText(R.string.component_coverLayer)
            "ABSORBER" -> itemBinding.tvComponent.setText(R.string.component_absorbentCore)
            "BACK_SHEET" -> itemBinding.tvComponent.setText(R.string.component_waterproofLayer)
            "ADHESIVE" -> itemBinding.tvComponent.setText(R.string.component_adhesive)
            "ADDITIVE" -> itemBinding.tvComponent.setText(R.string.component_additionalParts)
        }

        // 성분 아이템 클릭 이벤트
        itemBinding.root.setOnClickListener {
            // 성분 상세페이지로 이동하면서 ingredientId 전달
//            val intent = Intent(requireContext(), ::class.java)
//            intent.putExtra("INGREDIENT_ID", avoidIngredient.ingredientId)
//            startActivity(intent)
        }
        // root 뷰를 추가
        binding.llAvoidIngredients.addView(itemBinding.root)
    }

}