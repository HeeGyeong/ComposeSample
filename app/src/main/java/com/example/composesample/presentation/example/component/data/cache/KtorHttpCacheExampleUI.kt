package com.example.composesample.presentation.example.component.data.cache

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.composesample.presentation.MainHeader
import com.example.composesample.presentation.example.component.data.cache.KtorHttpCacheViewModel.Policy
import com.example.composesample.presentation.example.component.data.cache.KtorHttpCacheViewModel.RequestStep
import org.koin.androidx.compose.koinViewModel

/**
 * Ktor 클라이언트 HTTP 캐시 — HttpCache 플러그인 + MockEngine 원 서버
 *
 * 요청마다 "서버 기록(도달 여부·조건부 헤더·서버 응답)"과 "클라이언트 관찰(앱이 받은 상태·캐시 이벤트)"을 나란히 보여준다.
 * 3.6.0 신규 clearAllCaches()·FileStorage(Path, FileSystem)·acceptHeaderMergeStrategy 를 실제로 호출한다.
 * 참고 자료와 핵심 개념은 같은 폴더의 exampleGuide.kt 참고.
 */
@Composable
fun KtorHttpCacheExampleUI(
    onBackEvent: () -> Unit
) {
    val viewModel: KtorHttpCacheViewModel = koinViewModel()
    val running by viewModel.running.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Ktor HTTP Cache Example",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { OverviewCard() }
            item { PolicyCard(viewModel, running) }
            item { SharedCard(viewModel, running) }
            item { ClearAllCachesCard(viewModel, running) }
            item { FileStorageCard(viewModel, running) }
            item { AcceptMergeCard(viewModel, running) }
            item { TakeawayCard() }
        }
    }
}

// ==================== 1. 개요 ====================

@Composable
private fun OverviewCard() {
    SectionCard(
        title = "🗄️ HttpCache — 클라이언트 쪽 HTTP 캐시",
        description = "서버가 보낸 Cache-Control·ETag·Last-Modified 를 해석해 응답을 저장하고, 신선하면 서버에 가지 않고 돌려주며, " +
            "만료됐으면 If-None-Match / If-Modified-Since 로 재검증한다. 서버는 MockEngine 이라 네트워크 없이 매번 같은 결과가 나온다.",
        containerColor = Color(0xFFE3F2FD)
    ) {
        CodeBox(
            code = "HttpClient(engine) {\n" +
                "    install(HttpCache) {\n" +
                "        isShared = false                     // 공유(프록시형) 캐시 여부\n" +
                "        publicStorage(CacheStorage.Unlimited())\n" +
                "        privateStorage(CacheStorage.Unlimited())\n" +
                "        // 디스크: publicStorage(FileStorage(Path(dir), SystemFileSystem))\n" +
                "    }\n" +
                "}\n" +
                "client.monitor.subscribe(HttpCache.HttpResponseFromCache) { … }\n" +
                "client.plugin(HttpCache).clearAllCaches()   // 3.6.0"
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "Ktor 3.6.0 에서 새로 생긴 것(3.5.2 aar 와 대조)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF424242))
        Spacer(modifier = Modifier.height(4.dp))
        BulletText("HttpCache.clearAllCaches() · CacheStorage.clear() — 이전에는 캐시를 비우는 공개 API 가 없었다")
        BulletText("FileStorage(Path, FileSystem, dispatcher) — kotlinx-io 기반. 기존 FileStorage(File) 은 이제 이 함수에 위임한다")
        BulletText("ContentNegotiationConfig.acceptHeaderMergeStrategy (Default / SkipIfPresent)")
        Spacer(modifier = Modifier.height(8.dp))
        NoteBox(
            "서버가 304 를 돌려줘도 앱 코드는 캐시 본문을 담은 200 을 받는다 — 304 는 호출자에게 보이지 않는다. " +
                "네트워크를 탔는지는 캐시 이벤트만으로도 구분되지 않으므로(신선한 적중과 304 재검증 모두 이벤트가 난다) 아래 표는 서버 기록을 함께 보여준다."
        )
    }
}

// ==================== 2. Cache-Control 정책별 동작 ====================

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PolicyCard(viewModel: KtorHttpCacheViewModel, running: String?) {
    var policy by remember { mutableStateOf(Policy.MAX_AGE) }
    val result by viewModel.policyResult.collectAsStateWithLifecycle()

    SectionCard(
        title = "① 응답 헤더에 따라 캐시가 하는 일",
        description = "같은 URL 을 몇 번 요청하며 요청마다 서버에 닿았는지, 어떤 조건부 헤더를 달았는지, 앱이 무엇을 받았는지를 기록한다."
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Policy.entries.forEach { item ->
                FilterChip(
                    selected = policy == item,
                    onClick = { policy = item },
                    label = { Text(item.label, fontSize = 12.sp) }
                )
            }
        }
        Text(text = "서버 응답 헤더: ${policy.headerLine}", fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF37474F))
        Spacer(modifier = Modifier.height(8.dp))
        RunButton(
            label = if (running == "policy") "실행 중…" else "시나리오 실행",
            enabled = running == null,
            onClick = { viewModel.runPolicy(policy) }
        )

        result?.takeIf { it.policy == policy }?.let { current ->
            Spacer(modifier = Modifier.height(10.dp))
            current.steps.forEach { StepRow(it) }
            if (current.steps.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                val reached = current.steps.count { it.reachedServer }
                val notModified = current.steps.count { it.serverStatus == 304 }
                val events = current.steps.count { it.fromCacheEvent }
                SummaryText(
                    "요청 ${current.steps.size}회 · 서버 도달 ${reached}회(그중 304 ${notModified}회) · " +
                        "네트워크 없이 끝남 ${current.steps.size - reached}회 · 캐시 이벤트 ${events}회"
                )
            }
        }
    }
}

