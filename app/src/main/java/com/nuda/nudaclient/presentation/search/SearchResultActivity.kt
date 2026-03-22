package com.nuda.nudaclient.presentation.search

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.nuda.nudaclient.R
import com.nuda.nudaclient.data.remote.RetrofitClient.searchService
import com.nuda.nudaclient.databinding.ActivitySearchResultBinding
import com.nuda.nudaclient.extensions.executeWithHandler
import com.nuda.nudaclient.extensions.setInfiniteScrollListener
import com.nuda.nudaclient.presentation.common.activity.BaseActivity
import com.nuda.nudaclient.presentation.ingredient.IngredientDetailActivity
import com.nuda.nudaclient.presentation.ingredient.adapter.IngredientItemAdapter
import com.nuda.nudaclient.presentation.product.ProductDetailActivity
import com.nuda.nudaclient.presentation.product.adapter.ProductAdapter
import com.nuda.nudaclient.presentation.search.adapter.AutoCompleteAdapter
import com.nuda.nudaclient.presentation.search.adapter.SearchIngredientAdapter
import retrofit2.http.Query

// intent에 검색 텍스트, 화면 모드 함께 전달 필요!!
class SearchResultActivity : BaseActivity() {
    // 검색 결과 화면으로 이동할 때 intent에 검색 상태 변수 전달 필요!!

    private lateinit var binding: ActivitySearchResultBinding

    // 검색어 자동 완성 어댑터
    private lateinit var autoCompleteAdapter: AutoCompleteAdapter

    // Debounce용 Handler
    private val debounceHandler = Handler(Looper.getMainLooper())
    private var debounceRunnable: Runnable? = null


    // 검색 결과 리사이클러뷰 어댑터
    private lateinit var productAdapter: ProductAdapter
    private lateinit var ingredientAdapter: SearchIngredientAdapter
    private lateinit var productSignupAdapter: ProductAdapter
    private lateinit var productNewReviewAdapter: ProductAdapter

    private var productCurrentCursor: Int? = null // 다음 페이지 요청에 쓸 커서
    private var productIsLoading = false // 현재 로딩 중인지 체크

    private lateinit var pageMode: String
    private lateinit var query: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySearchResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        pageMode = intent.getStringExtra("PAGEMODE") ?: ""
        query = intent.getStringExtra("query") ?: ""

        binding.etSearchbar.setText(query)

        setToolbar() // 툴바 설정

        createAdapter() // 어댑터 생성
        switchMode(pageMode) // 화면 모드에 따른 어댑터 연결

