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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.watchlist.model.MovieItem
import com.example.watchlist.viewmodel.FavoritesViewModel
import com.example.watchlist.viewmodel.MovieViewModel
import kotlinx.coroutines.CoroutineScope

private val DarkNavy       = Color(0xFF0A1D37)
private val LightGrayBlue  = Color(0xFFA5ABBD)
private val MediumGrayBlue = Color(0xFF717788)
private val White          = Color(0xFFFFFFFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    navController: NavHostController,
    favoritesViewModel: FavoritesViewModel = viewModel(),
    movieViewModel: MovieViewModel       = viewModel()
) {
    var showSearch   by rememberSaveable { mutableStateOf(false) }
    var showFilters  by rememberSaveable { mutableStateOf(false) }
    var query        by rememberSaveable { mutableStateOf("") }
    var genre        by rememberSaveable { mutableStateOf<String?>(null) }
    var startYear    by rememberSaveable { mutableStateOf("") }
    var endYear      by rememberSaveable { mutableStateOf("") }
    var minRating    by rememberSaveable { mutableStateOf("") }
    var maxRating    by rememberSaveable { mutableStateOf("") }
    var currentPage  by rememberSaveable { mutableStateOf(1) }
    val pageSize     = 20

    val allMovies    = favoritesViewModel.favoriteMovies
    val scrollState  = rememberScrollState()
    val scope: CoroutineScope = rememberCoroutineScope()

    val filtered = remember(allMovies, query, genre, startYear, endYear, minRating, maxRating) {
        allMovies
            .filter { it.title.contains(query, ignoreCase = true) }
            .filter { genre?.let { g -> it.getGenreNames().contains(g) } ?: true }
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
            .filter {
                minRating.toDoubleOrNull()?.let { mr ->
                    it.rating >= mr
                } ?: true
            }
            .filter {
                maxRating.toDoubleOrNull()?.let { xr ->
                    it.rating <= xr
                } ?: true
            }
    }


    val totalPages = (filtered.size + pageSize - 1) / pageSize
    if (currentPage > totalPages && totalPages > 0) currentPage = totalPages
    val pageItems = filtered.drop((currentPage - 1) * pageSize).take(pageSize)

    Scaffold(
        containerColor = DarkNavy,
        bottomBar      = { BottomBar(navController) }
    ) { paddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                .background(DarkNavy)
                .padding(paddingValues)
                .padding(16.dp)
        ) {

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = { showSearch = !showSearch }) {
                    Icon(Icons.Filled.Search, contentDescription = "Search", tint = LightGrayBlue)
                }
                IconButton(onClick = { showFilters = !showFilters }) {
                    Icon(Icons.Filled.FilterList, contentDescription = "Filters", tint = LightGrayBlue)
                }
            }


            AnimatedVisibility(showSearch, enter = fadeIn(), exit = fadeOut()) {
                OutlinedTextField(
                    value         = query,
                    onValueChange = { query = it; currentPage = 1 },
                    modifier      = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    singleLine    = true,
                    textStyle     = TextStyle(color = White),
                    placeholder   = { Text("Search favorites…", color = White) }
                )
            }


            AnimatedVisibility(showFilters, enter = fadeIn(), exit = fadeOut()) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .border(1.dp, LightGrayBlue, RoundedCornerShape(12.dp))
                        .background(MediumGrayBlue, RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    Text("Genre", fontWeight = FontWeight.Bold, color = White)
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .horizontalScroll(scrollState),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Action", "Drama", "Comedy", "Sci-Fi").forEach { g ->
                            val sel = genre == g
                            FilterChip(
                                selected = sel,
                                onClick  = { genre = if (sel) null else g; currentPage = 1 },
                                label    = { Text(g, color = if (sel) Color.Black else White) },
                                colors   = FilterChipDefaults.filterChipColors(
                                    containerColor = if (sel) LightGrayBlue else DarkNavy,
                                    labelColor     = if (sel) Color.Black     else White
                                )
                            )
                        }
                    }


                    Text("Year Range", fontWeight = FontWeight.Bold, color = White)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value         = startYear,
                            onValueChange = { startYear = it; currentPage = 1 },
                            modifier      = Modifier.weight(1f),
                            singleLine    = true,
                            textStyle     = TextStyle(color = Color.Black),
                            label         = { Text("From (YYYY)", color = Color.Black) }
                        )
                        OutlinedTextField(
                            value         = endYear,
                            onValueChange = { endYear = it; currentPage = 1 },
                            modifier      = Modifier.weight(1f),
                            singleLine    = true,
                            textStyle     = TextStyle(color = Color.Black),
                            label         = { Text("To (YYYY)", color = Color.Black) }
                        )
                    }

                    Text("Rating Range", fontWeight = FontWeight.Bold, color = White)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value         = minRating,
                            onValueChange = { minRating = it; currentPage = 1 },
                            modifier      = Modifier.weight(1f),
                            singleLine    = true,
                            textStyle     = TextStyle(color = Color.Black),
                            label         = { Text("Min", color = Color.Black) }
                        )
                        OutlinedTextField(
                            value         = maxRating,
                            onValueChange = { maxRating = it; currentPage = 1 },
                            modifier      = Modifier.weight(1f),
                            singleLine    = true,
                            textStyle     = TextStyle(color = Color.Black),
                            label         = { Text("Max", color = Color.Black) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))


            LazyColumn(
                Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(pageItems) { movie: MovieItem ->
                    MovieCard(movie, movieViewModel, scope)
                }
            }


            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { if (currentPage > 1) currentPage-- },
                    enabled = currentPage > 1
                ) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Prev", tint = LightGrayBlue)
                }
                Text(
                    "Page $currentPage / ${if (totalPages >= 1) totalPages else 1}",
                    color    = LightGrayBlue,
                    fontSize = 14.sp
                )
                IconButton(
                    onClick = { if (currentPage < totalPages) currentPage++ },
                    enabled = currentPage < totalPages
                ) {
                    Icon(Icons.Filled.ArrowForward, contentDescription = "Next", tint = LightGrayBlue)
                }
            }
        }
    }
}
