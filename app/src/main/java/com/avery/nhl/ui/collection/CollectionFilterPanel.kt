package com.avery.nhl.ui.collection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.avery.nhl.model.jersey.CollectionFilters
import com.avery.nhl.model.jersey.Jersey
import com.avery.nhl.model.jersey.JerseySort
import com.avery.nhl.model.jersey.MonthYearFilter
import com.avery.nhl.model.jersey.SortMode
import com.avery.nhl.model.jersey.SortRule
import com.avery.nhl.model.jersey.displayName
import com.avery.nhl.util.extractDateParts
import com.avery.nhl.util.monthNames
import kotlin.collections.map

@Composable
fun CollectionFilterPanel(
    jerseys: List<Jersey>,
    filters: CollectionFilters,
    sortRules: List<SortRule>,
    sortMode: SortMode,

    onApply: (
        CollectionFilters,
        List<SortRule>,
        SortMode
    ) -> Unit
) {
    var expanded by remember {
        mutableStateOf(false)
    }

    var draftFilters by remember(filters) {
        mutableStateOf(filters)
    }

    var draftSortRules by remember(sortRules) {
        mutableStateOf(sortRules)
    }

    var draftSortMode by remember(sortMode) {
        mutableStateOf(sortMode)
    }

    val cancelled =
        jerseys.mapNotNull { it.cancelled }
            .distinct()
            .sorted()

    val leagues =
        jerseys.mapNotNull { it.league }
            .distinct()
            .sorted()

    val teams =
        jerseys.map { it.team }
            .distinct()
            .sorted()

    val players =
        jerseys.mapNotNull { it.playerName }
            .distinct()
            .sorted()

    val sizes =
        jerseys.mapNotNull { it.size }
            .distinct()
            .sorted()

    val brands =
        jerseys.mapNotNull { it.brand }
            .distinct()
            .sorted()

    val models =
        jerseys.mapNotNull { it.model }
            .distinct()
            .sorted()

    val suppliers =
        jerseys.mapNotNull { it.supplier }
            .distinct()
            .sorted()

    val colours =
        jerseys
            .flatMap {
                it.colours
            }
            .distinct()
            .sorted()

    val numbers =
        jerseys.mapNotNull { it.number }
            .distinct()
            .sorted()

    val makes =
        jerseys.mapNotNull { it.make }
            .distinct()
            .sorted()

    val firstSeasons =
        jerseys.mapNotNull { it.firstSeason }
            .distinct()
            .sorted()

    val lastSeasons =
        jerseys.mapNotNull { it.lastSeason }
            .distinct()
            .sorted()

    val aPatches =
        jerseys.mapNotNull { it.aPatch }
            .distinct()
            .sorted()

    val cPatches =
        jerseys.mapNotNull { it.cPatch }
            .distinct()
            .sorted()

    val positions =
        jerseys.mapNotNull { it.position }
            .distinct()
            .sorted()

    val nationalities =
        jerseys.mapNotNull { it.nationality }
            .distinct()
            .sorted()

    fun yearsFromDates(
        dates: List<String?>
    ): List<Int> {

        return dates
            .mapNotNull {
                extractDateParts(it)?.year
            }
            .distinct()
            .sortedDescending()
    }

    val dobYears =
        yearsFromDates(
            jerseys.map {
                it.details["DOB"]
            }
        )

    val orderYears =
        yearsFromDates(
            jerseys.map {
                it.orderDate
            }
        )

    val receiveYears =
        yearsFromDates(
            jerseys.map {
                it.receiveDate
            }
        )

    val openYears =
        yearsFromDates(
            jerseys.map {
                it.openDate
            }
        )

    val manufactureYears =
        yearsFromDates(
            jerseys.map {
                it.manufactureDate
            }
        )

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.SpaceBetween
        ) {

            Button(
                onClick = {
                    expanded = !expanded
                }
            ) {
                Text(
                    if (expanded)
                        "Hide Filters"
                    else
                        "Filters"
                )
            }

            OutlinedButton(
                onClick = {
                    draftFilters =
                        CollectionFilters()

                    draftSortRules =
                        listOf(
                            SortRule(
                                field = JerseySort.ID,
                                ascending = true
                            )
                        )

                    draftSortMode =
                        SortMode.ORDERED

                    onApply(
                        draftFilters,
                        draftSortRules,
                        draftSortMode
                    )
                }
            ) {
                Text("Clear")
            }
        }

        if (expanded) {

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {

                Column(
                    modifier = Modifier.padding(12.dp)
                ) {

                    // Scroll ONLY the controls.
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 330.dp)
                            .verticalScroll(
                                rememberScrollState()
                            ),
                        verticalArrangement =
                            Arrangement.spacedBy(8.dp)
                    ) {

                        MultiSelectDropdown(
                            label = "Cancelled",
                            selectedValues =
                                draftFilters.cancelled,
                            options = cancelled,
                            onSelectionChanged = {
                                draftFilters =
                                    draftFilters.copy(
                                        cancelled = it
                                    )
                            }
                        )

                        MultiSelectDropdown(
                            label = "League",
                            selectedValues =
                                draftFilters.leagues,
                            options = leagues,
                            onSelectionChanged = {
                                draftFilters =
                                    draftFilters.copy(
                                        leagues = it
                                    )
                            }
                        )

                        MultiSelectDropdown(
                            label = "Nationality",
                            selectedValues =
                                draftFilters.nationalities,
                            options = nationalities,
                            onSelectionChanged = {
                                draftFilters =
                                    draftFilters.copy(
                                        nationalities = it
                                    )
                            }
                        )

                        MultiSelectDropdown(
                            label = "Team",
                            selectedValues =
                                draftFilters.teams,
                            options = teams,
                            onSelectionChanged = {
                                draftFilters =
                                    draftFilters.copy(
                                        teams = it
                                    )
                            }
                        )

                        MultiSelectDropdown(
                            label = "Player",
                            selectedValues =
                                draftFilters.players,
                            options = players,
                            onSelectionChanged = {
                                draftFilters =
                                    draftFilters.copy(
                                        players = it
                                    )
                            }
                        )

                        MultiSelectDropdown(
                            label = "Position",
                            selectedValues =
                                draftFilters.positions,
                            options = positions,
                            onSelectionChanged = {
                                draftFilters =
                                    draftFilters.copy(
                                        positions = it
                                    )
                            }
                        )

                        MultiSelectDropdown(
                            label = "Size",
                            selectedValues =
                                draftFilters.sizes,
                            options = sizes,
                            onSelectionChanged = {
                                draftFilters =
                                    draftFilters.copy(
                                        sizes = it
                                    )
                            }
                        )

                        MultiSelectDropdown(
                            label = "Brand",
                            selectedValues =
                                draftFilters.brands,
                            options = brands,
                            onSelectionChanged = {
                                draftFilters =
                                    draftFilters.copy(
                                        brands = it
                                    )
                            }
                        )

                        MultiSelectDropdown(
                            label = "Make",
                            selectedValues =
                                draftFilters.makes,
                            options = makes,
                            onSelectionChanged = {
                                draftFilters =
                                    draftFilters.copy(
                                        makes = it
                                    )
                            }
                        )

                        MultiSelectDropdown(
                            label = "Style / Model",
                            selectedValues =
                                draftFilters.models,
                            options = models,
                            onSelectionChanged = {
                                draftFilters =
                                    draftFilters.copy(
                                        models = it
                                    )
                            }
                        )

                        MultiSelectDropdown(
                            label = "Supplier",
                            selectedValues =
                                draftFilters.suppliers,
                            options = suppliers,
                            onSelectionChanged = {
                                draftFilters =
                                    draftFilters.copy(
                                        suppliers = it
                                    )
                            }
                        )

                        MultiSelectDropdown(
                            label = "Colours",

                            selectedValues =
                                draftFilters.colours,

                            options =
                                colours,

                            onSelectionChanged = {

                                draftFilters =
                                    draftFilters.copy(
                                        colours = it
                                    )
                            }
                        )

                        MultiSelectDropdown(
                            label = "Number",
                            selectedValues =
                                draftFilters.numbers
                                    .map { it.toString() }
                                    .toSet(),
                            options =
                                numbers.map {
                                    it.toString()
                                },
                            onSelectionChanged = {
                                draftFilters =
                                    draftFilters.copy(
                                        numbers =
                                            it.mapNotNull {
                                                    value ->
                                                value.toIntOrNull()
                                            }.toSet()
                                    )
                            }
                        )

                        MultiSelectDropdown(
                            label = "First Season",
                            selectedValues =
                                draftFilters.firstSeasons,
                            options = firstSeasons,
                            onSelectionChanged = {
                                draftFilters =
                                    draftFilters.copy(
                                        firstSeasons = it
                                    )
                            }
                        )

                        MultiSelectDropdown(
                            label = "Last Season",
                            selectedValues =
                                draftFilters.lastSeasons,
                            options = lastSeasons,
                            onSelectionChanged = {
                                draftFilters =
                                    draftFilters.copy(
                                        lastSeasons = it
                                    )
                            }
                        )

                        MultiSelectDropdown(
                            label = "Captain Patch",
                            selectedValues =
                                draftFilters.cPatches,
                            options = cPatches,
                            onSelectionChanged = {
                                draftFilters =
                                    draftFilters.copy(
                                        cPatches = it
                                    )
                            }
                        )

                        MultiSelectDropdown(
                            label = "Assistant Patch",
                            selectedValues =
                                draftFilters.aPatches,
                            options = aPatches,
                            onSelectionChanged = {
                                draftFilters =
                                    draftFilters.copy(
                                        aPatches = it
                                    )
                            }
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(
                                vertical = 8.dp
                            )
                        )

                        Text(
                            text = "Date Filters",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        MonthYearFilterControl(
                            label = "DOB",
                            filter = draftFilters.dob,
                            availableYears = dobYears,
                            onChanged = {
                                draftFilters =
                                    draftFilters.copy(
                                        dob = it
                                    )
                            }
                        )

                        MonthYearFilterControl(
                            label = "Order Date",
                            filter = draftFilters.orderDate,
                            availableYears = orderYears,
                            onChanged = {
                                draftFilters =
                                    draftFilters.copy(
                                        orderDate = it
                                    )
                            }
                        )

                        MonthYearFilterControl(
                            label = "Receive Date",
                            filter = draftFilters.receiveDate,
                            availableYears = receiveYears,
                            onChanged = {
                                draftFilters =
                                    draftFilters.copy(
                                        receiveDate = it
                                    )
                            }
                        )

                        MonthYearFilterControl(
                            label = "Open Date",
                            filter = draftFilters.openDate,
                            availableYears = openYears,
                            onChanged = {
                                draftFilters =
                                    draftFilters.copy(
                                        openDate = it
                                    )
                            }
                        )

                        MonthYearFilterControl(
                            label = "Manufacture Date",
                            filter = draftFilters.manufactureDate,
                            availableYears = manufactureYears,
                            onChanged = {
                                draftFilters =
                                    draftFilters.copy(
                                        manufactureDate = it
                                    )
                            }
                        )

                        // Keep your existing
                        // Jersey Type radio buttons here.

                        // Keep your existing
                        // Image Filter radio buttons here.

                        HorizontalDivider(
                            modifier = Modifier.padding(
                                vertical = 8.dp
                            )
                        )

                        Text(
                            text = "Sort Priority",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        draftSortRules.forEachIndexed { index, rule ->

                            SortRuleControl(
                                priority = index + 1,
                                rule = rule,

                                availableFields =
                                    JerseySort.entries,

                                onChanged = { updatedRule ->

                                    draftSortRules =
                                        draftSortRules
                                            .toMutableList()
                                            .also {
                                                it[index] =
                                                    updatedRule
                                            }
                                },

                                onRemove = {

                                    draftSortRules =
                                        draftSortRules
                                            .toMutableList()
                                            .also {
                                                it.removeAt(index)
                                            }
                                }
                            )
                        }

                        OutlinedButton(
                            onClick = {

                                val usedFields =
                                    draftSortRules
                                        .map { it.field }
                                        .toSet()

                                val nextField =
                                    JerseySort.entries
                                        .firstOrNull {
                                            it !in usedFields
                                        }

                                if (nextField != null) {

                                    draftSortRules =
                                        draftSortRules +
                                                SortRule(
                                                    field = nextField
                                                )
                                }
                            },

                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("+ Add Sort")
                        }

                        Text(
                            text = "Sort Mode",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(
                                top = 8.dp
                            )
                        )

                        Row(
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            RadioButton(
                                selected =
                                    draftSortMode ==
                                            SortMode.ORDERED,

                                onClick = {
                                    draftSortMode =
                                        SortMode.ORDERED
                                }
                            )

                            Text("Priority Sort")
                        }

                        Row(
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            RadioButton(
                                selected =
                                    draftSortMode ==
                                            SortMode.RANDOM,

                                onClick = {
                                    draftSortMode =
                                        SortMode.RANDOM
                                }
                            )

                            Text("Random")
                        }
                    }

                    HorizontalDivider(
                        modifier =
                            Modifier.padding(
                                vertical = 8.dp
                            )
                    )

                    Button(
                        onClick = {

                            onApply(
                                draftFilters,
                                draftSortRules,
                                draftSortMode
                            )

                            expanded = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Apply Filters")
                    }
                }
            }
        }
    }
}

