package com.avery.nhl.model.predictions


enum class PredictionResultType {
    REG,
    OT,
    SO;

    companion object {

        fun fromString(
            value: String?
        ): PredictionResultType {

            return when (
                value
                    ?.trim()
                    ?.uppercase()
            ) {

                "OT" ->
                    OT

                "SO" ->
                    SO

                else ->
                    REG
            }
        }
    }
}


data class GamePrediction(

    /*
     * NHL game ID.
     *
     * Keep this Long? because imported historical
     * workbook rows may not always contain one.
     */
    val gameId: Long? = null,


    val gameDate: String,

    val predictionDate: String? = null,


    val awayTeam: String,

    val homeTeam: String,


    /*
     * User prediction.
     */
    val predictedAwayScore: Int? = null,

    val predictedHomeScore: Int? = null,

    val predictedResult:
    PredictionResultType =
        PredictionResultType.REG,


    /*
     * Optional model-generated information.
     */
    val predictedWinner: String? = null,

    val awayWinProbability: Double? = null,

    val homeWinProbability: Double? = null,

    val confidence: Double? = null,


    /*
     * Actual result.
     */
    val gameIsOver: Boolean = false,

    val actualAwayScore: Int? = null,

    val actualHomeScore: Int? = null,

    val actualResult:
    PredictionResultType? = null,


    /*
     * Optional personal metadata already
     * represented in the Python workbook.
     */
    val watchedGame: Boolean = false,

    val gameHasShutOut: Boolean = false,


    /*
     * Keep legacy workbook scores available.
     */
    val gamePredictionScore: Double? = null,

    val gamePredictionScore2: Double? = null,

    val enhancedScore: Double? = null
) {

    val hasPrediction: Boolean
        get() =
            predictedAwayScore != null &&
                    predictedHomeScore != null


    val predictedWinnerFromScore: String?
        get() {

            val away =
                predictedAwayScore
                    ?: return predictedWinner

            val home =
                predictedHomeScore
                    ?: return predictedWinner


            return when {

                away > home ->
                    awayTeam

                home > away ->
                    homeTeam

                else ->
                    predictedWinner
            }
        }


    val actualWinner: String?
        get() {

            val away =
                actualAwayScore
                    ?: return null

            val home =
                actualHomeScore
                    ?: return null


            return when {

                away > home ->
                    awayTeam

                home > away ->
                    homeTeam

                else ->
                    null
            }
        }


    val correctWinnerPrediction: Boolean?
        get() {

            if (
                !gameIsOver
            ) {
                return null
            }


            val predicted =
                predictedWinnerFromScore
                    ?: return null

            val actual =
                actualWinner
                    ?: return null


            return predicted.equals(
                actual,
                ignoreCase = true
            )
        }


    val correctAwayScorePrediction: Boolean?
        get() {

            if (
                !gameIsOver
            ) {
                return null
            }


            val predicted =
                predictedAwayScore
                    ?: return null

            val actual =
                actualAwayScore
                    ?: return null


            return predicted ==
                    actual
        }


    val correctHomeScorePrediction: Boolean?
        get() {

            if (
                !gameIsOver
            ) {
                return null
            }


            val predicted =
                predictedHomeScore
                    ?: return null

            val actual =
                actualHomeScore
                    ?: return null


            return predicted ==
                    actual
        }
}