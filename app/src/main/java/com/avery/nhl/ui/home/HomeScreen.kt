package com.avery.nhl.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.avery.nhl.navigation.AppDestination


@Composable
fun HomeScreen(
    onNavigate: (
        AppDestination
    ) -> Unit,
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),

        verticalArrangement =
            Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = "NHL Companion",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Live NHL",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        HomeMenuRow(
            leftText = "Scores",
            leftDestination =
                AppDestination.SCORES,

            rightText = "Standings",
            rightDestination =
                AppDestination.STANDINGS,

            onNavigate = onNavigate
        )

        HomeMenuRow(
            leftText = "Leaders",
            leftDestination =
                AppDestination.LEADERS,

            rightText = "Playoffs",
            rightDestination =
                AppDestination.PLAYOFFS,

            onNavigate = onNavigate
        )

        Text(
            text = "My Data",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier =
                Modifier.padding(top = 12.dp)
        )

        HomeMenuRow(
            leftText = "Predictions",
            leftDestination =
                AppDestination.PREDICTIONS,

            rightText = "Collection",
            rightDestination =
                AppDestination.COLLECTION,

            onNavigate = onNavigate
        )

        HomeMenuRow(
            leftText = "Jersey Schedule",
            leftDestination =
                AppDestination.JERSEY_SCHEDULE,

            rightText = "History",
            rightDestination =
                AppDestination.HISTORY,

            onNavigate = onNavigate
        )
    }
}

@Composable
private fun HomeMenuRow(
    leftText: String,
    leftDestination: AppDestination,
    rightText: String,
    rightDestination: AppDestination,
    onNavigate: (AppDestination) -> Unit
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        HomeMenuButton(
            title = leftText,
            onClick = {
                onNavigate(leftDestination)
            },
            modifier = Modifier.weight(1f)
        )

        HomeMenuButton(
            title = rightText,
            onClick = {
                onNavigate(rightDestination)
            },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun HomeMenuButton(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    Card(
        modifier = modifier
            .height(96.dp)
            .clickable(
                onClick = onClick
            )
    ) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),

            contentAlignment =
                Alignment.Center
        ) {

            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}