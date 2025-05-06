// MovieResponse.kt
package com.example.watchlist.model

import com.google.gson.annotations.SerializedName

data class MovieResponse(
    @SerializedName("results") val results: List<MovieItem>
)