package com.example.watchlist

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
fun HomeScreen(movieViewModel: MovieViewModel = viewModel()) {
    val movies by movieViewModel.movies
    val searchQuery by movieViewModel.searchQuery

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { movieViewModel.updateSearchQuery(it) },
            label = { Text("Search Movies") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(movies.size) { index ->
                MovieCard(movie = movies[index], movieViewModel = movieViewModel)
            }
        }
    }
}

@Composable
fun MovieCard(movie: MovieItem, movieViewModel: MovieViewModel) {
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
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
            val painter = rememberAsyncImagePainter(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("https://image.tmdb.org/t/p/w500${movie.posterUrl}")
                    .crossfade(true)
                    .build()
            )

            Image(
                painter = painter,
                contentDescription = movie.title,
                modifier = Modifier
                    .width(100.dp)
                    .height(140.dp)
                    .padding(end = 12.dp),
                contentScale = ContentScale.Fit
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.Top)
                    .padding(top = 4.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(movie.title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                Text("Release: ${movie.releaseDate}", fontSize = 14.sp, color = Color.DarkGray)
                Text("IMDB Rating: ${movie.rating}", fontSize = 14.sp, color = Color.DarkGray)
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
