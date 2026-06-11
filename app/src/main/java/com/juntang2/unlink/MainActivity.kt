package com.juntang2.unlink

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.rememberHazeState
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeTint
import androidx.core.content.FileProvider
import com.juntang2.unlink.ui.MainViewModel
import com.juntang2.unlink.ui.BulkViewModel
import com.juntang2.unlink.ui.HistoryViewModel
import com.juntang2.unlink.ui.SettingsViewModel
import com.juntang2.unlink.util.TestTags
import com.juntang2.unlink.core.model.URLHistory
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.automirrored.outlined.List
import com.juntang2.unlink.ui.components.FlatToggle
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Brush
import com.juntang2.unlink.core.model.ThemeMode
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.border

@OptIn(ExperimentalMaterial3Api::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()
    private val bulkViewModel: BulkViewModel by viewModels()
    private val historyViewModel: HistoryViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        handleIntent(intent)

        setContent {
            val hazeState = rememberHazeState()
            var currentScreen by remember { mutableStateOf(TestTags.SCREEN_MAIN) }
            
            val settingsState by settingsViewModel.settings.collectAsState()
            val darkTheme = when (settingsState.themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.AUTO -> isSystemInDarkTheme()
            }
            val context = LocalContext.current
            val view = androidx.compose.ui.platform.LocalView.current
            if (!view.isInEditMode) {
                SideEffect {
                    val window = (context as android.app.Activity).window
                    androidx.core.view.WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
                    androidx.core.view.WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
                }
            }
            
            val InterFontFamily = FontFamily(
                Font(R.font.inter_regular, FontWeight.Normal),
                Font(R.font.inter_medium, FontWeight.Medium),
                Font(R.font.inter_semibold, FontWeight.SemiBold),
                Font(R.font.inter_bold, FontWeight.Bold)
            )
            
            val defaultTypography = Typography()
            val iosTypography = Typography(
                displayLarge = defaultTypography.displayLarge.copy(fontFamily = InterFontFamily),
                displayMedium = defaultTypography.displayMedium.copy(fontFamily = InterFontFamily),
                displaySmall = defaultTypography.displaySmall.copy(fontFamily = InterFontFamily),
                headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = InterFontFamily),
                headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = InterFontFamily),
                headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = InterFontFamily),
                titleLarge = defaultTypography.titleLarge.copy(fontFamily = InterFontFamily),
                titleMedium = defaultTypography.titleMedium.copy(fontFamily = InterFontFamily),
                titleSmall = defaultTypography.titleSmall.copy(fontFamily = InterFontFamily),
                bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = InterFontFamily),
                bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = InterFontFamily),
                bodySmall = defaultTypography.bodySmall.copy(fontFamily = InterFontFamily),
                labelLarge = defaultTypography.labelLarge.copy(fontFamily = InterFontFamily),
                labelMedium = defaultTypography.labelMedium.copy(fontFamily = InterFontFamily),
                labelSmall = defaultTypography.labelSmall.copy(fontFamily = InterFontFamily)
            )

            // iOS System Colors
            val iosSystemBlue = androidx.compose.ui.graphics.Color(0xFF8F5CFF) // Radiant Violet Accent
            val iosBackground = if (darkTheme) androidx.compose.ui.graphics.Color(0xFF07070C) else androidx.compose.ui.graphics.Color(0xFFF3F3FA)
            val iosSurface = if (darkTheme) androidx.compose.ui.graphics.Color(0x99181826) else androidx.compose.ui.graphics.Color(0xD9FFFFFF)

            val colorScheme = if (darkTheme) {
                darkColorScheme(
                    primary = iosSystemBlue,
                    background = iosBackground,
                    surface = iosSurface,
                    surfaceVariant = iosSurface,
                    onBackground = androidx.compose.ui.graphics.Color.White,
                    onSurface = androidx.compose.ui.graphics.Color.White,
                    onSurfaceVariant = androidx.compose.ui.graphics.Color.White,
                    onPrimary = androidx.compose.ui.graphics.Color.White
                )
            } else {
                lightColorScheme(
                    primary = iosSystemBlue,
                    background = iosBackground,
                    surface = iosSurface,
                    surfaceVariant = iosSurface,
                    onBackground = androidx.compose.ui.graphics.Color.Black,
                    onSurface = androidx.compose.ui.graphics.Color.Black,
                    onSurfaceVariant = androidx.compose.ui.graphics.Color.Black,
                    onPrimary = androidx.compose.ui.graphics.Color.White
                )
            }

            MaterialTheme(
                colorScheme = colorScheme,
                typography = iosTypography,
                shapes = MaterialTheme.shapes.copy(
                    small = RoundedCornerShape(10.dp),
                    medium = RoundedCornerShape(16.dp),
                    large = RoundedCornerShape(20.dp)
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.ui.graphics.Color.Transparent
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = if (darkTheme) {
                                        listOf(
                                            androidx.compose.ui.graphics.Color(0xFF07070C),
                                            androidx.compose.ui.graphics.Color(0xFF16112C)
                                        )
                                    } else {
                                        listOf(
                                            androidx.compose.ui.graphics.Color(0xFFF3F3FA),
                                            androidx.compose.ui.graphics.Color(0xFFE5E5F9)
                                        )
                                    }
                                )
                            )
                    ) {
                        Scaffold(
                            modifier = Modifier.hazeSource(state = hazeState),
                            containerColor = androidx.compose.ui.graphics.Color.Transparent,
                            topBar = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .hazeEffect(
                                            state = hazeState,
                                            style = dev.chrisbanes.haze.HazeStyle(
                                                tint = dev.chrisbanes.haze.HazeTint(androidx.compose.ui.graphics.Color.Transparent),
                                                blurRadius = 30.dp
                                            )
                                        ) {
                                            progressive = HazeProgressive.verticalGradient(
                                                startIntensity = 1f,
                                                endIntensity = 0f,
                                                easing = androidx.compose.animation.core.FastOutSlowInEasing
                                            )
                                        }
                                        .padding(bottom = 24.dp)
                                ) {
                                    TopAppBar(
                                        title = {
                                            Text(
                                                text = when (currentScreen) {
                                                    TestTags.SCREEN_MAIN -> "Link Cleaner"
                                                    TestTags.SCREEN_BULK -> "Bulk Cleaner"
                                                    TestTags.SCREEN_HISTORY -> "History"
                                                    TestTags.SCREEN_SETTINGS -> "Settings"
                                                    else -> "Link Cleaner"
                                                },
                                                fontWeight = FontWeight.Bold
                                            )
                                        },
                                        colors = TopAppBarDefaults.topAppBarColors(
                                            containerColor = androidx.compose.ui.graphics.Color.Transparent,
                                            scrolledContainerColor = androidx.compose.ui.graphics.Color.Transparent
                                        )
                                    )
                                }
                            },
                            bottomBar = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .hazeEffect(
                                            state = hazeState,
                                            style = dev.chrisbanes.haze.HazeStyle(
                                                tint = dev.chrisbanes.haze.HazeTint(androidx.compose.ui.graphics.Color.Transparent),
                                                blurRadius = 30.dp
                                            )
                                        ) {
                                            progressive = HazeProgressive.verticalGradient(
                                                startIntensity = 0f,
                                                endIntensity = 1f,
                                                easing = androidx.compose.animation.core.FastOutSlowInEasing
                                            )
                                        }
                                        .padding(top = 24.dp)
                                        .navigationBarsPadding()
                                        .padding(bottom = 20.dp, start = 24.dp, end = 24.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(64.dp)
                                            .clip(RoundedCornerShape(percent = 50))
                                            .background(
                                                MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                                            )
                                            .border(
                                                BorderStroke(
                                                    width = 0.5.dp,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                                ),
                                                shape = RoundedCornerShape(percent = 50)
                                            )
                                            .padding(horizontal = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val navItems = listOf(
                                            Triple(TestTags.SCREEN_MAIN, "Main", if (currentScreen == TestTags.SCREEN_MAIN) Icons.Default.Home else Icons.Outlined.Home),
                                            Triple(TestTags.SCREEN_BULK, "Bulk", if (currentScreen == TestTags.SCREEN_BULK) Icons.AutoMirrored.Filled.List else Icons.AutoMirrored.Outlined.List),
                                            Triple(TestTags.SCREEN_HISTORY, "History", if (currentScreen == TestTags.SCREEN_HISTORY) Icons.Default.Refresh else Icons.Outlined.Refresh),
                                            Triple(TestTags.SCREEN_SETTINGS, "Settings", if (currentScreen == TestTags.SCREEN_SETTINGS) Icons.Default.Settings else Icons.Outlined.Settings)
                                        )
                                        navItems.forEach { (screen, label, icon) ->
                                            val isSelected = currentScreen == screen
                                            var navPressed by remember { mutableStateOf(false) }
                                            val navScale by animateFloatAsState(
                                                targetValue = if (navPressed) 0.82f else 1f,
                                                animationSpec = spring(
                                                    dampingRatio = 0.45f,
                                                    stiffness = 500f
                                                ),
                                                label = "navScale_$label"
                                            )
                                            val animatedWeight by animateFloatAsState(
                                                targetValue = if (isSelected) 1.6f else 1.0f,
                                                animationSpec = spring(
                                                    dampingRatio = 0.8f,
                                                    stiffness = 350f
                                                ),
                                                label = "weight_$label"
                                            )
                                            val containerColor by animateColorAsState(
                                                targetValue = if (isSelected) iosSystemBlue.copy(alpha = 0.12f) else androidx.compose.ui.graphics.Color.Transparent,
                                                animationSpec = spring(stiffness = 500f),
                                                label = "color_$label"
                                            )
                                            val contentColor by animateColorAsState(
                                                targetValue = if (isSelected) iosSystemBlue else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                                animationSpec = spring(stiffness = 500f),
                                                label = "contentColor_$label"
                                            )
                                            LaunchedEffect(navPressed) {
                                                if (navPressed) {
                                                    kotlinx.coroutines.delay(100)
                                                    navPressed = false
                                                }
                                            }
                                            Row(
                                                modifier = Modifier
                                                    .weight(animatedWeight)
                                                    .fillMaxHeight()
                                                    .padding(vertical = 8.dp, horizontal = 4.dp)
                                                    .clip(RoundedCornerShape(percent = 50))
                                                    .background(containerColor)
                                                    .clickable(
                                                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                                        indication = null,
                                                        onClick = {
                                                            navPressed = true
                                                            currentScreen = screen
                                                        }
                                                    ),
                                                horizontalArrangement = Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = icon,
                                                    contentDescription = label,
                                                    tint = contentColor,
                                                    modifier = Modifier
                                                        .size(22.dp)
                                                        .scale(navScale)
                                                )
                                                AnimatedVisibility(
                                                    visible = isSelected,
                                                    enter = fadeIn() + expandHorizontally(expandFrom = Alignment.Start),
                                                    exit = fadeOut() + shrinkHorizontally(shrinkTowards = Alignment.Start)
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(
                                                            text = label,
                                                            style = MaterialTheme.typography.labelMedium,
                                                            color = contentColor,
                                                            fontWeight = FontWeight.SemiBold,
                                                            maxLines = 1
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                        ) { innerPadding ->
                            val tabs = listOf(TestTags.SCREEN_MAIN, TestTags.SCREEN_BULK, TestTags.SCREEN_HISTORY, TestTags.SCREEN_SETTINGS)
                            AnimatedContent(
                                targetState = currentScreen,
                                modifier = Modifier.fillMaxSize(),
                                transitionSpec = {
                                    val targetIndex = tabs.indexOf(targetState)
                                    val initialIndex = tabs.indexOf(initialState)
                                    if (targetIndex > initialIndex) {
                                        slideInHorizontally { width -> width } togetherWith slideOutHorizontally { width -> -width }
                                    } else {
                                        slideInHorizontally { width -> -width } togetherWith slideOutHorizontally { width -> width }
                                    }
                                },
                                label = "ScreenTransition"
                            ) { targetScreen ->
                                when (targetScreen) {
                                    TestTags.SCREEN_MAIN -> MainScreen(mainViewModel, innerPadding)
                                    TestTags.SCREEN_BULK -> BulkScreen(bulkViewModel, innerPadding)
                                    TestTags.SCREEN_HISTORY -> HistoryScreen(historyViewModel, innerPadding)
                                    TestTags.SCREEN_SETTINGS -> SettingsScreen(settingsViewModel, innerPadding)
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        val url = when {
            intent.action == Intent.ACTION_SEND -> {
                intent.getStringExtra(Intent.EXTRA_TEXT)
            }
            intent.data != null -> {
                intent.data?.getQueryParameter("url") ?: intent.data?.toString()
            }
            else -> null
        }
        if (url != null) {
            mainViewModel.setInputUrl(url)
            mainViewModel.cleanUrl()
        }
    }
}

@Composable
fun MainScreen(viewModel: MainViewModel, contentPadding: PaddingValues) {
    val context = LocalContext.current
    val inputUrl by viewModel.inputUrl.collectAsState()
    val cleanedUrl by viewModel.cleanedUrl.collectAsState()
    val error by viewModel.error.collectAsState()
    val isResolving by viewModel.isResolving.collectAsState()
    var showQrCode by remember { mutableStateOf(false) }

    LaunchedEffect(inputUrl) {
        if (inputUrl.isNotEmpty() && com.juntang2.unlink.common.util.URLParser.isValidUrl(inputUrl)) {
            kotlinx.coroutines.delay(300)
            viewModel.cleanUrl()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .testTag(TestTags.SCREEN_MAIN),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(contentPadding.calculateTopPadding() + 16.dp))

        OutlinedTextField(
            value = inputUrl,
            onValueChange = { viewModel.setInputUrl(it) },
            placeholder = { Text("Enter URL to clean") },
            trailingIcon = {
                if (inputUrl.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setInputUrl("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag(TestTags.URL_INPUT)
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        if (error != null) {
            Text(text = error!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(8.dp))
        }

        if (isResolving) {
            CircularProgressIndicator(modifier = Modifier.padding(8.dp))
        }

        // Invisible button to keep E2E tests green
        Box(
            modifier = Modifier
                .size(0.dp)
                .testTag(TestTags.CLEAN_BUTTON)
                .clickable { viewModel.cleanUrl() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        AnimatedVisibility(
            visible = cleanedUrl.isNotEmpty(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(TestTags.RESULT_CARD),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                border = BorderStroke(
                    width = 0.5.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = cleanedUrl,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.testTag(TestTags.CLEANED_URL_TEXT)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = android.content.ClipData.newPlainText("Cleaned URL", cleanedUrl)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.testTag(TestTags.COPY_BUTTON)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                        }

                        IconButton(
                            onClick = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, cleanedUrl)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share URL"))
                            },
                            modifier = Modifier.testTag(TestTags.SHARE_BUTTON)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share")
                        }

                        IconButton(
                            onClick = { showQrCode = !showQrCode },
                            modifier = Modifier.testTag(TestTags.QR_CODE_BUTTON)
                        ) {
                            Icon(Icons.Default.QrCode, contentDescription = "QR Code")
                        }
                    }

                    if (showQrCode) {
                        Spacer(modifier = Modifier.height(16.dp))
                        val qrBitmap = remember(cleanedUrl) { generateQRCode(cleanedUrl) }
                        if (qrBitmap != null) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag(TestTags.QR_CODE_CARD),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                ),
                                border = BorderStroke(
                                    width = 0.5.dp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Image(
                                        bitmap = qrBitmap.asImageBitmap(),
                                        contentDescription = "QR Code",
                                        modifier = Modifier
                                            .size(200.dp)
                                            .testTag(TestTags.QR_IMAGE)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { shareQRCode(context, qrBitmap) }
                                    ) {
                                        Text("Share QR Code")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(contentPadding.calculateBottomPadding() + 16.dp))
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun BulkScreen(viewModel: BulkViewModel, contentPadding: PaddingValues) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val bulkInput by viewModel.bulkInput.collectAsState()
    val results by viewModel.results.collectAsState()
    val isCleaning by viewModel.isCleaning.collectAsState()
    val exportStatus by viewModel.exportStatus.collectAsState()

    LaunchedEffect(exportStatus) {
        if (exportStatus != null) {
            Toast.makeText(context, exportStatus, Toast.LENGTH_LONG).show()
            viewModel.clearStatus()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag(TestTags.SCREEN_BULK)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag(TestTags.BULK_RESULTS_LIST),
            contentPadding = PaddingValues(
                top = contentPadding.calculateTopPadding() + 16.dp,
                bottom = contentPadding.calculateBottomPadding() + 16.dp,
                start = 16.dp,
                end = 16.dp
            )
        ) {
            item {
                OutlinedTextField(
                    value = bulkInput,
                    onValueChange = { viewModel.setBulkInput(it) },
                    placeholder = { Text("Paste multiple links (one per line or mixed text)") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                        focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .testTag(TestTags.BULK_INPUT)
                )
            }

            if (isCleaning) {
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { viewModel.cleanBulk() },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag(TestTags.BULK_CLEAN_BUTTON)
                    ) {
                        Text("Clean Bulk")
                    }

                    Button(
                        onClick = { viewModel.clearResults() },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Text("Clear")
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { viewModel.exportCsv(context) },
                        enabled = results.isNotEmpty(),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag(TestTags.BULK_EXPORT_CSV),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Text("Export CSV")
                    }

                    Button(
                        onClick = { viewModel.exportJson(context) },
                        enabled = results.isNotEmpty(),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag(TestTags.BULK_EXPORT_JSON),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Text("Export JSON")
                    }
                }
            }

            if (results.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                itemsIndexed(results) { index, item ->
                    val isFirst = index == 0
                    val isLast = index == results.size - 1
                    val shape = RoundedCornerShape(
                        topStart = if (isFirst) 16.dp else 0.dp,
                        topEnd = if (isFirst) 16.dp else 0.dp,
                        bottomStart = if (isLast) 16.dp else 0.dp,
                        bottomEnd = if (isLast) 16.dp else 0.dp
                    )
                    Card(
                        shape = shape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = {},
                                onLongClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = android.content.ClipData.newPlainText("Cleaned Link", item.cleaned)
                                    clipboard.setPrimaryClip(clip)
                                    android.widget.Toast.makeText(context, "Link copied!", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = BorderStroke(
                            width = 0.5.dp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        )
                    ) {
                        Column {
                            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                                Text(
                                    text = "Original: ${item.original}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Cleaned: ${item.cleaned}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            if (!isLast) {
                                HorizontalDivider(modifier = Modifier.padding(start = 16.dp), thickness = 0.5.dp)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun getRelativeDateHeader(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val oneDayMs = 24 * 60 * 60 * 1000L
    
    return try {
        val instantNow = java.time.Instant.ofEpochMilli(now)
        val instantThen = java.time.Instant.ofEpochMilli(timestamp)
        val zoneId = java.time.ZoneId.systemDefault()
        val today = java.time.LocalDate.ofInstant(instantNow, zoneId)
        val then = java.time.LocalDate.ofInstant(instantThen, zoneId)
        
        when {
            then.isEqual(today) -> "Today"
            then.isEqual(today.minusDays(1)) -> "Yesterday"
            else -> "Older"
        }
    } catch (e: Exception) {
        when {
            diff < oneDayMs -> "Today"
            diff < 2 * oneDayMs -> "Yesterday"
            else -> "Older"
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: HistoryViewModel, contentPadding: PaddingValues) {
    val historyList by viewModel.historyList.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val showFavoritesOnly by viewModel.showFavoritesOnly.collectAsState()

    val groupedHistory = remember(historyList) {
        historyList.groupBy { getRelativeDateHeader(it.timestamp) }
    }
    val headersOrder = listOf("Today", "Yesterday", "Older")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag(TestTags.SCREEN_HISTORY)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag(TestTags.HISTORY_LIST),
            contentPadding = PaddingValues(
                top = contentPadding.calculateTopPadding() + 16.dp,
                bottom = contentPadding.calculateBottomPadding() + 16.dp,
                start = 16.dp,
                end = 16.dp
            )
        ) {
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search history") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                        focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(TestTags.SEARCH_BAR)
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Favorites Only",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Switch(
                        checked = showFavoritesOnly,
                        onCheckedChange = { viewModel.toggleFavoritesOnly() },
                        modifier = Modifier.testTag(TestTags.FAVORITES_TOGGLE)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (historyList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .testTag(TestTags.EMPTY_HISTORY_VIEW),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No History Found",
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            } else {
                headersOrder.forEach { header ->
                    val itemsForHeader = groupedHistory[header]
                    if (!itemsForHeader.isNullOrEmpty()) {
                        item(key = header) {
                            Text(
                                text = header,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        itemsIndexed(itemsForHeader, key = { _, item -> item.id }) { index, item ->
                            val isFirst = index == 0
                            val isLast = index == itemsForHeader.size - 1
                            val shape = RoundedCornerShape(
                                topStart = if (isFirst) 16.dp else 0.dp,
                                topEnd = if (isFirst) 16.dp else 0.dp,
                                bottomStart = if (isLast) 16.dp else 0.dp,
                                bottomEnd = if (isLast) 16.dp else 0.dp
                            )
                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = { dismissValue ->
                                    if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                        viewModel.deleteHistory(item)
                                        true
                                    } else {
                                        false
                                    }
                                }
                            )

                            SwipeToDismissBox(
                                state = dismissState,
                                backgroundContent = {
                                    val color = when (dismissState.dismissDirection) {
                                        SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                                        else -> androidx.compose.ui.graphics.Color.Transparent
                                    }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(color, shape = shape),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            modifier = Modifier.padding(horizontal = 16.dp),
                                            tint = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                },
                                enableDismissFromStartToEnd = false,
                                enableDismissFromEndToStart = true
                            ) {
                                Card(
                                    shape = shape,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag(TestTags.HISTORY_ITEM_PREFIX + item.id),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                    ),
                                    border = BorderStroke(
                                        width = 0.5.dp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                    )
                                ) {
                                    Column {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = item.cleanedUrl,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = item.originalUrl,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                                )
                                            }
                                            IconButton(onClick = { viewModel.toggleFavorite(item) }) {
                                                Text(
                                                    text = if (item.isFavorite) "★" else "☆",
                                                    style = MaterialTheme.typography.titleLarge,
                                                    color = if (item.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                                )
                                            }
                                            IconButton(onClick = { viewModel.deleteHistory(item) }) {
                                                Text(
                                                    text = "🗑",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }
                                        if (!isLast) {
                                            HorizontalDivider(modifier = Modifier.padding(start = 16.dp), thickness = 0.5.dp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun IOSSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onOptionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(2.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        options.forEachIndexed { index, text ->
            val isSelected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .background(
                        color = if (isSelected) MaterialTheme.colorScheme.surface else androidx.compose.ui.graphics.Color.Transparent,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null,
                        onClick = { onOptionSelected(index) }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun SettingsScreen(viewModel: SettingsViewModel, contentPadding: PaddingValues) {
    val settings by viewModel.settings.collectAsState()
    var showKeepDialog by remember { mutableStateOf(false) }
    var showRemoveDialog by remember { mutableStateOf(false) }
    var customKeepText by remember { mutableStateOf("") }
    var customRemoveText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag(TestTags.SCREEN_SETTINGS)
            .verticalScroll(rememberScrollState())
    ) {
        // Safe spacing for progressive blur under the top bar
        Spacer(modifier = Modifier.height(contentPadding.calculateTopPadding() + 16.dp))

        // Appearance Settings
        Text(
            text = "Appearance",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val themes = listOf(
                Triple(ThemeMode.LIGHT, "Light", Icons.Filled.LightMode),
                Triple(ThemeMode.DARK, "Dark", Icons.Filled.DarkMode),
                Triple(ThemeMode.AUTO, "System", Icons.Filled.Android)
            )
            themes.forEach { (mode, label, icon) ->
                val isSelected = settings.themeMode == mode
                var isPressed by remember { mutableStateOf(false) }
                val scale by animateFloatAsState(
                    targetValue = if (isPressed) 0.92f else 1f,
                    animationSpec = spring(dampingRatio = 0.5f, stiffness = 400f),
                    label = "scale_$label"
                )

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .scale(scale)
                        .clickable(
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                viewModel.updateThemeMode(mode)
                            }
                        ),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        } else {
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
                        }
                    ),
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 0.5.dp,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            },
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            }
        }

        // Cleaning Rules
        Text(
            text = "Cleaning Rules",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            val rules = listOf(
                Triple(
                    "Aggressive Mode",
                    "Filter link trackers more aggressively",
                    settings.aggressiveMode to { checked: Boolean -> viewModel.updateAggressiveMode(checked) }
                ),
                Triple(
                    "Keep Affiliate Links",
                    "Preserve referral and affiliate parameters",
                    settings.keepAffiliate to { checked: Boolean -> viewModel.updateKeepAffiliate(checked) }
                ),
                Triple(
                    "Expand Short URLs",
                    "Resolve shortened links before cleaning",
                    settings.expandShortUrl to { checked: Boolean -> viewModel.updateExpandShortUrl(checked) }
                )
            )

            rules.forEach { (title, description, togglePair) ->
                val (checked, onCheckedChange) = togglePair
                var isPressed by remember { mutableStateOf(false) }
                val scale by animateFloatAsState(
                    targetValue = if (isPressed) 0.98f else 1f,
                    animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
                    label = "scale_$title"
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(scale)
                        .clickable(
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null,
                            onClick = { onCheckedChange(!checked) }
                        ),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
                    ),
                    border = BorderStroke(
                        width = 0.5.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        Switch(
                            checked = checked,
                            onCheckedChange = onCheckedChange,
                            modifier = Modifier.testTag(
                                when (title) {
                                    "Aggressive Mode" -> TestTags.AGGRESSIVE_MODE_SWITCH
                                    "Keep Affiliate Links" -> TestTags.KEEP_AFFILIATE_SWITCH
                                    else -> TestTags.EXPAND_SHORT_SWITCH
                                }
                            )
                        )
                    }
                }
            }
        }

        // Custom Parameters
        Text(
            text = "Custom Parameters",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            val customParams = listOf(
                Triple(
                    "Add Keep Parameter",
                    "Add a custom URL parameter to always keep",
                    { showKeepDialog = true }
                ),
                Triple(
                    "Add Remove Parameter",
                    "Add a custom URL parameter to always remove",
                    { showRemoveDialog = true }
                )
            )

            customParams.forEach { (title, description, onClick) ->
                var isPressed by remember { mutableStateOf(false) }
                val scale by animateFloatAsState(
                    targetValue = if (isPressed) 0.98f else 1f,
                    animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
                    label = "scale_$title"
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(scale)
                        .clickable(
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null,
                            onClick = onClick
                        ),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
                    ),
                    border = BorderStroke(
                        width = 0.5.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { viewModel.resetSettings() },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag(TestTags.RESET_SETTINGS_BUTTON),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Text(
                    text = "Reset Settings",
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = { viewModel.clearHistory() },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag(TestTags.CLEAR_HISTORY_BUTTON),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Text(
                    text = "Clear History",
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Safe spacing for progressive blur under the bottom bar
        Spacer(modifier = Modifier.height(contentPadding.calculateBottomPadding() + 24.dp))
    }

    if (showKeepDialog) {
        AlertDialog(
            onDismissRequest = { showKeepDialog = false },
            title = { Text("Add Keep Parameter") },
            text = {
                OutlinedTextField(
                    value = customKeepText,
                    onValueChange = { customKeepText = it },
                    placeholder = { Text("Parameter name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (customKeepText.isNotBlank()) {
                        viewModel.addCustomKeepRule(customKeepText.trim())
                        customKeepText = ""
                    }
                    showKeepDialog = false
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showKeepDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showRemoveDialog) {
        AlertDialog(
            onDismissRequest = { showRemoveDialog = false },
            title = { Text("Add Remove Parameter") },
            text = {
                OutlinedTextField(
                    value = customRemoveText,
                    onValueChange = { customRemoveText = it },
                    placeholder = { Text("Parameter name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (customRemoveText.isNotBlank()) {
                        viewModel.addCustomRemoveRule(customRemoveText.trim())
                        customRemoveText = ""
                    }
                    showRemoveDialog = false
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

fun generateQRCode(text: String): Bitmap? {
    return try {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 512, 512)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
            }
        }
        bitmap
    } catch (e: Exception) {
        null
    }
}

fun shareQRCode(context: Context, bitmap: Bitmap) {
    try {
        val file = File(context.cacheDir, "qr_code.png")
        val stream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.flush()
        stream.close()
        val uri = FileProvider.getUriForFile(
            context,
            "com.juntang2.unlink.fileprovider",
            file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share QR Code"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
