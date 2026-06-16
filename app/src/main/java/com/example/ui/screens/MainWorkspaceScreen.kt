package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.VideoTask
import com.example.ui.components.VideoPlayerView
import com.example.ui.theme.*
import com.example.ui.viewmodel.VideoViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainWorkspaceScreen(
    viewModel: VideoViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val allTasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val selectedModel by viewModel.selectedModel.collectAsStateWithLifecycle()
    val appModels by viewModel.appModels.collectAsStateWithLifecycle()
    val isLoadingModels by viewModel.isLoadingModels.collectAsStateWithLifecycle()
    val currentEnv by viewModel.currentEnv.collectAsStateWithLifecycle()
    val prompt by viewModel.prompt.collectAsStateWithLifecycle()
    val imageUrl by viewModel.imageUrl.collectAsStateWithLifecycle()
    val isUploadingImage by viewModel.isUploadingImage.collectAsStateWithLifecycle()

    val isDrawing by viewModel.isDrawing.collectAsStateWithLifecycle()
    val drawResult by viewModel.drawResult.collectAsStateWithLifecycle()
    val rewardCredits by viewModel.pickedRewardCredits.collectAsStateWithLifecycle()

    val showBilling by viewModel.showBillingDialog.collectAsStateWithLifecycle()
    val showReportTask by viewModel.showReportDialog.collectAsStateWithLifecycle()
    val reportedVisible by viewModel.reportedStatusVisible.collectAsStateWithLifecycle()
    val toastMsg by viewModel.toastMessage.collectAsStateWithLifecycle()

    // Workspace View Tabs (0: Workshop, 1: Draw Center, 2: History Library)
    var selectedTab by remember { mutableIntStateOf(0) }

    // Toast alert listener
    LaunchedEffect(toastMsg) {
        toastMsg?.let {
            // We use simple dialog alerts or snackbars. Let's do instant snackbar-style popups inside our screen space!
            kotlinx.coroutines.delay(2500)
            viewModel.clearToast()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = SleekBackground,
                modifier = Modifier.width(320.dp)
            ) {
                // DrawerHeader with Sleek Brand styling
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = SleekPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "VidKraft AI Studio",
                                color = SleekText,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "多模型旗舰级视频生成工坊",
                            color = Color(0xFF49454F),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                HorizontalDivider(color = SleekBorder.copy(alpha = 0.5f))

                // Logged User Profile card styled as Sleek Interface
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(SleekSurfaceVariant)
                        .padding(16.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(colors = listOf(Color(0xFF6366F1), Color(0xFFC084FC))))
                                    .clickable {
                                        // Trigger quick login simulation toggle
                                        if (userProfile?.uid?.isEmpty() == true) {
                                            viewModel.loginWithGoogle("Mosioey@gmail.com", "Mosioey User")
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (userProfile?.uid?.isNotEmpty() == true) userProfile!!.displayName.take(1).uppercase() else "G",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                if (userProfile?.uid?.isNotEmpty() == true) {
                                    Text(
                                        text = userProfile!!.displayName,
                                        color = SleekText,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = userProfile!!.email,
                                        color = Color(0xFF49454F),
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                } else {
                                    Text(
                                        text = "匿名创意者",
                                        color = Color(0xFF49454F),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "点击一键接入 Google 登录",
                                        color = SleekPrimary,
                                        fontSize = 11.sp,
                                        modifier = Modifier.clickable {
                                            viewModel.loginWithGoogle("Mosioey@gmail.com", "Mosioey User")
                                        }
                                    )
                                }
                            }
                        }

                        if (userProfile?.uid?.isNotEmpty() == true) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.MonetizationOn,
                                        contentDescription = null,
                                        tint = PremiumGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (userProfile!!.isSubscribed) "专业版无限量" else "${userProfile!!.credits} 点",
                                        color = SleekText,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                if (!userProfile!!.isSubscribed) {
                                    Text(
                                        text = "开通会员",
                                        color = SleekPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .clickable {
                                                coroutineScope.launch { drawerState.close() }
                                                viewModel.showBilling()
                                            }
                                            .padding(vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = SleekBorder.copy(alpha = 0.5f))

                // Navigation Items with Sleek styling
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp)
                ) {
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.ManageAccounts, contentDescription = null, tint = SleekPrimary) },
                        label = { Text("一键模拟 Google 登录", color = SleekText) },
                        selected = false,
                        onClick = {
                            viewModel.loginWithGoogle("Mosioey@gmail.com", "Mosioey User")
                            coroutineScope.launch { drawerState.close() }
                        },
                        colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.CardMembership, contentDescription = null, tint = PremiumGold) },
                        label = { Text("订阅与计费管理 (PRO)", color = SleekText) },
                        selected = false,
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            viewModel.showBilling()
                        },
                        colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.OpenInNew, contentDescription = null, tint = Color(0xFF49454F)) },
                        label = { Text("管理 Google Play 订阅", color = SleekText) },
                        selected = false,
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            // Compliance redirect routine
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse("https://play.google.com/store/account/subscriptions?package=${context.packageName}")
                            }
                            context.startActivity(intent)
                        },
                        colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Account Deletion Section
                    if (userProfile?.uid?.isNotEmpty() == true) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ErrorRed.copy(alpha = 0.08f),
                            border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth().testTag("delete_account_button")
                        ) {
                            Column(
                                modifier = Modifier
                                    .clickable {
                                        coroutineScope.launch { drawerState.close() }
                                        viewModel.deleteAccount()
                                    }
                                    .padding(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteForever,
                                        contentDescription = null,
                                        tint = ErrorRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "删除用户账号",
                                        color = ErrorRed,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "立即清除账号信息、订阅关系及所有本地视频生成历史，数据不可逆恢复",
                                    color = Color(0xFF49454F),
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VideoCall,
                                contentDescription = null,
                                tint = SleekPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "VidKraft AI",
                                color = SleekText,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "菜单", tint = SleekText)
                        }
                    },
                    actions = {
                        // Action: Credit tag styled cleanly as Sleek
                        if (userProfile != null) {
                            Box(
                                modifier = Modifier
                                    .padding(end = 12.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (userProfile!!.isSubscribed) {
                                            Brush.horizontalGradient(listOf(PremiumGold, AmberBronze))
                                        } else {
                                            Brush.horizontalGradient(listOf(SleekSecondary, SleekSurfaceVariant))
                                        }
                                    )
                                    .clickable { viewModel.showBilling() }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("credit_counter_chip")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (userProfile!!.isSubscribed) Icons.Default.WorkspacePremium else Icons.Default.MonetizationOn,
                                        contentDescription = null,
                                        tint = if (userProfile!!.isSubscribed) Color.Black else SleekPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (userProfile!!.isSubscribed) "PRO 无限量" else "${userProfile!!.credits} 点",
                                        color = if (userProfile!!.isSubscribed) Color.Black else SleekPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = SleekBackground
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = SleekNavBar,
                    contentColor = Color(0xFF49454F),
                    modifier = Modifier.navigationBarsPadding() // Compliance notch & nav-safe constraint
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(if (selectedTab == 0) Icons.Default.Create else Icons.Outlined.Create, contentDescription = null) },
                        label = { Text(androidx.compose.ui.res.stringResource(com.example.R.string.title_creative_module)) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SleekPrimary,
                            selectedTextColor = SleekText,
                            unselectedIconColor = Color(0xFF49454F),
                            unselectedTextColor = Color(0xFF49454F),
                            indicatorColor = SleekSecondary
                        ),
                        modifier = Modifier.testTag("tab_workshop")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(if (selectedTab == 1) Icons.Default.Widgets else Icons.Outlined.Widgets, contentDescription = null) },
                        label = { Text(androidx.compose.ui.res.stringResource(com.example.R.string.title_task_system)) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SleekPrimary,
                            selectedTextColor = SleekText,
                            unselectedIconColor = Color(0xFF49454F),
                            unselectedTextColor = Color(0xFF49454F),
                            indicatorColor = SleekSecondary
                        ),
                        modifier = Modifier.testTag("tab_lucky_draw")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = {
                            BadgedBox(
                                badge = {
                                    val activeGens = allTasks.count { it.status == "uploading" || it.status == "processing" }
                                    if (activeGens > 0) {
                                        Badge(containerColor = SleekPrimary) {
                                            Text(text = activeGens.toString(), color = Color.White)
                                        }
                                    }
                                }
                            ) {
                                Icon(if (selectedTab == 2) Icons.Default.History else Icons.Outlined.History, contentDescription = null)
                            }
                        },
                        label = { Text(androidx.compose.ui.res.stringResource(com.example.R.string.title_history)) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SleekPrimary,
                            selectedTextColor = SleekText,
                            unselectedIconColor = Color(0xFF49454F),
                            unselectedTextColor = Color(0xFF49454F),
                            indicatorColor = SleekSecondary
                        ),
                        modifier = Modifier.testTag("tab_history")
                    )
                }
            },
            containerColor = SleekBackground
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(SleekBackground)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Pristine Dynamic Sleek Header bar (the Jordan avatar and credits pill from HTML theme)
                    SleekHeaderSection(
                        userProfile = userProfile,
                        onAddCredits = { viewModel.showBilling() }
                    )

                    HorizontalDivider(color = SleekBorder.copy(alpha = 0.3f), thickness = 1.dp)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        // Crossfade workspace views
                        Crossfade(targetState = selectedTab, label = "tabCrossfade") { tabIndex ->
                            when (tabIndex) {
                                0 -> WorkshopView(
                                    selectedModel = selectedModel,
                                    onModelChange = { viewModel.selectModel(it) },
                                    prompt = prompt,
                                    onPromptChange = { viewModel.updatePrompt(it) },
                                    imageUrl = imageUrl,
                                    isUploadingImage = isUploadingImage,
                                    onUploadImage = { viewModel.simulateImageUpload() },
                                    onRemoveImage = { viewModel.removeUploadedImage() },
                                    onSubmit = { viewModel.startVideoGeneration() },
                                    appModels = appModels,
                                    currentEnv = currentEnv,
                                    onToggleEnv = { viewModel.toggleEnvironment() },
                                    onRefreshModels = { viewModel.loadModels() }
                                )

                                1 -> LuckyDrawView(
                                    isDrawing = isDrawing,
                                    drawResult = drawResult,
                                    rewardCredits = rewardCredits,
                                    userProfile = userProfile,
                                    onDrawClick = { viewModel.drawDailyCard() },
                                    onBuyPromotionPack = { credits: Int, price: String -> viewModel.purchaseItem(credits, price) },
                                    onDismissResult = { viewModel.dismissDrawResult() }
                                )

                                2 -> HistoryView(
                                    tasks = allTasks,
                                    onReportAbuse = { viewModel.showReportDialog(it) },
                                    onClearHistory = { viewModel.clearHistory() }
                                )
                            }
                        }
                    }
                }

                // Interactive Overlay Snack/Toast Notify
                AnimatedVisibility(
                    visible = toastMsg != null,
                    enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp)
                ) {
                    toastMsg?.let { text ->
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = SleekPrimary,
                            tonalElevation = 8.dp,
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = CosmicWhite, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = text,
                                    color = CosmicWhite,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Abuse Report Modal Box Dialog
                if (showReportTask != null) {
                    val reasons = listOf(
                        stringResource(com.example.R.string.report_reason_1),
                        stringResource(com.example.R.string.report_reason_2),
                        stringResource(com.example.R.string.report_reason_3),
                        stringResource(com.example.R.string.report_reason_4)
                    )
                    var selectedReason by remember(reasons) { mutableStateOf(reasons.first()) }
                    var extraComments by remember { mutableStateOf("") }

                    AlertDialog(
                        onDismissRequest = { viewModel.hideReportDialog() },
                        title = {
                            Text(text = stringResource(com.example.R.string.report_dialog_title), color = CosmicWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        },
                        text = {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = stringResource(com.example.R.string.report_dialog_desc),
                                    color = CosmicGrey,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                reasons.forEach { reason ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedReason = reason }
                                            .padding(vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = selectedReason == reason,
                                            onClick = { selectedReason = reason },
                                            colors = RadioButtonDefaults.colors(selectedColor = CosmicCyan)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = reason, color = CosmicWhite, fontSize = 13.sp)
                                    }
                                }

                                OutlinedTextField(
                                    value = extraComments,
                                    onValueChange = { extraComments = it },
                                    label = { Text(stringResource(com.example.R.string.report_extra_label), color = CosmicGrey) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 12.dp),
                                    textStyle = TextStyle(color = CosmicWhite)
                                )
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = { viewModel.submitAbuseReport(selectedReason) },
                                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                                modifier = Modifier.testTag("dialog_submit_report")
                            ) {
                                Text(stringResource(com.example.R.string.report_submit))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { viewModel.hideReportDialog() }) {
                                Text(stringResource(com.example.R.string.report_cancel), color = CosmicGrey)
                            }
                        },
                        containerColor = DeepSlateSurface
                    )
                }

                // Billing and Packages Modal Dialog
                if (showBilling) {
                    AlertDialog(
                        onDismissRequest = { viewModel.hideBilling() },
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = PremiumGold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(com.example.R.string.billing_dialog_title), color = CosmicWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        },
                        text = {
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                item {
                                    Text(
                                        text = stringResource(com.example.R.string.billing_dialog_desc),
                                        color = CosmicGrey,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                }

                                // Tier 1
                                item {
                                    BillingItemCard(
                                        title = stringResource(com.example.R.string.tier1_title),
                                        price = stringResource(com.example.R.string.tier1_price),
                                        description = stringResource(com.example.R.string.tier1_desc),
                                        onPurchase = { viewModel.purchaseItem(25, "¥6.00体验积分包") },
                                        test_tag = "purchase_credits_tier_1"
                                    )
                                }

                                // Tier 2
                                item {
                                    BillingItemCard(
                                        title = stringResource(com.example.R.string.tier2_title),
                                        price = stringResource(com.example.R.string.tier2_price),
                                        description = stringResource(com.example.R.string.tier2_desc),
                                        onPurchase = { viewModel.purchaseItem(150, "¥28.00创作者积分包") },
                                        test_tag = "purchase_credits_tier_2"
                                    )
                                }

                                // Subscription Tier
                                item {
                                    BillingItemCard(
                                        title = stringResource(com.example.R.string.tier3_title),
                                        price = stringResource(com.example.R.string.tier3_price),
                                        description = stringResource(com.example.R.string.tier3_desc),
                                        isProSub = true,
                                        onPurchase = { viewModel.purchaseSubscription() },
                                        test_tag = "purchase_subscription_monthly"
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { viewModel.hideBilling() }) {
                                Text(stringResource(com.example.R.string.dialog_close), color = CosmicCyan)
                            }
                        },
                        containerColor = DeepSlateSurface
                    )
                }

                // Abuse Report confirmation feedback overlay
                AnimatedVisibility(
                    visible = reportedVisible,
                    enter = fadeIn() + scaleIn(initialScale = 0.8f),
                    exit = fadeOut() + scaleOut(targetScale = 0.8f),
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = DeepSlateSurface,
                        border = BorderStroke(2.dp, ErrorRed),
                        modifier = Modifier
                            .width(280.dp)
                            .padding(16.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = ErrorRed,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = stringResource(com.example.R.string.report_success_title),
                                color = CosmicWhite,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(com.example.R.string.report_success_desc),
                                color = CosmicGrey,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SleekHeaderSection(
    userProfile: com.example.data.UserProfile?,
    onAddCredits: () -> Unit
) {
    val displayName = if (userProfile?.uid?.isNotEmpty() == true) userProfile.displayName else "Jordan"
    val creditsText = if (userProfile?.isSubscribed == true) "PRO Sub" else "${userProfile?.credits ?: 420} Credits"
    val initials = if (displayName.isNotEmpty()) displayName.take(1).uppercase() else "J"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SleekBackground)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left side: User Avatar + Welcome texts
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(colors = listOf(Color(0xFF6366F1), Color(0xFFC084FC)))) // Indigo to Purple
                    .border(2.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Column {
                Text(
                    text = "Welcome back,",
                    color = Color(0xFF49454F),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = displayName,
                    color = SleekText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Right side: interactive pill shape
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(SleekSecondary) // Soft lavender background
                .clickable { onAddCredits() }
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = creditsText.uppercase(),
                color = SleekOnSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(SleekOnSecondary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// Creative workshop tab view implementation
@Composable
fun WorkshopView(
    selectedModel: String,
    onModelChange: (String) -> Unit,
    prompt: String,
    onPromptChange: (String) -> Unit,
    imageUrl: String?,
    isUploadingImage: Boolean,
    onUploadImage: () -> Unit,
    onRemoveImage: () -> Unit,
    onSubmit: () -> Unit,
    appModels: List<com.example.data.api.AppModel>,
    currentEnv: String,
    onToggleEnv: () -> Unit,
    onRefreshModels: () -> Unit
) {
    var dropdownExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SleekBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Upper Intro section
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(com.example.R.string.one_stop_title),
                    color = SleekText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = stringResource(com.example.R.string.one_stop_desc),
                    color = Color(0xFF49454F),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Section: Model Choice Dropdown selector (beautifully styled with selection state)
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(com.example.R.string.step1_title),
                        color = SleekText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Compact environment switcher badge with pulse-ring visual
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (currentEnv == "Online") SleekPrimary.copy(alpha = 0.12f) else Color(0xFFE2F0D9))
                            .clickable { onToggleEnv() }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (currentEnv == "Online") SleekPrimary else Color(0xFF385723))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (currentEnv == "Online") "线上: api-a.frai.live" else "测试: api.tacpay.cn",
                                color = if (currentEnv == "Online") SleekPrimary else Color(0xFF385723),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val arrowRotation by animateFloatAsState(
                        targetValue = if (dropdownExpanded) 180f else 0f,
                        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                        label = "arrowRotation"
                    )

                    val activeModelObj = appModels.find { it.model == selectedModel }
                    val activeLabel = activeModelObj?.name ?: selectedModel
                    val activeDesc = activeModelObj?.desc ?: "高性能生成式视频模型"

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SleekSurface)
                            .border(BorderStroke(1.dp, SleekBorder), RoundedCornerShape(16.dp))
                            .clickable { dropdownExpanded = !dropdownExpanded }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .testTag("model_selector_trigger"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SleekSecondary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = null,
                                tint = SleekPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = activeLabel,
                                color = SleekText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = activeDesc,
                                color = Color(0xFF49454F).copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "展开选择",
                            tint = SleekText,
                            modifier = Modifier
                                .rotate(arrowRotation)
                                .size(24.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SleekSurface)
                            .border(BorderStroke(1.dp, SleekBorder.copy(alpha = 0.5f)), RoundedCornerShape(12.dp))
                            .padding(vertical = 4.dp)
                    ) {
                        if (appModels.isEmpty()) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "无可用网络模型 (点击刷新)",
                                        color = SleekText,
                                        fontSize = 14.sp
                                    )
                                },
                                onClick = {
                                    onRefreshModels()
                                    dropdownExpanded = false
                                }
                            )
                        } else {
                            appModels.forEach { appModel ->
                                val active = appModel.model == selectedModel
                                val isHot = appModel.isHot == 1
                                val points = appModel.points ?: "1.00"

                                DropdownMenuItem(
                                    text = {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = appModel.name,
                                                        color = if (active) SleekPrimary else SleekText,
                                                        fontSize = 14.sp,
                                                        fontWeight = if (active) FontWeight.Bold else FontWeight.Medium
                                                    )
                                                    if (isHot) {
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(4.dp))
                                                                .background(Color(0xFFFF4D4F))
                                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                                        ) {
                                                            Text(
                                                                text = "HOT",
                                                                color = Color.White,
                                                                fontSize = 8.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                    }
                                                }
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = "${points}积分",
                                                        color = SleekPrimary.copy(alpha = 0.7f),
                                                        fontSize = 11.sp,
                                                        modifier = Modifier.padding(end = 6.dp)
                                                    )
                                                    if (active) {
                                                        Icon(
                                                            imageVector = Icons.Default.Check,
                                                            contentDescription = "已选择",
                                                            tint = SleekPrimary,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }
                                            }
                                            val modelDesc = appModel.desc ?: ""
                                            if (modelDesc.isNotEmpty()) {
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = modelDesc,
                                                    color = Color(0xFF49454F).copy(alpha = 0.7f),
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        onModelChange(appModel.model)
                                        dropdownExpanded = false
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("model_selector_${appModel.model}")
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section: Image upload area
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(com.example.R.string.step2_title),
                        color = SleekText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (imageUrl != null) {
                        Text(
                            text = stringResource(com.example.R.string.remove_image),
                            color = ErrorRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { onRemoveImage() }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(SleekSurfaceVariant)
                        .border(BorderStroke(1.dp, SleekBorder), RoundedCornerShape(24.dp))
                        .clickable(enabled = imageUrl == null && !isUploadingImage) { onUploadImage() }
                        .testTag("image_upload_canvas")
                ) {
                    if (isUploadingImage) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(color = SleekPrimary, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(stringResource(com.example.R.string.upload_progress), color = Color(0xFF49454F), fontSize = 11.sp)
                        }
                    } else if (imageUrl != null) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = "上传的参考首帧",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.2f))
                            )
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(12.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.Black.copy(alpha = 0.6f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CloudDone, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(com.example.R.string.upload_ready), color = Color.White, fontSize = 10.sp)
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = SleekPrimary, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(stringResource(com.example.R.string.upload_placeholder_title), color = SleekText, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Text(stringResource(com.example.R.string.upload_placeholder_desc), color = Color(0xFF49454F).copy(alpha = 0.7f), fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        // Section: Prompt details
        item {
            Column {
                Text(
                    text = stringResource(com.example.R.string.step3_title),
                    color = SleekText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = prompt,
                    onValueChange = onPromptChange,
                    placeholder = {
                        Text(
                            text = stringResource(com.example.R.string.prompt_placeholder),
                            color = Color(0xFF49454F).copy(alpha = 0.5f),
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .testTag("prompt_input_field"),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SleekSurface,
                        unfocusedContainerColor = SleekSurface,
                        focusedBorderColor = SleekPrimary,
                        unfocusedBorderColor = SleekBorder,
                        focusedLabelColor = SleekPrimary
                    ),
                    textStyle = TextStyle(color = SleekText, fontSize = 13.sp)
                )

                // Quick Prompt templates
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val suggestions = listOf(
                        stringResource(com.example.R.string.suggestion_1),
                        stringResource(com.example.R.string.suggestion_2),
                        stringResource(com.example.R.string.suggestion_3)
                    )
                    suggestions.forEach { label ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SleekSurfaceVariant)
                                .clickable { onPromptChange(label) }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(text = label, color = SleekPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Section: Launch Task button
        item {
            Button(
                onClick = onSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("launch_generation_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SleekPrimary,
                    contentColor = Color.White
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.PlayForWork, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(com.example.R.string.start_generation),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = stringResource(com.example.R.string.cost_credits, 5),
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}

// Task Daily Card system lottery logic
@Composable
fun LuckyDrawView(
    isDrawing: Boolean,
    drawResult: String?,
    rewardCredits: Int,
    userProfile: com.example.data.UserProfile?,
    onDrawClick: () -> Unit,
    onBuyPromotionPack: (Int, String) -> Unit,
    onDismissResult: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    // Flip card variables
    var card1Flipped by remember { mutableStateOf(false) }
    var card2Flipped by remember { mutableStateOf(false) }
    var card3Flipped by remember { mutableStateOf(false) }

    // Synchronize flips based on drawing state
    LaunchedEffect(isDrawing) {
        if (isDrawing) {
            card1Flipped = false
            card2Flipped = false
            card3Flipped = false
        }
    }

    // Dynamic rotation angles
    val rotationCard1 by animateFloatAsState(targetValue = if (card1Flipped) 180f else 0f, animationSpec = tween(700, easing = LinearOutSlowInEasing))
    val rotationCard2 by animateFloatAsState(targetValue = if (card2Flipped) 180f else 0f, animationSpec = tween(700, easing = LinearOutSlowInEasing))
    val rotationCard3 by animateFloatAsState(targetValue = if (card3Flipped) 180f else 0f, animationSpec = tween(700, easing = LinearOutSlowInEasing))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SleekBackground)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Daily Draw Intro Header
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(com.example.R.string.draw_title),
                color = SleekText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = stringResource(com.example.R.string.draw_desc),
                color = Color(0xFF49454F),
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 4.dp),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Cards Matrix Rows layout (Flipping and interactive graphicsLayer)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Card 1
            LotteryFlipCard(
                angle = rotationCard1,
                labelNumber = "1",
                isFlipped = card1Flipped,
                onFlip = {
                    if (!isDrawing && !card1Flipped && userProfile?.drawChanceRemaining != 0) {
                        card1Flipped = true
                        onDrawClick()
                    }
                },
                test_tag = "lucky_draw_card_1"
            )

            // Card 2
            LotteryFlipCard(
                angle = rotationCard2,
                labelNumber = "2",
                isFlipped = card2Flipped,
                onFlip = {
                    if (!isDrawing && !card2Flipped && userProfile?.drawChanceRemaining != 0) {
                        card2Flipped = true
                        onDrawClick()
                    }
                },
                test_tag = "lucky_draw_card_2"
            )

            // Card 3
            LotteryFlipCard(
                angle = rotationCard3,
                labelNumber = "3",
                isFlipped = card3Flipped,
                onFlip = {
                    if (!isDrawing && !card3Flipped && userProfile?.drawChanceRemaining != 0) {
                        card3Flipped = true
                        onDrawClick()
                    }
                },
                test_tag = "lucky_draw_card_3"
            )
        }

        // Display drawing action guidelines
        if (userProfile != null && userProfile.drawChanceRemaining <= 0 && drawResult == null) {
            Surface(
                color = SleekSurfaceVariant,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, SleekBorder),
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.HourglassBottom, contentDescription = null, tint = SleekPrimary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(stringResource(com.example.R.string.draw_chance_exhausted_title), color = SleekText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(stringResource(com.example.R.string.draw_chance_exhausted_desc), color = Color(0xFF49454F), fontSize = 10.sp)
                    }
                }
            }
        } else {
            if (drawResult == null) {
                Text(
                    text = stringResource(com.example.R.string.draw_guide_tip),
                    color = SleekPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Trigger and display Result Promotion Bundle Dialog
        AnimatedVisibility(
            visible = drawResult != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            drawResult?.let { result ->
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = SleekSurface,
                    border = BorderStroke(1.dp, SleekBorder),
                    tonalElevation = 6.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .testTag("drawing_reward_banner")
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CardGiftcard,
                            contentDescription = null,
                            tint = SleekPrimary,
                            modifier = Modifier.size(52.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(com.example.R.string.reward_dialog_title),
                            color = Color(0xFF49454F),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = result,
                            color = SleekText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )

                        Text(
                            text = if (rewardCredits == 15) stringResource(com.example.R.string.reward_credit_desc_free)
                            else stringResource(com.example.R.string.reward_credit_desc_paid),
                            color = Color(0xFF49454F),
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = onDismissResult,
                                modifier = Modifier.weight(1f),
                                border = BorderStroke(1.dp, SleekBorder),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(stringResource(com.example.R.string.give_up), color = Color(0xFF49454F), fontSize = 12.sp)
                            }

                            if (rewardCredits != 15) {
                                Button(
                                    onClick = {
                                        // Trigger promotional Google billing simulation
                                        onBuyPromotionPack(rewardCredits, "¥9.90抽卡特惠包")
                                    },
                                    modifier = Modifier
                                        .weight(1.5f)
                                        .testTag("buy_draw_promotion_button"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary, contentColor = Color.White)
                                ) {
                                    Text(stringResource(com.example.R.string.double_claim_text), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 3D Matrix flip lottery card item
@Composable
fun LotteryFlipCard(
    angle: Float,
    labelNumber: String,
    isFlipped: Boolean,
    onFlip: () -> Unit,
    test_tag: String
) {
    Box(
        modifier = Modifier
            .size(90.dp, 140.dp)
            .graphicsLayer {
                rotationY = angle
                cameraDistance = 8 * density
            }
            .clip(RoundedCornerShape(16.dp))
            .clickable { onFlip() }
            .testTag(test_tag),
        contentAlignment = Alignment.Center
    ) {
        if (angle <= 90f) {
            // Front Cover Card Side with Sleek purple-indigo gradients
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(SleekPrimary, SleekSecondary)
                        )
                    )
                    .border(2.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(28.dp)
                            .rotate(15f)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = stringResource(com.example.R.string.game_card_label, labelNumber),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            // Back/Flipped Revealing layout (glowing amber/gold warm tone in light mode)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationY = 180f }
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFFFFF7E6), Color(0xFFFFECC8))
                        )
                    )
                    .border(2.dp, AmberBronze, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = null,
                        tint = AmberBronze,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(com.example.R.string.reward_congrats),
                        color = AmberBronze,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = stringResource(com.example.R.string.reward_special_card),
                        color = SleekText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// History viewer library view
@Composable
fun HistoryView(
    tasks: List<VideoTask>,
    onReportAbuse: (VideoTask) -> Unit,
    onClearHistory: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SleekBackground)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = stringResource(com.example.R.string.history_title),
                    color = SleekText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = stringResource(com.example.R.string.history_desc),
                    color = Color(0xFF49454F),
                    fontSize = 11.sp
                )
            }

            if (tasks.isNotEmpty()) {
                TextButton(onClick = onClearHistory) {
                    Text(stringResource(com.example.R.string.clear_history), color = ErrorRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (tasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.HourglassEmpty,
                        contentDescription = null,
                        tint = Color(0xFF49454F).copy(alpha = 0.5f),
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(com.example.R.string.history_empty_title),
                        color = SleekText.copy(alpha = 0.6f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(com.example.R.string.history_empty_desc),
                        color = Color(0xFF49454F),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(tasks) { task ->
                    HistoryTaskCard(
                        task = task,
                        onReportAbuseClicked = { onReportAbuse(task) }
                    )
                }
            }
        }
    }
}

// History Task Pipeline display item
@Composable
fun HistoryTaskCard(
    task: VideoTask,
    onReportAbuseClicked: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = SleekSurface,
        border = BorderStroke(
            1.dp,
            if (task.status == "processing") SleekPrimary else SleekBorder
        ),
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Task status upper labels row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SleekSurfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (task.modelType == "Minimax") "Minimax V2" else if (task.modelType == "Wan") "Wan 2.1" else "Kling AI",
                            color = SleekPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = formatTime(task.timestamp),
                        color = Color(0xFF49454F),
                        fontSize = 10.sp
                    )
                }

                // Active Pipeline status display
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            when (task.status) {
                                "success" -> SuccessGreen.copy(alpha = 0.15f)
                                "failed" -> ErrorRed.copy(alpha = 0.15f)
                                else -> SleekPrimary.copy(alpha = 0.15f)
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = when (task.status) {
                            "success" -> stringResource(com.example.R.string.status_success)
                            "failed" -> stringResource(com.example.R.string.status_failed)
                            "uploading" -> stringResource(com.example.R.string.status_uploading)
                            "processing" -> stringResource(com.example.R.string.status_processing)
                            else -> stringResource(com.example.R.string.status_queued)
                        },
                        color = when (task.status) {
                            "success" -> SuccessGreen
                            "failed" -> ErrorRed
                            else -> SleekPrimary
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Task prompt information
            Text(
                text = task.prompt,
                color = SleekText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Layout branch selection by status
            when (task.status) {
                "success" -> {
                    // Feed video player inside item
                    VideoPlayerView(
                        task = task,
                        onReportClick = onReportAbuseClicked
                    )
                }

                "failed" -> {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = ErrorRed.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = ErrorRed)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(stringResource(com.example.R.string.security_blocked_title), color = SleekText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(stringResource(com.example.R.string.security_blocked_desc), color = Color(0xFF49454F), fontSize = 10.sp)
                            }
                        }
                    }
                }

                else -> {
                    // Processing / uploading status loader sliders bar
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (task.status == "uploading") stringResource(com.example.R.string.progress_uploading) else stringResource(com.example.R.string.progress_rendering),
                                color = Color(0xFF49454F),
                                fontSize = 11.sp
                            )
                            Text(
                                text = "${task.progress}%",
                                color = SleekPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { task.progress / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(CircleShape),
                            color = SleekPrimary,
                            trackColor = SleekSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BillingItemCard(
    title: String,
    price: String,
    description: String,
    isProSub: Boolean = false,
    onPurchase: () -> Unit,
    test_tag: String
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = if (isProSub) SleekSecondary.copy(alpha = 0.25f) else SleekSurface,
        border = BorderStroke(1.dp, if (isProSub) SleekPrimary else SleekBorder),
        tonalElevation = if (isProSub) 4.dp else 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPurchase() }
            .testTag(test_tag)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = SleekText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = price,
                    color = SleekPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = description,
                color = Color(0xFF49454F),
                fontSize = 11.sp,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onPurchase,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SleekPrimary,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                Text(
                    text = if (isProSub) stringResource(com.example.R.string.buy_pro_desc) else stringResource(com.example.R.string.buy_secure),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun formatTime(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    val sdf = java.text.SimpleDateFormat("MM-dd HH:mm:ss", java.util.Locale.getDefault())
    return sdf.format(date)
}
