package com.avery.nhl.ui.collection

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.avery.nhl.data.collection.JerseyRepository
import com.avery.nhl.data.nhl.fetchNhlPlayerProfile
import com.avery.nhl.model.jersey.Jersey
import com.avery.nhl.model.jersey.buildCollectionAnniversaries
import com.avery.nhl.model.jersey.buildFrequencyGroups
import com.avery.nhl.model.jersey.formatDays
import com.avery.nhl.model.jersey.toDisplayString
import com.avery.nhl.model.nhl.AnniversaryType
import com.avery.nhl.model.nhl.CollectionAnniversary
import com.avery.nhl.model.nhl.CollectionStats
import com.avery.nhl.model.nhl.FrequencyGroup
import com.avery.nhl.model.nhl.FrequencyItem
import com.avery.nhl.model.nhl.calculateCollectionStats
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.collections.forEachIndexed

@Composable
fun CollectionStatsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {

    val context =
        LocalContext.current

    val jerseyRepository = remember(context) {
        JerseyRepository(context)
    }

    val jerseys =
        remember {
            jerseyRepository.loadJerseys()
        }

    val today =
        remember {
            LocalDate.now()
        }

    val stats =
        remember(jerseys, today) {
            calculateCollectionStats(
                jerseys = jerseys,
                today = today
            )
        }

    val anniversaries =
        remember(jerseys, today) {
            buildCollectionAnniversaries(
                jerseys = jerseys,
                today = today
            )
        }

    val imageUris =
        remember {
            jerseyRepository.loadImageUris()
        }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(
                rememberScrollState()
            )
            .padding(16.dp)
    ) {

        Button(
            onClick = onBack
        ) {
            Text("Back")
        }

        Text(
            text = "Collection Stats",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier =
                Modifier.padding(
                    top = 20.dp,
                    bottom = 16.dp
                )
        )

        CollectionFinancialStats(
            stats
        )

        CollectionTimingStats(
            stats
        )

        CollectionFrequencyStats(
            jerseys
        )

        CollectionAnniversaryStats(
            anniversaries = anniversaries,
            today = today,
            imageUris = imageUris
        )

        Spacer(
            Modifier.height(32.dp)
        )
    }
}

@Composable
fun AnniversaryRow(
    anniversary:
    CollectionAnniversary,
    mode: String = "current",

    imageUris:
    Map<String, Uri>
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),

        verticalAlignment =
            Alignment.CenterVertically,

        horizontalArrangement =
            Arrangement.spacedBy(10.dp)
    ) {

        AnniversaryImage(
            anniversary =
                anniversary,

            imageUris =
                imageUris
        )

        Column(
            modifier =
                Modifier.weight(1f)
        ) {

            Text(
                text =
                    anniversary.title,

                fontWeight =
                    FontWeight.Bold,

                fontSize = 13.sp
            )

            Text(
                text =
                    when (
                        anniversary.type
                    ) {

                        AnniversaryType.DOB ->
                            if (mode == "past") "Birthday • turned ${anniversary.years}"
                            else "Birthday • turns ${anniversary.years}"

                        AnniversaryType.MANUFACTURE -> "Manufactured • ${anniversary.years}-year anniversary"

                        else -> "${anniversary.type.displayName} • " +
                                "${anniversary.years}-year anniversary"
                    },

                fontSize = 11.sp
            )
        }

        Column(
            horizontalAlignment =
                Alignment.End
        ) {

            Text(
                text =
                    if (
                        anniversary.type ==
                        AnniversaryType.MANUFACTURE
                    ) {

                        anniversary
                            .occurrenceDate
                            .month
                            .getDisplayName(
                                java.time.format.TextStyle.SHORT,
                                Locale.CANADA
                            )

                    } else {

                        anniversary
                            .occurrenceDate
                            .format(
                                DateTimeFormatter.ofPattern(
                                    "MMM d"
                                )
                            )
                    },

                fontSize = 12.sp
            )

            Text(
                text =
                    when {

                        anniversary.daysAway == 0L ->
                            "Today"

                        anniversary.daysAway > 0 ->
                            "in ${anniversary.daysAway} days"

                        else ->
                            "${-anniversary.daysAway} days ago"
                    },

                fontSize = 10.sp
            )
        }
    }

    HorizontalDivider()
}

@Composable
fun AnniversaryImage(
    anniversary: CollectionAnniversary,
    imageUris: Map<String, Uri>
) {

    val jersey =
        anniversary.jersey

    if (
        anniversary.type ==
        AnniversaryType.DOB &&
        jersey?.nhlId != null
    ) {

        var headshot by remember(
            jersey.nhlId
        ) {
            mutableStateOf<String?>(null)
        }

        LaunchedEffect(
            jersey.nhlId
        ) {

            headshot =
                fetchNhlPlayerProfile(
                    jersey.nhlId
                )?.headshot
        }

        if (headshot != null) {

            AsyncImage(
                model = headshot,

                contentDescription =
                    jersey.playerName,

                contentScale =
                    ContentScale.Crop,

                modifier = Modifier
                    .size(54.dp)
                    .clip(
                        RoundedCornerShape(
                            27.dp
                        )
                    )
            )

        } else {

            AnniversaryJerseyImage(
                jersey,
                imageUris
            )
        }

    } else {

        AnniversaryJerseyImage(
            jersey,
            imageUris
        )
    }
}

