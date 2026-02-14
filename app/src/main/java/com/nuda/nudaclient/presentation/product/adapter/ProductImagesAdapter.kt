package com.nuda.nudaclient.presentation.product.adapter

import android.media.Image
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nuda.nudaclient.R
import com.nuda.nudaclient.databinding.ItemProductDetailImageBinding

class ProductImagesAdapter(
    private val imageList: List<String> // 이미지 리소스 URL
) : RecyclerView.Adapter<ProductImagesAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ImageViewHolder {
        val binding = ItemProductDetailImageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ImageViewHolder,
        position: Int
    ) {
        holder.bind(imageList[position])
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    // 중첩 클래스로 뷰 홀더 정의
    class ImageViewHolder(
        private val binding: ItemProductDetailImageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        // 데이터 바인딩 메소드 (이밎 url 문자열 하나 전달)
        fun bind(imageUrl: String) {
            Glide.with(binding.root.context)
                .load(imageUrl)
                .placeholder(R.drawable.image_product2)
                .error(R.drawable.image_product2)
                .centerCrop()
                .into(binding.ivProduct)
        }
    }


}