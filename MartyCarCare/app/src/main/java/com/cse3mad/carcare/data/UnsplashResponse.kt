package com.cse3mad.carcare.data

data class UnsplashResponse(
    val results: List<Photo>? = null
)

data class Photo(
    val urls: PhotoUrls
)

data class PhotoUrls(
    val regular: String
) 