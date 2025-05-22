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
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.watchlist.ui.theme.DarkBackground
import com.example.watchlist.ui.theme.DarkSecondary
import com.example.watchlist.ui.theme.LightButton


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
    // 1) Preload all strings
    val context       = LocalContext.current
    val sFirstName    = stringResource(R.string.first_name_label)
    val sLastName     = stringResource(R.string.last_name_label)
    val sEmail        = stringResource(R.string.email_label)
    val sEmailHint    = stringResource(R.string.email_placeholder)
    val sPassword     = stringResource(R.string.password_label)
    val sPasswordHint = stringResource(R.string.password_placeholder)
    val sConfirmPwd   = stringResource(R.string.confirm_new_password)
    val sSignUp       = stringResource(R.string.sign_up_button)
    val sHaveAccount  = stringResource(R.string.already_have_account)
    val sLogIn        = stringResource(R.string.login)
    val sErrFirst     = stringResource(R.string.err_first_required)
    val sErrLast      = stringResource(R.string.err_last_required)
    val sErrEmail     = stringResource(R.string.err_invalid_email)
    val sErrPwdStrong = stringResource(R.string.err_pwd_strength)
    val sErrMatch     = stringResource(R.string.err_pwd_match)
    val sSignedUp     = stringResource(R.string.msg_signed_up)

    val authViewModel: AuthViewModel = viewModel()
    val colors       = MaterialTheme.colorScheme
    val isDark       = colors.background == DarkBackground
    val buttonCol    = if (isDark) DarkSecondary else LightButton
    val textCol      = if (isDark) colors.onBackground else Color.Black


    var firstName       by remember { mutableStateOf("") }
    var lastName        by remember { mutableStateOf("") }
    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPwd         by remember { mutableStateOf(false) }
    var showConfirm     by remember { mutableStateOf(false) }
    var errorMessage    by remember { mutableStateOf<String?>(null) }

    val canSubmit = listOf(firstName, lastName, email, password, confirmPassword)
        .all { it.isNotBlank() }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_previous),
                            tint = colors.onBackground
                        )
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
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            Image(
                painter = painterResource(R.drawable.cinecue),
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
                    fun updateError(msg: String?) { errorMessage = msg }

                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = { Text(sFirstName, color = colors.onSurface) },
                        singleLine = true,
                        textStyle = TextStyle(color = colors.onSurface),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = colors.primary,
                            unfocusedBorderColor = colors.onSurfaceVariant,
                            cursorColor          = colors.primary
                        )
                    )
                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = { Text(sLastName, color = colors.onSurface) },
                        singleLine = true,
                        textStyle = TextStyle(color = colors.onSurface),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = colors.primary,
                            unfocusedBorderColor = colors.onSurfaceVariant,
                            cursorColor          = colors.primary
                        )
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(sEmail, color = colors.onSurface) },
                        placeholder = { Text(sEmailHint, color = colors.onSurface.copy(alpha = 0.5f)) },
                        singleLine = true,
                        textStyle = TextStyle(color = colors.onSurface),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = colors.primary,
                            unfocusedBorderColor = colors.onSurfaceVariant,
                            cursorColor          = colors.primary
                        )
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(sPassword, color = colors.onSurface) },
                        placeholder = { Text(sPasswordHint, color = colors.onSurface.copy(alpha = 0.5f)) },
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
                            focusedBorderColor   = colors.primary,
                            unfocusedBorderColor = colors.onSurfaceVariant,
                            cursorColor          = colors.primary
                        )
                    )
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text(sConfirmPwd, color = colors.onSurface) },
                        placeholder = { Text(sPasswordHint, color = colors.onSurface.copy(alpha = 0.5f)) },
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
                            focusedBorderColor   = colors.primary,
                            unfocusedBorderColor = colors.onSurfaceVariant,
                            cursorColor          = colors.primary
                        )
                    )

                    Button(
                        onClick = {
                            errorMessage = when {
                                firstName.isBlank() -> sErrFirst
                                lastName.isBlank()  -> sErrLast
                                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> sErrEmail
                                !isStrongPassword(password) -> sErrPwdStrong
                                password != confirmPassword -> sErrMatch
                                else -> {
                                    authViewModel.registerUser(
                                        firstName = firstName,
                                        lastName = lastName,
                                        email = email,
                                        password = password,
                                        onSuccess = {
                                            Toast.makeText(context, sSignedUp, Toast.LENGTH_SHORT).show()
                                            onSignUpSuccess()
                                        },
                                        onError = { updateError(it) }
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
                            containerColor         = buttonCol,
                            contentColor           = textCol,
                            disabledContainerColor = buttonCol.copy(alpha = 0.4f),
                            disabledContentColor   = textCol.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(sSignUp, fontSize = 16.sp)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(sHaveAccount, color = colors.onBackground)
                Text(
                    text = sLogIn,
                    color = colors.primary,
                    modifier = Modifier.clickable { onLoginClick() }
                )
            }
        }
    }
}
