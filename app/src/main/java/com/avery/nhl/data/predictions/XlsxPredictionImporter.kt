package com.avery.nhl.data.predictions

import android.content.Context
import android.net.Uri

import com.avery.nhl.model.predictions.GamePrediction
import com.avery.nhl.model.predictions.PredictionImportResult
import com.avery.nhl.model.predictions.PredictionResultType

import org.w3c.dom.Element

import java.io.ByteArrayInputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.zip.ZipInputStream

import javax.xml.parsers.DocumentBuilderFactory


class XlsxPredictionImporter(
    private val context: Context
) {

    fun importFile(
        uri: Uri
    ): PredictionImportResult {

        val entries =
            readZipEntries(
                uri
            )


        if (entries.isEmpty()) {

            return PredictionImportResult(
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
                        "The selected file could not be read as an XLSX workbook."
                    )
            )
        }


        val sharedStrings =
            parseSharedStrings(
                entries[
                    "xl/sharedStrings.xml"
                ]
            )


        val worksheetNames =
            entries.keys
                .filter {
                    it.startsWith(
                        "xl/worksheets/"
                    ) &&
                            it.endsWith(
                                ".xml"
                            )
                }
                .sorted()


        for (sheetName in worksheetNames) {

            val bytes =
                entries[sheetName]
                    ?: continue


            val rows =
                parseWorksheet(
                    bytes =
                        bytes,

                    sharedStrings =
                        sharedStrings
                )


            val result =
                parsePredictionRows(
                    rows
                )


            /*
             * Use the first worksheet that actually
             * contains the prediction-table headers.
             */
            if (
                result != null
            ) {

                return result
            }
        }


        return PredictionImportResult(
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
                    "No worksheet containing GameDate, AwayTeam and HomeTeam was found."
                )
        )
    }

    private fun readZipEntries(
        uri: Uri
    ): Map<String, ByteArray> {

        val result =
            mutableMapOf<String, ByteArray>()


        context.contentResolver
            .openInputStream(
                uri
            )
            ?.use { input ->

                ZipInputStream(
                    input
                )
                    .use { zip ->

                        while (true) {

                            val entry =
                                zip.nextEntry
                                    ?: break


                            if (
                                !entry.isDirectory
                            ) {

                                result[
                                    entry.name
                                ] =
                                    zip.readBytes()
                            }


                            zip.closeEntry()
                        }
                    }
            }


        return result
    }

}

private fun parseSharedStrings(
    bytes: ByteArray?
): List<String> {

    if (
        bytes == null
    ) {

        return emptyList()
    }


    return try {

        val document =
            parseXml(
                bytes
            )


        val nodes =
            document
                .getElementsByTagName(
                    "si"
                )


        buildList {

            for (
            index in 0 until
                    nodes.length
            ) {

                val element =
                    nodes.item(
                        index
                    ) as? Element
                        ?: continue


                val textNodes =
                    element
                        .getElementsByTagName(
                            "t"
                        )


                val text =
                    buildString {

                        for (
                        textIndex in 0 until
                                textNodes.length
                        ) {

                            append(
                                textNodes
                                    .item(
                                        textIndex
                                    )
                                    .textContent
                            )
                        }
                    }


                add(
                    text
                )
            }
        }

    } catch (_: Exception) {

        emptyList()
    }
}

private fun parseWorksheet(
    bytes: ByteArray,
    sharedStrings: List<String>
): List<List<String?>> {

    val document =
        parseXml(
            bytes
        )


    val rowNodes =
        document
            .getElementsByTagName(
                "row"
            )


    val result =
        mutableListOf<List<String?>>()


    for (
    rowIndex in 0 until
            rowNodes.length
    ) {

        val row =
            rowNodes.item(
                rowIndex
            ) as? Element
                ?: continue


        val cellNodes =
            row.getElementsByTagName(
                "c"
            )


        val values =
            mutableMapOf<Int, String?>()


        var maxColumn =
            -1


        for (
        cellIndex in 0 until
                cellNodes.length
        ) {

            val cell =
                cellNodes.item(
                    cellIndex
                ) as? Element
                    ?: continue


            val reference =
                cell.getAttribute(
                    "r"
                )


            val column =
                excelColumnIndex(
                    reference
                )


            if (
                column < 0
            ) {
                continue
            }


            maxColumn =
                maxOf(
                    maxColumn,
                    column
                )


            values[column] =
                readCellValue(
                    cell =
                        cell,

                    sharedStrings =
                        sharedStrings
                )
        }


        if (
            maxColumn < 0
        ) {

            result.add(
                emptyList()
            )

        } else {

            result.add(
                List(
                    maxColumn + 1
                ) {
                        column ->

                    values[column]
                }
            )
        }
    }


    return result
}

