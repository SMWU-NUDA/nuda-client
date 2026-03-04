package com.nuda.nudaclient.presentation.navigation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nuda.nudaclient.R
import com.nuda.nudaclient.data.remote.dto.common.Product
import com.nuda.nudaclient.databinding.ItemWishProductBinding

class HomeKeywordRankingAdapter(
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<HomeKeywordRankingAdapter.KeywordRankingViewHolder>() {

    private val products = mutableListOf<Product>()

    // 랭킹 조회 후 응답 저장
    fun submitList(newItems: List<Product>) {
        products.clear() // 전체 리스트 제거
        products.addAll(newItems) // 새로운 리스트로 교체
        notifyDataSetChanged() // 리스트 전체 갱신
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): KeywordRankingViewHolder {
        val binding = ItemWishProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false)

        return KeywordRankingViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: KeywordRankingViewHolder,
        position: Int
    ) {
        val product = products[position]

        holder.bind(product)
        holder.productCard.setOnClickListener {
            onItemClick(product.productId)
        }
    }

    override fun getItemCount(): Int {
        return products.size
    }

    inner class KeywordRankingViewHolder(
        private val binding: ItemWishProductBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        val productCard = binding.root

        fun bind(product: Product) {
            binding.tvBrand.text = product.brandName
            binding.tvProductName.text = product.productName
            binding.tvRatingAndReview.text = "${product.averageRating}(${product.reviewCount})"

            // 상품 이미지 : URL 문자열 이미지로 로드 및 업데이트
            Glide.with(binding.root.context)
                .load(product.thumbnailImg)
                .placeholder(R.drawable.image_product2)
                .error(R.drawable.image_product)
                .centerCrop()
                .into(binding.ivProduct)
        }

    }

}