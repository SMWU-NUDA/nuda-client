package com.nuda.nudaclient.presentation.review

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.nuda.nudaclient.R
import com.nuda.nudaclient.data.remote.RetrofitClient.productsService
import com.nuda.nudaclient.data.remote.RetrofitClient.reviewsService
import com.nuda.nudaclient.data.remote.RetrofitClient.searchService
import com.nuda.nudaclient.data.remote.dto.reviews.ReviewsCreateReviewRequest
import com.nuda.nudaclient.data.remote.dto.reviews.ReviewsUploadImageRequest
import com.nuda.nudaclient.data.remote.dto.reviews.UriWithFile
import com.nuda.nudaclient.databinding.ActivityReviewCreateBinding
import com.nuda.nudaclient.extensions.executeWithHandler
import com.nuda.nudaclient.extensions.toFormattedPrice
import com.nuda.nudaclient.presentation.common.activity.BaseActivity
import com.nuda.nudaclient.presentation.search.SearchResultActivity
import com.nuda.nudaclient.presentation.search.adapter.AutoCompleteAdapter
import com.nuda.nudaclient.utils.CustomToast
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class ReviewCreateActivity : BaseActivity() {
    // 리뷰 작성 화면으로 이동할 때 Intent에 상태 변수 담아서 전달 필요 !!! (state : product / mypage)
    private val TAG = "ReviewCreateActivity"

    private lateinit var binding: ActivityReviewCreateBinding

    private lateinit var autoCompleteAdapter: AutoCompleteAdapter

    // Debounce용 Handler
    private val debounceHandler = Handler(Looper.getMainLooper())
    private var debounceRunnable: Runnable? = null

    // 자동완성의 textWatcher 무시 플래그
    private var isSettingText = false

    // 액티비티 중복 사용을 위한 상태 변수
    private lateinit var state: String // product, mypage

    private var productId: Int = -1 // 상품 ID

    // 선택된 이미지의 Uri를 담아두는 리스트
    private var selectedImageUris = mutableListOf<Uri>()
    // 리뷰 API 호출에 사용할 이미지 Url 리스트
    private var uploadedImageUrls = mutableListOf<String>()

    // 리뷰 이미지 최대 5장 선택 가능
    private val pickImage = registerForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(5)
    ) { uris ->
        // 사용자가 이미지를 선택했을 때 콜백으로 Uri 리스트가 넘어옴
        if (uris.isNotEmpty()) {
            selectedImageUris.addAll(uris) // 기존 거 유지하고 새로 선택한 거 추가
            updateImagePreview(selectedImageUris) // 전체 리스트로 미리보기 갱신
            getPresignedUrl(uris) // 새로 추가된 것만 업로드
        }
    }

    // launcher 등록 (onCreate 전에 선언)
    private val  searchProductLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedProductId = result.data?.getIntExtra("PRODUCT_ID", -1) ?: return@registerForActivityResult

            if (selectedProductId == -1) return@registerForActivityResult

            // 선택한 상품 아이디 저장
            productId = selectedProductId
            // 상태 변경 및 화면 재로드
            state = "product"
            loadScreen()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityReviewCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Intent에서 값 받기
        state = intent.getStringExtra("STATE") ?: "" // 상태 변수 값
        productId = intent.getIntExtra("PRODUCT_ID", -1) // 상품 아이디(state = product로 진입 시에만)

        // 초기 별점 설정
        binding.ratingBar.rating = 0f // 초기 기본값 0
        // 상품 아이템 카드 아래의 구분선 삭제
        binding.itemProductCard.line.visibility = View.GONE

        setToolbar() // 툴바 설정

        loadScreen() // 상태 변수에 따른 화면 로드

        setAddImages() // 사진 등록 버튼
        setSaveReview() // 리뷰 등록 버튼
        setDetailReview() // 상세 리뷰 입력창 설정
    }

    // 툴바 설정
    private fun setToolbar() {
        setToolbarTitle("새 리뷰 쓰기") // 타이틀
        setBackButton() // 뒤로가기 버튼
    }

    // 상세 리뷰 입력창 설정
    private fun setDetailReview() {
        binding.etReviewDetail.setOnTouchListener { v, event ->
            v.parent.requestDisallowInterceptTouchEvent(true)
            if (event.action == MotionEvent.ACTION_UP) {
                v.parent.requestDisallowInterceptTouchEvent(false)
            }
            false
        }
    }

    // 상태 변수에 따른 화면 로드
    private fun loadScreen() {
        binding.itemProductCard.tvRank.visibility = View.GONE // 상품 아이템 카드의 순위 제거

        // guideline을 0dp로 이동
        val guideline = binding.itemProductCard.guideline
        val params = guideline.layoutParams as ConstraintLayout.LayoutParams
        params.guideBegin = 0
        guideline.layoutParams = params

        when (state) {
            "mypage" -> { // 마이페이지의 리뷰 작성일 때 - 상품 선택 필요
                binding.clSearch.visibility = View.VISIBLE // 검색바 설정
                binding.itemProductCard.root.visibility = View.GONE // 상품 아이템 카드 설정
                binding.viewOverlay.visibility = View.VISIBLE // 오버레이 설정

                // 검색어 자동 완성 세팅
                setupAutoComplete()
                setupSearchBar()
                setupSearchButton()
            }
            "product" -> { // 상품 상세 페이지의 리뷰 작성일 때 - 상품 선택 필요 없음
                binding.clSearch.visibility = View.GONE // 검색바 설정
                binding.itemProductCard.root.visibility = View.VISIBLE // 상품 아이템 카드 설정
                binding.viewOverlay.visibility = View.GONE // 오버레이 없음

                loadProductInfo() // 상품 정보 로드
            }
        }
    }

    private fun loadProductInfo() {
        productsService.getProductInfo(productId)
            .executeWithHandler(
                context = this,
                onSuccess = { body ->
                    if (body.success == true) {
                        body.data?.let { data ->
                            binding.itemProductCard.tvProductBrand.text = data.brandName
                            binding.itemProductCard.tvProductName.text = data.name
                            binding.itemProductCard.tvRatingAndReview.text = "${data.averageRating}(${data.reviewCount})"
                            binding.itemProductCard.tvProductPrice.text = data.price.toFormattedPrice()

                            // 성분 바인딩 (최대 3개)
                            val ingredients = data.ingredientLabels // List<String>
                            binding.itemProductCard.tvProductIngredient.text = when {
                                ingredients.isNullOrEmpty() -> "키워드 없음"
                                ingredients.size <= 3 -> ingredients.joinToString(", ")
                                else -> ingredients.take(3).joinToString(", ") + " ..."
                            }

                            // 상품 이미지
                            Glide.with(this)
                                .load(data.mainImageUrls[0])
                                .placeholder(R.drawable.image_product2)
                                .error(R.drawable.image_product)
                                .centerCrop()
                                .into(binding.itemProductCard.ivProduct)

                        }

                    }
                }
            )
    }

    // 리뷰 이미지 등록 버튼 설정
    private fun setAddImages() {
        binding.imgAddProduct.setOnClickListener {
            // 버튼 클릭 시 갤러리 열기
            pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    // 선택된 이미지 미리보기 설정
    private fun updateImagePreview(uris: List<Uri>) {
        val container = binding.llImageContainer
        container.removeAllViews() // 기존 뷰 제거 후 다시 그림

        // 선택된 이미지마다 아이템 뷰 추가
        uris.forEachIndexed { index, uri ->
            val itemView = layoutInflater.inflate(R.layout.item_review_upload_image, container, false)
            val ivPreview = itemView.findViewById<ImageView>(R.id.ivPreview)
            val btnDelete = itemView.findViewById<ImageView>(R.id.btnDelete)

            // 이미지 로드 (Glide 사용)
            Glide.with(this).load(uri).into(ivPreview)

            // x 버튼 클릭 시 해당 이미지 삭제
            btnDelete.setOnClickListener {
                selectedImageUris.removeAt(index)
                uploadedImageUrls.removeAt(index) // 업로드된 url도 초기화
                updateImagePreview(selectedImageUris) // 미리보기 갱신
            }
            container.addView(itemView) // 화면 이미지 리스트에 추가
        }
    }

    // Presigned URL 발급 (네트워크 요청)
    // 선택된 이미지 Uri 리스트를 받아서 presigned URL 발급
    // S3 업로드 후 리뷰 API에 쓸 imageUrl 리스트 반환
    fun getPresignedUrl(uris: List<Uri>): List<String> {

        // 모든 Uri에서 파일 정보 추출해서 리스트 만들기
        // uri도 함께 저장하기 위해 새로운 데이터 클래스 및 리스트 생성
        val uriWithFiles = mutableListOf<UriWithFile>()

        // uri 전부 파일 정보 추출 및 저장
        for (uri in uris) {
            val contentType = getContentType(uri) // Uri에서 확장자 추출
            val fileName = getFileName(uri, contentType) // Uri에서 파일명 추출
            var file: ReviewsUploadImageRequest.File // File 데이터 형식으로 저장할 변수

            // 확장자가 지원하지 않는 형식일 때 (png, jpeg, webp만 지원)
            if (contentType == null) {
                CustomToast.show(binding.root, "png, jpeg, webp 확장자 이미지만 업로드 가능합니다")
                continue // 다음 이미지로 넘어감
            }

            // API 요청에 사용할 uriWithFiles 리스트에 데이터 저장 (uri, 파일 이름, 확장자)
            uriWithFiles.add(
                UriWithFile(
                    uri = uri,
                    file = ReviewsUploadImageRequest.File(fileName, contentType)
                )
            )
        }

        // 유효한 파일이 없으면 빈 리스트 반환
        if (uriWithFiles.isEmpty()) return uploadedImageUrls

        // S3 Presigned URL 발급 API 호출
        reviewsService.uploadReviewImages(
            ReviewsUploadImageRequest(
                files = uriWithFiles.map { it.file } // file만 꺼내서 리스트 새로 생성
            )
        ).executeWithHandler(
            context = this,
            onSuccess = { body ->
                if (body.success == true) {
                    // 응답 데이터 urls 리스트 저장
                    val urls = body.data ?: return@executeWithHandler

                    // 응답 urls 리스트 순서대로 S3 업로드
                    urls.forEachIndexed { index, url ->
                        // uploadUrl로 S3에 실제 이미지 업로드
                        uploadToS3(
                            uri = uriWithFiles[index].uri,
                            uploadUrl = url.uploadUrl,
                            contentType = uriWithFiles[index].file.contentType
                        )
                        // 리뷰 API에 쓸 imageUrl 저장
                        uploadedImageUrls.add(url.imageUrl)
                    }
                }
            }
        )
        return uploadedImageUrls
    }

    // uploadUrl로 이미지를 S3에 직접 업로드
    fun uploadToS3(uri: Uri, uploadUrl: String, contentType: String) {
        val bytes = contentResolver.openInputStream(uri)?.readBytes() ?: return

        val requestBody = bytes.toRequestBody(contentType.toMediaType())

        val request = Request.Builder()
            .url(uploadUrl)
            .put(requestBody)
            .header("Content-Type", contentType)
            .build()

        // suspend 대신 Thread로 IO 작업 실행 (에러 방지)
        Thread {
            OkHttpClient().newCall(request).execute()
        }.start()
    }

    // Uri에서 실제 파일명을 추출하는 함수
    // 추출 실패 시 타임스탬프 기반 이름으로 대체
    private fun getFileName(uri: Uri, contentType: String?): String {
        var name = "image_${System.currentTimeMillis()}.${contentType}"
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst()) name = cursor.getString(nameIndex)
        }
        return name
    }

    // 확장자 추출 함수
    fun getContentType(uri: Uri): String? {
        val contentType = contentResolver.getType(uri)

        // 백엔드에서 허용하는 형식은 image/jpeg, image/png 만
        // 그 외 형식이면 null 반환
        return when (contentType) {
            "image/jpeg", "image/png", "image/webp" -> contentType
            else -> null
        }
    }

    // 리뷰 등록 버튼 설정
    private fun setSaveReview() {
        binding.btnSaveReview.setOnClickListener {
            val rating = binding.ratingBar.rating.toDouble() // 별점
            val reviewText = binding.etReviewDetail.text.toString() // 상세 리뷰
            val imageUrls = if (uploadedImageUrls.isEmpty()) {
                emptyList()
            } else {
                uploadedImageUrls
            }

            // 유효성 검사
            if (rating == 0.0) { // 별점 입력이 없을 때
                CustomToast.show(binding.root, "별점을 입력해주세요")
                Log.e("API_ERROR", "[$TAG] 별점 미입력")
                return@setOnClickListener
            }

            if (reviewText.isBlank()) { // 상세 리뷰 입력이 없을 때
                CustomToast.show(binding.root, "상세 리뷰를 입력해주세요")
                Log.e("API_ERROR", "[$TAG] 상세 리뷰 미입력")
                return@setOnClickListener
            }

            // 리뷰 작성 API 호출
            reviewsService.createReview(
                ReviewsCreateReviewRequest(
                    productId = productId ?: -1,
                    rating = rating,
                    content = reviewText,
                    imageUrls = imageUrls
                )
            ).executeWithHandler(
                context = this,
                onSuccess = { body ->
                    if (body.success == true) {
                        Log.d("API_DEBUG", "[$TAG] 리뷰 등록 성공")
                        finish() // 액티비티 종료, 이전 화면으로 이동
                    }
                }
            )
        }

    }

    // 검색어 자동 완성
    // 자동완성 리사이클러뷰 세팅
    private fun setupAutoComplete() {
        autoCompleteAdapter = AutoCompleteAdapter { keyword ->
            // debounce 취소
            debounceRunnable?.let { debounceHandler.removeCallbacks(it) }
            // 드롭다운 숨기기
            hideAutoComplete()

            isSettingText = true // TextWatcher 무시 플래그 ON
            binding.etSearchbar.setText(keyword) // 검색바 채우고 바로 검색
            binding.etSearchbar.setSelection(keyword.length) // 커서를 맨 끝으로
            isSettingText = false // 플래그 OFF

            navigateToSearchResult(keyword)
        }

        binding.rvAutocomplete.apply {
            layoutManager = LinearLayoutManager(this@ReviewCreateActivity)
            adapter = autoCompleteAdapter
        }
    }

    // 검색바 입력 감지 + Debounce
    private fun setupSearchBar() {
        binding.etSearchbar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isSettingText) return // 플래그 on이면 무시
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
                navigateToSearchResult(query)
            }
        }

        binding.etSearchbar.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.etSearchbar.text.toString().trim()
                if (query.isNotEmpty()) {
                    debounceRunnable?.let { debounceHandler.removeCallbacks(it) }
                    hideAutoComplete()
                    navigateToSearchResult(query)
                }
                true
            } else false
        }
    }

    // 자동 완성 API 호출
    private fun fetchAutoComplete(query: String) {
        searchService.searchAutoComplete(query, "PRODUCT")
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

    // 검색 결과 화면으로 이동
    private fun navigateToSearchResult(query: String) {
        val intent = Intent(this, SearchResultActivity::class.java)
        intent.putExtra("query", query)
        intent.putExtra("PAGEMODE", "PRODUCT_NEW_REVIEW")
        searchProductLauncher.launch(intent)

        Log.d("API_DEBUG", "[$TAG] 상품 검색 결과로 화면 이동")
    }


}