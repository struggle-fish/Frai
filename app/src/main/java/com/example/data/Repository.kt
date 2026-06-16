package com.example.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlin.random.Random

class VideoRepository(
    private val videoTaskDao: VideoTaskDao,
    private val userProfileDao: UserProfileDao
) {
    val allTasks: Flow<List<VideoTask>> = videoTaskDao.getAllTasks()
    val userProfile: Flow<UserProfile?> = userProfileDao.getProfile()

    // Public curated video URLs matching different stylistic models
    private val sampleVideos = mapOf(
        "Minimax" to listOf(
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4"
        ),
        "Wan" to listOf(
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4"
        ),
        "Kling" to listOf(
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4",
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4"
        )
    )

    suspend fun getProfileDirect(): UserProfile? {
        return userProfileDao.getProfileDirect()
    }

    suspend fun initializeDefaultUser(email: String = "Mosioey@gmail.com", name: String = "VidGen User") {
        val existing = userProfileDao.getProfileDirect()
        if (existing == null) {
            val randomUid = "uid_" + Random.nextInt(100000, 999999)
            userProfileDao.insertOrUpdateProfile(
                UserProfile(
                    uid = randomUid,
                    email = email,
                    displayName = name,
                    credits = 50,
                    isSubscribed = false,
                    drawChanceRemaining = 1
                )
            )
        }
    }

    suspend fun logoutUser() {
        val current = userProfileDao.getProfileDirect()
        if (current != null) {
            // Delete task logs too for clean toggle
            videoTaskDao.clearAllTasks()
            // Reset to empty profile
            userProfileDao.insertOrUpdateProfile(
                UserProfile(
                    uid = "",
                    email = "",
                    displayName = "",
                    credits = 0,
                    isSubscribed = false,
                    drawChanceRemaining = 0
                )
            )
        }
    }

    suspend fun loginWithGoogle(email: String, name: String) {
        val existing = userProfileDao.getProfileDirect()
        val uid = "google_" + Random.nextInt(100000, 999999)
        val profile = UserProfile(
            uid = uid,
            email = email,
            displayName = name,
            credits = existing?.credits ?: 50,
            isSubscribed = existing?.isSubscribed ?: false,
            drawChanceRemaining = existing?.drawChanceRemaining ?: 1
        )
        userProfileDao.insertOrUpdateProfile(profile)
    }

    suspend fun insertTask(task: VideoTask): Long {
        return videoTaskDao.insertTask(task)
    }

    suspend fun updateTask(task: VideoTask) {
        videoTaskDao.updateTask(task)
    }

    suspend fun reportTaskAbuse(id: Int) {
        val task = videoTaskDao.getTaskById(id)
        if (task != null) {
            videoTaskDao.updateTask(task.copy(reported = true))
        }
    }

    suspend fun updateCredits(credits: Int) {
        userProfileDao.updateCredits(credits)
    }

    suspend fun setSubscription(isSubscribed: Boolean) {
        userProfileDao.updateSubscribed(isSubscribed)
        if (isSubscribed) {
            // Subscription awards big balance immediately as verification reward
            val current = userProfileDao.getProfileDirect()
            val bonusCredits = (current?.credits ?: 0) + 1000
            userProfileDao.updateCredits(bonusCredits)
        }
    }

    suspend fun updateDrawChance(count: Int) {
        userProfileDao.updateDrawChance(count)
    }

    suspend fun clearHistory() {
        videoTaskDao.clearAllTasks()
    }

    suspend fun fetchAppModels(baseUrl: String): List<com.example.data.api.AppModel>? {
        return try {
            val service = com.example.data.api.ApiService.create(baseUrl)
            val catResponse = service.getAppModelList(System.currentTimeMillis())
            if (catResponse.code == 200) {
                val parents = catResponse.data ?: emptyList()
                val allChildren = mutableListOf<com.example.data.api.AppModel>()
                parents.forEach { parent ->
                    try {
                        val detailsResponse = service.getAppModelDetailsList(id = parent.id)
                        if (detailsResponse.code == 200) {
                            val childList = detailsResponse.data?.childList
                            if (childList != null) {
                                allChildren.addAll(childList)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                allChildren.filter { it.outputType == 2 }
                    .sortedByDescending { it.isHot == 1 }
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Trigger asynchronous multi-stage video generation simulation
    fun triggerSimulationTask(taskId: Int, scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            val task = videoTaskDao.getTaskById(taskId) ?: return@launch

            // Step 1: Uploading state
            videoTaskDao.updateTask(task.copy(status = "uploading", progress = 10))
            delay(1500)
            videoTaskDao.updateTask(task.copy(status = "uploading", progress = 35))
            delay(1500)

            // Step 2: Processing state
            videoTaskDao.updateTask(task.copy(status = "processing", progress = 55))
            delay(2000)
            videoTaskDao.updateTask(task.copy(status = "processing", progress = 85))
            delay(2000)

            // Selection of matching sample video or fallback
            val matchedKey = sampleVideos.keys.find { key -> 
                task.modelType.lowercase().contains(key.lowercase())
            }
            val urls = if (matchedKey != null) {
                sampleVideos[matchedKey] ?: emptyList()
            } else {
                if (task.modelType.lowercase().contains("wan")) {
                    sampleVideos["Wan"] ?: emptyList()
                } else if (task.modelType.lowercase().contains("kling")) {
                    sampleVideos["Kling"] ?: emptyList()
                } else if (task.modelType.lowercase().contains("minimax")) {
                    sampleVideos["Minimax"] ?: emptyList()
                } else {
                    emptyList()
                }
            }
            val finalUrls = if (urls.isEmpty()) {
                sampleVideos.values.flatten()
            } else {
                urls
            }
            val selectedUrl = finalUrls[Random.nextInt(finalUrls.size)]

            // Step 3: Success state (or failure if prompt tells it to fail)
            if (task.prompt.lowercase().contains("fail") || task.prompt.contains("失败")) {
                videoTaskDao.updateTask(task.copy(status = "failed", progress = 0))
            } else {
                videoTaskDao.updateTask(task.copy(status = "success", progress = 100, resultVideoUrl = selectedUrl))
            }
        }
    }
}
