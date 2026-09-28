package com.avery.nhl.model.predictions

import com.avery.nhl.model.nhl.NHLGame


data class PredictionGame(
    val game: NHLGame,
    val prediction: GamePrediction?
) {

    val hasPrediction: Boolean
        get() =
            prediction
                ?.hasPrediction ==
                    true
}