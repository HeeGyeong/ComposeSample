package com.example.composesample.presentation.example.component.system.security

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.composesample.presentation.MainHeader
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * androidx.security:security-state — 기기의 보안 패치 수준(SPL)을 로컬에서 읽고,
 * OSV 취약점 보고서와 대조해 게시 SPL·이 기기에 아직 없는 수정(CVE)을 계산한다.
 */
@Composable
fun SecurityStateExampleUI(onBackEvent: () -> Unit) {
    val viewModel: SecurityStateViewModel = koinViewModel()
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        MainHeader(title = "Security State Example", onBackIconClicked = onBackEvent)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { OverviewCard(uiState.busy) }
            item { LocalCard(uiState) { viewModel.readLocal() } }
            item { ReportCard(uiState) { viewModel.fetchReport() } }
            item { CompareCard(uiState) }
            item { CveCard(uiState) }
            item { ProviderCard(uiState) { viewModel.queryProviders() } }
            item { CodeCard() }
            item { ReadingCard() }
        }
    }
}

// ==================== 1. 개요 ====================

@Composable
private fun OverviewCard(busy: String?) {
    StateCard(title = "1. 기기 SPL 과 게시 SPL") {
        BodyText(
            "기기 SPL(보안 패치 수준)은 기기 안에서 읽힌다. 하지만 '최신인가'는 밖의 기준이 있어야 답할 수 있다 — " +
                "Android 보안 공지를 기계가 읽는 형태로 만든 OSV 보고서를 앱이 직접 받아 라이브러리에 넣으면, 게시 SPL 과 그 사이에 고쳐진 CVE 를 계산해 준다."
        )
        Spacer(modifier = Modifier.height(6.dp))
        BulletText("SecurityStateManagerCompat.getGlobalSecurityState — 시스템 SPL·커널 버전·모듈/WebView 패키지 버전(Bundle, 네트워크 0)")
        BulletText("SecurityPatchState.getDeviceSecurityPatchLevel — 컴포넌트별 기기 SPL(SYSTEM · SYSTEM_MODULES · KERNEL)")
        BulletText("createVulnerabilityReportUrl → 앱이 받는다 → loadVulnerabilityReport(@WorkerThread) → 게시 SPL · getPatchedCves · isDeviceFullyUpdated")
        BulletText("queryAllAvailableUpdates — 기기의 업데이트 제공자(UpdateInfoService)를 IPC 로 묻는다")
        busy?.let {
            Spacer(modifier = Modifier.height(6.dp))
            WarningText("진행 중 — $it")
        }
    }
}

// ==================== 2. 로컬 ====================

@Composable
private fun LocalCard(uiState: SecurityStateViewModel.UiState, onRefresh: () -> Unit) {
    StateCard(title = "2. 이 기기 — 로컬에서 읽은 값") {
        SubTitle("getGlobalSecurityState()")
        CaptionText("인자 없이 부르면 모듈 메타데이터를 AOSP 이름(com.android.modulemetadata)으로 찾는다.")
        if (uiState.bundleRows.isEmpty()) CaptionText("읽는 중…")
        uiState.bundleRows.forEach { (key, value) -> LabeledText(bundleKeyLabel(key), value) }
        Spacer(modifier = Modifier.height(8.dp))
        SubTitle("감시 모듈 버전(DEFAULT_SYSTEM_MODULES)")
        uiState.moduleRows.forEach { (module, version) -> LabeledText(module, version) }
        CaptionText("SYSTEM_MODULES SPL 은 이 중 가장 낮은 날짜에서 출발한다 — 모듈 메타데이터 하나만 보면 더 최신으로 보인다.")
        Spacer(modifier = Modifier.height(8.dp))
        SubTitle("getDeviceSecurityPatchLevel(component)")
        uiState.rows.forEach { row ->
            LabeledText(row.component.label, row.device ?: row.deviceError ?: "읽는 중…")
        }
        Spacer(modifier = Modifier.height(8.dp))
        ActionButton("다시 읽기", onRefresh)
    }
}

// ==================== 3. 보고서 ====================

@Composable
private fun ReportCard(uiState: SecurityStateViewModel.UiState, onFetch: () -> Unit) {
    StateCard(title = "3. OSV 취약점 보고서 받기") {
        BodyText(
            "라이브러리는 보고서를 대신 받지 않는다. createVulnerabilityReportUrl() 이 이 기기 API 레벨의 주소를 만들어 주면 " +
                "앱이 받아(이 예제는 OkHttp) 문자열로 넣는다. 자체 서버는 필요 없다."
        )
        Spacer(modifier = Modifier.height(8.dp))
        ActionButton("보고서 받고 대조하기", onFetch)
        uiState.report?.let { report ->
            Spacer(modifier = Modifier.height(8.dp))
            LabeledText("주소", report.url)
            report.httpCode?.let { LabeledText("응답", "HTTP $it · ${report.bytes ?: 0}바이트 · ${report.millis ?: 0}ms") }
            LabeledText("상태", report.error ?: if (report.loaded) "loadVulnerabilityReport 완료" else "받는 중…")
        }
        Spacer(modifier = Modifier.height(4.dp))
        CaptionText("1.1.0 에서 DEFAULT_VULNERABILITY_REPORTS_URL(storage.googleapis.com)은 deprecated — '곧 동작을 멈춘다'고 적혀 있다.")
    }
}

