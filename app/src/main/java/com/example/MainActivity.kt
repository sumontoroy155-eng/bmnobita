package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Choreographer
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.domain.SensitivityEngine
import com.example.i18n.AppStrings
import com.example.model.AppLanguage
import com.example.ui.MainViewModel
import com.example.ui.components.CinematicBmIntroScreen
import com.example.ui.components.SubtleParticlesBackground
import com.example.ui.screens.AimTrainingScreen
import com.example.ui.screens.GamingToolsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.HudDesignerScreen
import com.example.ui.screens.MyProfileScreen
import com.example.ui.screens.PhoneMatchScreen
import com.example.ui.screens.ProfileLibraryScreen
import com.example.ui.screens.SensitivityScreen
import com.example.ui.theme.BMGamingProTheme
import com.example.ui.theme.DeepCarbon
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SilverText
import kotlinx.coroutines.launch

private const val ROUTE_HOME = "home"
private const val ROUTE_SENSITIVITY = "sensitivity"
private const val ROUTE_AIM_TRAINING = "aim_training"
private const val ROUTE_PHONE_MATCH = "phone_match"
private const val ROUTE_HUD_DESIGNER = "hud_designer"
private const val ROUTE_PROFILE_LIBRARY = "profile_library"
private const val ROUTE_GAMING_TOOLS = "gaming_tools"
private const val ROUTE_MY_PROFILE = "my_profile"

