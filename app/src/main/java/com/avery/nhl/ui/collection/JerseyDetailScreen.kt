package com.avery.nhl.ui.collection

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import com.avery.nhl.data.nhl.fetchNhlPlayerProfile
import com.avery.nhl.model.jersey.Jersey
import com.avery.nhl.model.jersey.toDisplayString
import com.avery.nhl.model.nhl.NHLPlayerProfile
import com.avery.nhl.model.nhl.NHLSeasonStats
import com.avery.nhl.model.nhl.NHLTeamStats
import com.avery.nhl.util.formatNhlSeason
import com.avery.nhl.util.nhlTeamLogoUrl
import kotlinx.coroutines.launch
import java.util.Locale

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
        mutableStateOf<NHLPlayerProfile?>(null)
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
//                text = "J_${jersey.id.toString().padStart(3, '0')}",
                text = jersey.toDisplayString(),
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

            NHLPlayerHeader(
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

            NHLSeasonStatsTable(
                profile = profile
            )

            NHLTeamSummaryTable(
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
        DetailValueRow("Make", jersey.make)
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

        DetailValueRow(
            "PriceF",
            jersey.priceF.toString(),
            displayName = "Price"
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
                    "Make",
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
fun NHLSeasonStatsTable(
    profile: NHLPlayerProfile
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

    var expanded by remember {
        mutableStateOf(false)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {

        Column {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        expanded = !expanded
                    }
                    .padding(12.dp),

                horizontalArrangement =
                    Arrangement.SpaceBetween,

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Text(
                    text = "Season-by-Season",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(
                        bottom = 8.dp
                    )
                )

                Text(
                    if (expanded) "▲" else "▼"
                )
            }

            if (expanded) {
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
    }
}


@Composable
fun SkaterStatsTable(
    seasons: List<NHLSeasonStats>
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
fun SkaterStatsRow(
    seasonLabel: String,
    stats: NHLSeasonStats
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
fun NHLPlayerHeader(
    jersey: Jersey,
    profile: NHLPlayerProfile
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
            Arrangement.spacedBy(
                16.dp
            )
    ) {

        profile.headshot
            ?.let { url ->

                AsyncImage(
                    model = url,

                    contentDescription =
                        "${profile.fullName} headshot",

                    contentScale =
                        ContentScale.Fit,

                    modifier =
                        Modifier.size(
                            110.dp
                        )
                )
            }


        Column(
            modifier =
                Modifier.weight(1f)
        ) {

            Text(
                text =
                    profile.fullName,

                fontSize =
                    22.sp,

                fontWeight =
                    FontWeight.Bold
            )


            profile.position
                ?.let { position ->

                    Text(
                        text =
                            if (
                                profile.sweaterNumber != null
                            ) {

                                "$position  •  #${profile.sweaterNumber}"

                            } else {

                                position
                            }
                    )
                }


            when {

                !jersey
                    .details["RetireDate"]
                    .isNullOrBlank() -> {

                    Text(
                        "Retired"
                    )
                }


                profile.isActive -> {

                    Text(
                        "Active"
                    )
                }


                else -> {

                    Text(
                        "Inactive"
                    )
                }
            }


            if (
                profile.inHallOfFame
            ) {

                Text(
                    text =
                        "Hockey Hall of Fame",

                    fontWeight =
                        FontWeight.Bold
                )

            } else {

                Text(
                    text =
                        "Not in Hockey Hall of Fame (as of 2025)",

                    fontWeight =
                        FontWeight.Bold
                )
            }
        }


        profile.teamLogo
            ?.let { logo ->

                AsyncImage(
                    model = logo,

                    contentDescription =
                        "Team logo",

                    contentScale =
                        ContentScale.Fit,

                    modifier =
                        Modifier.size(
                            80.dp
                        )
                )
            }
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
fun GoalieStatsTable(
    seasons: List<NHLSeasonStats>
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
fun GoalieStatsRow(
    seasonLabel: String,
    stats: NHLSeasonStats
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
    value: String?,
    displayName: String? = null,
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
        "PriceM",
        "PriceF"
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
            label = displayName ?: key,
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

fun summarizeCareer(
    seasons: List<NHLSeasonStats>
): NHLTeamStats {

    val nhlRows =
        seasons.filter {
            it.league == "NHL" &&
                    it.gameTypeId == 2
        }

    return NHLTeamStats(
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
fun NHLTeamSummaryTable(
    profile: NHLPlayerProfile
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

    var expanded by remember {
        mutableStateOf(false)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {

        Column {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        expanded = !expanded
                    }
                    .padding(12.dp),

                horizontalArrangement =
                    Arrangement.SpaceBetween,

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Text(
                    text = "Career",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(
                        bottom = 8.dp
                    )
                )

                Text(
                    if (expanded) "▲" else "▼"
                )
            }

            if (expanded) {
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
    }
}

fun summarizeStatsByTeam(
    seasons: List<NHLSeasonStats>
): List<NHLTeamStats> {

    return seasons
        .filter {
            it.league == "NHL" &&
                    it.gameTypeId == 2
        }
        .groupBy {
            it.teamAbbrev ?: it.teamName
        }
        .map { (_, rows) ->

            NHLTeamStats(
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

@Composable
fun CareerSummaryRow(
    stats: NHLTeamStats
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
fun TeamSummaryRow(
    stats: NHLTeamStats
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