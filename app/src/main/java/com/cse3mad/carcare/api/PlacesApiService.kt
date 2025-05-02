package com.cse3mad.carcare.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface PlacesApiService {
    @GET("nearbysearch/json")
    fun getNearbyPlaces(
        @Query("location") location: String,
        @Query("radius") radius: Int,
        @Query("type") type: String,
        @Query("key") key: String
    ): Call<PlacesResponse>
}

data class PlacesResponse(
    val results: List<Place>,
    val status: String
)

data class Place(
    val name: String,
    val vicinity: String,
    val geometry: Geometry,
    val rating: Float?,
    val place_id: String,
    val opening_hours: OpeningHours?,
    val business_status: String?
)

data class Geometry(
    val location: Location
)

data class Location(
    val lat: Double,
    val lng: Double
)

data class OpeningHours(
    val open_now: Boolean
) 