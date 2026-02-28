package com.nuda.nudaclient.presentation.shopping.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nuda.nudaclient.databinding.ItemOrderProductBinding
import com.nuda.nudaclient.extensions.toFormattedPrice
import com.nuda.nudaclient.presentation.shopping.OrderProduct

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
//            binding.ivProduct =
            binding.tvBrand.text = item.brandName
            binding.tvProductName.text = item.productName
            binding.tvProductCount.text = "${item.quantity}개"
            binding.tvPrice.text = item.totalPrice.toFormattedPrice()
        }
    }
}