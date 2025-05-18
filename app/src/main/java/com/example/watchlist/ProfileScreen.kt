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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.watchlist.ui.theme.SoftPink
import com.example.watchlist.ui.theme.White
import com.example.watchlist.viewmodel.ProfileViewModel
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private val DarkNavy      = Color(0xFF0A1D37)
private val LightGrayBlue = Color(0xFFA5ABBD)
private val CardWhite     = Color(0xFFF2F2F2)
private val TextDark      = Color(0xFF333333)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    vm: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    var firstName by remember { mutableStateOf("") }
    var lastName  by remember { mutableStateOf("") }
    val user      = FirebaseAuth.getInstance().currentUser
    val email     = user?.email.orEmpty()
    var showResetDialog by remember { mutableStateOf(false) }
    var oldPwd      by remember { mutableStateOf("") }
    var newPwd      by remember { mutableStateOf("") }
    var confirmPwd  by remember { mutableStateOf("") }
    var err         by remember { mutableStateOf<String?>(null) }
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
        containerColor = DarkNavy,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Profile", color = White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, null, tint = White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = DarkNavy)
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .background(DarkNavy)
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
                    .background(LightGrayBlue),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.AccountCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(64.dp)
                )
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("ABOUT", fontSize = 18.sp, color = TextDark, fontWeight = FontWeight.Bold)
                    Text("$firstName $lastName", color = TextDark, fontSize = 16.sp)
                    Text(email, color = TextDark, fontSize = 16.sp)
                    Divider()
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { showResetDialog = true }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.LockReset, null, tint = LightGrayBlue)
                        Spacer(Modifier.width(8.dp))
                        Text("Change Password", color = TextDark, fontSize = 16.sp)
                    }
                    err?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 14.sp) }
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("PREFERRED GENRES", fontSize = 18.sp, color = TextDark, fontWeight = FontWeight.Bold)
                    if (genres.isEmpty()) {
                        Text("No genres selected.", color = TextDark)
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(genres) { genre ->
                                FilterChip(
                                    selected = true,
                                    onClick = {},
                                    label = { Text(genre) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = SoftPink,
                                        labelColor = TextDark
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
                colors = ButtonDefaults.buttonColors(containerColor = LightGrayBlue)
            ) {
                Icon(Icons.Filled.ExitToApp, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Log Out", color = Color.White)
            }
        }

        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = {
                    showResetDialog = false
                    oldPwd = ""
                    newPwd = ""
                    confirmPwd = ""
                    err = null
                },
                title = { Text("Change Password") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = oldPwd,
                            onValueChange = { oldPwd = it },
                            label = { Text("Current Password") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation()
                        )
                        OutlinedTextField(
                            value = newPwd,
                            onValueChange = { newPwd = it },
                            label = { Text("New Password") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation()
                        )
                        OutlinedTextField(
                            value = confirmPwd,
                            onValueChange = { confirmPwd = it },
                            label = { Text("Confirm New Password") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation()
                        )
                        err?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (oldPwd.isBlank()) {
                            err = "Please enter current password"
                        } else if (newPwd.length < 6) {
                            err = "Password must be at least 6 chars"
                        } else if (newPwd != confirmPwd) {
                            err = "Passwords do not match"
                        } else if (user == null) {
                            err = "No user logged in"
                        } else {
                            val cred = EmailAuthProvider.getCredential(email, oldPwd)
                            user.reauthenticate(cred)
                                .addOnSuccessListener {
                                    user.updatePassword(newPwd)
                                        .addOnSuccessListener {
                                            Toast.makeText(context, "Password changed", Toast.LENGTH_SHORT).show()
                                            showResetDialog = false
                                            oldPwd = ""
                                            newPwd = ""
                                            confirmPwd = ""
                                            err = null
                                        }
                                        .addOnFailureListener { e ->
                                            err = e.localizedMessage
                                        }
                                }
                                .addOnFailureListener {
                                    err = "Current password is incorrect"
                                }
                        }
                    }) {
                        Text("Apply")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showResetDialog = false
                        oldPwd = ""
                        newPwd = ""
                        confirmPwd = ""
                        err = null
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
