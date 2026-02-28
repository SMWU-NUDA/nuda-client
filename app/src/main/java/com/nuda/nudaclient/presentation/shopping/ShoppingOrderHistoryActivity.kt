package com.nuda.nudaclient.presentation.shopping

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.nuda.nudaclient.R
import com.nuda.nudaclient.data.remote.RetrofitClient.shoppingService
import com.nuda.nudaclient.data.remote.dto.shopping.ShoppingGetCartItemsResponse
import com.nuda.nudaclient.data.remote.dto.shopping.ShoppingGetOrderHistoryResponse
import com.nuda.nudaclient.databinding.ActivityShoppingOrderHistoryBinding
import com.nuda.nudaclient.extensions.executeWithHandler
import com.nuda.nudaclient.extensions.setInfiniteScrollListener
import com.nuda.nudaclient.presentation.common.activity.BaseActivity
import com.nuda.nudaclient.presentation.shopping.adapter.OrderHistoryAdapter
import com.nuda.nudaclient.presentation.shopping.convertData.CartItem
import com.nuda.nudaclient.presentation.shopping.convertData.OrderHistoryItem

class ShoppingOrderHistoryActivity : BaseActivity() {

    private lateinit var binding: ActivityShoppingOrderHistoryBinding
    private lateinit var orderHistoryAdapter: OrderHistoryAdapter

    // 어댑터와 공유하는 리스트
    private val orderHistoryItems = mutableListOf<OrderHistoryItem>()

    private var currentCursor: Int? = null // 다음 페이지 요청에 쓸 커서
    private var isLoading = false // 현재 로딩 중인지 체크

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityShoppingOrderHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setToolbar() // 툴바 설정

        setupRecyclerView() // 리사이클러뷰 설정
        loadOrderHistory() // 주문 내역 로드
    }

    // 툴바 설정
    private fun setToolbar() {
        setToolbarTitle("주문 내역") // 타이틀
        setBackButton() // 뒤로가기 버튼
    }

    // 리사이클러뷰 설정
    private fun setupRecyclerView() {
        // 어댑터 설정
        orderHistoryAdapter = OrderHistoryAdapter(
            orderHistoryItems
        )
        binding.rvOrderHistory.apply {
            adapter = orderHistoryAdapter
            layoutManager = LinearLayoutManager(this@ShoppingOrderHistoryActivity)
        }
        setScrollListner() // 스크롤 리스너 등록 (무한 스크롤 감지)
    }

    // 스크롤 리스너 설정 (무한 스크롤)
    private fun setScrollListner() {
        binding.rvOrderHistory.setInfiniteScrollListener {
            if (!isLoading // 로딩 중이 아니고
                && currentCursor != null) { // 다음 페이지가 있으면
                loadOrderHistory() // 다음 페이지 로드
            }
        }
    }

    // 주문 내역 로드
    private fun loadOrderHistory() {
        if (isLoading) return // 로딩 중이면 리턴
        // 로딩 시작
        isLoading = true

        shoppingService.getOrderHistory(cursor = currentCursor)
            .executeWithHandler(
                context = this,
                onSuccess = { body ->
                    if (body.success == true) {
                        body.data?.let { data ->
                            orderHistoryItems.clear()
                            orderHistoryItems.addAll(convertToOrderHistoryItems(data))
                            orderHistoryAdapter.notifyDataSetChanged() // 리스트 전체 갱신

                            // 다음 커서 업데이트
                            currentCursor = if (data.hasNext) { // 다음 페이지가 있으면
                                data.nextCursor
                            } else { // 마지막 페이지면
                                null
                            }
                            Log.d("API_DEBUG", "주문 내역 로드 성공")
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

    // API 응답의 중첩 구조를 리사이클러뷰가 사용할 수 있는 1차원 리스트로 펼치는 함수
    private fun convertToOrderHistoryItems(data: ShoppingGetOrderHistoryResponse) : List<OrderHistoryItem> {
        val convertData = mutableListOf<OrderHistoryItem>()
        data.content.forEach { content ->
            // 날짜 헤더 먼저 추가
            convertData.add(
                OrderHistoryItem.DateHeader(
                    content.orderDate,
                    content.orderNum
                ))
            // 해당 주문의 상품 아이템 추가
            content.brands.forEach { brand ->
               brand.products.forEach { product ->
                   convertData.add(
                       OrderHistoryItem.Product(
                           brand.brandId,
                           brand.brandName,
                           product.productId,
                           product.thumbnailImg,
                           product.productName,
                           product.quantity,
                           product.price,
                           product.totalPrice
                       ))
               }
            }
            // 총 금액 푸터 추가
            convertData.add(
                OrderHistoryItem.PriceFooter(
                    content.totalAmount
                )
            )
        }
        return convertData
    }



}