package com.nuda.nudaclient.data.remote.dto.reviews

import android.net.Uri

data class UriWithFile (
    val uri: Uri,
    val file: ReviewsUploadImageRequest.File
)