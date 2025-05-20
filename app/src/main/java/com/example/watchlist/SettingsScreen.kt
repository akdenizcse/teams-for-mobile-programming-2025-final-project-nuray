package com.example.watchlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavHostController,
    isDarkMode: Boolean,
    currentLanguageTag: String,
    onThemeToggle: (Boolean) -> Unit,
    onLanguageSelected: (String) -> Unit
) {
    val colors = MaterialTheme.colorScheme


    val languages = listOf(
        "en" to stringResource(R.string.lang_english),
        "tr" to stringResource(R.string.lang_turkish),
        "es" to stringResource(R.string.lang_spanish)
    )
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.settings_title), color = colors.onBackground) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = colors.onBackground)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = colors.background)
            )
        },
        containerColor = colors.background
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.dark_mode),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.onSurface
                    )
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = onThemeToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor   = colors.primary,
                            uncheckedThumbColor = colors.onSurfaceVariant,
                            checkedTrackColor   = colors.primary.copy(alpha = 0.4f),
                            uncheckedTrackColor = colors.onSurfaceVariant.copy(alpha = 0.2f)
                        )
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Column {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { expanded = true }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.language_label),
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.onSurface
                        )
                        Text(

                            text = languages.first { it.first == currentLanguageTag }.second,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurfaceVariant
                        )
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = null,
                            tint = colors.onSurfaceVariant
                        )
                    }
                    Divider(color = colors.onSurfaceVariant.copy(alpha = 0.2f))
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.surface)
                    ) {
                        languages.forEach { (tag, label) ->
                            DropdownMenuItem(
                                text = { Text(label, style = MaterialTheme.typography.bodyMedium, color = colors.onSurface) },
                                onClick = {
                                    onLanguageSelected(tag)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
