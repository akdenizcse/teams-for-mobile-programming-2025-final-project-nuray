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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.watchlist.model.MovieItem
import com.example.watchlist.viewmodel.FavoritesViewModel
import com.example.watchlist.viewmodel.MovieViewModel
import kotlinx.coroutines.launch


private val genreResMap = listOf(
    "Action" to R.string.genre_action,
    "Drama"  to R.string.genre_drama,
    "Comedy" to R.string.genre_comedy,
    "Sci-Fi" to R.string.genre_scifi
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    navController: NavHostController,
    favoritesViewModel: FavoritesViewModel = viewModel(),
    movieViewModel: MovieViewModel       = viewModel()
) {
    // Compose calls inside
    val cdSearch    = stringResource(R.string.cd_search)
    val cdFilter    = stringResource(R.string.cd_filter)
    val cdSettings  = stringResource(R.string.settings_title)
    val hintFav     = stringResource(R.string.hint_search) // reuse
    val labelGenre  = stringResource(R.string.label_genre)
    val yearLabel   = stringResource(R.string.year_range_label)
    val ratingLabel = stringResource(R.string.rating_range_label)
    val fromHint    = stringResource(R.string.from_hint)
    val toHint      = stringResource(R.string.to_hint)
    val minHint     = stringResource(R.string.min_hint)
    val maxHint     = stringResource(R.string.max_hint)
    val pageFmt     = stringResource(R.string.page_label)

    val colors    = MaterialTheme.colorScheme
    val filterBg  = colors.secondaryContainer
    val chipBg    = colors.surfaceVariant
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
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = { showSearch = !showSearch }) {
                        Icon(Icons.Filled.Search, contentDescription = cdSearch, tint = tint)
                    }
                    IconButton(onClick = { showFilters = !showFilters }) {
                        Icon(Icons.Filled.FilterList, contentDescription = cdFilter, tint = tint)
                    }
                }
                IconButton(onClick = { navController.navigate("settings") }) {
                    Icon(Icons.Filled.Settings, contentDescription = cdSettings, tint = tint)
                }
            }

            AnimatedVisibility(showSearch, enter = fadeIn(), exit = fadeOut()) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it; currentPage = 1 },
                    modifier    = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    singleLine  = true,
                    textStyle   = TextStyle(color = colors.onBackground),
                    placeholder = { Text(hintFav, color = colors.onBackground.copy(alpha = .6f)) },
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
                    Text(labelGenre, fontWeight = FontWeight.Bold, color = colors.onSurface)
                    Row(Modifier.horizontalScroll(scrollState), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        genreResMap.forEach { (tag, resId) ->
                            val label = stringResource(resId)
                            val sel   = genres.contains(tag)
                            FilterChip(
                                selected = sel,
                                onClick  = { genres = if (sel) genres - tag else genres + tag; currentPage = 1 },
                                label    = { Text(label, color = if (sel) colors.onPrimary else colors.onSurface) },
                                colors   = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = tint,
                                    selectedLabelColor     = colors.onPrimary,
                                    containerColor         = chipBg,
                                    labelColor             = colors.onSurface
                                )
                            )
                        }
                    }

                    Text(yearLabel, fontWeight = FontWeight.Bold, color = colors.onSurface)
                    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = startYear,
                            onValueChange = { startYear = it; currentPage = 1 },
                            modifier    = Modifier.weight(1f),
                            singleLine  = true,
                            textStyle   = TextStyle(color = colors.onSurface),
                            placeholder = { Text(fromHint, color = colors.onSurface.copy(alpha = .6f)) },
                            colors      = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor   = tint,
                                unfocusedBorderColor = tint,
                                cursorColor          = tint
                            )
                        )
                        OutlinedTextField(
                            value = endYear,
                            onValueChange = { endYear = it; currentPage = 1 },
                            modifier    = Modifier.weight(1f),
                            singleLine  = true,
                            textStyle   = TextStyle(color = colors.onSurface),
                            placeholder = { Text(toHint, color = colors.onSurface.copy(alpha = .6f)) },
                            colors      = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor   = tint,
                                unfocusedBorderColor = tint,
                                cursorColor          = tint
                            )
                        )
                    }

                    Text(ratingLabel, fontWeight = FontWeight.Bold, color = colors.onSurface)
                    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = minRating,
                            onValueChange = { minRating = it; currentPage = 1 },
                            modifier    = Modifier.weight(1f),
                            singleLine  = true,
                            textStyle   = TextStyle(color = colors.onSurface),
                            placeholder = { Text(minHint, color = colors.onSurface.copy(alpha = .6f)) },
                            colors      = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor   = tint,
                                unfocusedBorderColor = tint,
                                cursorColor          = tint
                            )
                        )
                        OutlinedTextField(
                            value = maxRating,
                            onValueChange = { maxRating = it; currentPage = 1 },
                            modifier    = Modifier.weight(1f),
                            singleLine  = true,
                            textStyle   = TextStyle(color = colors.onSurface),
                            placeholder = { Text(maxHint, color = colors.onSurface.copy(alpha = .6f)) },
                            colors      = OutlinedTextFieldDefaults.colors(
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { if (currentPage > 1) currentPage-- }, enabled = currentPage > 1) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = tint)
                }
                Text(
                    text = pageFmt.format(currentPage, totalPages),
                    color = tint,
                    fontSize = 14.sp
                )
                IconButton(onClick = { if (currentPage < totalPages) currentPage++ }, enabled = currentPage < totalPages) {
                    Icon(Icons.Filled.ArrowForward, contentDescription = null, tint = tint)
                }
            }
        }
    }
}
