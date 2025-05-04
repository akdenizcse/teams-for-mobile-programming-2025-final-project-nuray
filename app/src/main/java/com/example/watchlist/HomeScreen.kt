package com.example.watchlist

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.watchlist.ui.theme.Purple40

data class Movie(
    val id: String,
    val title: String,
    val genre: String,
    val runtime: String,
    val ageRating: String,
    val releaseYear: Int,
    val imdbRating: Double,
    var isFavorite: Boolean = false,
    var isInWatchlist: Boolean = false
)

@Composable
fun HomeScreen(authViewModel: AuthViewModel = viewModel()) {
    val movies = remember {
        mutableStateListOf(
            Movie("1", "Inception", "Sci-Fi", "2h 28m", "PG-13", 2010, 8.8),
            Movie("2", "The Matrix", "Action", "2h 16m", "R", 1999, 8.7),
            Movie("3", "Interstellar", "Adventure", "2h 49m", "PG-13", 2014, 8.6),
            Movie("4", "Joker", "Drama", "2h 2m", "R", 2019, 8.5),
            Movie("5", "Dune", "Sci-Fi", "2h 35m", "PG-13", 2021, 8.1)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = "",
            onValueChange = {},
            label = { Text("Search Title", color = Color.Black) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFFFC1E3),
                unfocusedContainerColor = Color(0xFFFFC1E3),
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color.Gray,
                focusedLabelColor = Color.Black,
                unfocusedLabelColor = Color.Gray,
                cursorColor = Color.Black
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(movies.size) { index ->
                MovieCardRow(movie = movies[index], authViewModel = authViewModel)
            }
        }
    }
}

@Composable
fun MovieCardRow(movie: Movie, authViewModel: AuthViewModel) {
    var isFavorite by remember { mutableStateOf(movie.isFavorite) }
    var isInWatchlist by remember { mutableStateOf(movie.isInWatchlist) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F2F2)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Text("IMAGE", color = Color.DarkGray)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(text = movie.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                Text("Genre: ${movie.genre}", fontSize = 12.sp, color = Color.Black)
                Text("Runtime: ${movie.runtime}", fontSize = 12.sp, color = Color.Black)
                Text("Age Rating: ${movie.ageRating}", fontSize = 12.sp, color = Color.Black)
                Text("Year: ${movie.releaseYear}", fontSize = 12.sp, color = Color.Black)
                Text("IMDB: ${movie.imdbRating}", fontSize = 12.sp, color = Color.Black)
            }

            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End,
                modifier = Modifier.height(120.dp)
            ) {
                IconButton(onClick = {
                    isFavorite = !isFavorite
                    authViewModel.toggleFavorite(movie.id, isFavorite)
                }) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color.Red else Color.Gray
                    )
                }

                IconButton(onClick = {
                    isInWatchlist = !isInWatchlist
                    authViewModel.toggleWatchlist(movie.id, isInWatchlist)
                }) {
                    Icon(
                        imageVector = if (isInWatchlist) Icons.Filled.Check else Icons.Filled.Add,
                        contentDescription = "Add to Watchlist",
                        tint = if (isInWatchlist) Purple40 else Color.Black
                    )
                }
            }
        }
    }
}
