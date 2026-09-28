package com.avery.nhl.model.nhl


enum class LeaderType {
    SKATER,
    GOALIE
}


enum class SkaterLeaderCategory(
    val apiName: String,
    val displayName: String
) {

    POINTS(
        "points",
        "Points"
    ),

    GOALS(
        "goals",
        "Goals"
    ),

    ASSISTS(
        "assists",
        "Assists"
    ),

    PLUS_MINUS(
        "plusMinus",
        "+/-"
    ),

    POWER_PLAY_GOALS(
        "goalsPp",
        "PP Goals"
    )
}


enum class GoalieLeaderCategory(
    val apiName: String,
    val displayName: String
) {

    WINS(
        "wins",
        "Wins"
    ),

    SAVE_PERCENTAGE(
        "savePctg",
        "Save %"
    ),

    GAA(
        "goalsAgainstAverage",
        "GAA"
    ),

    SHUTOUTS(
        "shutouts",
        "Shutouts"
    )
}


data class NHLStatLeader(
    val playerId: Long?,
    val firstName: String?,
    val lastName: String?,

    val teamAbbrev: String?,
    val teamName: String?,

    val headshot: String?,
    val teamLogo: String?,

    val position: String?,
    val sweaterNumber: Int?,

    val value: Double?
) {

    val name: String
        get() {

            val result =
                listOfNotNull(
                    firstName,
                    lastName
                )
                    .filter {
                        it.isNotBlank()
                    }
                    .joinToString(" ")

            return result.ifBlank {
                "Unknown Player"
            }
        }

    val fallbackHeadshot: String?
        get() {

            val id =
                playerId
                    ?: return null


            return "https://assets.nhle.com/mugs/nhl/latest/$id.png"
        }

}

enum class NHLGameType(
    val apiValue: Int,
    val displayName: String
) {

    REGULAR_SEASON(
        2,
        "Regular Season"
    ),

    PLAYOFFS(
        3,
        "Playoffs"
    )
}

data class NHLSeasonOption(
    val value: Int,
    val displayName: String
)

fun buildNHLSeasonOptions(
    newestStartYear: Int = 2025,
    oldestStartYear: Int = 1917
): List<NHLSeasonOption> {

    return (newestStartYear downTo oldestStartYear)
        .map { startYear ->

            NHLSeasonOption(
                value =
                    "$startYear${startYear + 1}"
                        .toInt(),

                displayName =
                    "$startYear-${(startYear + 1)
                        .toString()
                        .takeLast(2)}"
            )
        }
}