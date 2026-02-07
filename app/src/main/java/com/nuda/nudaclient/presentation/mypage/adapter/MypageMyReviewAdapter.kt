package com.nuda.nudaclient.presentation.mypage.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nuda.nudaclient.R
import com.nuda.nudaclient.data.remote.dto.reviews.ReviewsGetMyReviewsResponse
import com.nuda.nudaclient.databinding.ItemMyReviewCardBinding

class MypageMyReviewAdapter
    : RecyclerView.Adapter<MypageMyReviewAdapter.MyReviewViewholder>() {

        // 어댑터 내부에서 리뷰 데이터를 저장하는 리스트 생성
        private val content = mutableListOf<ReviewsGetMyReviewsResponse.Content>()

        // 삭제 버튼 클릭 리스너 인터페이스
        var onDeleteClickListner: ((reviewId: Int, position: Int) -> Unit)? = null


        // inner 클래스로 뷰 홀더 정의. ViewHolder는 리뷰 카드 1개를 표시하는 틀 
        inner class MyReviewViewholder(binding: ItemMyReviewCardBinding) :
            RecyclerView.ViewHolder(binding.root) {

            // 리뷰 카드 아이템 뷰를 변수에 저장
            val ratingBar = binding.ratingBar
            val tv_date = binding.tvDate
            val btn_deleteReview = binding.btnDeleteReview
            val iv_productImage = binding.ivProductImage
            val tv_Brand = binding.tvBrand
            val tv_productName = binding.tvProductName
            val tv_reviewText = binding.tvReviewText
        }

        // 뷰 홀더를 만드는 함수
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): MyReviewViewholder { // inner 클래스로 정의한 뷰 홀더를 객체로 반환
            // item_my_review_card.xml을 inflate (XML -> View 객체로 반환)
            val binding: ItemMyReviewCardBinding = ItemMyReviewCardBinding.inflate(
                LayoutInflater.from(parent.context), parent, false)
            return MyReviewViewholder(binding)
        }

        // 만든 뷰 홀더와 데이터를 묶어주는 함수
        // 스크롤할 때마다 계속 호출
        override fun onBindViewHolder(
            holder: MyReviewViewholder,
            position: Int
        ) {
            Log.d("ADAPTER_DEBUG", "onBindViewHolder 호출 - position: $position")

            // 리뷰 content 리스트에서 리뷰를 한 개 가져옴
            val reviewContent = content[position]

            Log.d("ADAPTER_DEBUG", "리뷰 이름: ${reviewContent.productName}")

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

            // 삭제 버튼 클릭 이벤트 설정
            holder.btn_deleteReview.setOnClickListener {
                // 어댑터는 클릭했다는 사실만 액티비티에 알림 (삭제 처리x)
                onDeleteClickListner?.invoke(reviewContent.reviewId, position)
            }

        }

        override fun getItemCount(): Int {
            return content.size
        }

        // 새로운 아이템 추가 함수 (무한 스크롤용)
        fun addItems(newItems: List<ReviewsGetMyReviewsResponse.Content>) {
            Log.d("ADAPTER_DEBUG", "==== addItems 호출 ====")
            Log.d("ADAPTER_DEBUG", "추가 전 아이템 개수: ${content.size}")
            Log.d("ADAPTER_DEBUG", "새로 추가할 아이템 개수: ${newItems.size}")

            val startPositions = content.size // 현재 리스트 크기 저장
            content.addAll(newItems) // 새 데이터 추가

            Log.d("ADAPTER_DEBUG", "추가 후 아이템 개수: ${content.size}")
            Log.d("ADAPTER_DEBUG", "notifyItemRangeInserted($startPositions, ${newItems.size})")
            Log.d("ADAPTER_DEBUG", "====================")

            notifyItemRangeInserted(startPositions, newItems.size) // 리사이클러뷰에 데이터 변경을 알리는 함수
        }

        // 아이템 삭제 함수 (삭제 버튼용)
        fun removeItem(position: Int) {
            content.removeAt(position)
            notifyItemRemoved(position)
        }
    }
