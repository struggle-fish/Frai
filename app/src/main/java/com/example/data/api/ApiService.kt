package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class ModelResponse(
    @Json(name = "code") val code: Int,
    @Json(name = "msg") val msg: String,
    @Json(name = "data") val data: ModelData?
)

@JsonClass(generateAdapter = true)
data class ModelData(
    @Json(name = "content") val content: String?,
    @Json(name = "child_list") val childList: List<AppModel>?
)

@JsonClass(generateAdapter = true)
data class AppModel(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "image") val image: String?,
    @Json(name = "desc") val desc: String?,
    @Json(name = "content") val content: String?,
    @Json(name = "status") val status: Int,
    @Json(name = "parent_id") val parentId: Int?,
    @Json(name = "model") val model: String,
    @Json(name = "model_type") val modelType: String?,
    @Json(name = "points") val points: String?,
    @Json(name = "is_discount") val isDiscount: Int?,
    @Json(name = "is_hot") val isHot: Int?,
    @Json(name = "output_type") val outputType: Int?
)

@JsonClass(generateAdapter = true)
data class CategoryResponse(
    @Json(name = "code") val code: Int,
    @Json(name = "msg") val msg: String,
    @Json(name = "data") val data: List<AppModel>?
)

@JsonClass(generateAdapter = true)
data class UploadResponse(
    @Json(name = "code") val code: Int,
    @Json(name = "msg") val msg: String?,
    @Json(name = "data") val data: UploadData?
)

@JsonClass(generateAdapter = true)
data class UploadData(
    @Json(name = "file_url") val fileUrl: String?,
    @Json(name = "url") val url: String?
)

interface ApiService {

    @GET("api/index/getAppModelList")
    suspend fun getAppModelList(
        @retrofit2.http.Query("_t") timestamp: Long? = null
    ): CategoryResponse

    @GET("api/index/getAppModelDetailsList")
    suspend fun getAppModelDetailsList(
        @retrofit2.http.Query("id") id: Int? = 1,
        @retrofit2.http.Query("parent_id") parentId: Int? = null,
        @retrofit2.http.Query("type") type: String? = null
    ): ModelResponse

    /** 与 PC uploadSceneImageFile 一致：上传参考图并返回 file_url */
    @Multipart
    @POST("api/ImageUpload/upload")
    suspend fun uploadSceneImage(
        @Part file: MultipartBody.Part,
        @Part("res") resolution: RequestBody
    ): UploadResponse

    /** 与 PC fileuploads2 一致：上传参考视频/音频 */
    @Multipart
    @POST("api/Fileuploads/upload")
    suspend fun fileUploads(
        @Part file: MultipartBody.Part,
        @Part("upload_type") uploadType: RequestBody
    ): UploadResponse

    companion object {
        private const val DEFAULT_TIMEOUT_SEC = 15L
        private const val UPLOAD_TIMEOUT_SEC = 120L

        fun create(baseUrl: String, forUpload: Boolean = false): ApiService {
            val timeout = if (forUpload) UPLOAD_TIMEOUT_SEC else DEFAULT_TIMEOUT_SEC
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(timeout, TimeUnit.SECONDS)
                .readTimeout(timeout, TimeUnit.SECONDS)
                .writeTimeout(timeout, TimeUnit.SECONDS)
                .build()

            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()

            // Ensure the baseUrl ends with a slash /
            val url = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"

            return Retrofit.Builder()
                .baseUrl(url)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(ApiService::class.java)
        }
    }
}
