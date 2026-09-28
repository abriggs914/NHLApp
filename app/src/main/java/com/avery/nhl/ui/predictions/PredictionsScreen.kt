package com.avery.nhl.ui.predictions

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import androidx.compose.material3.*

import androidx.compose.runtime.*

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.avery.nhl.data.nhl.NHLRepository

import com.avery.nhl.data.predictions.PredictionRepository
import com.avery.nhl.data.predictions.XlsxPredictionImporter
import com.avery.nhl.model.nhl.NHLGame
import com.avery.nhl.model.predictions.GamePrediction
import com.avery.nhl.model.predictions.PredictionGame
import com.avery.nhl.model.predictions.PredictionImportResult
import com.avery.nhl.model.predictions.buildPredictionSummary
import com.avery.nhl.model.predictions.mergeImportedPredictions
import com.avery.nhl.model.predictions.mergePredictionGames
import com.avery.nhl.model.predictions.synchronizePredictions
import java.time.LocalDate
import kotlin.coroutines.cancellation.CancellationException

private enum class PredictionsView(
    val displayName: String
) {

    UPCOMING(
        "Upcoming"
    ),

    COMPLETED(
        "Completed"
    ),

    ACCURACY(
        "Accuracy"
    )
}

@Composable
fun PredictionsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {

    val context =
        LocalContext.current


    val repository =
        remember(
            context
        ) {

            PredictionRepository(
                context
            )
        }


    val nhlRepository =
        remember {
            NHLRepository()
        }


    var schedule by remember {
        mutableStateOf<List<NHLGame>>(
            emptyList()
        )
    }

    var scheduleLoading by remember {
        mutableStateOf(
            true
        )
    }

    var scheduleError by remember {
        mutableStateOf<String?>(
            null
        )
    }

    var selectedGame by remember {
        mutableStateOf<NHLGame?>(
            null
        )
    }


    var predictions by remember {

        mutableStateOf(
            repository
                .loadPredictions()
        )
    }


    var selectedView by remember {

        mutableStateOf(
            PredictionsView.UPCOMING
        )
    }


    val xlsxImporter =
        remember(
            context
        ) {

            XlsxPredictionImporter(
                context
            )
        }

    var importResult by remember {

        mutableStateOf<PredictionImportResult?>(
            null
        )
    }


    var showImportResult by remember {

        mutableStateOf(
            false
        )
    }

    val importLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts
                    .OpenDocument()
        ) {
                uri ->

            if (
                uri != null
            ) {

                try {

                    val imported =
                        xlsxImporter
                            .importFile(
                                uri
                            )


                    val merged =
                        mergeImportedPredictions(
                            existing =
                                predictions,

                            imported =
                                imported.imported
                        )


                    predictions =
                        merged.predictions


                    repository
                        .savePredictions(
                            merged.predictions
                        )


                    importResult =
                        imported.copy(
                            errors =
                                imported.errors +
                                        listOf(
                                            "Added: ${merged.added}",
                                            "Updated: ${merged.updated}",
                                            "Unchanged: ${merged.unchanged}"
                                        )
                        )


                    showImportResult =
                        true

                } catch (
                    e: Exception
                ) {

                    importResult =
                        PredictionImportResult(

                            imported =
                                emptyList(),

                            totalRows =
                                0,

                            acceptedRows =
                                0,

                            skippedRows =
                                0,

                            errors =
                                listOf(
                                    e.message
                                        ?: "Import failed."
                                )
                        )


                    showImportResult =
                        true
                }
            }
        }


    LaunchedEffect(
        Unit
    ) {

        scheduleLoading =
            true

        scheduleError =
            null

        try {

            schedule =
                nhlRepository
                    .getSchedule(
                        LocalDate.now()
                    )

        } catch (
            e: CancellationException
        ) {

            throw e

        } catch (
            e: Exception
        ) {

            scheduleError =
                e.message
                    ?: "Could not load the NHL schedule."

        } finally {

            scheduleLoading =
                false
        }
    }


    LaunchedEffect(
        predictions
            .mapNotNull {
                if (
                    !it.gameIsOver
                ) {
                    it.gameDate
                } else {
                    null
                }
            }
            .distinct()
    ) {

        val unfinished =
            predictions.filter {
                !it.gameIsOver &&
                        it.gameId != null
            }

        if (
            unfinished.isEmpty()
        ) {
            return@LaunchedEffect
        }

        var updated =
            predictions

        val dates =
            unfinished
                .mapNotNull {
                        prediction ->

                    try {

                        LocalDate.parse(
                            prediction.gameDate
                                .take(
                                    10
                                )
                        )

                    } catch (_: Exception) {

                        null
                    }
                }
                .distinct()
                .filter {
                    !it.isAfter(
                        LocalDate.now()
                    )
                }

        try {

            dates.forEach {
                    date ->

                val games =
                    nhlRepository
                        .getScores(
                            date
                        )

                updated =
                    synchronizePredictions(
                        predictions =
                            updated,

                        games =
                            games
                    )
            }

            if (
                updated !=
                predictions
            ) {

                predictions =
                    updated

                repository
                    .savePredictions(
                        updated
                    )
            }

        } catch (
            e: CancellationException
        ) {

            throw e

        } catch (_: Exception) {

            /*
             * Results syncing should not prevent
             * the Predictions screen itself from
             * opening.
             */
        }
    }


    val editingGame =
        selectedGame

    if (
        editingGame != null
    ) {

        val existing =
            predictions
                .firstOrNull {
                    it.gameId ==
                            editingGame.id
                }

        PredictionEditorScreen(
            game =
                editingGame,

            existingPrediction =
                existing,

            onSave = {
                    prediction ->

                predictions =
                    repository
                        .upsertPrediction(
                            prediction
                        )

                selectedGame =
                    null
            },

            onDelete =
                existing
                    ?.let {

                        {

                            predictions =
                                repository
                                    .deletePrediction(
                                        it
                                    )

                            selectedGame =
                                null
                        }
                    },

            onBack = {
                selectedGame =
                    null
            },

            modifier =
                modifier
        )

        return
    }


    if (
        showImportResult
    ) {

        importResult
            ?.let {
                    result ->

                AlertDialog(

                    onDismissRequest = {
                        showImportResult =
                            false
                    },


                    title = {

                        Text(
                            "Excel Import"
                        )
                    },


                    text = {

                        Column(
                            verticalArrangement =
                                Arrangement.spacedBy(
                                    4.dp
                                )
                        ) {

                            Text(
                                "Rows found: ${result.totalRows}"
                            )

                            Text(
                                "Imported: ${result.acceptedRows}"
                            )

                            Text(
                                "Skipped: ${result.skippedRows}"
                            )


                            if (
                                result.errors.isNotEmpty()
                            ) {

                                HorizontalDivider(
                                    modifier =
                                        Modifier.padding(
                                            vertical = 5.dp
                                        )
                                )


                                result.errors
                                    .take(
                                        10
                                    )
                                    .forEach {

                                        Text(
                                            text =
                                                it,

                                            fontSize =
                                                11.sp
                                        )
                                    }
                            }
                        }
                    },


                    confirmButton = {

                        TextButton(
                            onClick = {
                                showImportResult =
                                    false
                            }
                        ) {

                            Text(
                                "OK"
                            )
                        }
                    }
                )
            }
    }


    Column(
        modifier =
            modifier.fillMaxSize()
    ) {

        PredictionsTopBar(

            onBack =
                onBack,

            onImport = {

                importLauncher.launch(
                    arrayOf(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                        "application/vnd.ms-excel"
                    )
                )
            }
        )


        PredictionsViewSelector(
            selected =
                selectedView,

            onSelected = {
                selectedView =
                    it
            }
        )


        when (
            selectedView
        ) {

            PredictionsView.UPCOMING -> {

                when {

                    scheduleLoading -> {

                        Box(
                            modifier =
                                Modifier.fillMaxSize(),

                            contentAlignment =
                                Alignment.Center
                        ) {

                            CircularProgressIndicator()
                        }
                    }


                    scheduleError != null -> {

                        Box(
                            modifier =
                                Modifier.fillMaxSize(),

                            contentAlignment =
                                Alignment.Center
                        ) {

                            Text(
                                text =
                                    scheduleError!!,

                                color =
                                    MaterialTheme
                                        .colorScheme
                                        .error
                            )
                        }
                    }


                    else -> {

                        UpcomingPredictionList(
                            items =
                                mergePredictionGames(
                                    games =
                                        schedule
                                            .filter {
                                                !it.isFinal
                                            },

                                    predictions =
                                        predictions
                                ),

                            onGameClick = {
                                    game ->

                                selectedGame =
                                    game
                            }
                        )
                    }
                }
            }


            PredictionsView.COMPLETED -> {

                PredictionList(
                    predictions =
                        predictions
                            .filter {
                                it.gameIsOver
                            }
                            .sortedByDescending {
                                it.gameDate
                            },

                    emptyMessage =
                        "No completed predictions."
                )
            }


            PredictionsView.ACCURACY -> {

                PredictionAccuracyScreen(
                    predictions =
                        predictions
                )
            }
        }
    }
}

