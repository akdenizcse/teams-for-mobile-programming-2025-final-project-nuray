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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
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

private val DarkNavy       = Color(0xFF0A1D37)
private val LightGrayBlue  = Color(0xFFA5ABBD)
private val MediumGrayBlue = Color(0xFF717788)
private val BorderGray     = Color(0xFFCCCCCC)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    movieViewModel: MovieViewModel = viewModel()
) {
    var showSearch  by rememberSaveable { mutableStateOf(false) }
    var showFilters by rememberSaveable { mutableStateOf(false) }
    var query       by rememberSaveable { mutableStateOf("") }
    var genre       by rememberSaveable { mutableStateOf<String?>(null) }
    var startYear   by rememberSaveable { mutableStateOf("") }
    var endYear     by rememberSaveable { mutableStateOf("") }
    var minRating   by rememberSaveable { mutableStateOf("") }
    var maxRating   by rememberSaveable { mutableStateOf("") }

    val movies      = movieViewModel.movies
    val currentPage = movieViewModel.currentPage
    val totalPages  = movieViewModel.totalPages

    val scrollState = rememberScrollState()
    val scope       = rememberCoroutineScope()

    Scaffold(
        containerColor = DarkNavy,
        bottomBar      = { BottomBar(navController) }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .background(DarkNavy)
                .padding(16.dp)
        ) {
            // Search & filter icons
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = { showSearch = !showSearch }) {
                    Icon(Icons.Filled.Search, contentDescription = "Search", tint = LightGrayBlue)
                }
                IconButton(onClick = { showFilters = !showFilters }) {
                    Icon(Icons.Filled.FilterList, contentDescription = "Filters", tint = LightGrayBlue)
                }
            }

            // Search field
            AnimatedVisibility(showSearch, enter = fadeIn(), exit = fadeOut()) {
                OutlinedTextField(
                    value         = query,
                    onValueChange = {
                        query = it
                        movieViewModel.searchMovies(it)
                    },
                    modifier      = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    singleLine    = true,
                    textStyle     = TextStyle(color = Color.White),
                    placeholder   = { Text("Search movies…", color = Color.White) }
                )
            }

            // Filters panel
            AnimatedVisibility(showFilters, enter = fadeIn(), exit = fadeOut()) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderGray, RoundedCornerShape(12.dp))
                        .background(MediumGrayBlue, RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Genre chips
                    Text("Genre", fontWeight = FontWeight.Bold, color = Color.Black)
                    Row(
                        Modifier.horizontalScroll(scrollState),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Action", "Drama", "Comedy", "Sci-Fi").forEach { g ->
                            val isSelected = genre == g
                            FilterChip(
                                selected = isSelected,
                                onClick  = {
                                    genre = if (isSelected) null else g
                                    movieViewModel.selectedGenre = genre
                                    movieViewModel.applyFilters()
                                },
                                label    = { Text(g, color = if (isSelected) Color.Black else Color.White) },
                                colors   = FilterChipDefaults.filterChipColors(
                                    containerColor = if (isSelected) LightGrayBlue else DarkNavy,
                                    labelColor     = if (isSelected) Color.Black     else Color.White
                                ),
                                shape    = RoundedCornerShape(24.dp)
                            )
                        }
                    }

                    // Year range
                    Text("Year Range", fontWeight = FontWeight.Bold, color = Color.Black)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value         = startYear,
                            onValueChange = {
                                startYear = it
                                movieViewModel.selectedStartYear = it.ifBlank { null }
                                movieViewModel.applyFilters()
                            },
                            modifier      = Modifier.weight(1f),
                            singleLine    = true,
                            textStyle     = TextStyle(color = Color.Black),
                            label         = { Text("From (YYYY)", color = Color.Black) }
                        )
                        OutlinedTextField(
                            value         = endYear,
                            onValueChange = {
                                endYear = it
                                movieViewModel.selectedEndYear = it.ifBlank { null }
                                movieViewModel.applyFilters()
                            },
                            modifier      = Modifier.weight(1f),
                            singleLine    = true,
                            textStyle     = TextStyle(color = Color.Black),
                            label         = { Text("To (YYYY)", color = Color.Black) }
                        )
                    }

                    // Rating range
                    Text("Rating Range", fontWeight = FontWeight.Bold, color = Color.Black)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value         = minRating,
                            onValueChange = {
                                minRating = it
                                movieViewModel.selectedMinRating = it.toDoubleOrNull()
                                movieViewModel.applyFilters()
                            },
                            modifier      = Modifier.weight(1f),
                            singleLine    = true,
                            textStyle     = TextStyle(color = Color.Black),
                            label         = { Text("Min", color = Color.Black) }
                        )
                        OutlinedTextField(
                            value         = maxRating,
                            onValueChange = {
                                maxRating = it
                                movieViewModel.selectedMaxRating = it.toDoubleOrNull()
                                movieViewModel.applyFilters()
                            },
                            modifier      = Modifier.weight(1f),
                            singleLine    = true,
                            textStyle     = TextStyle(color = Color.Black),
                            label         = { Text("Max", color = Color.Black) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Movie list
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(movies) { movie ->
                    MovieCard(movie, movieViewModel, scope)
                }
            }

            Spacer(Modifier.height(12.dp))

            // Pagination
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                IconButton(onClick = { movieViewModel.prevPage() }, enabled = currentPage > 1) {
                    Icon(Icons.Filled.ArrowBack, "Prev", tint = LightGrayBlue)
                }
                Text("Page $currentPage / $totalPages", color = LightGrayBlue, fontSize = 14.sp)
                IconButton(onClick = { movieViewModel.nextPage() }, enabled = currentPage < totalPages) {
                    Icon(Icons.Filled.ArrowForward, "Next", tint = LightGrayBlue)
                }
            }
        }
    }
}

@Composable
private fun MovieCard(
    movie: MovieItem,
    viewModel: MovieViewModel,
    scope: kotlinx.coroutines.CoroutineScope
) {
    // Read directly from ViewModel each recomposition
    val isFav   = viewModel.isMovieFavorite(movie.id.toString())
    val isWatch = viewModel.isMovieInWatchlist(movie.id.toString())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row {
            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current)
                        .data("https://image.tmdb.org/t/p/w300${movie.posterUrl}")
                        .crossfade(true).build()
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
                Text("IMDB: ${movie.rating}", fontSize = 14.sp)
                Text(movie.getGenreNames(), fontSize = 14.sp)
            }

            Column(
                Modifier.padding(8.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                IconButton(onClick = {
                    scope.launch {
                        viewModel.toggleFavorite(movie.id.toString(), !isFav)
                    }
                }) {
                    Icon(
                        imageVector = if (isFav) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFav) Color.Red else LightGrayBlue
                    )
                }
                IconButton(onClick = {
                    scope.launch {
                        viewModel.toggleWatchlist(movie.id.toString(), !isWatch)
                    }
                }) {
                    Icon(
                        imageVector = if (isWatch) Icons.Filled.Check else Icons.Filled.Add,
                        contentDescription = "Watchlist",
                        tint = if (isWatch) Color.Green else LightGrayBlue
                    )
                }
            }
        }
    }
}
