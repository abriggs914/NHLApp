package com.avery.nhl.model.predictions


data class PredictionImportResult(
    val imported: List<GamePrediction>,
    val totalRows: Int,
    val acceptedRows: Int,
    val skippedRows: Int,
    val errors: List<String>
)


data class PredictionMergeResult(
    val predictions: List<GamePrediction>,
    val added: Int,
    val updated: Int,
    val unchanged: Int
)