// ==================== 4. 대조 ====================

@Composable
private fun CompareCard(uiState: SecurityStateViewModel.UiState) {
    StateCard(title = "4. 기기 SPL ↔ 게시 SPL") {
        if (uiState.report?.loaded != true) {
            CaptionText("3번에서 보고서를 받으면 채워진다.")
            return@StateCard
        }
        uiState.rows.forEach { row ->
            SubTitle(row.component.label)
            LabeledText("기기", row.device ?: row.deviceError ?: "-")
            LabeledText("게시", row.publishedError ?: row.published.joinToString())
            val device = row.device
            val latest = row.published.firstOrNull()
            if (row.component.dateBased && device != null && latest != null) {
                monthsBehind(device, latest)?.let { LabeledText("차이", if (it > 0) "${it}개월 뒤처짐" else "최신") }
            }
            if (!row.component.dateBased && device != null && row.published.isNotEmpty()) {
                kernelLineSupported(device, row.published)?.let { supported ->
                    LabeledText(
                        "계열",
                        if (supported) "게시 목록에 같은 계열이 있다 — 그 버전과 비교된다"
                        else "이 계열은 게시 목록에 없다(지원 종료) — isDeviceFullyUpdated 는 커널을 비교하지 않는다"
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }
        uiState.fullyUpdated?.let { LabeledText("isDeviceFullyUpdated()", it) }
        Spacer(modifier = Modifier.height(4.dp))
        CaptionText("SYSTEM·SYSTEM_MODULES 의 게시 SPL 은 보고서 전체에서 가장 최근 날짜다(KDoc 'Global Max'). 커널은 지원 중인 LTS 버전 목록이다.")
    }
}

// ==================== 5. CVE ====================

@Composable
private fun CveCard(uiState: SecurityStateViewModel.UiState) {
    StateCard(title = "5. 이 기기에 아직 없는 수정") {
        BodyText("getPatchedCves(component, 게시 SPL) − getPatchedCves(component, 기기 SPL) = 기기 SPL 뒤에 나온 공지의 수정.")
        Spacer(modifier = Modifier.height(6.dp))
        if (uiState.cves.isEmpty()) {
            CaptionText("3번에서 보고서를 받으면 채워진다.")
            return@StateCard
        }
        uiState.cves.forEach { cve ->
            SubTitle(cve.component.label)
            if (cve.error != null) {
                LabeledText("오류", cve.error)
            } else {
                LabeledText("기기 SPL 까지 고쳐진 CVE", severitySummary(cve.patchedAtDevice))
                LabeledText("아직 없는 수정", severitySummary(cve.missing))
                cve.missing["CRITICAL"]?.sorted()?.take(3)?.let { LabeledText("CRITICAL 예", it.joinToString()) }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }
        uiState.cveCheck.forEach { LabeledText("확인", it) }
    }
}

// ==================== 6. 업데이트 제공자 ====================

@Composable
private fun ProviderCard(uiState: SecurityStateViewModel.UiState, onQuery: () -> Unit) {
    StateCard(title = "6. 받을 수 있는 업데이트 — 기기의 업데이트 제공자") {
        BodyText(
            "queryAllAvailableUpdates() 는 UpdateInfoService 를 구현한 신뢰된 앱(시스템 업데이터·Play 스토어 등)을 찾아 IPC 로 묻는다. " +
                "구현한 앱이 없으면 빈 목록이고, fetchAvailableSecurityPatchLevel 은 기기 SPL 을 그대로 돌려준다."
        )
        Spacer(modifier = Modifier.height(8.dp))
        ActionButton("제공자 조회", onQuery)
        uiState.providerNote?.let {
            Spacer(modifier = Modifier.height(6.dp))
            LabeledText("결과", it)
        }
        uiState.providers?.forEach { p ->
            LabeledText(p.packageName, "마지막 확인 ${formatMillis(p.lastCheckMillis)} · " + if (p.updates.isEmpty()) "업데이트 없음" else p.updates.joinToString())
        }
    }
}

private fun formatMillis(millis: Long): String =
    if (millis <= 0) "-" else SimpleDateFormat("MM-dd HH:mm", Locale.US).format(Date(millis))

// ==================== 7. 코드 ====================

@Composable
private fun CodeCard() {
    StateCard(title = "7. 코드") {
        CodeBlock(
            "val state = SecurityPatchState(context)\n" +
                "val device = state.getDeviceSecurityPatchLevel(SecurityPatchState.COMPONENT_SYSTEM)\n\n" +
                "// 보고서는 앱이 받는다 — IO 스레드에서\n" +
                "val url = SecurityPatchState.createVulnerabilityReportUrl()   // API 26+\n" +
                "state.loadVulnerabilityReport(httpGet(url))                  // @WorkerThread\n\n" +
                "val published = state.getPublishedSecurityPatchLevel(SecurityPatchState.COMPONENT_SYSTEM).first()\n" +
                "val missing = state.getPatchedCves(COMPONENT_SYSTEM, published).mapValues { (s, c) ->\n" +
                "    c - state.getPatchedCves(COMPONENT_SYSTEM, device)[s].orEmpty()\n" +
                "}\n" +
                "if (!state.isDeviceFullyUpdated()) showUpdatePrompt(device, published)"
        )
    }
}

// ==================== 8. 읽는 법 ====================

@Composable
private fun ReadingCard() {
    StateCard(title = "8. 읽는 법") {
        BulletText("기기 SPL 은 로컬 값이라 네트워크 없이 나온다. '얼마나 뒤처졌나'·'무엇이 빠졌나'는 보고서를 받아야 계산된다.")
        BulletText(
            "Galaxy A72(API 33): 시스템 SPL 2023-09-01 → 게시 2026-10-01 대비 37개월, 아직 없는 수정 CRITICAL 40 · HIGH 842 · MODERATE 2. " +
                "커널 4.14 는 게시 LTS 목록(5.4·5.10·5.15)에 없어 isDeviceFullyUpdated 가 커널을 비교하지 않는다(시스템 SPL 때문에 false)."
        )
        BulletText(
            "Pixel Tablet API 35 에뮬레이터: SPL 2024-09-05 → 25개월, 아직 없는 수정 CRITICAL 67 · HIGH 630 · MODERATE 2. " +
                "커널 6.6.30 은 게시 6.6.143 과 같은 계열이라 비교된다."
        )
        BulletText(
            "SYSTEM_MODULES SPL 은 감시 모듈 중 가장 낮은 날짜다 — A72 는 모듈 메타데이터 2026-07 · telemetry 06 · adservices 05 → 2026-05-01. " +
                "모듈 메타데이터 하나만 보면 두 달 앞서 보인다."
        )
        BulletText(
            "getGlobalSecurityState() 를 인자 없이 부르면 API 33 경로는 com.android.modulemetadata(AOSP 이름)를 찾아 Google 기기에선 빈 값이다. " +
                "API 35 는 시스템 서비스가 com.google.android.modulemetadata 와 WebView 후보 6개를 키로 준다(에뮬레이터는 WebView 값이 모두 빈 문자열)."
        )
        BulletText("업데이트 제공자는 두 기기 모두 GMS(com.google.android.gms) 1곳이 응답했고 업데이트가 없어, 받을 수 있는 SPL 은 기기 SPL 그대로였다(80~258ms).")
        BulletText("VENDOR 컴포넌트는 1.1.0 에서 internal(USE_VENDOR_SPL=false)이라 공개 API 로 묻지 않는다.")
        Spacer(modifier = Modifier.height(4.dp))
        CaptionText("실측: Galaxy A72(SM-A725F, API 33) debug·release · Pixel Tablet API 35 에뮬레이터 debug. 보고서 84,335바이트(API 33)·74,399바이트(API 35).")
    }
}

// ==================== 공통 UI ====================

@Composable
private fun ActionButton(label: String, onClick: () -> Unit) {
    Button(onClick = onClick, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF37474F))) {
        Text(text = label, color = Color.White, fontSize = 13.sp)
    }
}

@Composable
private fun StateCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF212121))
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun SubTitle(text: String) {
    Text(text = text, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
}

/** 라벨·값을 한 덩어리로 — Row(라벨, 값)는 라벨이 길면 값 칸이 0 폭에 가까워져 글자가 세로로 쌓인다 */
@Composable
private fun LabeledText(label: String, value: String) {
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFF37474F))) { append(label) }
            append("  ")
            append(value)
        },
        fontSize = 11.sp,
        color = Color(0xFF424242),
        lineHeight = 16.sp,
        modifier = Modifier.padding(vertical = 2.dp)
    )
}

@Composable
private fun BodyText(text: String) {
    Text(text = text, fontSize = 13.sp, color = Color(0xFF424242), lineHeight = 19.sp)
}

@Composable
private fun CaptionText(text: String) {
    Text(text = text, fontSize = 11.sp, color = Color(0xFF757575), lineHeight = 16.sp)
}

@Composable
private fun WarningText(text: String) {
    Text(text = text, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100), lineHeight = 16.sp)
}

@Composable
private fun BulletText(text: String) {
    Text(
        text = "· $text",
        fontSize = 12.sp,
        color = Color(0xFF616161),
        lineHeight = 18.sp,
        modifier = Modifier.padding(vertical = 2.dp)
    )
}

@Composable
private fun CodeBlock(text: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(Color(0xFF263238), RoundedCornerShape(6.dp))
            .horizontalScroll(rememberScrollState())
            .padding(10.dp)
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFFECEFF1),
            lineHeight = 15.sp
        )
    }
}
