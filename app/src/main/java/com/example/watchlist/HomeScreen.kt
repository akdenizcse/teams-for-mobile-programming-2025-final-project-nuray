package com.example.watchlist

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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

@Composable
fun HomeScreen(
    movieViewModel: MovieViewModel = viewModel()
) {
    // Observe ViewModel state
    val allMovies = movieViewModel.movies
    val searchQuery = movieViewModel.searchQuery
    val currentPage = movieViewModel.currentPage
    val totalPages = movieViewModel.totalPages

    // Local UI state for filters
    var selectedGenre by rememberSaveable { mutableStateOf("All") }
    var selectedYear by rememberSaveable { mutableStateOf("All") }
    var selectedRating by rememberSaveable { mutableStateOf("All") }

    // Apply filters and search
    val filteredMovies = allMovies.filter { movie ->
        val year = movie.releaseDate.substringBefore('-')
        val threshold = selectedRating.removeSuffix("+").toDoubleOrNull() ?: 0.0
        (searchQuery.isBlank() || movie.title.contains(searchQuery, ignoreCase = true)) &&
                (selectedGenre == "All" || movie.getGenreNames().contains(selectedGenre, ignoreCase = true)) &&
                (selectedYear == "All" || year == selectedYear) &&
                (selectedRating == "All" || movie.rating >= threshold)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // Search input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { movieViewModel.updateSearchQuery(it) },
            label = { Text("Search Movies") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        // Filter options
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            FilterDropdown("Genre", listOf("All", "Action", "Drama", "Comedy", "Sci-Fi"), selectedGenre) { selectedGenre = it }
            FilterDropdown("Year", listOf("All", "2024", "2023", "2022", "2021"), selectedYear) { selectedYear = it }
            FilterDropdown("Rating", listOf("All", "9+", "8+", "7+", "6+"), selectedRating) { selectedRating = it }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Reset filters
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = {
                selectedGenre = "All"
                selectedYear = "All"
                selectedRating = "All"
            }) {
                Text("Reset Filters")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Movie list
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(filteredMovies) { movie ->
                MovieCard(movie = movie, movieViewModel = movieViewModel)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Pagination controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { movieViewModel.prevPage() },
                enabled = currentPage > 1
            ) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Previous Page")
            }
            Text("Page $currentPage / $totalPages", fontSize = 14.sp)
            IconButton(
                onClick = { movieViewModel.nextPage() },
                enabled = currentPage < totalPages
            ) {
                Icon(Icons.Filled.Check, contentDescription = "Next Page")
            }
        }
    }
}

@Composable
fun FilterDropdown(
    label: String,
    options: List<String>,
    selected: String,
    onSelectedChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.width(120.dp)) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "$label: $selected", fontSize = 12.sp)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option) }, onClick = {
                    onSelectedChange(option)
                    expanded = false
                })
            }
        }
    }
}

@Composable
fun MovieCard(
    movie: MovieItem,
    movieViewModel: MovieViewModel
) {
    val scope = rememberCoroutineScope()
    var isFavorite by remember { mutableStateOf(false) }
    var isInWatchlist by remember { mutableStateOf(false) }

    LaunchedEffect(movie.id) {
        isFavorite = movieViewModel.isMovieFavorite(movie.id)
        isInWatchlist = movieViewModel.isMovieInWatchlist(movie.id)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F2F2)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current)
                        .data("https://image.tmdb.org/t/p/w500${movie.posterUrl}")
                        .crossfade(true)
                        .build()
                ),
                contentDescription = movie.title,
                modifier = Modifier
                    .width(100.dp)
                    .height(140.dp)
                    .padding(end = 12.dp),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.Top)
                    .padding(top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(movie.title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                Text("Release: ${movie.releaseDate}", fontSize = 14.sp, color = Color.DarkGray)
                Text("IMDB: ${movie.rating}", fontSize = 14.sp, color = Color.DarkGray)
                Text("Genre: ${movie.getGenreNames()}", fontSize = 14.sp, color = Color.DarkGray)
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.End,
                modifier = Modifier.height(140.dp)
            ) {
                IconButton(onClick = {
                    isFavorite = !isFavorite
                    scope.launch { movieViewModel.toggleFavorite(movie, isFavorite) }
                }) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color.Red else Color.Gray
                    )
                }
                IconButton(onClick = {
                    isInWatchlist = !isInWatchlist
                    scope.launch { movieViewModel.toggleWatchlist(movie, isInWatchlist) }
                }) {
                    Icon(
                        imageVector = if (isInWatchlist) Icons.Filled.Check else Icons.Filled.Add,
                        contentDescription = "Watchlist",
                        tint = if (isInWatchlist) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                }
            }
        }
    }
}
