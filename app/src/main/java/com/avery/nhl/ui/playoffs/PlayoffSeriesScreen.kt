package com.avery.nhl.ui.playoffs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed

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

import com.avery.nhl.data.nhl.NHLRepository
import com.avery.nhl.model.nhl.NHLGame
import com.avery.nhl.model.nhl.NHLPlayoffSeries
import com.avery.nhl.model.nhl.NHLPlayoffSeriesDetail
import com.avery.nhl.model.nhl.NHLPlayoffTeam
import com.avery.nhl.ui.scores.GameDetailScreen

import kotlinx.coroutines.CancellationException

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.LocalDate

@Composable
fun PlayoffSeriesScreen(
    season: Int,
    series: NHLPlayoffSeries,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {

    val repository =
        remember {
            NHLRepository()
        }


    var detail by remember(
        season,
        series.seriesLetter
    ) {
        mutableStateOf<NHLPlayoffSeriesDetail?>(
            null
        )
    }


    var selectedGameId by remember {
        mutableStateOf<Long?>(
            null
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


    val gameId =
        selectedGameId


    if (gameId != null) {

        GameDetailScreen(
            gameId =
                gameId,

            onBack = {
                selectedGameId =
                    null
            },

            modifier =
                modifier
        )

        return
    }


    LaunchedEffect(
        season,
        series.seriesLetter,
        refreshKey
    ) {

        loading = true
        error = null


        try {

            detail =
                repository
                    .getPlayoffSeriesSchedule(
                        season =
                            season,

                        seriesLetter =
                            series.seriesLetter
                    )

        } catch (
            e: CancellationException
        ) {

            throw e

        } catch (
            e: Exception
        ) {

            detail = null

            error =
                e.message
                    ?: "Could not load playoff series."

        } finally {

            loading = false
        }
    }


    Column(
        modifier =
            modifier.fillMaxSize()
    ) {

        PlayoffSeriesTopBar(
            series =
                series,

            onBack =
                onBack,

            onRefresh = {
                refreshKey++
            }
        )


        PlayoffSeriesHeader(
            series =
                series
        )


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

                PlayoffSeriesError(
                    message =
                        error!!,

                    onRetry = {
                        refreshKey++
                    }
                )
            }


            detail != null -> {

                PlayoffGamesList(
                    games =
                        detail!!.games,

                    onGameClick = {
                        selectedGameId =
                            it
                    }
                )
            }
        }
    }
}

@Composable
private fun PlayoffSeriesTopBar(
    series: NHLPlayoffSeries,
    onBack: () -> Unit,
    onRefresh: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 8.dp,
                vertical = 6.dp
            ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        TextButton(
            onClick =
                onBack
        ) {

            Text(
                "‹ Playoffs"
            )
        }


        Text(
            text =
                series.seriesTitle,

            modifier =
                Modifier.weight(1f),

            textAlign =
                TextAlign.Center,

            fontSize =
                18.sp,

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
private fun PlayoffSeriesHeader(
    series: NHLPlayoffSeries
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                12.dp,
                10.dp
            )
    ) {

        Column(
            modifier =
                Modifier.padding(
                    12.dp
                )
        ) {

            PlayoffSeriesHeaderTeam(
                team =
                    series.topSeedTeam,

                wins =
                    series.topSeedWins,

                winner =
                    series.winningTeamId ==
                            series.topSeedTeam?.id
            )


            Text(
                text =
                    seriesScoreText(
                        series
                    ),

                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        vertical = 5.dp
                    ),

                textAlign =
                    TextAlign.Center,

                fontSize =
                    12.sp,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )


            PlayoffSeriesHeaderTeam(
                team =
                    series.bottomSeedTeam,

                wins =
                    series.bottomSeedWins,

                winner =
                    series.winningTeamId ==
                            series.bottomSeedTeam?.id
            )
        }
    }
}

private fun seriesScoreText(
    series: NHLPlayoffSeries
): String {

    val top =
        series.topSeedTeam?.abbrev
            ?: "TBD"

    val bottom =
        series.bottomSeedTeam?.abbrev
            ?: "TBD"


    return when {

        series.winningTeam != null -> {

            val winner =
                series.winningTeam!!

            val loserWins =
                if (
                    winner.id ==
                    series.topSeedTeam?.id
                ) {
                    series.bottomSeedWins
                } else {
                    series.topSeedWins
                }


            "${winner.abbrev} won series 4-$loserWins"
        }


        series.topSeedWins >
                series.bottomSeedWins ->

            "$top leads " +
                    "${series.topSeedWins}-${series.bottomSeedWins}"


        series.bottomSeedWins >
                series.topSeedWins ->

            "$bottom leads " +
                    "${series.bottomSeedWins}-${series.topSeedWins}"


        else ->

            "Series tied " +
                    "${series.topSeedWins}-${series.bottomSeedWins}"
    }
}

@Composable
private fun PlayoffSeriesHeaderTeam(
    team: NHLPlayoffTeam?,
    wins: Int,
    winner: Boolean
) {

    Row(
        modifier =
            Modifier.fillMaxWidth(),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        if (team != null) {

            AsyncImage(
                model =
                    team.darkLogo
                        ?: team.logo,

                contentDescription =
                    team.name,

                contentScale =
                    ContentScale.Fit,

                modifier =
                    Modifier.size(
                        42.dp
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

                    fontWeight =
                        if (winner) {
                            FontWeight.Bold
                        } else {
                            FontWeight.Normal
                        }
                )


                Text(
                    text =
                        team.abbrev,

                    fontSize =
                        11.sp,

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )
            }

        } else {

            Text(
                text =
                    "TBD",

                modifier =
                    Modifier.weight(1f)
            )
        }


        Text(
            text =
                wins.toString(),

            fontSize =
                26.sp,

            fontWeight =
                FontWeight.Bold
        )
    }
}

