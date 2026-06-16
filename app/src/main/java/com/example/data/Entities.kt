package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "video_tasks")
data class VideoTask(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val modelType: String, // "Minimax", "Wan", "Kling"
    val prompt: String,
    val sourceImageUri: String? = null,
    val status: String = "idle", // "idle", "uploading", "processing", "success", "failed"
    val progress: Int = 0,
    val resultVideoUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val reported: Boolean = false
)

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1, // Singleton profile
    val uid: String,
    val email: String,
    val displayName: String,
    val credits: Int = 50,
    val isSubscribed: Boolean = false,
    val drawChanceRemaining: Int = 1
)
