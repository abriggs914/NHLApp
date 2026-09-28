package com.avery.nhl.ui.predictions

import androidx.compose.foundation.layout.*

import androidx.compose.material3.*

import androidx.compose.runtime.Composable

import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.avery.nhl.model.predictions.GamePrediction
import com.avery.nhl.model.predictions.buildPredictionSummary

import java.util.Locale

@Composable
fun PredictionAccuracyScreen(
    predictions:
    List<GamePrediction>
) {

    val summary =
        buildPredictionSummary(
            predictions
        )


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                12.dp
            ),

        verticalArrangement =
            Arrangement.spacedBy(
                9.dp
            )
    ) {

        Text(
            text =
                "Prediction Accuracy",

            fontSize =
                21.sp,

            fontWeight =
                FontWeight.Bold
        )


        Row(
            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.spacedBy(
                    8.dp
                )
        ) {

            PredictionMetricCard(
                title =
                    "Completed",

                value =
                    summary
                        .completedPredictions
                        .toString(),

                modifier =
                    Modifier.weight(1f)
            )


            PredictionMetricCard(
                title =
                    "Correct",

                value =
                    summary
                        .correctWinners
                        .toString(),

                modifier =
                    Modifier.weight(1f)
            )


            PredictionMetricCard(
                title =
                    "Accuracy",

                value =
                    String.format(
                        Locale.CANADA,
                        "%.1f%%",
                        summary
                            .winnerAccuracy *
                                100.0
                    ),

                modifier =
                    Modifier.weight(1f)
            )
        }


        Row(
            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.spacedBy(
                    8.dp
                )
        ) {

            PredictionMetricCard(
                title =
                    "Exact Score",

                value =
                    summary
                        .exactScores
                        .toString(),

                modifier =
                    Modifier.weight(1f)
            )


            PredictionMetricCard(
                title =
                    "Result Type",

                value =
                    summary
                        .exactResultTypes
                        .toString(),

                modifier =
                    Modifier.weight(1f)
            )


            PredictionMetricCard(
                title =
                    "Upcoming",

                value =
                    summary
                        .upcomingPredictions
                        .toString(),

                modifier =
                    Modifier.weight(1f)
            )
        }


        summary
            .averageEnhancedScore
            ?.let {
                    average ->

                PredictionMetricCard(
                    title =
                        "Enhanced v3 Average",

                    value =
                        String.format(
                            Locale.CANADA,
                            "%.3f",
                            average
                        ),

                    modifier =
                        Modifier.fillMaxWidth()
                )
            }


        if (
            summary.completedPredictions ==
            0
        ) {

            Text(
                text =
                    "No completed predictions are stored yet.",

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PredictionMetricCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {

    Card(
        modifier =
            modifier
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    10.dp
                )
        ) {

            Text(
                text =
                    value,

                fontSize =
                    20.sp,

                fontWeight =
                    FontWeight.Bold
            )


            Text(
                text =
                    title,

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