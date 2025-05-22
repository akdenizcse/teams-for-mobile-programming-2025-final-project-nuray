package com.example.watchlist.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.watchlist.model.MovieDetailResponse
import com.example.watchlist.network.RetrofitClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModel : ViewModel() {
    var watchlistIds by mutableStateOf<List<String>>(emptyList()); private set
    var watchlistGenres by mutableStateOf<Set<String>>(emptySet()); private set
    var avatarUrl by mutableStateOf<String?>(null); private set

    private val firestore = FirebaseFirestore.getInstance()
    private val auth      = FirebaseAuth.getInstance()
    private val api       = RetrofitClient.api
    private val apiKey    = "8a0e3d26a508195b8b070d519d3ad671"

    private val genreMap = mapOf(
        28 to "Action",
        18 to "Drama",
        35 to "Comedy",
        878 to "Sci-Fi"
    )

    init {
        auth.currentUser?.uid?.let { uid ->

            firestore.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { doc ->
                    avatarUrl = doc.getString("avatarUrl")
                }

            viewModelScope.launch {
                val docs = firestore.collection("users")
                    .document(uid)
                    .collection("watchlist")
                    .get()
                    .await()

                watchlistIds = docs.documents.map { it.id }

                val deferredGenres = watchlistIds.map { id ->
                    async {
                        try {
                            val detail: MovieDetailResponse =
                                api.getMovieDetails(id.toInt(), apiKey)
                            detail.genres
                                ?.mapNotNull { genreMap[it.id] }
                                ?: emptyList()
                        } catch (_: Exception) {
                            emptyList()
                        }
                    }
                }

                watchlistGenres = deferredGenres
                    .map { it.await() }
                    .flatten()
                    .toSet()
            }
        }
    }

    fun updateAvatar(newUrl: String, onComplete: () -> Unit = {}) {
        auth.currentUser?.uid?.let { uid ->
            firestore.collection("users")
                .document(uid)
                .update("avatarUrl", newUrl)
                .addOnSuccessListener {
                    avatarUrl = newUrl
                    onComplete()
                }
        }
    }
}
