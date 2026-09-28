package com.avery.nhl.model.nhl

data class NHLPlayerProfile(
    val playerId: Long,
    val firstName: String,
    val lastName: String,

    val isActive: Boolean,
    val inHallOfFame: Boolean,

    val currentTeamAbbrev: String?,
    val teamLogo: String?,
    val headshot: String?,

    val position: String?,
    val sweaterNumber: Int?,

    val seasonStats: List<NHLSeasonStats>
) {
    val fullName: String
        get() = "$firstName $lastName".trim()
}