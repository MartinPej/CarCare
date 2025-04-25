package com.cse3mad.carcare.data

data class GoogleSearchResponse(
    val items: List<SearchItem>? = null
)

data class SearchItem(
    val link: String,
    val image: ImageInfo
)

data class ImageInfo(
    val thumbnailLink: String,
    val contextLink: String
) 