private fun readCellValue(
    cell: Element,
    sharedStrings: List<String>
): String? {

    val type =
        cell.getAttribute(
            "t"
        )


    /*
     * Inline strings store their text inside
     * <is><t>...</t></is>.
     */
    if (
        type ==
        "inlineStr"
    ) {

        val texts =
            cell.getElementsByTagName(
                "t"
            )


        return buildString {

            for (
            index in 0 until
                    texts.length
            ) {

                append(
                    texts.item(
                        index
                    ).textContent
                )
            }
        }
            .trim()
            .takeIf {
                it.isNotEmpty()
            }
    }


    val valueNodes =
        cell.getElementsByTagName(
            "v"
        )


    if (
        valueNodes.length ==
        0
    ) {

        return null
    }


    val raw =
        valueNodes
            .item(
                0
            )
            .textContent
            ?.trim()
            ?: return null


    return when (
        type
    ) {

        "s" -> {

            val index =
                raw.toIntOrNull()


            if (
                index != null
            ) {

                sharedStrings
                    .getOrNull(
                        index
                    )

            } else {

                raw
            }
        }


        "b" -> {

            if (
                raw == "1"
            ) {
                "TRUE"
            } else {
                "FALSE"
            }
        }


        else ->
            raw
    }
}

private fun excelColumnIndex(
    reference: String
): Int {

    val letters =
        reference
            .takeWhile {
                it.isLetter()
            }
            .uppercase()


    if (
        letters.isBlank()
    ) {

        return -1
    }


    var result =
        0


    letters.forEach {
            character ->

        result =
            result * 26 +
                    (
                            character -
                                    'A' +
                                    1
                            )
    }


    return result - 1
}

private fun parseXml(
    bytes: ByteArray
) =
    DocumentBuilderFactory
        .newInstance()
        .apply {

            isNamespaceAware =
                false
        }
        .newDocumentBuilder()
        .parse(
            ByteArrayInputStream(
                bytes
            )
        )

private fun parsePredictionRows(
    rows: List<List<String?>>
): PredictionImportResult? {

    val headerIndex =
        rows.indexOfFirst {
                row ->

            val normalized =
                row.map {
                    normalizeHeader(
                        it
                    )
                }


            "gamedate" in normalized &&
                    "awayteam" in normalized &&
                    "hometeam" in normalized
        }


    if (
        headerIndex < 0
    ) {

        return null
    }


    val headerRow =
        rows[
            headerIndex
        ]


    val headers =
        headerRow
            .mapIndexedNotNull {
                    index,
                    value ->

                normalizeHeader(
                    value
                )
                    .takeIf {
                        it.isNotBlank()
                    }
                    ?.let {
                        it to index
                    }
            }
            .toMap()


    val imported =
        mutableListOf<GamePrediction>()


    val errors =
        mutableListOf<String>()


    var totalRows =
        0


    var skippedRows =
        0


    rows
        .drop(
            headerIndex + 1
        )
        .forEachIndexed {
                index,
                row ->

            if (
                row.all {
                    it.isNullOrBlank()
                }
            ) {

                return@forEachIndexed
            }


            totalRows++


            try {

                val prediction =
                    parsePredictionRow(
                        row =
                            row,

                        headers =
                            headers
                    )


                if (
                    prediction != null
                ) {

                    imported.add(
                        prediction
                    )

                } else {

                    skippedRows++
                }

            } catch (
                e: Exception
            ) {

                skippedRows++


                errors.add(
                    "Row ${headerIndex + index + 2}: " +
                            (
                                    e.message
                                        ?: "Could not parse row."
                                    )
                )
            }
        }


    return PredictionImportResult(

        imported =
            imported,

        totalRows =
            totalRows,

        acceptedRows =
            imported.size,

        skippedRows =
            skippedRows,

        errors =
            errors
    )
}

private fun normalizeHeader(
    value: String?
): String {

    return value
        ?.trim()
        ?.lowercase()
        ?.replace(
            " ",
            ""
        )
        ?.replace(
            "_",
            ""
        )
        ?: ""
}

