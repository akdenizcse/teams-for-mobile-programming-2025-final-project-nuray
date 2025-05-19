// ProfileScreen.kt
package com.example.watchlist

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.watchlist.viewmodel.ProfileViewModel
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    vm: ProfileViewModel = viewModel()
) {
    val colors = MaterialTheme.colorScheme
    val context = LocalContext.current

    var firstName by remember { mutableStateOf("") }
    var lastName  by remember { mutableStateOf("") }
    val user      = FirebaseAuth.getInstance().currentUser
    val email     = user?.email.orEmpty()

    var showResetDialog by remember { mutableStateOf(false) }
    var oldPwd by remember { mutableStateOf("") }
    var newPwd by remember { mutableStateOf("") }
    var confirmPwd by remember { mutableStateOf("") }
    var err by remember { mutableStateOf<String?>(null) }

    val genres = vm.watchlistGenres.toList()

    LaunchedEffect(Unit) {
        user?.uid?.let { uid ->
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { doc ->
                    firstName = doc.getString("firstName").orEmpty()
                    lastName  = doc.getString("lastName").orEmpty()
                }
        }
    }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Profile", color = colors.onBackground) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = colors.primary)
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = colors.background
                )
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .background(colors.background)
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Box(
                Modifier
                    .size(100.dp)
                    .align(Alignment.CenterHorizontally)
                    .clip(CircleShape)
                    .background(colors.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.AccountCircle,
                    contentDescription = "Avatar",
                    tint = colors.onSecondaryContainer,
                    modifier = Modifier.size(64.dp)
                )
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("ABOUT", fontSize = 18.sp, color = colors.onSurface, fontWeight = FontWeight.Bold)
                    Text("$firstName $lastName", color = colors.onSurface, fontSize = 16.sp)
                    Text(email, color = colors.onSurface, fontSize = 16.sp)
                    Divider(color = colors.outline)
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { showResetDialog = true }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.LockReset, contentDescription = "Reset", tint = colors.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("Change Password", color = colors.onSurface, fontSize = 16.sp)
                    }
                    err?.let { Text(it, color = colors.error, fontSize = 14.sp) }
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("PREFERRED GENRES", fontSize = 18.sp, color = colors.onSurface, fontWeight = FontWeight.Bold)
                    if (genres.isEmpty()) {
                        Text("No genres selected.", color = colors.onSurface)
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(genres) { g ->
                                FilterChip(
                                    selected = true,
                                    onClick = { },
                                    label = { Text(g, color = colors.onPrimary) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = colors.primary,
                                        selectedLabelColor = colors.onPrimary,
                                        containerColor = colors.surfaceVariant,
                                        labelColor = colors.onSurface
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    FirebaseAuth.getInstance().signOut()
                    Toast.makeText(context, "Logged out", Toast.LENGTH_SHORT).show()
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.ExitToApp, contentDescription = "Logout")
                Spacer(Modifier.width(8.dp))
                Text("Log Out", fontSize = 16.sp)
            }
        }

        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = {
                    showResetDialog = false
                    oldPwd = ""; newPwd = ""; confirmPwd = ""; err = null
                },
                title = { Text("Change Password", color = colors.onBackground) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = oldPwd,
                            onValueChange = { oldPwd = it },
                            label = { Text("Current Password") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor   = colors.primary,
                                unfocusedBorderColor = colors.primary,
                                cursorColor          = colors.primary
                            )
                        )
                        OutlinedTextField(
                            value = newPwd,
                            onValueChange = { newPwd = it },
                            label = { Text("New Password") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor   = colors.primary,
                                unfocusedBorderColor = colors.primary,
                                cursorColor          = colors.primary
                            )
                        )
                        OutlinedTextField(
                            value = confirmPwd,
                            onValueChange = { confirmPwd = it },
                            label = { Text("Confirm New Password") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor   = colors.primary,
                                unfocusedBorderColor = colors.primary,
                                cursorColor          = colors.primary
                            )
                        )
                        err?.let { Text(it, color = colors.error) }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        when {
                            oldPwd.isBlank()     -> err = "Please enter current password"
                            newPwd.length < 6    -> err = "Password must be at least 6 chars"
                            newPwd != confirmPwd -> err = "Passwords do not match"
                            user == null         -> err = "No user logged in"
                            else -> {
                                val cred = EmailAuthProvider.getCredential(email, oldPwd)
                                user.reauthenticate(cred)
                                    .addOnSuccessListener {
                                        user.updatePassword(newPwd)
                                            .addOnSuccessListener {
                                                Toast.makeText(context, "Password changed", Toast.LENGTH_SHORT).show()
                                                showResetDialog = false
                                                oldPwd = ""; newPwd = ""; confirmPwd = ""; err = null
                                            }
                                            .addOnFailureListener { e -> err = e.localizedMessage }
                                    }
                                    .addOnFailureListener { err = "Current password is incorrect" }
                            }
                        }
                    }) {
                        Text("Apply", color = colors.primary)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showResetDialog = false
                        oldPwd = ""; newPwd = ""; confirmPwd = ""; err = null
                    }) {
                        Text("Cancel", color = colors.primary)
                    }
                },
                containerColor = colors.surface
            )
        }
    }
}
