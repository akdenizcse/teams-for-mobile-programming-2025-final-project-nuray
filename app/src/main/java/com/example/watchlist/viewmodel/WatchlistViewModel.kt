package com.example.watchlist.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.watchlist.model.MovieItem
import com.example.watchlist.network.RetrofitClient
import com.example.watchlist.model.MovieDetailResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

class WatchlistViewModel : ViewModel() {
    var watchlistMovies by mutableStateOf<List<MovieItem>>(emptyList()); private set
    var isLoading       by mutableStateOf(false);                 private set

    private val auth   = FirebaseAuth.getInstance()
    private val db     = FirebaseFirestore.getInstance()
    private val apiKey = "8a0e3d26a508195b8b070d519d3ad671"

    init {
        auth.currentUser?.uid?.let { uid ->
            subscribeToWatchlist(uid)
        }
    }

    private fun subscribeToWatchlist(uid: String) {
        isLoading = true
        db.collection("users")
            .document(uid)
            .collection("watchlist")
            .addSnapshotListener { snap, error ->
                if (error != null) {
                    Log.e("WatchVM","listener error",error)
                    isLoading = false
                    return@addSnapshotListener
                }
                val ids = snap?.documents?.mapNotNull { it.id.toIntOrNull() } ?: emptyList()
                fetchDetails(ids)
            }
    }

    private fun fetchDetails(ids: List<Int>) {
        viewModelScope.launch {
            isLoading = true
            val deferred = ids.map { id ->
                async {
                    try {
                        RetrofitClient.api
                            .getMovieDetails(id, apiKey)
                            .toMovieItem()
                    } catch (e: Exception) {
                        Log.e("WatchVM","detail fail for $id", e)
                        null
                    }
                }
            }
            watchlistMovies = deferred.awaitAll().filterNotNull()
            isLoading = false
        }
    }
}