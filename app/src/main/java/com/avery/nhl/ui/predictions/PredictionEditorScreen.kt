package com.avery.nhl.ui.predictions

import androidx.compose.foundation.layout.*

import androidx.compose.material3.*

import androidx.compose.runtime.*

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.avery.nhl.model.nhl.NHLGame
import com.avery.nhl.model.predictions.GamePrediction
import com.avery.nhl.model.predictions.PredictionResultType
import com.avery.nhl.model.predictions.createPredictionForGame

@Composable
fun PredictionEditorScreen(
    game: NHLGame,
    existingPrediction:
    GamePrediction?,

    onSave:
        (GamePrediction) -> Unit,

    onDelete:
    (() -> Unit)?,

    onBack: () -> Unit,

    modifier: Modifier = Modifier
) {

    var awayScore by remember(
        game.id
    ) {

        mutableIntStateOf(
            existingPrediction
                ?.predictedAwayScore
                ?: 3
        )
    }

    var homeScore by remember(
        game.id
    ) {

        mutableIntStateOf(
            existingPrediction
                ?.predictedHomeScore
                ?: 2
        )
    }

    var resultType by remember(
        game.id
    ) {

        mutableStateOf(
            existingPrediction
                ?.predictedResult
                ?: PredictionResultType.REG
        )
    }

    val scoreValid =
        when (
            resultType
        ) {

            PredictionResultType.REG ->

                awayScore !=
                        homeScore


            PredictionResultType.OT,
            PredictionResultType.SO ->

                kotlin.math.abs(
                    awayScore -
                            homeScore
                ) == 1
        }

    val validationMessage =
        when {

            awayScore ==
                    homeScore ->

                "An NHL game cannot end in a tie."


            resultType !=
                    PredictionResultType.REG &&
                    kotlin.math.abs(
                        awayScore -
                                homeScore
                    ) != 1 ->

                "An OT/SO prediction must have a one-goal final margin."


            else ->
                null
        }

    validationMessage
        ?.let {

            Text(
                text =
                    it,

                color =
                    MaterialTheme
                        .colorScheme
                        .error,

                fontSize =
                    12.sp
            )
        }

    Column(
        modifier =
            modifier.fillMaxSize()
    ) {

        PredictionEditorTopBar(
            onBack =
                onBack
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    16.dp
                ),

            verticalArrangement =
                Arrangement.spacedBy(
                    16.dp
                )
        ) {

            Text(
                text =
                    game.gameDate,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )

            PredictionScoreRow(
                team =
                    game.awayTeam.abbrev,

                score =
                    awayScore,

                onScoreChanged = {
                    awayScore =
                        it
                }
            )

            Text(
                text =
                    "@",

                modifier =
                    Modifier.align(
                        Alignment.CenterHorizontally
                    ),

                fontSize =
                    18.sp
            )

            PredictionScoreRow(
                team =
                    game.homeTeam.abbrev,

                score =
                    homeScore,

                onScoreChanged = {
                    homeScore =
                        it
                }
            )

            HorizontalDivider()

            Text(
                text =
                    "Game Result",

                fontWeight =
                    FontWeight.Bold
            )

            PredictionResultSelector(
                selected =
                    resultType,

                onSelected = {
                        type ->

                    resultType =
                        type

                    if (
                        type !=
                        PredictionResultType.REG
                    ) {

                        if (
                            awayScore ==
                            homeScore
                        ) {

                            homeScore =
                                awayScore + 1

                        } else if (
                            kotlin.math.abs(
                                awayScore -
                                        homeScore
                            ) != 1
                        ) {

                            if (
                                awayScore >
                                homeScore
                            ) {

                                awayScore =
                                    homeScore + 1

                            } else {

                                homeScore =
                                    awayScore + 1
                            }
                        }
                    }
                }
            )

            if (
                !scoreValid
            ) {

                Text(
                    text =
                        "An NHL game prediction cannot end in a tie.",

                    color =
                        MaterialTheme
                            .colorScheme
                            .error,

                    fontSize =
                        12.sp
                )
            }

            Button(
                onClick = {

                    val newPrediction =
                        createPredictionForGame(
                            game =
                                game,

                            awayScore =
                                awayScore,

                            homeScore =
                                homeScore,

                            resultType =
                                resultType
                        )

                    onSave(
                        if (
                            existingPrediction != null
                        ) {

                            newPrediction.copy(
                                predictionDate =
                                    existingPrediction
                                        .predictionDate
                                        ?: newPrediction
                                            .predictionDate
                            )

                        } else {

                            newPrediction
                        }
                    )
                },

                enabled =
                    scoreValid,

                modifier =
                    Modifier.fillMaxWidth()
            ) {

                Text(
                    if (
                        existingPrediction ==
                        null
                    ) {
                        "Save Prediction"
                    } else {
                        "Update Prediction"
                    }
                )
            }

            if (
                existingPrediction != null &&
                onDelete != null
            ) {

                OutlinedButton(
                    onClick =
                        onDelete,

                    modifier =
                        Modifier.fillMaxWidth()
                ) {

                    Text(
                        "Delete Prediction"
                    )
                }
            }
        }
    }
}

@Composable
private fun PredictionEditorTopBar(
    onBack: () -> Unit
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
                "‹ Predictions"
            )
        }

        Text(
            text =
                "Make Prediction",

            modifier =
                Modifier.weight(1f),

            fontSize =
                20.sp,

            fontWeight =
                FontWeight.Bold
        )
    }
}

@Composable
private fun PredictionScoreRow(
    team: String,
    score: Int,
    onScoreChanged: (Int) -> Unit
) {

    Row(
        modifier =
            Modifier.fillMaxWidth(),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Text(
            text =
                team,

            modifier =
                Modifier.weight(1f),

            fontSize =
                24.sp,

            fontWeight =
                FontWeight.Bold
        )

        OutlinedButton(
            onClick = {

                onScoreChanged(
                    (score - 1)
                        .coerceAtLeast(
                            0
                        )
                )
            }
        ) {

            Text(
                "−"
            )
        }

        Text(
            text =
                score.toString(),

            modifier =
                Modifier.padding(
                    horizontal = 18.dp
                ),

            fontSize =
                27.sp,

            fontWeight =
                FontWeight.Bold
        )

        OutlinedButton(
            onClick = {

                onScoreChanged(
                    (score + 1)
                        .coerceAtMost(
                            20
                        )
                )
            }
        ) {

            Text(
                "+"
            )
        }
    }
}

@Composable
private fun PredictionResultSelector(
    selected:
    PredictionResultType,

    onSelected:
        (PredictionResultType) -> Unit
) {

    Row(
        horizontalArrangement =
            Arrangement.spacedBy(
                8.dp
            )
    ) {

        PredictionResultType
            .entries
            .forEach {
                    type ->

                FilterChip(
                    selected =
                        selected ==
                                type,

                    onClick = {

                        onSelected(
                            type
                        )
                    },

                    label = {

                        Text(
                            type.name
                        )
                    }
                )
            }
    }
}