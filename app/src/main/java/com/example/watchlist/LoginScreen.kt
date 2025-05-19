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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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

private fun isValidEmail(email: String): Boolean =
    Patterns.EMAIL_ADDRESS.matcher(email).matches()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit = {},
    onSignUpClick: () -> Unit = {}
) {
    val colors    = MaterialTheme.colorScheme
    val isDark    = colors.background == DarkBackground
    val buttonCol = if (isDark) DarkSecondary else LightButton
    val textCol   = if (isDark) colors.onBackground else Color.Black

    var email             by remember { mutableStateOf("") }
    var password          by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage      by remember { mutableStateOf<String?>(null) }
    val authVM: AuthViewModel = viewModel()

    Box(
        Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement   = Arrangement.Center,
            horizontalAlignment   = Alignment.CenterHorizontally
        ) {
            Image(
                painter           = painterResource(id = R.drawable.cinecue),
                contentDescription = "App Logo",
                modifier          = Modifier
                    .width(250.dp)
                    .height(125.dp),
                contentScale      = ContentScale.Fit
            )

            AnimatedVisibility(visible = errorMessage != null, enter = fadeIn()) {
                Text(
                    text     = errorMessage.orEmpty(),
                    color    = colors.error,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(24.dp),
                colors   = CardDefaults.cardColors(containerColor = colors.surface)
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
                        color = colors.onSurface
                    )
                    Text(
                        "Login to your account",
                        fontSize = 14.sp,
                        color = colors.onSurface.copy(alpha = 0.7f)
                    )

                    OutlinedTextField(
                        value         = email,
                        onValueChange = { email = it },
                        isError       = errorMessage != null && !isValidEmail(email),
                        label         = { Text("Email", color = colors.onSurface) },
                        placeholder   = { Text("you@domain.com", color = colors.onSurface.copy(alpha = 0.5f)) },
                        singleLine    = true,
                        textStyle     = TextStyle(color = colors.onSurface),
                        modifier      = Modifier.fillMaxWidth(),
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = colors.primary,
                            unfocusedBorderColor = colors.onSurfaceVariant,
                            cursorColor          = colors.primary,
                            errorBorderColor     = colors.error
                        )
                    )

                    OutlinedTextField(
                        value               = password,
                        onValueChange       = { password = it },
                        isError             = errorMessage != null && password.length < 6,
                        label               = { Text("Password", color = colors.onSurface) },
                        placeholder         = { Text("••••••", color = colors.onSurface.copy(alpha = 0.5f)) },
                        singleLine          = true,
                        textStyle           = TextStyle(color = colors.onSurface),
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon        = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector        = if (isPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = null,
                                    tint               = colors.onSurfaceVariant
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors   = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = colors.primary,
                            unfocusedBorderColor = colors.onSurfaceVariant,
                            cursorColor          = colors.primary,
                            errorBorderColor     = colors.error
                        )
                    )

                    Button(
                        onClick = {
                            when {
                                !isValidEmail(email) -> errorMessage = "Invalid email format"
                                password.length < 6  -> errorMessage = "Password must be at least 6 characters"
                                else -> {
                                    errorMessage = null
                                    authVM.loginUser(
                                        email    = email,
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
                        colors = ButtonDefaults.buttonColors(
                            containerColor         = buttonCol,
                            contentColor           = textCol,
                            disabledContainerColor = buttonCol.copy(alpha = 0.4f),
                            disabledContentColor   = textCol.copy(alpha = 0.4f)
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
                Text("Don't have an account? ", color = colors.onBackground)
                Text(
                    "Sign up",
                    color = colors.primary,
                    modifier = Modifier.clickable { onSignUpClick() }
                )
            }
        }
    }
}
