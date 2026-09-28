package com.avery.nhl.ui.collection

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.avery.nhl.model.jersey.Jersey
import com.avery.nhl.model.jersey.jerseyColour
import com.avery.nhl.model.jersey.toDisplayString

@Composable
fun JerseyCard(
    jersey: Jersey,
    imageUris: Map<String, Uri>,
    onClick: () -> Unit
) {
    val primaryImageUri = jersey.images
        .firstNotNullOfOrNull { fileName ->
            imageUris[fileName]
        }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },
    ) {
        Column {

            if (primaryImageUri != null) {

                AsyncImage(
                    model = primaryImageUri,
                    contentDescription = "${jersey.team} jersey",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )
            }
            else {

                Text(
                    text = "No images",
                )
            }

            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                Text(
                    text = jersey.team,
                    fontSize = 18.sp
                )

                jersey.playerName?.let { player ->

                    val playerText =
                        if (jersey.number != null) {
                            "$player #${jersey.number}"
                        } else {
                            player
                        }

                    Text(
                        text = playerText,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                jersey.league?.let {
                    Text(
                        text = it,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                jersey.model?.let {
                    Text(text = it)
                }

                jersey.size?.let {
                    Text(text = "Size: $it")
                }

                Text(
//                    text = "ID: J_${jersey.id.toString().padStart(3, '0')}",
                    text = jersey.toDisplayString(),
                    modifier = Modifier.padding(top = 8.dp)
                )

                Text(
//                    text = "ID: J_${jersey.id.toString().padStart(3, '0')}",
                    text = jersey.manufactureDate.toString(),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun ColourSwatch(
    colourName: String,
    size: Dp = 22.dp
) {

    Box(
        modifier = Modifier
            .size(size)
            .background(
                color = jerseyColour(colourName),
                shape = RoundedCornerShape(3.dp)
            )
            .border(
                width = 1.dp,
                color = Color.Gray,
                shape = RoundedCornerShape(3.dp)
            )
            .semantics {
                contentDescription =
                    "Colour: $colourName"
            }
    )
}

@Composable
fun ColourMultiSelectDropdown(
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
            text = "Colours",
            fontSize = 12.sp
        )

        OutlinedButton(
            onClick = {
                expanded = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {

            if (selectedValues.isEmpty()) {

                Text("All")

            } else {

                Row(
                    horizontalArrangement =
                        Arrangement.spacedBy(5.dp),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    selectedValues
                        .take(6)
                        .forEach {

                            ColourSwatch(
                                colourName = it,
                                size = 18.dp
                            )
                        }

                    if (selectedValues.size > 6) {
                        Text(
                            "+${selectedValues.size - 6}"
                        )
                    }
                }
            }
        }

        DropdownMenu(
            expanded = expanded,

            onDismissRequest = {
                expanded = false
            }
        ) {

            options.forEach { colour ->

                val selected =
                    colour in selectedValues

                DropdownMenuItem(
                    text = {

                        Row(
                            verticalAlignment =
                                Alignment.CenterVertically,

                            horizontalArrangement =
                                Arrangement.spacedBy(8.dp)
                        ) {

                            Checkbox(
                                checked = selected,
                                onCheckedChange = null
                            )

                            ColourSwatch(
                                colourName = colour,
                                size = 18.dp
                            )

                            Text(colour)
                        }
                    },

                    onClick = {

                        val updated =
                            if (selected) {
                                selectedValues - colour
                            } else {
                                selectedValues + colour
                            }

                        onSelectionChanged(
                            updated
                        )
                    }
                )
            }

            HorizontalDivider()

            DropdownMenuItem(
                text = {
                    Text("Clear selection")
                },
                onClick = {
                    onSelectionChanged(
                        emptySet()
                    )
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