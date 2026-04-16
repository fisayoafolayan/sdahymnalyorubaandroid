package com.sdahymnal.yoruba

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import com.sdahymnal.yoruba.navigation.HymnNavGraph
import com.sdahymnal.yoruba.ui.theme.SDAHymnalTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        viewModel.setDeepLink(parseHymnFromIntent(intent))

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()

            SDAHymnalTheme(themeMode = themeMode) {
                val navController = rememberNavController()
                HymnNavGraph(
                    navController = navController,
                    viewModel = viewModel,
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        viewModel.setDeepLink(parseHymnFromIntent(intent))
    }

    private fun parseHymnFromIntent(intent: Intent?): Int {
        val uri = intent?.data ?: return -1
        val number = uri.getQueryParameter("hymn")?.toIntOrNull() ?: return -1
        return if (number > 0) number else -1
    }
}
