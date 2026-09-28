package com.avery.nhl.model.playoffs


data class PlayoffSavedState(
    val playoffYear: Int,

    val predictionBracket: PlayoffPredictionBracket? = null,

    val projectionWeights: ProjectionWeights =
        ProjectionWeights(),

    val simulationIterations: Int = 10_000
)