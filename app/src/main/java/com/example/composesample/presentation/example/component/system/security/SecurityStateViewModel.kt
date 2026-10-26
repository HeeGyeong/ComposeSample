package com.example.composesample.presentation.example.component.system.security

import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.security.state.SecurityPatchState
import androidx.security.state.SecurityStateManagerCompat
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * Security State 예제의 실행부.
 *
 * ① 로컬(네트워크 0): SecurityStateManagerCompat.getGlobalSecurityState 와 컴포넌트별 기기 SPL
 * ② OSV 취약점 보고서(JSON)를 앱이 직접 받아 SecurityPatchState.loadVulnerabilityReport 에 넣은 뒤
 *    게시 SPL·패치된 CVE·isDeviceFullyUpdated·areCvesPatched
 * ③ 기기의 업데이트 제공자(UpdateInfoService) IPC 조회 — 구현한 앱이 없으면 빈 결과
 */
class SecurityStateViewModel(application: Application) : AndroidViewModel(application) {

    data class ReportState(
        val url: String,
        val httpCode: Int? = null,
        val bytes: Int? = null,
        val millis: Long? = null,
        val loaded: Boolean = false,
        val error: String? = null
    )

    data class CveState(
        val component: SpComponent,
        val patchedAtDevice: Map<String, Set<String>> = emptyMap(),
        val missing: Map<String, Set<String>> = emptyMap(),
        val error: String? = null
    )

    data class ProviderRow(val packageName: String, val lastCheckMillis: Long, val updates: List<String>)

    data class UiState(
        val bundleRows: List<Pair<String, String>> = emptyList(),
        /** SYSTEM_MODULES SPL 을 정하는 감시 모듈(DEFAULT_SYSTEM_MODULES)의 versionName */
        val moduleRows: List<Pair<String, String>> = emptyList(),
        val rows: List<SplRow> = SpComponent.entries.map { SplRow(it) },
        val report: ReportState? = null,
        val fullyUpdated: String? = null,
        val cves: List<CveState> = emptyList(),
        val cveCheck: List<String> = emptyList(),
        val providers: List<ProviderRow>? = null,
        val providerNote: String? = null,
        val busy: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val patchState = SecurityPatchState(application)
    private val client = OkHttpClient.Builder()
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()

    init {
        readLocal()
    }

    /** ① 로컬 — getGlobalSecurityState 는 PackageManager·파일을 읽어 IO 에서 */
    fun readLocal() {
        viewModelScope.launch {
            _uiState.update { it.copy(busy = "로컬 값 읽는 중") }
            val (bundleRows, rows, modules) = withContext(Dispatchers.IO) {
                // 인자 없이 부르면 AOSP 이름(com.android.modulemetadata)을 본다 — Google 기기의 모듈은 아래 moduleVersions 로
                Triple(
                    bundleEntries(SecurityStateManagerCompat(getApplication()).getGlobalSecurityState()),
                    SpComponent.entries.map { readDevice(it) },
                    moduleVersions()
                )
            }
            _uiState.update { state ->
                state.copy(
                    bundleRows = bundleRows,
                    moduleRows = modules,
                    rows = rows.map { row -> state.rows.first { it.component == row.component }.copy(device = row.device, deviceError = row.deviceError) },
                    busy = null
                )
            }
        }
    }

    /** ② 보고서를 받아 넣고 게시 값과 대조한다 */
    fun fetchReport() {
        viewModelScope.launch {
            val url = reportUrl()
            _uiState.update { it.copy(busy = "보고서 받는 중", report = ReportState(url)) }
            val report = withContext(Dispatchers.IO) { download(url) }
            _uiState.update { it.copy(report = report, busy = if (report.loaded) "대조 중" else null) }
            if (!report.loaded) return@launch
            val compared = withContext(Dispatchers.IO) { compare() }
            _uiState.update { state ->
                state.copy(
                    rows = state.rows.map { row -> compared.rows.firstOrNull { it.component == row.component }?.let { row.copy(published = it.published, publishedError = it.publishedError) } ?: row },
                    fullyUpdated = compared.fullyUpdated,
                    cves = compared.cves,
                    cveCheck = compared.cveCheck,
                    busy = null
                )
            }
        }
    }

    /** ③ 업데이트 제공자 IPC — 바인딩 대기 5초(UPDATE_INFO_SERVICE_BINDING_TIMEOUT_MS) */
    fun queryProviders() {
        viewModelScope.launch {
            _uiState.update { it.copy(busy = "업데이트 제공자 조회 중", providers = null, providerNote = null) }
            val started = System.currentTimeMillis()
            try {
                val results = patchState.queryAllAvailableUpdates()
                val available = patchState.fetchAvailableSecurityPatchLevel(SecurityPatchState.COMPONENT_SYSTEM)
                val elapsed = System.currentTimeMillis() - started
                _uiState.update {
                    it.copy(
                        providers = results.map { r ->
                            ProviderRow(r.providerPackageName, r.lastCheckTimeMillis, r.updates.map { u -> "${u.component} ${u.securityPatchLevel}" })
                        },
                        providerNote = "제공자 ${results.size}곳 · 받을 수 있는 시스템 SPL $available(업데이트가 없으면 기기 SPL 그대로) · ${elapsed}ms",
                        busy = null
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(providers = emptyList(), providerNote = describeError(e), busy = null) }
            }
        }
    }

    // ==================== 내부 ====================

    private fun bundleEntries(bundle: Bundle): List<Pair<String, String>> =
        bundle.keySet().sorted().map { key ->
            @Suppress("DEPRECATION") // 값 타입이 String·String[] 로 섞여 있어 타입별 getter 대신 표시용으로 꺼낸다
            val value = when (val v = bundle.get(key)) {
                is Array<*> -> if (v.isEmpty()) "(없음)" else v.joinToString()
                else -> displayValue(v?.toString())
            }
            key to value
        }

    /** SYSTEM_MODULES SPL 은 이 모듈들의 버전(날짜) 중 가장 낮은 값에서 출발한다(라이브러리 getSystemModulesSecurityPatchLevel) */
    private fun moduleVersions(): List<Pair<String, String>> {
        val packageManager = getApplication<Application>().packageManager
        return SecurityPatchState.DEFAULT_SYSTEM_MODULES.map { module ->
            val version = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    packageManager.getPackageInfo(module, PackageManager.PackageInfoFlags.of(0)).versionName
                } else {
                    @Suppress("DEPRECATION") // API 33 미만에는 PackageInfoFlags 오버로드가 없다
                    packageManager.getPackageInfo(module, 0).versionName
                }
            } catch (e: PackageManager.NameNotFoundException) {
                null
            }
            module to (version ?: "(없음)")
        }
    }

