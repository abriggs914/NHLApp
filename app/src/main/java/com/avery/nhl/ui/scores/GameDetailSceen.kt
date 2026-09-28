package com.avery.nhl.ui.scores

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

import com.avery.nhl.data.nhl.NHLRepository
import com.avery.nhl.model.nhl.*

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds


@Composable
fun GameDetailScreen(
    gameId: Long,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {

    val repository =
        remember {
            NHLRepository()
        }

    var boxscore by remember(
        gameId
    ) {
        mutableStateOf<NHLBoxscore?>(
            null
        )
    }

    var detail by remember(
        gameId
    ) {
        mutableStateOf<NHLGameDetail?>(
            null
        )
    }


    var loading by remember(
        gameId
    ) {
        mutableStateOf(true)
    }


    var error by remember(
        gameId
    ) {
        mutableStateOf<String?>(
            null
        )
    }


    var refreshKey by remember {
        mutableIntStateOf(0)
    }


    LaunchedEffect(
        gameId,
        refreshKey
    ) {

        loading = true
        error = null


        try {

            detail =
                repository.getGameDetail(
                    gameId
                )


            boxscore =
                try {
                    repository.getGameBoxscore(
                        gameId
                    )
                } catch (
                    _: Exception
                ) {
                    null
                }

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
                    ?: "Could not load game details."

        } finally {

            loading = false
        }
    }

    val isLive =
        detail?.isLive == true


    var hasLoadedOnce by remember(
        gameId
    ) {
        mutableStateOf(false)
    }

    var refreshing by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(
        gameId,
        refreshKey
    ) {

        if (!hasLoadedOnce) {
            loading = true
            refreshing = true
        }

        error = null


        try {

            detail =
                repository.getGameDetail(
                    gameId
                )


            boxscore =
                try {

                    repository.getGameBoxscore(
                        gameId
                    )

                } catch (
                    _: Exception
                ) {

                    null
                }


            hasLoadedOnce = true

        } catch (
            e: CancellationException
        ) {

            throw e

        } catch (
            e: Exception
        ) {

            if (!hasLoadedOnce) {

                detail = null

                error =
                    e.message
                        ?: "Could not load game details."
            }

        } finally {

            loading = false
        }
    }

    LaunchedEffect(
        gameId,
        isLive
    ) {

        if (!isLive) {
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
    ) {

        GameDetailTopBar(
            onBack = onBack,

            onRefresh = {
                refreshKey++
            }
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
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),

                    horizontalAlignment =
                        Alignment.CenterHorizontally,

                    verticalArrangement =
                        Arrangement.Center
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
                                top = 16.dp
                            )
                    ) {

                        Text("Retry")
                    }
                }
            }


            detail != null -> {

                GameDetailContent(
                    detail = detail!!,
                    boxscore = boxscore
                )
            }
        }
    }
}

@Composable
private fun GameDetailTopBar(
    onBack: () -> Unit,
    onRefresh: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 12.dp,
                vertical = 8.dp
            ),

        verticalAlignment =
            Alignment.CenterVertically,

        horizontalArrangement =
            Arrangement.SpaceBetween
    ) {

        TextButton(
            onClick = onBack
        ) {
            Text("‹ Scores")
        }


        Text(
            text = "Game Detail",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )


        TextButton(
            onClick = onRefresh
        ) {
            Text("Refresh")
        }
    }
}

@Composable
private fun GameDetailContent(
    detail: NHLGameDetail,
    boxscore: NHLBoxscore?
) {

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = 12.dp
            ),

        verticalArrangement =
            Arrangement.spacedBy(
                12.dp
            ),

        contentPadding =
            PaddingValues(
                bottom = 32.dp
            )
    ) {

        item {

            GameScoreHeader(
                detail = detail
            )
        }

        if (boxscore != null) {

            item {

                GameLeadersSection(
                    boxscore =
                        boxscore
                )
            }
        }

        if (boxscore != null) {

            item {

                TeamComparisonCard(
                    away = boxscore.awayTeam,
                    home = boxscore.homeTeam
                )
            }
        }

        if (
            !detail.venue.isNullOrBlank() ||
            detail.attendance != null
        ) {

            item {

                GameInformationCard(
                    detail = detail
                )
            }
        }


        if (
            detail.scoringPeriods
                .any {
                    it.goals.isNotEmpty()
                }
        ) {

            item {

                Text(
                    text = "Scoring Summary",

                    fontSize = 20.sp,

                    fontWeight =
                        FontWeight.Bold
                )
            }


            items(
                detail.scoringPeriods
            ) { period ->

                ScoringPeriodCard(
                    period = period
                )
            }
        }

        if (
            boxscore != null &&
            (
                    boxscore.awayTeam.goalies.isNotEmpty() ||
                            boxscore.homeTeam.goalies.isNotEmpty()
                    )
        ) {

            item {

                Text(
                    text = "Goaltending",

                    fontSize = 20.sp,

                    fontWeight =
                        FontWeight.Bold
                )
            }


            item {

                GoalieTable(
                    team =
                        boxscore.awayTeam
                )
            }


            item {

                GoalieTable(
                    team =
                        boxscore.homeTeam
                )
            }
        }

        if (
            detail.threeStars.isNotEmpty()
        ) {

            item {

                ThreeStarsSection(
                    stars =
                        detail.threeStars
                )
            }
        }
    }
}

