package com.avery.nhl.ui.standings

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
import com.avery.nhl.model.nhl.NHLStandings
import com.avery.nhl.model.nhl.NHLStandingsTeam
import com.avery.nhl.model.nhl.StandingsView

import kotlinx.coroutines.CancellationException

@Composable
fun StandingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {

    val repository =
        remember {
            NHLRepository()
        }


    var standings by remember {
        mutableStateOf<NHLStandings?>(
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


    var view by remember {
        mutableStateOf(
            StandingsView.DIVISION
        )
    }


    var refreshKey by remember {
        mutableIntStateOf(0)
    }


    LaunchedEffect(
        refreshKey
    ) {

        loading = true
        error = null


        try {

            standings =
                repository.getStandings()

        } catch (
            e: CancellationException
        ) {

            throw e

        } catch (
            e: Exception
        ) {

            standings = null

            error =
                e.message
                    ?: "Could not load NHL standings."

        } finally {

            loading = false
        }
    }


    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                horizontal = 12.dp
            )
    ) {

        StandingsTopBar(
            onBack = onBack,

            onRefresh = {
                refreshKey++
            }
        )


        StandingsViewSelector(
            view = view,

            onViewChanged = {
                view = it
            }
        )


        Spacer(
            modifier =
                Modifier.height(8.dp)
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
                        Modifier.fillMaxSize(),

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
                                top = 12.dp
                            )
                    ) {

                        Text("Retry")
                    }
                }
            }


            standings != null -> {

                val data =
                    standings!!


                data.date
                    ?.let {

                        Text(
                            text =
                                "Standings as of $it",

                            fontSize = 11.sp,

                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant,

                            modifier =
                                Modifier.padding(
                                    bottom = 5.dp
                                )
                        )
                    }


                StandingsContent(
                    standings =
                        data,

                    view =
                        view
                )
            }
        }
    }
}

@Composable
private fun StandingsTopBar(
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
            onClick = onBack
        ) {

            Text("‹ Home")
        }


        Text(
            text = "NHL Standings",

            fontSize = 22.sp,

            fontWeight =
                FontWeight.Bold
        )


        TextButton(
            onClick = onRefresh
        ) {

            Text("Refresh")
        }
    }
}

@Composable
private fun StandingsViewSelector(
    view: StandingsView,
    onViewChanged: (StandingsView) -> Unit
) {

    SingleChoiceSegmentedButtonRow(
        modifier =
            Modifier.fillMaxWidth()
    ) {

        StandingsView.entries
            .forEachIndexed {
                    index,
                    item ->

                SegmentedButton(
                    selected =
                        view == item,

                    onClick = {
                        onViewChanged(
                            item
                        )
                    },

                    shape =
                        SegmentedButtonDefaults
                            .itemShape(
                                index =
                                    index,

                                count =
                                    StandingsView
                                        .entries
                                        .size
                            )
                ) {

                    Text(
                        when (item) {

                            StandingsView.LEAGUE ->
                                "League"

                            StandingsView.CONFERENCE ->
                                "Conference"

                            StandingsView.DIVISION ->
                                "Division"
                        }
                    )
                }
            }
    }
}

@Composable
private fun StandingsContent(
    standings: NHLStandings,
    view: StandingsView
) {

    when (view) {

        StandingsView.LEAGUE -> {

            LeagueStandings(
                teams =
                    standings.teams
            )
        }


        StandingsView.CONFERENCE -> {

            ConferenceStandings(
                teams =
                    standings.teams
            )
        }


        StandingsView.DIVISION -> {

            DivisionStandings(
                teams =
                    standings.teams
            )
        }
    }
}

@Composable
private fun LeagueStandings(
    teams: List<NHLStandingsTeam>
) {

    val sorted =
        teams.sortedWith(
            compareBy<NHLStandingsTeam> {
                it.leagueSequence
                    ?: Int.MAX_VALUE
            }
                .thenByDescending {
                    it.points
                }
        )


    StandingsTable(
        teams = sorted,

        rankForTeam = {
            it.leagueSequence
        }
    )
}

@Composable
private fun ConferenceStandings(
    teams: List<NHLStandingsTeam>
) {

    val groups =
        teams
            .groupBy {
                it.conferenceName
                    ?: "Unknown"
            }
            .toSortedMap()


    LazyColumn(
        verticalArrangement =
            Arrangement.spacedBy(
                12.dp
            ),

        contentPadding =
            PaddingValues(
                bottom = 24.dp
            )
    ) {

        groups.forEach {
                conference,
                conferenceTeams ->

            item(
                key =
                    "conference-$conference"
            ) {

                StandingsSectionHeader(
                    title =
                        "$conference Conference"
                )
            }


            item(
                key =
                    "conference-table-$conference"
            ) {

                StandingsTableBody(
                    teams =
                        conferenceTeams
                            .sortedWith(
                                compareBy {
                                    it.conferenceSequence
                                        ?: Int.MAX_VALUE
                                }
                            ),

                    rankForTeam = {
                        it.conferenceSequence
                    }
                )
            }
        }
    }
}

