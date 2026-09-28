package com.avery.nhl.ui.leaders

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState

import androidx.compose.material3.*

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import coil3.compose.AsyncImage

import com.avery.nhl.data.nhl.NHLRepository

import com.avery.nhl.model.nhl.GoalieLeaderCategory
import com.avery.nhl.model.nhl.LeaderType
import com.avery.nhl.model.nhl.NHLGameType
import com.avery.nhl.model.nhl.NHLSeasonOption
import com.avery.nhl.model.nhl.NHLStatLeader
import com.avery.nhl.model.nhl.SkaterLeaderCategory
import com.avery.nhl.model.nhl.buildNHLSeasonOptions
import com.avery.nhl.ui.player.PlayerProfileScreen

import kotlinx.coroutines.CancellationException

@Composable
fun LeadersScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {

    val repository =
        remember {
            NHLRepository()
        }

    var selectedPlayerId by remember {
        mutableStateOf<Long?>(
            null
        )
    }

    var leaderType by remember {
        mutableStateOf(
            LeaderType.SKATER
        )
    }

    val seasons =
        remember {
            buildNHLSeasonOptions(
                newestStartYear = 2025,
                oldestStartYear = 1990
            )
        }


    var selectedSeasonValue by rememberSaveable {
        mutableIntStateOf(
            20252026
        )
    }


    var gameTypeValue by rememberSaveable {
        mutableIntStateOf(
            NHLGameType.REGULAR_SEASON.apiValue
        )
    }


    val selectedSeason =
        seasons.first {
            it.value ==
                    selectedSeasonValue
        }


    val selectedGameType =
        NHLGameType.entries.first {
            it.apiValue ==
                    gameTypeValue
        }

    var skaterCategory by remember {
        mutableStateOf(
            SkaterLeaderCategory.POINTS
        )
    }


    var goalieCategory by remember {
        mutableStateOf(
            GoalieLeaderCategory.WINS
        )
    }


    var leaders by remember {
        mutableStateOf<List<NHLStatLeader>>(
            emptyList()
        )
    }


    var loading by remember {
        mutableStateOf(true)
    }


    var error by remember {
        mutableStateOf<String?>(
            null
        )
    }


    var refreshKey by remember {
        mutableIntStateOf(0)
    }


    val categoryName =
        when (leaderType) {

            LeaderType.SKATER ->
                skaterCategory.apiName

            LeaderType.GOALIE ->
                goalieCategory.apiName
        }

    val limit = 25


    LaunchedEffect(
        leaderType,
        categoryName,
        selectedSeasonValue,
        gameTypeValue,
        refreshKey
    ) {

        loading = true
        error = null

        try {

            leaders =
                when (leaderType) {

                    LeaderType.SKATER ->

                        repository.getSkaterLeaders(
                            category =
                                skaterCategory.apiName,

                            limit =
                                limit,

                            season =
                                selectedSeasonValue,

                            gameType =
                                gameTypeValue
                        )


                    LeaderType.GOALIE ->

                        repository.getGoalieLeaders(
                            category =
                                goalieCategory.apiName,

                            limit =
                                limit,

                            season =
                                selectedSeasonValue,

                            gameType =
                                gameTypeValue
                        )
                }

        } catch (
            e: CancellationException
        ) {

            throw e

        } catch (
            e: Exception
        ) {

            leaders =
                emptyList()

            error =
                e.message
                    ?: "Could not load league leaders."

        } finally {

            loading = false
        }
    }

    val playerId =
        selectedPlayerId


    if (
        playerId != null
    ) {

        PlayerProfileScreen(
            playerId =
                playerId,

            onBack = {
                selectedPlayerId =
                    null
            },

            modifier =
                modifier
        )

        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                horizontal = 12.dp
            )
    ) {

        LeadersTopBar(
            seasonName =
                selectedSeason.displayName,

            gameTypeName =
                selectedGameType.displayName,

            onBack =
                onBack,

            onRefresh = {
                refreshKey++
            }
        )


        LeaderTypeSelector(
            selected =
                leaderType,

            onSelected = {
                leaderType = it
            }
        )


        Spacer(
            modifier =
                Modifier.height(
                    8.dp
                )
        )


        when (leaderType) {

            LeaderType.SKATER ->

                SkaterCategorySelector(
                    selected =
                        skaterCategory,

                    onSelected = {
                        skaterCategory =
                            it
                    }
                )


            LeaderType.GOALIE ->

                GoalieCategorySelector(
                    selected =
                        goalieCategory,

                    onSelected = {
                        goalieCategory =
                            it
                    }
                )
        }


        Spacer(
            modifier =
                Modifier.height(
                    10.dp
                )
        )

        Column(
            verticalArrangement =
                Arrangement.spacedBy(
                    6.dp
                )
        ) {

            SeasonSelector(
                selectedSeason =
                    selectedSeason,

                seasons =
                    seasons,

                onSelected = {
                    selectedSeasonValue =
                        it.value
                }
            )


            GameTypeSelector(
                selected =
                    selectedGameType,

                onSelected = {
                    gameTypeValue =
                        it.apiValue
                }
            )
        }

        when {

            loading -> {

                Box(
                    modifier =
                        Modifier.fillMaxSize(),

                    contentAlignment =
                        Alignment.Center
                ) {

                    CircularProgressIndicator()
                }
            }


            error != null -> {

                LeadersError(
                    message =
                        error!!,

                    onRetry = {
                        refreshKey++
                    }
                )
            }


            leaders.isEmpty() -> {

                Box(
                    modifier =
                        Modifier.fillMaxSize(),

                    contentAlignment =
                        Alignment.Center
                ) {

                    Text(
                        text =
                            "No ${selectedGameType.displayName.lowercase()} " +
                                    "leader data is available for " +
                                    "${selectedSeason.displayName}."
                    )
                }
            }


            else -> {

                LeadersList(
                    leaders =
                        leaders,

                    type =
                        leaderType,

                    skaterCategory =
                        skaterCategory,

                    goalieCategory =
                        goalieCategory,

                    onPlayerClick = {
                            playerId ->

                        selectedPlayerId =
                            playerId
                    }
                )
            }
        }
    }
}

