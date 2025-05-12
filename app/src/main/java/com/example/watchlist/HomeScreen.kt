package com.example.watchlist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.watchlist.model.MovieItem
import com.example.watchlist.viewmodel.MovieViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
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

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = { showSearch = !showSearch }) {
                Icon(Icons.Filled.Search, contentDescription = "Search")
            }
            IconButton(onClick = { showFilters = !showFilters }) {
                Icon(Icons.Filled.FilterList, contentDescription = "Filters")
            }
        }

        AnimatedVisibility(visible = showSearch, enter = fadeIn(), exit = fadeOut()) {
            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                    movieViewModel.searchMovies(it)
                },
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                singleLine = true,
                placeholder = { Text("Search movies…") }
            )
        }

        AnimatedVisibility(visible = showFilters, enter = fadeIn(), exit = fadeOut()) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Genre", fontWeight = FontWeight.Bold)
                Row(
                    Modifier.horizontalScroll(scrollState),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Action", "Drama", "Comedy", "Sci-Fi").forEach { g ->
                        FilterChip(
                            selected = genre == g,
                            onClick = {
                                genre = if (genre == g) null else g
                                movieViewModel.selectedGenre = genre
                                movieViewModel.applyFilters()
                            },
                            label = { Text(g) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor     = Color.White
                            )
                        )
                    }
                }

                Text("Year Range", fontWeight = FontWeight.Bold)
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = startYear,
                        onValueChange = { v ->
                            startYear = v
                            movieViewModel.selectedStartYear = v.ifBlank { null }
                            movieViewModel.applyFilters()
                        },
                        label      = { Text("From (YYYY)") },
                        modifier   = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = endYear,
                        onValueChange = { v ->
                            endYear = v
                            movieViewModel.selectedEndYear = v.ifBlank { null }
                            movieViewModel.applyFilters()
                        },
                        label      = { Text("To (YYYY)") },
                        modifier   = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Text("Rating Range", fontWeight = FontWeight.Bold)
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = minRating,
                        onValueChange = { v ->
                            minRating = v
                            movieViewModel.selectedMinRating = v.toDoubleOrNull()
                            movieViewModel.applyFilters()
                        },
                        label = { Text("Min") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = maxRating,
                        onValueChange = { v ->
                            maxRating = v
                            movieViewModel.selectedMaxRating = v.toDoubleOrNull()
                            movieViewModel.applyFilters()
                        },
                        label = { Text("Max") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier             = Modifier.weight(1f)
        ) {
            items(movies) { movie ->
                MovieCard(movie, movieViewModel, scope)
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment   = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { movieViewModel.prevPage() },
                enabled = currentPage > 1
            ) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Prev Page")
            }
            Text("Page $currentPage / $totalPages", fontSize = 14.sp)
            IconButton(
                onClick = { movieViewModel.nextPage() },
                enabled = currentPage < totalPages
            ) {
                Icon(Icons.Filled.ArrowForward, contentDescription = "Next Page")
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
    var fav   by remember { mutableStateOf(viewModel.isMovieFavorite(movie.id.toString())) }
    var watch by remember { mutableStateOf(viewModel.isMovieInWatchlist(movie.id.toString())) }

    Card(
        Modifier
            .fillMaxWidth()
            .height(160.dp),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = Color(0xFFF2F2F2)),
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
                Text("IMDB: ${movie.rating}",       fontSize = 14.sp)
                Text("Genre: ${movie.getGenreNames()}", fontSize = 14.sp)
            }
            Column(
                verticalArrangement  = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End,
                modifier            = Modifier.padding(8.dp)
            ) {
                IconButton(onClick = {
                    fav = !fav
                    scope.launch { viewModel.toggleFavorite(movie, fav) }
                }) {
                    Icon(
                        imageVector = if (fav) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite"
                    )
                }
                IconButton(onClick = {
                    watch = !watch
                    scope.launch { viewModel.toggleWatchlist(movie, watch) }
                }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add to Watchlist")
                }
            }
        }
    }
}
