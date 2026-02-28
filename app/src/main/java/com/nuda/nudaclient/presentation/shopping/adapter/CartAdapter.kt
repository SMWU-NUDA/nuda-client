package com.nuda.nudaclient.presentation.shopping.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nuda.nudaclient.databinding.ItemCartBrandHeaderBinding
import com.nuda.nudaclient.databinding.ItemCartProductBinding
import com.nuda.nudaclient.extensions.toFormattedPrice
import com.nuda.nudaclient.presentation.shopping.CartItem

class CartAdapter(
    // 체크/수량/삭제 이벤트를 Activity로 올려보내는 콜백들
    private val items: MutableList<CartItem>,
    // 어댑터는 뷰 바인딩만 담당하고 로직은 Activity에서 처리하기 위함
    private val onBrandChecked: (position: Int, isChecked: Boolean) -> Unit,
    private val onProductChecked: (position: Int, isChecked: Boolean) -> Unit,
    private val onQuantityChanged: (cartItemId: Int, delta: Int) -> Unit,
    private val onDeleteClicked: (cartItemId: Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // 뷰 타입 싱글턴
    companion object {
        const val TYPE_BRAND_HEADER = 0
        const val TYPE_PRODUCT = 1
    }

    // position 아이템의 뷰 타입 반환
    override fun getItemViewType(position: Int): Int {
        return when(items[position]) {
            is CartItem.BrandHeader -> TYPE_BRAND_HEADER
            is CartItem.Product -> TYPE_PRODUCT
        }
    }

    // 뷰 홀더 만드는 함수, 뷰 타입에 따라 다른 뷰 홀더 생성
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_BRAND_HEADER -> { // 브랜드 헤더인 경우
                val binding = ItemCartBrandHeaderBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                BrandHeaderViewHolder(binding)
            }
            TYPE_PRODUCT -> { // 상품 아이템인 경우
                val binding = ItemCartProductBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                ProductViewHolder(binding)
            }
            else -> { // 예외 처리
                throw IllegalArgumentException("Invalid view type: $viewType")
            }
        }
    }

    // 뷰 홀더 타입에 맞는 bind 함수 호출
    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
       when (holder) {
           is BrandHeaderViewHolder -> holder.bind(items[position] as CartItem.BrandHeader, position)
           is ProductViewHolder -> holder.bind(items[position] as CartItem.Product, position)
       }
    }

    override fun getItemCount(): Int {
        return items.size
    }


    // 브랜드 헤더 뷰 홀더
    inner class BrandHeaderViewHolder(
        private val binding: ItemCartBrandHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        val cbBrand = binding.cbBrand

        // 바인딩 메소드
        fun bind(brandItem: CartItem.BrandHeader, position: Int) {
            // 브랜드 이름
            binding.tvBrand.text = brandItem.brandName

            // 브랜드 체크박스
            cbBrand.setOnCheckedChangeListener(null) // 기존 리스너 제거(무한 루프 방지)
            cbBrand.isChecked = brandItem.isChecked // 체크 상태 세팅
            cbBrand.setOnCheckedChangeListener { _, isChecked -> // 체크 변경 감지 리스너, isChecked: 클릭 후의 체크 상태
                onBrandChecked(position, isChecked) // 상태 업데이트
            }
        }
    }

    // 상품 뷰 홀더
    inner class ProductViewHolder(
        private val binding: ItemCartProductBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        val cbProduct = binding.cbProduct

        fun bind(productItem: CartItem.Product, position: Int) {
            // 데이터 바인딩
//            binding.ivProduct
            binding.tvBrand.text = productItem.brandName
            binding.tvProductName.text = productItem.productName
            binding.tvProductCount.text = productItem.quantity.toString()
            binding.tvPrice.text = productItem.price.toFormattedPrice() // 가격 포맷

            // 상품 체크박스
            cbProduct.setOnCheckedChangeListener(null) // 기존 리스너 제거
            cbProduct.isChecked = productItem.isChecked // 체크 상태 세팅
            cbProduct.setOnCheckedChangeListener { _, isChecked -> // 체크 변경 감지 리스너, isChecked: 클릭 후의 체크 상태
                onProductChecked(position, isChecked) // 상태 업데이트
            }

            // 상품 수량 변경
            binding.ivMinus.setOnClickListener { onQuantityChanged(productItem.cartItemId, -1) }
            binding.ivPlus.setOnClickListener { onQuantityChanged(productItem.cartItemId, 1) }

            // 상품 삭제 버튼
            binding.ivDelete.setOnClickListener { onDeleteClicked(productItem.cartItemId) }

            // 구분선 설정
            when {
                position == items.size - 1 -> { // 목록의 마지막 아이템
                    binding.dividerThin.visibility = View.GONE
                    binding.dividerThick.visibility = View.GONE
                }
                items[position + 1] is CartItem.BrandHeader -> { // 브랜드 마지막 상품
                    binding.dividerThin.visibility = View.GONE
                    binding.dividerThick.visibility = View.VISIBLE
                }
                else -> { // 일반 상품
                    binding.dividerThin.visibility = View.VISIBLE
                    binding.dividerThick.visibility = View.GONE
                }
            }
        }
    }
}