@Composable
private fun StepRow(step: RequestStep) {
    val server = if (step.reachedServer) {
        "서버 ${step.serverStatus}" + (step.conditionalHeader?.let { " ← $it" } ?: "")
    } else {
        "서버 도달 없음"
    }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        shape = RoundedCornerShape(6.dp),
        color = if (step.reachedServer) Color(0xFFFFF3E0) else Color(0xFFE8F5E9)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = "#${step.index}" + (if (step.note.isNotEmpty()) " ${step.note}" else ""),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF263238)
            )
            Text(text = server, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF5D4037))
            Text(
                text = "앱이 받은 상태 ${step.clientStatus} · 캐시 이벤트 ${if (step.fromCacheEvent) "O" else "X"} · 본문 ${step.body}",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF1B5E20)
            )
        }
    }
}

// ==================== 3. private 응답과 공유 캐시 ====================

@Composable
private fun SharedCard(viewModel: KtorHttpCacheViewModel, running: String?) {
    val cells by viewModel.sharedCells.collectAsStateWithLifecycle()

    SectionCard(
        title = "② private 응답 · isShared",
        description = "Cache-Control 의 public/private 과 클라이언트의 isShared 를 2×2 로 조합해 같은 요청을 두 번 보낸다. " +
            "저장소 건수는 우리가 넘긴 CacheStorage 를 findAll 로 직접 조회한 값이다."
    ) {
        RunButton(
            label = if (running == "shared") "실행 중…" else "2×2 실행",
            enabled = running == null,
            onClick = { viewModel.runSharedComparison() }
        )
        if (cells.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            cells.forEach { cell ->
                ResultRow(
                    condition = "Cache-Control: ${cell.cacheControl} · isShared = ${cell.shared}",
                    result = "서버 ${cell.serverHits}회 · 캐시 이벤트 ${cell.fromCacheEvents} · public ${cell.publicEntries}건 · private ${cell.privateEntries}건",
                    cached = cell.serverHits == 1
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            NoteBox(
                "private 응답은 비공유 캐시(isShared = false)에서 privateStorage 로 들어가고, 공유 캐시(isShared = true)에서는 아예 저장되지 않아 " +
                    "두 번째 요청도 서버로 간다. 사용자별 응답을 여러 사용자가 함께 쓰는 캐시에 남기지 않기 위한 규칙이다."
            )
        }
    }
}

// ==================== 4. clearAllCaches ====================

@Composable
private fun ClearAllCachesCard(viewModel: KtorHttpCacheViewModel, running: String?) {
    val log by viewModel.clearLog.collectAsStateWithLifecycle()

    SectionCard(
        title = "③ clearAllCaches() — 3.6.0",
        description = "public 경로와 private 경로를 하나씩 캐시해 두고 비운다. 로그아웃·계정 전환처럼 캐시를 통째로 버려야 할 때 쓰는 API 다."
    ) {
        RunButton(
            label = if (running == "clear") "실행 중…" else "실행",
            enabled = running == null,
            onClick = { viewModel.runClearAllCaches() }
        )
        if (log.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            log.forEach { MonoLine(it) }
        }
    }
}

// ==================== 5. FileStorage ====================

@Composable
private fun FileStorageCard(viewModel: KtorHttpCacheViewModel, running: String?) {
    val context = LocalContext.current
    val result by viewModel.fileResult.collectAsStateWithLifecycle()

    SectionCard(
        title = "④ FileStorage — kotlinx-io Path vs java.io.File",
        description = "디스크 저장소는 클라이언트를 새로 만들어도(앱을 다시 켠 것과 같은 상황) 남는다. 앱 cacheDir 아래 디렉터리를 쓰고, 실행할 때마다 비우고 시작한다."
    ) {
        CodeBox(
            code = "FileStorage(Path(dir.path), SystemFileSystem)  // 3.6.0 · kotlinx-io\n" +
                "FileStorage(dir)                               // JVM · java.io.File"
        )
        Spacer(modifier = Modifier.height(8.dp))
        RunButton(
            label = if (running == "file") "실행 중…" else "실행",
            enabled = running == null,
            onClick = { viewModel.runFileStorage(context.cacheDir) }
        )
        result?.let { file ->
            Spacer(modifier = Modifier.height(10.dp))
            MonoLine("FileStorage(Path) → ${file.pathEntryClass}")
            MonoLine("FileStorage(File) → ${file.fileEntryClass}")
            MonoLine("첫 요청 뒤 디렉터리: ${file.filesAfterFirstRequest.joinToString().ifEmpty { "비어 있음" }}")
            Spacer(modifier = Modifier.height(6.dp))
            StepRow(file.newClientWithDisk)
            StepRow(file.newClientWithMemory)
            Spacer(modifier = Modifier.height(6.dp))
            MonoLine("clearAllCaches() 뒤 파일 ${file.filesAfterClear}개 · 디렉터리 ${if (file.directoryKept) "유지" else "삭제"}")
            Spacer(modifier = Modifier.height(8.dp))
            NoteBox(
                "두 입구 모두 같은 저장소(메모리 앞단 + 파일)를 돌려준다 — 3.6.0 의 FileStorage(File) 은 Path(file.path) 로 바꿔 새 함수를 부른다(바이트코드 확인). " +
                    "파일 이름은 URL 해시라 사람이 읽을 수 없고, URL 하나에 파일 하나가 생긴다."
            )
        }
    }
}

// ==================== 6. Accept 헤더 병합 ====================

@Composable
private fun AcceptMergeCard(viewModel: KtorHttpCacheViewModel, running: String?) {
    val rows by viewModel.acceptRows.collectAsStateWithLifecycle()

    SectionCard(
        title = "⑤ Accept 헤더 병합 전략 — 3.6.0",
        description = "ContentNegotiation(gson) 은 등록된 변환기의 타입(application/json)을 Accept 에 넣는다. 요청에서 accept(...) 를 직접 지정했을 때 " +
            "그 위에 덧붙일지(Default) 그대로 둘지(SkipIfPresent)를 acceptHeaderMergeStrategy 로 고른다. 값은 서버가 받은 Accept 헤더다."
    ) {
        RunButton(
            label = if (running == "accept") "실행 중…" else "실행",
            enabled = running == null,
            onClick = { viewModel.runAcceptMerge() }
        )
        if (rows.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            rows.forEach { row ->
                ResultRow(
                    condition = "${row.strategy} · accept(${row.explicitAccept ?: "지정 안 함"})",
                    result = "Accept → " + row.acceptValues.joinToString(" + ") { "\"$it\"" }.ifEmpty { "없음" },
                    cached = null
                )
            }
        }
    }
}

// ==================== 7. 정리 ====================

@Composable
private fun TakeawayCard() {
    SectionCard(
        title = "📌 정리",
        description = "위 시나리오를 실행해 확인한 동작이다(Ktor 3.6.0).",
        containerColor = Color(0xFFFFF8E1)
    ) {
        BulletText("max-age 안에서는 서버에 가지 않는다. 만료되면 ETag 로 재검증하고, 304 의 Cache-Control 로 신선도가 다시 늘어난다")
        BulletText("no-cache 와 'ETag 만 있는 응답'은 매번 재검증한다(저장은 한다). no-store 는 저장하지 않아 조건부 헤더도 없다")
        BulletText("ETag 가 없으면 Last-Modified 로 If-Modified-Since 를 보낸다")
        BulletText("서버 콘텐츠가 바뀌면 재검증에서 200 + 새 ETag 를 받아 캐시가 교체된다")
        BulletText("앱은 304 를 보지 못한다 — 트래픽 계측이 필요하면 엔진/서버 쪽 로그로 센다")
        BulletText("메모리 저장소(Unlimited)는 클라이언트와 함께 사라진다. 앱 재시작 뒤에도 쓰려면 FileStorage")
    }
}

// ==================== 공통 UI ====================

@Composable
private fun SectionCard(
    title: String,
    description: String,
    containerColor: Color = Color(0xFFFAFAFA),
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1976D2))
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = description, fontSize = 13.sp, color = Color(0xFF616161))
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun RunButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
    ) {
        Text(label, fontSize = 13.sp)
    }
}

