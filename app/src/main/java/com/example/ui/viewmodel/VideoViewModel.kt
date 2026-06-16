package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.VideoRepository
import com.example.data.VideoTask
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlin.random.Random

class VideoViewModel(
    application: Application,
    private val repository: VideoRepository
) : AndroidViewModel(application) {

    val userProfile: StateFlow<com.example.data.UserProfile?> = repository.userProfile
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val allTasks: StateFlow<List<VideoTask>> = repository.allTasks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedModel = MutableStateFlow("Minimax")
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    private val _appModels = MutableStateFlow<List<com.example.data.api.AppModel>>(emptyList())
    val appModels: StateFlow<List<com.example.data.api.AppModel>> = _appModels.asStateFlow()

    private val _isLoadingModels = MutableStateFlow(false)
    val isLoadingModels: StateFlow<Boolean> = _isLoadingModels.asStateFlow()

    private val _currentEnv = MutableStateFlow("Online") // "Online" or "Test"
    val currentEnv: StateFlow<String> = _currentEnv.asStateFlow()

    private val _prompt = MutableStateFlow("")
    val prompt: StateFlow<String> = _prompt.asStateFlow()

    private val _imageUrl = MutableStateFlow<String?>(null)
    val imageUrl: StateFlow<String?> = _imageUrl.asStateFlow()

    private val _isUploadingImage = MutableStateFlow(false)
    val isUploadingImage: StateFlow<Boolean> = _isUploadingImage.asStateFlow()

    // Task Draw (lottery mechanics)
    private val _isDrawing = MutableStateFlow(false)
    val isDrawing: StateFlow<Boolean> = _isDrawing.asStateFlow()

    private val _drawResult = MutableStateFlow<String?>(null)
    val drawResult: StateFlow<String?> = _drawResult.asStateFlow()

    private val _pickedRewardCredits = MutableStateFlow(0)
    val pickedRewardCredits: StateFlow<Int> = _pickedRewardCredits.asStateFlow()

    // Transaction & Report Dialog States
    private val _showBillingDialog = MutableStateFlow(false)
    val showBillingDialog: StateFlow<Boolean> = _showBillingDialog.asStateFlow()

    private val _showReportDialog = MutableStateFlow<VideoTask?>(null)
    val showReportDialog: StateFlow<VideoTask?> = _showReportDialog.asStateFlow()

    private val _reportedStatusVisible = MutableStateFlow(false)
    val reportedStatusVisible: StateFlow<Boolean> = _reportedStatusVisible.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initializeDefaultUser()
        }
        loadModels()
    }

    fun loadModels() {
        viewModelScope.launch {
            _isLoadingModels.value = true
            try {
                val baseUrl = if (_currentEnv.value == "Online") "https://api-a.frai.live" else "https://api.tacpay.cn"
                val response = repository.fetchAppModels(baseUrl)
                if (response != null && response.code == 200) {
                    val list = response.data?.childList ?: emptyList()
                    _appModels.value = list
                    if (list.isNotEmpty()) {
                        // If selected model is not in the list, set to the first model
                        if (!list.any { it.model == _selectedModel.value }) {
                            _selectedModel.value = list.first().model
                        }
                    } else {
                        loadFallbackModels()
                    }
                } else {
                    showToast("获取模型列表失败，已启用本地预置模型")
                    loadFallbackModels()
                }
            } catch (e: Exception) {
                showToast("网络连接异常，已启用本地预置模型")
                loadFallbackModels()
                e.printStackTrace()
            } finally {
                _isLoadingModels.value = false
            }
        }
    }

    private fun loadFallbackModels() {
        _appModels.value = listOf(
            com.example.data.api.AppModel(
                id = 1001,
                name = "Minimax V2",
                image = null,
                desc = "极速文本级语义理解与多模态生成",
                content = "",
                status = 1,
                parentId = null,
                model = "Minimax",
                modelType = "minimax",
                points = "1.00",
                isDiscount = 0,
                isHot = 0,
                outputType = 2
            ),
            com.example.data.api.AppModel(
                id = 1002,
                name = "Wan 2.1",
                image = null,
                desc = "旗舰级超写实微缩模型与逼真画质",
                content = "",
                status = 1,
                parentId = null,
                model = "Wan",
                modelType = "wan",
                points = "1.00",
                isDiscount = 0,
                isHot = 1,
                outputType = 2
            ),
            com.example.data.api.AppModel(
                id = 1003,
                name = "Kling AI",
                image = null,
                desc = "高精度运镜控制与专业级动态构图",
                content = "",
                status = 1,
                parentId = null,
                model = "Kling",
                modelType = "kling",
                points = "1.00",
                isDiscount = 0,
                isHot = 0,
                outputType = 2
            )
        )
    }

    fun toggleEnvironment() {
        val newEnv = if (_currentEnv.value == "Online") "Test" else "Online"
        _currentEnv.value = newEnv
        showToast("环境切至 ${if (newEnv == "Online") "线上环境: api-a.frai.live" else "测试环境: api.tacpay.cn"}")
        loadModels()
    }

    fun selectModel(model: String) {
        _selectedModel.value = model
    }

    fun updatePrompt(text: String) {
        _prompt.value = text
    }

    fun simulateImageUpload() {
        viewModelScope.launch {
            _isUploadingImage.value = true
            delay(1200) // Simulates image upload upload processing
            val sampleImageUrls = listOf(
                "https://images.unsplash.com/photo-1579783902614-a3fb3927b6a5?q=80&w=300",
                "https://images.unsplash.com/photo-1541701494587-cb58502866ab?q=80&w=300",
                "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?q=80&w=300"
            )
            _imageUrl.value = sampleImageUrls.random()
            _isUploadingImage.value = false
            showToast("图片上传成功！")
        }
    }

    fun removeUploadedImage() {
        _imageUrl.value = null
    }

    fun startVideoGeneration() {
        val currentPrompt = _prompt.value.trim()
        if (currentPrompt.isEmpty()) {
            showToast("请输入生成提示词！")
            return
        }

        viewModelScope.launch {
            val profile = repository.getProfileDirect()
            if (profile == null || profile.uid.isEmpty()) {
                showToast("请先登录账号！")
                return@launch
            }

            val requiredCredits = 5
            if (profile.credits < requiredCredits && !profile.isSubscribed) {
                showToast("积分余额不足，请前往获取！")
                _showBillingDialog.value = true
                return@launch
            }

            // Deduct credits if not unlimited subscriber
            if (!profile.isSubscribed) {
                repository.updateCredits(profile.credits - requiredCredits)
            }

            val newTask = VideoTask(
                modelType = _selectedModel.value,
                prompt = currentPrompt,
                sourceImageUri = _imageUrl.value,
                status = "idle",
                progress = 0
            )

            val taskId = repository.insertTask(newTask)
            repository.triggerSimulationTask(taskId.toInt(), viewModelScope)

            // Reset input values
            _prompt.value = ""
            _imageUrl.value = null
            showToast("任务已提交至队列，后台生成中...")
        }
    }

    // Task Card System: Flipping drawing lottery cards
    fun drawDailyCard() {
        viewModelScope.launch {
            val profile = repository.getProfileDirect()
            if (profile == null || profile.drawChanceRemaining <= 0) {
                showToast("今天已经没有抽卡机会啦，明天再来吧！")
                return@launch
            }

            _isDrawing.value = true
            _drawResult.value = null
            delay(2000) // Animating flipping delay

            repository.updateDrawChance(0)

            val roll = Random.nextInt(100)
            val resultText: String
            val creditsGained: Int
            if (roll < 35) {
                resultText = "9.9元/50次视频生成特惠包！"
                creditsGained = 250
            } else if (roll < 70) {
                resultText = "免费下发15点体验积分爆顶包！"
                creditsGained = 15
            } else {
                resultText = "终身专业版大额立减特专金卡！"
                creditsGained = 500
            }

            _drawResult.value = resultText
            _pickedRewardCredits.value = creditsGained
            _isDrawing.value = false

            // Automatically award free experience credits right away
            if (roll in 35..69) {
                val current = repository.getProfileDirect()
                current?.let {
                    repository.updateCredits(it.credits + creditsGained)
                }
                showToast("恭喜：已为您加算 $creditsGained 点积分！")
            }
        }
    }

    // Simulation of Google Play purchases
    fun purchaseItem(credits: Int, priceLabel: String) {
        viewModelScope.launch {
            val profile = repository.getProfileDirect()
            if (profile == null || profile.uid.isEmpty()) {
                showToast("购买失败：请先完成登录！")
                return@launch
            }

            showToast("正在通过 Google Play 结算 $priceLabel...")
            delay(1500) // simulated Google play payment sheet delay

            // Purchase success
            val current = repository.getProfileDirect()
            if (current != null) {
                repository.updateCredits(current.credits + credits)
                showToast("购买成功！已向您的账号分配 $credits 积分。")
            }
            _showBillingDialog.value = false
            dismissDrawResult()
        }
    }

    fun purchaseSubscription() {
        viewModelScope.launch {
            val profile = repository.getProfileDirect()
            if (profile == null || profile.uid.isEmpty()) {
                showToast("订阅失败：请先登录！")
                return@launch
            }

            showToast("正在通过 Google Play 验证订阅资格...")
            delay(1800)

            repository.setSubscription(true)
            showToast("订阅成功！您已升级为无限生成专业会员，并赠送1000测试金币！")
            _showBillingDialog.value = false
        }
    }

    fun dismissDrawResult() {
        _drawResult.value = null
        _pickedRewardCredits.value = 0
    }

    fun showBilling() {
        _showBillingDialog.value = true
    }

    fun hideBilling() {
        _showBillingDialog.value = false
    }

    fun showReportDialog(task: VideoTask) {
        _showReportDialog.value = task
    }

    fun hideReportDialog() {
        _showReportDialog.value = null
    }

    fun submitAbuseReport(reasonStr: String) {
        val task = _showReportDialog.value
        if (task != null) {
            viewModelScope.launch {
                repository.reportTaskAbuse(task.id)
                hideReportDialog()
                _reportedStatusVisible.value = true
                delay(2000)
                _reportedStatusVisible.value = false
                showToast("举报信息已发回，我们的安全审核人员将即刻介入审核。感谢支持！")
            }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            repository.logoutUser()
            showToast("账号及云端生成历史已彻底删除清除。")
            _imageUrl.value = null
            _prompt.value = ""
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            showToast("本地历史已清空。")
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logoutUser()
            showToast("已成功登出。")
        }
    }

    fun loginWithGoogle(email: String, name: String) {
        viewModelScope.launch {
            repository.loginWithGoogle(email, name)
            showToast("Google 授权登录成功！欢迎回来。")
        }
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    class Factory(
        private val application: Application,
        private val repository: VideoRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(VideoViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return VideoViewModel(application, repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
