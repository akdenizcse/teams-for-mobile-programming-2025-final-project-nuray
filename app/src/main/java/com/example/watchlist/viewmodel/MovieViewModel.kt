package com.example.watchlist.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.watchlist.model.MovieItem
import com.example.watchlist.network.RetrofitClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MovieViewModel : ViewModel() {

    var movies by mutableStateOf<List<MovieItem>>(emptyList()); private set
    var currentPage by mutableStateOf(1); private set
    var totalPages by mutableStateOf(1); private set
    var selectedSort by mutableStateOf<String>("Default")

    private var filterQuery: String? = null
    private var filterGenres: Set<String>? = null
    private var filterStartYear: String? = null
    private var filterEndYear: String? = null
    private var filterMinVote: Double? = null
    private var filterMaxVote: Double? = null

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private var favIdsState by mutableStateOf<Set<String>>(emptySet())
    private var watchIdsState by mutableStateOf<Set<String>>(emptySet())

    init {
        subscribeToFavorites()
        subscribeToWatchlist()
    }

    fun applyFilters(
        query: String? = null,
        genres: Set<String>? = null,
        startYear: String? = null,
        endYear: String? = null,
        minVote: Double? = null,
        maxVote: Double? = null
    ) {
        filterQuery = query
        filterGenres = genres
        filterStartYear = startYear
        filterEndYear = endYear
        filterMinVote = minVote
        filterMaxVote = maxVote
        loadPage(1)
    }

    fun nextPage() {
        if (currentPage < totalPages) loadPage(currentPage + 1)
    }

    fun prevPage() {
        if (currentPage > 1) loadPage(currentPage - 1)
    }

    private fun loadPage(page: Int) {
        viewModelScope.launch {
            try {
                val response = if (filterQuery.isNullOrBlank()) {
                    RetrofitClient.api.discoverMovies(
                        apiKey = API_KEY,
                        genreIds = filterGenres?.joinToString(",") { genreMap[it] ?: "" },
                        releaseDateGte = filterStartYear?.let { "$it-01-01" },
                        releaseDateLte = filterEndYear?.let { "$it-12-31" },
                        voteAverageGte = filterMinVote,
                        voteAverageLte = filterMaxVote,
                        sortBy = sortMap[selectedSort],
                        page = page
                    )
                } else {
                    RetrofitClient.api.searchMovies(
                        apiKey = API_KEY,
                        query = filterQuery!!,
                        page = page
                    )
                }
                var list = response.results
                if (selectedSort == "By IMDb Rating") {
                    list = list.sortedByDescending { it.rating }
                }
                if (selectedSort == "By Release Date") {
                    list = list.sortedByDescending {
                        runCatching {
                            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it.releaseDate)
                                ?: Date(0)
                        }.getOrNull() ?: Date(0)
                    }
                }
                movies = list
                currentPage = response.page
                totalPages = response.total_pages
            } catch (_: Exception) { }
        }
    }

    fun isMovieFavorite(movieId: String) = favIdsState.contains(movieId)
    fun isMovieInWatchlist(movieId: String) = watchIdsState.contains(movieId)

    fun toggleFavorite(movieId: String, makeFav: Boolean) {
        auth.currentUser?.uid?.let { uid ->
            val ref = db.collection("users").document(uid).collection("favorites").document(movieId)
            viewModelScope.launch {
                if (makeFav) ref.set(mapOf("movieId" to movieId)) else ref.delete()
            }
        }
    }

    fun toggleWatchlist(movieId: String, add: Boolean) {
        auth.currentUser?.uid?.let { uid ->
            val ref = db.collection("users").document(uid).collection("watchlist").document(movieId)
            viewModelScope.launch {
                if (add) ref.set(mapOf("movieId" to movieId)) else ref.delete()
            }
        }
    }

    private fun subscribeToFavorites() {
        auth.currentUser?.uid?.let { uid ->
            db.collection("users").document(uid).collection("favorites")
                .addSnapshotListener { snap, _ ->
                    favIdsState = snap?.documents?.map { it.id }?.toSet() ?: emptySet()
                }
        }
    }

    private fun subscribeToWatchlist() {
        auth.currentUser?.uid?.let { uid ->
            db.collection("users").document(uid).collection("watchlist")
                .addSnapshotListener { snap, _ ->
                    watchIdsState = snap?.documents?.map { it.id }?.toSet() ?: emptySet()
                }
        }
    }

    companion object {
        const val API_KEY = "8a0e3d26a508195b8b070d519d3ad671"
        val genreMap = mapOf(
            "Action" to "28",
            "Drama" to "18",
            "Comedy" to "35",
            "Sci-Fi" to "878"
        )
        val sortMap = mapOf(
            "Default" to null,
            "By IMDb Rating" to "vote_average.desc",
            "By Release Date" to "primary_release_date.desc"
        )
    }
}
