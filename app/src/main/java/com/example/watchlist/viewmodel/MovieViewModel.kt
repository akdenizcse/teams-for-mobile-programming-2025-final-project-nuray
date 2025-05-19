// MovieViewModel.kt
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

    var movies            by mutableStateOf<List<MovieItem>>(emptyList()); private set
    var currentPage       by mutableStateOf(1);                             private set
    var totalPages        by mutableStateOf(1);                             private set

    var selectedGenre     by mutableStateOf<String?>(null)
    var selectedStartYear by mutableStateOf<String?>(null)
    var selectedEndYear   by mutableStateOf<String?>(null)
    var selectedMinRating by mutableStateOf<Double?>(null)
    var selectedMaxRating by mutableStateOf<Double?>(null)
    var selectedSort      by mutableStateOf<String?>("Default")

    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()

    private var favIdsState   by mutableStateOf<Set<String>>(emptySet())
    private var watchIdsState by mutableStateOf<Set<String>>(emptySet())

    val favIds   get() = favIdsState
    val watchIds get() = watchIdsState

    private val sortMap = mapOf(
        "Default"           to null,
        "By IMDb Rating"    to "vote_average.desc",
        "By Release Date"   to "primary_release_date.desc"
    )

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    init {
        loadPage(1, null)
        subscribeToFavorites()
        subscribeToWatchlist()
    }

    fun applyFilters(
        query: String?,
        genre: String? = null,
        yearFrom: String? = null,
        yearTo: String? = null,
        ratingMin: Double? = null,
        ratingMax: Double? = null
    ) {
        selectedGenre     = genre
        selectedStartYear = yearFrom
        selectedEndYear   = yearTo
        selectedMinRating = ratingMin
        selectedMaxRating = ratingMax
        currentPage       = 1
        loadPage(1, query)
    }

    private fun loadPage(page: Int, query: String?) {
        viewModelScope.launch {
            try {
                val resp = if (query.isNullOrBlank()) {
                    RetrofitClient.api.discoverMovies(
                        apiKey           = API_KEY,
                        genreIds         = selectedGenre?.let { genreMap[it] },
                        releaseDateGte   = selectedStartYear?.let { "$it-01-01" },
                        releaseDateLte   = selectedEndYear?.let { "$it-12-31" },
                        voteAverageGte   = selectedMinRating,
                        voteAverageLte   = selectedMaxRating,
                        sortBy           = sortMap[selectedSort],
                        page             = page
                    )
                } else {
                    RetrofitClient.api.searchMovies(
                        apiKey = API_KEY,
                        query  = query,
                        page   = page
                    )
                }

                var list = resp.results


                when (selectedSort) {
                    "By IMDb Rating" ->
                        list = list.sortedByDescending { it.rating }
                    "By Release Date" ->
                        list = list.sortedByDescending {
                            runCatching {
                                dateFormatter.parse(it.releaseDate) ?: Date(0)
                            }.getOrNull() ?: Date(0)
                        }
                }

                movies      = list
                currentPage = resp.page
                totalPages  = resp.total_pages
            } catch (_: Exception) {

            }
        }
    }

    fun nextPage() {
        if (currentPage < totalPages) loadPage(currentPage + 1, null)
    }

    fun prevPage() {
        if (currentPage > 1) loadPage(currentPage - 1, null)
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

    fun isMovieFavorite(movieId: String)    = favIds.contains(movieId)
    fun isMovieInWatchlist(movieId: String) = watchIds.contains(movieId)

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

    private companion object {
        const val API_KEY = "8a0e3d26a508195b8b070d519d3ad671"
        val genreMap = mapOf(
            "Action" to "28",
            "Drama"  to "18",
            "Comedy" to "35",
            "Sci-Fi" to "878"
        )
    }
}