@Composable
private fun DivisionStandings(
    teams: List<NHLStandingsTeam>
) {

    val divisionOrder =
        listOf(
            "Atlantic",
            "Metropolitan",
            "Central",
            "Pacific"
        )


    val grouped =
        teams.groupBy {
            it.divisionName
                ?: "Unknown"
        }


    LazyColumn(
        verticalArrangement =
            Arrangement.spacedBy(
                12.dp
            ),

        contentPadding =
            PaddingValues(
                bottom = 24.dp
            )
    ) {

        divisionOrder.forEach {
                division ->

            val divisionTeams =
                grouped[division]
                    ?: emptyList()


            if (
                divisionTeams.isNotEmpty()
            ) {

                item(
                    key =
                        "division-$division"
                ) {

                    StandingsSectionHeader(
                        title =
                            "$division Division"
                    )
                }


                item(
                    key =
                        "division-table-$division"
                ) {

                    StandingsTableBody(
                        teams =
                            divisionTeams
                                .sortedWith(
                                    compareBy {
                                        it.divisionSequence
                                            ?: Int.MAX_VALUE
                                    }
                                ),

                        rankForTeam = {
                            it.divisionSequence
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun StandingsTable(
    teams: List<NHLStandingsTeam>,
    rankForTeam: (NHLStandingsTeam) -> Int?
) {

    LazyColumn(
        contentPadding =
            PaddingValues(
                bottom = 24.dp
            )
    ) {

        item {

            StandingsTableBody(
                teams =
                    teams,

                rankForTeam =
                    rankForTeam
            )
        }
    }
}

@Composable
private fun StandingsTableBody(
    teams: List<NHLStandingsTeam>,
    rankForTeam: (NHLStandingsTeam) -> Int?
) {

    Card(
        modifier =
            Modifier.fillMaxWidth()
    ) {

        Column {

            StandingsTableHeader()


            teams.forEach {
                    team ->

                HorizontalDivider()


                StandingsTeamRow(
                    team =
                        team,

                    rank =
                        rankForTeam(
                            team
                        )
                )
            }
        }
    }
}

@Composable
private fun StandingsTableHeader() {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 8.dp,
                vertical = 7.dp
            ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Text(
            text = "#",

            modifier =
                Modifier.width(
                    24.dp
                ),

            fontSize = 10.sp,

            fontWeight =
                FontWeight.Bold
        )


        Text(
            text = "TEAM",

            modifier =
                Modifier.weight(1f),

            fontSize = 10.sp,

            fontWeight =
                FontWeight.Bold
        )


        StandingsHeaderCell(
            "GP"
        )

        StandingsHeaderCell(
            "W"
        )

        StandingsHeaderCell(
            "L"
        )

        StandingsHeaderCell(
            "OT"
        )

        StandingsHeaderCell(
            "PTS",
            width = 34
        )

        StandingsHeaderCell(
            "DIFF",
            width = 38
        )
    }
}

@Composable
private fun StandingsHeaderCell(
    text: String,
    width: Int = 28
) {

    Text(
        text = text,

        modifier =
            Modifier.width(
                width.dp
            ),

        textAlign =
            TextAlign.Center,

        fontSize = 9.sp,

        fontWeight =
            FontWeight.Bold
    )
}

@Composable
private fun StandingsTeamRow(
    team: NHLStandingsTeam,
    rank: Int?
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 8.dp,
                vertical = 7.dp
            ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Text(
            text =
                rank?.toString()
                    ?: "-",

            modifier =
                Modifier.width(
                    24.dp
                ),

            fontSize = 11.sp,

            fontWeight =
                FontWeight.SemiBold
        )


        Row(
            modifier =
                Modifier.weight(1f),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            team.teamLogo
                ?.let { logo ->

                    AsyncImage(
                        model =
                            logo,

                        contentDescription =
                            "${team.teamName} logo",

                        contentScale =
                            ContentScale.Fit,

                        modifier =
                            Modifier.size(
                                28.dp
                            )
                    )


                    Spacer(
                        modifier =
                            Modifier.width(
                                5.dp
                            )
                    )
                }


            Column {

                Text(
                    text =
                        team.teamAbbrev,

                    fontWeight =
                        FontWeight.Bold,

                    fontSize = 12.sp
                )


                Text(
                    text =
                        team.streakDisplay,

                    fontSize = 9.sp
                )
            }
        }


        StandingsValueCell(
            team.gamesPlayed
                .toString()
        )

        StandingsValueCell(
            team.wins
                .toString()
        )

        StandingsValueCell(
            team.losses
                .toString()
        )

        StandingsValueCell(
            team.otLosses
                .toString()
        )

        StandingsValueCell(
            team.points
                .toString(),

            width = 34,

            bold = true
        )

        StandingsValueCell(
            formatGoalDifferential(
                team.goalDifferential
            ),

            width = 38
        )
    }
}

@Composable
private fun StandingsValueCell(
    text: String,
    width: Int = 28,
    bold: Boolean = false
) {

    Text(
        text = text,

        modifier =
            Modifier.width(
                width.dp
            ),

        textAlign =
            TextAlign.Center,

        fontSize = 11.sp,

        fontWeight =
            if (bold) {
                FontWeight.Bold
            } else {
                FontWeight.Normal
            }
    )
}

private fun formatGoalDifferential(
    value: Int?
): String {

    return when {

        value == null ->
            "-"

        value > 0 ->
            "+$value"

        else ->
            value.toString()
    }
}

@Composable
private fun StandingsSectionHeader(
    title: String
) {

    Text(
        text = title,

        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = 8.dp,
                bottom = 4.dp
            ),

        fontSize = 18.sp,

        fontWeight =
            FontWeight.Bold
    )
}