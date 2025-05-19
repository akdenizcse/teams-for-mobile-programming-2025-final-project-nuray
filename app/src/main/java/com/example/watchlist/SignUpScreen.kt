package com.example.watchlist

import android.util.Patterns
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.watchlist.ui.theme.LightButton
import com.example.watchlist.ui.theme.DarkSecondary
import com.example.watchlist.ui.theme.DarkBackground

fun isStrongPassword(password: String): Boolean {
    val uppercase = Regex("[A-Z]")
    val lowercase = Regex("[a-z]")
    val special = Regex("[^A-Za-z0-9]")
    return password.length >= 6 &&
            uppercase.containsMatchIn(password) &&
            lowercase.containsMatchIn(password) &&
            special.containsMatchIn(password)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onBackClick: () -> Unit,
    onSignUpSuccess: () -> Unit,
    onLoginClick: () -> Unit
) {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel()
    val colors = MaterialTheme.colorScheme
    val isDark = colors.background == DarkBackground
    val buttonCol = if (isDark) DarkSecondary else LightButton
    val textCol = if (isDark) colors.onBackground else Color.Black

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPwd by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val canSubmit = listOf(firstName, lastName, email, password, confirmPassword).all { it.isNotBlank() }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = colors.onBackground)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = colors.background)
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .background(colors.background)
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            Image(
                painter = painterResource(id = R.drawable.cinecue),
                contentDescription = null,
                modifier = Modifier
                    .width(250.dp)
                    .height(100.dp),
                contentScale = ContentScale.Fit
            )

            AnimatedVisibility(visible = errorMessage != null, enter = fadeIn()) {
                Text(
                    text = errorMessage.orEmpty(),
                    color = colors.error,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface)
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = { Text("First Name", color = colors.onSurface) },
                        singleLine = true,
                        textStyle = TextStyle(color = colors.onSurface),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.onSurfaceVariant,
                            cursorColor = colors.primary
                        )
                    )
                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = { Text("Last Name", color = colors.onSurface) },
                        singleLine = true,
                        textStyle = TextStyle(color = colors.onSurface),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.onSurfaceVariant,
                            cursorColor = colors.primary
                        )
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email", color = colors.onSurface) },
                        placeholder = { Text("you@domain.com", color = colors.onSurface.copy(alpha = 0.5f)) },
                        singleLine = true,
                        textStyle = TextStyle(color = colors.onSurface),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.onSurfaceVariant,
                            cursorColor = colors.primary
                        )
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password", color = colors.onSurface) },
                        placeholder = { Text("••••••", color = colors.onSurface.copy(alpha = 0.5f)) },
                        singleLine = true,
                        textStyle = TextStyle(color = colors.onSurface),
                        visualTransformation = if (showPwd) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showPwd = !showPwd }) {
                                Icon(
                                    imageVector = if (showPwd) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = null,
                                    tint = colors.onSurfaceVariant
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.onSurfaceVariant,
                            cursorColor = colors.primary
                        )
                    )
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm Password", color = colors.onSurface) },
                        placeholder = { Text("••••••", color = colors.onSurface.copy(alpha = 0.5f)) },
                        singleLine = true,
                        textStyle = TextStyle(color = colors.onSurface),
                        visualTransformation = if (showConfirm) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showConfirm = !showConfirm }) {
                                Icon(
                                    imageVector = if (showConfirm) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = null,
                                    tint = colors.onSurfaceVariant
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.onSurfaceVariant,
                            cursorColor = colors.primary
                        )
                    )
                    Button(
                        onClick = {
                            errorMessage = when {
                                firstName.isBlank() -> "First name required"
                                lastName.isBlank() -> "Last name required"
                                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Invalid email"
                                !isStrongPassword(password) -> "Password must be ≥6 chars, upper, lower & symbol"
                                password != confirmPassword -> "Passwords do not match"
                                else -> {
                                    authViewModel.registerUser(
                                        firstName = firstName,
                                        lastName = lastName,
                                        email = email,
                                        password = password,
                                        onSuccess = {
                                            Toast.makeText(context, "Signed up!",	Toast.LENGTH_SHORT).show()
                                            onSignUpSuccess()
                                        },
                                        onError = { errorMessage = it }
                                    )
                                    null
                                }
                            }
                        },
                        enabled = canSubmit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = buttonCol,
                            contentColor = textCol,
                            disabledContainerColor = buttonCol.copy(alpha = 0.4f),
                            disabledContentColor = textCol.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Sign Up", fontSize = 16.sp)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text("Already have an account? ", color = colors.onBackground)
                Text(
                    "Log in",
                    color = colors.primary,
                    modifier = Modifier.clickable { onLoginClick() }
                )
            }
        }
    }
}