private fun parsePredictionRow(
    row: List<String?>,
    headers: Map<String, Int>
): GamePrediction? {

    fun cell(
        name: String
    ): String? {

        val index =
            headers[
                normalizeHeader(
                    name
                )
            ]
                ?: return null


        return row
            .getOrNull(
                index
            )
            ?.trim()
            ?.takeIf {
                it.isNotBlank()
            }
    }


    val awayTeam =
        cell(
            "AwayTeam"
        )
            ?: return null


    val homeTeam =
        cell(
            "HomeTeam"
        )
            ?: return null


    val gameDate =
        parseExcelDate(
            cell(
                "GameDate"
            )
        )
            ?: return null


    val awayProbability =
        parseProbability(
            cell(
                "AwayWinProb"
            )
        )


    val homeProbability =
        parseProbability(
            cell(
                "HomeWinProb"
            )
        )


    val confidence =
        parseProbability(
            cell(
                "Confidence"
            )
        )


    return GamePrediction(

        gameId =
            parseLong(
                cell(
                    "GameID"
                )
            ),


        gameDate =
            gameDate,


        predictionDate =
            parseExcelDateTime(
                cell(
                    "PredictionDate"
                )
            ),


        awayTeam =
            awayTeam.uppercase(),

        homeTeam =
            homeTeam.uppercase(),


        predictedAwayScore =
            parseInt(
                cell(
                    "PredictedAwayScore"
                )
            ),

        predictedHomeScore =
            parseInt(
                cell(
                    "PredictedHomeScore"
                )
            ),


        predictedResult =
            PredictionResultType
                .fromString(
                    cell(
                        "PredictedResult"
                    )
                ),


        predictedWinner =
            cell(
                "PredictedWinner"
            )
                ?.uppercase(),


        awayWinProbability =
            awayProbability,

        homeWinProbability =
            homeProbability,

        confidence =
            confidence,


        gameIsOver =
            parseBoolean(
                cell(
                    "GameIsOver"
                )
            ),


        actualAwayScore =
            parseInt(
                cell(
                    "ActualAwayScore"
                )
            ),

        actualHomeScore =
            parseInt(
                cell(
                    "ActualHomeScore"
                )
            ),


        actualResult =
            cell(
                "ActualResult"
            )
                ?.let {
                    PredictionResultType
                        .fromString(
                            it
                        )
                },


        watchedGame =
            parseBoolean(
                cell(
                    "WatchedGame"
                )
            ),


        gameHasShutOut =
            parseBoolean(
                cell(
                    "GameHasShutOut"
                )
            ),


        gamePredictionScore =
            parseDouble(
                cell(
                    "GamePredictionScore"
                )
            ),


        gamePredictionScore2 =
            parseDouble(
                cell(
                    "GamePredictionScore2"
                )
            ),


        enhancedScore =
            parseDouble(
                cell(
                    "EnhancedScore"
                )
            )
    )
}

private fun parseDouble(
    value: String?
): Double? {

    return value
        ?.trim()
        ?.replace(
            ",",
            ""
        )
        ?.toDoubleOrNull()
}


private fun parseInt(
    value: String?
): Int? {

    return parseDouble(
        value
    )
        ?.toInt()
}


private fun parseLong(
    value: String?
): Long? {

    return parseDouble(
        value
    )
        ?.toLong()
}

private fun parseBoolean(
    value: String?
): Boolean {

    val cleaned =
        value
            ?.trim()
            ?.uppercase()
            ?.removeSuffix(
                ".0"
            )
            ?: return false


    return cleaned in
            setOf(
                "Y",
                "YES",
                "1",
                "TRUE"
            )
}

private fun parseProbability(
    value: String?
): Double? {

    val number =
        parseDouble(
            value
        )
            ?: return null


    return when {

        number > 1.0 ->
            (
                    number /
                            100.0
                    )
                .coerceIn(
                    0.0,
                    1.0
                )


        else ->
            number.coerceIn(
                0.0,
                1.0
            )
    }
}

private fun parseExcelDate(
    value: String?
): String? {

    if (
        value.isNullOrBlank()
    ) {
        return null
    }


    val numeric =
        value.toDoubleOrNull()


    if (
        numeric != null &&
        numeric > 1_000
    ) {

        return excelSerialDate(
            numeric
        )
            .toLocalDate()
            .toString()
    }


    val cleaned =
        value
            .trim()
            .take(
                10
            )


    val formats =
        listOf(
            DateTimeFormatter.ISO_LOCAL_DATE,

            DateTimeFormatter.ofPattern(
                "M/d/yyyy"
            ),

            DateTimeFormatter.ofPattern(
                "MM/dd/yyyy"
            ),

            DateTimeFormatter.ofPattern(
                "yyyy/M/d"
            )
        )


    formats.forEach {
            formatter ->

        try {

            return LocalDate
                .parse(
                    cleaned,
                    formatter
                )
                .toString()

        } catch (_: Exception) {
        }
    }


    return null
}

private fun parseExcelDateTime(
    value: String?
): String? {

    if (
        value.isNullOrBlank()
    ) {
        return null
    }


    val numeric =
        value.toDoubleOrNull()


    if (
        numeric != null &&
        numeric > 1_000
    ) {

        return excelSerialDate(
            numeric
        )
            .toString()
    }


    return try {

        LocalDateTime
            .parse(
                value.trim()
            )
            .toString()

    } catch (_: Exception) {

        parseExcelDate(
            value
        )
    }
}

private fun excelSerialDate(
    serial: Double
): LocalDateTime {

    /*
     * 1899-12-30 correctly compensates for
     * Excel's historic 1900 leap-year bug.
     */
    val base =
        LocalDate.of(
            1899,
            12,
            30
        )


    val wholeDays =
        serial.toLong()


    val fraction =
        serial -
                wholeDays


    val seconds =
        (
                fraction *
                        86_400.0
                )
            .toLong()


    return base
        .plusDays(
            wholeDays
        )
        .atStartOfDay()
        .plusSeconds(
            seconds
        )
}