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
import androidx.compose.ui.platform.LocalContext
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

fun isStrongPassword(password: String): Boolean {
    val uppercase = Regex("[A-Z]")
    val lowercase = Regex("[a-z]")
    val special   = Regex("[^A-Za-z0-9]")
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

    var firstName       by remember { mutableStateOf("") }
    var lastName        by remember { mutableStateOf("") }
    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPwd         by remember { mutableStateOf(false) }
    var showConfirm     by remember { mutableStateOf(false) }
    var errorMessage    by remember { mutableStateOf<String?>(null) }

    val canSubmit = listOf(firstName, lastName, email, password, confirmPassword).all { it.isNotBlank() }

    Scaffold(
        containerColor = DarkNavy,
        topBar = {
            CenterAlignedTopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { onBackClick() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = Color.White)
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
                    .height(100.dp)
            )

            AnimatedVisibility(visible = errorMessage != null, enter = fadeIn()) {
                Text(
                    errorMessage.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                )
            }

            Card(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = White)
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
                        label = { Text("First Name", color = DarkNavy) },
                        singleLine = true,
                        textStyle = TextStyle(color = DarkNavy),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = { Text("Last Name", color = DarkNavy) },
                        singleLine = true,
                        textStyle = TextStyle(color = DarkNavy),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email", color = DarkNavy) },
                        placeholder = { Text("you@domain.com", color = LightGrayBlue) },
                        singleLine = true,
                        textStyle = TextStyle(color = DarkNavy),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password", color = DarkNavy) },
                        placeholder = { Text("••••••", color = LightGrayBlue) },
                        singleLine = true,
                        textStyle = TextStyle(color = DarkNavy),
                        visualTransformation = if (showPwd) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showPwd = !showPwd }) {
                                Icon(
                                    imageVector = if (showPwd) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = null,
                                    tint = LightGrayBlue
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm Password", color = DarkNavy) },
                        placeholder = { Text("••••••", color = LightGrayBlue) },
                        singleLine = true,
                        textStyle = TextStyle(color = DarkNavy),
                        visualTransformation = if (showConfirm) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showConfirm = !showConfirm }) {
                                Icon(
                                    imageVector = if (showConfirm) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = null,
                                    tint = LightGrayBlue
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            errorMessage = when {
                                firstName.isBlank() -> "First name required"
                                lastName.isBlank()  -> "Last name required"
                                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Invalid email"
                                !isStrongPassword(password) -> "Password must be ≥6 chars, upper, lower & symbol"
                                password != confirmPassword -> "Passwords do not match"
                                else -> {
                                    authViewModel.registerUser(
                                        firstName = firstName,
                                        lastName  = lastName,
                                        email     = email,
                                        password  = password,
                                        onSuccess = {
                                            Toast.makeText(context, "Signed up!", Toast.LENGTH_SHORT).show()
                                            onBackClick()
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
                            containerColor = SoftPink,
                            contentColor   = White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Sign Up", fontSize = 16.sp)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Row {
                Text("Already have an account? ", color = White)
                Text(
                    "Log in",
                    color = LightGrayBlue,
                    modifier = Modifier.clickable { onLoginClick() }
                )
            }
        }
    }
}
