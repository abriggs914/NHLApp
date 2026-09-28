package com.avery.nhl.data.nhl

import com.avery.nhl.model.nhl.NHLPlayerProfile


suspend fun fetchNhlPlayerProfile(
    playerId: Long?
): NHLPlayerProfile? {

    return NHLRepository()
        .getPlayerProfile(
            playerId
        )
}