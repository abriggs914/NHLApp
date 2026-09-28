package com.avery.nhl.ui.player

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

import androidx.compose.material3.*

import androidx.compose.runtime.*

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import coil3.compose.AsyncImage

import com.avery.nhl.data.nhl.NHLRepository
import com.avery.nhl.model.nhl.NHLPlayerProfile
import com.avery.nhl.ui.collection.NHLTeamSummaryTable
import com.avery.nhl.ui.collection.NHLSeasonStatsTable

import kotlinx.coroutines.CancellationException

@Composable
fun PlayerProfileScreen(
    playerId: Long,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {

    val repository =
        remember {
            NHLRepository()
        }


    var profile by remember(
        playerId
    ) {
        mutableStateOf<NHLPlayerProfile?>(
            null
        )
    }


    var loading by remember(
        playerId
    ) {
        mutableStateOf(true)
    }


    var error by remember(
        playerId
    ) {
        mutableStateOf<String?>(
            null
        )
    }


    var refreshKey by remember {
        mutableIntStateOf(0)
    }


    LaunchedEffect(
        playerId,
        refreshKey
    ) {

        loading = true
        error = null


        try {

            profile =
                repository.getPlayerProfile(
                    playerId
                )


            if (profile == null) {

                error =
                    "Player information is unavailable."
            }

        } catch (
            e: CancellationException
        ) {

            throw e

        } catch (
            e: Exception
        ) {

            error =
                e.message
                    ?: "Could not load player information."

        } finally {

            loading = false
        }
    }


    Column(
        modifier = modifier
            .fillMaxSize()
    ) {

        PlayerProfileTopBar(
            onBack = onBack,

            onRefresh = {
                refreshKey++
            }
        )


        when {

            loading -> {

                Box(
                    modifier =
                        Modifier.fillMaxSize(),

                    contentAlignment =
                        Alignment.Center
                ) {

                    CircularProgressIndicator()
                }
            }


            error != null -> {

                PlayerProfileError(
                    message =
                        error!!,

                    onRetry = {
                        refreshKey++
                    }
                )
            }


            profile != null -> {

                PlayerProfileContent(
                    profile =
                        profile!!
                )
            }
        }
    }
}

@Composable
private fun PlayerProfileTopBar(
    onBack: () -> Unit,
    onRefresh: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 12.dp,
                vertical = 8.dp
            ),

        horizontalArrangement =
            Arrangement.SpaceBetween,

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        TextButton(
            onClick =
                onBack
        ) {

            Text(
                "‹ Back"
            )
        }


        Text(
            text =
                "Player Profile",

            fontSize =
                20.sp,

            fontWeight =
                FontWeight.Bold
        )


        TextButton(
            onClick =
                onRefresh
        ) {

            Text(
                "Refresh"
            )
        }
    }
}

@Composable
fun PlayerProfileHeader(
    profile: NHLPlayerProfile
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 12.dp
            )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    16.dp
                ),

            verticalAlignment =
                Alignment.CenterVertically,

            horizontalArrangement =
                Arrangement.spacedBy(
                    14.dp
                )
        ) {

            profile.headshot
                ?.let { url ->

                    AsyncImage(
                        model = url,

                        contentDescription =
                            "${profile.fullName} headshot",

                        contentScale =
                            ContentScale.Fit,

                        modifier =
                            Modifier.size(
                                110.dp
                            )
                    )
                }


            Column(
                modifier =
                    Modifier.weight(1f)
            ) {

                Text(
                    text =
                        profile.fullName,

                    fontSize =
                        23.sp,

                    fontWeight =
                        FontWeight.Bold
                )


                val detailLine =
                    buildList {

                        profile.position
                            ?.takeIf {
                                it.isNotBlank()
                            }
                            ?.let {
                                add(it)
                            }


                        profile.sweaterNumber
                            ?.let {
                                add("#$it")
                            }


                        profile.currentTeamAbbrev
                            ?.takeIf {
                                it.isNotBlank()
                            }
                            ?.let {
                                add(it)
                            }
                    }
                        .joinToString(
                            " • "
                        )


                if (
                    detailLine.isNotBlank()
                ) {

                    Text(
                        text =
                            detailLine,

                        modifier =
                            Modifier.padding(
                                top = 3.dp
                            )
                    )
                }


                Text(
                    text =
                        if (
                            profile.isActive
                        ) {
                            "Active"
                        } else {
                            "Inactive"
                        },

                    modifier =
                        Modifier.padding(
                            top = 4.dp
                        )
                )


                if (
                    profile.inHallOfFame
                ) {

                    Text(
                        text =
                            "Hockey Hall of Fame",

                        fontWeight =
                            FontWeight.Bold,

                        modifier =
                            Modifier.padding(
                                top = 3.dp
                            )
                    )
                }
            }


            profile.teamLogo
                ?.let {

                    AsyncImage(
                        model = it,

                        contentDescription =
                            "Team logo",

                        modifier =
                            Modifier.size(
                                70.dp
                            ),

                        contentScale =
                            ContentScale.Fit
                    )
                }
        }
    }
}

@Composable
private fun PlayerProfileContent(
    profile: NHLPlayerProfile
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(
                rememberScrollState()
            )
            .padding(
                bottom = 24.dp
            )
    ) {

        PlayerProfileHeader(
            profile =
                profile
        )


        Spacer(
            modifier =
                Modifier.height(
                    12.dp
                )
        )


        Column(
            modifier =
                Modifier.padding(
                    horizontal = 12.dp
                )
        ) {

            NHLSeasonStatsTable(
                profile =
                    profile
            )


            Spacer(
                modifier =
                    Modifier.height(
                        8.dp
                    )
            )


            NHLTeamSummaryTable(
                profile =
                    profile
            )
        }
    }
}

@Composable
private fun PlayerProfileError(
    message: String,
    onRetry: () -> Unit
) {

    Column(
        modifier =
            Modifier.fillMaxSize(),

        horizontalAlignment =
            Alignment.CenterHorizontally,

        verticalArrangement =
            Arrangement.Center
    ) {

        Text(
            text =
                message,

            color =
                MaterialTheme
                    .colorScheme
                    .error
        )


        Button(
            onClick =
                onRetry,

            modifier =
                Modifier.padding(
                    top = 12.dp
                )
        ) {

            Text(
                "Retry"
            )
        }
    }
}