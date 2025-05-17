// model/MovieItem.kt
package com.example.watchlist.model

import com.google.gson.annotations.SerializedName

private val GenreMap = mapOf(
    28 to "Action",
    18 to "Drama",
    35 to "Comedy",
    878 to "Sci-Fi"
)

data class MovieItem(
    @SerializedName("id")           val id: Int,
    @SerializedName("title")        val title: String,
    @SerializedName("release_date") val releaseDate: String,
    @SerializedName("poster_path")  val posterUrl: String,
    @SerializedName("genre_ids")    val genreIds: List<Int>,
    @SerializedName("vote_average") val rating: Double
) {
    fun getGenreNames(): String =
        genreIds.mapNotNull { GenreMap[it] }
            .ifEmpty { listOf("N/A") }
            .joinToString(", ")
}
