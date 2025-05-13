// app/src/main/java/com/example/watchlist/viewmodel/FavoritesViewModel.kt
package com.example.watchlist.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.watchlist.model.MovieItem
import com.example.watchlist.network.RetrofitClient
import com.example.watchlist.network.MovieDetailResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

class FavoritesViewModel : ViewModel() {

    // UI tarafından gözlemlenecek durum
    var favoriteMovies by mutableStateOf<List<MovieItem>>(emptyList()); private set
    var isLoading      by mutableStateOf(false);                private set

    private val auth   = FirebaseAuth.getInstance()
    private val db     = FirebaseFirestore.getInstance()
    private val apiKey = "8a0e3d26a508195b8b070d519d3ad671"

    private val authListener = AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser
        if (user != null) {
            Log.d("FavVM", "AuthStateListener: user logged in, uid=${user.uid}")
            subscribeToFavorites(user.uid)
        } else {
            Log.d("FavVM", "AuthStateListener: user logged out")
            favoriteMovies = emptyList()
        }
    }

    init {
        // Eğer uygulama açıldığında kullanıcı zaten girişli ise hemen abone ol
        auth.currentUser?.uid?.let { uid ->
            Log.d("FavVM", "init: already logged in, uid=$uid")
            subscribeToFavorites(uid)
        }
        // Oturum açma/kapama durumlarını dinle
        auth.addAuthStateListener(authListener)
    }

    override fun onCleared() {
        // Listener'ı kaldır
        auth.removeAuthStateListener(authListener)
        super.onCleared()
    }

    /**
     * Firestore’daki users/{uid}/favorites koleksiyonuna abone olur.
     * Her güncellemede ID listesini alıp detayları çekmek için loadDetails’i çağırır.
     */
    private fun subscribeToFavorites(uid: String) {
        Log.d("FavVM", "subscribeToFavorites(uid=$uid)")
        isLoading = true
        db.collection("users")
            .document(uid)
            .collection("favorites")
            .addSnapshotListener { snap, error ->
                if (error != null) {
                    Log.e("FavVM", "Firestore listener error", error)
                    isLoading = false
                    return@addSnapshotListener
                }
                val ids = snap
                    ?.documents
                    ?.mapNotNull { it.id.toIntOrNull() }
                    ?: emptyList()
                Log.d("FavVM", "Firestore returned IDs: $ids")
                fetchDetails(ids)
            }
    }

    /**
     * Verilen ID listesi için TMDB’den detayları asenkron olarak çeker,
     * favoriteMovies state’ini günceller.
     */
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
                        Log.e("FavVM", "getMovieDetails failed for id=$id", e)
                        null
                    }
                }
            }
            favoriteMovies = deferred.awaitAll().filterNotNull()
            Log.d("FavVM", "Loaded favoriteMovies size=${favoriteMovies.size}")
            isLoading = false
        }
    }
}
