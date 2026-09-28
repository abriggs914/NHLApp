package com.avery.nhl.ui.collection

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil3.compose.AsyncImage
import com.avery.nhl.model.jersey.CollectionFilters
import com.avery.nhl.model.jersey.JerseySort
import com.avery.nhl.model.jersey.SortMode
import com.avery.nhl.model.jersey.SortRule
import com.avery.nhl.data.collection.JerseyRepository
import com.avery.nhl.model.jersey.Jersey
import com.avery.nhl.model.jersey.filterJerseys
import com.avery.nhl.model.jersey.sortJerseys
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun CollectionScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var showOverview by remember {
        mutableStateOf(false)
    }

    val jerseyRepository = remember(context) {
        JerseyRepository(context)
    }

    val collectionGridState =
        rememberLazyGridState()

    val collectionScope =
        rememberCoroutineScope()

    val jerseys = remember {
        jerseyRepository.loadJerseys()
    }

    var filters by remember {
        mutableStateOf(CollectionFilters())
    }

    var sortRules by remember {
        mutableStateOf(
            listOf(
                SortRule(
                    field = JerseySort.ID,
                    ascending = true
                )
            )
        )
    }

    var sortMode by remember {
        mutableStateOf(SortMode.ORDERED)
    }

    var randomSeed by remember {
        mutableStateOf(0)
    }

    var selectedJerseyId by remember {
        mutableStateOf<Int?>(null)
    }

    val displayedJerseys = remember(
        jerseys,
        filters,
        sortRules,
        sortMode,
        randomSeed
    ) {
        sortJerseys(
            jerseys = filterJerseys(
                jerseys,
                filters
            ),
            sortRules = sortRules,
            sortMode = sortMode,
            randomSeed = randomSeed
        )
    }

    val selectedJersey =
        displayedJerseys.firstOrNull {
            it.id == selectedJerseyId
        }

    var hasImagePermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= 33) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            }
        )
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { granted ->
            hasImagePermission = granted
        }

    LaunchedEffect(Unit) {
        if (!hasImagePermission) {

            val permission =
                if (Build.VERSION.SDK_INT >= 33) {
                    Manifest.permission.READ_MEDIA_IMAGES
                } else {
                    Manifest.permission.READ_EXTERNAL_STORAGE
                }

            permissionLauncher.launch(permission)
        }
    }

    val imageUris = remember(
        hasImagePermission
    ) {
        if (hasImagePermission) {
            jerseyRepository.loadImageUris()
        } else {
            emptyMap()
        }
    }

    if (selectedJersey != null) {

        JerseyDetailScreen(
            jersey = selectedJersey,

            // Previous/Next follows current filters + sorting
            sequence = displayedJerseys,

            // Related links search the complete collection
            allJerseys = jerseys,

            imageUris = imageUris,

            onBack = {
                selectedJerseyId = null
            },

            onSelectJersey = { jersey ->
                selectedJerseyId = jersey.id
            },

            onShowTeam = { team ->
                filters = filters.copy(
                    teams = setOf(team)
                )
                selectedJerseyId = null
            },

            onShowPlayer = { player ->
                filters = filters.copy(
                    players = setOf(player)
                )
                selectedJerseyId = null
            },

            onShowNumber = { number ->
                filters = filters.copy(
                    numbers = setOf(number)
                )
                selectedJerseyId = null
            },

            modifier = modifier
        )

        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Button(
            onClick = onBack
        ) {
            Text("Back")
        }

        Text(
            text = "Jersey Collection",
            fontSize = 28.sp,
            modifier = Modifier.padding(
                top = 20.dp,
                bottom = 4.dp
            )
        )

        Text(
            text =
                if (displayedJerseys.size == jerseys.size) {
                    "${jerseys.size} jerseys"
                } else {
                    "${displayedJerseys.size} of ${jerseys.size} jerseys"
                },
            modifier = Modifier.padding(
                bottom = 16.dp
            )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = 20.dp,
                    bottom = 4.dp
                ),
            horizontalArrangement =
                Arrangement.SpaceBetween,
            verticalAlignment =
                Alignment.CenterVertically
        ) {

//            Text(
//                text = "Jersey Collection",
//                fontSize = 28.sp
//            )

            OutlinedButton(
                onClick = {
                    showOverview = !showOverview
                }
            ) {
                Text(
                    if (showOverview) {
                        "Grid"
                    } else {
                        "Overview"
                    }
                )
            }
        }

        CollectionFilterPanel(
            jerseys = jerseys,
            filters = filters,
            sortRules = sortRules,
            sortMode = sortMode,

            onApply = {
                    newFilters,
                    newSortRules,
                    newSortMode ->

                filters = newFilters
                sortRules = newSortRules
                sortMode = newSortMode

                if (
                    newSortMode ==
                    SortMode.RANDOM
                ) {
                    randomSeed =
                        Random.nextInt()
                }
            }
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        if (showOverview) {

            JerseyOverviewGrid(
                jerseys = displayedJerseys,
                imageUris = imageUris,

                onJerseySelected = { jersey ->

                    val index =
                        displayedJerseys.indexOfFirst {
                            it.id == jersey.id
                        }

                    if (index >= 0) {

                        showOverview = false

                        collectionScope.launch {
                            collectionGridState
                                .animateScrollToItem(index)
                        }
                    }
                }
            )

        } else {

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),

                state = collectionGridState,

                horizontalArrangement =
                    Arrangement.spacedBy(12.dp),

                verticalArrangement =
                    Arrangement.spacedBy(12.dp),

                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {

                items(
                    items = displayedJerseys,
                    key = { jersey -> jersey.id }
                ) { jersey ->

                    JerseyCard(
                        jersey = jersey,
                        imageUris = imageUris,
                        onClick = {
                            selectedJerseyId = jersey.id
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun JerseyOverviewGrid(
    jerseys: List<Jersey>,
    imageUris: Map<String, Uri>,
    onJerseySelected: (Jersey) -> Unit
) {

    LazyVerticalGrid(
        columns = GridCells.Fixed(8),

        horizontalArrangement =
            Arrangement.spacedBy(2.dp),

        verticalArrangement =
            Arrangement.spacedBy(2.dp),

        modifier = Modifier.fillMaxSize()
    ) {

        items(
            items = jerseys,
            key = { jersey -> jersey.id }
        ) { jersey ->

            JerseyOverviewItem(
                jersey = jersey,
                imageUris = imageUris,
                onClick = {
                    onJerseySelected(jersey)
                }
            )
        }
    }
}

@Composable
fun JerseyOverviewItem(
    jersey: Jersey,
    imageUris: Map<String, Uri>,
    onClick: () -> Unit
) {

    val imageUri =
        jersey.images
            .firstNotNullOfOrNull { fileName ->
                imageUris[fileName]
            }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clickable {
                onClick()
            },
        contentAlignment =
            Alignment.Center
    ) {

        if (imageUri != null) {

            AsyncImage(
                model = imageUri,

                contentDescription =
                    "${jersey.team} ${jersey.playerName ?: ""}",

                contentScale =
                    ContentScale.Crop,

                modifier =
                    Modifier.fillMaxSize()
            )

        } else {

            Text(
                text =
                    jersey.id
                        .toString()
                        .padStart(3, '0'),

                fontSize = 9.sp
            )
        }
    }
}