package com.nuda.nudaclient.presentation.search

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.nuda.nudaclient.R
import com.nuda.nudaclient.data.remote.RetrofitClient.searchService
import com.nuda.nudaclient.databinding.ActivitySearchBinding
import com.nuda.nudaclient.extensions.executeWithHandler
import com.nuda.nudaclient.presentation.common.activity.BaseActivity
import com.nuda.nudaclient.presentation.search.adapter.AutoCompleteAdapter

class SearchActivity : BaseActivity() {

    // TODO 제품 검색일 떄, 상품 검색일 때 나눠서 처리 필요 (API 호출도 따로 관리)
    // TODO 제품 검색 결과, 상품 검색 결과 둘 다 같은 액티비티지만 intent에 상태 변수 담아서 함께 전달

    private lateinit var binding : ActivitySearchBinding
    private lateinit var autoCompleteAdapter: AutoCompleteAdapter

    // Debounce용 Handler
    private val debounceHandler = Handler(Looper.getMainLooper())
    private var debounceRunnable: Runnable? = null

    private lateinit var pageMode: String

    // 인기 검색어 리스트 객체 생성
    private val popular_keywords by lazy {
        listOf(
            binding.tvSearch,
            binding.tvSearch2,
            binding.tvSearch3,
            binding.tvSearch4,
            binding.tvSearch5,
            binding.tvSearch6,
            binding.tvSearch7,
            binding.tvSearch8,
            binding.tvSearch9,
            binding.tvSearch10
        )
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        setToolbar() // 툴바 설정

        setSearchMode() // 화면 모드 설정

        // 검색어 자동 완성 세팅
        setupAutoComplete()
        setupSearchBar()
        setupSearchButton()

    }

    override fun onDestroy() {
        super.onDestroy()
        // 메모리 누수 방지
        debounceRunnable?.let { debounceHandler.removeCallbacks(it) }
    }

    // 툴바 설정
    private fun setToolbar() {
        setToolbarTitle("") // 타이틀
        setBackButton() // 뒤로가기 버튼
        setCartButton() // 장바구니 버튼만 추가
        setToolbarShadow(false) // 툴바 그림자 제거
    }

    // 모드 선택 버튼 설정
    private fun setSearchMode() {
        // (기본값) 제품 검색
        setSearchModeButton(true)
        pageMode = "PRODUCT"
        setUIByMode()

        // 제품 검색 버튼
        binding.btnSearchProduct.setOnClickListener {
            setSearchModeButton(true)
            pageMode = "PRODUCT"
            setUIByMode()
        }
        // 상품 검색 버튼
        binding.btnSearchIngredient.setOnClickListener {
            setSearchModeButton(false)
            pageMode = "INGREDIENT"
            setUIByMode()
        }
    }

    // 버튼 모드 설정
    private fun setSearchModeButton(isProductMode: Boolean) { // 둘 중 하나만 선택 가능하도록
        binding.btnSearchProduct.isSelected = isProductMode
        binding.btnSearchIngredient.isSelected = !isProductMode
    }

    // 화면 모드에 따른 UI 업데이트 (인기 검색어 API 호출)
    private fun setUIByMode() {
        when(pageMode) {
            "PRODUCT" -> { // 상품 인기 검색어 조회 API 호출
                searchService.getProductTOP10()
                    .executeWithHandler(
                        context = this,
                        onSuccess = { body ->
                            if (body.success == true) {
                                body.data?.let { data ->
                                    setPopularKeywords(data.stringList)
                                    Log.d("API_DEBUG", "상품 인기 검색어 조회 성공")
                                }
                            }
                        }
                    )
            }
            "INGREDIENT" -> { // 성분 인기 검색어 조회 API 호출
                searchService.getIngredientTOP10()
                    .executeWithHandler(
                        context = this,
                        onSuccess = { body ->
                            if (body.success == true) {
                                body.data?.let { data ->
                                    setPopularKeywords(data.stringList)
                                    Log.d("API_DEBUG", "성분 인기 검색어 조회 성공")
                                }
                            }
                        }
                    )
            }
        }
    }

    // 인기 검색어 업데이트
    private fun setPopularKeywords(keywords: List<String>) {
        popular_keywords.forEachIndexed { index, textView ->
            if (index < keywords.size) {
                textView.visibility = View.VISIBLE
                textView.text = "${index + 1} ${keywords[index]}"
            } else {
                textView.visibility = View.GONE
            }
        }
    }

    // 검색어 자동 완성
    // 자동완성 리사이클러뷰 세팅
    private fun setupAutoComplete() {
        autoCompleteAdapter = AutoCompleteAdapter { keyword ->
            // 드롭다운 항목 클릭 시: 검색바 채우고 바로 검색
            binding.etSearchbar.setText(keyword)
            hideAutoComplete()
            navigateToSearchResult(keyword)
        }

        binding.rvAutocomplete.apply {
            layoutManager = LinearLayoutManager(this@SearchActivity)
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
                    fetchAutoComplete(query)
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
                hideAutoComplete()
                navigateToSearchResult(query)
            }
        }
    }

    // 자동 완성 API 호출
    private fun fetchAutoComplete(query: String) {
        searchService.searchAutoComplete(query, pageMode)
            .executeWithHandler(
                context = this,
                onSuccess = { body ->
                    if (body.success == true) {
                        body.data?.let { data ->
                            val resultKeywords = data.stringList
                            if (resultKeywords.isEmpty()) {
                                hideAutoComplete()
                                return@let
                            }
                            // TODO API 완성 후 수정 필요할지도.?
                            autoCompleteAdapter.submitList(resultKeywords)

                            // 최대 5개 높이로 동적 조절
                            // 리사이클러뷰 아이템이 그려진 후 실제 높이 측정
                            binding.rvAutocomplete.post {
                                val itemHeight = binding.rvAutocomplete.getChildAt(0)?.height ?: 0
                                val count = minOf(resultKeywords.size, 5)
                                binding.rvAutocomplete.layoutParams =
                                    binding.rvAutocomplete.layoutParams.apply {
                                        height = itemHeight * count
                                    }
                                binding.cardAutocomplete.visibility = View.VISIBLE
                            }
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

    // 검색 결과 화면으로 이동
    private fun navigateToSearchResult(query: String) {
        val intent = Intent(this, SearchResultActivity::class.java)
        intent.putExtra("query", query)
        startActivity(intent)
    }

}