package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoTaskDao {
    @Query("SELECT * FROM video_tasks ORDER BY timestamp DESC")
    fun getAllTasks(): Flow<List<VideoTask>>

    @Query("SELECT * FROM video_tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: Int): VideoTask?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: VideoTask): Long

    @Update
    suspend fun updateTask(task: VideoTask)

    @Delete
    suspend fun deleteTask(task: VideoTask)

    @Query("DELETE FROM video_tasks")
    suspend fun clearAllTasks()
}

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getProfileDirect(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfile)

    @Query("UPDATE user_profile SET credits = :credits WHERE id = 1")
    suspend fun updateCredits(credits: Int)

    @Query("UPDATE user_profile SET isSubscribed = :isSubscribed WHERE id = 1")
    suspend fun updateSubscribed(isSubscribed: Boolean)

    @Query("UPDATE user_profile SET drawChanceRemaining = :count WHERE id = 1")
    suspend fun updateDrawChance(count: Int)
}
