package com.avery.nhl

import coil3.compose.AsyncImage
import java.util.Locale
import java.net.URL
import kotlin.random.Random
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

import android.os.Bundle
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.content.ContentUris
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build

import androidx.core.content.ContextCompat
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.TextButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Checkbox
import androidx.compose.material3.RadioButton
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.material3.IconButton

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope

import com.avery.nhl.ui.theme.NHLTheme


// ============================================

val monthNames = listOf(
    "January",
    "February",
    "March",
    "April",
    "May",
    "June",
    "July",
    "August",
    "September",
    "October",
    "November",
    "December"
)

data class Jersey(
    val id: Int,
    val league: String?,
    val team: String,
    val number: Int?,
    val playerFirst: String?,
    val playerLast: String?,

    val brand: String?,
    val model: String?,
    val supplier: String?,

    val orderDate: String?,
    val receiveDate: String?,
    val openDate: String?,

    val colour1: String?,
    val colour2: String?,
    val colour3: String?,
    val size: String?,

    val images: List<String>,

    // Keeps every field from collections.json
    val details: Map<String, String?>
) {
    val playerName: String?
        get() {
            val parts = listOfNotNull(
                playerFirst?.takeIf { it.isNotBlank() },
                playerLast?.takeIf { it.isNotBlank() }
            )

            return parts
                .joinToString(" ")
                .takeIf { it.isNotBlank() }
        }

    val hasPlayer: Boolean
        get() = !playerName.isNullOrBlank()

    val colours: List<String>
        get() = listOfNotNull(
            colour1,
            colour2,
            colour3
        )
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()

    val nhlId: Long?
        get() =
            details["NHLID"]
                ?.toDoubleOrNull()
                ?.toLong()
}

fun Jersey.receiveMonth(): String? {
    return receiveDate
        ?.takeIf { it.length >= 7 }
        ?.substring(0, 7)
}

data class CollectionFilters(
    val leagues: Set<String> = emptySet(),
    val teams: Set<String> = emptySet(),
    val players: Set<String> = emptySet(),
    val sizes: Set<String> = emptySet(),
    val brands: Set<String> = emptySet(),
    val models: Set<String> = emptySet(),
    val numbers: Set<Int> = emptySet(),
    val suppliers: Set<String> = emptySet(),
    val colours: Set<String> = emptySet(),

    val jerseyType: JerseyType = JerseyType.ALL,
    val imageFilter: ImageFilter = ImageFilter.ALL,

    val dob: MonthYearFilter = MonthYearFilter(),
    val orderDate: MonthYearFilter = MonthYearFilter(),
    val receiveDate: MonthYearFilter = MonthYearFilter(),
    val openDate: MonthYearFilter = MonthYearFilter()
)

data class MonthYearFilter(
    val month: Int? = null,
    val year: Int? = null
)

enum class SortOrder {
    ASCENDING,
    DESCENDING,
    RANDOM
}

enum class JerseyType {
    ALL,
    PLAYER,
    BLANK
}

enum class ImageFilter {
    ALL,
    HAS_IMAGES,
    NO_IMAGES
}

data class SortRule(
    val field: JerseySort,
    val ascending: Boolean = true
)

enum class SortMode {
    ORDERED,
    RANDOM
}

enum class JerseySort {
    ID,
    LEAGUE,
    TEAM,
    PLAYER,
    NUMBER,
    BRAND,
    MODEL,
    SUPPLIER,
    SIZE,

    COLOUR1,
    COLOUR2,
    COLOUR3,
    COLOURS,

    DOB,
    ORDER_DATE,
    RECEIVE_DATE,
    OPEN_DATE,

    IMAGE_COUNT
}

data class NhlPlayerProfile(
    val playerId: Long,
    val firstName: String,
    val lastName: String,

    val isActive: Boolean,
    val inHallOfFame: Boolean,

    val currentTeamAbbrev: String?,
    val teamLogo: String?,
    val headshot: String?,

    val position: String?,
    val sweaterNumber: Int?,

    val seasonStats: List<NhlSeasonStats>
) {
    val fullName: String
        get() = "$firstName $lastName".trim()
}

data class NhlSeasonStats(
    val season: Int,
    val teamName: String,
    val teamAbbrev: String?,
    val league: String,
    val gameTypeId: Int,

    val gamesPlayed: Int = 0,

    // Skaters
    val goals: Int = 0,
    val assists: Int = 0,
    val points: Int = 0,
    val plusMinus: Int = 0,
    val pim: Int = 0,

    // Goalies
    val wins: Int = 0,
    val losses: Int = 0,
    val otLosses: Int = 0,
    val shutouts: Int = 0,
    val savePctg: Double? = null,
    val goalsAgainstAverage: Double? = null
)

data class NhlTeamStats(
    val teamAbbrev: String?,
    val teamName: String,
    val season: NhlSeasonStats,

    val gamesPlayed: Int,

    val goals: Int,
    val assists: Int,
    val points: Int,
    val plusMinus: Int,
    val pim: Int,

    val wins: Int,
    val losses: Int,
    val otLosses: Int,
    val shutouts: Int
)

