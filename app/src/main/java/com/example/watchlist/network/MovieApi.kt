// MovieApi.kt
package com.example.watchlist.network


import com.example.watchlist.model.DiscoverResponse
import com.example.watchlist.model.MovieDetailResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MovieApi {
    @GET("discover/movie")
    suspend fun discoverMovies(
        @Query("api_key") apiKey: String,
        @Query("with_genres") genreIds: String?,
        @Query("primary_release_date.gte") releaseDateGte: String?,
        @Query("primary_release_date.lte") releaseDateLte: String?,
        @Query("vote_average.gte") voteAverageGte: Double?,
        @Query("vote_average.lte") voteAverageLte: Double?,
        @Query("sort_by") sortBy: String?,
        @Query("page") page: Int

    ): DiscoverResponse

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("api_key") apiKey: String,
        @Query("query") query: String,
        @Query("page") page: Int
    ): DiscoverResponse

    @GET("movie/{id}")
    suspend fun getMovieDetails(
        @Path("id") id: Int,
        @Query("api_key") apiKey: String
    ): MovieDetailResponse
}
