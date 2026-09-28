package com.avery.nhl.navigation

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding

import com.avery.nhl.ui.home.HomeScreen
import com.avery.nhl.ui.collection.CollectionScreen
import com.avery.nhl.ui.collection.CollectionStatsScreen
import com.avery.nhl.ui.common.PlaceholderScreen
import com.avery.nhl.ui.leaders.LeadersScreen
import com.avery.nhl.ui.playoffs.PlayoffsScreen
import com.avery.nhl.ui.predictions.PredictionsScreen
import com.avery.nhl.ui.scores.ScoresScreen
import com.avery.nhl.ui.standings.StandingsScreen

@Composable
fun NHLApp() {

    var destination by remember {
        mutableStateOf(
            AppDestination.HOME
        )
    }

    Scaffold { innerPadding ->

        val modifier =
            Modifier.padding(innerPadding)

        when (destination) {

            AppDestination.HOME -> {

                HomeScreen(
                    onNavigate = {
                        destination = it
                    },
                    modifier = modifier
                )
            }

            AppDestination.COLLECTION -> {

                CollectionScreen(
                    onBack = {
                        destination =
                            AppDestination.HOME
                    },
                    modifier = modifier
                )
            }

            AppDestination.COLLECTION_STATS -> {

                CollectionStatsScreen(
                    onBack = {
                        destination =
                            AppDestination.HOME
                    },
                    modifier = modifier
                )
            }

            AppDestination.SCORES -> {

                ScoresScreen(
                    onBack = {
                        destination =
                            AppDestination.HOME
                    },

                    modifier = modifier
                )
            }

            AppDestination.STANDINGS -> {

                StandingsScreen(
                    onBack = {
                        destination =
                            AppDestination.HOME
                    },

                    modifier =
                        modifier
                )
            }

            AppDestination.LEADERS -> {

                LeadersScreen(
                    onBack = {

                        destination =
                            AppDestination.HOME
                    },

                    modifier =
                        modifier
                )
            }

            AppDestination.PLAYOFFS -> {

                PlayoffsScreen(
                    onBack = {
                        destination =
                            AppDestination.HOME
                    },

                    modifier =
                        modifier
                )
            }

            AppDestination.PREDICTIONS -> {

                PredictionsScreen(
                    onBack = {
                        destination =
                            AppDestination.HOME
                    },

                    modifier =
                        modifier
                )
            }

            else -> {

                PlaceholderScreen(
                    destination = destination,
                    onBack = {
                        destination =
                            AppDestination.HOME
                    },
                    modifier = modifier
                )
            }
        }
    }
}