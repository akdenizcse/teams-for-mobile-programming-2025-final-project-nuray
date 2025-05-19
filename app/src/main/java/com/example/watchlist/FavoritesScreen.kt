// FavoritesScreen.kt
package com.example.watchlist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.watchlist.model.MovieItem
import com.example.watchlist.viewmodel.FavoritesViewModel
import com.example.watchlist.viewmodel.MovieViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    navController: NavHostController,
    favoritesViewModel: FavoritesViewModel = viewModel(),
    movieViewModel: MovieViewModel       = viewModel()
) {
    val colors    = MaterialTheme.colorScheme
    val filterBg  = colors.secondaryContainer
    val chipBg    = colors.surfaceVariant
    val onBg      = colors.onBackground
    val onSurface = colors.onSurface
    val tint      = colors.primary

    var showSearch  by rememberSaveable { mutableStateOf(false) }
    var showFilters by rememberSaveable { mutableStateOf(false) }
    var query       by rememberSaveable { mutableStateOf("") }
    var genres      by rememberSaveable { mutableStateOf<List<String>>(emptyList()) }
    var startYear   by rememberSaveable { mutableStateOf("") }
    var endYear     by rememberSaveable { mutableStateOf("") }
    var minRating   by rememberSaveable { mutableStateOf("") }
    var maxRating   by rememberSaveable { mutableStateOf("") }
    var currentPage by rememberSaveable { mutableStateOf(1) }
    val pageSize    = 20

    val allMovies   = favoritesViewModel.favoriteMovies
    val scrollState = rememberScrollState()
    val scope       = rememberCoroutineScope()

    val filtered = remember(allMovies, query, genres, startYear, endYear, minRating, maxRating) {
        allMovies
            .filter { it.title.contains(query, ignoreCase = true) }
            .filter { genres.isEmpty() || genres.any { g -> it.getGenreNames().contains(g) } }
            .filter {
                startYear.toIntOrNull()?.let { sy ->
                    it.releaseDate.take(4).toIntOrNull()?.let { rd -> rd >= sy } ?: true
                } ?: true
            }
            .filter {
                endYear.toIntOrNull()?.let { ey ->
                    it.releaseDate.take(4).toIntOrNull()?.let { rd -> rd <= ey } ?: true
                } ?: true
            }
            .filter { minRating.toDoubleOrNull()?.let { mr -> it.rating >= mr } ?: true }
            .filter { maxRating.toDoubleOrNull()?.let { xr -> it.rating <= xr } ?: true }
    }

    val totalPages = (filtered.size + pageSize - 1) / pageSize
    if (currentPage > totalPages && totalPages > 0) currentPage = totalPages
    val pageItems = filtered.drop((currentPage - 1) * pageSize).take(pageSize)

    Scaffold(
        containerColor = colors.background,
        bottomBar      = { BottomBar(navController) }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .background(colors.background)
                .padding(padding)
                .padding(16.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = { showSearch = !showSearch }) {
                        Icon(Icons.Filled.Search, contentDescription = "Search", tint = tint)
                    }
                    IconButton(onClick = { showFilters = !showFilters }) {
                        Icon(Icons.Filled.FilterList, contentDescription = "Filters", tint = tint)
                    }
                }
                IconButton(onClick = { navController.navigate("settings") }) {
                    Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = tint)
                }
            }

            AnimatedVisibility(showSearch, enter = fadeIn(), exit = fadeOut()) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it; currentPage = 1 },
                    modifier    = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    singleLine  = true,
                    textStyle   = TextStyle(color = onBg),
                    placeholder = { Text("Search favorites…", color = onBg.copy(alpha = .6f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = tint,
                        unfocusedBorderColor = tint,
                        cursorColor          = tint
                    )
                )
            }

            AnimatedVisibility(showFilters, enter = fadeIn(), exit = fadeOut()) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .border(1.dp, tint, RoundedCornerShape(12.dp))
                        .background(filterBg, RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Genre", fontWeight = FontWeight.Bold, color = onSurface)
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .horizontalScroll(scrollState),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Action","Drama","Comedy","Sci-Fi").forEach { g ->
                            val sel = genres.contains(g)
                            FilterChip(
                                selected = sel,
                                onClick  = { genres = if (sel) genres - g else genres + g; currentPage = 1 },
                                label    = { Text(g, color = if (sel) colors.onPrimary else onSurface) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = tint,
                                    selectedLabelColor     = colors.onPrimary,
                                    containerColor         = chipBg,
                                    labelColor             = onSurface
                                ),
                                shape = RoundedCornerShape(24.dp)
                            )
                        }
                    }

                    Text("Year Range", fontWeight = FontWeight.Bold, color = onSurface)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = startYear,
                            onValueChange = { startYear = it; currentPage = 1 },
                            modifier   = Modifier.weight(1f),
                            singleLine = true,
                            textStyle  = TextStyle(color = onSurface),
                            placeholder = { Text("From (YYYY)", color = onSurface.copy(alpha = .6f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor   = tint,
                                unfocusedBorderColor = tint,
                                cursorColor          = tint
                            )
                        )
                        OutlinedTextField(
                            value = endYear,
                            onValueChange = { endYear = it; currentPage = 1 },
                            modifier   = Modifier.weight(1f),
                            singleLine = true,
                            textStyle  = TextStyle(color = onSurface),
                            placeholder = { Text("To (YYYY)", color = onSurface.copy(alpha = .6f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor   = tint,
                                unfocusedBorderColor = tint,
                                cursorColor          = tint
                            )
                        )
                    }

                    Text("Rating Range", fontWeight = FontWeight.Bold, color = onSurface)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = minRating,
                            onValueChange = { minRating = it; currentPage = 1 },
                            modifier   = Modifier.weight(1f),
                            singleLine = true,
                            textStyle  = TextStyle(color = onSurface),
                            placeholder = { Text("Min", color = onSurface.copy(alpha = .6f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor   = tint,
                                unfocusedBorderColor = tint,
                                cursorColor          = tint
                            )
                        )
                        OutlinedTextField(
                            value = maxRating,
                            onValueChange = { maxRating = it; currentPage = 1 },
                            modifier   = Modifier.weight(1f),
                            singleLine = true,
                            textStyle  = TextStyle(color = onSurface),
                            placeholder = { Text("Max", color = onSurface.copy(alpha = .6f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor   = tint,
                                unfocusedBorderColor = tint,
                                cursorColor          = tint
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            LazyColumn(
                Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(pageItems) { movie ->
                    MovieCard(movie, movieViewModel, scope)
                }
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                IconButton(onClick = { if (currentPage > 1) currentPage-- }, enabled = currentPage > 1) {
                    Icon(Icons.Filled.ArrowBack, tint = tint, contentDescription = "Prev")
                }
                Text("Page $currentPage / $totalPages", color = tint, fontSize = 14.sp)
                IconButton(onClick = { if (currentPage < totalPages) currentPage++ }, enabled = currentPage < totalPages) {
                    Icon(Icons.Filled.ArrowForward, tint = tint, contentDescription = "Next")
                }
            }
        }
    }
}
