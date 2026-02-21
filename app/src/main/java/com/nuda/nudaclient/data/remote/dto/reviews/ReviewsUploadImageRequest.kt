package com.nuda.nudaclient.data.remote.dto.reviews

data class ReviewsUploadImageRequest(
    val type: String = "REVIEW",
    val files: List<File>
) {
    data class File(
        val fileName: String,
        val contentType: String
    )
}