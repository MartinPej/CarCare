package com.cse3mad.carcare.api

import com.cse3mad.carcare.data.GoogleSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface GoogleSearchService {
    @GET("customsearch/v1")
    suspend fun searchImages(
        @Query("key") apiKey: String,
        @Query("cx") searchEngineId: String,
        @Query("q") query: String,
        @Query("searchType") searchType: String = "image",
        @Query("num") numResults: Int = 1
    ): GoogleSearchResponse
} 