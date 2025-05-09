package com.example.watchlist.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.watchlist.model.MovieItem
import com.example.watchlist.model.MovieResponse
import com.example.watchlist.network.RetrofitClient
import kotlinx.coroutines.launch

class MovieViewModel : ViewModel() {
    var movies by mutableStateOf<List<MovieItem>>(emptyList())
    var searchQuery by mutableStateOf("")
    var currentPage by mutableIntStateOf(1)
    var totalPages by mutableIntStateOf(1)
    var error by mutableStateOf<String?>(null)

    private val apiKey = "8a0e3d26a508195b8b070d519d3ad671"

    init {
        fetchPopular(1)
    }

    private fun fetchPopular(page: Int) {
        viewModelScope.launch {
            try {
                val response: MovieResponse =
                    RetrofitClient.api.getPopularMovies(apiKey = apiKey, page = page)
                movies = response.results
                currentPage = response.page
                totalPages = response.total_pages
                error = null
            } catch (e: Exception) {
                error = e.localizedMessage
            }
        }
    }

    fun nextPage() {
        if (currentPage < totalPages) fetchPopular(currentPage + 1)
    }

    fun prevPage() {
        if (currentPage > 1) fetchPopular(currentPage - 1)
    }

    fun updateSearchQuery(query: String) {
        searchQuery = query
        fetchPopular(1)
    }

    // Placeholder implementations for favorites/watchlist
    fun isMovieFavorite(id: String): Boolean = false
    fun isMovieInWatchlist(id: String): Boolean = false
    fun toggleFavorite(movie: MovieItem, fav: Boolean) {}
    fun toggleWatchlist(movie: MovieItem, watch: Boolean) {}
}