/** 조건 한 줄 + 결과 한 줄. cached 가 true 면 두 번째 요청이 캐시로 끝난 칸(초록), false 면 서버로 간 칸(주황) */
@Composable
private fun ResultRow(condition: String, result: String, cached: Boolean?) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        shape = RoundedCornerShape(6.dp),
        color = when (cached) {
            true -> Color(0xFFE8F5E9)
            false -> Color(0xFFFFF3E0)
            null -> Color(0xFFECEFF1)
        }
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(text = condition, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF263238))
            Text(text = result, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF37474F))
        }
    }
}

@Composable
private fun MonoLine(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontFamily = FontFamily.Monospace,
        color = Color(0xFF37474F),
        modifier = Modifier.padding(vertical = 1.dp)
    )
}

@Composable
private fun SummaryText(text: String) {
    Text(text = text, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1976D2))
}

@Composable
private fun BulletText(text: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.Top) {
        Text(text = "•", fontSize = 12.sp, color = Color(0xFF424242))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = text, fontSize = 12.sp, color = Color(0xFF424242))
    }
}

@Composable
private fun NoteBox(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF1976D2).copy(alpha = 0.08f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(10.dp),
            fontSize = 12.sp,
            color = Color(0xFF37474F),
            fontStyle = FontStyle.Italic
        )
    }
}

@Composable
private fun CodeBox(code: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF263238)
    ) {
        Text(
            text = code,
            modifier = Modifier.padding(12.dp),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFFECEFF1),
            lineHeight = 16.sp
        )
    }
}