    private fun displayValue(value: String?): String = when {
        value == null -> "null"
        // API 33 경로는 패키지가 없으면 빈 문자열, API 35 서비스 경로는 WebView 후보 키를 빈 값으로 함께 준다(실측)
        value.isEmpty() -> "(빈 문자열)"
        else -> value
    }

    private fun readDevice(component: SpComponent): SplRow = try {
        SplRow(component, device = patchState.getDeviceSecurityPatchLevel(component.id).toString())
    } catch (e: Exception) {
        SplRow(component, deviceError = describeError(e))
    }

    /** API 26 미만에는 createVulnerabilityReportUrl 이 없다 — 같은 규칙으로 직접 만든다 */
    @SuppressLint("ObsoleteSdkInt") // debug 변형은 HotSwan 때문에 minSdk 26 이지만 release 는 24 라 이 분기가 필요하다
    private fun reportUrl(): String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            SecurityPatchState.createVulnerabilityReportUrl().toString()
        } else {
            "https://android-api.osv.dev/v1/android_sdk_${Build.VERSION.SDK_INT}.json"
        }

    private fun download(url: String): ReportState {
        val started = System.currentTimeMillis()
        return try {
            client.newCall(Request.Builder().url(url).build()).execute().use { response ->
                val body = response.body.string()
                val millis = System.currentTimeMillis() - started
                if (!response.isSuccessful) {
                    ReportState(url, response.code, body.length, millis, error = "HTTP ${response.code}")
                } else {
                    // @WorkerThread — JSON 파싱
                    patchState.loadVulnerabilityReport(body)
                    ReportState(url, response.code, body.toByteArray(Charsets.UTF_8).size, millis, loaded = true)
                }
            }
        } catch (e: Exception) {
            ReportState(url, error = describeError(e))
        }
    }

    private data class Compared(
        val rows: List<SplRow>,
        val fullyUpdated: String,
        val cves: List<CveState>,
        val cveCheck: List<String>
    )

    private fun compare(): Compared {
        val rows = SpComponent.entries.map { component ->
            try {
                SplRow(component, published = patchState.getPublishedSecurityPatchLevel(component.id).map { it.toString() })
            } catch (e: Exception) {
                SplRow(component, publishedError = describeError(e))
            }
        }
        val fully = try {
            patchState.isDeviceFullyUpdated().toString()
        } catch (e: Exception) {
            describeError(e)
        }
        val cves = SpComponent.entries.filter { it.cveCapable }.map { component -> cveState(component) }
        // 아직 없는 수정 하나와 이미 있는 수정 하나로 areCvesPatched 를 확인한다
        val missingOne = cves.firstNotNullOfOrNull { pickOne(it.missing) }
        val patchedOne = cves.firstNotNullOfOrNull { pickOne(it.patchedAtDevice) }
        val check = listOfNotNull(patchedOne, missingOne).map { cve ->
            val result = try {
                patchState.areCvesPatched(listOf(cve)).toString()
            } catch (e: Exception) {
                describeError(e)
            }
            "areCvesPatched([$cve]) = $result"
        }
        return Compared(rows, fully, cves, check)
    }

    private fun cveState(component: SpComponent): CveState = try {
        val device = patchState.getDeviceSecurityPatchLevel(component.id)
        val published = patchState.getPublishedSecurityPatchLevel(component.id).first()
        val atDevice = patchState.getPatchedCves(component.id, device).mapKeys { it.key.name }
        val atPublished = patchState.getPatchedCves(component.id, published).mapKeys { it.key.name }
        CveState(component, atDevice, missingFixes(atPublished, atDevice))
    } catch (e: Exception) {
        CveState(component, error = describeError(e))
    }

    companion object {
        private const val TIMEOUT_SECONDS = 15L
    }
}
