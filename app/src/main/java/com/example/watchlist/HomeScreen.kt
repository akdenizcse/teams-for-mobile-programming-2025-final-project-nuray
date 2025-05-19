// HomeScreen.kt
package com.example.watchlist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.watchlist.model.MovieItem
import com.example.watchlist.viewmodel.MovieViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: MovieViewModel = viewModel()
) {
    val colors       = MaterialTheme.colorScheme
    val filterBg     = colors.secondaryContainer
    val chipBg       = colors.surfaceVariant

    var showSearch   by rememberSaveable { mutableStateOf(false) }
    var showFilters  by rememberSaveable { mutableStateOf(false) }
    var showSortMenu by rememberSaveable { mutableStateOf(false) }
    var searchQuery  by rememberSaveable { mutableStateOf("") }
    var genreFilter  by rememberSaveable { mutableStateOf<String?>(null) }
    var fromYear     by rememberSaveable { mutableStateOf("") }
    var toYear       by rememberSaveable { mutableStateOf("") }
    var minRating    by rememberSaveable { mutableStateOf("") }
    var maxRating    by rememberSaveable { mutableStateOf("") }

    val sortOptions  = listOf("Default", "By IMDb Rating", "By Release Date")
    val scrollState  = rememberScrollState()
    val scope        = rememberCoroutineScope()
    val moviesList   = viewModel.movies

    Scaffold(
        containerColor = colors.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Home", color = colors.onBackground) },
                actions = {
                    IconButton(onClick = { showSearch = !showSearch }) {
                        Icon(Icons.Filled.Search, tint = colors.primary, contentDescription = "Search")
                    }
                    IconButton(onClick = { showFilters = !showFilters }) {
                        Icon(Icons.Filled.FilterList, tint = colors.primary, contentDescription = "Filter")
                    }
                    IconButton(onClick = { showSortMenu = !showSortMenu }) {
                        Icon(Icons.Filled.Sort, tint = colors.primary, contentDescription = "Sort")
                    }
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(Icons.Filled.Settings, tint = colors.primary, contentDescription = "Settings")
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false },
                        containerColor = colors.surface
                    ) {
                        sortOptions.forEach { option ->
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text(option, color = colors.onSurface) },
                                onClick = {
                                    viewModel.selectedSort = option
                                    viewModel.applyFilters(
                                        query     = searchQuery.ifBlank { null },
                                        genre     = genreFilter,
                                        yearFrom  = fromYear.ifBlank { null },
                                        yearTo    = toYear.ifBlank { null },
                                        ratingMin = minRating.toDoubleOrNull(),
                                        ratingMax = maxRating.toDoubleOrNull()
                                    )
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = colors.background)
            )
        },
        bottomBar = { BottomBar(navController) }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .background(colors.background)
                .padding(padding)
                .padding(16.dp)
        ) {
            AnimatedVisibility(showSearch, enter = fadeIn(), exit = fadeOut()) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        viewModel.applyFilters(
                            query     = it.ifBlank { null },
                            genre     = genreFilter,
                            yearFrom  = fromYear.ifBlank { null },
                            yearTo    = toYear.ifBlank { null },
                            ratingMin = minRating.toDoubleOrNull(),
                            ratingMax = maxRating.toDoubleOrNull()
                        )
                    },
                    modifier   = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    singleLine = true,
                    textStyle  = TextStyle(color = colors.onBackground),
                    placeholder = { Text("Search movies…", color = colors.onBackground.copy(alpha = .6f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = colors.primary,
                        unfocusedBorderColor = colors.primary,
                        cursorColor          = colors.primary
                    )
                )
            }

            AnimatedVisibility(showFilters, enter = fadeIn(), exit = fadeOut()) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .border(1.dp, colors.outline, RoundedCornerShape(12.dp))
                        .background(filterBg, RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Genre", fontWeight = FontWeight.Bold, color = colors.onSurface)
                    Row(
                        Modifier.horizontalScroll(scrollState),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Action", "Drama", "Comedy", "Sci-Fi").forEach { g ->
                            val selected = genreFilter == g
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    genreFilter = if (selected) null else g
                                    viewModel.applyFilters(
                                        query     = searchQuery.ifBlank { null },
                                        genre     = genreFilter,
                                        yearFrom  = fromYear.ifBlank { null },
                                        yearTo    = toYear.ifBlank { null },
                                        ratingMin = minRating.toDoubleOrNull(),
                                        ratingMax = maxRating.toDoubleOrNull()
                                    )
                                },
                                label = { Text(g, color = if (selected) colors.onPrimary else colors.onSurface) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = colors.primary,
                                    selectedLabelColor     = colors.onPrimary,
                                    containerColor         = chipBg,
                                    labelColor             = colors.onSurface
                                ),
                                shape = RoundedCornerShape(24.dp)
                            )
                        }
                    }

                    Text("Year Range", fontWeight = FontWeight.Bold, color = colors.onSurface)
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = fromYear,
                            onValueChange = { new ->
                                fromYear = new
                                viewModel.applyFilters(
                                    query     = searchQuery.ifBlank { null },
                                    genre     = genreFilter,
                                    yearFrom  = fromYear.ifBlank { null },
                                    yearTo    = toYear.ifBlank { null },
                                    ratingMin = minRating.toDoubleOrNull(),
                                    ratingMax = maxRating.toDoubleOrNull()
                                )
                            },
                            modifier   = Modifier.weight(1f),
                            singleLine = true,
                            placeholder = { Text("From (YYYY)", color = colors.onBackground) },
                            textStyle  = TextStyle(color = colors.onBackground),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor   = colors.primary,
                                unfocusedBorderColor = colors.primary,
                                cursorColor          = colors.primary
                            )
                        )
                        OutlinedTextField(
                            value = toYear,
                            onValueChange = { new ->
                                toYear = new
                                viewModel.applyFilters(
                                    query     = searchQuery.ifBlank { null },
                                    genre     = genreFilter,
                                    yearFrom  = fromYear.ifBlank { null },
                                    yearTo    = toYear.ifBlank { null },
                                    ratingMin = minRating.toDoubleOrNull(),
                                    ratingMax = maxRating.toDoubleOrNull()
                                )
                            },
                            modifier   = Modifier.weight(1f),
                            singleLine = true,
                            placeholder = { Text("To (YYYY)", color = colors.onBackground) },
                            textStyle  = TextStyle(color = colors.onBackground),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor   = colors.primary,
                                unfocusedBorderColor = colors.primary,
                                cursorColor          = colors.primary
                            )
                        )
                    }

                    Text("Rating Range", fontWeight = FontWeight.Bold, color = colors.onSurface)
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = minRating,
                            onValueChange = { new ->
                                minRating = new
                                viewModel.applyFilters(
                                    query     = searchQuery.ifBlank { null },
                                    genre     = genreFilter,
                                    yearFrom  = fromYear.ifBlank { null },
                                    yearTo    = toYear.ifBlank { null },
                                    ratingMin = minRating.toDoubleOrNull(),
                                    ratingMax = maxRating.toDoubleOrNull()
                                )
                            },
                            modifier   = Modifier.weight(1f),
                            singleLine = true,
                            placeholder = { Text("Min", color = colors.onBackground) },
                            textStyle  = TextStyle(color = colors.onBackground),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor   = colors.primary,
                                unfocusedBorderColor = colors.primary,
                                cursorColor          = colors.primary
                            )
                        )
                        OutlinedTextField(
                            value = maxRating,
                            onValueChange = { new ->
                                maxRating = new
                                viewModel.applyFilters(
                                    query     = searchQuery.ifBlank { null },
                                    genre     = genreFilter,
                                    yearFrom  = fromYear.ifBlank { null },
                                    yearTo    = toYear.ifBlank { null },
                                    ratingMin = minRating.toDoubleOrNull(),
                                    ratingMax = maxRating.toDoubleOrNull()
                                )
                            },
                            modifier   = Modifier.weight(1f),
                            singleLine = true,
                            placeholder = { Text("Max", color = colors.onBackground) },
                            textStyle  = TextStyle(color = colors.onBackground),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor   = colors.primary,
                                unfocusedBorderColor = colors.primary,
                                cursorColor          = colors.primary
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
                items(moviesList) { movie -> MovieCard(movie, viewModel, scope) }
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.prevPage() }, enabled = viewModel.currentPage > 1) {
                    Icon(Icons.Filled.ArrowBack, tint = colors.primary, contentDescription = "Prev")
                }
                Text("${viewModel.currentPage} / ${viewModel.totalPages}", color = colors.primary, fontSize = 14.sp)
                IconButton(onClick = { viewModel.nextPage() }, enabled = viewModel.currentPage < viewModel.totalPages) {
                    Icon(Icons.Filled.ArrowForward, tint = colors.primary, contentDescription = "Next")
                }
            }
        }
    }
}