        // 검색어 자동 완성 세팅
        setupAutoComplete()
        setupSearchBar()
        setupSearchButton()
    }

    override fun onDestroy() {
        super.onDestroy()
        debounceRunnable?.let { debounceHandler.removeCallbacks(it) }
    }

    // 툴바 설정
    private fun setToolbar() {
        setToolbarTitle("") // 타이틀
        setBackButton() // 뒤로가기 버튼
        setToolbarShadow(false) // 툴바 그림자 제거
    }

    // 어댑터 생성
    private fun createAdapter() {
        // 상품 검색
        productAdapter = ProductAdapter() { productId, thumbnail ->
            val intent = Intent(this, ProductDetailActivity::class.java)
            intent.putExtra("PRODUCT_ID", productId)
            startActivity(intent)
        }
        // 성분 검색
        ingredientAdapter = SearchIngredientAdapter() { ingredientId -> // 성분 아이템 클릭 이벤트 설정
            // 성분 상세페이지로 이동, ingredientId 전달
            val intent = Intent(this, IngredientDetailActivity::class.java)
            intent.putExtra("INGREDIENT_ID", ingredientId)
            startActivity(intent)
        }
        // 회원가입 - 설문 - 사용 상품 검색
        productSignupAdapter = ProductAdapter() { productId, thumbnail ->
            val resultIntent = Intent().apply {
                putExtra("PRODUCT_ID", productId)
                putExtra("PRODUCT_THUMBNAIL", thumbnail)
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }
        // 마이페이지 - 새 리뷰 작성 - 상품 검색
        productNewReviewAdapter = ProductAdapter() { productId, thumbnail ->
            val resultIntent = Intent().apply {
                putExtra("PRODUCT_ID", productId)
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }

    // 화면 모드에 따른 어댑터 선택
    private fun switchMode(pageMode: String) {
        when(pageMode) {
            "PRODUCT" -> {
                binding.rvSearchResult.adapter = productAdapter // 상품 어댑터 연결
                loadProductSearch() // 상품 검색 결과 첫 로드
                setScrollListner() // 무한 스크롤 설정
            }
            "INGREDIENT" -> {
                binding.rvSearchResult.adapter = ingredientAdapter // 성분 어댑터 연결
                loadIngredientSearch() // 성분 검색 결과 로드 (전체)
            }
            "PRODUCT_SIGNUP" -> {
                binding.rvSearchResult.adapter = productSignupAdapter // 회원가입 상품 어댑터 연결
                binding.etSearchbar.setHint("제품을 검색하세요")
                setScrollListner() // 무한 스크롤 설정
            }
            "PRODUCT_NEW_REVIEW" -> {
                binding.rvSearchResult.adapter = productNewReviewAdapter // 마이페이지 상품 어댑터 연결
                loadProductSearch() // 상품 검색 결과 첫 로드
                setScrollListner() // 무한 스크롤 설정
            }
            else -> {
                Log.d("API_DEBUG", "검색 결과 화면 모드 오류")
            }
        }
        binding.rvSearchResult.layoutManager = LinearLayoutManager(this)
    }

    // 무힌 스크롤 리스너 설정
    private fun setScrollListner() {
        binding.rvSearchResult.setInfiniteScrollListener {
            if (!productIsLoading // 로딩 중이 아니고
                && productCurrentCursor != null) { // 다음 페이지가 있으면
                loadProductSearch() // 다음 페이지 로드
            }
        }
    }

    // 상품 검색 결과 API 호출
    private fun loadProductSearch() {
        if (productIsLoading) return
        productIsLoading = true

        searchService.searchProduct(query, productCurrentCursor)
            .executeWithHandler(
                context = this,
                onSuccess = { body ->
                    if (body.success == true) {
                        body.data?.let { data ->
                            // 현재 모드에 맞는 어댑터 선택
                            val currentAdapter = when(pageMode) {
                                "PRODUCT" -> productAdapter
                                "PRODUCT_SIGNUP" -> productSignupAdapter
                                "PRODUCT_NEW_REVIEW" -> productNewReviewAdapter
                                else -> productAdapter
                            }

                            if (productCurrentCursor == null) { // 첫 로드인 경우
                                currentAdapter.submitList(data.content)
                            } else { // 첫 로드가 아닌 경우
                                currentAdapter.appendItems(data.content)
                            }

                            // 다음 커서 업데이트
                            productCurrentCursor = if (data.hasNext) { // 다음 페이지가 있으면
                                data.nextCursor
                            } else { // 마지막 페이지면
                                null
                            }
                        }
                    }
                    productIsLoading = false
                },
                onError = {
                    productIsLoading = false
                }
            )
    }

    // 성분 검색 결과 API 호출
    private fun loadIngredientSearch() {
        searchService.searchIngredient(query)
            .executeWithHandler(
                context = this,
                onSuccess = { body ->
                    if (body.success == true) {
                        body.data?.let { data ->
                            ingredientAdapter.submitList(data)
                        }
                    }

                }
            )
    }


    // 검색어 자동 완성
    // 자동완성 리사이클러뷰 세팅
    private fun setupAutoComplete() {
        autoCompleteAdapter = AutoCompleteAdapter { keyword ->
            // 드롭다운 항목 클릭 시: 검색바 채우고 바로 검색
            binding.etSearchbar.setText(keyword)
            hideAutoComplete()
            updateSearchResult(keyword)
        }

        binding.rvAutocomplete.apply {
            layoutManager = LinearLayoutManager(this@SearchResultActivity)
            adapter = autoCompleteAdapter
        }
    }

    // 검색바 입력 감지 + Debounce
    private fun setupSearchBar() {
        binding.etSearchbar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim() ?: ""

                // 이전 Debounce 취소
                debounceRunnable?.let { debounceHandler.removeCallbacks(it) }

                if (query.length < 2) {
                    hideAutoComplete()
                    return
                }

                // 0.5초 뒤에 API 호출
                debounceRunnable = Runnable {
                    fetchAutoComplete(query) // 검색어 자동 완성 API 호출
                }.also {
                    debounceHandler.postDelayed(it, 500L)
                }
            }
        })
    }

    // 검색 버튼 클릭
    private fun setupSearchButton() {
        binding.btnSearch.setOnClickListener {
            val query = binding.etSearchbar.text.toString().trim()
            if (query.isNotEmpty()) {
                debounceRunnable?.let { debounceHandler.removeCallbacks(it) }
                hideAutoComplete()
                updateSearchResult(query)
            }
        }

        binding.etSearchbar.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.etSearchbar.text.toString().trim()
                if (query.isNotEmpty()) {
                    debounceRunnable?.let { debounceHandler.removeCallbacks(it) }
                    hideAutoComplete()
                    updateSearchResult(query)
                }
                true
            } else false
        }
    }

    // 자동 완성 API 호출
    private fun fetchAutoComplete(query: String) {
        searchService.searchAutoComplete(query, pageMode)
            .executeWithHandler(
                context = this,
                onSuccess = { body ->
                    if (body.success == true) {
                        body.data?.let { resultKeywords ->
                            if (resultKeywords.isEmpty()) {
                                hideAutoComplete()
                                return@let
                            }
                            autoCompleteAdapter.submitList(resultKeywords)
                            binding.cardAutocomplete.visibility = View.VISIBLE
                        }
                    }
                }
            )
    }

    // 드롭다운 숨기기
    private fun hideAutoComplete() {
        binding.cardAutocomplete.visibility = View.GONE
        autoCompleteAdapter.submitList(emptyList())
    }

    // 재검색 후 검색 결과 업데이트
    private fun updateSearchResult(query: String) {
        this.query = query // 검색어 업데이트
        hideAutoComplete()
        // 커서 초기화
        productCurrentCursor = null
        productIsLoading = false
        // 모드에 따라 재검색
        when(pageMode) {
            "PRODUCT", "PRODUCT_SIGNUP", "PRODUCT_NEW_REVIEW" -> loadProductSearch()
            "INGREDIENT" -> loadIngredientSearch()
        }
    }



}