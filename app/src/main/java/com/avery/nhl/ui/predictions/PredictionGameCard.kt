package com.avery.nhl.ui.predictions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*

import androidx.compose.material3.*

import androidx.compose.runtime.Composable

import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.avery.nhl.model.predictions.GamePrediction
import com.avery.nhl.model.predictions.PredictionGame

import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun PredictionGameCard(
    prediction: GamePrediction,
    onClick: (() -> Unit)? = null
) {

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .then(
                    if (
                        onClick != null
                    ) {

                        Modifier.clickable(
                            onClick =
                                onClick
                        )

                    } else {

                        Modifier
                    }
                )
    ) {

        Column(
            modifier =
                Modifier.padding(
                    12.dp
                )
        ) {

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {

                Text(
                    text =
                        formatPredictionDate(
                            prediction.gameDate
                        ),

                    fontSize =
                        11.sp,

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )


                PredictionStatusText(
                    prediction =
                        prediction
                )
            }


            Spacer(
                modifier =
                    Modifier.height(
                        7.dp
                    )
            )


            PredictionTeamLine(
                team =
                    prediction.awayTeam,

                predictedScore =
                    prediction
                        .predictedAwayScore,

                actualScore =
                    prediction
                        .actualAwayScore,

                completed =
                    prediction.gameIsOver
            )


            PredictionTeamLine(
                team =
                    prediction.homeTeam,

                predictedScore =
                    prediction
                        .predictedHomeScore,

                actualScore =
                    prediction
                        .actualHomeScore,

                completed =
                    prediction.gameIsOver
            )


            if (
                prediction.hasPrediction
            ) {

                HorizontalDivider(
                    modifier =
                        Modifier.padding(
                            vertical = 7.dp
                        )
                )


                Text(
                    text =
                        "Prediction: " +
                                (
                                        prediction
                                            .predictedWinnerFromScore
                                            ?: "—"
                                        ) +
                                " • " +
                                prediction
                                    .predictedResult
                                    .name,

                    fontSize =
                        11.sp
                )


                prediction.enhancedScore
                    ?.let {
                            score ->

                        Text(
                            text =
                                "Enhanced score: " +
                                        "%.3f"
                                            .format(
                                                score
                                            ),

                            fontSize =
                                10.sp,

                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant
                        )
                    }
            }
        }
    }
}

@Composable
private fun PredictionTeamLine(
    team: String,
    predictedScore: Int?,
    actualScore: Int?,
    completed: Boolean
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                vertical = 3.dp
            )
    ) {

        Text(
            text =
                team,

            modifier =
                Modifier.weight(1f),

            fontSize =
                17.sp,

            fontWeight =
                FontWeight.Bold
        )


        if (
            predictedScore != null
        ) {

            Text(
                text =
                    "P $predictedScore",

                modifier =
                    Modifier.padding(
                        horizontal = 8.dp
                    ),

                fontSize =
                    13.sp
            )
        }


        if (
            completed &&
            actualScore != null
        ) {

            Text(
                text =
                    "A $actualScore",

                fontSize =
                    17.sp,

                fontWeight =
                    FontWeight.Bold
            )
        }
    }
}

@Composable
private fun PredictionStatusText(
    prediction: GamePrediction
) {

    val text =
        when {

            !prediction.gameIsOver ->
                "Upcoming"


            prediction
                .correctWinnerPrediction ==
                    true ->

                "✓ Correct"


            prediction
                .correctWinnerPrediction ==
                    false ->

                "✕ Incorrect"


            else ->
                "Final"
        }


    val colour =
        when {

            prediction
                .correctWinnerPrediction ==
                    true ->

                MaterialTheme
                    .colorScheme
                    .primary


            prediction
                .correctWinnerPrediction ==
                    false ->

                MaterialTheme
                    .colorScheme
                    .error


            else ->

                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        }


    Text(
        text =
            text,

        fontSize =
            11.sp,

        fontWeight =
            FontWeight.Bold,

        color =
            colour
    )
}

private fun formatPredictionDate(
    value: String
): String {

    return try {

        LocalDate
            .parse(
                value.take(
                    10
                )
            )
            .format(
                DateTimeFormatter.ofPattern(
                    "EEE, MMM d, yyyy"
                )
            )

    } catch (_: Exception) {

        value
    }
}

@Composable
fun UpcomingPredictionGameCard(
    item: PredictionGame,
    onClick: () -> Unit
) {

    val game =
        item.game

    val prediction =
        item.prediction

    val time =
        formatPredictionGameTime(
            game.startTimeUTC
        )

    Text(
        text =
            buildString {

                append(
                    game.gameDate
                )

                if (
                    time.isNotBlank()
                ) {

                    append(
                        " • "
                    )

                    append(
                        time
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

    Card(
        onClick =
            onClick,

        modifier =
            Modifier.fillMaxWidth()
    ) {

        Column(
            modifier =
                Modifier.padding(
                    12.dp
                )
        ) {

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {

                Text(
                    text =
                        game.gameDate,

                    fontSize =
                        11.sp,

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )

                Text(
                    text =
                        if (
                            prediction
                                ?.hasPrediction ==
                            true
                        ) {

                            "✓ Predicted"

                        } else {

                            "Make Prediction"
                        },

                    fontSize =
                        11.sp,

                    fontWeight =
                        FontWeight.Bold
                )
            }

            Spacer(
                modifier =
                    Modifier.height(
                        7.dp
                    )
            )

            UpcomingTeamLine(
                abbreviation =
                    game.awayTeam.abbrev,

                score =
                    prediction
                        ?.predictedAwayScore
            )

            UpcomingTeamLine(
                abbreviation =
                    game.homeTeam.abbrev,

                score =
                    prediction
                        ?.predictedHomeScore
            )

            prediction
                ?.takeIf {
                    it.hasPrediction
                }
                ?.let {

                    Text(
                        text =
                            "${it.predictedWinnerFromScore} • " +
                                    it.predictedResult.name,

                        modifier =
                            Modifier.padding(
                                top = 7.dp
                            ),

                        fontSize =
                            11.sp,

                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )
                }
        }
    }
}

@Composable
private fun UpcomingTeamLine(
    abbreviation: String,
    score: Int?
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                vertical = 2.dp
            )
    ) {

        Text(
            text =
                abbreviation,

            modifier =
                Modifier.weight(1f),

            fontSize =
                18.sp,

            fontWeight =
                FontWeight.Bold
        )

        Text(
            text =
                score
                    ?.toString()
                    ?: "—",

            fontSize =
                18.sp,

            fontWeight =
                FontWeight.Bold
        )
    }
}

private fun formatPredictionGameTime(
    utc: String?
): String {

    if (
        utc.isNullOrBlank()
    ) {
        return ""
    }

    return try {

        java.time.Instant
            .parse(
                utc
            )
            .atZone(
                java.time.ZoneId
                    .systemDefault()
            )
            .format(
                java.time.format
                    .DateTimeFormatter
                    .ofPattern(
                        "h:mm a"
                    )
            )

    } catch (_: Exception) {

        ""
    }
}