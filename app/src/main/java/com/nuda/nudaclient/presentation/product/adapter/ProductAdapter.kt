package com.nuda.nudaclient.presentation.product.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nuda.nudaclient.R
import com.nuda.nudaclient.data.remote.dto.common.Product
import com.nuda.nudaclient.databinding.ItemProductCardBinding
import com.nuda.nudaclient.extensions.toFormattedPrice

class ProductAdapter (
    private val showRank: Boolean = false, // 랭킹 표시(true: 랭킹o, fals: 랭킹x)
    private val onItemClick: (Int, String) -> Unit
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
        val item = items[position]

        holder.bind(item, position, showRank)

        // 상품 카드 클릭 시 상품 상세페이지로 이동하는 이벤트 처리
        holder.productCard.setOnClickListener {
            onItemClick(item.productId, item.thumbnailImg) // 상품 ID 전달
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ProductViewHolder(
        private val binding: ItemProductCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        val productCard = binding.root

        fun bind(item: Product, position: Int, showRank: Boolean) {
            // 순위 설정
            if (showRank) { // 랭킹 있는 목록
                binding.tvRank.visibility = View.VISIBLE
                binding.tvRank.text = (position + 1).toString()
                // guideline을 38dp로
                (binding.guideline.layoutParams as ConstraintLayout.LayoutParams).apply {
                    guideBegin = 38.dpToPx()
                    binding.guideline.requestLayout()
                }
            } else { // 랭킹 없는 목록
                binding.tvRank.visibility = View.GONE
                // guideline을 0dp로 : 상품 이미지가 parent에 붙음
                (binding.guideline.layoutParams as ConstraintLayout.LayoutParams).apply {
                    guideBegin = 0.dpToPx()
                    binding.guideline.requestLayout()
                }
            }

            // 상품 이미지 : URL 문자열 이미지로 로드 및 업데이트
            Glide.with(binding.root.context)
                .load(item.thumbnailImg)
                .placeholder(R.drawable.image_product2)
                .error(R.drawable.image_product)
                .centerCrop()
                .into(binding.ivProduct)

            // 텍스트 바인딩
            binding.tvProductBrand.text = item.brandName
            binding.tvProductName.text = item.productName
            binding.tvRatingAndReview.text = "${item.averageRating}(${item.reviewCount})"
            binding.tvProductPrice.text = item.costPrice.toFormattedPrice()

            // 키워드 바인딩 (최대 3개)
            val keywords = item.ingredientLabels // List<String>
            binding.tvProductIngredient.text = when {
                keywords.isNullOrEmpty() -> "키워드 없음"
                keywords.size <= 3 -> keywords.joinToString(", ")
                else -> keywords.take(3).joinToString(", ") + " ..."
            }

        }

        // dp → px 변환 함수
        private fun Int.dpToPx(): Int {
            return (this * binding.root.context.resources.displayMetrics.density).toInt()
        }
    }


}