@Composable
private fun GameScoreHeader(
    detail: NHLGameDetail
) {

    Card(
        modifier =
            Modifier.fillMaxWidth()
    ) {

        Column(
            modifier =
                Modifier.padding(16.dp)
        ) {

            Text(
                text =
                    detailStatusText(
                        detail
                    ),

                modifier =
                    Modifier.fillMaxWidth(),

                textAlign =
                    TextAlign.Center,

                fontWeight =
                    FontWeight.Bold
            )


            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )


            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                DetailTeam(
                    team =
                        detail.awayTeam,

                    modifier =
                        Modifier.weight(1f)
                )


                Text(
                    text =
                        "${detail.awayTeam.score ?: "-"}  –  ${detail.homeTeam.score ?: "-"}",

                    fontWeight =
                        FontWeight.Bold,

                    fontSize = 30.sp,

                    textAlign =
                        TextAlign.Center,

                    modifier =
                        Modifier.weight(1f)
                )


                DetailTeam(
                    team =
                        detail.homeTeam,

                    modifier =
                        Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun DetailTeam(
    team: NHLGameTeam,
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier,

        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        team.logo?.let { logo ->

            AsyncImage(
                model = logo,

                contentDescription =
                    "${team.name} logo",

                contentScale =
                    ContentScale.Fit,

                modifier =
                    Modifier.size(60.dp)
            )
        }


        Text(
            text = team.abbrev,

            fontWeight =
                FontWeight.Bold,

            fontSize = 18.sp
        )
    }
}

private fun detailStatusText(
    detail: NHLGameDetail
): String {

    val live =
        detail.gameState == "LIVE" ||
                detail.gameState == "CRIT"


    val final =
        detail.gameState == "FINAL" ||
                detail.gameState == "OFF"


    return when {

        live -> {

            val period =
                when (
                    detail.periodType
                ) {

                    "OT" -> "OT"
                    "SO" -> "SO"

                    else ->
                        detail.period
                            ?.let {
                                when (it) {
                                    1 -> "1st"
                                    2 -> "2nd"
                                    3 -> "3rd"
                                    else -> "$it"
                                }
                            }
                            ?: ""
                }


            val clock =
                if (
                    detail.clock
                        ?.inIntermission == true
                ) {
                    "Intermission"
                } else {
                    detail.clock
                        ?.timeRemaining
                        .orEmpty()
                }


            listOf(
                "LIVE",
                period,
                clock
            )
                .filter {
                    it.isNotBlank()
                }
                .joinToString(" • ")
        }


        final -> {

            when (
                detail.periodType
            ) {

                "OT" ->
                    "FINAL / OT"

                "SO" ->
                    "FINAL / SO"

                else ->
                    "FINAL"
            }
        }


        else ->
            "Upcoming"
    }
}

@Composable
private fun GameInformationCard(
    detail: NHLGameDetail
) {

    Card(
        modifier =
            Modifier.fillMaxWidth()
    ) {

        Column(
            modifier =
                Modifier.padding(12.dp)
        ) {

            Text(
                text = "Game Information",

                fontWeight =
                    FontWeight.Bold,

                fontSize = 17.sp
            )


            detail.venue
                ?.let {

                    Text(
                        text =
                            "Venue: $it",

                        modifier =
                            Modifier.padding(
                                top = 6.dp
                            )
                    )
                }


            detail.attendance
                ?.let {

                    Text(
                        text =
                            "Attendance: %,d"
                                .format(it)
                    )
                }
        }
    }
}

@Composable
private fun ScoringPeriodCard(
    period: NHLScoringPeriod
) {

    Card(
        modifier =
            Modifier.fillMaxWidth()
    ) {

        Column(
            modifier =
                Modifier.padding(12.dp)
        ) {

            Text(
                text =
                    periodLabel(period),

                fontWeight =
                    FontWeight.Bold,

                fontSize = 16.sp
            )


            if (
                period.goals.isEmpty()
            ) {

                Text(
                    text = "No scoring",

                    modifier =
                        Modifier.padding(
                            top = 8.dp
                        )
                )

            } else {

                period.goals.forEachIndexed {
                        index,
                        goal ->

                    if (index > 0) {

                        HorizontalDivider(
                            modifier =
                                Modifier.padding(
                                    vertical = 8.dp
                                )
                        )
                    }


                    GoalRow(
                        goal = goal
                    )
                }
            }
        }
    }
}

private fun periodLabel(
    period: NHLScoringPeriod
): String {

    return when (
        period.periodType
    ) {

        "OT" -> "Overtime"
        "SO" -> "Shootout"

        else ->
            when (
                period.period
            ) {

                1 ->
                    "1st Period"

                2 ->
                    "2nd Period"

                3 ->
                    "3rd Period"

                else ->
                    "Period ${period.period}"
            }
    }
}

@Composable
private fun GoalRow(
    goal: NHLGoal
) {

    Row(
        modifier =
            Modifier.fillMaxWidth(),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        if (
            !goal.headshot.isNullOrBlank()
        ) {

            AsyncImage(
                model =
                    goal.headshot,

                contentDescription =
                    goal.playerName,

                contentScale =
                    ContentScale.Fit,

                modifier =
                    Modifier.size(52.dp)
            )

        } else {

            Box(
                modifier =
                    Modifier.size(52.dp),

                contentAlignment =
                    Alignment.Center
            ) {

                Text("🏒")
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

            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Text(
                    text =
                        goal.playerName,

                    fontWeight =
                        FontWeight.Bold
                )


                goal.goalsToDate
                    ?.let {

                        Text(
                            text =
                                " ($it)",

                            fontSize = 12.sp
                        )
                    }
            }


            val details =
                listOfNotNull(
                    goal.teamAbbrev,

                    goal.timeInPeriod,

                    goal.strength
                        ?.takeIf {
                            it.isNotBlank()
                        },

                    goal.shotType
                        ?.takeIf {
                            it.isNotBlank()
                        }
                )


            Text(
                text =
                    details.joinToString(
                        " • "
                    ),

                fontSize = 12.sp
            )


            if (
                goal.assists.isNotEmpty()
            ) {

                Text(
                    text =
                        "Assists: " +
                                goal.assists
                                    .joinToString(", ") {
                                            assist ->

                                        if (
                                            assist.assistsToDate != null
                                        ) {
                                            "${assist.name} (${assist.assistsToDate})"
                                        } else {
                                            assist.name
                                        }
                                    },

                    fontSize = 11.sp,

                    modifier =
                        Modifier.padding(
                            top = 3.dp
                        )
                )
            }
        }


        if (
            goal.awayScore != null &&
            goal.homeScore != null
        ) {

            Text(
                text =
                    "${goal.awayScore}-${goal.homeScore}",

                fontWeight =
                    FontWeight.Bold,

                fontSize = 18.sp
            )
        }
    }
}

@Composable
private fun ThreeStarsSection(
    stars: List<NHLGameStar>
) {

    Column {

        Text(
            text = "Three Stars",

            fontSize = 20.sp,

            fontWeight =
                FontWeight.Bold
        )


        Spacer(
            modifier =
                Modifier.height(6.dp)
        )


        stars
            .take(3)
            .forEachIndexed {
                    index,
                    star ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            vertical = 4.dp
                        )
                ) {

                    Row(
                        modifier =
                            Modifier.padding(
                                10.dp
                            ),

                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Text(
                            text =
                                when (index) {
                                    0 -> "⭐"
                                    1 -> "⭐⭐"
                                    else -> "⭐⭐⭐"
                                },

                            modifier =
                                Modifier.width(
                                    58.dp
                                )
                        )


                        if (
                            !star.headshot
                                .isNullOrBlank()
                        ) {

                            AsyncImage(
                                model =
                                    star.headshot,

                                contentDescription =
                                    star.name,

                                modifier =
                                    Modifier.size(
                                        48.dp
                                    )
                            )
                        }


                        Spacer(
                            modifier =
                                Modifier.width(8.dp)
                        )


                        Column {

                            Text(
                                text =
                                    star.name,

                                fontWeight =
                                    FontWeight.Bold
                            )


                            Text(
                                text =
                                    listOfNotNull(
                                        star.teamAbbrev,
                                        star.position
                                    )
                                        .joinToString(
                                            " • "
                                        ),

                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
    }
}

@Composable
private fun TeamComparisonCard(
    away: NHLBoxscoreTeam,
    home: NHLBoxscoreTeam
) {

    Card(
        modifier =
            Modifier.fillMaxWidth()
    ) {

        Column(
            modifier =
                Modifier.padding(12.dp)
        ) {

            Text(
                text = "Team Stats",
                fontSize = 20.sp,
                fontWeight =
                    FontWeight.Bold
            )


            Spacer(
                modifier =
                    Modifier.height(10.dp)
            )


            StatComparisonHeader(
                away = away.abbrev,
                home = home.abbrev
            )


            StatComparisonRow(
                awayValue =
                    away.shotsOnGoal
                        ?.toString()
                        ?: "-",

                label =
                    "Shots",

                homeValue =
                    home.shotsOnGoal
                        ?.toString()
                        ?: "-"
            )


            StatComparisonRow(
                awayValue =
                    away.skaters
                        .sumOf {
                            it.hits ?: 0
                        }
                        .toString(),

                label =
                    "Hits",

                homeValue =
                    home.skaters
                        .sumOf {
                            it.hits ?: 0
                        }
                        .toString()
            )


            StatComparisonRow(
                awayValue =
                    away.skaters
                        .sumOf {
                            it.blockedShots ?: 0
                        }
                        .toString(),

                label =
                    "Blocks",

                homeValue =
                    home.skaters
                        .sumOf {
                            it.blockedShots ?: 0
                        }
                        .toString()
            )


            StatComparisonRow(
                awayValue =
                    away.skaters
                        .sumOf {
                            it.pim ?: 0
                        }
                        .toString(),

                label =
                    "PIM",

                homeValue =
                    home.skaters
                        .sumOf {
                            it.pim ?: 0
                        }
                        .toString()
            )
        }
    }
}

@Composable
private fun StatComparisonHeader(
    away: String,
    home: String
) {

    Row(
        modifier =
            Modifier.fillMaxWidth(),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Text(
            text = away,

            modifier =
                Modifier.weight(1f),

            textAlign =
                TextAlign.Start,

            fontWeight =
                FontWeight.Bold
        )


        Text(
            text = "STAT",

            modifier =
                Modifier.weight(1f),

            textAlign =
                TextAlign.Center,

            fontSize = 11.sp
        )


        Text(
            text = home,

            modifier =
                Modifier.weight(1f),

            textAlign =
                TextAlign.End,

            fontWeight =
                FontWeight.Bold
        )
    }
}

@Composable
private fun StatComparisonRow(
    awayValue: String,
    label: String,
    homeValue: String
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

        Text(
            text = awayValue,

            modifier =
                Modifier.weight(1f),

            textAlign =
                TextAlign.Start,

            fontWeight =
                FontWeight.SemiBold
        )


        Text(
            text = label,

            modifier =
                Modifier.weight(1f),

            textAlign =
                TextAlign.Center,

            fontSize = 12.sp
        )


        Text(
            text = homeValue,

            modifier =
                Modifier.weight(1f),

            textAlign =
                TextAlign.End,

            fontWeight =
                FontWeight.SemiBold
        )
    }
}

@Composable
private fun GoalieTable(
    team: NHLBoxscoreTeam
) {

    Card(
        modifier =
            Modifier.fillMaxWidth()
    ) {

        Column(
            modifier =
                Modifier.padding(10.dp)
        ) {

            Text(
                text =
                    "${team.abbrev} Goalies",

                fontWeight =
                    FontWeight.Bold,

                fontSize = 16.sp
            )


            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )


            GoalieTableHeader()


            team.goalies.forEach {
                    goalie ->

                HorizontalDivider()


                GoalieTableRow(
                    goalie = goalie
                )
            }
        }
    }
}

@Composable
private fun GoalieTableHeader() {

    Row(
        modifier =
            Modifier.fillMaxWidth(),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Text(
            text = "Goalie",
            modifier =
                Modifier.weight(2.3f),
            fontSize = 11.sp,
            fontWeight =
                FontWeight.Bold
        )

        TableHeaderCell(
            "SA",
            0.7f
        )

        TableHeaderCell(
            "SV",
            0.7f
        )

        TableHeaderCell(
            "GA",
            0.7f
        )

        TableHeaderCell(
            "SV%",
            1f
        )

        TableHeaderCell(
            "TOI",
            1f
        )
    }
}

@Composable
private fun RowScope.TableHeaderCell(
    text: String,
    weight: Float
) {

    Text(
        text = text,

        modifier =
            Modifier.weight(weight),

        textAlign =
            TextAlign.Center,

        fontSize = 10.sp,

        fontWeight =
            FontWeight.Bold
    )
}

@Composable
private fun GoalieTableRow(
    goalie: NHLBoxscoreGoalie
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                vertical = 7.dp
            ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Column(
            modifier =
                Modifier.weight(2.3f)
        ) {

            Text(
                text =
                    buildString {

                        goalie.sweaterNumber
                            ?.let {
                                append("#$it ")
                            }

                        append(
                            goalie.name
                        )
                    },

                fontSize = 12.sp,

                fontWeight =
                    FontWeight.SemiBold
            )


            goalie.decision
                ?.takeIf {
                    it.isNotBlank()
                }
                ?.let {

                    Text(
                        text = it,
                        fontSize = 10.sp
                    )
                }
        }


        GoalieStatCell(
            goalie.shotsAgainst
                ?.toString()
                ?: "-",
            0.7f
        )


        GoalieStatCell(
            goalie.saves
                ?.toString()
                ?: "-",
            0.7f
        )


        GoalieStatCell(
            goalie.goalsAgainst
                ?.toString()
                ?: "-",
            0.7f
        )


        GoalieStatCell(
            formatSavePct(
                goalie.savePct
            ),
            1f
        )


        GoalieStatCell(
            goalie.toi
                ?: "-",
            1f
        )
    }
}

@Composable
private fun RowScope.GoalieStatCell(
    text: String,
    weight: Float
) {

    Text(
        text = text,

        modifier =
            Modifier.weight(weight),

        textAlign =
            TextAlign.Center,

        fontSize = 11.sp
    )
}

private fun formatSavePct(
    value: Double?
): String {

    if (value == null) {
        return "-"
    }


    return when {

        value <= 1.0 ->
            "%.3f".format(
                value
            )

        else ->
            "%.3f".format(
                value / 100.0
            )
    }
}

@Composable
private fun GameLeadersSection(
    boxscore: NHLBoxscore
) {

    val allPlayers =
        boxscore.awayTeam.skaters +
                boxscore.homeTeam.skaters


    val pointsLeader =
        allPlayers.maxWithOrNull(
            compareBy<NHLBoxscoreSkater> {
                it.points ?: 0
            }
                .thenBy {
                    it.goals ?: 0
                }
        )


    val shotsLeader =
        allPlayers.maxByOrNull {
            it.shotsOnGoal ?: 0
        }


    val hitsLeader =
        allPlayers.maxByOrNull {
            it.hits ?: 0
        }


    Card(
        modifier =
            Modifier.fillMaxWidth()
    ) {

        Column(
            modifier =
                Modifier.padding(12.dp)
        ) {

            Text(
                text = "Game Leaders",

                fontSize = 20.sp,

                fontWeight =
                    FontWeight.Bold
            )


            pointsLeader?.let {

                LeaderRow(
                    label =
                        "Points",

                    player =
                        it.name,

                    value =
                        "${it.points ?: 0}"
                )
            }


            shotsLeader?.let {

                LeaderRow(
                    label =
                        "Shots",

                    player =
                        it.name,

                    value =
                        "${it.shotsOnGoal ?: 0}"
                )
            }


            hitsLeader?.let {

                LeaderRow(
                    label =
                        "Hits",

                    player =
                        it.name,

                    value =
                        "${it.hits ?: 0}"
                )
            }
        }
    }
}

@Composable
private fun LeaderRow(
    label: String,
    player: String,
    value: String
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

        Text(
            text = label,
            modifier =
                Modifier.width(65.dp),
            fontSize = 12.sp
        )


        Text(
            text = player,

            modifier =
                Modifier.weight(1f),

            fontWeight =
                FontWeight.SemiBold
        )


        Text(
            text = value,

            fontWeight =
                FontWeight.Bold
        )
    }
}