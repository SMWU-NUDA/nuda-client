package com.nuda.nudaclient.presentation.mypage.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nuda.nudaclient.R
import com.nuda.nudaclient.data.remote.dto.reviews.ReviewsGetMyReviewsResponse
import com.nuda.nudaclient.databinding.ItemMyReviewCardBinding

class MypageMyReviewAdapter(private val content : List<ReviewsGetMyReviewsResponse.Content>)
    : RecyclerView.Adapter<MypageMyReviewAdapter.MyReviewViewholder>() {

    // inner 클래스로 뷰 홀더 정의
    inner class MyReviewViewholder(binding: ItemMyReviewCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // 변수에 뷰 객체 할당
        val ratingBar = binding.ratingBar
        val tv_date = binding.tvDate
        val btn_deleteReview = binding.btnDeleteReview
        val iv_productImage = binding.ivProductImage
        val tv_Brand = binding.tvBrand
        val tv_productName = binding.tvProductName
        val tv_reviewText = binding.tvReviewText

        val root = binding.root
    }

    // 뷰 홀더를 만드는 함수
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyReviewViewholder { // inner 클래스로 정의한 뷰 홀더를 객체로 반환
        val binding: ItemMyReviewCardBinding = ItemMyReviewCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return MyReviewViewholder(binding)
    }

    // 만든 뷰 홀더와 데이터를 묶어주는 함수
    override fun onBindViewHolder(
        holder: MyReviewViewholder,
        position: Int
    ) {
        // 리뷰 content 리스트에서 리뷰를 한 개 가져옴
        val reviewContent = content[position]

        holder.ratingBar.rating = reviewContent.rating.toFloat()
        holder.tv_date.text = reviewContent.createdAt.substringBefore(" (")
        holder.tv_Brand.text = reviewContent.brandName
        holder.tv_productName.text = reviewContent.productName
        holder.tv_reviewText.text = reviewContent.content

        // URL 문자열 이미지로 로드 및 업데이트
        Glide.with(holder.itemView.context)
            .load(reviewContent.productThumbnail)
            .error(R.drawable.image_product) // 이미지 로드 실패 시
            .into(holder.iv_productImage)

    }

    override fun getItemCount(): Int {
        return content.size
    }


}
