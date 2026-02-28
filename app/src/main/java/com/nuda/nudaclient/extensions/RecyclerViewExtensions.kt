package com.nuda.nudaclient.extensions

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

fun RecyclerView.setInfiniteScrollListener(
    onLoadMore: () -> Unit
) {
    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        // onScrolled(): 스크롤이 발생하면 자동 호출되는 함수
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            // 1. 레이아웃 메니저 가져오기
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager

            // 2. 현재 화면에 보이는 마지막 아이템 위치
            val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()

            // 3. 전체 아이템 개수
            val totalItemCount = layoutManager.itemCount

            // 4. 끝에 가까워졌는지 체크
            if (lastVisiblePosition >= totalItemCount - 3) { // 끝에서 3개 전이면
                onLoadMore()
            }
        }
    })
}