package com.avery.nhl.ui.playoffs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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

import com.avery.nhl.model.nhl.NHLPlayoffBracket
import com.avery.nhl.model.nhl.NHLPlayoffTeam

import com.avery.nhl.model.playoffs.*

@Composable
fun PlayoffPredictionScreen(
    officialBracket: NHLPlayoffBracket,

    savedBracket: PlayoffPredictionBracket?,

    onBracketChanged: (
        PlayoffPredictionBracket
    ) -> Unit,

    onBack: () -> Unit,

    modifier: Modifier = Modifier
) {

    var prediction by remember(
        officialBracket.year
    ) {

        mutableStateOf(
            restorePredictionBracket(
                officialBracket =
                    officialBracket,

                savedBracket =
                    savedBracket
            )
        )
    }


    var showResetDialog by remember {
        mutableStateOf(false)
    }


    if (showResetDialog) {

        AlertDialog(
            onDismissRequest = {
                showResetDialog =
                    false
            },

            title = {
                Text(
                    "Reset bracket?"
                )
            },

            text = {
                Text(
                    "All playoff predictions for ${officialBracket.year} will be cleared."
                )
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        val reset =
                            buildInitialPredictionBracket(
                                officialBracket
                            )


                        prediction =
                            reset


                        onBracketChanged(
                            reset
                        )


                        showResetDialog =
                            false
                    }
                ) {

                    Text(
                        "Reset"
                    )
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        showResetDialog =
                            false
                    }
                ) {

                    Text(
                        "Cancel"
                    )
                }
            }
        )
    }


    Column(
        modifier =
            modifier.fillMaxSize()
    ) {

        PredictionTopBar(
            year =
                officialBracket.year,

            onBack =
                onBack,

            onReset = {
                showResetDialog =
                    true
            }
        )


        PredictionBracketContent(
            prediction =
                prediction,

            onWinnerSelected = {
                    seriesId,
                    teamId ->

                val updated =
                    pickSeriesWinner(
                        bracket =
                            prediction,

                        seriesId =
                            seriesId,

                        winnerId =
                            teamId
                    )


                prediction =
                    updated


                onBracketChanged(
                    updated
                )
            },

            onSeriesScoreSelected = {
                    seriesId,
                    loserWins ->

                val updated =
                    setPredictedSeriesScore(
                        bracket =
                            prediction,

                        seriesId =
                            seriesId,

                        loserWins =
                            loserWins
                    )


                prediction =
                    updated


                onBracketChanged(
                    updated
                )
            }
        )
    }
}


@Composable
private fun PredictionTopBar(
    year: Int,
    onBack: () -> Unit,
    onReset: () -> Unit
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


        Column(
            modifier =
                Modifier.weight(1f),

            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Text(
                text =
                    "My Bracket",

                fontSize =
                    20.sp,

                fontWeight =
                    FontWeight.Bold
            )


            Text(
                text =
                    "$year Stanley Cup Playoffs",

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
                onReset
        ) {

            Text(
                "Reset"
            )
        }
    }
}

@Composable
private fun PredictionBracketContent(
    prediction: PlayoffPredictionBracket,
    onWinnerSelected: (
        String,
        Long
    ) -> Unit,
    onSeriesScoreSelected: (
        String,
        Int
    ) -> Unit
) {

    LazyColumn(
        modifier =
            Modifier.fillMaxSize(),

        contentPadding =
            PaddingValues(
                start = 12.dp,
                end = 12.dp,
                bottom = 30.dp
            ),

        verticalArrangement =
            Arrangement.spacedBy(
                12.dp
            )
    ) {

        item {

            PredictionConferenceSection(
                title =
                    "Western Conference",

                conference =
                    PredictionConference.WEST,

                prediction =
                    prediction,

                onWinnerSelected =
                    onWinnerSelected,

                onSeriesScoreSelected =
                    onSeriesScoreSelected
            )
        }


        item {

            PredictionConferenceSection(
                title =
                    "Eastern Conference",

                conference =
                    PredictionConference.EAST,

                prediction =
                    prediction,

                onWinnerSelected =
                    onWinnerSelected,

                onSeriesScoreSelected =
                    onSeriesScoreSelected
            )
        }


        item {

            Text(
                text =
                    "Stanley Cup Final",

                fontSize =
                    20.sp,

                fontWeight =
                    FontWeight.Bold,

                modifier =
                    Modifier.padding(
                        top = 10.dp
                    )
            )
        }


        item {

            PredictionSeriesCard(
                pick =
                    prediction.picks[
                        "CUP"
                    ],

                onWinnerSelected =
                    onWinnerSelected,

                onSeriesScoreSelected =
                    onSeriesScoreSelected
            )
        }


        prediction
            .picks["CUP"]
            ?.predictedWinner
            ?.let {
                    champion ->

                item {

                    PredictedChampionCard(
                        champion =
                            champion
                    )
                }
            }
    }
}

