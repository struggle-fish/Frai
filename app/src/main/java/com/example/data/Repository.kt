package com.example.data

import android.content.Context
import android.net.Uri
import com.example.data.api.ApiConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlin.random.Random
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

class VideoRepository(
    private val videoTaskDao: VideoTaskDao,
    private val userProfileDao: UserProfileDao,
    private val apiConfig: ApiConfig = ApiConfig()
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

    fun resolveBaseUrl(isOnline: Boolean): String = apiConfig.baseUrl(isOnline)

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

    /** 与 PC uploadSceneImageFile 一致：VITE_NEW_VIDEO=1 时走 Fileuploads，否则走 ImageUpload/upload */
    suspend fun uploadSceneImage(
        context: Context,
        baseUrl: String,
        uri: Uri,
        resolution: String = "480"
    ): String? {
        return try {
            val service = com.example.data.api.ApiService.create(baseUrl, forUpload = true)
            val part = uriToMultipartPart(context, uri, "file")
            val response = if (apiConfig.useNewVideoUpload) {
                val typeBody = "1".toRequestBody("text/plain".toMediaTypeOrNull())
                service.fileUploads(part, typeBody)
            } else {
                val resValue = if (resolution.endsWith("p")) resolution else "${resolution}p"
                val resBody = resValue.toRequestBody("text/plain".toMediaTypeOrNull())
                service.uploadSceneImage(part, resBody)
            }
            if (response.code == 200) {
                response.data?.fileUrl?.trim()?.takeIf { it.isNotEmpty() }
                    ?: response.data?.url?.trim()?.takeIf { it.isNotEmpty() }
            } else {
                android.util.Log.e("VideoRepository", "uploadSceneImage failed: code=${response.code}, msg=${response.msg}")
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /** 与 PC fileuploads2 一致：上传参考视频/音频 */
    suspend fun uploadReferenceMedia(
        context: Context,
        baseUrl: String,
        uri: Uri,
        uploadType: Int = 1
    ): String? {
        return try {
            val service = com.example.data.api.ApiService.create(baseUrl, forUpload = true)
            val part = uriToMultipartPart(context, uri, "file")
            val typeBody = uploadType.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val response = service.fileUploads(part, typeBody)
            if (response.code == 200) {
                response.data?.fileUrl?.trim()?.takeIf { it.isNotEmpty() }
            } else {
                android.util.Log.e("VideoRepository", "uploadReferenceMedia failed: code=${response.code}, msg=${response.msg}")
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun uriToMultipartPart(context: Context, uri: Uri, partName: String): MultipartBody.Part {
        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"
        val extension = mimeType.substringAfter('/', "bin")
        val tempFile = File.createTempFile("upload_", ".$extension", context.cacheDir)
        contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(tempFile).use { output -> input.copyTo(output) }
        } ?: throw IllegalStateException("Cannot open uri: $uri")
        val requestBody = tempFile.asRequestBody(mimeType.toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(partName, tempFile.name, requestBody)
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