@Composable
private fun LeadersTopBar(
    seasonName: String,
    gameTypeName: String,
    onBack: () -> Unit,
    onRefresh: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                vertical = 8.dp
            ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        TextButton(
            onClick =
                onBack
        ) {

            Text(
                "‹ Home"
            )
        }


        Column(
            modifier =
                Modifier.weight(1f),

            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Text(
                text =
                    "League Leaders",

                fontSize =
                    20.sp,

                fontWeight =
                    FontWeight.Bold
            )


            Text(
                text =
                    "$seasonName • $gameTypeName",

                fontSize =
                    11.sp,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }


        TextButton(
            onClick =
                onRefresh
        ) {

            Text(
                "Refresh"
            )
        }
    }
}

@Composable
private fun LeaderTypeSelector(
    selected: LeaderType,
    onSelected: (LeaderType) -> Unit
) {

    Row(
        modifier =
            Modifier.fillMaxWidth(),

        horizontalArrangement =
            Arrangement.spacedBy(
                8.dp
            )
    ) {

        FilterChip(
            selected =
                selected ==
                        LeaderType.SKATER,

            onClick = {
                onSelected(
                    LeaderType.SKATER
                )
            },

            label = {
                Text(
                    "Skaters"
                )
            }
        )


        FilterChip(
            selected =
                selected ==
                        LeaderType.GOALIE,

            onClick = {
                onSelected(
                    LeaderType.GOALIE
                )
            },

            label = {
                Text(
                    "Goalies"
                )
            }
        )
    }
}

@Composable
private fun SkaterCategorySelector(
    selected: SkaterLeaderCategory,
    onSelected: (
        SkaterLeaderCategory
    ) -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(
                rememberScrollState()
            ),

        horizontalArrangement =
            Arrangement.spacedBy(
                6.dp
            )
    ) {

        SkaterLeaderCategory.entries
            .forEach {
                    category ->

                FilterChip(
                    selected =
                        selected ==
                                category,

                    onClick = {
                        onSelected(
                            category
                        )
                    },

                    label = {
                        Text(
                            category
                                .displayName
                        )
                    }
                )
            }
    }
}

@Composable
private fun GoalieCategorySelector(
    selected: GoalieLeaderCategory,
    onSelected: (
        GoalieLeaderCategory
    ) -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(
                rememberScrollState()
            ),

        horizontalArrangement =
            Arrangement.spacedBy(
                6.dp
            )
    ) {

        GoalieLeaderCategory.entries
            .forEach {
                    category ->

                FilterChip(
                    selected =
                        selected ==
                                category,

                    onClick = {
                        onSelected(
                            category
                        )
                    },

                    label = {
                        Text(
                            category
                                .displayName
                        )
                    }
                )
            }
    }
}

@Composable
private fun LeadersList(
    leaders: List<NHLStatLeader>,
    type: LeaderType,
    skaterCategory: SkaterLeaderCategory,
    goalieCategory: GoalieLeaderCategory,
    onPlayerClick: (Long) -> Unit
) {

    LazyColumn(
        verticalArrangement =
            Arrangement.spacedBy(
                6.dp
            ),

        contentPadding =
            PaddingValues(
                bottom = 24.dp
            )
    ) {

        itemsIndexed(
            items = leaders,

            key = {
                    index,
                    leader ->

                leader.playerId
                    ?: index
            }
        ) {
                index,
                leader ->

            LeaderCard(
                rank =
                    index + 1,

                leader =
                    leader,

                value =
                    formatLeaderValue(
                        value =
                            leader.value,

                        type =
                            type,

                        skaterCategory =
                            skaterCategory,

                        goalieCategory =
                            goalieCategory
                    ),

                onClick = {

                    leader.playerId
                        ?.let {
                            onPlayerClick(it)
                        }
                }
            )
        }
    }
}

