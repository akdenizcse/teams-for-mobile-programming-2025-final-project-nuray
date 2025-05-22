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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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

private val genreResMap = listOf(
    "Action" to R.string.genre_action,
    "Drama"  to R.string.genre_drama,
    "Comedy" to R.string.genre_comedy,
    "Sci-Fi" to R.string.genre_scifi
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: MovieViewModel = viewModel()
) {
    val colors = MaterialTheme.colorScheme
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    var showSearch by rememberSaveable { mutableStateOf(false) }
    var showFilters by rememberSaveable { mutableStateOf(false) }
    var showSortMenu by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedGenres by rememberSaveable { mutableStateOf<Set<String>>(emptySet()) }
    var fromYear by rememberSaveable { mutableStateOf("") }
    var toYear by rememberSaveable { mutableStateOf("") }
    var minRating by rememberSaveable { mutableStateOf("") }
    var maxRating by rememberSaveable { mutableStateOf("") }

    val cdSearch      = stringResource(R.string.cd_search)
    val cdFilter      = stringResource(R.string.cd_filter)
    val cdSort        = stringResource(R.string.cd_sort)
    val cdPrev        = stringResource(R.string.cd_previous)
    val cdNext        = stringResource(R.string.cd_next)
    val cdSettings    = stringResource(R.string.settings_title)
    val homeTitle     = stringResource(R.string.home_title)
    val hintSearch    = stringResource(R.string.hint_search)

    val sortDefault   = stringResource(R.string.sort_default)
    val sortByRating  = stringResource(R.string.sort_by_rating)
    val sortByRelease = stringResource(R.string.sort_by_release)
    val sortOptions   = listOf(sortDefault, sortByRating, sortByRelease)

    val yearLabel     = stringResource(R.string.year_range_label)
    val fromHint      = stringResource(R.string.from_hint)
    val toHint        = stringResource(R.string.to_hint)
    val ratingLabel   = stringResource(R.string.rating_range_label)
    val minHint       = stringResource(R.string.min_hint)
    val maxHint       = stringResource(R.string.max_hint)

    LaunchedEffect(Unit) {
        viewModel.applyFilters()
    }

    LaunchedEffect(searchQuery, selectedGenres, fromYear, toYear, minRating, maxRating) {
        viewModel.applyFilters(
            query     = searchQuery.takeIf { it.isNotBlank() },
            genres    = selectedGenres.takeIf { it.isNotEmpty() },
            startYear = fromYear.takeIf { it.isNotBlank() },
            endYear   = toYear.takeIf { it.isNotBlank() },
            minVote   = minRating.toDoubleOrNull(),
            maxVote   = maxRating.toDoubleOrNull()
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title  = { Text(homeTitle, color = colors.onBackground) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = colors.background),
                actions = {
                    IconButton(onClick = { showSearch = !showSearch }) { Icon(Icons.Filled.Search, cdSearch, tint = colors.primary) }
                    IconButton(onClick = { showFilters = !showFilters }) { Icon(Icons.Filled.FilterList, cdFilter, tint = colors.primary) }
                    IconButton(onClick = { showSortMenu = !showSortMenu }) { Icon(Icons.Filled.Sort, cdSort, tint = colors.primary) }
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { navController.navigate("settings") }) { Icon(Icons.Filled.Settings, cdSettings, tint = colors.primary) }

                    DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                        sortOptions.forEach { option ->
                            DropdownMenuItem(text = { Text(option) }, onClick = {
                                viewModel.selectedSort = option
                                if (option == sortByRating) {
                                    viewModel.applyFilters(
                                        query     = searchQuery.takeIf { it.isNotBlank() },
                                        genres    = selectedGenres.takeIf { it.isNotEmpty() },
                                        startYear = fromYear.takeIf { it.isNotBlank() },
                                        endYear   = toYear.takeIf { it.isNotBlank() },
                                        minVote   = 0.9,
                                        maxVote   = 9.9
                                    )
                                } else {
                                    viewModel.applyFilters(
                                        query     = searchQuery.takeIf { it.isNotBlank() },
                                        genres    = selectedGenres.takeIf { it.isNotEmpty() },
                                        startYear = fromYear.takeIf { it.isNotBlank() },
                                        endYear   = toYear.takeIf { it.isNotBlank() },
                                        minVote   = minRating.toDoubleOrNull(),
                                        maxVote   = maxRating.toDoubleOrNull()
                                    )
                                }
                                showSortMenu = false
                            })
                        }
                    }
                }
            )
        },
        bottomBar = { BottomBar(navController) }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .background(colors.background)
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AnimatedVisibility(showSearch, enter = fadeIn(), exit = fadeOut()) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text(hintSearch, color = colors.onBackground.copy(alpha = .6f)) },
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
                        .background(colors.secondaryContainer, RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(stringResource(R.string.label_genre), fontWeight = FontWeight.Bold, color = colors.onSurface)
                    Row(Modifier.horizontalScroll(scrollState), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        genreResMap.forEach { (tag, resId) ->
                            val label = stringResource(resId)
                            val selected = tag in selectedGenres
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    selectedGenres = if (selected) selectedGenres - tag else selectedGenres + tag
                                },
                                label = { Text(label, color = if (selected) colors.onPrimary else colors.onSurface) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = colors.primary,
                                    selectedLabelColor     = colors.onPrimary,
                                    containerColor         = colors.surfaceVariant,
                                    labelColor             = colors.onSurface
                                )
                            )
                        }
                    }

                    Text(yearLabel, fontWeight = FontWeight.Bold, color = colors.onSurface)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = fromYear,
                            onValueChange = { fromYear = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text(fromHint) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor   = colors.primary,
                                unfocusedBorderColor = colors.primary,
                                cursorColor          = colors.primary
                            )
                        )
                        OutlinedTextField(
                            value = toYear,
                            onValueChange = { toYear = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text(toHint) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor   = colors.primary,
                                unfocusedBorderColor = colors.primary,
                                cursorColor          = colors.primary
                            )
                        )
                    }

                    Text(ratingLabel, fontWeight = FontWeight.Bold, color = colors.onSurface)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = minRating,
                            onValueChange = { minRating = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text(minHint) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor   = colors.primary,
                                unfocusedBorderColor = colors.primary,
                                cursorColor          = colors.primary
                            )
                        )
                        OutlinedTextField(
                            value = maxRating,
                            onValueChange = { maxRating = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text(maxHint) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor   = colors.primary,
                                unfocusedBorderColor = colors.primary,
                                cursorColor          = colors.primary
                            )
                        )
                    }
                }
            }

            LazyColumn(
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(viewModel.movies) { movie ->
                    MovieCard(movie, viewModel, scope)
                }
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.prevPage() }) {
                    Icon(Icons.Filled.ArrowBack, cdPrev, tint = colors.primary)
                }
                Text(
                    text = stringResource(R.string.page_label, viewModel.currentPage, viewModel.totalPages),
                    color = colors.primary,
                    fontSize = 14.sp
                )
                IconButton(onClick = { viewModel.nextPage() }) {
                    Icon(Icons.Filled.ArrowForward, cdNext, tint = colors.primary)
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
        modifier  = Modifier
            .fillMaxWidth()
            .height(160.dp),
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
                modifier           = Modifier
                    .width(120.dp)
                    .fillMaxHeight(),
                contentScale       = ContentScale.Crop
            )
            Column(
                modifier            = Modifier
                    .weight(1f)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(movie.title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = colors.onSurface)
                Text(stringResource(R.string.release_label, movie.releaseDate), fontSize = 14.sp, color = colors.onSurface)
                Text(stringResource(R.string.imdb_label, movie.rating), fontSize = 14.sp, color = colors.onSurface)
                val genreText = movie.getGenreNames()
                    .split(", ")
                    .map { tag ->
                        genreResMap.firstOrNull { it.first == tag }
                            ?.second
                            ?.let { resId -> stringResource(resId) }
                            ?: tag
                    }
                    .joinToString(", ")
                Text(genreText, fontSize = 14.sp, color = colors.onSurface)
            }
            Column(
                modifier            = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                IconButton(onClick = {
                    coroutineScope.launch { viewModel.toggleFavorite(movie.id.toString(), !isFav) }
                }) {
                    Icon(
                        if (isFav) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        tint               = if (isFav) Color.Red else tint,
                        contentDescription = stringResource(R.string.cd_favorite)
                    )
                }
                IconButton(onClick = {
                    coroutineScope.launch { viewModel.toggleWatchlist(movie.id.toString(), !isWatch) }
                }) {
                    Icon(
                        if (isWatch) Icons.Filled.Check else Icons.Filled.Add,
                        tint               = if (isWatch) Color.Green else tint,
                        contentDescription = stringResource(R.string.cd_watchlist)
                    )
                }
            }
        }
    }
}
