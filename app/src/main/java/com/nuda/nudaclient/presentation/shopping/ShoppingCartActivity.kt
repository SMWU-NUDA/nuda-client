package com.nuda.nudaclient.presentation.shopping

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.nuda.nudaclient.R
import com.nuda.nudaclient.data.remote.RetrofitClient.shoppingService
import com.nuda.nudaclient.data.remote.dto.shopping.ShoppingChangeQuantityRequest
import com.nuda.nudaclient.data.remote.dto.shopping.ShoppingCreateOrderRequest
import com.nuda.nudaclient.data.remote.dto.shopping.ShoppingDeleteSelectedCartItemRequest
import com.nuda.nudaclient.data.remote.dto.shopping.ShoppingGetCartItemsResponse
import com.nuda.nudaclient.databinding.ActivityShoppingCartBinding
import com.nuda.nudaclient.extensions.executeWithHandler
import com.nuda.nudaclient.extensions.toFormattedPrice
import com.nuda.nudaclient.presentation.common.activity.BaseActivity
import com.nuda.nudaclient.presentation.shopping.adapter.CartAdapter
import com.nuda.nudaclient.presentation.shopping.convertData.CartItem

class ShoppingCartActivity : BaseActivity() {

    // TODO 주문하기 로직 구현

    private lateinit var binding: ActivityShoppingCartBinding
    private lateinit var cartAdapter: CartAdapter

    // 어댑터와 공유하는 리스트
    // cartItems가 바뀌면 notifyItemChanged로 어댑터에 알려줘야 화면이 갱신됨
    private val cartItems = mutableListOf<CartItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityShoppingCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setToolbar() // 툴바 설정

        setupRecyclerView() // 리사이클러뷰 설정

        loadCartItems() // 장바구니 데이터 로드

        updateAllCheck() // 전체 선택 설정
        setupDeleteButton() // 삭제 버튼 설정

