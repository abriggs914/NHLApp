package com.avery.nhl.model.nhl


data class NHLStandings(
    val date: String?,
    val teams: List<NHLStandingsTeam>
)


data class NHLStandingsTeam(
    val teamAbbrev: String,
    val teamName: String,
    val teamLogo: String?,

    val conferenceName: String?,
    val divisionName: String?,

    val gamesPlayed: Int,
    val wins: Int,
    val losses: Int,
    val otLosses: Int,

    val points: Int,
    val pointPctg: Double?,

    val goalsFor: Int?,
    val goalsAgainst: Int?,
    val goalDifferential: Int?,

    val leagueSequence: Int?,
    val conferenceSequence: Int?,
    val divisionSequence: Int?,
    val wildcardSequence: Int?,

    val streakCode: String?,
    val streakCount: Int?
) {

    val streakDisplay: String
        get() {

            if (
                streakCode.isNullOrBlank() ||
                streakCount == null
            ) {
                return "-"
            }

            return "$streakCode$streakCount"
        }

    val playoffMarker: String?
        get() {

            if (
                divisionSequence != null &&
                divisionSequence <= 3
            ) {
                return "P"
            }


            if (
                wildcardSequence != null &&
                wildcardSequence in 1..2
            ) {
                return "WC"
            }


            return null
        }

    val pointsPercentage: Double
        get() =
            if (gamesPlayed > 0) {
                points.toDouble() /
                        (gamesPlayed * 2.0)
            } else {
                0.0
            }


    val goalDifferentialPerGame: Double
        get() =
            if (gamesPlayed > 0) {
                if (goalDifferential == null) 0.0
                else goalDifferential.toDouble() / gamesPlayed
            } else {
                0.0
            }
}


enum class StandingsView {
    LEAGUE,
    CONFERENCE,
    DIVISION
}