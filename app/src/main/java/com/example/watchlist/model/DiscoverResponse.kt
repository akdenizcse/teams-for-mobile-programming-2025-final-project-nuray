
package com.example.watchlist.model


import com.google.gson.annotations.SerializedName

data class DiscoverResponse(
    val page: Int,
    @SerializedName("total_pages") val total_pages: Int,
    val results: List<MovieItem>
)
