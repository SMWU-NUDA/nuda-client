package com.nuda.nudaclient.presentation.navigation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nuda.nudaclient.R
import com.nuda.nudaclient.data.remote.dto.common.Product
import com.nuda.nudaclient.databinding.ItemHomeRankingCardBinding

// ViewPager2 어댑터
class HomeRankingAdapter(
    private val products: List<Product>
) : RecyclerView.Adapter<HomeRankingAdapter.RankingViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RankingViewHolder {
        val binding = ItemHomeRankingCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RankingViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: RankingViewHolder,
        position: Int
    ) {
        holder.bind(products[position], position)
    }

    override fun getItemCount(): Int {
        return products.size
    }

    inner class RankingViewHolder(
        private val binding: ItemHomeRankingCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(product: Product, position: Int) {
            // 랭킹 숫자 이미지 (1~10)
            val rankDrawable = when (position + 1) {
                1 -> R.drawable.img_number_1
                2 -> R.drawable.img_number_2
                3 -> R.drawable.img_number_3
                4 -> R.drawable.img_number_4
                5 -> R.drawable.img_number_5
                6 -> R.drawable.img_number_6
                7 -> R.drawable.img_number_7
                8 -> R.drawable.img_number_8
                9 -> R.drawable.img_number_9
                10 -> R.drawable.img_number_10
                else -> R.drawable.img_number_0
            }
            binding.ivRankNumber.setImageResource(rankDrawable)

            // 텍스트 바인딩
            binding.tvBrand.text = product.brandName
            binding.tvProductName.text = product.productName

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