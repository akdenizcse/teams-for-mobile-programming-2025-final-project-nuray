// network/DiscoverResponse.kt
package com.example.watchlist.network

import com.example.watchlist.model.MovieItem
import com.google.gson.annotations.SerializedName

data class DiscoverResponse(
    val page: Int,
    @SerializedName("total_pages") val total_pages: Int,
    val results: List<MovieItem>
)