@Composable
private fun PlayoffGamesList(
    games: List<NHLGame>,
    onGameClick: (Long) -> Unit
) {

    if (games.isEmpty()) {

        Box(
            modifier =
                Modifier.fillMaxSize(),

            contentAlignment =
                Alignment.Center
        ) {

            Text(
                "No games are available for this series."
            )
        }

        return
    }


    LazyColumn(
        modifier =
            Modifier.fillMaxSize(),

        contentPadding =
            PaddingValues(
                start = 12.dp,
                end = 12.dp,
                bottom = 24.dp
            ),

        verticalArrangement =
            Arrangement.spacedBy(
                8.dp
            )
    ) {

        itemsIndexed(
            items =
                games,

            key = {
                    _,
                    game ->

                game.id
            }
        ) {
                index,
                game ->

            PlayoffGameCard(
                gameNumber =
                    index + 1,

                game =
                    game,

                onClick = {
                    onGameClick(
                        game.id
                    )
                }
            )
        }
    }
}

@Composable
private fun PlayoffGameCard(
    gameNumber: Int,
    game: NHLGame,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
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

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.SpaceBetween,

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Text(
                    text =
                        "Game $gameNumber",

                    fontWeight =
                        FontWeight.Bold
                )


                Text(
                    text =
                        playoffGameStatus(
                            game
                        ),

                    fontSize =
                        12.sp,

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )
            }


            Text(
                text =
                    playoffGameDate(
                        game
                    ),

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


            PlayoffGameTeamRow(
                abbreviation =
                    game.awayTeam.abbrev,

                name =
                    game.awayTeam.name,

                logo =
                    game.awayTeam.logo,

                score =
                    game.awayTeam.score
            )


            PlayoffGameTeamRow(
                abbreviation =
                    game.homeTeam.abbrev,

                name =
                    game.homeTeam.name,

                logo =
                    game.homeTeam.logo,

                score =
                    game.homeTeam.score
            )
        }
    }
}

@Composable
private fun PlayoffGameTeamRow(
    abbreviation: String,
    name: String,
    logo: String?,
    score: Int?
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                vertical = 2.dp
            ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        AsyncImage(
            model =
                logo,

            contentDescription =
                "$name logo",

            modifier =
                Modifier.size(
                    30.dp
                ),

            contentScale =
                ContentScale.Fit
        )


        Spacer(
            modifier =
                Modifier.width(
                    7.dp
                )
        )


        Text(
            text =
                if (
                    name.isNotBlank()
                ) {
                    name
                } else {
                    abbreviation
                },

            modifier =
                Modifier.weight(1f),

            fontSize =
                14.sp
        )


        if (score != null) {

            Text(
                text =
                    score.toString(),

                fontSize =
                    20.sp,

                fontWeight =
                    FontWeight.Bold
            )
        }
    }
}

private fun playoffGameStatus(
    game: NHLGame
): String {

    return when {

        game.isLive -> {

            if (
                game.clock?.inIntermission == true
            ) {

                "Intermission"

            } else {

                val period =
                    playoffPeriodText(
                        game
                    )


                val time =
                    game.clock
                        ?.timeRemaining


                listOfNotNull(
                    period,
                    time
                )
                    .joinToString(
                        " • "
                    )
            }
        }


        game.isFinal -> {

            when (
                game.periodType
                    ?.uppercase()
            ) {

                "OT" ->
                    "Final/OT"

                "SO" ->
                    "Final/SO"

                else ->
                    "Final"
            }
        }


        game.isPostponed ->

            "Postponed"


        else ->

            formatPlayoffStartTime(
                game.startTimeUTC
            )
    }
}

private fun playoffPeriodText(
    game: NHLGame
): String? {

    val period =
        game.period
            ?: return null


    return when {

        game.periodType
            ?.equals(
                "SO",
                ignoreCase = true
            ) == true ->

            "SO"


        period <= 3 ->

            "${period}${ordinalSuffix(period)}"


        period == 4 ->

            "OT"


        else ->

            "${period - 3}OT"
    }
}

private fun ordinalSuffix(
    number: Int
): String {

    return when {

        number % 100 in 11..13 ->
            "th"

        number % 10 == 1 ->
            "st"

        number % 10 == 2 ->
            "nd"

        number % 10 == 3 ->
            "rd"

        else ->
            "th"
    }
}

private fun playoffGameDate(
    game: NHLGame
): String {

    val date =
        try {

            LocalDate.parse(
                game.gameDate
            )

        } catch (_: Exception) {

            return game.gameDate
        }


    val dateText =
        date.format(
            DateTimeFormatter.ofPattern(
                "EEE, MMM d"
            )
        )


    val time =
        formatPlayoffStartTime(
            game.startTimeUTC
        )


    return if (
        game.isUpcoming &&
        time.isNotBlank()
    ) {

        "$dateText • $time"

    } else {

        dateText
    }
}

private fun formatPlayoffStartTime(
    value: String?
): String {

    if (
        value.isNullOrBlank()
    ) {
        return ""
    }


    return try {

        Instant
            .parse(
                value
            )
            .atZone(
                ZoneId.systemDefault()
            )
            .format(
                DateTimeFormatter.ofPattern(
                    "h:mm a"
                )
            )

    } catch (_: Exception) {

        ""
    }
}

@Composable
private fun PlayoffSeriesError(
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