@Composable
private fun PredictionConferenceSection(
    title: String,
    conference: PredictionConference,
    prediction: PlayoffPredictionBracket,
    onWinnerSelected: (
        String,
        Long
    ) -> Unit,
    onSeriesScoreSelected: (
        String,
        Int
    ) -> Unit
) {

    Column(
        verticalArrangement =
            Arrangement.spacedBy(
                8.dp
            )
    ) {

        Text(
            text =
                title,

            fontSize =
                20.sp,

            fontWeight =
                FontWeight.Bold
        )


        for (
        round in 1..3
        ) {

            Text(
                text =
                    predictionRoundName(
                        round
                    ),

                modifier =
                    Modifier.padding(
                        top = 7.dp
                    ),

                fontWeight =
                    FontWeight.Bold,

                color =
                    MaterialTheme
                        .colorScheme
                        .primary
            )


            val count =
                when (round) {

                    1 -> 4

                    2 -> 2

                    else -> 1
                }


            for (
            index in 0 until count
            ) {

                val id =
                    predictionSlotId(
                        conference,
                        round,
                        index
                    )


                PredictionSeriesCard(
                    pick =
                        prediction.picks[id],

                    onWinnerSelected =
                        onWinnerSelected,

                    onSeriesScoreSelected =
                        onSeriesScoreSelected
                )
            }
        }
    }
}

private fun predictionRoundName(
    round: Int
): String {

    return when (round) {

        1 ->
            "First Round"

        2 ->
            "Second Round"

        3 ->
            "Conference Final"

        else ->
            "Round $round"
    }
}

@Composable
private fun PredictionSeriesCard(
    pick: PlayoffSeriesPick?,
    onWinnerSelected: (
        String,
        Long
    ) -> Unit,
    onSeriesScoreSelected: (
        String,
        Int
    ) -> Unit
) {

    Card(
        modifier =
            Modifier.fillMaxWidth()
    ) {

        if (
            pick == null ||
            (
                    pick.topTeam == null &&
                            pick.bottomTeam == null
                    )
        ) {

            Text(
                text =
                    "Waiting for previous-round picks",

                modifier =
                    Modifier.padding(
                        14.dp
                    ),

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )

            return@Card
        }


        Column(
            modifier =
                Modifier.padding(
                    9.dp
                )
        ) {

            PredictionTeamRow(
                team =
                    pick.topTeam,

                selected =
                    pick.predictedWinnerId ==
                            pick.topTeam?.id,

                enabled =
                    pick.topTeam != null,

                onClick = {

                    pick.topTeam
                        ?.id
                        ?.let {

                            onWinnerSelected(
                                pick.seriesId,
                                it
                            )
                        }
                }
            )


            HorizontalDivider(
                modifier =
                    Modifier.padding(
                        vertical = 3.dp
                    )
            )


            PredictionTeamRow(
                team =
                    pick.bottomTeam,

                selected =
                    pick.predictedWinnerId ==
                            pick.bottomTeam?.id,

                enabled =
                    pick.bottomTeam != null,

                onClick = {

                    pick.bottomTeam
                        ?.id
                        ?.let {

                            onWinnerSelected(
                                pick.seriesId,
                                it
                            )
                        }
                }
            )


            if (
                pick.predictedWinner != null
            ) {

                PredictedSeriesLengthSelector(
                    pick =
                        pick,

                    onSelected = {
                            loserWins ->

                        onSeriesScoreSelected(
                            pick.seriesId,
                            loserWins
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun PredictionTeamRow(
    team: NHLPlayoffTeam?,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled =
                    enabled,

                onClick =
                    onClick
            )
            .padding(
                vertical = 6.dp
            ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        if (team == null) {

            Text(
                text =
                    "TBD",

                modifier =
                    Modifier.weight(1f),

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )

            return
        }


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
                    34.dp
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
                    if (selected) {
                        FontWeight.Bold
                    } else {
                        FontWeight.Normal
                    }
            )


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


        if (selected) {

            Text(
                text =
                    "PICK",

                fontSize =
                    11.sp,

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
private fun PredictedSeriesLengthSelector(
    pick: PlayoffSeriesPick,
    onSelected: (Int) -> Unit
) {

    val loserWins =
        when {

            pick.predictedTopWins == 4 ->
                pick.predictedBottomWins

            pick.predictedBottomWins == 4 ->
                pick.predictedTopWins

            else ->
                null
        }


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = 7.dp
            ),

        horizontalArrangement =
            Arrangement.spacedBy(
                5.dp
            ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Text(
            text =
                "Series:",

            fontSize =
                11.sp
        )


        for (
        losses in 0..3
        ) {

            FilterChip(
                selected =
                    loserWins ==
                            losses,

                onClick = {
                    onSelected(
                        losses
                    )
                },

                label = {

                    Text(
                        "4-$losses"
                    )
                }
            )
        }
    }
}

@Composable
private fun PredictedChampionCard(
    champion: NHLPlayoffTeam
) {

    Card(
        modifier =
            Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    18.dp
                ),

            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Text(
                text =
                    "Predicted Stanley Cup Champion",

                fontSize =
                    12.sp,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )


            AsyncImage(
                model =
                    champion.darkLogo
                        ?: champion.logo,

                contentDescription =
                    champion.name,

                modifier = Modifier
                    .size(
                        90.dp
                    )
                    .padding(
                        8.dp
                    ),

                contentScale =
                    ContentScale.Fit
            )


            Text(
                text =
                    champion.name,

                fontSize =
                    23.sp,

                fontWeight =
                    FontWeight.Bold,

                textAlign =
                    TextAlign.Center
            )
        }
    }
}