@Composable
private fun PredictionsTopBar(
    onBack: () -> Unit,
    onImport: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 8.dp,
                vertical = 6.dp
            ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        TextButton(
            onClick =
                onBack
        ) {

            Text(
                "‹ Home"
            )
        }


        TextButton(
            onClick =
                onImport
        ) {

            Text(
                "Import"
            )
        }


        Text(
            text =
                "Predictions",

            modifier =
                Modifier.weight(1f),

            fontSize =
                21.sp,

            fontWeight =
                FontWeight.Bold
        )
    }
}

@Composable
private fun PredictionsViewSelector(
    selected: PredictionsView,
    onSelected:
        (PredictionsView) -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 12.dp,
                vertical = 5.dp
            ),

        horizontalArrangement =
            Arrangement.spacedBy(
                7.dp
            )
    ) {

        PredictionsView.entries
            .forEach {
                    view ->

                FilterChip(
                    selected =
                        selected ==
                                view,

                    onClick = {
                        onSelected(
                            view
                        )
                    },

                    label = {

                        Text(
                            view.displayName
                        )
                    }
                )
            }
    }
}

@Composable
private fun PredictionList(
    predictions:
    List<GamePrediction>,

    emptyMessage: String
) {

    if (
        predictions.isEmpty()
    ) {

        Box(
            modifier =
                Modifier.fillMaxSize(),

            contentAlignment =
                Alignment.Center
        ) {

            Text(
                text =
                    emptyMessage,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }

        return
    }


    LazyColumn(
        modifier =
            Modifier.fillMaxSize(),

        contentPadding =
            PaddingValues(
                start = 12.dp,
                end = 12.dp,
                bottom = 24.dp
            ),

        verticalArrangement =
            Arrangement.spacedBy(
                8.dp
            )
    ) {

        items(
            items =
                predictions,

            key = {
                    prediction ->

                prediction.gameId
                    ?.toString()
                    ?: (
                            prediction.gameDate +
                                    "-" +
                                    prediction.awayTeam +
                                    "-" +
                                    prediction.homeTeam
                            )
            }
        ) {
                prediction ->

            PredictionGameCard(
                prediction =
                    prediction
            )
        }
    }
}

@Composable
private fun UpcomingPredictionList(
    items:
    List<PredictionGame>,

    onGameClick:
        (NHLGame) -> Unit
) {

    if (
        items.isEmpty()
    ) {

        Box(
            modifier =
                Modifier.fillMaxSize(),

            contentAlignment =
                Alignment.Center
        ) {

            Text(
                text =
                    "No upcoming NHL games found.",

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }

        return
    }

    LazyColumn(
        modifier =
            Modifier.fillMaxSize(),

        contentPadding =
            PaddingValues(
                start = 12.dp,
                end = 12.dp,
                bottom = 28.dp
            ),

        verticalArrangement =
            Arrangement.spacedBy(
                8.dp
            )
    ) {

        items(
            items =
                items,

            key = {
                it.game.id
            }
        ) {
                item ->

            UpcomingPredictionGameCard(
                item =
                    item,

                onClick = {

                    onGameClick(
                        item.game
                    )
                }
            )
        }
    }
}