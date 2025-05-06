package com.example.watchlist.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.watchlist.model.MovieItem
import com.example.watchlist.network.RetrofitClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MovieViewModel : ViewModel() {

    var movies = mutableStateOf<List<MovieItem>>(emptyList())
    var searchQuery = mutableStateOf("")
    var error = mutableStateOf<String?>(null)

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    init {
        getPopularMovies()
    }

    fun updateSearchQuery(query: String) {
        searchQuery.value = query
        if (query.isBlank()) getPopularMovies() else searchMovie(query)
    }

    private fun getPopularMovies() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.getPopularMovies("8a0e3d26a508195b8b070d519d3ad671")
                movies.value = response.results
                error.value = null
            } catch (e: Exception) {
                error.value = e.localizedMessage
            }
        }
    }

    private fun searchMovie(title: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.searchMovies("8a0e3d26a508195b8b070d519d3ad671", title)
                movies.value = response.results
                error.value = null
            } catch (e: Exception) {
                error.value = e.localizedMessage
            }
        }
    }

    fun toggleFavorite(movie: MovieItem, isFavorite: Boolean) {
        val userId = auth.currentUser?.uid ?: return
        val docRef = firestore.collection("users")
            .document(userId)
            .collection("favorites")
            .document(movie.id.toString())

        if (isFavorite) {
            docRef.set(movie)
        } else {
            docRef.delete()
        }
    }

    fun toggleWatchlist(movie: MovieItem, isInWatchlist: Boolean) {
        val userId = auth.currentUser?.uid ?: return
        val docRef = firestore.collection("users")
            .document(userId)
            .collection("watchlist")
            .document(movie.id.toString())

        if (isInWatchlist) {
            docRef.set(movie)
        } else {
            docRef.delete()
        }
    }

    suspend fun isMovieFavorite(movieId: Int): Boolean {
        val userId = auth.currentUser?.uid ?: return false
        val doc = firestore.collection("users")
            .document(userId)
            .collection("favorites")
            .document(movieId.toString())
            .get()
            .await()
        return doc.exists()
    }

    suspend fun isMovieInWatchlist(movieId: Int): Boolean {
        val userId = auth.currentUser?.uid ?: return false
        val doc = firestore.collection("users")
            .document(userId)
            .collection("watchlist")
            .document(movieId.toString())
            .get()
            .await()
        return doc.exists()
    }
}
