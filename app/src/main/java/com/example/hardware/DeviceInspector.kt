package com.example.hardware

import android.app.ActivityManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.Display
import android.view.WindowManager
import com.example.domain.SensitivityEngine
import com.example.model.DetectedDeviceInfo
import com.example.model.DeviceCatalogEntry
import com.example.model.LiveTelemetry
import com.example.model.PingResult
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DeviceInspector(private val context: Context) {

    fun detectDeviceInfo(): DetectedDeviceInfo {
        val resources = context.resources
        val config = resources.configuration
        val displayMetrics = resources.displayMetrics

        val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        val display: Display? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            runCatching { context.display }.getOrNull() ?: getLegacyDisplay(wm)
        } else {
            getLegacyDisplay(wm)
        }

        val (widthPx, heightPx) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && wm != null) {
            val bounds = wm.currentWindowMetrics.bounds
            bounds.width() to bounds.height()
        } else if (display != null) {
            val realMetrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            display.getRealMetrics(realMetrics)
            realMetrics.widthPixels to realMetrics.heightPixels
        } else {
            displayMetrics.widthPixels to displayMetrics.heightPixels
        }

        val density = displayMetrics.density.takeIf { it > 0f } ?: 1f
        val widthDp = (widthPx / density).roundToInt()
        val heightDp = (heightPx / density).roundToInt()
        val densityDpi = displayMetrics.densityDpi.takeIf { it > 0 }
        val smallestWidthDp = config.smallestScreenWidthDp.takeIf { it > 0 }
        val estimatedInches = SensitivityEngine.computeEstimateInches(
            widthPx = widthPx,
            heightPx = heightPx,
            xdpi = displayMetrics.xdpi,
            ydpi = displayMetrics.ydpi
        )

        val refreshRateHz = display?.refreshRate?.roundToInt()?.takeIf { it > 0 }
        val supportedRates = display?.supportedModes
            ?.map { it.refreshRate.roundToInt() }
            ?.filter { it > 0 }
            ?.distinct()
            ?.sorted()
            ?: emptyList()

        val cpuCores = Runtime.getRuntime().availableProcessors().coerceAtLeast(1)
        val processorLabel = resolveProcessorLabel(cpuCores)

        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        val hasMem = if (am != null) {
            am.getMemoryInfo(memInfo)
            memInfo.totalMem > 0
        } else {
            false
        }
        val totalRamMb = if (hasMem) memInfo.totalMem / (1024L * 1024L) else null
        val availableRamMb = if (hasMem) memInfo.availMem / (1024L * 1024L) else null
        val isLowRam = am?.isLowRamDevice == true || (totalRamMb != null && totalRamMb <= 4200L)

        val brand = Build.BRAND.replaceFirstChar { it.uppercase() }.ifBlank { "Android" }
        val model = Build.MODEL.ifBlank { "Device" }

        return DetectedDeviceInfo(
            brand = brand,
            model = model,
            androidVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
            sdkInt = Build.VERSION.SDK_INT,
            resolutionLabel = if (widthPx > 0 && heightPx > 0) "${widthPx} × ${heightPx}" else "Unknown",
            widthPx = widthPx,
            heightPx = heightPx,
            widthDp = widthDp,
            heightDp = heightDp,
            densityDpi = densityDpi,
            xdpi = displayMetrics.xdpi,
            ydpi = displayMetrics.ydpi,
            smallestWidthDp = smallestWidthDp,
            estimatedInches = estimatedInches,
            refreshRateHz = refreshRateHz,
            supportedRefreshRates = supportedRates,
            processorLabel = processorLabel,
            cpuCores = cpuCores,
            totalRamMb = totalRamMb,
            availableRamMb = availableRamMb,
            isLowRamDevice = isLowRam,
            isManualOverride = false
        )
    }

    fun applyManualCatalogDevice(
        base: DetectedDeviceInfo,
        entry: DeviceCatalogEntry
    ): DetectedDeviceInfo {
        val ramMb = entry.ramGb * 1024L
        return base.copy(
            brand = entry.brand,
            model = entry.model,
            estimatedInches = entry.screenInches,
            refreshRateHz = entry.refreshRateHz,
            processorLabel = entry.processorNote,
            totalRamMb = ramMb,
            isLowRamDevice = entry.ramGb <= 4,
            isManualOverride = true
        )
    }

    fun readTelemetry(currentUiFps: Int): LiveTelemetry {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        var totalRamMb: Long? = null
        var availRamMb: Long? = null
        if (am != null) {
            am.getMemoryInfo(memInfo)
            if (memInfo.totalMem > 0) {
                totalRamMb = memInfo.totalMem / (1024L * 1024L)
                availRamMb = memInfo.availMem / (1024L * 1024L)
            }
        }

        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPercent = if (level >= 0 && scale > 0) ((level * 100f) / scale).roundToInt() else null

        val rawTemp = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1
        val tempC = if (rawTemp > 0) rawTemp / 10f else null

        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL

        val (freeStorageGb, totalStorageGb) = runCatching {
            val stat = StatFs(Environment.getDataDirectory().path)
            val free = stat.availableBytes / (1024.0 * 1024.0 * 1024.0)
            val total = stat.totalBytes / (1024.0 * 1024.0 * 1024.0)
            free to total
        }.getOrElse { null to null }

        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val caps = cm?.activeNetwork?.let { cm.getNetworkCapabilities(it) }
        val isConnected = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        val netLabel = when {
            caps == null || !isConnected -> "Offline"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Mobile Data"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            else -> "Connected"
        }

        return LiveTelemetry(
            uiFps = currentUiFps,
            batteryPercent = batteryPercent,
            batteryTempCelsius = tempC,
            isCharging = isCharging,
            availableRamMb = availRamMb,
            totalRamMb = totalRamMb,
            freeStorageGb = freeStorageGb,
            totalStorageGb = totalStorageGb,
            networkTypeLabel = netLabel,
            isNetworkConnected = isConnected
        )
    }

    suspend fun runRealPingTest(host: String = "1.1.1.1", port: Int = 443): PingResult {
        return withContext(Dispatchers.IO) {
            val probes = 5
            val samples = mutableListOf<Int>()
            var lastError: String? = null
            repeat(probes) {
                val startNs = System.nanoTime()
                runCatching {
                    Socket().use { socket ->
                        socket.connect(InetSocketAddress(host, port), 2000)
                    }
                    val elapsedMs = ((System.nanoTime() - startNs) / 1_000_000L).toInt().coerceAtLeast(1)
                    samples.add(elapsedMs)
                }.onFailure { err ->
                    lastError = err.localizedMessage ?: "Connection failed"
                }
            }
            val lost = probes - samples.size
            val lossPercent = ((lost * 100f) / probes).roundToInt()
            if (samples.isEmpty()) {
                PingResult(
                    isRunning = false,
                    targetHost = host,
                    packetLossPercent = 100,
                    errorMessage = lastError ?: "Unreachable"
                )
            } else {
                val min = samples.min()
                val max = samples.max()
                val avg = samples.average().roundToInt()
                val jitter = if (samples.size > 1) {
                    samples.zipWithNext { a, b -> abs(a - b) }.average().roundToInt()
                } else {
                    0
                }
                PingResult(
                    isRunning = false,
                    targetHost = host,
                    avgMs = avg,
                    minMs = min,
                    maxMs = max,
                    jitterMs = jitter,
                    packetLossPercent = lossPercent,
                    errorMessage = null
                )
            }
        }
    }

    fun isDndPermissionGranted(): Boolean {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        return nm?.isNotificationPolicyAccessGranted == true
    }

    fun isDndActive(): Boolean {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        val filter = nm?.currentInterruptionFilter ?: NotificationManager.INTERRUPTION_FILTER_ALL
        return filter != NotificationManager.INTERRUPTION_FILTER_ALL
    }

    fun toggleDndMode(): Boolean {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return false
        if (!nm.isNotificationPolicyAccessGranted) return false
        val currentlyActive = isDndActive()
        nm.setInterruptionFilter(
            if (currentlyActive) NotificationManager.INTERRUPTION_FILTER_ALL
            else NotificationManager.INTERRUPTION_FILTER_PRIORITY
        )
        return true
    }

    fun createDndSettingsIntent(): Intent {
        return Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    @Suppress("DEPRECATION")
    private fun getLegacyDisplay(wm: WindowManager?): Display? = wm?.defaultDisplay

    private fun resolveProcessorLabel(cores: Int): String {
        val soc = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Build.SOC_MODEL.takeIf { it.isNotBlank() && !it.equals("unknown", ignoreCase = true) }
        } else {
            null
        }
        val abi = Build.SUPPORTED_ABIS.firstOrNull().orEmpty()
        val hw = Build.HARDWARE.takeIf { it.isNotBlank() && !it.equals("unknown", ignoreCase = true) }
        return when {
            soc != null -> "$soc (${cores}C • $abi)"
            hw != null -> "$hw (${cores}C • $abi)"
            abi.isNotBlank() -> "${cores}-Core ($abi)"
            else -> "${cores}-Core CPU"
        }
    }
}