@Composable
fun AnniversaryJerseyImage(
    jersey: Jersey?,
    imageUris: Map<String, Uri>
) {

    val imageUri =
        jersey
            ?.images
            ?.firstNotNullOfOrNull {
                imageUris[it]
            }

    if (imageUri != null) {

        AsyncImage(
            model = imageUri,
            contentDescription =
                jersey.toDisplayString(),

            contentScale =
                ContentScale.Crop,

            modifier = Modifier
                .size(54.dp)
                .clip(
                    RoundedCornerShape(6.dp)
                )
        )

    } else {

        Box(
            modifier = Modifier
                .size(54.dp)
                .background(
                    Color.LightGray,
                    RoundedCornerShape(6.dp)
                )
        )
    }
}

@Composable
fun CollectionAnniversaryStats(
    anniversaries:
    List<CollectionAnniversary>,
    imageUris: Map<String, Uri>,
    today: LocalDate
) {

    StatsSectionTitle(
        "Anniversaries"
    )

    var selectedTypes by remember {
        mutableStateOf(
            AnniversaryType.entries
                .toSet()
        )
    }

    var selectedNumber by remember {
        mutableStateOf(30)
    }

    AnniversaryTypeFilter(
        selected = selectedTypes,
        onChanged = {
            selectedTypes = it
        }
    )

    AnniversaryNumberRangeOptions(
        selected = selectedNumber,
        onChanged = {
            selectedNumber = it
        }
    )

    val filtered =
        anniversaries.filter {
            it.type in selectedTypes
        }

    val todayEvents =
        filtered
            .filter {
                it.daysAway == 0L
            }

    val recent =
        filtered
            .filter {
//                it.daysAway in -30L..-1L
                it.daysAway in -selectedNumber..-1L
            }
            .sortedByDescending {
                it.daysAway
            }

    val upcoming =
        filtered
            .filter {
//                it.daysAway in 1L..30L
                it.daysAway in 1L..selectedNumber
            }
            .sortedBy {
                it.daysAway
            }


    if (todayEvents.isNotEmpty()) {

        AnniversarySubheading(
            "Today"
        )

        todayEvents.forEach {
            AnniversaryRow(
                it,
                imageUris = imageUris
            )
        }
    }


    AnniversarySubheading(
        "Upcoming $selectedNumber Days"
    )

    if (upcoming.isEmpty()) {
        Text("None")
    } else {
        upcoming.forEach {
            AnniversaryRow(
                it,
                imageUris = imageUris
            )
        }
    }


    AnniversarySubheading(
        "Previous $selectedNumber Days"
    )

    if (recent.isEmpty()) {
        Text("None")
    } else {
        recent.forEach {
            AnniversaryRow(
                it,
                imageUris = imageUris
            )
        }
    }
}

@Composable
fun AnniversarySubheading(
    text: String
) {

    Text(
        text = text,
        fontSize = 16.sp,
        fontWeight =
            FontWeight.Bold,
        modifier =
            Modifier.padding(
                top = 14.dp,
                bottom = 4.dp
            )
    )
}

@Composable
fun AnniversaryNumberRangeOptions(
    selected: Int,

    onChanged:
        (Int) -> Unit
) {

    val options = listOf(30, 60, 90, 120, 150)

    Column {

        Text(
            text = "Date Range",
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()) {

            options
                .forEach { num ->

                    val checked =
                        num == selected

                    Row(
                        modifier = Modifier
                            .padding(8.dp)
                            .clickable {
                                onChanged(
                                    if (checked) {
                                        selected
                                    } else {
                                        num
                                    }
                                )
                            },
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Checkbox(
                            checked = checked,
                            onCheckedChange = null
                        )

                        Text(
                            "$num"
                        )
                    }
                }
        }
    }
}

@Composable
fun AnniversaryTypeFilter(
    selected:
    Set<AnniversaryType>,

    onChanged:
        (Set<AnniversaryType>) -> Unit
) {

    Column {

        Text(
            text = "Show",
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()) {

            AnniversaryType.entries
                .forEach { type ->

                    val checked =
                        type in selected

                    val vvs = Modifier.wrapContentWidth()

                    Row(
                        modifier = Modifier
                            .padding(5.dp)
                            .wrapContentWidth()
                            .clickable {

                                onChanged(
                                    if (checked) {
                                        selected - type
                                    } else {
                                        selected + type
                                    }
                                )
                            },

                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Checkbox(
                            checked = checked,
                            onCheckedChange = null
                        )

                        Text(
                            type.displayName
                        )
                    }
                }
        }
    }
}

@Composable
fun StatsSectionTitle(
    text: String
) {

    HorizontalDivider(
        modifier =
            Modifier.padding(
                vertical = 16.dp
            )
    )

    Text(
        text = text,
        fontSize = 20.sp,
        fontWeight =
            FontWeight.Bold,
        modifier =
            Modifier.padding(
                bottom = 8.dp
            )
    )
}