private data class NavDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BmGamingProRoot()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BmGamingProRoot(
    viewModel: MainViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val language by viewModel.language.collectAsStateWithLifecycle()
    val themeAccent by viewModel.themeAccent.collectAsStateWithLifecycle()
    val introEnabled by viewModel.introEnabled.collectAsStateWithLifecycle()
    val particlesEnabled by viewModel.particlesEnabled.collectAsStateWithLifecycle()
    val tutorialDismissed by viewModel.tutorialDismissed.collectAsStateWithLifecycle()
    val dpiNote by viewModel.dpiNote.collectAsStateWithLifecycle()
    val crosshairConfig by viewModel.crosshairConfig.collectAsStateWithLifecycle()
    val fireLabAppearance by viewModel.fireLabAppearance.collectAsStateWithLifecycle()
    val savedProfiles by viewModel.savedProfiles.collectAsStateWithLifecycle()
    val trainingProgress by viewModel.trainingProgress.collectAsStateWithLifecycle()
    val recentSessions by viewModel.recentAimSessions.collectAsStateWithLifecycle()
    val savedHudLayouts by viewModel.savedHudLayouts.collectAsStateWithLifecycle()
    val savedPerfectSetups by viewModel.savedPerfectSetups.collectAsStateWithLifecycle()
    val dailyStreak by viewModel.dailyStreak.collectAsStateWithLifecycle()
    val dailyGamingSeconds by viewModel.dailyGamingSeconds.collectAsStateWithLifecycle()
    val lastBackupTs by viewModel.lastBackupTs.collectAsStateWithLifecycle()

    val strings = AppStrings.get(language)
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: ROUTE_HOME

    var sensitivityInitialTab by remember { mutableIntStateOf(0) }
    var trainingInitialTab by remember { mutableIntStateOf(0) }

    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val totalXp = remember(recentSessions, trainingProgress, dailyStreak.first) {
        viewModel.totalXp(recentSessions, trainingProgress, dailyStreak.first)
    }
    val currentRank = remember(totalXp) { viewModel.currentRank(totalXp) }

    // Live UI Choreographer FPS measurement
    DisposableEffect(Unit) {
        var frameCount = 0
        var lastWindowNs = System.nanoTime()
        val choreographer = Choreographer.getInstance()
        val callback = object : Choreographer.FrameCallback {
            override fun doFrame(frameTimeNanos: Long) {
                frameCount++
                val elapsed = frameTimeNanos - lastWindowNs
                if (elapsed >= 1_000_000_000L) {
                    val fps = ((frameCount * 1_000_000_000.0) / elapsed).toInt().coerceIn(1, 240)
                    viewModel.updateUiFps(fps)
                    frameCount = 0
                    lastWindowNs = frameTimeNanos
                }
                choreographer.postFrameCallback(this)
            }
        }
        choreographer.postFrameCallback(callback)
        onDispose { choreographer.removeFrameCallback(callback) }
    }

    val allDestinations = listOf(
        NavDestination(ROUTE_HOME, strings.navHome, Icons.Default.Home),
        NavDestination(ROUTE_SENSITIVITY, strings.navSensitivity, Icons.Default.Tune),
        NavDestination(ROUTE_AIM_TRAINING, strings.navAimTraining, Icons.Default.SportsEsports),
        NavDestination(ROUTE_PHONE_MATCH, strings.navPhoneMatch, Icons.Default.PhoneAndroid),
        NavDestination(ROUTE_HUD_DESIGNER, strings.navHudDesigner, Icons.Default.DashboardCustomize),
        NavDestination(ROUTE_PROFILE_LIBRARY, strings.navProfileLibrary, Icons.Default.Bookmark),
        NavDestination(ROUTE_GAMING_TOOLS, strings.navGamingTools, Icons.Default.Build),
        NavDestination(ROUTE_MY_PROFILE, strings.navMyProfile, Icons.Default.Person)
    )

    val bottomDestinations = listOf(
        allDestinations[0],
        allDestinations[1],
        allDestinations[2],
        allDestinations[6],
        allDestinations[7]
    )

    fun navigateTopLevel(route: String) {
        navController.navigate(route) {
            popUpTo(ROUTE_HOME) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun showToastMessage(message: String) {
        coroutineScope.launch { snackbarHostState.showSnackbar(message) }
    }

    BMGamingProTheme(accent = themeAccent) {
        val primary = MaterialTheme.colorScheme.primary
        val secondary = MaterialTheme.colorScheme.secondary
        val container = MaterialTheme.colorScheme.primaryContainer

        Box(modifier = Modifier.fillMaxSize()) {
            ModalNavigationDrawer(
                drawerState = drawerState,
                gesturesEnabled = state.showIntro != true,
                drawerContent = {
                    ModalDrawerSheet(
                        drawerContainerColor = DeepCarbon,
                        drawerContentColor = PureWhite
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "BM GAMING PRO",
                            style = MaterialTheme.typography.headlineMedium,
                            color = PureWhite,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                        Text(
                            text = "ULTIMATE EDITION",
                            style = MaterialTheme.typography.labelSmall,
                            color = secondary,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        allDestinations.forEach { dest ->
                            NavigationDrawerItem(
                                label = { Text(dest.label) },
                                selected = currentRoute == dest.route,
                                onClick = {
                                    coroutineScope.launch { drawerState.close() }
                                    navigateTopLevel(dest.route)
                                },
                                icon = { Icon(dest.icon, contentDescription = dest.label) },
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedContainerColor = container,
                                    selectedIconColor = PureWhite,
                                    selectedTextColor = PureWhite,
                                    unselectedIconColor = SilverText,
                                    unselectedTextColor = SilverText
                                ),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            ) {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val useRail = maxWidth >= 600.dp

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = ObsidianBlack,
                        contentWindowInsets = WindowInsets.safeDrawing,
                        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                        topBar = {
                            TopAppBar(
                                navigationIcon = {
                                    IconButton(
                                        onClick = { coroutineScope.launch { drawerState.open() } },
                                        modifier = Modifier.testTag("open_nav_drawer_button")
                                    ) {
                                        Icon(Icons.Default.Menu, contentDescription = "Navigation Menu", tint = PureWhite)
                                    }
                                },
                                title = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = strings.appTitle,
                                            style = MaterialTheme.typography.headlineMedium,
                                            color = PureWhite
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "ULTIMATE",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = secondary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                },
                                actions = {
                                    Surface(
                                        onClick = { viewModel.toggleLanguage() },
                                        shape = RoundedCornerShape(20.dp),
                                        color = DeepCarbon,
                                        border = BorderStroke(1.dp, primary),
                                        modifier = Modifier
                                            .padding(end = 12.dp)
                                            .testTag("language_toggle_button")
                                    ) {
                                        Text(
                                            text = if (language == AppLanguage.BN) "বাংলা / EN" else "EN / বাংলা",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = PureWhite,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = DeepCarbon,
                                    titleContentColor = PureWhite
                                )
                            )
                        },
                        bottomBar = {
                            if (!useRail && state.showIntro == false) {
                                NavigationBar(containerColor = DeepCarbon) {
                                    bottomDestinations.forEach { item ->
                                        val selected = currentRoute == item.route
                                        NavigationBarItem(
                                            selected = selected,
                                            onClick = { navigateTopLevel(item.route) },
                                            modifier = Modifier.testTag("nav_${item.route}"),
                                            icon = { Icon(item.icon, contentDescription = item.label) },
                                            label = { Text(item.label, maxLines = 1) },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = PureWhite,
                                                selectedTextColor = secondary,
                                                indicatorColor = container,
                                                unselectedIconColor = SilverText,
                                                unselectedTextColor = SilverText
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            SubtleParticlesBackground(
                                enabled = particlesEnabled && state.showIntro == false,
                                isLowRam = state.deviceInfo.isLowRamDevice
                            )

                            Row(modifier = Modifier.fillMaxSize()) {
                                if (useRail && state.showIntro == false) {
                                    NavigationRail(containerColor = DeepCarbon) {
                                        allDestinations.forEach { item ->
                                            val selected = currentRoute == item.route
                                            NavigationRailItem(
                                                selected = selected,
                                                onClick = { navigateTopLevel(item.route) },
                                                icon = { Icon(item.icon, contentDescription = item.label) },
                                                label = { Text(item.label) },
                                                colors = NavigationRailItemDefaults.colors(
                                                    selectedIconColor = PureWhite,
                                                    selectedTextColor = secondary,
                                                    indicatorColor = container,
                                                    unselectedIconColor = SilverText,
                                                    unselectedTextColor = SilverText
                                                )
                                            )
                                        }
                                    }
                                }

                                NavHost(
                                    navController = navController,
                                    startDestination = ROUTE_HOME,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    composable(ROUTE_HOME) {
                                        HomeScreen(
                                            state = state,
                                            strings = strings,
                                            language = language,
                                            currentRank = currentRank,
                                            totalXp = totalXp,
                                            dailyStreak = dailyStreak.first,
                                            favoriteProfiles = savedProfiles.filter { it.isFavorite },
                                            tutorialDismissed = tutorialDismissed,
                                            onDismissTutorial = { viewModel.dismissTutorial() },
                                            onCopyQuickSens = {
                                                val txt = SensitivityEngine.formatShareText(
                                                    profileName = state.sensitivityMode.name,
                                                    deviceLabel = "${state.deviceInfo.brand} ${state.deviceInfo.model}",
                                                    values = state.sensitivityValues,
                                                    fireButtonPercent = state.fireButtonPercent,
                                                    dpiNote = dpiNote
                                                )
                                                copyToClipboard(context, "BM Sensitivity", txt)
                                                showToastMessage(strings.copiedToast)
                                            },
                                            onNextTip = { viewModel.nextDailyTip() },
                                            onNavigateRoute = { route -> navigateTopLevel(route) }
                                        )
                                    }

                                    composable(ROUTE_SENSITIVITY) {
                                        SensitivityScreen(
                                            state = state,
                                            strings = strings,
                                            language = language,
                                            dpiNote = dpiNote,
                                            fireLabAppearance = fireLabAppearance,
                                            savedProfiles = savedProfiles,
                                            savedPerfectSetups = savedPerfectSetups,
                                            initialSubTab = sensitivityInitialTab,
                                            onSelectMode = { viewModel.selectSensitivityMode(it) },
                                            onSelectSkillTier = { viewModel.selectSkillTier(it) },
                                            onUpdateSensitivity = { viewModel.updateSensitivityValues(it) },
                                            onRecalculate = { viewModel.recalculateRecommended() },
                                            onCopySensitivity = {
                                                val txt = SensitivityEngine.formatShareText(
                                                    profileName = state.sensitivityMode.name,
                                                    deviceLabel = "${state.deviceInfo.brand} ${state.deviceInfo.model}",
                                                    values = state.sensitivityValues,
                                                    fireButtonPercent = state.fireButtonPercent,
                                                    dpiNote = dpiNote
                                                )
                                                copyToClipboard(context, "BM Sensitivity", txt)
                                                showToastMessage(strings.copiedToast)
                                            },
                                            onSaveProfile = { name ->
                                                viewModel.saveCurrentProfile(name)
                                                showToastMessage(strings.savedToast)
                                            },
                                            onUpdateFireButton = { viewModel.updateFireButtonPercent(it) },
                                            onUpdateFireCompare = { viewModel.updateFireComparePercent(it) },
                                            onUpdateFireAppearance = { op, st -> viewModel.updateFireLabAppearance(op, st) },
                                            onUpdateDpiInches = { viewModel.updateDpiScreenInches(it) },
                                            onUpdateDpiPref = { viewModel.updateDpiPreference(it) },
                                            onSaveDpiNote = {
                                                viewModel.saveDpiNote(it)
                                                showToastMessage(strings.savedToast)
                                            },
                                            onSavePerfectSetup = { title, fingers ->
                                                viewModel.savePerfectSetupPackage(title, fingers)
                                                showToastMessage(strings.savedToast)
                                            },
                                            onDeletePerfectSetup = { viewModel.deletePerfectSetup(it) },
                                            onExportSetupJson = { json -> sharePlainText(context, json) }
                                        )
                                    }

                                    composable(ROUTE_AIM_TRAINING) {
                                        AimTrainingScreen(
                                            strings = strings,
                                            language = language,
                                            crosshairConfig = crosshairConfig,
                                            recentSessions = recentSessions,
                                            dailyStreak = dailyStreak.first,
                                            lastChallengeDate = dailyStreak.second,
                                            initialSubTab = trainingInitialTab,
                                            onUpdateCrosshair = { viewModel.updateCrosshairConfig(it) },
                                            onSaveAimSession = { m, h, s, r, sc ->
                                                viewModel.saveAimPracticeSession(m, h, s, r, sc)
                                                showToastMessage("+$sc pts saved!")
                                            },
                                            onRecordSwipePractice = { viewModel.recordSwipePractice(it) }
                                        )
                                    }

                                    composable(ROUTE_PHONE_MATCH) {
                                        PhoneMatchScreen(
                                            state = state,
                                            strings = strings,
                                            savedProfiles = savedProfiles,
                                            onSelectCatalogDevice = {
                                                viewModel.selectCatalogDevice(it)
                                                showToastMessage("${it.brand} ${it.model} ✓")
                                                navigateTopLevel(ROUTE_HOME)
                                            },
                                            onApplyCustomDevice = { b, m, r, hz, inc ->
                                                viewModel.applyCustomDeviceSpecs(b, m, r, hz, inc)
                                                showToastMessage("$b $m ✓")
                                                navigateTopLevel(ROUTE_HOME)
                                            },
                                            onResetAutoDetect = { viewModel.resetToAutoDetectedDevice() },
                                            onLoadSavedProfile = {
                                                viewModel.loadSavedProfile(it)
                                                sensitivityInitialTab = 0
                                                navigateTopLevel(ROUTE_SENSITIVITY)
                                            }
                                        )
                                    }

                                    composable(ROUTE_HUD_DESIGNER) {
                                        HudDesignerScreen(
                                            strings = strings,
                                            language = language,
                                            controls = state.hudControls,
                                            savedLayouts = savedHudLayouts,
                                            onUpdateControls = { viewModel.updateHudControls(it) },
                                            onLoadPresetFingers = { viewModel.loadHudPreset(it) },
                                            onSaveLayout = { name, fingers ->
                                                viewModel.saveCurrentHudLayout(name, fingers)
                                                showToastMessage(strings.savedToast)
                                            },
                                            onLoadSavedLayout = { viewModel.loadSavedHudLayout(it) },
                                            onDeleteLayout = { viewModel.deleteHudLayout(it) },
                                            onExportHudJson = { sharePlainText(context, viewModel.exportActiveHudJson()) }
                                        )
                                    }

                                    composable(ROUTE_PROFILE_LIBRARY) {
                                        ProfileLibraryScreen(
                                            state = state,
                                            strings = strings,
                                            savedProfiles = savedProfiles,
                                            lastBackupTs = lastBackupTs,
                                            onApplyPresetMode = { mode ->
                                                viewModel.selectSensitivityMode(mode)
                                                sensitivityInitialTab = 0
                                                navigateTopLevel(ROUTE_SENSITIVITY)
                                            },
                                            onLoadProfile = {
                                                viewModel.loadSavedProfile(it)
                                                sensitivityInitialTab = 0
                                                navigateTopLevel(ROUTE_SENSITIVITY)
                                            },
                                            onToggleFavorite = { viewModel.toggleFavoriteProfile(it) },
                                            onRenameProfile = { id, newName -> viewModel.renameProfile(id, newName) },
                                            onDuplicateProfile = { viewModel.duplicateProfile(it) },
                                            onDeleteProfile = { viewModel.deleteSavedProfile(it) },
                                            onShareProfile = { profile ->
                                                val txt = SensitivityEngine.formatShareText(
                                                    profileName = profile.name,
                                                    deviceLabel = profile.deviceLabel,
                                                    values = profile.toSensitivityValues(),
                                                    fireButtonPercent = profile.fireButtonPercent,
                                                    dpiNote = profile.dpiNote
                                                )
                                                sharePlainText(context, txt)
                                            },
                                            onCompareProfile = {
                                                sensitivityInitialTab = 1
                                                navigateTopLevel(ROUTE_SENSITIVITY)
                                            },
                                            onExportCopyJson = {
                                                copyToClipboard(context, "BM Gaming Profiles Backup", viewModel.exportProfilesJson())
                                                showToastMessage(strings.copiedToast)
                                            },
                                            onExportShareJson = { sharePlainText(context, viewModel.exportProfilesJson()) },
                                            onImportJson = { viewModel.importProfilesJson(it) },
                                            onShowMessage = { showToastMessage(it) }
                                        )
                                    }

                                    composable(ROUTE_GAMING_TOOLS) {
                                        GamingToolsScreen(
                                            state = state,
                                            strings = strings,
                                            language = language,
                                            dailyGamingSeconds = dailyGamingSeconds.second,
                                            onRunPingTest = { viewModel.runPingTest() },
                                            onGrantDndPermission = {
                                                runCatching { context.startActivity(viewModel.createDndSettingsIntent()) }
                                            },
                                            onToggleDnd = { viewModel.toggleDnd() },
                                            onSetTimerDuration = { viewModel.setTimerDuration(it) },
                                            onSetBreakReminder = { viewModel.setBreakReminderMinutes(it) },
                                            onDismissBreakAlert = { viewModel.dismissBreakAlert() },
                                            onToggleVoiceCue = { viewModel.toggleVoiceCue() },
                                            onToggleTimer = { viewModel.toggleTimer() },
                                            onResetTimer = { viewModel.resetTimer() },
                                            onNavigateRoute = { route -> navigateTopLevel(route) },
                                            onShowMessage = { showToastMessage(it) }
                                        )
                                    }

                                    composable(ROUTE_MY_PROFILE) {
                                        MyProfileScreen(
                                            strings = strings,
                                            language = language,
                                            themeAccent = themeAccent,
                                            introEnabled = introEnabled,
                                            particlesEnabled = particlesEnabled,
                                            currentRank = currentRank,
                                            totalXp = totalXp,
                                            dailyStreak = dailyStreak.first,
                                            recentSessions = recentSessions,
                                            onToggleLanguage = { viewModel.toggleLanguage() },
                                            onSelectThemeAccent = { viewModel.setThemeAccent(it) },
                                            onToggleIntroEnabled = { viewModel.setIntroEnabled(it) },
                                            onToggleParticlesEnabled = { viewModel.setParticlesEnabled(it) },
                                            onReplayIntro = { viewModel.replayIntro() },
                                            onReplayTutorial = {
                                                viewModel.showTutorial()
                                                navigateTopLevel(ROUTE_HOME)
                                            },
                                            onResetPreferences = {
                                                viewModel.resetAllPreferences()
                                                showToastMessage("Preferences reset")
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = state.showIntro == true,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                CinematicBmIntroScreen(
                    startLabel = strings.startGamingBtn,
                    skipLabel = strings.skipIntroBtn,
                    subtitleText = strings.introSubtitle,
                    isLowRam = state.deviceInfo.isLowRamDevice,
                    onFinish = { viewModel.dismissIntro() }
                )
            }
        }
    }
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    clipboard?.setPrimaryClip(ClipData.newPlainText(label, text))
}

private fun sharePlainText(context: Context, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "BM GAMING PRO"))
}
