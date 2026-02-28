package com.nuda.nudaclient.presentation.shopping.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nuda.nudaclient.R
import com.nuda.nudaclient.databinding.ItemOrderDateHeaderBinding
import com.nuda.nudaclient.databinding.ItemOrderPriceFooterBinding
import com.nuda.nudaclient.databinding.ItemOrderProductBinding
import com.nuda.nudaclient.extensions.toFormattedPrice
import com.nuda.nudaclient.presentation.shopping.convertData.OrderHistoryItem

class OrderHistoryAdapter(
    private val items: MutableList<OrderHistoryItem>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // 뷰 타입 싱글턴
    companion object {
        const val TYPE_DATE_HEADER = 0
        const val TYPE_ORDER_PRODUCT = 1
        const val TYPE_PRICE_FOOTER = 2
    }

    // position 아이템의 뷰 타입 반환
    override fun getItemViewType(position: Int): Int {
        return when(items[position]) {
            is OrderHistoryItem.DateHeader -> TYPE_DATE_HEADER
            is OrderHistoryItem.Product -> TYPE_ORDER_PRODUCT
            is OrderHistoryItem.PriceFooter -> TYPE_PRICE_FOOTER
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_DATE_HEADER -> { // 날짜 헤더인 경우
                val binding = ItemOrderDateHeaderBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                DateHeaderViewHolder(binding)
            }
            TYPE_ORDER_PRODUCT -> { // 상품 아이템인 경우
                val binding = ItemOrderProductBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                OrderProductViewHolder(binding)
            }
            TYPE_PRICE_FOOTER -> { // 총 금액 푸터인 경우
                val binding = ItemOrderPriceFooterBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                PriceFooterViewHolder(binding)
            }
            else -> { // 예외 처리
                throw IllegalArgumentException("Invalid view type: $viewType")
            }
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        when (holder) {
            is DateHeaderViewHolder -> holder.bind(items[position] as OrderHistoryItem.DateHeader)
            is OrderProductViewHolder -> holder.bind(items[position] as OrderHistoryItem.Product)
            is PriceFooterViewHolder -> holder.bind(items[position] as OrderHistoryItem.PriceFooter)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }


    // 날짜 헤더 뷰 홀더
    inner class DateHeaderViewHolder(
        private val binding: ItemOrderDateHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(dateHeader: OrderHistoryItem.DateHeader) {
            binding.tvDate.text = dateHeader.orderDate
            binding.tvOrderNumber.apply {
               text = dateHeader.orderNum.toString()
               paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG // 밑줄 설정
            }
        }
    }

    // 상품 아이템 뷰 홀더
    inner class OrderProductViewHolder(
        private val binding: ItemOrderProductBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(productItem: OrderHistoryItem.Product) {
            binding.tvBrand.text = productItem.brandName
            binding.tvProductName.text = productItem.productName
            binding.tvProductCount.text = "${productItem.quantity}개"
            binding.tvPrice.text = productItem.totalPrice.toFormattedPrice()

            // 상품 이미지 : URL 문자열 이미지로 로드 및 업데이트
            Glide.with(binding.root.context)
                .load(productItem.thumbnailImg)
                .placeholder(R.drawable.image_product2)
                .error(R.drawable.image_product)
                .centerCrop()
                .into(binding.ivProduct)
        }
    }

    // 총 금액 푸터 뷰 홀더
    inner class PriceFooterViewHolder(
        private val binding: ItemOrderPriceFooterBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(priceFooter: OrderHistoryItem.PriceFooter) {
            binding.tvTotalPrice.text = priceFooter.totalAmount.toFormattedPrice()
        }
    }
}