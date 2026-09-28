package com.avery.nhl

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.avery.nhl.navigation.NHLApp
import com.avery.nhl.ui.theme.NHLTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            NHLTheme {
                NHLApp()
            }
        }
    }
}