package com.avery.nhl.model.predictions


data class PredictionSummary(
    val totalPredictions: Int,

    val completedPredictions: Int,

    val upcomingPredictions: Int,

    val correctWinners: Int,

    val winnerAccuracy: Double,

    val exactScores: Int,

    val exactResultTypes: Int,

    val averageEnhancedScore: Double?
)

fun buildPredictionSummary(
    predictions: List<GamePrediction>
): PredictionSummary {

    val completed =
        predictions.filter {
            it.gameIsOver &&
                    it.hasPrediction
        }


    val upcoming =
        predictions.filter {
            !it.gameIsOver
        }


    val correctWinners =
        completed.count {
            it.correctWinnerPrediction ==
                    true
        }


    val exactScores =
        completed.count {

            it.correctAwayScorePrediction ==
                    true &&
                    it.correctHomeScorePrediction ==
                    true
        }


    val exactResultTypes =
        completed.count {

            val actual =
                it.actualResult

            actual != null &&
                    actual ==
                    it.predictedResult
        }


    val enhanced =
        completed
            .mapNotNull {
                it.enhancedScore
            }


    return PredictionSummary(

        totalPredictions =
            predictions.count {
                it.hasPrediction
            },

        completedPredictions =
            completed.size,

        upcomingPredictions =
            upcoming.size,

        correctWinners =
            correctWinners,

        winnerAccuracy =
            if (
                completed.isNotEmpty()
            ) {

                correctWinners.toDouble() /
                        completed.size

            } else {

                0.0
            },

        exactScores =
            exactScores,

        exactResultTypes =
            exactResultTypes,

        averageEnhancedScore =
            enhanced
                .takeIf {
                    it.isNotEmpty()
                }
                ?.average()
    )
}