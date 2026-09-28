package com.avery.nhl.model.predictions


fun mergeImportedPredictions(
    existing: List<GamePrediction>,
    imported: List<GamePrediction>
): PredictionMergeResult {

    val result =
        existing.toMutableList()

    var added = 0
    var updated = 0
    var unchanged = 0


    imported.forEach { incoming ->

        val index =
            result.indexOfFirst { current ->
                samePredictionGame(
                    current,
                    incoming
                )
            }


        if (index < 0) {

            result.add(
                incoming
            )

            added++

        } else {

            val current =
                result[index]


            /*
             * For an import, the workbook is considered
             * authoritative for fields that it actually
             * contains in GamePrediction.
             */
            if (current != incoming) {

                result[index] =
                    incoming

                updated++

            } else {

                unchanged++
            }
        }
    }


    return PredictionMergeResult(
        predictions =
            result.sortedWith(
                compareBy<GamePrediction> {
                    it.gameDate
                }
                    .thenBy {
                        it.gameId
                    }
            ),

        added =
            added,

        updated =
            updated,

        unchanged =
            unchanged
    )
}


private fun samePredictionGame(
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


    return first.gameDate
        .take(10) ==
            second.gameDate
                .take(10) &&

            first.awayTeam.equals(
                second.awayTeam,
                ignoreCase = true
            ) &&

            first.homeTeam.equals(
                second.homeTeam,
                ignoreCase = true
            )
}