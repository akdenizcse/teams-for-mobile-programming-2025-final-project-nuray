// viewmodel/MovieViewModel.kt
package com.example.watchlist.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.watchlist.model.MovieItem
import com.example.watchlist.network.RetrofitClient
import kotlinx.coroutines.launch

class MovieViewModel : ViewModel() {
    var movies             by mutableStateOf<List<MovieItem>>(emptyList()); private set
    var searchQuery        by mutableStateOf("");                                 private set
    var currentPage        by mutableStateOf(1);                                  private set
    var totalPages         by mutableStateOf(1);                                  private set

    var selectedGenre      by mutableStateOf<String?>(null)
    var selectedStartYear  by mutableStateOf<String?>(null)
    var selectedEndYear    by mutableStateOf<String?>(null)
    var selectedMinRating  by mutableStateOf<Double?>(null)
    var selectedMaxRating  by mutableStateOf<Double?>(null)

    private val apiKey = "8a0e3d26a508195b8b070d519d3ad671"
    private val genreMap = mapOf(
        "Action" to "28",
        "Drama"  to "18",
        "Comedy" to "35",
        "Sci-Fi" to "878"
    )

    init {
        loadPage(1)
    }

    fun searchMovies(query: String) {
        searchQuery = query
        loadPage(1, search = query.ifBlank { null })
    }

    fun applyFilters() {
        loadPage(1, search = searchQuery.ifBlank { null })
    }

    fun nextPage() {
        if (currentPage < totalPages) loadPage(currentPage + 1, search = searchQuery.ifBlank { null })
    }

    fun prevPage() {
        if (currentPage > 1) loadPage(currentPage - 1, search = searchQuery.ifBlank { null })
    }

    private fun loadPage(page: Int, search: String? = null) {
        viewModelScope.launch {
            try {
                val resp = if (search.isNullOrBlank()) {
                    RetrofitClient.api.discoverMovies(
                        apiKey           = apiKey,
                        genreIds         = selectedGenre?.let { genreMap[it] },
                        releaseDateGte   = selectedStartYear?.let { "$it-01-01" },
                        releaseDateLte   = selectedEndYear  ?.let { "$it-12-31" },
                        voteAverageGte   = selectedMinRating,
                        voteAverageLte   = selectedMaxRating,
                        page             = page
                    )
                } else {
                    RetrofitClient.api.searchMovies(
                        apiKey = apiKey,
                        query  = search,
                        page   = page
                    )
                }
                movies      = resp.results
                currentPage = resp.page
                totalPages  = resp.total_pages
            } catch (_: Exception) { }
        }
    }

    fun isMovieFavorite(movieId: String) = false
    fun isMovieInWatchlist(movieId: String) = false
    fun toggleFavorite(movie: MovieItem, fav: Boolean) { }
    fun toggleWatchlist(movie: MovieItem, watch: Boolean) { }
}