        setupOrderButton() // 주문하기 버튼 설정

    }

    // 툴바 설정
    private fun setToolbar() {
        setToolbarTitle("장바구니") // 타이틀
        setBackButton() // 뒤로가기 버튼
    }

    // 리사이클러뷰 설정
    private fun setupRecyclerView() {
        // 어댑터 설정
        cartAdapter = CartAdapter(
            items = cartItems,
            onBrandChecked = { position, isChecked ->
                updateBrandCheck(position, isChecked)
            },
            onProductChecked = { position, isChecked ->
                updateProductCheck(position, isChecked)
            },
            onQuantityChanged = { cartItemId, delta ->
                changeQuantity(cartItemId, delta)
            },
            onDeleteClicked = { cartItemId ->
                deleteCartItem(cartItemId)
            }
        )
        binding.rvCartItems.adapter = cartAdapter
        // 레이아웃 매니저 설정
        binding.rvCartItems.layoutManager = LinearLayoutManager(this)
    }

    // 장바구니 아이템 로드
    private fun loadCartItems() {
        shoppingService.getCartItems() // 장바구니 조회 API 호출
            .executeWithHandler(
                context = this,
                onSuccess = { body ->
                    if (body.success == true) {
                        body.data?.let { data ->
                            cartItems.clear() // 기존 카트 아이템 제거
                            cartItems.addAll(convertToCartItems(data)) // 중첩 구조의 응답을 1차원 리스트로 변환 후 추가
                            cartAdapter.notifyDataSetChanged() // 리스트 전체 갱신
                            
                            updateTotalQuantity() // 총 상품 개수 업데이트
                            updateTotalPrice() // 총 가격 업데이트
                            syncAllCheckState() // 전체 선택 체크 상태 동기화

                            Log.d("API_DEBUG", "cartItems: $cartItems, totalQuantity: ${data.totalQuantity}, totalPrice: ${data.totalPrice}")
                        }
                    }
                }
            )
    }

    // API 응답의 중첩 구조를 리사이클러뷰가 사용할 수 있는 1차원 리스트로 펼치는 함수
    private fun convertToCartItems(data: ShoppingGetCartItemsResponse) : List<CartItem> {
        val convertData = mutableListOf<CartItem>()
        data.brands.forEach { brand ->
            // 브랜드 헤더 먼저 추가
            convertData.add(
                CartItem.BrandHeader(
                brand.brandId,
                brand.brandName
            ))
            // 해당 브랜드의 상품 아이템 추가
            brand.products.forEach { product ->
                convertData.add(
                    CartItem.Product(
                    product.cartItemId,
                    brand.brandId,
                    brand.brandName,
                    product.productId,
                    product.productName,
                    product.quantity,
                    product.price,
                    product.totalPrice
                ))
            }
        }
        return convertData
    }

    // 상품 체크 박스 클릭 시 처리
    private fun updateProductCheck(position: Int, isChecked: Boolean) {
        // 1. 클릭한 상품의 isChecked 상태 업데이트
        val product = cartItems[position] as CartItem.Product // Product 타입으로 강제 변환
        product.isChecked = isChecked // 체크 상태 업데이트
        // 어댑터에 변경 사항 알리기
        cartAdapter.notifyItemChanged(position) // 해당 position 뷰 갱신

        // 2. 이 상품이 속한 브랜드 헤더의 position 찾기
        // indexOfLast: 조건에 맞는 마지막 인덱스 반환
        val brandPosition = cartItems.indexOfFirst { item ->
            // 브랜드 헤더 이면서 브랜드 아이디가 같은 것의 첫 번째 인덱스 반환
            item is CartItem.BrandHeader && item.brandId == product.brandId
        }

        // 3. 같은 브랜드 상품들 필터링
        val brandProducts = cartItems.filter { item ->
            item is CartItem.Product && item.brandId == product.brandId
        }

        // 4. 브랜드 내 상품이 전부 체크되었는지 확인
        // all { } : 모든 아이템이 조건을 만족하면 true
        val allChecked = brandProducts.all { it is CartItem.Product && it.isChecked }

        // 5. 브랜드 헤더 체크 상태 업데이트
        // 해당 브랜드 상품 전체 체크이면 브랜드 또한 체크, 하나라도 해제면 브랜드도 해제
        (cartItems[brandPosition] as CartItem.BrandHeader).isChecked = allChecked
        cartAdapter.notifyItemChanged(brandPosition) // 해당 브랜드 position 뷰 갱신

        updateTotalQuantity() // 총 상품 개수 업데이트
        updateTotalPrice() // 총 가격 업데이트
        syncAllCheckState() // 전체 선택 체크 상태 동기화
    }

    // 브랜드 체크 박스 클릭 시 처리
    private fun updateBrandCheck(position: Int, isChecked: Boolean) {
        // 1. 브랜드 헤더 체크 상태 업데이트
        val brand = cartItems[position] as CartItem.BrandHeader // BrandHeader 타입으로 강제 변환
        brand.isChecked = isChecked // 체크 상태 업데이트
        cartAdapter.notifyItemChanged(position) // 해당 position 뷰 갱신

        // 2. 해당 브랜드 상품을 찾아서 모두 상태 업데이트
        cartItems.forEachIndexed { index, item ->
            if (item is CartItem.Product && item.brandId == brand.brandId) {
                item.isChecked = isChecked // 체크 상태 업데이트
                cartAdapter.notifyItemChanged(index) // 해당 뷰 갱신
            }
        }

        updateTotalQuantity() // 총 상품 개수 업데이트
        updateTotalPrice() // 총 가격 업데이트
        syncAllCheckState() // 전체 선택 체크 상태 동기화
    }

    // 전체 선택 체크 박스 클릭 시 처리
    private fun updateAllCheck() {
        // 전체 선택 체크 박스 선택 리스너
        binding.cbAll.setOnCheckedChangeListener { _, isChecked ->
            cartItems.forEachIndexed { index, item ->
                // 체크 상태 업데이트
                when (item) {
                    is CartItem.BrandHeader -> item.isChecked = isChecked
                    is CartItem.Product -> item.isChecked = isChecked
                }
                cartAdapter.notifyItemChanged(index)
            }

            updateTotalQuantity() // 총 상품 개수 업데이트
            updateTotalPrice() // 총 가격 업데이트
        }
    }

    // 전체 선택 체크 박스 상태 동기화
    private fun syncAllCheckState() {
        // 리스너 제거 후 세팅 (무한 루프 방지)
        binding.cbAll.setOnCheckedChangeListener(null)

        if (cartItems.isEmpty()) { // 리스트가 비어있을 때
            binding.cbAll.isChecked = false
            updateAllCheck() // 리스너 재등록
            return
        }

        // 전체 상품이 선택되었는지 확인
        val allChecked = cartItems
            .filterIsInstance<CartItem.Product>()
            .all { it.isChecked }
        
        binding.cbAll.isChecked = allChecked
        updateAllCheck() // 리스너 재등록
    }

    // 총 가격 업데이트
    private fun updateTotalPrice() {
        var totalPrice = cartItems
            .filterIsInstance<CartItem.Product>() // 상품 아이템만 필터링
            .filter { it.isChecked } // 체크된 상품만 필터링
            .sumOf { it.totalPrice } // 체크된 상품의 totalPrice 전부 더하기

        if (totalPrice > 0) { // 총 가격이 0원 이상이면
            binding.btnOrder.text = "${totalPrice.toFormattedPrice()} 주문하기" // 가격 포맷
            binding.btnOrder.isEnabled = true // 버튼 클릭 가능
        } else { // 총 가격이 0원 이하이면
            binding.btnOrder.text = "주문하기"
            binding.btnOrder.isEnabled = false // 버튼 클릭 불가능
        }
    }

    // 총 상품 개수 업데이트
    private fun updateTotalQuantity() {
        var totalQuantity = cartItems
            .filterIsInstance<CartItem.Product>() // 상품 아이템만 필터링
            .filter { it.isChecked } // 체크된 상품만 필터링
            .sumOf { it.quantity } // 체크된 상품의 quantity 전부 더하기

        binding.tvTotalCount.text = totalQuantity.toString() // 총 상품 개수 업데이트
    }

    // 상품 수량 변경
    private fun changeQuantity(cartItemId: Int, delta: Int) {
        shoppingService.changeQuantity(cartItemId, ShoppingChangeQuantityRequest(delta))
            .executeWithHandler(
                context = this,
                onSuccess = { body ->
                    if (body.success == true) {
                        body.data?.let { data ->
                            if (data.quantity == 0) { // 수량이 0이면
                                // 서버에서 상품 데이터 삭제 완료, UI만 제거
                                val brandId = (cartItems.find {
                                    it is CartItem.Product && it.cartItemId == cartItemId
                                } as CartItem.Product).brandId
                                cartItems.removeIf { it is CartItem.Product && it.cartItemId == cartItemId }

                                // 브랜드 내 상품 전부 삭제이면 브랜드 헤더 또한 삭제
                                val hasProduct = cartItems.any { it is CartItem.Product && it.brandId == brandId }
                                if (!hasProduct) {
                                    cartItems.removeIf { it is CartItem.BrandHeader && it.brandId == brandId }
                                }
                                
                                cartAdapter.notifyDataSetChanged() // 리스트 전체 갱신
                            } else {
                                // 수량 변경한 상품 찾기
                                val product = cartItems.find { it is CartItem.Product && it.cartItemId == cartItemId } as CartItem.Product
                                product.quantity = data.quantity // 수량 업데이트
                                product.totalPrice = data.totalPrice // 상품 가격 업데이트
                                val productPosition = cartItems.indexOf(product) // position 찾기
                                cartAdapter.notifyItemChanged(productPosition) // 변경 감지 리스너
                            }

                            updateTotalQuantity() // 총 상품 개수 업데이트
                            updateTotalPrice() // 총 가격 업데이트
                            syncAllCheckState() // 전체 선택 체크 상태 동기화
                        }
                    }
                }
            )
    }

    // 단건 상품 삭제
    private fun deleteCartItem(cartItemId: Int) {
        shoppingService.deleteCartItem(cartItemId)
            .executeWithHandler(
                context = this,
                onSuccess = { body ->
                    if (body.success == true) {
                        // 삭제할 상품의 브랜드 아이디
                        val brandId = (cartItems.find {
                            it is CartItem.Product && it.cartItemId == cartItemId
                        } as CartItem.Product).brandId
                        // 상품 삭제
                        cartItems.removeIf { it is CartItem.Product && it.cartItemId == cartItemId }
                        // 해당 상품 브랜드에 상품이 하나도 없으면 브랜드 헤더도 삭제
                        // 상품이 하나라도 있으면 true
                        val hasProduct = cartItems.any { it is CartItem.Product && it.brandId == brandId }
                        if (!hasProduct) { // 상품이 없을 때(false)
                            // 브랜드 헤더 삭제
                            cartItems.removeIf { it is CartItem.BrandHeader && it.brandId == brandId }
                        }

                        cartAdapter.notifyDataSetChanged() // 리스트 전체 갱신
                        updateTotalQuantity() // 총 상품 개수 업데이트
                        updateTotalPrice() // 총 가격 업데이트
                        syncAllCheckState() // 전체 선택 체크 상태 동기화
                    }
                }
            )

    }

    // 선택 삭제 버튼 설정 (선택 삭제, 전체 삭제)
    private fun setupDeleteButton() {
        binding.btnDelete.setOnClickListener {
            if (binding.cbAll.isChecked) { // 전체 선택 체크박스 선택된 상태일 때 -> 전체 삭제 API 호출
                shoppingService.deleteAllCartItems()
                    .executeWithHandler(
                        context = this,
                        onSuccess = { body ->
                            if (body.success == true) {
                                cartItems.clear() // 장바구니 아이템 리스트 초기화
                                cartAdapter.notifyDataSetChanged() // 리스트 전체 갱신
                                updateTotalQuantity() // 총 상품 개수 업데이트
                                updateTotalPrice() // 총 가격 업데이트
                                syncAllCheckState() //  전체 선택 체크 상태 동기화
                            }
                        }
                    )
            } else { // 일부 선택 상태일 때 -> 선택 삭제 API 호출
                // 선택된 상품들의 cartItemId 리스트
                val cartItemIds = cartItems.filterIsInstance<CartItem.Product>()
                    .filter { it.isChecked }
                    .map { it.cartItemId } // cartItemId만 뽑아서 리스트로 만듦

                // 전체 선택된 브랜드의 brandId 리스트
                val brandIds = cartItems.filterIsInstance<CartItem.BrandHeader>()
                    .filter { it.isChecked } // 모든 상품이 체크되면 브랜드 헤더도 체크
                    .map { it.brandId } // brandId만 뽑아서 리스트로 만듦

                // 선택 삭제 API 호출
                shoppingService.deleteSelectedCartitems(
                    ShoppingDeleteSelectedCartItemRequest(cartItemIds))
                    .executeWithHandler(
                        context = this,
                        onSuccess = { body ->
                            if (body.success == true) {
                                // 상품 삭제
                                cartItems.removeIf { item ->
                                    item is CartItem.Product && item.cartItemId in cartItemIds
                                }
                                // 브랜드 헤더 삭제
                                brandIds.forEach { brandId ->
                                    // 브랜드 별로 상품이 남아있는지 확인
                                    val hasProduct = cartItems.any { it is CartItem.Product && it.brandId == brandId }
                                    if (!hasProduct) { // 하나도 없으면 브랜드 헤더 삭제
                                        cartItems.removeIf { it is CartItem.BrandHeader && it.brandId == brandId }
                                    }
                                }

                                cartAdapter.notifyDataSetChanged() // 리스트 전체 갱신

                                updateTotalQuantity() // 총 상품 개수 업데이트
                                updateTotalPrice() // 총 가격 업데이트
                                syncAllCheckState() // 전체 선택 체크 상태 동기화
                            }
                        }
                    )
            }

        }
    }

    // 주문하기 버튼 설정
    private fun setupOrderButton() {
        binding.btnOrder.setOnClickListener {
            // 선택된 상품들을 주문 request로 변환
            val items = cartItems.filterIsInstance<CartItem.Product>()
                .filter { it.isChecked } // 체크된 상품들 필터링
                .map { ShoppingCreateOrderRequest.Item(
                    productId = it.productId,
                    quantity = it.quantity
                ) }

            // 1. 주문 등록 API 호출
            shoppingService.createOrder(ShoppingCreateOrderRequest(items))
                .executeWithHandler(
                    context = this,
                    onSuccess = { body ->
                        if (body.success == true) {
                            body.data?.let { data ->
                                Log.d("API_DEBUG", "주문 1. 주문 등록 API 호출 성공")
                                // 2. 결제 요청 API 호출
                                shoppingService.createPayment(data.orderId)
                                    .executeWithHandler(
                                        context = this,
                                        onSuccess = { body ->
                                            if (body.success == true) {
                                                body.data?.let { data ->
                                                    Log.d("API_DEBUG", "주문 2. 결제 요청 API 호출 성공")
                                                    // 결제 완료 화면으로 이동
                                                    val intent = Intent(this,
                                                        ShoppingOrderCompleteActivity::class.java)
                                                    intent.putExtra("PAYMENT_ID", data.paymentId) // 결제 고유 식별자 전달
                                                    startActivity(intent)
                                                    finish()
                                                }
                                            }
                                        }
                                    )
                            }
                        }
                    }
                )

        }
    }
}