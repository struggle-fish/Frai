package com.example.data.api

/**
 * 与 PC 端 VITE_API_URL / VITE_URL_API / VITE_NEW_VIDEO 对应。
 * frai.live 线上、测试环境均开启新版上传（VITE_NEW_VIDEO = 1）。
 */
data class ApiConfig(
    val onlineBaseUrl: String = "https://api-a.frai.live",
    val testBaseUrl: String = "https://api.tacpay.cn",
    /** 1 → uploadSceneImageFile 走 /api/Fileuploads/upload；0 → /api/ImageUpload/upload */
    val useNewVideoUpload: Boolean = true
) {
    fun baseUrl(isOnline: Boolean): String = if (isOnline) onlineBaseUrl else testBaseUrl
}
