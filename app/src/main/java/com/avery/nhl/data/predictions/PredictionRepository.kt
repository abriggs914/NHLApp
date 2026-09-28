package com.avery.nhl.data.predictions

import android.content.Context

import com.avery.nhl.model.predictions.GamePrediction
import com.avery.nhl.model.predictions.PredictionResultType
import com.avery.nhl.model.predictions.calculateEnhancedPredictionScore

import org.json.JSONArray
import org.json.JSONObject

import java.io.File

class PredictionRepository(
    context: Context
) {

    private val file =
        File(
            context.filesDir,
            FILE_NAME
        )


    fun loadPredictions():
            List<GamePrediction> {

        if (
            !file.exists()
        ) {
            return emptyList()
        }


        return try {

            val text =
                file.readText()


            if (
                text.isBlank()
            ) {
                return emptyList()
            }


            val root =
                JSONObject(
                    text
                )


            val array =
                root.optJSONArray(
                    "predictions"
                )
                    ?: return emptyList()


            buildList {

                for (
                index in 0 until
                        array.length()
                ) {

                    val obj =
                        array.optJSONObject(
                            index
                        )
                            ?: continue


                    parsePrediction(
                        obj
                    )
                        ?.let {
                            add(
                                it
                            )
                        }
                }
            }
                .sortedWith(
                    compareBy<GamePrediction> {
                        it.gameDate
                    }
                        .thenBy {
                            it.gameId
                        }
                )

        } catch (_: Exception) {

            emptyList()
        }
    }


    fun savePredictions(
        predictions:
        List<GamePrediction>
    ) {

        val array =
            JSONArray()


        predictions.forEach { prediction ->

            array.put(
                predictionToJson(
                    prediction
                )
            )
        }


        val root =
            JSONObject().apply {

                put(
                    "version",
                    1
                )

                put(
                    "predictions",
                    array
                )
            }


        file.writeText(
            root.toString(
                2
            )
        )
    }

    fun upsertPrediction(
        prediction: GamePrediction
    ): List<GamePrediction> {

        val current =
            loadPredictions()
                .toMutableList()


        val index =
            current.indexOfFirst { existing ->

                sameGame(
                    existing,
                    prediction
                )
            }


        val withScore =
            prediction.copy(
                enhancedScore =
                    calculateEnhancedPredictionScore(
                        prediction
                    )
            )


        if (
            index >= 0
        ) {

            current[index] =
                withScore

        } else {

            current.add(
                withScore
            )
        }


        val result =
            current.sortedWith(
                compareBy<GamePrediction> {
                    it.gameDate
                }
                    .thenBy {
                        it.gameId
                    }
            )


        savePredictions(
            result
        )


        return result
    }


    fun deletePrediction(
        prediction: GamePrediction
    ): List<GamePrediction> {

        val result =
            loadPredictions()
                .filterNot {
                    sameGame(
                        it,
                        prediction
                    )
                }


        savePredictions(
            result
        )


        return result
    }


    private fun sameGame(
        first: GamePrediction,
        second: GamePrediction
    ): Boolean {

        if (
            first.gameId != null &&
            second.gameId != null
        ) {

            return first.gameId ==
                    second.gameId
        }


        return first.gameDate ==
                second.gameDate &&
                first.awayTeam.equals(
                    second.awayTeam,
                    ignoreCase = true
                ) &&
                first.homeTeam.equals(
                    second.homeTeam,
                    ignoreCase = true
                )
    }

    private fun predictionToJson(
        prediction: GamePrediction
    ): JSONObject {

        return JSONObject().apply {

            putNullable(
                "gameId",
                prediction.gameId
            )


            put(
                "gameDate",
                prediction.gameDate
            )


            putNullable(
                "predictionDate",
                prediction.predictionDate
            )


            put(
                "awayTeam",
                prediction.awayTeam
            )

            put(
                "homeTeam",
                prediction.homeTeam
            )


            putNullable(
                "predictedAwayScore",
                prediction.predictedAwayScore
            )

            putNullable(
                "predictedHomeScore",
                prediction.predictedHomeScore
            )


            put(
                "predictedResult",
                prediction.predictedResult.name
            )


            putNullable(
                "predictedWinner",
                prediction.predictedWinner
            )


            putNullable(
                "awayWinProbability",
                prediction.awayWinProbability
            )

            putNullable(
                "homeWinProbability",
                prediction.homeWinProbability
            )

            putNullable(
                "confidence",
                prediction.confidence
            )


            put(
                "gameIsOver",
                prediction.gameIsOver
            )


            putNullable(
                "actualAwayScore",
                prediction.actualAwayScore
            )

            putNullable(
                "actualHomeScore",
                prediction.actualHomeScore
            )


            putNullable(
                "actualResult",
                prediction.actualResult
                    ?.name
            )


            put(
                "watchedGame",
                prediction.watchedGame
            )


            put(
                "gameHasShutOut",
                prediction.gameHasShutOut
            )


            putNullable(
                "gamePredictionScore",
                prediction.gamePredictionScore
            )

            putNullable(
                "gamePredictionScore2",
                prediction.gamePredictionScore2
            )

            putNullable(
                "enhancedScore",
                prediction.enhancedScore
            )
        }
    }

    private fun JSONObject.putNullable(
        name: String,
        value: Any?
    ) {

        put(
            name,
            value ?: JSONObject.NULL
        )
    }

    private fun parsePrediction(
        obj: JSONObject
    ): GamePrediction? {

        val gameDate =
            obj.optNullableString(
                "gameDate"
            )
                ?: return null


        val awayTeam =
            obj.optNullableString(
                "awayTeam"
            )
                ?: return null


        val homeTeam =
            obj.optNullableString(
                "homeTeam"
            )
                ?: return null


        return GamePrediction(

            gameId =
                obj.optNullableLong(
                    "gameId"
                ),


            gameDate =
                gameDate,


            predictionDate =
                obj.optNullableString(
                    "predictionDate"
                ),


            awayTeam =
                awayTeam,

            homeTeam =
                homeTeam,


            predictedAwayScore =
                obj.optNullableInt(
                    "predictedAwayScore"
                ),

            predictedHomeScore =
                obj.optNullableInt(
                    "predictedHomeScore"
                ),


            predictedResult =
                PredictionResultType
                    .fromString(
                        obj.optNullableString(
                            "predictedResult"
                        )
                    ),


            predictedWinner =
                obj.optNullableString(
                    "predictedWinner"
                ),


            awayWinProbability =
                obj.optNullableDouble(
                    "awayWinProbability"
                ),

            homeWinProbability =
                obj.optNullableDouble(
                    "homeWinProbability"
                ),

            confidence =
                obj.optNullableDouble(
                    "confidence"
                ),


            gameIsOver =
                obj.optBoolean(
                    "gameIsOver",
                    false
                ),


            actualAwayScore =
                obj.optNullableInt(
                    "actualAwayScore"
                ),

            actualHomeScore =
                obj.optNullableInt(
                    "actualHomeScore"
                ),


            actualResult =
                obj.optNullableString(
                    "actualResult"
                )
                    ?.let {
                        PredictionResultType
                            .fromString(
                                it
                            )
                    },


            watchedGame =
                obj.optBoolean(
                    "watchedGame",
                    false
                ),


            gameHasShutOut =
                obj.optBoolean(
                    "gameHasShutOut",
                    false
                ),


            gamePredictionScore =
                obj.optNullableDouble(
                    "gamePredictionScore"
                ),

            gamePredictionScore2 =
                obj.optNullableDouble(
                    "gamePredictionScore2"
                ),

            enhancedScore =
                obj.optNullableDouble(
                    "enhancedScore"
                )
        )
    }

    private fun JSONObject.optNullableString(
        name: String
    ): String? {

        if (
            !has(name) ||
            isNull(name)
        ) {
            return null
        }


        return optString(
            name
        )
            .takeIf {
                it.isNotBlank()
            }
    }


    private fun JSONObject.optNullableLong(
        name: String
    ): Long? {

        if (
            !has(name) ||
            isNull(name)
        ) {
            return null
        }


        return optLong(
            name
        )
    }


    private fun JSONObject.optNullableInt(
        name: String
    ): Int? {

        if (
            !has(name) ||
            isNull(name)
        ) {
            return null
        }


        return optInt(
            name
        )
    }


    private fun JSONObject.optNullableDouble(
        name: String
    ): Double? {

        if (
            !has(name) ||
            isNull(name)
        ) {
            return null
        }


        return optDouble(
            name
        )
    }


    companion object {

        private const val FILE_NAME =
            "game_predictions.json"
    }

}