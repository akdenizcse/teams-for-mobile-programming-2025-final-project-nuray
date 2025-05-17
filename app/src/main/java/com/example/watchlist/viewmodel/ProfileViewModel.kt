package com.example.watchlist.viewmodel

import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore

class ProfileViewModel : ViewModel() {

    var fullName by mutableStateOf("")
        private set

    var email by mutableStateOf("")
        private set

    var preferredGenres = mutableStateListOf<String>()
        private set

    var isPasswordDialogVisible by mutableStateOf(false)
        private set

    var passwordChangeMessage by mutableStateOf<String?>(null)
        private set

    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()
    private val uid  = auth.currentUser?.uid

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        auth.currentUser?.let { user ->
            email = user.email ?: ""
            uid?.let { id ->
                db.collection("users").document(id).get()
                    .addOnSuccessListener { doc ->
                        val fn = doc.getString("firstName")
                        val ln = doc.getString("lastName")
                        fullName = when {
                            !fn.isNullOrBlank() && !ln.isNullOrBlank() -> "$fn $ln"
                            !fn.isNullOrBlank() -> fn
                            !ln.isNullOrBlank() -> ln
                            !doc.getString("fullName").isNullOrBlank() -> doc.getString("fullName")!!
                            !user.displayName.isNullOrBlank() -> user.displayName!!
                            else -> email
                        }
                        (doc.get("preferredGenres") as? List<*>)?.filterIsInstance<String>()
                            ?.let {
                                preferredGenres.clear()
                                preferredGenres.addAll(it)
                            }
                    }
                    .addOnFailureListener {
                        Log.e("ProfileVM", "load profile failed", it)
                        fullName = email
                    }
            }
        }
    }

    fun savePreferredGenres(selected: List<String>) {
        uid?.let { id ->
            db.collection("users").document(id)
                .update("preferredGenres", selected)
                .addOnSuccessListener {
                    preferredGenres.clear()
                    preferredGenres.addAll(selected)
                }
        }
    }

    fun showPasswordDialog()       { isPasswordDialogVisible = true }
    fun hidePasswordDialog() {
        isPasswordDialogVisible = false
        passwordChangeMessage   = null
    }

    fun changePassword(current: String, newPass: String, confirmPass: String) {
        passwordChangeMessage = null
        if (newPass.length < 6) {
            passwordChangeMessage = "New password must be at least 6 characters"
            return
        }
        if (newPass != confirmPass) {
            passwordChangeMessage = "New passwords do not match"
            return
        }
        val user = auth.currentUser ?: run {
            passwordChangeMessage = "No user"
            return
        }
        val credential = EmailAuthProvider.getCredential(user.email!!, current)
        user.reauthenticate(credential)
            .addOnSuccessListener {
                user.updatePassword(newPass)
                    .addOnSuccessListener {
                        passwordChangeMessage = "Password changed successfully"
                        isPasswordDialogVisible = false
                    }
                    .addOnFailureListener { e ->
                        passwordChangeMessage = e.message ?: "Error updating password"
                    }
            }
            .addOnFailureListener {
                passwordChangeMessage = "Current password is incorrect"
            }
    }

    fun signOut() {
        auth.signOut()
    }
}
