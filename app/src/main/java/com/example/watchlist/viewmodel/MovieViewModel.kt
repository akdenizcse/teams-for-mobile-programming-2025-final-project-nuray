package com.example.watchlist.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.watchlist.model.MovieItem
import com.example.watchlist.model.MovieResponse
import com.example.watchlist.network.RetrofitClient
import kotlinx.coroutines.launch

class MovieViewModel : ViewModel() {
    var movies = mutableStateOf<List<MovieItem>>(emptyList())
    var searchQuery = mutableStateOf("")
    var error = mutableStateOf<String?>(null)

    init {
        getPopularMovies()
    }

    fun updateSearchQuery(query: String) {
        searchQuery.value = query
        if (query.isBlank()) {
            getPopularMovies()
        } else {
            searchMovies(query)
        }
    }

    private fun getPopularMovies() {
        viewModelScope.launch {
            try {
                val response: MovieResponse = RetrofitClient.api.getPopularMovies("8a0e3d26a508195b8b070d519d3ad671")
                movies.value = response.results
                error.value = null
            } catch (e: Exception) {
                error.value = e.localizedMessage
            }
        }
    }

    private fun searchMovies(query: String) {
        viewModelScope.launch {
            try {
                val response: MovieResponse = RetrofitClient.api.searchMovies("8a0e3d26a508195b8b070d519d3ad671", query)
                movies.value = response.results
                error.value = null
            } catch (e: Exception) {
                error.value = e.localizedMessage
            }
        }
    }
}
