// app/src/main/java/com/example/watchlist/ProfileScreen.kt
package com.example.watchlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBarDefaults.centerAlignedTopAppBarColors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.watchlist.viewmodel.ProfileViewModel

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
    val fullName   = vm.fullName
    val email      = vm.email
    val genres     = vm.preferredGenres.toList()
    val showDialog = vm.isPasswordDialogVisible
    val statusMsg  = vm.passwordChangeMessage

    var selected by remember { mutableStateOf(genres.toMutableSet()) }
    LaunchedEffect(genres) {
        if (selected.isEmpty()) selected = genres.toMutableSet()
    }

    Scaffold(
        containerColor = DarkNavy,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Profile", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                colors = centerAlignedTopAppBarColors(containerColor = DarkNavy)
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .background(DarkNavy)
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(16.dp)
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
            Spacer(Modifier.height(24.dp))

            // ABOUT card
            Card(
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("ABOUT", fontSize = 18.sp, color = TextDark)
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Filled.Edit, contentDescription = "Edit About", tint = LightGrayBlue)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("Full Name", color = TextDark, fontSize = 14.sp)
                    Text(fullName, color = TextDark, fontSize = 16.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("Email Address", color = TextDark, fontSize = 14.sp)
                    Text(email, color = TextDark, fontSize = 16.sp)
                    Spacer(Modifier.height(8.dp))
                    Divider()
                    Spacer(Modifier.height(8.dp))
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { vm.showPasswordDialog() }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.LockReset, contentDescription = "Change Password", tint = LightGrayBlue)
                        Spacer(Modifier.width(8.dp))
                        Text("Change Password", color = TextDark, fontSize = 16.sp)
                    }
                    statusMsg?.let {
                        Text(it, color = Color.Red, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Preferred Genres card
            Card(
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("PREFERRED GENRES", fontSize = 18.sp, color = TextDark)
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Filled.Edit, contentDescription = "Edit Genres", tint = LightGrayBlue)
                    }
                    Spacer(Modifier.height(12.dp))
                    if (genres.isEmpty()) {
                        Text("No genres selected yet.", color = TextDark)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            genres.forEach { genre ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val checked = selected.contains(genre)
                                    Checkbox(
                                        checked = checked,
                                        onCheckedChange = {
                                            if (checked) selected -= genre else selected += genre
                                            vm.savePreferredGenres(selected.toList())
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor   = LightGrayBlue,
                                            uncheckedColor = TextDark
                                        )
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(genre, color = TextDark)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Log Out button
            Button(
                onClick = {
                    vm.signOut()
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

        // Change Password dialog
        if (showDialog) {
            var currentPass by remember { mutableStateOf("") }
            var newPass1    by remember { mutableStateOf("") }
            var newPass2    by remember { mutableStateOf("") }
            var hideOld    by remember { mutableStateOf(true) }
            var hideNew1   by remember { mutableStateOf(true) }
            var hideNew2   by remember { mutableStateOf(true) }

            AlertDialog(
                onDismissRequest = { vm.hidePasswordDialog() },
                title = { Text("Change Password") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = currentPass,
                            onValueChange = { currentPass = it },
                            label = { Text("Current Password") },
                            singleLine = true,
                            visualTransformation = if (hideOld) PasswordVisualTransformation() else VisualTransformation.None,
                            trailingIcon = {
                                IconButton(onClick={ hideOld = !hideOld }) {
                                    Icon(
                                        imageVector = if(hideOld) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                        contentDescription = null
                                    )
                                }
                            }
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newPass1,
                            onValueChange = { newPass1 = it },
                            label = { Text("New Password") },
                            singleLine = true,
                            visualTransformation = if (hideNew1) PasswordVisualTransformation() else VisualTransformation.None,
                            trailingIcon = {
                                IconButton(onClick={ hideNew1 = !hideNew1 }) {
                                    Icon(
                                        imageVector = if(hideNew1) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                        contentDescription = null
                                    )
                                }
                            }
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newPass2,
                            onValueChange = { newPass2 = it },
                            label = { Text("Confirm New Password") },
                            singleLine = true,
                            visualTransformation = if (hideNew2) PasswordVisualTransformation() else VisualTransformation.None,
                            trailingIcon = {
                                IconButton(onClick={ hideNew2 = !hideNew2 }) {
                                    Icon(
                                        imageVector = if(hideNew2) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                        contentDescription = null
                                    )
                                }
                            }
                        )
                        vm.passwordChangeMessage?.let { msg ->
                            Spacer(Modifier.height(8.dp))
                            Text(msg, color = Color.Red, fontSize = 14.sp)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        vm.changePassword(currentPass, newPass1, newPass2)
                    }) {
                        Text("Apply")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { vm.hidePasswordDialog() }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