@Composable
fun MultiSelectDropdown(
    label: String,
    selectedValues: Set<String>,
    options: List<String>,
    onSelectionChanged: (Set<String>) -> Unit
) {
    var expanded by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {

        Text(
            text = label,
            fontSize = 12.sp
        )

        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                when {
                    selectedValues.isEmpty() -> "All"
                    selectedValues.size == 1 ->
                        selectedValues.first()
                    else ->
                        "${selectedValues.size} selected"
                }
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {

            options.forEach { option ->

                val selected =
                    option in selectedValues

                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selected,
                                onCheckedChange = null
                            )

                            Text(option)
                        }
                    },

                    onClick = {

                        val updated =
                            if (selected) {
                                selectedValues - option
                            } else {
                                selectedValues + option
                            }

                        onSelectionChanged(updated)

                        // Deliberately don't close menu:
                        // lets you pick Calgary + Edmonton etc.
                    }
                )
            }

            HorizontalDivider()

            DropdownMenuItem(
                text = {
                    Text("Clear selection")
                },
                onClick = {
                    onSelectionChanged(emptySet())
                }
            )

            DropdownMenuItem(
                text = {
                    Text("Done")
                },
                onClick = {
                    expanded = false
                }
            )
        }
    }
}

@Composable
fun FilterDropdown(
    label: String,
    selectedValue: String?,
    options: List<String>,
    onSelected: (String?) -> Unit
) {
    var expanded by remember {
        mutableStateOf(false)
    }

    Column {
        Text(
            text = label,
            fontSize = 12.sp
        )

        OutlinedButton(
            onClick = {
                expanded = true
            }
        ) {
            Text(
                selectedValue ?: "All"
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {
            DropdownMenuItem(
                text = {
                    Text("All")
                },
                onClick = {
                    onSelected(null)
                    expanded = false
                }
            )

            options.forEach { option ->

                DropdownMenuItem(
                    text = {
                        Text(option)
                    },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun MonthYearFilterControl(
    label: String,
    filter: MonthYearFilter,
    availableYears: List<Int>,
    onChanged: (MonthYearFilter) -> Unit
) {

    Text(
        text = label,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 8.dp)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement =
            Arrangement.spacedBy(8.dp)
    ) {

        Box(
            modifier = Modifier.weight(1f)
        ) {

            FilterDropdown(
                label = "Month",

                selectedValue =
                    filter.month?.let {
                        monthNames[it - 1]
                    },

                options = monthNames,

                onSelected = { selected ->

                    val month =
                        selected?.let {
                            monthNames.indexOf(it) + 1
                        }

                    onChanged(
                        filter.copy(
                            month = month
                        )
                    )
                }
            )
        }

        Box(
            modifier = Modifier.weight(1f)
        ) {

            FilterDropdown(
                label = "Year",

                selectedValue =
                    filter.year?.toString(),

                options =
                    availableYears.map {
                        it.toString()
                    },

                onSelected = { selected ->

                    onChanged(
                        filter.copy(
                            year =
                                selected
                                    ?.toIntOrNull()
                        )
                    )
                }
            )
        }
    }
}

@Composable
fun SortRuleControl(
    priority: Int,
    rule: SortRule,
    availableFields: List<JerseySort>,
    onChanged: (SortRule) -> Unit,
    onRemove: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {

        Column(
            modifier = Modifier.padding(10.dp)
        ) {

            Text(
                text = "Priority $priority",
                fontWeight = FontWeight.Bold
            )

            FilterDropdown(
                label = "Field",

                selectedValue =
                    rule.field.displayName(),

                options =
                    availableFields.map {
                        it.displayName()
                    },

                onSelected = { selected ->

                    val field =
                        availableFields
                            .firstOrNull {
                                it.displayName() ==
                                        selected
                            }

                    if (field != null) {
                        onChanged(
                            rule.copy(
                                field = field
                            )
                        )
                    }
                }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                RadioButton(
                    selected =
                        rule.ascending,

                    onClick = {
                        onChanged(
                            rule.copy(
                                ascending = true
                            )
                        )
                    }
                )

                Text("Ascending")

                RadioButton(
                    selected =
                        !rule.ascending,

                    onClick = {
                        onChanged(
                            rule.copy(
                                ascending = false
                            )
                        )
                    }
                )

                Text("Descending")
            }

            TextButton(
                onClick = onRemove
            ) {
                Text("Remove")
            }
        }
    }
}