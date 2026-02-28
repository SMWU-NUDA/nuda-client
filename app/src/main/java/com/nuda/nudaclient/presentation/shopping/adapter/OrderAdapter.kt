package com.nuda.nudaclient.presentation.shopping.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nuda.nudaclient.R
import com.nuda.nudaclient.databinding.ItemOrderProductBinding
import com.nuda.nudaclient.extensions.toFormattedPrice
import com.nuda.nudaclient.presentation.shopping.convertData.OrderProduct

class OrderAdapter(
    private val items: MutableList<OrderProduct>
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int): OrderViewHolder {
        val binding = ItemOrderProductBinding.inflate (
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: OrderViewHolder,
        position: Int
    ) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class OrderViewHolder(
        private val binding: ItemOrderProductBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: OrderProduct) {
            binding.tvBrand.text = item.brandName
            binding.tvProductName.text = item.productName
            binding.tvProductCount.text = "${item.quantity}개"
            binding.tvPrice.text = item.totalPrice.toFormattedPrice()

            // 상품 이미지 : URL 문자열 이미지로 로드 및 업데이트
            Glide.with(binding.root.context)
                .load(item.thumbnailImg)
                .placeholder(R.drawable.image_product2)
                .error(R.drawable.image_product)
                .centerCrop()
                .into(binding.ivProduct)
        }
    }
}