package com.example.watchlist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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

private val DarkBackground = Color(0xFF0A1D37)
private val AccentBlue     = Color(0xFFA5ABBD)
private val FilterBg       = Color(0xFF717788)
private val BorderGray     = Color(0xFFCCCCCC)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: MovieViewModel = viewModel()
) {
    var showSearch      by rememberSaveable { mutableStateOf(false) }
    var showFilterPanel by rememberSaveable { mutableStateOf(false) }
    var showSortMenu    by rememberSaveable { mutableStateOf(false) }
    var searchQuery     by rememberSaveable { mutableStateOf("") }
    var genreFilter     by rememberSaveable { mutableStateOf<String?>(null) }
    var fromYear        by rememberSaveable { mutableStateOf("") }
    var toYear          by rememberSaveable { mutableStateOf("") }
    var minRating       by rememberSaveable { mutableStateOf("") }
    var maxRating       by rememberSaveable { mutableStateOf("") }
    val sortOptions     = listOf("Default", "By IMDb Rating", "By Release Date")


    val moviesList = viewModel.movies.filter { m ->
        m.title.isNotBlank()
    }

    val scrollState    = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        containerColor = DarkBackground,
        bottomBar      = { BottomBar(navController) }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(padding)
                .padding(16.dp)
        ) {
            // -- Arama / Filtre / Sıralama ikonları --
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                IconButton(onClick = { showSearch = !showSearch }) {
                    Icon(Icons.Filled.Search, contentDescription = null, tint = AccentBlue)
                }
                IconButton(onClick = { showFilterPanel = !showFilterPanel }) {
                    Icon(Icons.Filled.FilterList, contentDescription = null, tint = AccentBlue)
                }
                IconButton(onClick = { showSortMenu = !showSortMenu }) {
                    Icon(Icons.Filled.Sort, contentDescription = null, tint = AccentBlue)
                }
                DropdownMenu(
                    expanded         = showSortMenu,
                    onDismissRequest = { showSortMenu = false }
                ) {
                    sortOptions.forEach { option ->
                        DropdownMenuItem(
                            text    = { Text(option) },
                            onClick = {
                                viewModel.selectedSort = option
                                viewModel.applyFilters(
                                    query       = searchQuery.ifBlank { null },
                                    genre       = genreFilter,
                                    yearFrom    = fromYear.ifBlank { null },
                                    yearTo      = toYear.ifBlank { null },
                                    ratingMin   = minRating.toDoubleOrNull(),
                                    ratingMax   = maxRating.toDoubleOrNull()
                                )
                                showSortMenu = false
                            }
                        )
                    }
                }
            }

            // -- Arama alanı --
            AnimatedVisibility(showSearch, enter = fadeIn(), exit = fadeOut()) {
                OutlinedTextField(
                    value         = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        viewModel.applyFilters(
                            query       = it.ifBlank { null },
                            genre       = genreFilter,
                            yearFrom    = fromYear.ifBlank { null },
                            yearTo      = toYear.ifBlank { null },
                            ratingMin   = minRating.toDoubleOrNull(),
                            ratingMax   = maxRating.toDoubleOrNull()
                        )
                    },
                    modifier    = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    singleLine  = true,
                    textStyle   = TextStyle(color = Color.White),
                    placeholder = { Text("Search movies…", color = Color.White) }
                )
            }

            // -- Filtre paneli --
            AnimatedVisibility(showFilterPanel, enter = fadeIn(), exit = fadeOut()) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderGray, RoundedCornerShape(12.dp))
                        .background(FilterBg, RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Genre", fontWeight = FontWeight.Bold, color = Color.Black)
                    Row(Modifier.horizontalScroll(scrollState), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Action", "Drama", "Comedy", "Sci-Fi").forEach { g ->
                            val sel = genreFilter == g
                            FilterChip(
                                selected = sel,
                                onClick  = {
                                    genreFilter = if (sel) null else g
                                    viewModel.applyFilters(
                                        query       = searchQuery.ifBlank { null },
                                        genre       = genreFilter,
                                        yearFrom    = fromYear.ifBlank { null },
                                        yearTo      = toYear.ifBlank { null },
                                        ratingMin   = minRating.toDoubleOrNull(),
                                        ratingMax   = maxRating.toDoubleOrNull()
                                    )
                                },
                                label  = { Text(g, color = if (sel) Color.Black else Color.White) },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = if (sel) AccentBlue else DarkBackground,
                                    labelColor     = if (sel) Color.Black else Color.White
                                ),
                                shape  = RoundedCornerShape(24.dp)
                            )
                        }
                    }
                    Text("Year Range", fontWeight = FontWeight.Bold, color = Color.Black)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value         = fromYear,
                            onValueChange = {
                                fromYear = it
                                viewModel.applyFilters(
                                    query       = searchQuery.ifBlank { null },
                                    genre       = genreFilter,
                                    yearFrom    = it.ifBlank { null },
                                    yearTo      = toYear.ifBlank { null },
                                    ratingMin   = minRating.toDoubleOrNull(),
                                    ratingMax   = maxRating.toDoubleOrNull()
                                )
                            },
                            modifier    = Modifier.weight(1f),
                            singleLine  = true,
                            textStyle   = TextStyle(color = Color.Black),
                            label       = { Text("From (YYYY)", color = Color.Black) }
                        )
                        OutlinedTextField(
                            value         = toYear,
                            onValueChange = {
                                toYear = it
                                viewModel.applyFilters(
                                    query       = searchQuery.ifBlank { null },
                                    genre       = genreFilter,
                                    yearFrom    = fromYear.ifBlank { null },
                                    yearTo      = it.ifBlank { null },
                                    ratingMin   = minRating.toDoubleOrNull(),
                                    ratingMax   = maxRating.toDoubleOrNull()
                                )
                            },
                            modifier    = Modifier.weight(1f),
                            singleLine  = true,
                            textStyle   = TextStyle(color = Color.Black),
                            label       = { Text("To (YYYY)", color = Color.Black) }
                        )
                    }
                    Text("Rating Range", fontWeight = FontWeight.Bold, color = Color.Black)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value         = minRating,
                            onValueChange = {
                                minRating = it
                                viewModel.applyFilters(
                                    query       = searchQuery.ifBlank { null },
                                    genre       = genreFilter,
                                    yearFrom    = fromYear.ifBlank { null },
                                    yearTo      = toYear.ifBlank { null },
                                    ratingMin   = it.toDoubleOrNull(),
                                    ratingMax   = maxRating.toDoubleOrNull()
                                )
                            },
                            modifier    = Modifier.weight(1f),
                            singleLine  = true,
                            textStyle   = TextStyle(color = Color.Black),
                            label       = { Text("Min", color = Color.Black) }
                        )
                        OutlinedTextField(
                            value         = maxRating,
                            onValueChange = {
                                maxRating = it
                                viewModel.applyFilters(
                                    query       = searchQuery.ifBlank { null },
                                    genre       = genreFilter,
                                    yearFrom    = fromYear.ifBlank { null },
                                    yearTo      = toYear.ifBlank { null },
                                    ratingMin   = minRating.toDoubleOrNull(),
                                    ratingMax   = it.toDoubleOrNull()
                                )
                            },
                            modifier    = Modifier.weight(1f),
                            singleLine  = true,
                            textStyle   = TextStyle(color = Color.Black),
                            label       = { Text("Max", color = Color.Black) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))


            LazyColumn(
                Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(moviesList) { movie ->
                    MovieCard(movie, viewModel, coroutineScope)
                }
            }

            Spacer(Modifier.height(12.dp))


            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.prevPage() }, enabled = viewModel.currentPage > 1) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = AccentBlue)
                }
                Text("${viewModel.currentPage} / ${viewModel.totalPages}", color = AccentBlue, fontSize = 14.sp)
                IconButton(onClick = { viewModel.nextPage() }, enabled = viewModel.currentPage < viewModel.totalPages) {
                    Icon(Icons.Filled.ArrowForward, contentDescription = null, tint = AccentBlue)
                }
            }
        }
    }
}



