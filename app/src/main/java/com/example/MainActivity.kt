package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.VideoRepository
import com.example.ui.screens.MainWorkspaceScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.VideoViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Edge to edge rendering
        enableEdgeToEdge()

        // Setup local Room Database persistence
        val database = AppDatabase.getDatabase(applicationContext)
        
        // Build Repository data flows
        val repository = VideoRepository(
            videoTaskDao = database.videoTaskDao(),
            userProfileDao = database.userProfileDao()
        )

        // Instantiate state-holding view model via factory
        val viewModelFactory = VideoViewModel.Factory(application, repository)
        val viewModel = ViewModelProvider(this, viewModelFactory)[VideoViewModel::class.java]

        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Main galactic video generator workshop screen
                    MainWorkspaceScreen(
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}
