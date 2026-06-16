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
import com.example.data.api.ApiConfig
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
        
        // 与 PC .env 中 VITE_API_URL / VITE_NEW_VIDEO 一致：线上/测试均走 Fileuploads 新版上传
        val apiConfig = ApiConfig(
            onlineBaseUrl = "https://api-a.frai.live",
            testBaseUrl = "https://api.tacpay.cn",
            useNewVideoUpload = true
        )

        // Build Repository data flows
        val repository = VideoRepository(
            videoTaskDao = database.videoTaskDao(),
            userProfileDao = database.userProfileDao(),
            apiConfig = apiConfig
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
