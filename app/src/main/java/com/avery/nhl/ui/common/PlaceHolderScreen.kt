package com.avery.nhl.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.avery.nhl.navigation.AppDestination


@Composable
fun PlaceholderScreen(
    destination: AppDestination,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),

        horizontalAlignment =
            Alignment.CenterHorizontally,

        verticalArrangement =
            Arrangement.Center
    ) {

        Text(
            text = destination.title,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "This section is not implemented yet.",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(
                top = 12.dp
            )
        )

        Button(
            onClick = onBack,
            modifier = Modifier.padding(
                top = 24.dp
            )
        ) {
            Text("Back")
        }
    }
}