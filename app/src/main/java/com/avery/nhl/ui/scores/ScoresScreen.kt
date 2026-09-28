package com.avery.nhl.ui.scores

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable

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
import com.avery.nhl.model.nhl.NHLGame
import com.avery.nhl.model.nhl.NHLGameTeam
import com.avery.nhl.model.nhl.formatStartTime

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.ZoneOffset

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds


@Composable
fun ScoresScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {

    val repository =
        remember {
            NHLRepository()
        }


    var selectedDateIso by rememberSaveable {
        mutableStateOf(
            LocalDate.now().toString()
        )
    }


    val selectedDate =
        remember(selectedDateIso) {
            LocalDate.parse(
                selectedDateIso
            )
        }


    var games by remember {
        mutableStateOf<List<NHLGame>>(
            emptyList()
        )
    }


    var loading by remember {
        mutableStateOf(true)
    }


    var error by remember {
        mutableStateOf<String?>(null)
    }


    var showScores by rememberSaveable {
        mutableStateOf(true)
    }


    var refreshKey by remember {
        mutableIntStateOf(0)
    }


    var selectedGameId by rememberSaveable {
        mutableStateOf<Long?>(null)
    }


    var showDatePicker by remember {
        mutableStateOf(false)
    }

    val gameId =
        selectedGameId

    var hasLoadedOnce by remember {
        mutableStateOf(false)
    }

    var refreshing by remember {
        mutableStateOf(false)
    }

    if (showDatePicker) {

        ScoresDatePicker(
            currentDate =
                selectedDate,

            onDateSelected = {
                    date ->

                selectedDateIso =
                    date.toString()
            },

            onDismiss = {
                showDatePicker = false
            }
        )
    }

    if (gameId != null) {

        GameDetailScreen(
            gameId = gameId,

            onBack = {
                selectedGameId = null
            },

            modifier = modifier
        )

        return
    }

    LaunchedEffect(
        selectedDate,
        refreshKey
    ) {

        if (!hasLoadedOnce) {
            loading = true
        }
        if (hasLoadedOnce) {
            refreshing = true
        }
        error = null

        try {

            games =
                repository.getScores(
                    selectedDate
                )

            hasLoadedOnce = true

        } catch (
            e: CancellationException
        ) {

            throw e

        } catch (
            e: Exception
        ) {

            games = emptyList()

            error =
                e.message
                    ?: "Could not load NHL scores."

        } finally {

            loading = false
        }
    }

    LaunchedEffect(
        selectedDate
    ) {
        hasLoadedOnce = false
    }

    val hasLiveGames =
        games.any {
            it.isLive
        }

    LaunchedEffect(
        selectedDate,
        hasLiveGames
    ) {

        if (!hasLiveGames) {
            return@LaunchedEffect
        }


        while (true) {

            delay(
                15_000L.milliseconds
            )

            refreshKey++
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        ScoresHeader(
            selectedDate =
                selectedDate,

            showScores =
                showScores,

            onBack =
                onBack,

            onPreviousDay = {

                selectedDateIso =
                    selectedDate
                        .minusDays(1)
                        .toString()
            },

            onNextDay = {

                selectedDateIso =
                    selectedDate
                        .plusDays(1)
                        .toString()
            },

            onToday = {

                selectedDateIso =
                    LocalDate.now()
                        .toString()
            },

            onOpenCalendar = {
                showDatePicker = true
            },

            onToggleScores = {
                showScores =
                    !showScores
            },

            onRefresh = {
                refreshKey++
            },

            refreshing = refreshing
        )

        Spacer(
            modifier =
                Modifier.height(12.dp)
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

                Column(
                    modifier =
                        Modifier.fillMaxWidth(),

                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {

                    Text(
                        text =
                            error
                                ?: "Unknown error",

                        color =
                            MaterialTheme
                                .colorScheme
                                .error
                    )

                    Button(
                        onClick = {
                            refreshKey++
                        },

                        modifier =
                            Modifier.padding(
                                top = 12.dp
                            )
                    ) {

                        Text("Retry")
                    }
                }
            }


            games.isEmpty() -> {

                Text(
                    text =
                        "No NHL games scheduled for this date.",

                    modifier =
                        Modifier.padding(
                            top = 24.dp
                        )
                )
            }


            else -> {

                LazyColumn(
                    verticalArrangement =
                        Arrangement.spacedBy(
                            10.dp
                        ),

                    modifier =
                        Modifier.fillMaxSize()
                ) {

                    items(
                        items = games,
                        key = { game ->
                            game.id
                        }
                    ) { game ->

                        NHLGameCard(
                            game = game,
                            showScores =
                                showScores,
                            onClick = {
                                selectedGameId =
                                    game.id
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScoresHeader(
    selectedDate: LocalDate,
    showScores: Boolean,
    refreshing: Boolean,

    onBack: () -> Unit,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onToday: () -> Unit,
    onToggleScores: () -> Unit,
    onRefresh: () -> Unit,
    onOpenCalendar: () -> Unit
) {

    val today =
        LocalDate.now()

    Column(
        modifier =
            Modifier.fillMaxWidth()
    ) {

        Row(
            modifier =
                Modifier.fillMaxWidth(),

            verticalAlignment =
                Alignment.CenterVertically,

            horizontalArrangement =
                Arrangement.SpaceBetween
        ) {

            Button(
                onClick = onBack
            ) {
                Text("Back")
            }

            Text(
                text = "NHL Scores",
                fontSize = 26.sp,
                fontWeight =
                    FontWeight.Bold
            )

            TextButton(
                onClick = onRefresh
            ) {
                Text("Refresh")
            }
        }


        Spacer(
            modifier =
                Modifier.height(12.dp)
        )


        Row(
            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.SpaceBetween,

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            OutlinedButton(
                onClick =
                    onPreviousDay
            ) {
                Text("‹")
            }


            Column(
                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {

                TextButton(
                    onClick =
                        onOpenCalendar
                ) {

                    Text(
                        text =
                            selectedDate.format(
                                DateTimeFormatter
                                    .ofLocalizedDate(
                                        FormatStyle.MEDIUM
                                    )
                            ),

                        fontWeight =
                            FontWeight.Bold
                    )
                }


                if (
                    selectedDate != today
                ) {

                    TextButton(
                        onClick =
                            onToday
                    ) {
                        Text("Today")
                    }
                }
            }


            OutlinedButton(
                onClick =
                    onNextDay
            ) {
                Text("›")
            }
        }


        Row(
            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.End,

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Text(
                text =
                    if (showScores) {
                        "Scores visible"
                    } else {
                        "Spoiler mode"
                    },

                fontSize = 12.sp
            )

            Spacer(
                modifier =
                    Modifier.width(8.dp)
            )

            Switch(
                checked = showScores,
                onCheckedChange = {
                    onToggleScores()
                }
            )
        }
    }

    Row(
        verticalAlignment =
            Alignment.CenterVertically
    ) {

        if (refreshing) {

            CircularProgressIndicator(
                modifier =
                    Modifier.size(16.dp),

                strokeWidth = 2.dp
            )

            Spacer(
                modifier =
                    Modifier.width(4.dp)
            )
        }


        TextButton(
            onClick =
                onRefresh
        ) {

            Text("Refresh")
        }
    }
}

@Composable
private fun NHLGameCard(
    game: NHLGame,
    showScores: Boolean,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick
            ),

        colors =
            if (game.isLive) {

                CardDefaults.cardColors(
                    containerColor =
                        MaterialTheme
                            .colorScheme
                            .secondaryContainer
                )

            } else {

                CardDefaults.cardColors()
            }
    ) {

        Column(
            modifier =
                Modifier.padding(12.dp)
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
                        gameStatusText(game),

                    fontWeight =
                        FontWeight.Bold,

                    fontSize = 13.sp
                )

                if (
                    game.isUpcoming
                ) {

                    game.startTimeUTC
                        ?.let {
                            formatStartTime(it)
                        }
                        ?.let { time ->

                            Text(
                                text = time,
                                fontSize = 13.sp
                            )
                        }
                }
            }


            HorizontalDivider(
                modifier =
                    Modifier.padding(
                        vertical = 8.dp
                    )
            )


            GameTeamRow(
                team =
                    game.awayTeam,

                showScore =
                    showScores &&
                            !game.isUpcoming
            )


            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )


            GameTeamRow(
                team =
                    game.homeTeam,

                showScore =
                    showScores &&
                            !game.isUpcoming
            )


            if (
                game.isLive &&
                !showScores
            ) {

                Text(
                    text =
                        "Game in progress",

                    fontSize = 12.sp,

                    modifier =
                        Modifier.padding(
                            top = 8.dp
                        )
                )
            }
        }
    }
}

@Composable
private fun GameTeamRow(
    team: NHLGameTeam,
    showScore: Boolean
) {

    Row(
        modifier =
            Modifier.fillMaxWidth(),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        if (
            team.logo != null
        ) {

            AsyncImage(
                model = team.logo,

                contentDescription =
                    "${team.name} logo",

                contentScale =
                    ContentScale.Fit,

                modifier =
                    Modifier.size(42.dp)
            )

        } else {

            Box(
                modifier =
                    Modifier.size(42.dp),

                contentAlignment =
                    Alignment.Center
            ) {

                Text(
                    text =
                        team.abbrev,

                    fontSize = 11.sp
                )
            }
        }


        Spacer(
            modifier =
                Modifier.width(10.dp)
        )


        Column(
            modifier =
                Modifier.weight(1f)
        ) {

            Text(
                text =
                    team.name,

                fontWeight =
                    FontWeight.SemiBold
            )

            Text(
                text =
                    team.abbrev,

                fontSize = 11.sp
            )
        }


        if (
            showScore
        ) {

            Text(
                text =
                    team.score
                        ?.toString()
                        ?: "-",

                fontSize = 28.sp,

                fontWeight =
                    FontWeight.Bold,

                textAlign =
                    TextAlign.End
            )
        }
    }
}

private fun gameStatusText(
    game: NHLGame
): String {

    return when {

        game.isLive -> {

            if (
                game.clock
                    ?.inIntermission == true
            ) {

                val period =
                    formatPeriod(
                        game.period,
                        game.periodType
                    )

                if (
                    period.isBlank()
                ) {
                    "INTERMISSION"
                } else {
                    "$period INTERMISSION"
                }

            } else {

                val period =
                    formatPeriod(
                        game.period,
                        game.periodType
                    )


                val remaining =
                    game.clock
                        ?.timeRemaining
                        .orEmpty()


                listOf(
                    "LIVE",
                    period,
                    remaining
                )
                    .filter {
                        it.isNotBlank()
                    }
                    .joinToString(
                        " • "
                    )
            }
        }


        game.isFinal -> {

            when (
                game.periodType
            ) {

                "OT" ->
                    "FINAL / OT"

                "SO" ->
                    "FINAL / SO"

                else ->
                    "FINAL"
            }
        }


        game.isPostponed ->
            "POSTPONED"


        else ->
            "UPCOMING"
    }
}

private fun formatPeriod(
    period: Int?,
    periodType: String?
): String {

    if (
        periodType == "SO"
    ) {
        return "SO"
    }


    if (
        periodType == "OT"
    ) {

        return when {

            period == null ->
                "OT"

            period <= 4 ->
                "OT"

            else ->
                "${period - 3}OT"
        }
    }


    return when (period) {

        1 -> "1st"
        2 -> "2nd"
        3 -> "3rd"

        null -> ""

        else ->
            "${period}th"
    }
}

@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
private fun ScoresDatePicker(
    currentDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {

    val initialMillis =
        currentDate
            .atStartOfDay(
                ZoneOffset.UTC
            )
            .toInstant()
            .toEpochMilli()


    val state =
        rememberDatePickerState(
            initialSelectedDateMillis =
                initialMillis
        )


    DatePickerDialog(
        onDismissRequest =
            onDismiss,

        confirmButton = {

            TextButton(
                onClick = {

                    state.selectedDateMillis
                        ?.let { millis ->

                            val date =
                                Instant
                                    .ofEpochMilli(
                                        millis
                                    )
                                    .atZone(
                                        ZoneOffset.UTC
                                    )
                                    .toLocalDate()


                            onDateSelected(
                                date
                            )
                        }

                    onDismiss()
                }
            ) {

                Text("OK")
            }
        },

        dismissButton = {

            TextButton(
                onClick =
                    onDismiss
            ) {

                Text("Cancel")
            }
        }
    ) {

        DatePicker(
            state = state
        )
    }
}