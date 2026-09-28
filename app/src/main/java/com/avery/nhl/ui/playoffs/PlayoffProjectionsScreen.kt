package com.avery.nhl.ui.playoffs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import androidx.compose.material3.*

import androidx.compose.runtime.*

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import coil3.compose.AsyncImage

import com.avery.nhl.data.playoffs.PlayoffProjectionRepository
import com.avery.nhl.model.nhl.NHLPlayoffBracket
import com.avery.nhl.model.nhl.NHLPlayoffSeries
import com.avery.nhl.model.nhl.NHLPlayoffTeam

import com.avery.nhl.model.playoffs.*

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.max

@Composable
fun PlayoffProjectionsScreen(
    bracket: NHLPlayoffBracket,
    season: Int,

    initialWeights: ProjectionWeights =
        ProjectionWeights(),

    initialSimulationIterations: Int =
        10_000,

    onSettingsChanged: (
        ProjectionWeights,
        Int
    ) -> Unit,

    modifier: Modifier = Modifier
) {

    val repository =
        remember {
            PlayoffProjectionRepository()
        }


    var simulation by remember {
        mutableStateOf<PlayoffSimulationResult?>(
            null
        )
    }


    var simulationLoading by remember {
        mutableStateOf(false)
    }


    var stats by remember(
        bracket.year
    ) {
        mutableStateOf<
                Map<String, TeamProjectionStats>
                >(
            emptyMap()
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


    val firstRound =
        remember(
            bracket
        ) {

            bracket.series
                .filter {
                    it.playoffRound == 1
                }
        }


    var weights by remember(
        bracket.year
    ) {

        mutableStateOf(
            initialWeights
        )
    }


    var simulationIterations by remember(
        bracket.year
    ) {

        mutableIntStateOf(
            initialSimulationIterations
        )
    }


    LaunchedEffect(
        bracket.year,
        season
    ) {

        loading = true
        error = null


        try {

            val teamAbbrevs =
                firstRound
                    .flatMap {
                            series ->

                        listOfNotNull(
                            series
                                .topSeedTeam
                                ?.abbrev,

                            series
                                .bottomSeedTeam
                                ?.abbrev
                        )
                    }


            stats =
                repository.getTeamStats(
                    teamAbbrevs =
                        teamAbbrevs,

                    season =
                        season
                )

        } catch (
            e: CancellationException
        ) {

            throw e

        } catch (
            e: Exception
        ) {

            error =
                e.message
                    ?: "Could not build playoff projections."

        } finally {

            loading =
                false
        }
    }


    LaunchedEffect(
        stats,
        weights,
        simulationIterations
    ) {

        if (
            stats.isEmpty()
        ) {

            simulation =
                null

            return@LaunchedEffect
        }


        simulationLoading =
            true


        try {

            simulation =
                withContext(
                    Dispatchers.Default
                ) {

                    simulatePlayoffs(
                        bracket =
                            bracket,

                        stats =
                            stats,

                        settings =
                            PlayoffSimulationSettings(
                                iterations =
                                    simulationIterations,

                                weights =
                                    weights
                            )
                    )
                }

        } finally {

            simulationLoading =
                false
        }
    }


    when {

        loading -> {

            Box(
                modifier =
                    modifier.fillMaxSize(),

                contentAlignment =
                    Alignment.Center
            ) {

                CircularProgressIndicator()
            }
        }


        error != null -> {

            Box(
                modifier =
                    modifier.fillMaxSize(),

                contentAlignment =
                    Alignment.Center
            ) {

                Text(
                    text =
                        error!!,

                    color =
                        MaterialTheme
                            .colorScheme
                            .error
                )
            }
        }


        else -> {

            ProjectionContent(
                bracket =
                    bracket,

                stats =
                    stats,

                weights =
                    weights,

                onWeightsChanged = {
                        newWeights ->

                    weights =
                        newWeights


                    onSettingsChanged(
                        newWeights,
                        simulationIterations
                    )
                },

                simulation =
                    simulation,

                simulationLoading =
                    simulationLoading,

                simulationIterations =
                    simulationIterations,

                onSimulationIterationsChanged = {
                        newIterations ->

                    simulationIterations =
                        newIterations


                    onSettingsChanged(
                        weights,
                        newIterations
                    )
                },

                modifier =
                    modifier
            )
        }
    }
}

@Composable
private fun ProjectionContent(
    bracket: NHLPlayoffBracket,
    stats: Map<String, TeamProjectionStats>,
    weights: ProjectionWeights,
    onWeightsChanged: (ProjectionWeights) -> Unit,

    simulation: PlayoffSimulationResult?,
    simulationLoading: Boolean,

    simulationIterations: Int,
    onSimulationIterationsChanged: (Int) -> Unit,

    modifier: Modifier = Modifier
) {

    val projections =
        bracket.series
            .filter {
                it.playoffRound == 1
            }
            .mapNotNull {
                    series ->

                buildProjection(
                    series =
                        series,

                    stats =
                        stats,

                    weights =
                        weights
                )
            }


    LazyColumn(
        modifier =
            modifier.fillMaxSize(),

        contentPadding =
            PaddingValues(
                start = 12.dp,
                end = 12.dp,
                bottom = 28.dp
            ),

        verticalArrangement =
            Arrangement.spacedBy(
                10.dp
            )
    ) {

        item {

            ProjectionSummary(
                projections =
                    projections
            )
        }


        item {

            SimulationControls(
                iterations =
                    simulationIterations,

                onIterationsChanged =
                    onSimulationIterationsChanged
            )
        }


        item {

            when {

                simulationLoading -> {

                    Row(
                        modifier =
                            Modifier.fillMaxWidth(),

                        horizontalArrangement =
                            Arrangement.Center
                    ) {

                        CircularProgressIndicator()
                    }
                }


                simulation != null -> {

                    SimulationResultsTable(
                        result =
                            simulation
                    )
                }
            }
        }


        item {

            ProjectionWeightControls(
                weights =
                    weights,

                onChanged =
                    onWeightsChanged
            )
        }


        items(
            items =
                projections,

            key = {
                    projection ->

                projection.topTeam.id
                    .toString() +
                        "-" +
                        projection.bottomTeam.id
            }
        ) {
                projection ->

            MatchupProjectionCard(
                projection =
                    projection
            )
        }
    }
}

private fun buildProjection(
    series: NHLPlayoffSeries,
    stats: Map<String, TeamProjectionStats>,
    weights: ProjectionWeights
): MatchupProjection? {

    val top =
        series.topSeedTeam
            ?: return null


    val bottom =
        series.bottomSeedTeam
            ?: return null


    return projectPlayoffMatchup(
        topTeam =
            top,

        bottomTeam =
            bottom,

        topStats =
            stats[
                top.abbrev.uppercase()
            ],

        bottomStats =
            stats[
                bottom.abbrev.uppercase()
            ],

        topHasHomeIce =
            true,

        weights =
            weights
    )
}

@Composable
private fun MatchupProjectionCard(
    projection: MatchupProjection
) {

    Card(
        modifier =
            Modifier.fillMaxWidth()
    ) {

        Column(
            modifier =
                Modifier.padding(
                    12.dp
                )
        ) {

            ProjectionTeamRow(
                team =
                    projection.topTeam,

                probability =
                    projection.topProbability,

                winner =
                    projection.projectedWinner.id ==
                            projection.topTeam.id
            )


            LinearProgressIndicator(
                progress = {
                    projection.topProbability
                        .toFloat()
                },

                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        vertical = 7.dp
                    )
            )


            ProjectionTeamRow(
                team =
                    projection.bottomTeam,

                probability =
                    projection.bottomProbability,

                winner =
                    projection.projectedWinner.id ==
                            projection.bottomTeam.id
            )


            HorizontalDivider(
                modifier =
                    Modifier.padding(
                        vertical = 8.dp
                    )
            )


            Text(
                text =
                    "Projection: " +
                            "${projection.projectedWinner.abbrev} " +
                            "wins 4-${projection.projectedLoserWins}",

                fontWeight =
                    FontWeight.Bold
            )


            Text(
                text =
                    projectionExplanation(
                        projection
                    ),

                fontSize =
                    11.sp,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant,

                modifier =
                    Modifier.padding(
                        top = 3.dp
                    )
            )
        }
    }
}

@Composable
private fun ProjectionTeamRow(
    team: NHLPlayoffTeam,
    probability: Double,
    winner: Boolean
) {

    Row(
        modifier =
            Modifier.fillMaxWidth(),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        AsyncImage(
            model =
                team.darkLogo
                    ?: team.logo,

            contentDescription =
                team.name,

            modifier =
                Modifier.size(
                    38.dp
                ),

            contentScale =
                ContentScale.Fit
        )


        Spacer(
            modifier =
                Modifier.width(
                    8.dp
                )
        )


        Text(
            text =
                team.name,

            modifier =
                Modifier.weight(1f),

            fontWeight =
                if (winner) {
                    FontWeight.Bold
                } else {
                    FontWeight.Normal
                }
        )


        Text(
            text =
                String.format(
                    java.util.Locale.CANADA,
                    "%.1f%%",
                    probability * 100.0
                ),

            fontSize =
                20.sp,

            fontWeight =
                FontWeight.Bold
        )
    }
}

private fun projectionExplanation(
    projection: MatchupProjection
): String {

    val winner =
        projection.projectedWinner


    val stats =
        if (
            winner.id ==
            projection.topTeam.id
        ) {

            projection.topStats

        } else {

            projection.bottomStats
        }


    if (stats == null) {

        return "Limited team data available."
    }


    val gd =
        String.format(
            java.util.Locale.CANADA,
            "%+.2f",
            stats.goalDifferentialPerGame
        )


    val recent =
        if (
            stats.recentGames > 0
        ) {

            "${stats.recentWins}-${stats.recentGames - stats.recentWins} " +
                    "in last ${stats.recentGames}"

        } else {

            "recent form unavailable"
        }


    return buildString {

        append(
            String.format(
                java.util.Locale.CANADA,
                "%.3f points percentage",
                stats.pointsPct
            )
        )

        append(
            " • $gd goal differential/game"
        )

        append(
            " • $recent"
        )
    }
}

@Composable
private fun ProjectionWeightControls(
    weights: ProjectionWeights,
    onChanged: (ProjectionWeights) -> Unit
) {

    var expanded by remember {
        mutableStateOf(false)
    }


    Card(
        modifier =
            Modifier.fillMaxWidth()
    ) {

        Column(
            modifier =
                Modifier.padding(
                    12.dp
                )
        ) {

            TextButton(
                onClick = {
                    expanded =
                        !expanded
                }
            ) {

                Text(
                    if (expanded) {
                        "Hide Projection Weights"
                    } else {
                        "Projection Weights"
                    }
                )
            }


            if (expanded) {

                ProjectionWeightSlider(
                    title =
                        "Season points %",

                    value =
                        weights.pointsPct,

                    onChanged = {

                        onChanged(
                            weights.copy(
                                pointsPct =
                                    it
                            )
                        )
                    }
                )


                ProjectionWeightSlider(
                    title =
                        "Goal differential",

                    value =
                        weights.goalDifferential,

                    onChanged = {

                        onChanged(
                            weights.copy(
                                goalDifferential =
                                    it
                            )
                        )
                    }
                )


                ProjectionWeightSlider(
                    title =
                        "Recent form",

                    value =
                        weights.recentForm,

                    onChanged = {

                        onChanged(
                            weights.copy(
                                recentForm =
                                    it
                            )
                        )
                    }
                )


                ProjectionWeightSlider(
                    title =
                        "Home ice",

                    value =
                        weights.homeIce,

                    onChanged = {

                        onChanged(
                            weights.copy(
                                homeIce =
                                    it
                            )
                        )
                    }
                )


                TextButton(
                    onClick = {

                        onChanged(
                            ProjectionWeights()
                        )
                    }
                ) {

                    Text(
                        "Reset Weights"
                    )
                }
            }
        }
    }
}

@Composable
private fun ProjectionWeightSlider(
    title: String,
    value: Double,
    onChanged: (Double) -> Unit
) {

    Column {

        Row(
            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.SpaceBetween
        ) {

            Text(
                title,
                fontSize = 12.sp
            )


            Text(
                String.format(
                    java.util.Locale.CANADA,
                    "%.0f%%",
                    value * 100.0
                ),

                fontSize =
                    12.sp
            )
        }


        Slider(
            value =
                value.toFloat(),

            onValueChange = {

                onChanged(
                    it.toDouble()
                )
            },

            valueRange =
                0f..1f
        )
    }
}

@Composable
private fun ProjectionSummary(
    projections: List<MatchupProjection>
) {

    val closest =
        projections
            .minByOrNull {

                abs(
                    it.topProbability -
                            0.5
                )
            }


    val strongest =
        projections
            .maxByOrNull {

                max(
                    it.topProbability,
                    it.bottomProbability
                )
            }


    Column(
        verticalArrangement =
            Arrangement.spacedBy(
                3.dp
            )
    ) {

        Text(
            text =
                "Model Projections",

            fontSize =
                21.sp,

            fontWeight =
                FontWeight.Bold
        )


        strongest
            ?.let {

                Text(
                    text =
                        "Strongest favourite: " +
                                "${it.projectedWinner.abbrev} " +
                                String.format(
                                    java.util.Locale.CANADA,
                                    "%.1f%%",
                                    max(
                                        it.topProbability,
                                        it.bottomProbability
                                    ) * 100.0
                                ),

                    fontSize =
                        12.sp
                )
            }


        closest
            ?.let {

                Text(
                    text =
                        "Closest matchup: " +
                                "${it.topTeam.abbrev} vs " +
                                it.bottomTeam.abbrev,

                    fontSize =
                        12.sp
                )
            }


        Text(
            text =
                "Calculated by this app; not an official NHL projection.",

            fontSize =
                10.sp,

            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        )
    }
}

@Composable
private fun SimulationControls(
    iterations: Int,
    onIterationsChanged: (Int) -> Unit
) {

    Card(
        modifier =
            Modifier.fillMaxWidth()
    ) {

        Column(
            modifier =
                Modifier.padding(
                    12.dp
                )
        ) {

            Text(
                text =
                    "Bracket Simulation",

                fontWeight =
                    FontWeight.Bold
            )


            Text(
                text =
                    "Iterations",

                fontSize =
                    11.sp,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )


            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(
                        6.dp
                    )
            ) {

                listOf(
                    1_000,
                    10_000,
                    25_000,
                    50_000
                )
                    .forEach {
                            value ->

                        FilterChip(
                            selected =
                                iterations ==
                                        value,

                            onClick = {

                                onIterationsChanged(
                                    value
                                )
                            },

                            label = {

                                Text(
                                    when {

                                        value >=
                                                1_000 ->

                                            "${value / 1_000}K"

                                        else ->
                                            value.toString()
                                    }
                                )
                            }
                        )
                    }
            }
        }
    }
}

