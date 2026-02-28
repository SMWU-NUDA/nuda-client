package com.nuda.nudaclient.presentation.shopping

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.nuda.nudaclient.R
import com.nuda.nudaclient.data.remote.RetrofitClient.shoppingService
import com.nuda.nudaclient.data.remote.dto.shopping.ShoppingPaymentCompleteResponse
import com.nuda.nudaclient.databinding.ActivityShoppingOrderCompleteBinding
import com.nuda.nudaclient.extensions.executeWithHandler
import com.nuda.nudaclient.presentation.common.activity.BaseActivity
import com.nuda.nudaclient.presentation.navigation.NavigationActivity
import com.nuda.nudaclient.presentation.shopping.adapter.OrderAdapter
import com.nuda.nudaclient.presentation.shopping.convertData.OrderProduct

class ShoppingOrderCompleteActivity : BaseActivity() {
    // 결제 완료 화면으로 이동할 때 Intent에 paymentId 담아서 전달 필요 !!!

    private lateinit var binding: ActivityShoppingOrderCompleteBinding
    private lateinit var orderAdapter: OrderAdapter

    // 리사이클러뷰에 출력할 리스트 (1차원 변환)
    private val orderItems = mutableListOf<OrderProduct>()

    var paymentId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityShoppingOrderCompleteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // paymentId 전달 받기
        paymentId = intent.getIntExtra("PAYMENT_ID", -1)

        setToolbar()

        setupRecyclerView() // 리사이클러뷰 설정
        loadPaymentInfo() // 결제 정보 화면 로드

        setHomeButton() // 홈 이동 버튼

    }


    // 툴바 설정
    private fun setToolbar() {
        setToolbarTitle("주문 완료") // 타이틀
        setToolbarBackBtn(false) // 뒤로가기 버튼 숨김
        setToolbarShadow(false) // 툴바 그림자 숨김
    }

    // 리사이클러뷰 설정
    private fun setupRecyclerView() {
        // 어댑터 설정
        orderAdapter = OrderAdapter(
            items = orderItems
        )
        binding.rvOrderProduct.apply {
            adapter = orderAdapter
            layoutManager = LinearLayoutManager(this@ShoppingOrderCompleteActivity)
        }
    }

    // 결제 완료 정보 로드
    private fun loadPaymentInfo() {
        // 3. 결제 테스트용 완료 API 호출
        shoppingService.CompletePayment(paymentId)
            .executeWithHandler(
                context = this,
                onSuccess = { body ->
                    if (body.success == true) {
                        body.data?.let { data ->
                            binding.tvOrderNumber.text = data.orderNum.toString()
                            binding.tvName.text = data.deliveryResponse.recipient
                            binding.tvPhone.text = data.deliveryResponse.phoneNum
//                            TODO binding.tvTotalPrice.text = data.
                            val address = "${data.deliveryResponse.address1} ${data.deliveryResponse.address2} (${data.deliveryResponse.postalCode})"
                            binding.tvAddress.text = address

                            // 주문 상품 목록
                            orderItems.clear()
                            orderItems.addAll(convertToOrderItems(data))
                            orderAdapter.notifyDataSetChanged() // 리스트 전체 갱신

                            Log.d("API_DEBUG", "주문 3. 결제 테스트용 완료 API 호출 성공")
                        }


                    }
                }
            )
    }

    // API 응답의 중첩 구조를 리사이클러뷰가 사용할 수 있는 1차원 리스트로 펼치는 함수
    private fun convertToOrderItems(data: ShoppingPaymentCompleteResponse) : List<OrderProduct> {
        val convertData = mutableListOf<OrderProduct>()
        data.brands.forEach { brand ->
            // 상품 아이템 1차원 리스트로 변환
            brand.products.forEach { product ->
                convertData.add(
                    OrderProduct(
                        brand.brandId,
                        brand.brandName,
                        product.productId,
                        product.productName,
                        product.quantity,
                        product.price,
                        product.totalPrice
                    )
                )
            }
        }
        return convertData
    }

    // 홈 이동 버튼 설정
    private fun setHomeButton() {
        binding.btnGoToHome.setOnClickListener {
            startActivity(Intent(this, NavigationActivity::class.java))
            finish()
        }
    }


}