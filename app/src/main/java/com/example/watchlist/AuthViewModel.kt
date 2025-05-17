package com.example.watchlist

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AuthViewModel : ViewModel() {
    private val auth       = FirebaseAuth.getInstance()
    private val firestore  = FirebaseFirestore.getInstance()

    fun registerUser(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener
                val userDoc = mapOf(
                    "firstName"       to firstName,
                    "lastName"        to lastName,
                    "fullName"        to "$firstName $lastName",
                    "email"           to email,
                    "preferredGenres" to listOf<String>()
                )
                firestore.collection("users")
                    .document(uid)
                    .set(userDoc)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { e -> onError(e.message ?: "Error saving user data") }
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Unknown error")
            }
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
        val favDoc = firestore.collection("users")
            .document(userId)
            .collection("favorites")
            .document(movieId)
        if (isFavorite) favDoc.set(mapOf("movieId" to movieId))
        else favDoc.delete()
    }

    fun toggleWatchlist(movieId: String, isInWatchlist: Boolean) {
        val userId = auth.currentUser?.uid ?: return
        val watchDoc = firestore.collection("users")
            .document(userId)
            .collection("watchlist")
            .document(movieId)
        if (isInWatchlist) watchDoc.set(mapOf("movieId" to movieId))
        else watchDoc.delete()
    }

    fun signOut() {
        auth.signOut()
    }
}