@Composable
fun MovieCard(
    movie: MovieItem,
    viewModel: MovieViewModel,
    coroutineScope: kotlinx.coroutines.CoroutineScope
) {
    val colors = MaterialTheme.colorScheme
    val bg     = colors.surface
    val tint   = colors.primary
    val isFav  = viewModel.isMovieFavorite(movie.id.toString())
    val isWatch= viewModel.isMovieInWatchlist(movie.id.toString())

    Card(
        Modifier.fillMaxWidth().height(160.dp),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = bg),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row {
            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current)
                        .data("https://image.tmdb.org/t/p/w300${movie.posterUrl}")
                        .crossfade(true)
                        .build()
                ),
                contentDescription = movie.title,
                modifier           = Modifier.width(120.dp).fillMaxHeight(),
                contentScale       = ContentScale.Crop
            )
            Column(Modifier.weight(1f).padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(movie.title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = colors.onSurface)
                Text("Release: ${movie.releaseDate}", fontSize = 14.sp, color = colors.onSurface)
                Text("IMDb: ${movie.rating}", fontSize = 14.sp, color = colors.onSurface)
                Text(movie.getGenreNames(), fontSize = 14.sp, color = colors.onSurface)
            }
            Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.SpaceBetween, horizontalAlignment = Alignment.End) {
                IconButton(onClick = { coroutineScope.launch { viewModel.toggleFavorite(movie.id.toString(), !isFav) } }) {
                    Icon(if (isFav) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder, tint = if (isFav) Color.Red else tint, contentDescription = "Favorite")
                }
                IconButton(onClick = { coroutineScope.launch { viewModel.toggleWatchlist(movie.id.toString(), !isWatch) } }) {
                    Icon(if (isWatch) Icons.Filled.Check else Icons.Filled.Add, tint = if (isWatch) Color.Green else tint, contentDescription = "Watchlist")
                }
            }
        }
    }
}