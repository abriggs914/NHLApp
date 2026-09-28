package com.avery.nhl.ui.playoffs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import coil3.compose.AsyncImage

import com.avery.nhl.data.nhl.NHLRepository
import com.avery.nhl.data.playoffs.PlayoffStateRepository
import com.avery.nhl.model.nhl.NHLPlayoffBracket
import com.avery.nhl.model.nhl.NHLPlayoffSeries
import com.avery.nhl.model.nhl.NHLPlayoffTeam
import com.avery.nhl.model.playoffs.PlayoffSavedState
import com.avery.nhl.model.playoffs.ProjectionWeights

import kotlinx.coroutines.CancellationException

@Composable
fun PlayoffsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {

    val repository =
        remember {
            NHLRepository()
        }

    var selectedSeriesLetter by remember {
        mutableStateOf<String?>(
            null
        )
    }

    var selectedYear by rememberSaveable {
        mutableIntStateOf(
            2026
        )
    }


    var bracket by remember {
        mutableStateOf<NHLPlayoffBracket?>(
            null
        )
    }

    val selectedSeries =
        bracket
            ?.series
            ?.firstOrNull {
                it.seriesLetter ==
                        selectedSeriesLetter
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

    var playoffsView by remember {
        mutableStateOf(
            PlayoffsView.OFFICIAL
        )
    }


    val context =
        LocalContext.current


    val stateRepository =
        remember(
            context
        ) {
            PlayoffStateRepository(
                context
            )
        }

    var savedState by remember(
        selectedYear
    ) {
        mutableStateOf<PlayoffSavedState?>(
            null
        )
    }


    var savedStateLoaded by remember(
        selectedYear
    ) {
        mutableStateOf(
            false
        )
    }


    LaunchedEffect(
        selectedYear
    ) {

        savedStateLoaded =
            false


        savedState =
            stateRepository.load(
                selectedYear
            )


        savedStateLoaded =
            true
    }


    LaunchedEffect(
        selectedYear,
        refreshKey
    ) {

        loading = true
        error = null


        try {

            bracket =
                repository.getPlayoffBracket(
                    selectedYear
                )

        } catch (
            e: CancellationException
        ) {

            throw e

        } catch (
            e: Exception
        ) {

            bracket = null

            error =
                e.message
                    ?: "Could not load playoff bracket."

        } finally {

            loading = false
        }
    }

    if (
        selectedSeries != null
    ) {

        val season =
            (selectedYear - 1) * 10000 +
                    selectedYear


        PlayoffSeriesScreen(
            season =
                season,

            series =
                selectedSeries,

            onBack = {
                selectedSeriesLetter =
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

        PlayoffsTopBar(
            onBack = onBack,

            onRefresh = {
                refreshKey++
            }
        )


        PlayoffYearSelector(
            selectedYear =
                selectedYear,

            onSelected = {
                selectedYear =
                    it
            }
        )


        Spacer(
            modifier =
                Modifier.height(
                    8.dp
                )
        )

        if (
            !savedStateLoaded
        ) {

            Box(
                modifier =
                    Modifier.fillMaxSize(),

                contentAlignment =
                    Alignment.Center
            ) {

                CircularProgressIndicator()
            }

        } else {

            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(
                        8.dp
                    )
            ) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .horizontalScroll(
                                rememberScrollState()
                            ),

                    horizontalArrangement =
                        Arrangement.spacedBy(
                            8.dp
                        )
                ) {
                    FilterChip(
                        selected =
                            playoffsView ==
                                    PlayoffsView.OFFICIAL,

                        onClick = {

                            playoffsView =
                                PlayoffsView.OFFICIAL
                        },

                        label = {
                            Text(
                                "Official"
                            )
                        }
                    )


                    FilterChip(
                        selected =
                            playoffsView ==
                                    PlayoffsView.PREDICTION,

                        onClick = {

                            playoffsView =
                                PlayoffsView.PREDICTION
                        },

                        label = {
                            Text(
                                "My Bracket"
                            )
                        }
                    )


                    FilterChip(
                        selected =
                            playoffsView ==
                                    PlayoffsView.PROJECTIONS,

                        onClick = {
                            playoffsView =
                                PlayoffsView.PROJECTIONS
                        },

                        label = {
                            Text(
                                "Projections"
                            )
                        }
                    )
                }
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

                    PlayoffsError(
                        message =
                            error!!,

                        onRetry = {
                            refreshKey++
                        }
                    )
                }


                bracket != null -> {

                    when (playoffsView) {

                        PlayoffsView.OFFICIAL -> {

                            PlayoffBracketContent(
                                bracket =
                                    bracket!!,

                                onSeriesClick = {
                                    selectedSeriesLetter =
                                        it.seriesLetter
                                }
                            )
                        }


                        PlayoffsView.PREDICTION -> {

                            PlayoffPredictionScreen(
                                officialBracket =
                                    bracket!!,

                                savedBracket =
                                    savedState
                                        ?.predictionBracket,

                                onBracketChanged = { updatedBracket ->

                                    val updatedState =
                                        (
                                                savedState
                                                    ?: PlayoffSavedState(
                                                        playoffYear =
                                                            selectedYear
                                                    )
                                                )
                                            .copy(
                                                predictionBracket =
                                                    updatedBracket
                                            )


                                    savedState =
                                        updatedState


                                    stateRepository.save(
                                        updatedState
                                    )
                                },

                                onBack = {
                                    playoffsView =
                                        PlayoffsView.OFFICIAL
                                },

                                modifier =
                                    Modifier.fillMaxSize()
                            )
                        }


                        PlayoffsView.PROJECTIONS -> {

                            val season =
                                (selectedYear - 1) *
                                        10000 +
                                        selectedYear


                            PlayoffProjectionsScreen(
                                bracket =
                                    bracket!!,

                                season =
                                    season,

                                initialWeights =
                                    savedState
                                        ?.projectionWeights
                                        ?: ProjectionWeights(),

                                initialSimulationIterations =
                                    savedState
                                        ?.simulationIterations
                                        ?: 10_000,

                                onSettingsChanged = { weights,
                                                      iterations ->

                                    val updated =
                                        (
                                                savedState
                                                    ?: PlayoffSavedState(
                                                        playoffYear =
                                                            selectedYear
                                                    )
                                                )
                                            .copy(
                                                projectionWeights =
                                                    weights,

                                                simulationIterations =
                                                    iterations
                                            )


                                    savedState =
                                        updated


                                    stateRepository.save(
                                        updated
                                    )
                                },

                                modifier =
                                    Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayoffsTopBar(
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
            Alignment.CenterVertically,

        horizontalArrangement =
            Arrangement.SpaceBetween
    ) {

        TextButton(
            onClick =
                onBack
        ) {

            Text(
                "‹ Home"
            )
        }


        Text(
            text =
                "NHL Playoffs",

            fontSize =
                22.sp,

            fontWeight =
                FontWeight.Bold
        )


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
private fun PlayoffYearSelector(
    selectedYear: Int,
    onSelected: (Int) -> Unit
) {

    var expanded by remember {
        mutableStateOf(false)
    }

    val years =
        remember {
            (2026 downTo 2000)
                .toList()
        }


    Box {

        OutlinedButton(
            onClick = {
                expanded = true
            }
        ) {

            Text(
                "$selectedYear Playoffs"
            )
        }


        DropdownMenu(
            expanded =
                expanded,

            onDismissRequest = {
                expanded = false
            }
        ) {

            years.forEach {
                    year ->

                DropdownMenuItem(
                    text = {

                        Text(
                            "$year Playoffs"
                        )
                    },

                    onClick = {

                        onSelected(
                            year
                        )

                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun PlayoffBracketContent(
    bracket: NHLPlayoffBracket,
    onSeriesClick: (NHLPlayoffSeries) -> Unit
) {

    val grouped =
        bracket.series
            .groupBy {
                it.playoffRound
            }


    LazyColumn(
        verticalArrangement =
            Arrangement.spacedBy(
                10.dp
            ),

        contentPadding =
            PaddingValues(
                bottom = 24.dp
            )
    ) {

        bracket.bracketLogo
            ?.let {
                    logo ->

                item {

                    AsyncImage(
                        model =
                            logo,

                        contentDescription =
                            "${bracket.year} playoff logo",

                        contentScale =
                            ContentScale.Fit,

                        modifier = Modifier
                            .fillMaxWidth()
                            .height(
                                70.dp
                            )
                    )
                }
            }


        for (
        round in 1..4
        ) {

            val roundSeries =
                grouped[round]
                    .orEmpty()


            if (
                roundSeries.isNotEmpty()
            ) {

                item(
                    key =
                        "round-header-$round"
                ) {

                    PlayoffRoundHeader(
                        round =
                            round
                    )
                }


                items(
                    items =
                        roundSeries,

                    key = {
                        it.seriesLetter
                    }
                ) {
                        series ->

                    PlayoffSeriesCard(
                        series =
                            series,

                        onClick = {
                            onSeriesClick(
                                series
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayoffRoundHeader(
    round: Int
) {

    val title =
        when (round) {

            1 ->
                "First Round"

            2 ->
                "Second Round"

            3 ->
                "Conference Finals"

            4 ->
                "Stanley Cup Final"

            else ->
                "Round $round"
        }


    Text(
        text =
            title,

        modifier =
            Modifier.padding(
                top = 8.dp,
                bottom = 2.dp
            ),

        fontSize =
            19.sp,

        fontWeight =
            FontWeight.Bold
    )
}

@Composable
private fun PlayoffSeriesCard(
    series: NHLPlayoffSeries,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled =
                    series.seriesLetter
                        .isNotBlank(),

                onClick =
                    onClick
            )
    ) {

        Column(
            modifier =
                Modifier.padding(
                    10.dp
                )
        ) {

            if (
                series.playoffRound >= 3
            ) {

                Text(
                    text =
                        series.seriesTitle,

                    fontSize =
                        11.sp,

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant,

                    modifier =
                        Modifier.padding(
                            bottom = 6.dp
                        )
                )
            }


            PlayoffTeamRow(
                team =
                    series.topSeedTeam,

                seed =
                    series.topSeedRankAbbrev,

                wins =
                    series.topSeedWins,

                winner =
                    series.winningTeamId ==
                            series.topSeedTeam?.id
            )


            HorizontalDivider(
                modifier =
                    Modifier.padding(
                        vertical = 4.dp
                    )
            )


            PlayoffTeamRow(
                team =
                    series.bottomSeedTeam,

                seed =
                    series.bottomSeedRankAbbrev,

                wins =
                    series.bottomSeedWins,

                winner =
                    series.winningTeamId ==
                            series.bottomSeedTeam?.id
            )
        }
    }
}

@Composable
private fun PlayoffTeamRow(
    team: NHLPlayoffTeam?,
    seed: String?,
    wins: Int,
    winner: Boolean
) {

    Row(
        modifier =
            Modifier.fillMaxWidth(),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        if (
            team != null
        ) {

            AsyncImage(
                model =
                    team.darkLogo
                        ?: team.logo,

                contentDescription =
                    "${team.name} logo",

                contentScale =
                    ContentScale.Fit,

                modifier =
                    Modifier.size(
                        36.dp
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
                        team.name,

                    fontSize =
                        14.sp,

                    fontWeight =
                        if (winner) {
                            FontWeight.Bold
                        } else {
                            FontWeight.Normal
                        }
                )


                Row(
                    horizontalArrangement =
                        Arrangement.spacedBy(
                            5.dp
                        )
                ) {

                    seed
                        ?.let {

                            Text(
                                text =
                                    it,

                                fontSize =
                                    10.sp,

                                color =
                                    MaterialTheme
                                        .colorScheme
                                        .onSurfaceVariant
                            )
                        }


                    Text(
                        text =
                            team.abbrev,

                        fontSize =
                            10.sp,

                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )
                }
            }

        } else {

            Text(
                text =
                    "TBD",

                modifier =
                    Modifier.weight(1f),

                fontWeight =
                    FontWeight.Bold
            )
        }


        Text(
            text =
                wins.toString(),

            fontSize =
                24.sp,

            fontWeight =
                FontWeight.Bold
        )


        if (winner) {

            Spacer(
                modifier =
                    Modifier.width(
                        5.dp
                    )
            )


            Text(
                text =
                    "✓",

                fontWeight =
                    FontWeight.Bold,

                color =
                    MaterialTheme
                        .colorScheme
                        .primary
            )
        }
    }
}

@Composable
private fun PlayoffsError(
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

private enum class PlayoffsView {
    OFFICIAL,
    PREDICTION,
    PROJECTIONS
}