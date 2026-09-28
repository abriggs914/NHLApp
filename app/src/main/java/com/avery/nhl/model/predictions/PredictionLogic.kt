package com.avery.nhl.model.predictions

import com.avery.nhl.model.nhl.NHLGame

import java.time.Instant


fun findPredictionForGame(
    game: NHLGame,
    predictions: List<GamePrediction>
): GamePrediction? {

    return predictions
        .firstOrNull {
                prediction ->

            prediction.gameId ==
                    game.id
        }
        ?: predictions
            .firstOrNull {
                    prediction ->

                prediction.gameDate ==
                        game.gameDate &&

                        prediction.awayTeam.equals(
                            game.awayTeam.abbrev,
                            ignoreCase = true
                        ) &&

                        prediction.homeTeam.equals(
                            game.homeTeam.abbrev,
                            ignoreCase = true
                        )
            }
}


fun mergePredictionGames(
    games: List<NHLGame>,
    predictions: List<GamePrediction>
): List<PredictionGame> {

    return games
        .map {
                game ->

            PredictionGame(
                game =
                    game,

                prediction =
                    findPredictionForGame(
                        game =
                            game,

                        predictions =
                            predictions
                    )
            )
        }
}

fun createPredictionForGame(
    game: NHLGame,

    awayScore: Int,
    homeScore: Int,

    resultType: PredictionResultType
): GamePrediction {

    return GamePrediction(

        gameId =
            game.id,

        gameDate =
            game.gameDate,

        predictionDate =
            Instant
                .now()
                .toString(),

        awayTeam =
            game.awayTeam.abbrev,

        homeTeam =
            game.homeTeam.abbrev,

        predictedAwayScore =
            awayScore,

        predictedHomeScore =
            homeScore,

        predictedResult =
            resultType,

        predictedWinner =
            when {

                awayScore >
                        homeScore ->

                    game.awayTeam.abbrev


                homeScore >
                        awayScore ->

                    game.homeTeam.abbrev


                else ->
                    null
            }
    )
}

fun applyNhlResult(
    prediction: GamePrediction,
    game: NHLGame
): GamePrediction {

    if (
        prediction.gameId !=
        game.id
    ) {
        return prediction
    }

    if (
        !game.isFinal
    ) {
        return prediction
    }

    val awayScore =
        game.awayTeam.score
            ?: return prediction

    val homeScore =
        game.homeTeam.score
            ?: return prediction

    val resultType =
        when (
            game.periodType
                ?.uppercase()
        ) {

            "SO" ->
                PredictionResultType.SO

            "OT" ->
                PredictionResultType.OT

            else ->
                PredictionResultType.REG
        }

    val updated =
        prediction.copy(

            gameIsOver =
                true,

            actualAwayScore =
                awayScore,

            actualHomeScore =
                homeScore,

            actualResult =
                resultType
        )

    return updated.copy(
        enhancedScore =
            calculateEnhancedPredictionScore(
                updated
            )
    )
}

fun synchronizePredictions(
    predictions: List<GamePrediction>,
    games: List<NHLGame>
): List<GamePrediction> {

    if (
        games.isEmpty()
    ) {
        return predictions
    }

    val gamesById =
        games.associateBy {
            it.id
        }

    return predictions
        .map {
                prediction ->

            val id =
                prediction.gameId

            val game =
                if (id != null) {
                    gamesById[id]
                } else {
                    null
                }

            if (game != null) {

                applyNhlResult(
                    prediction =
                        prediction,

                    game =
                        game
                )

            } else {

                prediction
            }
        }
}