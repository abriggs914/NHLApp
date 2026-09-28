package com.avery.nhl.model.predictions

import kotlin.math.abs


fun calculateEnhancedPredictionScore(
    prediction: GamePrediction
): Double? {

    if (
        !prediction.gameIsOver
    ) {
        return null
    }


    val predictedAway =
        prediction.predictedAwayScore
            ?: return null

    val predictedHome =
        prediction.predictedHomeScore
            ?: return null

    val actualAway =
        prediction.actualAwayScore
            ?: return null

    val actualHome =
        prediction.actualHomeScore
            ?: return null

    val actualResult =
        prediction.actualResult
            ?: PredictionResultType.REG


    /*
     * A) Correct winner — 0.55
     */
    val winnerScore =
        if (
            prediction.correctWinnerPrediction ==
            true
        ) {

            0.55

        } else {

            0.0
        }


    /*
     * B) Result type — 0.10
     *
     * Exact REG / OT / SO match:
     *     0.10
     *
     * OT vs SO:
     *     0.06
     */
    val resultScore =
        when {

            prediction.predictedResult ==
                    actualResult ->

                0.10


            prediction.predictedResult.isExtraTime() &&
                    actualResult.isExtraTime() ->

                0.06


            else ->
                0.0
        }


    /*
     * C) Margin closeness — max 0.175
     */
    val predictedMargin =
        predictedHome -
                predictedAway


    val actualMargin =
        actualHome -
                actualAway


    val marginError =
        abs(
            predictedMargin -
                    actualMargin
        )


    val marginScore =
        when (marginError) {

            0 ->
                0.175

            1 ->
                0.140

            2 ->
                0.105

            3 ->
                0.070

            4 ->
                0.035

            else ->
                0.0
        }


    /*
     * D) Per-team score closeness — max 0.175
     */
    val awayError =
        abs(
            predictedAway -
                    actualAway
        )


    val homeError =
        abs(
            predictedHome -
                    actualHome
        )


    val totalScoreError =
        awayError +
                homeError


    val scoreCloseness =
        when (totalScoreError) {

            0 ->
                0.175

            1 ->
                0.145

            2 ->
                0.115

            3 ->
                0.085

            4 ->
                0.055

            5 ->
                0.025

            else ->
                0.0
        }


    /*
     * E) Extra-time partial credit.
     *
     * Only applies when the winner itself
     * was incorrect.
     */
    val extraTimeBonus =
        if (
            prediction.correctWinnerPrediction ==
            false
        ) {

            when {

                prediction.predictedResult ==
                        actualResult &&
                        actualResult.isExtraTime() ->

                    0.10


                prediction.predictedResult.isExtraTime() &&
                        actualResult.isExtraTime() ->

                    0.06


                else ->
                    0.0
            }

        } else {

            0.0
        }


    return (
            winnerScore +
                    resultScore +
                    marginScore +
                    scoreCloseness +
                    extraTimeBonus
            )
        .coerceIn(
            0.0,
            1.0
        )
}


private fun PredictionResultType.isExtraTime(): Boolean {

    return this ==
            PredictionResultType.OT ||
            this ==
            PredictionResultType.SO
}