@Composable
private fun SimulationResultsTable(
    result: PlayoffSimulationResult
) {

    Column(
        modifier =
            Modifier.fillMaxWidth()
    ) {

        Text(
            text =
                "Tournament Probabilities",

            fontSize =
                19.sp,

            fontWeight =
                FontWeight.Bold
        )


        Spacer(
            modifier =
                Modifier.height(
                    6.dp
                )
        )


        SimulationHeaderRow()


        result.teams
            .forEach {
                    team ->

                SimulationTeamRow(
                    result =
                        team
                )

                HorizontalDivider()
            }


        Text(
            text =
                "${result.iterations} simulated playoff brackets",

            fontSize =
                10.sp,

            modifier =
                Modifier.padding(
                    top = 6.dp
                ),

            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        )
    }
}

@Composable
private fun SimulationHeaderRow() {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                vertical = 4.dp
            ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Text(
            text =
                "Team",

            modifier =
                Modifier.weight(
                    1.5f
                ),

            fontSize =
                10.sp,

            fontWeight =
                FontWeight.Bold
        )


        listOf(
            "R2",
            "CF",
            "SCF",
            "Cup"
        )
            .forEach {
                    label ->

                Text(
                    text =
                        label,

                    modifier =
                        Modifier.weight(
                            0.7f
                        ),

                    fontSize =
                        10.sp,

                    fontWeight =
                        FontWeight.Bold,

                    textAlign =
                        TextAlign.End
                )
            }
    }
}

