package com.avery.nhl.data.playoffs

import com.avery.nhl.data.nhl.NHLRepository
import com.avery.nhl.model.playoffs.TeamProjectionStats
import com.avery.nhl.model.playoffs.buildTeamProjectionStats


class PlayoffProjectionRepository(
    private val nhlRepository: NHLRepository =
        NHLRepository()
) {

    suspend fun getTeamStats(
        teamAbbrevs: Collection<String>,
        season: Int
    ): Map<String, TeamProjectionStats> {

        val standings =
            nhlRepository
                .getStandings()
                .teams


        val result =
            mutableMapOf<String, TeamProjectionStats>()


        teamAbbrevs
            .distinct()
            .forEach {
                    abbreviation ->

                val standingsTeam =
                    standings.firstOrNull {
                            team ->

                        team.teamAbbrev.equals(
                            abbreviation,
                            ignoreCase = true
                        )
                    }
                        ?: return@forEach


                val games =
                    nhlRepository
                        .getTeamSeasonSchedule(
                            teamAbbrev =
                                abbreviation,

                            season =
                                season
                        )


                result[
                    abbreviation.uppercase()
                ] =
                    buildTeamProjectionStats(
                        standings =
                            standingsTeam,

                        seasonGames =
                            games
                    )
            }


        return result
    }
}