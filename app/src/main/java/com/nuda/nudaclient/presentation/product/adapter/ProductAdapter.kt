package com.nuda.nudaclient.presentation.product.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nuda.nudaclient.data.remote.dto.common.Product
import com.nuda.nudaclient.databinding.ItemProductCardBinding

class ProductAdapter (
    private val showRank: Boolean = false // 랭킹 표시(true: 랭킹o, fals: 랭킹x)
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    private val items = mutableListOf<Product>()

    // 처음 로드 or 필터 변경 시 전체 교체
    fun submitList(newItems: List<Product>) {
        items.clear() // 전체 리스트 제거
        items.addAll(newItems) // 새로운 리스트로 교체
        notifyDataSetChanged() // 리스트 전체 갱신
    }

    // 무한 스크롤 추가 로드 시
    fun appendItems(newItems: List<Product>) {
        val startPosition = items.size
        items.addAll(newItems)
        notifyItemRangeInserted(startPosition, newItems.size)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProductViewHolder {
        val binding = ItemProductCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ProductViewHolder,
        position: Int
    ) {
        holder.bind(items[position], position, showRank)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ProductViewHolder(
        private val binding: ItemProductCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Product, position: Int, showRank: Boolean) {
            if (showRank) { // 랭킹 있는 목록
                binding.tvRank.visibility = View.VISIBLE
                binding.space.visibility = View.VISIBLE
                binding.tvRank.text = (position + 1).toString()
            } else { // 랭킹 없는 목록
                binding.tvRank.visibility = View.GONE
                binding.space.visibility = View.GONE
            }
        }
    }
}