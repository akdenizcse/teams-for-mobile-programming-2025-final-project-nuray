package com.example.watchlist.model


import com.google.gson.annotations.SerializedName

data class MovieDetailResponse(
    @SerializedName("id")           val id: Int,
    @SerializedName("title")        val title: String,
    @SerializedName("release_date") val releaseDate: String,
    @SerializedName("poster_path")  val posterUrl: String,
    @SerializedName("genres")       val genres: List<Genre>?,
    @SerializedName("vote_average") val rating: Double
) {
    fun toMovieItem(): MovieItem = MovieItem(
        id          = id,
        title       = title,
        releaseDate = releaseDate,
        posterUrl   = posterUrl,
        genreIds    = genres?.map { it.id } ?: emptyList(),
        rating      = rating
    )

    data class Genre(
        val id: Int,
        val name: String
    )
}
