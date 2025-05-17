// LoginScreen.kt
package com.example.watchlist

import android.util.Patterns
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.watchlist.ui.theme.SoftPink
import com.example.watchlist.ui.theme.White

private val DarkNavy      = Color(0xFF0A1D37)
private val LightGrayBlue = Color(0xFFA5ABBD)

fun isValidEmail(email: String): Boolean =
    Patterns.EMAIL_ADDRESS.matcher(email).matches()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit = {},
    onSignUpClick: () -> Unit = {}
) {
    var email             by remember { mutableStateOf("") }
    var password          by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage      by remember { mutableStateOf<String?>(null) }
    val authViewModel: AuthViewModel = viewModel()

    Box(
        Modifier
            .fillMaxSize()
            .background(DarkNavy)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.cinecue),
                contentDescription = "App Logo",
                modifier = Modifier

                    .width(250.dp)
                    .height(125.dp)
                    .padding(bottom = 0.dp)
            )

            AnimatedVisibility(visible = errorMessage != null, enter = fadeIn()) {
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = cardColors(containerColor = White)
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Welcome",
                        style = MaterialTheme.typography.headlineSmall,
                        color = DarkNavy
                    )
                    Text(
                        "Login to your account",
                        fontSize = 14.sp,
                        color = DarkNavy.copy(alpha = 0.7f)
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        isError = errorMessage != null && !isValidEmail(email),
                        label = { Text("Email", color = DarkNavy) },
                        placeholder = { Text("you@domain.com", color = LightGrayBlue) },
                        singleLine = true,
                        textStyle = TextStyle(color = DarkNavy),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        isError = errorMessage != null && password.length < 6,
                        label = { Text("Password", color = DarkNavy) },
                        placeholder = { Text("••••••", color = LightGrayBlue) },
                        singleLine = true,
                        textStyle = TextStyle(color = DarkNavy),
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = null,
                                    tint = LightGrayBlue
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            when {
                                !isValidEmail(email) -> errorMessage = "Invalid email format"
                                password.length < 6  -> errorMessage = "Password must be at least 6 characters"
                                else -> {
                                    errorMessage = null
                                    authViewModel.loginUser(
                                        email = email,
                                        password = password,
                                        onSuccess = { onLoginSuccess() },
                                        onError   = { err -> errorMessage = err }
                                    )
                                }
                            }
                        },
                        enabled = email.isNotBlank() && password.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = buttonColors(
                            containerColor = SoftPink,
                            contentColor = White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Log In", fontSize = 16.sp)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text("Don't have an account? ", color = White)
                Text(
                    "Sign up",
                    color = LightGrayBlue,
                    modifier = Modifier.clickable { onSignUpClick() }
                )
            }
        }
    }
}
