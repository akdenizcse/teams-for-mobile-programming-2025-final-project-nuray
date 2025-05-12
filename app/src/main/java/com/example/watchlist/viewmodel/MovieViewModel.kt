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

class MovieViewModel : ViewModel() {

    var movies      by mutableStateOf<List<MovieItem>>(emptyList());   private set
    var currentPage by mutableStateOf(1);                              private set
    var totalPages  by mutableStateOf(1);                              private set

    var selectedGenre     by mutableStateOf<String?>(null)
    var selectedStartYear by mutableStateOf<String?>(null)
    var selectedEndYear   by mutableStateOf<String?>(null)
    var selectedMinRating by mutableStateOf<Double?>(null)
    var selectedMaxRating by mutableStateOf<Double?>(null)

    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()

    // artık Compose tarafından izlenebilen state’ler
    private var favIdsState   by mutableStateOf<Set<String>>(emptySet())
    private var watchIdsState by mutableStateOf<Set<String>>(emptySet())

    val favIds: Set<String>   get() = favIdsState
    val watchIds: Set<String> get() = watchIdsState

    init {
        loadPage(1)
        subscribeToFavorites()
        subscribeToWatchlist()
    }

    private fun loadPage(page: Int, search: String? = null) {
        viewModelScope.launch {
            try {
                val resp = if (search.isNullOrBlank()) {
                    RetrofitClient.api.discoverMovies(
                        apiKey           = API_KEY,
                        genreIds         = selectedGenre?.let { genreMap[it] },
                        releaseDateGte   = selectedStartYear?.let { "$it-01-01" },
                        releaseDateLte   = selectedEndYear?.let { "$it-12-31" },
                        voteAverageGte   = selectedMinRating,
                        voteAverageLte   = selectedMaxRating,
                        page             = page
                    )
                } else {
                    RetrofitClient.api.searchMovies(
                        apiKey = API_KEY,
                        query  = search,
                        page   = page
                    )
                }
                movies      = resp.results
                currentPage = resp.page
                totalPages  = resp.total_pages
            } catch (_: Exception) { /* hata yoksay */ }
        }
    }

    fun searchMovies(query: String) = loadPage(1, search = query.ifBlank { null })
    fun applyFilters()               = loadPage(1)
    fun nextPage() {
        if (currentPage < totalPages) loadPage(currentPage + 1)
    }
    fun prevPage() {
        if (currentPage > 1) loadPage(currentPage - 1)
    }

    private fun subscribeToFavorites() {
        auth.currentUser?.uid?.let { uid ->
            db.collection("users")
                .document(uid)
                .collection("favorites")
                .addSnapshotListener { snap, _ ->
                    val newSet = snap?.documents?.map { it.id }?.toSet() ?: emptySet()
                    favIdsState = newSet
                }
        }
    }

    private fun subscribeToWatchlist() {
        auth.currentUser?.uid?.let { uid ->
            db.collection("users")
                .document(uid)
                .collection("watchlist")
                .addSnapshotListener { snap, _ ->
                    val newSet = snap?.documents?.map { it.id }?.toSet() ?: emptySet()
                    watchIdsState = newSet
                }
        }
    }

    fun isMovieFavorite(movieId: String): Boolean    = favIds.contains(movieId)
    fun isMovieInWatchlist(movieId: String): Boolean = watchIds.contains(movieId)

    fun toggleFavorite(movieId: String, makeFav: Boolean) {
        auth.currentUser?.uid?.let { uid ->
            val ref = db.collection("users")
                .document(uid)
                .collection("favorites")
                .document(movieId)
            viewModelScope.launch {
                if (makeFav) ref.set(mapOf("movieId" to movieId))
                else         ref.delete()
            }
        }
    }

    fun toggleWatchlist(movieId: String, add: Boolean) {
        auth.currentUser?.uid?.let { uid ->
            val ref = db.collection("users")
                .document(uid)
                .collection("watchlist")
                .document(movieId)
            viewModelScope.launch {
                if (add) ref.set(mapOf("movieId" to movieId))
                else     ref.delete()
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