@Composable
private fun LeaderCard(
    rank: Int,
    leader: NHLStatLeader,
    value: String,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled =
                    leader.playerId != null,

                onClick =
                    onClick
            )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    10.dp
                ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Text(
                text =
                    rank.toString(),

                modifier =
                    Modifier.width(
                        28.dp
                    ),

                textAlign =
                    TextAlign.Center,

                fontSize =
                    18.sp,

                fontWeight =
                    FontWeight.Bold
            )


            AsyncImage(
                model =
                    leader.headshot
                        ?: leader
                            .fallbackHeadshot,

                contentDescription =
                    leader.name,

                contentScale =
                    ContentScale.Fit,

                modifier =
                    Modifier.size(
                        54.dp
                    )
            )


            Spacer(
                modifier =
                    Modifier.width(
                        8.dp
                    )
            )


            Column(
                modifier =
                    Modifier.weight(1f)
            ) {

                Text(
                    text =
                        leader.name,

                    fontWeight =
                        FontWeight.Bold,

                    fontSize =
                        15.sp
                )


                Row(
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    leader.teamLogo
                        ?.let {
                                logo ->

                            AsyncImage(
                                model =
                                    logo,

                                contentDescription =
                                    null,

                                modifier =
                                    Modifier.size(
                                        18.dp
                                    )
                            )


                            Spacer(
                                modifier =
                                    Modifier.width(
                                        4.dp
                                    )
                            )
                        }


                    Text(
                        text =
                            buildString {

                                leader.teamAbbrev
                                    ?.let {
                                        append(it)
                                    }


                                leader.position
                                    ?.let {

                                        if (
                                            isNotBlank()
                                        ) {
                                            append(" • ")
                                        }

                                        append(it)
                                    }


                                leader.sweaterNumber
                                    ?.let {

                                        append(
                                            " • #$it"
                                        )
                                    }
                            },

                        fontSize =
                            11.sp,

                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )
                }
            }


            Text(
                text =
                    value,

                fontSize =
                    22.sp,

                fontWeight =
                    FontWeight.Bold
            )
        }
    }
}

private fun formatLeaderValue(
    value: Double?,
    type: LeaderType,
    skaterCategory: SkaterLeaderCategory,
    goalieCategory: GoalieLeaderCategory
): String {

    if (value == null) {
        return "-"
    }


    if (
        type ==
        LeaderType.SKATER
    ) {

        return when (
            skaterCategory
        ) {

            SkaterLeaderCategory.POINTS,
            SkaterLeaderCategory.GOALS,
            SkaterLeaderCategory.ASSISTS,
            SkaterLeaderCategory.PLUS_MINUS,
            SkaterLeaderCategory.POWER_PLAY_GOALS -> {

                value
                    .toInt()
                    .let {

                        if (
                            skaterCategory ==
                            SkaterLeaderCategory.PLUS_MINUS &&
                            it > 0
                        ) {
                            "+$it"
                        } else {
                            it.toString()
                        }
                    }
            }
        }
    }


    return when (
        goalieCategory
    ) {

        GoalieLeaderCategory.WINS,
        GoalieLeaderCategory.SHUTOUTS ->

            value
                .toInt()
                .toString()


        GoalieLeaderCategory.SAVE_PERCENTAGE -> {

            val pct =
                if (
                    value > 1.0
                ) {
                    value / 100.0
                } else {
                    value
                }


            "%.3f".format(
                pct
            )
                .removePrefix(
                    "0"
                )
        }


        GoalieLeaderCategory.GAA ->

            "%.2f".format(
                value
            )
    }
}

@Composable
private fun LeadersError(
    message: String,
    onRetry: () -> Unit
) {

    Column(
        modifier =
            Modifier.fillMaxSize(),

        horizontalAlignment =
            Alignment.CenterHorizontally,

        verticalArrangement =
            Arrangement.Center
    ) {

        Text(
            text =
                message,

            color =
                MaterialTheme
                    .colorScheme
                    .error
        )


        Button(
            onClick =
                onRetry,

            modifier =
                Modifier.padding(
                    top = 12.dp
                )
        ) {

            Text(
                "Retry"
            )
        }
    }
}

@Composable
private fun SeasonSelector(
    selectedSeason: NHLSeasonOption,
    seasons: List<NHLSeasonOption>,
    onSelected: (NHLSeasonOption) -> Unit
) {

    var expanded by remember {
        mutableStateOf(false)
    }


    Box {

        OutlinedButton(
            onClick = {
                expanded = true
            }
        ) {

            Text(
                text =
                    selectedSeason.displayName
            )
        }


        DropdownMenu(
            expanded =
                expanded,

            onDismissRequest = {
                expanded = false
            }
        ) {

            seasons.forEach {
                    season ->

                DropdownMenuItem(
                    text = {

                        Text(
                            season.displayName
                        )
                    },

                    onClick = {

                        onSelected(
                            season
                        )

                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun GameTypeSelector(
    selected: NHLGameType,
    onSelected: (NHLGameType) -> Unit
) {

    Row(
        horizontalArrangement =
            Arrangement.spacedBy(
                8.dp
            )
    ) {

        NHLGameType.entries
            .forEach {
                    type ->

                FilterChip(
                    selected =
                        selected == type,

                    onClick = {
                        onSelected(
                            type
                        )
                    },

                    label = {
                        Text(
                            type.displayName
                        )
                    }
                )
            }
    }
}