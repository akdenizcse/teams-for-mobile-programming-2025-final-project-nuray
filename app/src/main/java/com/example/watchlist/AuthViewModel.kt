package com.example.watchlist

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun registerUser(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Unknown error") }
    }

    fun loginUser(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Login failed") }
    }

    fun toggleFavorite(movieId: String, isFavorite: Boolean) {
        val userId = auth.currentUser?.uid ?: return
        val userDoc = firestore.collection("users").document(userId)
        val favoriteDoc = userDoc.collection("favorites").document(movieId)

        if (isFavorite) {
            favoriteDoc.set(mapOf("movieId" to movieId))
        } else {
            favoriteDoc.delete()
        }
    }

    fun toggleWatchlist(movieId: String, isInWatchlist: Boolean) {
        val userId = auth.currentUser?.uid ?: return
        val userDoc = firestore.collection("users").document(userId)
        val watchlistDoc = userDoc.collection("watchlist").document(movieId)

        if (isInWatchlist) {
            watchlistDoc.set(mapOf("movieId" to movieId))
        } else {
            watchlistDoc.delete()
        }
    }
}
