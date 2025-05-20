package com.example.watchlist

import android.util.Patterns
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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


private fun isValidEmail(email: String): Boolean =
    Patterns.EMAIL_ADDRESS.matcher(email).matches()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit = {},
    onSignUpClick: () -> Unit = {}
) {

    val sLogoDesc       = stringResource(R.string.app_name)
    val sEmailLabel     = stringResource(R.string.email_label)
    val sEmailHint      = stringResource(R.string.email_placeholder)
    val sPwdLabel       = stringResource(R.string.password_label)
    val sPwdHint        = stringResource(R.string.password_placeholder)
    val sWelcome        = stringResource(R.string.login_welcome)
    val sSubtitle       = stringResource(R.string.login_subtitle)
    val sLogIn          = stringResource(R.string.login)
    val sErrInvalidMail = stringResource(R.string.err_invalid_email)
    val sErrPwdShort    = stringResource(R.string.err_pwd_length)
    val sPromptNoAcc    = stringResource(R.string.already_have_account)
    val sSignUp         = stringResource(R.string.sign_up_button)


    val authVM: AuthViewModel    = viewModel()
    val colors                   = MaterialTheme.colorScheme
    val isDark                   = colors.background == DarkBackground
    val btnColor                 = if (isDark) DarkSecondary else LightButton
    val txtColor                 = if (isDark) colors.onBackground else Color.Black


    var email        by remember { mutableStateOf("") }
    var password     by remember { mutableStateOf("") }
    var showPwd      by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Image(
                painter           = painterResource(R.drawable.cinecue),
                contentDescription= sLogoDesc,
                modifier          = Modifier
                    .width(250.dp)
                    .height(125.dp),
                contentScale      = ContentScale.Fit
            )

            Spacer(Modifier.height(16.dp))


            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp)),
                colors   = CardDefaults.cardColors(containerColor = colors.surface)
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    Text(
                        text  = sWelcome,
                        style = MaterialTheme.typography.headlineSmall,
                        color = colors.onSurface
                    )
                    Text(
                        text     = sSubtitle,
                        fontSize = 14.sp,
                        color    = colors.onSurface.copy(alpha = 0.7f)
                    )


                    AnimatedVisibility(visible = errorMessage != null, enter = fadeIn()) {
                        Text(
                            text     = errorMessage.orEmpty(),
                            color    = colors.error,
                            fontSize = 14.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        )
                    }


                    OutlinedTextField(
                        value         = email,
                        onValueChange = { email = it },
                        isError       = errorMessage != null && !isValidEmail(email),
                        label         = { Text(sEmailLabel, color = colors.onSurface) },
                        placeholder   = { Text(sEmailHint, color = colors.onSurface.copy(alpha = .5f)) },
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
                        value                = password,
                        onValueChange        = { password = it },
                        isError              = errorMessage != null && password.length < 6,
                        label                = { Text(sPwdLabel, color = colors.onSurface) },
                        placeholder          = { Text(sPwdHint, color = colors.onSurface.copy(alpha = .5f)) },
                        singleLine           = true,
                        visualTransformation = if (showPwd) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon         = {
                            IconButton(onClick = { showPwd = !showPwd }) {
                                Icon(
                                    imageVector        = if (showPwd) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = null,
                                    tint               = colors.onSurfaceVariant
                                )
                            }
                        },
                        textStyle = TextStyle(color = colors.onSurface),
                        modifier  = Modifier.fillMaxWidth(),
                        colors    = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = colors.primary,
                            unfocusedBorderColor = colors.onSurfaceVariant,
                            cursorColor          = colors.primary,
                            errorBorderColor     = colors.error
                        )
                    )


                    Button(
                        onClick = {
                            errorMessage = when {
                                !isValidEmail(email) -> sErrInvalidMail
                                password.length < 6  -> sErrPwdShort
                                else -> {
                                    authVM.loginUser(
                                        email    = email,
                                        password = password,
                                        onSuccess= { onLoginSuccess() },
                                        onError  = { err -> errorMessage = err }
                                    )
                                    null
                                }
                            }
                        },
                        enabled  = email.isNotBlank() && password.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor         = btnColor,
                            contentColor           = txtColor,
                            disabledContainerColor = btnColor.copy(alpha = 0.4f),
                            disabledContentColor   = txtColor.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(sLogIn, fontSize = 16.sp)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))


            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(sPromptNoAcc, color = colors.onBackground)
                Text(
                    text     = " $sSignUp",
                    color    = colors.primary,
                    modifier = Modifier.clickable { onSignUpClick() }
                )
            }
        }
    }
}