@Composable
fun CollectionFinancialStats(
    stats: CollectionStats
) {

    StatsSectionTitle(
        "Collection"
    )

    StatsCardRow(
        leftTitle =
            "Collection Cost",

        leftValue =
            String.format(
                Locale.CANADA,
                "$%,.2f",
                stats.totalCost
            ),

        leftSubtitle =
            "${stats.pricedCount} priced jerseys",

        rightTitle =
            "Average Price",

        rightValue =
            stats.averagePrice
                ?.let {
                    String.format(
                        Locale.CANADA,
                        "$%,.2f",
                        it
                    )
                }
                ?: "-"
    )

    Spacer(
        Modifier.height(8.dp)
    )

    StatsCardRow(
        leftTitle =
            "Cost per Day",

        leftValue =
            stats.dollarsPerDay
                ?.let {
                    String.format(
                        Locale.CANADA,
                        "$%.2f",
                        it
                    )
                }
                ?: "-",

        leftSubtitle =
            stats.collectionAgeDays
                ?.let {
                    "$it days collecting"
                },

        rightTitle =
            "Jerseys",

        rightValue =
            stats.jerseyCount.toString()
    )
}

@Composable
fun StatsCardRow(
    leftTitle: String,
    leftValue: String,
    leftSubtitle: String? = null,

    rightTitle: String,
    rightValue: String,
    rightSubtitle: String? = null
) {

    Row(
        modifier =
            Modifier.fillMaxWidth(),

        horizontalArrangement =
            Arrangement.spacedBy(8.dp)
    ) {

        StatsCard(
            title = leftTitle,
            value = leftValue,
            subtitle = leftSubtitle,
            modifier =
                Modifier.weight(1f)
        )

        StatsCard(
            title = rightTitle,
            value = rightValue,
            subtitle = rightSubtitle,
            modifier =
                Modifier.weight(1f)
        )
    }
}

@Composable
fun StatsCard(
    title: String,
    value: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {

    Card(
        modifier = modifier
    ) {

        Column(
            modifier =
                Modifier.padding(12.dp)
        ) {

            Text(
                text = title,
                fontSize = 12.sp
            )

            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight =
                    FontWeight.Bold
            )

            subtitle?.let {

                Text(
                    text = it,
                    fontSize = 11.sp,
                    modifier =
                        Modifier.padding(
                            top = 2.dp
                        )
                )
            }
        }
    }
}

@Composable
fun CollectionTimingStats(
    stats: CollectionStats
) {

    StatsSectionTitle(
        "Timing"
    )

    StatsCardRow(
        leftTitle =
            "Order → Receive",

        leftValue =
            formatDays(
                stats.averageOrderToReceive
            ),

        rightTitle =
            "Receive → Open",

        rightValue =
            formatDays(
                stats.averageReceiveToOpen
            )
    )

    Spacer(
        Modifier.height(8.dp)
    )

    StatsCardRow(
        leftTitle =
            "Order → Open",

        leftValue =
            formatDays(
                stats.averageOrderToOpen
            ),

        rightTitle =
            "Waiting to Receive",

        rightValue =
            stats.waitingToReceive
                .toString()
    )

    Spacer(
        Modifier.height(8.dp)
    )

    StatsCard(
        title =
            "Waiting to Open",

        value =
            stats.waitingToOpen
                .toString(),

        modifier =
            Modifier.fillMaxWidth()
    )
}


@Composable
fun CollectionFrequencyStats(
    jerseys: List<Jersey>
) {

    val groups =
        remember(jerseys) {
            buildFrequencyGroups(
                jerseys
            )
        }

    StatsSectionTitle(
        "Most / Least Common"
    )

    groups.forEach { group ->

        FrequencyGroupCard(
            group = group
        )
    }
}

@Composable
fun FrequencyGroupCard(
    group: FrequencyGroup
) {

    var expanded by remember {
        mutableStateOf(false)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {

        Column {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        expanded = !expanded
                    }
                    .padding(12.dp),

                horizontalArrangement =
                    Arrangement.SpaceBetween,

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Text(
                    text = group.title,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    if (expanded) "▲" else "▼"
                )
            }

            if (expanded) {

                HorizontalDivider()

                FrequencyRanking(
                    title = "Most Common",
                    items = group.mostCommon
                )

                HorizontalDivider()

                FrequencyRanking(
                    title = "Least Common",
                    items = group.leastCommon
                )
            }
        }
    }
}

@Composable
fun FrequencyRanking(
    title: String,
    items: List<FrequencyItem>
) {

    Column(
        modifier =
            Modifier.padding(12.dp)
    ) {

        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            modifier =
                Modifier.padding(
                    bottom = 4.dp
                )
        )

        items.forEachIndexed {
                index,
                item ->

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        vertical = 2.dp
                    ),

                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {

                Text(
                    text =
                        "${index + 1}. ${item.label}"
                )

                Text(
                    text =
                        "${item.count}×",
                    fontWeight =
                        FontWeight.Bold
                )
            }
        }
    }
}