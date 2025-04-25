package com.cse3mad.carcare.api

import com.cse3mad.carcare.data.UnsplashResponse
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface UnsplashService {
    @Headers("Authorization: Client-ID YOUR_UNSPLASH_ACCESS_KEY")
    @GET("search/photos")
    suspend fun searchPhotos(
        @Query("query") query: String,
        @Query("per_page") perPage: Int = 1
    ): UnsplashResponse
} 