@Composable
private fun SimulationTeamRow(
    result: TeamSimulationResult
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                vertical = 5.dp
            ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Row(
            modifier =
                Modifier.weight(
                    1.5f
                ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            AsyncImage(
                model =
                    result.team.darkLogo
                        ?: result.team.logo,

                contentDescription =
                    result.team.name,

                modifier =
                    Modifier.size(
                        24.dp
                    ),

                contentScale =
                    ContentScale.Fit
            )


            Spacer(
                modifier =
                    Modifier.width(
                        5.dp
                    )
            )


            Text(
                text =
                    result.team.abbrev,

                fontWeight =
                    FontWeight.Bold,

                fontSize =
                    11.sp
            )
        }


        SimulationProbabilityCell(
            result.round2Probability
        )


        SimulationProbabilityCell(
            result.conferenceFinalProbability
        )


        SimulationProbabilityCell(
            result.cupFinalProbability
        )


        SimulationProbabilityCell(
            result.cupWinProbability
        )
    }
}

@Composable
private fun RowScope.SimulationProbabilityCell(
    probability: Double
) {

    Text(
        text =
            String.format(
                java.util.Locale.CANADA,
                "%.1f%%",
                probability * 100.0
            ),

        modifier =
            Modifier.weight(
                0.7f
            ),

        textAlign =
            TextAlign.End,

        fontSize =
            10.sp
    )
}