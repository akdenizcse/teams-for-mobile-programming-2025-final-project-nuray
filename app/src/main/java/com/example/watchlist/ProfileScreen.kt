package com.example.watchlist

import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme

    val sProfileTitle       = stringResource(R.string.profile_title)
    val sBackDesc           = stringResource(R.string.cd_previous)
    val sAbout              = stringResource(R.string.label_about)
    val sChangePassword     = stringResource(R.string.change_password)
    val sPreferredGenres    = stringResource(R.string.preferred_genres)
    val sNoGenres           = stringResource(R.string.no_genres_selected)
    val sLogout             = stringResource(R.string.logout)
    val sConfirmLogout      = stringResource(R.string.confirm_logout)
    val sYes                = stringResource(R.string.yes)
    val sNo                 = stringResource(R.string.no)
    val sEditName           = stringResource(R.string.edit_name)
    val sFirstNameLabel     = stringResource(R.string.first_name_label)
    val sLastNameLabel      = stringResource(R.string.last_name_label)
    val sSave               = stringResource(R.string.save)
    val sCancel             = stringResource(R.string.cancel)
    val sApply              = stringResource(R.string.apply)
    val sCurrentPassword    = stringResource(R.string.current_password)
    val sNewPassword        = stringResource(R.string.new_password)
    val sConfirmNewPassword = stringResource(R.string.confirm_new_password)
    val sNameUpdated        = stringResource(R.string.name_updated)
    val sPwdChanged         = stringResource(R.string.pwd_changed)
    val sErrEnterCurrent    = stringResource(R.string.err_enter_current)
    val sErrPwdLength       = stringResource(R.string.err_pwd_length)
    val sErrPwdComplex      = stringResource(R.string.err_pwd_strength)
    val sErrPwdMatch        = stringResource(R.string.err_pwd_match)
    val sErrNoUser          = stringResource(R.string.err_no_user)
    val sErrCurrentIncorrect= stringResource(R.string.err_current_incorrect)
    val sSelectAvatar       = stringResource(R.string.select_avatar)
    val sAvatarUpdated      = stringResource(R.string.avatar_updated)

    // UI state
    var showAvatarDialog by remember { mutableStateOf(false) }
    var showEditDialog   by remember { mutableStateOf(false) }
    var showResetDialog  by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    var editFirst  by remember { mutableStateOf("") }
    var editLast   by remember { mutableStateOf("") }
    var oldPwd     by remember { mutableStateOf("") }
    var newPwd     by remember { mutableStateOf("") }
    var confirmPwd by remember { mutableStateOf("") }
    var err        by remember { mutableStateOf<String?>(null) }

    val user   = FirebaseAuth.getInstance().currentUser
    val email  = user?.email.orEmpty()
    val genres = vm.watchlistGenres.toList()


    val avatars = listOf(
        R.drawable.avatar2,
        R.drawable.avatar1,
        R.drawable.avatar3,
        R.drawable.avatar4,
        R.drawable.avatar5,
        R.drawable.avatar6,
        R.drawable.avatar7,
        R.drawable.avatar8,
        R.drawable.avatar9,
    )


    LaunchedEffect(Unit) {
        user?.uid?.let { uid ->
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { doc ->
                    editFirst = doc.getString("firstName").orEmpty()
                    editLast  = doc.getString("lastName").orEmpty()
                }
        }
    }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(sProfileTitle, color = colors.onBackground) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = sBackDesc, tint = colors.primary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = colors.background)
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
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
                    .background(colors.secondaryContainer)
                    .clickable { showAvatarDialog = true },
                contentAlignment = Alignment.Center
            ) {
                val resId = vm.avatarUrl?.toIntOrNull() ?: avatars.first()
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                )
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(24.dp)
                        .offset((-4).dp, (-4).dp)
                )
            }


            Card(colors = CardDefaults.cardColors(containerColor = colors.surface), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(sAbout, fontSize = 18.sp, color = colors.onSurface, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("$editFirst $editLast", color = colors.onSurface, fontSize = 16.sp)
                        Spacer(Modifier.width(8.dp))
                        IconButton(onClick = { showEditDialog = true }) {
                            Icon(Icons.Filled.Edit, contentDescription = sEditName, tint = colors.primary)
                        }
                    }
                    Text(email, color = colors.onSurface, fontSize = 16.sp)
                    Divider(color = colors.outline)
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { showResetDialog = true }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.LockReset, contentDescription = sChangePassword, tint = colors.primary)
                        Spacer(Modifier.width(8.dp))
                        Text(sChangePassword, color = colors.onSurface, fontSize = 16.sp)
                    }
                    err?.let { Text(it, color = colors.error, fontSize = 14.sp) }
                }
            }


            Card(colors = CardDefaults.cardColors(containerColor = colors.surface), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(sPreferredGenres, fontSize = 18.sp, color = colors.onSurface, fontWeight = FontWeight.Bold)
                    if (genres.isEmpty()) {
                        Text(sNoGenres, color = colors.onSurface)
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(genres) { g ->
                                FilterChip(
                                    selected = true,
                                    onClick  = { },
                                    label    = { Text(g, color = colors.onPrimary) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = colors.primary,
                                        selectedLabelColor     = colors.onPrimary,
                                        containerColor         = colors.surfaceVariant,
                                        labelColor             = colors.onSurface
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                )
                            }
                        }
                    }
                }
            }


            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.onPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.ExitToApp, contentDescription = sLogout)
                Spacer(Modifier.width(8.dp))
                Text(sLogout, fontSize = 16.sp)
            }
        }


        if (showAvatarDialog) {
            AlertDialog(
                onDismissRequest = { showAvatarDialog = false },
                title   = { Text(sSelectAvatar, color = colors.onBackground) },
                text    = {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(avatars) { id ->
                            Image(
                                painter = painterResource(id = id),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        vm.updateAvatar(id.toString()) {
                                            Toast.makeText(context, sAvatarUpdated, Toast.LENGTH_SHORT).show()
                                            showAvatarDialog = false
                                        }
                                    }
                            )
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showAvatarDialog = false }) {
                        Text(sCancel, color = colors.primary)
                    }
                },
                containerColor = colors.surface
            )
        }

        if (showEditDialog) {
            AlertDialog(
                onDismissRequest = { showEditDialog = false },
                title            = { Text(sEditName, color = colors.onBackground) },
                text             = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = editFirst,
                            onValueChange = { editFirst = it },
                            label = { Text(sFirstNameLabel) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor   = colors.primary,
                                unfocusedBorderColor = colors.primary,
                                cursorColor          = colors.primary
                            )
                        )
                        OutlinedTextField(
                            value = editLast,
                            onValueChange = { editLast = it },
                            label = { Text(sLastNameLabel) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor   = colors.primary,
                                unfocusedBorderColor = colors.primary,
                                cursorColor          = colors.primary
                            )
                        )
                    }
                },
                confirmButton    = {
                    TextButton(onClick = {
                        user?.uid?.let { uid ->
                            FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(uid)
                                .update(mapOf("firstName" to editFirst, "lastName" to editLast))
                                .addOnSuccessListener {
                                    Toast.makeText(context, sNameUpdated, Toast.LENGTH_SHORT).show()
                                    showEditDialog = false
                                }
                        }
                    }) {
                        Text(sSave, color = colors.primary)
                    }
                },
                dismissButton    = {
                    TextButton(onClick = { showEditDialog = false }) {
                        Text(sCancel, color = colors.primary)
                    }
                },
                containerColor   = colors.surface
            )
        }


        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = {
                    showResetDialog = false
                    oldPwd = ""; newPwd = ""; confirmPwd = ""; err = null
                },
                title            = { Text(sChangePassword, color = colors.onBackground) },
                text             = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = oldPwd,
                            onValueChange = { oldPwd = it },
                            label = { Text(sCurrentPassword) },
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
                            label = { Text(sNewPassword) },
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
                            label = { Text(sConfirmNewPassword) },
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
                confirmButton    = {
                    TextButton(onClick = {
                        err = when {
                            oldPwd.isBlank() -> sErrEnterCurrent
                            newPwd.length < 6 -> sErrPwdLength
                            !newPwd.any { it.isUpperCase() } ||
                                    !newPwd.any { it.isLowerCase() } ||
                                    !newPwd.any { !it.isLetterOrDigit() } -> sErrPwdComplex
                            newPwd != confirmPwd -> sErrPwdMatch
                            user == null -> sErrNoUser
                            else -> null
                        }
                        if (err == null) {
                            val cred = EmailAuthProvider.getCredential(email, oldPwd)
                            user!!.reauthenticate(cred)
                                .addOnSuccessListener {
                                    user.updatePassword(newPwd)
                                        .addOnSuccessListener {
                                            Toast.makeText(context, sPwdChanged, Toast.LENGTH_SHORT).show()
                                            showResetDialog = false
                                            oldPwd = ""; newPwd = ""; confirmPwd = ""; err = null
                                        }
                                }
                                .addOnFailureListener { err = sErrCurrentIncorrect }
                        }
                    }) {
                        Text(sApply, color = colors.primary)
                    }
                },
                dismissButton    = {
                    TextButton(onClick = {
                        showResetDialog = false
                        oldPwd = ""; newPwd = ""; confirmPwd = ""; err = null
                    }) {
                        Text(sCancel, color = colors.primary)
                    }
                },
                containerColor   = colors.surface
            )
        }


        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title            = { Text(sConfirmLogout, color = colors.onBackground) },
                text             = { Text(sConfirmLogout, color = colors.onBackground) },
                confirmButton    = {
                    TextButton(onClick = {
                        FirebaseAuth.getInstance().signOut()
                        Toast.makeText(context, sLogout, Toast.LENGTH_SHORT).show()
                        navController.navigate("login") { popUpTo("home") { inclusive = true } }
                    }) {
                        Text(sYes, color = colors.primary)
                    }
                },
                dismissButton    = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text(sNo, color = colors.primary)
                    }
                },
                containerColor   = colors.surface
            )
        }
    }
}