@Composable
fun MovieCard(
    movie: MovieItem,
    viewModel: MovieViewModel,
    scope: kotlinx.coroutines.CoroutineScope
) {
    val isFav   = viewModel.isMovieFavorite(movie.id.toString())
    val isWatch = viewModel.isMovieInWatchlist(movie.id.toString())

    Card(
        Modifier
            .fillMaxWidth()
            .height(160.dp),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(4.dp)
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
                modifier           = Modifier
                    .width(120.dp)
                    .fillMaxHeight(),
                contentScale       = ContentScale.Crop
            )
            Column(
                Modifier
                    .weight(1f)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(movie.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Release: ${movie.releaseDate}", fontSize = 14.sp)
                Text("IMDb: ${movie.rating}", fontSize = 14.sp)
                Text(movie.getGenreNames(), fontSize = 14.sp)
            }
            Column(
                Modifier.padding(8.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                IconButton(onClick = {
                    scope.launch { viewModel.toggleFavorite(movie.id.toString(), !isFav) }
                }) {
                    Icon(
                        imageVector   = if (isFav) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = null,
                        tint           = if (isFav) Color.Red else AccentBlue
                    )
                }
                IconButton(onClick = {
                    scope.launch { viewModel.toggleWatchlist(movie.id.toString(), !isWatch) }
                }) {
                    Icon(
                        imageVector   = if (isWatch) Icons.Filled.Check else Icons.Filled.Add,
                        contentDescription = null,
                        tint           = if (isWatch) Color.Green else AccentBlue
                    )
                }
            }
        }
    }
}