// ============================================

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            NHLTheme {
                var currentScreen by remember {
                    mutableStateOf("home")
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->

                    when (currentScreen) {
                        "home" -> HomeScreen(
                            onBrowseCollection = {
                                currentScreen = "collection"
                            },
                            modifier = Modifier.padding(innerPadding)
                        )

                        "collection" -> CollectionScreen(
                            onBack = {
                                currentScreen = "home"
                            },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}

fun loadImageUris(context: Context): Map<String, Uri> {

    val imageMap = mutableMapOf<String, Uri>()

    val collection =
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI

    val projection = arrayOf(
        MediaStore.Images.Media._ID,
        MediaStore.Images.Media.DISPLAY_NAME
    )

    context.contentResolver.query(
        collection,
        projection,
        null,
        null,
        null
    )?.use { cursor ->

        val idColumn = cursor.getColumnIndexOrThrow(
            MediaStore.Images.Media._ID
        )

        val nameColumn = cursor.getColumnIndexOrThrow(
            MediaStore.Images.Media.DISPLAY_NAME
        )

        while (cursor.moveToNext()) {

            val id = cursor.getLong(idColumn)
            val fileName = cursor.getString(nameColumn)

            val uri = ContentUris.withAppendedId(
                collection,
                id
            )

            imageMap[fileName] = uri
        }
    }

    return imageMap
}

fun org.json.JSONObject.optNullableString(
    name: String
): String? {

    if (!has(name) || isNull(name)) {
        return null
    }

    return optString(name)
        .takeIf { it.isNotBlank() }
}

fun loadJerseysFromAssets(context: Context): List<Jersey> {

    val jsonText = context.assets
        .open("collections.json")
        .bufferedReader()
        .use { it.readText() }

    val jsonArray = JSONArray(jsonText)

    val jerseys = mutableListOf<Jersey>()

    for (i in 0 until jsonArray.length()) {

        val obj = jsonArray.getJSONObject(i)

        val images = mutableListOf<String>()

        val imageArray = obj.optJSONArray("images")

        if (imageArray != null) {
            for (j in 0 until imageArray.length()) {
                images.add(imageArray.getString(j))
            }
        }

        val number =
            if (obj.isNull("Number")) {
                null
            } else {
                obj.optDouble("Number").toInt()
            }

        val details = mutableMapOf<String, String?>()

        val keys = obj.keys()

        while (keys.hasNext()) {
            val key = keys.next()

            if (key != "images") {
                details[key] =
                    if (obj.isNull(key)) {
                        null
                    } else {
                        obj.get(key).toString()
                    }
            }
        }

        jerseys.add(
            Jersey(
                id = obj.getInt("ID"),
                league = obj.optNullableString("League"),
                team = obj.optNullableString("Team")
                    ?: "Unknown Team",
                number =
                    if (obj.isNull("Number")) null
                    else obj.optDouble("Number").toInt(),
                playerFirst =
                    obj.optNullableString("PlayerFirst"),
                playerLast =
                    obj.optNullableString("PlayerLast"),
                brand =
                    obj.optNullableString("Brand"),
                model =
                    obj.optNullableString("Model"),
                supplier =
                    obj.optNullableString("Supplier"),
                orderDate =
                    obj.optNullableString("OrderDate"),
                receiveDate =
                    obj.optNullableString("ReceiveDate"),
                openDate =
                    obj.optNullableString("OpenDate"),
                colour1 =
                    obj.optNullableString("Colour1"),
                colour2 =
                    obj.optNullableString("Colour2"),
                colour3 =
                    obj.optNullableString("Colour3"),
                size =
                    obj.optNullableString("Size"),
                images = images,
                details = details
            )
        )
    }

    return jerseys
}

@Composable
fun HomeScreen(
    onBrowseCollection: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "My Hockey Collection",
            fontSize = 28.sp
        )

        Button(
            onClick = onBrowseCollection,
            modifier = Modifier.padding(top = 24.dp)
        ) {
            Text("Browse Collection")
        }
    }
}

@Composable
fun CollectionScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var showOverview by remember {
        mutableStateOf(false)
    }

    val collectionGridState =
        rememberLazyGridState()

    val collectionScope =
        rememberCoroutineScope()

    val jerseys = remember {
        loadJerseysFromAssets(context)
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

    val imageUris = remember(hasImagePermission) {
        if (hasImagePermission) {
            loadImageUris(context)
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
//
//        LazyVerticalGrid(
//            columns =
//                GridCells.Fixed(2),
//            horizontalArrangement =
//                Arrangement.spacedBy(12.dp),
//            verticalArrangement =
//                Arrangement.spacedBy(12.dp),
//            modifier =
//                Modifier.fillMaxWidth().weight(1f)
//        ) {
//            items(
//                items = displayedJerseys,
//                key = { jersey -> jersey.id }
//            ) { jersey ->
//
//                JerseyCard(
//                    jersey = jersey,
//                    imageUris = imageUris,
//                    onClick = {
//                        selectedJerseyId = jersey.id
//                    }
//                )
//            }
//        }
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

@Composable
fun CollectionFilterPanel(
    jerseys: List<Jersey>,
    filters: CollectionFilters,
    sortRules: List<SortRule>,
    sortMode: SortMode,

    onApply: (
        CollectionFilters,
        List<SortRule>,
        SortMode
    ) -> Unit
) {
    var expanded by remember {
        mutableStateOf(false)
    }

    var draftFilters by remember(filters) {
        mutableStateOf(filters)
    }

    var draftSortRules by remember(sortRules) {
        mutableStateOf(sortRules)
    }

    var draftSortMode by remember(sortMode) {
        mutableStateOf(sortMode)
    }

    val leagues =
        jerseys.mapNotNull { it.league }
            .distinct()
            .sorted()

    val teams =
        jerseys.map { it.team }
            .distinct()
            .sorted()

    val players =
        jerseys.mapNotNull { it.playerName }
            .distinct()
            .sorted()

    val sizes =
        jerseys.mapNotNull { it.size }
            .distinct()
            .sorted()

    val brands =
        jerseys.mapNotNull { it.brand }
            .distinct()
            .sorted()

    val models =
        jerseys.mapNotNull { it.model }
            .distinct()
            .sorted()

    val suppliers =
        jerseys.mapNotNull { it.supplier }
            .distinct()
            .sorted()

    val colours =
        jerseys
            .flatMap {
                it.colours
            }
            .distinct()
            .sorted()

    val numbers =
        jerseys.mapNotNull { it.number }
            .distinct()
            .sorted()

    fun yearsFromDates(
        dates: List<String?>
    ): List<Int> {

        return dates
            .mapNotNull {
                extractDateParts(it)?.year
            }
            .distinct()
            .sortedDescending()
    }

    val dobYears =
        yearsFromDates(
            jerseys.map {
                it.details["DOB"]
            }
        )

    val orderYears =
        yearsFromDates(
            jerseys.map {
                it.orderDate
            }
        )

    val receiveYears =
        yearsFromDates(
            jerseys.map {
                it.receiveDate
            }
        )

    val openYears =
        yearsFromDates(
            jerseys.map {
                it.openDate
            }
        )

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.SpaceBetween
        ) {

            Button(
                onClick = {
                    expanded = !expanded
                }
            ) {
                Text(
                    if (expanded)
                        "Hide Filters"
                    else
                        "Filters"
                )
            }

            OutlinedButton(
                onClick = {
                    draftFilters =
                        CollectionFilters()

                    draftSortRules =
                        listOf(
                            SortRule(
                                field = JerseySort.ID,
                                ascending = true
                            )
                        )

                    draftSortMode =
                        SortMode.ORDERED

                    onApply(
                        draftFilters,
                        draftSortRules,
                        draftSortMode
                    )
                }
            ) {
                Text("Clear")
            }
        }

        if (expanded) {

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {

                Column(
                    modifier = Modifier.padding(12.dp)
                ) {

                    // Scroll ONLY the controls.
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 330.dp)
                            .verticalScroll(
                                rememberScrollState()
                            ),
                        verticalArrangement =
                            Arrangement.spacedBy(8.dp)
                    ) {

                        MultiSelectDropdown(
                            label = "League",
                            selectedValues =
                                draftFilters.leagues,
                            options = leagues,
                            onSelectionChanged = {
                                draftFilters =
                                    draftFilters.copy(
                                        leagues = it
                                    )
                            }
                        )

                        MultiSelectDropdown(
                            label = "Team",
                            selectedValues =
                                draftFilters.teams,
                            options = teams,
                            onSelectionChanged = {
                                draftFilters =
                                    draftFilters.copy(
                                        teams = it
                                    )
                            }
                        )

                        MultiSelectDropdown(
                            label = "Player",
                            selectedValues =
                                draftFilters.players,
                            options = players,
                            onSelectionChanged = {
                                draftFilters =
                                    draftFilters.copy(
                                        players = it
                                    )
                            }
                        )

                        MultiSelectDropdown(
                            label = "Size",
                            selectedValues =
                                draftFilters.sizes,
                            options = sizes,
                            onSelectionChanged = {
                                draftFilters =
                                    draftFilters.copy(
                                        sizes = it
                                    )
                            }
                        )

                        MultiSelectDropdown(
                            label = "Brand",
                            selectedValues =
                                draftFilters.brands,
                            options = brands,
                            onSelectionChanged = {
                                draftFilters =
                                    draftFilters.copy(
                                        brands = it
                                    )
                            }
                        )

                        MultiSelectDropdown(
                            label = "Style / Model",
                            selectedValues =
                                draftFilters.models,
                            options = models,
                            onSelectionChanged = {
                                draftFilters =
                                    draftFilters.copy(
                                        models = it
                                    )
                            }
                        )

                        MultiSelectDropdown(
                            label = "Supplier",
                            selectedValues =
                                draftFilters.suppliers,
                            options = suppliers,
                            onSelectionChanged = {
                                draftFilters =
                                    draftFilters.copy(
                                        suppliers = it
                                    )
                            }
                        )

                        MultiSelectDropdown(
                            label = "Colours",

                            selectedValues =
                                draftFilters.colours,

                            options =
                                colours,

                            onSelectionChanged = {

                                draftFilters =
                                    draftFilters.copy(
                                        colours = it
                                    )
                            }
                        )

                        MultiSelectDropdown(
                            label = "Number",
                            selectedValues =
                                draftFilters.numbers
                                    .map { it.toString() }
                                    .toSet(),
                            options =
                                numbers.map {
                                    it.toString()
                                },
                            onSelectionChanged = {
                                draftFilters =
                                    draftFilters.copy(
                                        numbers =
                                            it.mapNotNull {
                                                    value ->
                                                value.toIntOrNull()
                                            }.toSet()
                                    )
                            }
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(
                                vertical = 8.dp
                            )
                        )

                        Text(
                            text = "Date Filters",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        MonthYearFilterControl(
                            label = "DOB",
                            filter = draftFilters.dob,
                            availableYears = dobYears,
                            onChanged = {
                                draftFilters =
                                    draftFilters.copy(
                                        dob = it
                                    )
                            }
                        )

                        MonthYearFilterControl(
                            label = "Order Date",
                            filter = draftFilters.orderDate,
                            availableYears = orderYears,
                            onChanged = {
                                draftFilters =
                                    draftFilters.copy(
                                        orderDate = it
                                    )
                            }
                        )

                        MonthYearFilterControl(
                            label = "Receive Date",
                            filter = draftFilters.receiveDate,
                            availableYears = receiveYears,
                            onChanged = {
                                draftFilters =
                                    draftFilters.copy(
                                        receiveDate = it
                                    )
                            }
                        )

                        MonthYearFilterControl(
                            label = "Open Date",
                            filter = draftFilters.openDate,
                            availableYears = openYears,
                            onChanged = {
                                draftFilters =
                                    draftFilters.copy(
                                        openDate = it
                                    )
                            }
                        )

                        // Keep your existing
                        // Jersey Type radio buttons here.

                        // Keep your existing
                        // Image Filter radio buttons here.

                        HorizontalDivider(
                            modifier = Modifier.padding(
                                vertical = 8.dp
                            )
                        )

                        Text(
                            text = "Sort Priority",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        draftSortRules.forEachIndexed { index, rule ->

                            SortRuleControl(
                                priority = index + 1,
                                rule = rule,

                                availableFields =
                                    JerseySort.entries,

                                onChanged = { updatedRule ->

                                    draftSortRules =
                                        draftSortRules
                                            .toMutableList()
                                            .also {
                                                it[index] =
                                                    updatedRule
                                            }
                                },

                                onRemove = {

                                    draftSortRules =
                                        draftSortRules
                                            .toMutableList()
                                            .also {
                                                it.removeAt(index)
                                            }
                                }
                            )
                        }

                        OutlinedButton(
                            onClick = {

                                val usedFields =
                                    draftSortRules
                                        .map { it.field }
                                        .toSet()

                                val nextField =
                                    JerseySort.entries
                                        .firstOrNull {
                                            it !in usedFields
                                        }

                                if (nextField != null) {

                                    draftSortRules =
                                        draftSortRules +
                                                SortRule(
                                                    field = nextField
                                                )
                                }
                            },

                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("+ Add Sort")
                        }

                        Text(
                            text = "Sort Mode",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(
                                top = 8.dp
                            )
                        )

                        Row(
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            RadioButton(
                                selected =
                                    draftSortMode ==
                                            SortMode.ORDERED,

                                onClick = {
                                    draftSortMode =
                                        SortMode.ORDERED
                                }
                            )

                            Text("Priority Sort")
                        }

                        Row(
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            RadioButton(
                                selected =
                                    draftSortMode ==
                                            SortMode.RANDOM,

                                onClick = {
                                    draftSortMode =
                                        SortMode.RANDOM
                                }
                            )

                            Text("Random")
                        }
                    }

                    HorizontalDivider(
                        modifier =
                            Modifier.padding(
                                vertical = 8.dp
                            )
                    )

                    Button(
                        onClick = {

                            onApply(
                                draftFilters,
                                draftSortRules,
                                draftSortMode
                            )

                            expanded = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Apply Filters")
                    }
                }
            }
        }
    }
}

@Composable
fun JerseyDetailScreen(
    jersey: Jersey,

    // Current filtered/sorted sequence
    sequence: List<Jersey>,

    // Entire collection, used for related links
    allJerseys: List<Jersey>,

    imageUris: Map<String, Uri>,

    onBack: () -> Unit,
    onSelectJersey: (Jersey) -> Unit,

    onShowTeam: (String) -> Unit,
    onShowPlayer: (String) -> Unit,
    onShowNumber: (Int) -> Unit,

    modifier: Modifier = Modifier
) {

    var fullScreenImage by remember(jersey.id) {
        mutableStateOf<Uri?>(null)
    }

    // --------------------------------------------
    // Images
    // --------------------------------------------

    val availableImages = jersey.images.mapNotNull { fileName ->
        imageUris[fileName]
    }

    var imageIndex by remember(jersey.id) {
        mutableStateOf(0)
    }

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = {
            maxOf(
                availableImages.size,
                1
            )
        }
    )

    val coroutineScope =
        rememberCoroutineScope()

    LaunchedEffect(jersey.id) {
        pagerState.scrollToPage(0)
    }

    // Extra safety in case the next jersey has fewer images
    if (
        availableImages.isNotEmpty() &&
        imageIndex > availableImages.lastIndex
    ) {
        imageIndex = 0
    }

    val currentImage =
        availableImages.getOrNull(imageIndex)


    // --------------------------------------------
    // Previous / Next jersey
    // --------------------------------------------

    val currentJerseyIndex =
        sequence.indexOfFirst {
            it.id == jersey.id
        }

    val previousJersey =
        sequence.getOrNull(currentJerseyIndex - 1)

    val nextJersey =
        sequence.getOrNull(currentJerseyIndex + 1)


    // --------------------------------------------
    // Related jerseys
    // --------------------------------------------

    val sameTeam = allJerseys.filter {
        it.id != jersey.id &&
                it.team == jersey.team
    }

    val samePlayer =
        jersey.playerName?.let { player ->

            allJerseys.filter {
                it.id != jersey.id &&
                        it.playerName == player
            }

        } ?: emptyList()

    val sameNumber =
        jersey.number?.let { number ->

            allJerseys.filter {
                it.id != jersey.id &&
                        it.number == number
            }

        } ?: emptyList()

    var nhlProfile by remember(jersey.id) {
        mutableStateOf<NhlPlayerProfile?>(null)
    }

    var nhlLoading by remember(jersey.id) {
        mutableStateOf(false)
    }

    var nhlLoadFailed by remember(jersey.id) {
        mutableStateOf(false)
    }

    LaunchedEffect(
        jersey.id,
        jersey.nhlId
    ) {

        nhlProfile = null
        nhlLoadFailed = false

        val playerId =
            jersey.nhlId

        if (playerId != null) {

            nhlLoading = true

            val result =
                fetchNhlPlayerProfile(
                    playerId
                )

            nhlProfile =
                result

            nhlLoadFailed =
                result == null

            nhlLoading = false
        }
    }

    // --------------------------------------------
    // Screen
    // --------------------------------------------

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        // ----------------------------------------
        // Header
        // ----------------------------------------

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Button(
                onClick = onBack
            ) {
                Text("Back")
            }

            Text(
                text = "J_${jersey.id.toString().padStart(3, '0')}",
                fontSize = 16.sp
            )
        }


        // ----------------------------------------
        // Image
        // ----------------------------------------

        if (availableImages.isNotEmpty()) {

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp)
            ) { page ->

                val uri =
                    availableImages[page]

                AsyncImage(
                    model = uri,
                    contentDescription =
                        "${jersey.team} jersey image ${page + 1}",
                    contentScale = ContentScale.Fit,

                    modifier = Modifier
                        .fillMaxSize()
                        .clickable {
                            fullScreenImage = uri
                        }
                )
            }

            Text(
                text =
                    "${pagerState.currentPage + 1} / ${availableImages.size}",
                modifier = Modifier
                    .align(
                        Alignment.CenterHorizontally
                    )
                    .padding(top = 4.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {

                TextButton(
                    onClick = {

                        coroutineScope.launch {

                            pagerState.animateScrollToPage(
                                pagerState.currentPage - 1
                            )
                        }
                    },
                    enabled =
                        pagerState.currentPage > 0
                ) {
                    Text("‹ Previous")
                }

                TextButton(
                    onClick = {

                        coroutineScope.launch {

                            pagerState.animateScrollToPage(
                                pagerState.currentPage + 1
                            )
                        }
                    },
                    enabled =
                        pagerState.currentPage <
                                availableImages.lastIndex
                ) {
                    Text("Next ›")
                }
            }

        } else {

            Text(
                text = "No images available",
                modifier = Modifier
                    .align(
                        Alignment.CenterHorizontally
                    )
                    .padding(vertical = 40.dp)
            )
        }

        // ----------------------------------------
        // Main identity
        // ----------------------------------------

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Text(
            text = jersey.team,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        jersey.playerName?.let { player ->

            Text(
                text =
                    if (jersey.number != null) {
                        "$player #${jersey.number}"
                    } else {
                        player
                    },
                fontSize = 22.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        if (jersey.number != null) {

            Text(
                text = "#${jersey.number}",
                fontSize = 22.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        nhlProfile?.let { profile ->

            NhlPlayerHeader(
                jersey = jersey,
                profile = profile
            )
        }

//        nhlProfile?.let { profile ->
//
//            NhlSeasonStatsSection(
//                profile = profile
//            )
//        }

        nhlProfile?.let { profile ->

            HorizontalDivider(
                modifier = Modifier.padding(
                    vertical = 16.dp
                )
            )

            NhlSeasonStatsTable(
                profile = profile
            )

            NhlTeamSummaryTable(
                profile = profile
            )
        }


        // ----------------------------------------
        // Main jersey information
        // ----------------------------------------

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Text(
            text = "Jersey Details",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        DetailValueRow("League", jersey.league)
        DetailValueRow("Brand", jersey.brand)
        DetailValueRow("Model", jersey.model)
        DetailValueRow("Size", jersey.size)

        ColourDetailRow(
            colours = jersey.colours
        )

        DetailValueRow("Supplier", jersey.supplier)

        DetailValueRow(
            "Order Date",
            jersey.orderDate
        )

        DetailValueRow(
            "Receive Date",
            jersey.receiveDate
        )

        DetailValueRow(
            "Open Date",
            jersey.openDate
        )


        // ----------------------------------------
        // All remaining JSON properties
        // ----------------------------------------

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Text(
            text = "All Details",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        jersey.details.forEach { (key, value) ->

            if (
                key !in setOf(
                    "ID",
                    "League",
                    "Team",
                    "Number",
                    "PlayerFirst",
                    "PlayerLast",
                    "Brand",
                    "Model",
                    "Size",
                    "Colour1",
                    "Colour2",
                    "Colour3",
                    "Supplier",
                    "OrderDate",
                    "ReceiveDate",
                    "OpenDate"
                )
            ) {

                DetailValueRow(
                    key = key,
                    value = value
                )
            }
        }


        // ----------------------------------------
        // Previous / Next jersey
        // ----------------------------------------

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Text(
            text =
                if (sequence.isNotEmpty()) {
                    "Jersey ${currentJerseyIndex + 1} of ${sequence.size}"
                } else {
                    ""
                },
            modifier = Modifier.align(
                Alignment.CenterHorizontally
            )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Button(
                onClick = {
                    previousJersey?.let {
                        onSelectJersey(it)
                    }
                },
                enabled = previousJersey != null
            ) {
                Text("Previous Jersey")
            }

            Button(
                onClick = {
                    nextJersey?.let {
                        onSelectJersey(it)
                    }
                },
                enabled = nextJersey != null
            ) {
                Text("Next Jersey")
            }
        }


        // ----------------------------------------
        // Related collection links
        // ----------------------------------------

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Text(
            text = "Related Jerseys",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        if (sameTeam.isNotEmpty()) {

            RelatedLink(
                label = jersey.team,
                count = sameTeam.size,
                onClick = {
                    onShowTeam(jersey.team)
                }
            )
        }

        jersey.playerName?.let { player ->

            if (samePlayer.isNotEmpty()) {

                RelatedLink(
                    label = player,
                    count = samePlayer.size,
                    onClick = {
                        onShowPlayer(player)
                    }
                )
            }
        }

        jersey.number?.let { number ->

            if (sameNumber.isNotEmpty()) {

                RelatedLink(
                    label = "#$number",
                    count = sameNumber.size,
                    onClick = {
                        onShowNumber(number)
                    }
                )
            }
        }

        if (
            sameTeam.isEmpty() &&
            samePlayer.isEmpty() &&
            sameNumber.isEmpty()
        ) {
            Text(
                text = "No related jerseys found.",
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }

    fullScreenImage?.let { uri ->

        FullScreenImageViewer(
            uri = uri,
            contentDescription =
                "${jersey.team} jersey",

            onClose = {
                fullScreenImage = null
            }
        )
    }
}

@Composable
fun NhlPlayerHeader(
    jersey: Jersey,
    profile: NhlPlayerProfile
) {

    HorizontalDivider(
        modifier = Modifier.padding(
            vertical = 16.dp
        )
    )

    Row(
        modifier =
            Modifier.fillMaxWidth(),

        verticalAlignment =
            Alignment.CenterVertically,

        horizontalArrangement =
            Arrangement.spacedBy(16.dp)
    ) {

        profile.headshot?.let { url ->
            AsyncImage(
                model = url,
                contentDescription =
                    "${profile.fullName} headshot",

                contentScale =
                    ContentScale.Fit,

                modifier =
                    Modifier.size(110.dp)
            )
        }

        Column(
            modifier =
                Modifier.weight(1f)
        ) {
            Text(
                text =
                    profile.fullName,

                fontSize = 22.sp,
                fontWeight =
                    FontWeight.Bold
            )

            profile.position?.let {

                Text(
                    text =
                        if (
                            profile.sweaterNumber != null
                        ) {
                            "$it  •  #${profile.sweaterNumber}"
                        } else {
                            it
                        }
                )
            }

            when {

                !jersey.details["RetireDate"]
                    .isNullOrBlank() -> {

                    Text("Retired")
                }

                profile.isActive -> {

                    Text("Active")
                }

                else -> {

                    Text("Inactive")
                }
            }

            if (profile.inHallOfFame) {
                Text(
                    text = "Hockey Hall of Fame",
                    fontWeight =
                        FontWeight.Bold
                )
            }
            else {
                Text(
                    text = "Not in Hockey Hall of Fame (as of 2025)",
                    fontWeight =
                        FontWeight.Bold
                )
            }
        }

        profile.teamLogo?.let { logo ->

            AsyncImage(
                model = logo,
                contentDescription =
                    "Team logo",

                contentScale =
                    ContentScale.Fit,

                modifier =
                    Modifier.size(80.dp)
            )
        }
    }
}

@Composable
fun NhlSeasonStatsSection(
    profile: NhlPlayerProfile
) {

    val seasons =
        profile.seasonStats
            .filter {
                it.league == "NHL" &&
                        it.gameTypeId == 2
            }
            .sortedByDescending {
                it.season
            }

    if (seasons.isEmpty()) {
        return
    }

    HorizontalDivider(
        modifier = Modifier.padding(
            vertical = 16.dp
        )
    )

    Text(
        text = "NHL Career",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold
    )

    seasons.forEach { season ->
        NhlSeasonRow(
            season = season
        )
    }
}

@Composable
fun ColourDetailRow(
    colours: List<String>
) {

    if (colours.isEmpty()) {
        return
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Text(
            text = "Colours",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(0.40f)
        )

        Row(
            modifier = Modifier.weight(0.60f),

            horizontalArrangement =
                Arrangement.spacedBy(
                    6.dp,
                    Alignment.End
                ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            colours.forEach { colour ->

                ColourSwatch(
                    colourName = colour
                )
            }
        }
    }
}

@Composable
fun FullScreenImageViewer(
    uri: Uri,
    contentDescription: String,
    onClose: () -> Unit
) {

    var scale by remember(uri) {
        mutableFloatStateOf(1f)
    }

    var offsetX by remember(uri) {
        mutableFloatStateOf(0f)
    }

    var offsetY by remember(uri) {
        mutableFloatStateOf(0f)
    }

    val transformState =
        rememberTransformableState {
                zoomChange,
                panChange,
                _ ->

            val newScale =
                (scale * zoomChange)
                    .coerceIn(
                        1f,
                        6f
                    )

            scale = newScale

            if (scale > 1f) {

                offsetX +=
                    panChange.x

                offsetY +=
                    panChange.y

            } else {

                offsetX = 0f
                offsetY = 0f
            }
        }

    Dialog(
        onDismissRequest = onClose,

        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .transformable(
                        state = transformState
                    )
            ) {

                AsyncImage(
                    model = uri,

                    contentDescription =
                        contentDescription,

                    contentScale =
                        ContentScale.Fit,

                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale

                            translationX =
                                offsetX

                            translationY =
                                offsetY
                        }
                )
            }

            Button(
                onClick = onClose,

                modifier = Modifier
                    .align(
                        Alignment.TopEnd
                    )
                    .padding(16.dp)
            ) {
                Text("Close")
            }

            if (scale > 1f) {

                TextButton(
                    onClick = {
                        scale = 1f
                        offsetX = 0f
                        offsetY = 0f
                    },

                    modifier = Modifier
                        .align(
                            Alignment.BottomCenter
                        )
                        .padding(16.dp)
                ) {
                    Text("Reset Zoom")
                }
            }
        }
    }
}

@Composable
fun DetailRow(
    label: String,
    value: String?
) {
    if (!value.isNullOrBlank()) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {

            Text(
                text = label,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(0.40f)
            )

            Text(
                text = value,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(0.60f)
            )
        }
    }
}

@Composable
fun DetailValueRow(
    key: String,
    value: String?
) {

    // Null values: don't display anything
    if (value == null) {
        return
    }

    val uriHandler = LocalUriHandler.current

    val currencyKeys = setOf(
        "StickerPriceCAD",
        "StickerPriceCDN",
        "StickerPriceUS",
        "Duty",
        "Shipping",
        "Discount",
        "PriceC",
        "PriceM"
    )

    // -------------------------------------------------
    // Currency
    // Check FIELD TYPE before generic null handling.
    // -------------------------------------------------

    if (key in currencyKeys) {

        val numericValue =
            value?.toDoubleOrNull()

        val formatted =
            if (numericValue != null) {
                String.format(
                    Locale.CANADA,
                    "$%,.2f",
                    numericValue
                )
            } else {
                ""
            }

        DetailRow(
            label = key,
            value = formatted
        )

        return
    }


    // -------------------------------------------------
    // Dates
    // -------------------------------------------------

    if (
        key.contains(
            "Date",
            ignoreCase = true
        ) ||
        key.equals(
            "DOB",
            ignoreCase = true
        )
    ) {

        val formattedDate =
            value
                ?.takeIf { it.length >= 10 }
                ?.take(10)
                ?: ""

        DetailRow(
            label = key,
            value = formattedDate
        )

        return
    }


    // -------------------------------------------------
    // NHL Uniform hyperlink
    // -------------------------------------------------

    if (key == "NHLUniformLink") {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Text(
                text = key,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(0.4f)
            )

            if (!value.isNullOrBlank()) {

                TextButton(
                    onClick = {
                        uriHandler.openUri(value.replace("https://nhluniforms.com", "https://nhluniforms.com/Mobile"))
                    },
                    modifier = Modifier.weight(0.6f)
                ) {
                    Text("Open Link")
                }

            } else {

                Text(
                    text = "",
                    modifier = Modifier.weight(0.6f)
                )
            }
        }

        return
    }


    // -------------------------------------------------
    // Boolean / null
    // -------------------------------------------------

    val booleanState =
        when (value?.lowercase()) {

            "true" ->
                ToggleableState.On

            "false" ->
                ToggleableState.Off

//            null ->
//                ToggleableState.Indeterminate

            else ->
                null
        }

    if (booleanState != null) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Text(
                text = key,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(0.40f)
            )

            Box(
                modifier = Modifier.weight(0.60f),
                contentAlignment = Alignment.CenterEnd
            ) {

                Checkbox(
                    checked =
                        booleanState == ToggleableState.On,

                    onCheckedChange = null
                )
            }
        }

        return
    }

    if (key == "NHLID") {

        val formatted =
            value
                ?.toDoubleOrNull()
                ?.toLong()
                ?.toString()
                ?: value

        DetailRow(
            label = key,
            value = formatted
        )

        return
    }


    // -------------------------------------------------
    // Normal field
    // -------------------------------------------------

    DetailRow(
        label = key,
        value = value
    )
}

@Composable
fun RelatedLink(
    label: String,
    count: Int,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {

        Text(
            text =
                "$label  •  $count other ${
                    if (count == 1) "jersey"
                    else "jerseys"
                }"
        )
    }
}

@Composable
fun JerseyCard(
    jersey: Jersey,
    imageUris: Map<String, Uri>,
    onClick: () -> Unit
) {
    val primaryImageUri = jersey.images
        .firstNotNullOfOrNull { fileName ->
            imageUris[fileName]
        }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },
    ) {
        Column {

            if (primaryImageUri != null) {

                AsyncImage(
                    model = primaryImageUri,
                    contentDescription = "${jersey.team} jersey",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )
            }
            else {

                Text(
                    text = "No images",
                )
            }

            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                Text(
                    text = jersey.team,
                    fontSize = 18.sp
                )

                jersey.playerName?.let { player ->

                    val playerText =
                        if (jersey.number != null) {
                            "$player #${jersey.number}"
                        } else {
                            player
                        }

                    Text(
                        text = playerText,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                jersey.league?.let {
                    Text(
                        text = it,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                jersey.model?.let {
                    Text(text = it)
                }

                jersey.size?.let {
                    Text(text = "Size: $it")
                }

                Text(
                    text = "ID: J_${jersey.id.toString().padStart(3, '0')}",
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun MultiSelectDropdown(
    label: String,
    selectedValues: Set<String>,
    options: List<String>,
    onSelectionChanged: (Set<String>) -> Unit
) {
    var expanded by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {

        Text(
            text = label,
            fontSize = 12.sp
        )

        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                when {
                    selectedValues.isEmpty() -> "All"
                    selectedValues.size == 1 ->
                        selectedValues.first()
                    else ->
                        "${selectedValues.size} selected"
                }
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {

            options.forEach { option ->

                val selected =
                    option in selectedValues

                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selected,
                                onCheckedChange = null
                            )

                            Text(option)
                        }
                    },

                    onClick = {

                        val updated =
                            if (selected) {
                                selectedValues - option
                            } else {
                                selectedValues + option
                            }

                        onSelectionChanged(updated)

                        // Deliberately don't close menu:
                        // lets you pick Calgary + Edmonton etc.
                    }
                )
            }

            HorizontalDivider()

            DropdownMenuItem(
                text = {
                    Text("Clear selection")
                },
                onClick = {
                    onSelectionChanged(emptySet())
                }
            )

            DropdownMenuItem(
                text = {
                    Text("Done")
                },
                onClick = {
                    expanded = false
                }
            )
        }
    }
}

@Composable
fun FilterDropdown(
    label: String,
    selectedValue: String?,
    options: List<String>,
    onSelected: (String?) -> Unit
) {
    var expanded by remember {
        mutableStateOf(false)
    }

    Column {
        Text(
            text = label,
            fontSize = 12.sp
        )

        OutlinedButton(
            onClick = {
                expanded = true
            }
        ) {
            Text(
                selectedValue ?: "All"
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {
            DropdownMenuItem(
                text = {
                    Text("All")
                },
                onClick = {
                    onSelected(null)
                    expanded = false
                }
            )

            options.forEach { option ->

                DropdownMenuItem(
                    text = {
                        Text(option)
                    },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun MonthYearFilterControl(
    label: String,
    filter: MonthYearFilter,
    availableYears: List<Int>,
    onChanged: (MonthYearFilter) -> Unit
) {

    Text(
        text = label,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 8.dp)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement =
            Arrangement.spacedBy(8.dp)
    ) {

        Box(
            modifier = Modifier.weight(1f)
        ) {

            FilterDropdown(
                label = "Month",

                selectedValue =
                    filter.month?.let {
                        monthNames[it - 1]
                    },

                options = monthNames,

                onSelected = { selected ->

                    val month =
                        selected?.let {
                            monthNames.indexOf(it) + 1
                        }

                    onChanged(
                        filter.copy(
                            month = month
                        )
                    )
                }
            )
        }

        Box(
            modifier = Modifier.weight(1f)
        ) {

            FilterDropdown(
                label = "Year",

                selectedValue =
                    filter.year?.toString(),

                options =
                    availableYears.map {
                        it.toString()
                    },

                onSelected = { selected ->

                    onChanged(
                        filter.copy(
                            year =
                                selected
                                    ?.toIntOrNull()
                        )
                    )
                }
            )
        }
    }
}

@Composable
fun ColourSwatch(
    colourName: String,
    size: Dp = 22.dp
) {

    Box(
        modifier = Modifier
            .size(size)
            .background(
                color = jerseyColour(colourName),
                shape = RoundedCornerShape(3.dp)
            )
            .border(
                width = 1.dp,
                color = Color.Gray,
                shape = RoundedCornerShape(3.dp)
            )
            .semantics {
                contentDescription =
                    "Colour: $colourName"
            }
    )
}

@Composable
fun ColourMultiSelectDropdown(
    selectedValues: Set<String>,
    options: List<String>,
    onSelectionChanged: (Set<String>) -> Unit
) {

    var expanded by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {

        Text(
            text = "Colours",
            fontSize = 12.sp
        )

        OutlinedButton(
            onClick = {
                expanded = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {

            if (selectedValues.isEmpty()) {

                Text("All")

            } else {

                Row(
                    horizontalArrangement =
                        Arrangement.spacedBy(5.dp),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    selectedValues
                        .take(6)
                        .forEach {

                            ColourSwatch(
                                colourName = it,
                                size = 18.dp
                            )
                        }

                    if (selectedValues.size > 6) {
                        Text(
                            "+${selectedValues.size - 6}"
                        )
                    }
                }
            }
        }

        DropdownMenu(
            expanded = expanded,

            onDismissRequest = {
                expanded = false
            }
        ) {

            options.forEach { colour ->

                val selected =
                    colour in selectedValues

                DropdownMenuItem(
                    text = {

                        Row(
                            verticalAlignment =
                                Alignment.CenterVertically,

                            horizontalArrangement =
                                Arrangement.spacedBy(8.dp)
                        ) {

                            Checkbox(
                                checked = selected,
                                onCheckedChange = null
                            )

                            ColourSwatch(
                                colourName = colour,
                                size = 18.dp
                            )

                            Text(colour)
                        }
                    },

                    onClick = {

                        val updated =
                            if (selected) {
                                selectedValues - colour
                            } else {
                                selectedValues + colour
                            }

                        onSelectionChanged(
                            updated
                        )
                    }
                )
            }

            HorizontalDivider()

            DropdownMenuItem(
                text = {
                    Text("Clear selection")
                },
                onClick = {
                    onSelectionChanged(
                        emptySet()
                    )
                }
            )

            DropdownMenuItem(
                text = {
                    Text("Done")
                },
                onClick = {
                    expanded = false
                }
            )
        }
    }
}

@Composable
fun SortRuleControl(
    priority: Int,
    rule: SortRule,
    availableFields: List<JerseySort>,
    onChanged: (SortRule) -> Unit,
    onRemove: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {

        Column(
            modifier = Modifier.padding(10.dp)
        ) {

            Text(
                text = "Priority $priority",
                fontWeight = FontWeight.Bold
            )

            FilterDropdown(
                label = "Field",

                selectedValue =
                    rule.field.displayName(),

                options =
                    availableFields.map {
                        it.displayName()
                    },

                onSelected = { selected ->

                    val field =
                        availableFields
                            .firstOrNull {
                                it.displayName() ==
                                        selected
                            }

                    if (field != null) {
                        onChanged(
                            rule.copy(
                                field = field
                            )
                        )
                    }
                }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                RadioButton(
                    selected =
                        rule.ascending,

                    onClick = {
                        onChanged(
                            rule.copy(
                                ascending = true
                            )
                        )
                    }
                )

                Text("Ascending")

                RadioButton(
                    selected =
                        !rule.ascending,

                    onClick = {
                        onChanged(
                            rule.copy(
                                ascending = false
                            )
                        )
                    }
                )

                Text("Descending")
            }

            TextButton(
                onClick = onRemove
            ) {
                Text("Remove")
            }
        }
    }
}

@Composable
fun NhlSeasonRow(
    season: NhlSeasonStats
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {

        Row(
            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.SpaceBetween
        ) {

            Text(
                text =
                    formatNhlSeason(
                        season.season
                    ),

                fontWeight =
                    FontWeight.Bold
            )

            Text(
                text =
                    season.teamName
            )
        }

        if (
            season.goals != null ||
            season.assists != null ||
            season.points != null
        ) {

            Text(
                text =
                    "GP ${season.gamesPlayed ?: 0}   " +
                            "G ${season.goals ?: 0}   " +
                            "A ${season.assists ?: 0}   " +
                            "P ${season.points ?: 0}"
            )

        } else {

            Text(
                text =
                    "GP ${season.gamesPlayed ?: 0}   " +
                            "W ${season.wins ?: 0}   " +
                            "L ${season.losses ?: 0}"
            )
        }
    }
}

fun filterJerseys(
    jerseys: List<Jersey>,
    filters: CollectionFilters
): List<Jersey> {

    return jerseys.filter { jersey ->

        val leagueMatches =
            filters.leagues.isEmpty() ||
                    jersey.league in filters.leagues

        val teamMatches =
            filters.teams.isEmpty() ||
                    jersey.team in filters.teams

        val playerMatches =
            filters.players.isEmpty() ||
                    jersey.playerName in filters.players

        val sizeMatches =
            filters.sizes.isEmpty() ||
                    jersey.size in filters.sizes

        val brandMatches =
            filters.brands.isEmpty() ||
                    jersey.brand in filters.brands

        val modelMatches =
            filters.models.isEmpty() ||
                    jersey.model in filters.models

        val numberMatches =
            filters.numbers.isEmpty() ||
                    jersey.number in filters.numbers

        val supplierMatches =
            filters.suppliers.isEmpty() ||
                    jersey.supplier in filters.suppliers

        val colourMatches =
            filters.colours.isEmpty() ||
                    jersey.colours.any { jerseyColour ->

                        filters.colours.any { selectedColour ->

                            jerseyColour.equals(
                                selectedColour,
                                ignoreCase = true
                            )
                        }
                    }

        val playerTypeMatches =
            when (filters.jerseyType) {
                JerseyType.ALL ->
                    true

                JerseyType.PLAYER ->
                    jersey.hasPlayer

                JerseyType.BLANK ->
                    !jersey.hasPlayer
            }

        val imageMatches =
            when (filters.imageFilter) {
                ImageFilter.ALL ->
                    true

                ImageFilter.HAS_IMAGES ->
                    jersey.images.isNotEmpty()

                ImageFilter.NO_IMAGES ->
                    jersey.images.isEmpty()
            }

        val dobMatches =
            matchesMonthYear(
                dateValue =
                    jersey.details["DOB"],
                filter =
                    filters.dob
            )

        val orderDateMatches =
            matchesMonthYear(
                dateValue =
                    jersey.orderDate,
                filter =
                    filters.orderDate
            )

        val receiveDateMatches =
            matchesMonthYear(
                dateValue =
                    jersey.receiveDate,
                filter =
                    filters.receiveDate
            )

        val openDateMatches =
            matchesMonthYear(
                dateValue =
                    jersey.openDate,
                filter =
                    filters.openDate
            )

        leagueMatches &&
                teamMatches &&
                playerMatches &&
                sizeMatches &&
                brandMatches &&
                modelMatches &&
                numberMatches &&
                supplierMatches &&
                playerTypeMatches &&
                imageMatches &&
                dobMatches &&
                orderDateMatches &&
                receiveDateMatches &&
                openDateMatches &&
                colourMatches
    }
}

fun sortJerseys(
    jerseys: List<Jersey>,
    sortRules: List<SortRule>,
    sortMode: SortMode,
    randomSeed: Int = 0
): List<Jersey> {

    if (sortMode == SortMode.RANDOM) {
        return jerseys.shuffled(
            Random(randomSeed)
        )
    }

    if (sortRules.isEmpty()) {
        return jerseys
    }

    val comparator =
        Comparator<Jersey> { a, b ->

            for (rule in sortRules) {

                var result =
                    compareJerseysByField(
                        a = a,
                        b = b,
                        field = rule.field
                    )

                if (!rule.ascending) {
                    result = -result
                }

                if (result != 0) {
                    return@Comparator result
                }
            }

            // Final deterministic tie-breaker
            a.id.compareTo(b.id)
        }

    return jerseys.sortedWith(comparator)
}

fun compareJerseysByField(
    a: Jersey,
    b: Jersey,
    field: JerseySort
): Int {

    return when (field) {

        JerseySort.ID ->
            a.id.compareTo(b.id)

        JerseySort.LEAGUE ->
            compareNullableText(
                a.league,
                b.league
            )

        JerseySort.TEAM ->
            compareNullableText(
                a.team,
                b.team
            )

        JerseySort.PLAYER ->
            compareNullableText(
                a.playerName,
                b.playerName
            )

        JerseySort.NUMBER ->
            compareNullableInt(
                a.number,
                b.number
            )

        JerseySort.SIZE ->
            compareNullableText(
                a.size,
                b.size
            )

        JerseySort.BRAND ->
            compareNullableText(
                a.brand,
                b.brand
            )

        JerseySort.MODEL ->
            compareNullableText(
                a.model,
                b.model
            )

        JerseySort.SUPPLIER ->
            compareNullableText(
                a.supplier,
                b.supplier
            )

        JerseySort.COLOUR1 ->
            compareNullableText(
                a.colour1,
                b.colour1
            )

        JerseySort.COLOUR2 ->
            compareNullableText(
                a.colour2,
                b.colour2
            )

        JerseySort.COLOUR3 ->
            compareNullableText(
                a.colour3,
                b.colour3
            )

        JerseySort.COLOURS ->
            compareNullableText(
                a.colours.joinToString("|"),
                b.colours.joinToString("|")
            )

        JerseySort.DOB ->
            compareNullableText(
                a.details["DOB"],
                b.details["DOB"]
            )

        JerseySort.ORDER_DATE ->
            compareNullableText(
                a.orderDate,
                b.orderDate
            )

        JerseySort.RECEIVE_DATE ->
            compareNullableText(
                a.receiveDate,
                b.receiveDate
            )

        JerseySort.OPEN_DATE ->
            compareNullableText(
                a.openDate,
                b.openDate
            )

        JerseySort.IMAGE_COUNT ->
            a.images.size.compareTo(
                b.images.size
            )
    }
}

fun compareNullableText(
    a: String?,
    b: String?
): Int {

    if (a == null && b == null) {
        return 0
    }

    if (a == null) {
        return 1
    }

    if (b == null) {
        return -1
    }

    return a.compareTo(
        b,
        ignoreCase = true
    )
}


fun compareNullableInt(
    a: Int?,
    b: Int?
): Int {

    if (a == null && b == null) {
        return 0
    }

    if (a == null) {
        return 1
    }

    if (b == null) {
        return -1
    }

    return a.compareTo(b)
}

fun jerseyColour(
    value: String
): Color {

    val name = value
        .trim()
        .lowercase()
        .replace("-", " ")
        .replace("_", " ")

    return when (name) {

        "black" ->
            Color.Black

        "white" ->
            Color.White

        "red" ->
            Color(0xFFD32F2F)

        "dark red",
        "maroon" ->
            Color(0xFF800000)

        "blue" ->
            Color(0xFF1976D2)

        "dark blue",
        "navy",
        "navy blue" ->
            Color(0xFF0D2B56)

        "light blue",
        "powder blue" ->
            Color(0xFF87CEEB)

        "green" ->
            Color(0xFF388E3C)

        "dark green" ->
            Color(0xFF1B5E20)

        "yellow" ->
            Color(0xFFFFD600)

        "gold" ->
            Color(0xFFFFC107)

        "orange" ->
            Color(0xFFF57C00)

        "purple" ->
            Color(0xFF7B1FA2)

        "pink" ->
            Color(0xFFE91E63)

        "grey",
        "gray" ->
            Color(0xFF808080)

        "silver" ->
            Color(0xFFC0C0C0)

        "cream" ->
            Color(0xFFFFFDD0)

        "beige" ->
            Color(0xFFF5F5DC)

        "brown" ->
            Color(0xFF795548)

        else -> {

            // Also support hex values in your data,
            // e.g. #C8102E
            try {

                if (
                    value.startsWith("#") &&
                    value.length == 7
                ) {
                    Color(
                        android.graphics.Color.parseColor(value)
                    )
                } else {
                    Color.LightGray
                }

            } catch (_: Exception) {
                Color.LightGray
            }
        }
    }
}

fun JerseySort.displayName(): String {
    return when (this) {
        JerseySort.ID -> "ID"
        JerseySort.LEAGUE -> "League"
        JerseySort.TEAM -> "Team"
        JerseySort.PLAYER -> "Player"
        JerseySort.NUMBER -> "Jersey Number"

        JerseySort.BRAND -> "Brand"
        JerseySort.MODEL -> "Style / Model"
        JerseySort.SUPPLIER -> "Supplier"
        JerseySort.SIZE -> "Size"

        JerseySort.COLOUR1 -> "Primary Colour"
        JerseySort.COLOUR2 -> "Secondary Colour"
        JerseySort.COLOUR3 -> "Tertiary Colour"
        JerseySort.COLOURS -> "Colour Combination"

        JerseySort.DOB -> "DOB"
        JerseySort.ORDER_DATE -> "Order Date"
        JerseySort.RECEIVE_DATE -> "Receive Date"
        JerseySort.OPEN_DATE -> "Open Date"

        JerseySort.IMAGE_COUNT -> "Image Count"
    }
}

data class SimpleDateParts(
    val year: Int,
    val month: Int
)

fun extractDateParts(
    value: String?
): SimpleDateParts? {

    if (value.isNullOrBlank() || value.length < 7) {
        return null
    }

    val year =
        value.substring(0, 4)
            .toIntOrNull()
            ?: return null

    val month =
        value.substring(5, 7)
            .toIntOrNull()
            ?: return null

    if (month !in 1..12) {
        return null
    }

    return SimpleDateParts(
        year = year,
        month = month
    )
}

fun matchesMonthYear(
    dateValue: String?,
    filter: MonthYearFilter
): Boolean {

    // No date filter applied
    if (
        filter.month == null &&
        filter.year == null
    ) {
        return true
    }

    val parts =
        extractDateParts(dateValue)
            ?: return false

    val monthMatches =
        filter.month == null ||
                parts.month == filter.month

    val yearMatches =
        filter.year == null ||
                parts.year == filter.year

    return monthMatches &&
            yearMatches
}

suspend fun fetchNhlPlayerProfile(
    playerId: Long
): NhlPlayerProfile? {

    return withContext(Dispatchers.IO) {

        try {

            val url =
                "https://api-web.nhle.com/v1/player/$playerId/landing"

            val jsonText =
                URL(url).readText()

            val obj =
                JSONObject(jsonText)

            val seasonStats =
                mutableListOf<NhlSeasonStats>()

            val totals =
                obj.optJSONArray("seasonTotals")

            if (totals != null) {

                for (i in 0 until totals.length()) {

                    val season =
                        totals.getJSONObject(i)

                    val teamName =
                        season
                            .optJSONObject("teamName")
                            ?.optString("default")
                            ?: ""

                    val teamAbbrev =
                        teamNameToAbbrev(teamName)

                    seasonStats.add(
                        NhlSeasonStats(
                            season =
                                season.optInt("season"),

                            teamName =
                                season
                                    .optJSONObject("teamName")
                                    ?.optString("default")
                                    ?: "",

                            teamAbbrev = teamAbbrev,

                            league =
                                season.optString(
                                    "leagueAbbrev"
                                ),

                            gameTypeId =
                                season.optInt(
                                    "gameTypeId"
                                ),

                            gamesPlayed =
                                season
                                    .optInt("gamesPlayed"),
//                                    .takeIf {
//                                        season.has("gamesPlayed")
//                                    },

                            goals =
                                season
                                    .optInt("goals"),
//                                    .takeIf {
//                                        season.has("goals")
//                                    },

                            assists =
                                season
                                    .optInt("assists"),
//                                    .takeIf {
//                                        season.has("assists")
//                                    },

                            points =
                                season
                                    .optInt("points"),
//                                    .takeIf {
//                                        season.has("points")
//                                    },

                            wins =
                                season
                                    .optInt("wins"),
//                                    .takeIf {
//                                        season.has("wins")
//                                    },

                            losses =
                                season
                                    .optInt("losses"),
//                                    .takeIf {
//                                        season.has("losses")
//                                    },

                            savePctg =
                                season
                                    .optDouble("savePctg")
                                    .takeIf {
                                        season.has("savePctg")
                                    },

                            goalsAgainstAverage =
                                season
                                    .optDouble(
                                        "goalsAgainstAvg"
                                    )
                                    .takeIf {
                                        season.has(
                                            "goalsAgainstAvg"
                                        )
                                    }
                        )
                    )
                }
            }

            NhlPlayerProfile(
                playerId =
                    obj.getLong("playerId"),

                firstName =
                    obj
                        .optJSONObject("firstName")
                        ?.optString("default")
                        ?: "",

                lastName =
                    obj
                        .optJSONObject("lastName")
                        ?.optString("default")
                        ?: "",

                isActive =
                    obj.optBoolean(
                        "isActive",
                        false
                    ),

                inHallOfFame =
                    obj.optInt(
                        "inHHOF",
                        0
                    ) == 1,

                currentTeamAbbrev =
                    obj.optString(
                        "currentTeamAbbrev"
                    ).takeIf {
                        it.isNotBlank()
                    },

                teamLogo =
                    obj.optString(
                        "teamLogo"
                    ).takeIf {
                        it.isNotBlank()
                    },

                headshot =
                    obj.optString(
                        "headshot"
                    ).takeIf {
                        it.isNotBlank()
                    },

                position =
                    obj.optString(
                        "position"
                    ).takeIf {
                        it.isNotBlank()
                    },

                sweaterNumber =
                    if (
                        obj.has("sweaterNumber") &&
                        !obj.isNull("sweaterNumber")
                    ) {
                        obj.optInt("sweaterNumber")
                    } else {
                        null
                    },

                seasonStats =
                    seasonStats
            )

        } catch (e: Exception) {

            e.printStackTrace()
            null
        }
    }
}

fun formatNhlSeason(
    season: Int
): String {

    val value =
        season.toString()

    if (value.length != 8) {
        return value
    }

    val start =
        value.substring(0, 4)

    val end =
        value.substring(6, 8)

    return "$start-$end"
}

fun nhlTeamLogoUrl(
    teamAbbrev: String?
): String? {

    if (teamAbbrev.isNullOrBlank()) {
        return null
    }

    return "https://assets.nhle.com/logos/nhl/svg/${teamAbbrev.uppercase()}_light.svg"
}

fun teamNameToAbbrev(
    teamName: String
): String? {

    return when (
        teamName.trim().lowercase()
    ) {

        "anaheim ducks" -> "ANA"
        "arizona coyotes" -> "ARI"
        "boston bruins" -> "BOS"
        "buffalo sabres" -> "BUF"
        "calgary flames" -> "CGY"
        "carolina hurricanes" -> "CAR"
        "chicago blackhawks" -> "CHI"
        "colorado avalanche" -> "COL"
        "columbus blue jackets" -> "CBJ"
        "dallas stars" -> "DAL"
        "detroit red wings" -> "DET"
        "edmonton oilers" -> "EDM"
        "florida panthers" -> "FLA"
        "los angeles kings" -> "LAK"
        "minnesota wild" -> "MIN"
        "montreal canadiens" -> "MTL"
        "nashville predators" -> "NSH"
        "new jersey devils" -> "NJD"
        "new york islanders" -> "NYI"
        "new york rangers" -> "NYR"
        "ottawa senators" -> "OTT"
        "philadelphia flyers" -> "PHI"
        "pittsburgh penguins" -> "PIT"
        "san jose sharks" -> "SJS"
        "seattle kraken" -> "SEA"
        "st. louis blues" -> "STL"
        "tampa bay lightning" -> "TBL"
        "toronto maple leafs" -> "TOR"
        "utah hockey club" -> "UTA"
        "vancouver canucks" -> "VAN"
        "vegas golden knights" -> "VGK"
        "washington capitals" -> "WSH"
        "winnipeg jets" -> "WPG"

        else -> null
    }
}

fun summarizeStatsByTeam(
    seasons: List<NhlSeasonStats>
): List<NhlTeamStats> {

    return seasons
        .filter {
            it.league == "NHL" &&
                    it.gameTypeId == 2
        }
        .groupBy {
            it.teamAbbrev ?: it.teamName
        }
        .map { (_, rows) ->

            NhlTeamStats(
                teamAbbrev =
                    rows.first().teamAbbrev,

                teamName =
                    rows.first().teamName,

                season = seasons.first(),

                gamesPlayed =
                    rows.sumOf {
                        it.gamesPlayed
                    },

                goals =
                    rows.sumOf {
                        it.goals
                    },

                assists =
                    rows.sumOf {
                        it.assists
                    },

                points =
                    rows.sumOf {
                        it.points
                    },

                plusMinus =
                    rows.sumOf {
                        it.plusMinus
                    },

                pim =
                    rows.sumOf {
                        it.pim
                    },

                wins =
                    rows.sumOf {
                        it.wins
                    },

                losses =
                    rows.sumOf {
                        it.losses
                    },

                otLosses =
                    rows.sumOf {
                        it.otLosses
                    },

                shutouts =
                    rows.sumOf {
                        it.shutouts
                    }
            )
        }
        .sortedByDescending {
            it.gamesPlayed
        }
}

fun summarizeCareer(
    seasons: List<NhlSeasonStats>
): NhlTeamStats {

    val nhlRows =
        seasons.filter {
            it.league == "NHL" &&
                    it.gameTypeId == 2
        }

    return NhlTeamStats(
        teamAbbrev = null,
        teamName = "Career",

        season = seasons.first(),

        gamesPlayed =
            nhlRows.sumOf {
                it.gamesPlayed
            },

        goals =
            nhlRows.sumOf {
                it.goals
            },

        assists =
            nhlRows.sumOf {
                it.assists
            },

        points =
            nhlRows.sumOf {
                it.points
            },

        plusMinus =
            nhlRows.sumOf {
                it.plusMinus
            },

        pim =
            nhlRows.sumOf {
                it.pim
            },

        wins =
            nhlRows.sumOf {
                it.wins
            },

        losses =
            nhlRows.sumOf {
                it.losses
            },

        otLosses =
            nhlRows.sumOf {
                it.otLosses
            },

        shutouts =
            nhlRows.sumOf {
                it.shutouts
            }
    )
}

@Composable
fun NhlSeasonStatsTable(
    profile: NhlPlayerProfile
) {

    val seasons =
        profile.seasonStats
            .filter {
                it.league == "NHL" &&
                        it.gameTypeId == 2
            }
            .sortedByDescending {
                it.season
            }

    if (seasons.isEmpty()) {
        return
    }

    val isGoalie =
        profile.position == "G"

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {

        Text(
            text = "Season-by-Season",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(
                bottom = 8.dp
            )
        )

        if (isGoalie) {
            GoalieStatsTable(
                seasons
            )
        } else {
            SkaterStatsTable(
                seasons
            )
        }
    }
}

@Composable
fun SkaterStatsTable(
    seasons: List<NhlSeasonStats>
) {

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {

        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            TableHeader(
                "Season",
                Modifier.weight(1.45f)
            )

            TableHeader(
                "Team",
                Modifier.weight(0.8f)
            )

            TableHeader(
                "GP",
                Modifier.weight(0.65f)
            )

            TableHeader(
                "G",
                Modifier.weight(0.55f)
            )

            TableHeader(
                "A",
                Modifier.weight(0.55f)
            )

            TableHeader(
                "P",
                Modifier.weight(0.55f)
            )
        }

        HorizontalDivider()

        seasons.forEach { stats ->

            SkaterStatsRow(
                seasonLabel =
                    formatNhlSeason(
                        stats.season
                    ),

                stats = stats
            )
        }
    }
}

@Composable
fun TableHeader(
    text: String,
    modifier: Modifier = Modifier
) {

    Text(
        text = text,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        textAlign = TextAlign.Center,
        modifier = modifier
    )
}

@Composable
fun SkaterStatsRow(
    seasonLabel: String,
    stats: NhlSeasonStats
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Text(
            text = seasonLabel,
            fontSize = 12.sp,
            modifier = Modifier.weight(1.45f)
        )

        TeamLogoCell(
            teamAbbrev =
                stats.teamAbbrev,

            contentDescription =
                stats.teamName,

            modifier =
                Modifier.weight(0.8f)
        )

        StatCell(
            stats.gamesPlayed,
            Modifier.weight(0.65f)
        )

        StatCell(
            stats.goals,
            Modifier.weight(0.55f)
        )

        StatCell(
            stats.assists,
            Modifier.weight(0.55f)
        )

        StatCell(
            stats.points,
            Modifier.weight(0.55f)
        )
    }

    HorizontalDivider()
}

@Composable
fun TeamLogoCell(
    teamAbbrev: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {

    val logoUrl =
        nhlTeamLogoUrl(
            teamAbbrev
        )

    Box(
        modifier = modifier,
        contentAlignment =
            Alignment.Center
    ) {

        if (logoUrl != null) {

            AsyncImage(
                model = logoUrl,

                contentDescription =
                    contentDescription,

                contentScale =
                    ContentScale.Fit,

                modifier =
                    Modifier.size(30.dp)
            )

        } else {

            Text(
                text =
                    teamAbbrev ?: "?",
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun StatCell(
    value: Int,
    modifier: Modifier = Modifier
) {

    Text(
        text = value.toString(),
        fontSize = 12.sp,
        textAlign = TextAlign.Center,
        modifier = modifier
    )
}

@Composable
fun NhlTeamSummaryTable(
    profile: NhlPlayerProfile
) {

    val teamTotals =
        summarizeStatsByTeam(
            profile.seasonStats
        )

    val career =
        summarizeCareer(
            profile.seasonStats
        )

    if (teamTotals.isEmpty()) {
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp)
    ) {

        Text(
            text = "Totals by Team",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(
                bottom = 8.dp
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {

            TableHeader(
                "Team",
                Modifier.weight(1.45f)
            )

            TableHeader(
                "GP",
                Modifier.weight(0.7f)
            )

            TableHeader(
                "G",
                Modifier.weight(0.6f)
            )

            TableHeader(
                "A",
                Modifier.weight(0.6f)
            )

            TableHeader(
                "P",
                Modifier.weight(0.6f)
            )
        }

        HorizontalDivider()

        teamTotals.forEach { stats ->

            TeamSummaryRow(
                stats = stats
            )
        }

        HorizontalDivider(
            modifier =
                Modifier.padding(
                    vertical = 4.dp
                )
        )

        CareerSummaryRow(
            stats = career
        )
    }
}

@Composable
fun TeamSummaryRow(
    stats: NhlTeamStats
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Row(
            modifier =
                Modifier.weight(1.45f),

            verticalAlignment =
                Alignment.CenterVertically,

            horizontalArrangement =
                Arrangement.spacedBy(6.dp)
        ) {

            nhlTeamLogoUrl(
                stats.teamAbbrev
            )?.let { logo ->

                AsyncImage(
                    model = logo,
                    contentDescription =
                        stats.teamName,

                    contentScale =
                        ContentScale.Fit,

                    modifier =
                        Modifier.size(32.dp)
                )
            }

            Text(
                text =
                    stats.teamAbbrev
                        ?: stats.teamName,

                fontSize = 11.sp
            )
        }

        StatCell(
            stats.gamesPlayed,
            Modifier.weight(0.7f)
        )

        StatCell(
            stats.goals,
            Modifier.weight(0.6f)
        )

        StatCell(
            stats.assists,
            Modifier.weight(0.6f)
        )

        StatCell(
            stats.points,
            Modifier.weight(0.6f)
        )
    }
}

@Composable
fun CareerSummaryRow(
    stats: NhlTeamStats
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Text(
            text = "CAREER",
            fontWeight = FontWeight.Bold,
            modifier =
                Modifier.weight(1.45f)
        )

        StatCell(
            stats.gamesPlayed,
            Modifier.weight(0.7f)
        )

        StatCell(
            stats.goals,
            Modifier.weight(0.6f)
        )

        StatCell(
            stats.assists,
            Modifier.weight(0.6f)
        )

        StatCell(
            stats.points,
            Modifier.weight(0.6f)
        )
    }
}

@Composable
fun GoalieStatsTable(
    seasons: List<NhlSeasonStats>
) {

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {

        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            TableHeader(
                "Season",
                Modifier.weight(1.30f)
            )

            TableHeader(
                "Team",
                Modifier.weight(0.75f)
            )

            TableHeader(
                "GP",
                Modifier.weight(0.50f)
            )

            TableHeader(
                "W",
                Modifier.weight(0.45f)
            )

            TableHeader(
                "L",
                Modifier.weight(0.45f)
            )

            TableHeader(
                "OTL",
                Modifier.weight(0.55f)
            )

            TableHeader(
                "SV%",
                Modifier.weight(0.70f)
            )

            TableHeader(
                "GAA",
                Modifier.weight(0.70f)
            )
        }

        HorizontalDivider()

        seasons.forEach { stats ->

            GoalieStatsRow(
                seasonLabel =
                    formatNhlSeason(
                        stats.season
                    ),

                stats = stats
            )
        }
    }
}

@Composable
fun GoalieStatsRow(
    seasonLabel: String,
    stats: NhlSeasonStats
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Text(
            text = seasonLabel,
            fontSize = 12.sp,
            modifier =
                Modifier.weight(1.30f)
        )

        TeamLogoCell(
            teamAbbrev =
                stats.teamAbbrev,

            contentDescription =
                stats.teamName,

            modifier =
                Modifier.weight(0.75f)
        )

        StatCell(
            stats.gamesPlayed,
            Modifier.weight(0.50f)
        )

        StatCell(
            stats.wins,
            Modifier.weight(0.45f)
        )

        StatCell(
            stats.losses,
            Modifier.weight(0.45f)
        )

        StatCell(
            stats.otLosses,
            Modifier.weight(0.55f)
        )

        GoalieDecimalStatCell(
            value = stats.savePctg,
            decimals = 3,
            modifier =
                Modifier.weight(0.70f)
        )

        GoalieDecimalStatCell(
            value =
                stats.goalsAgainstAverage,

            decimals = 2,

            modifier =
                Modifier.weight(0.70f)
        )
    }

    HorizontalDivider()
}

@Composable
fun GoalieDecimalStatCell(
    value: Double?,
    decimals: Int,
    modifier: Modifier = Modifier
) {

    val formatted =
        if (value != null) {
            "%.${decimals}f".format(
                Locale.US,
                value
            )
        } else {
            "-"
        }

    Text(
        text = formatted,
        fontSize = 12.sp,
        textAlign = TextAlign.Center,
        modifier = modifier
    )
}