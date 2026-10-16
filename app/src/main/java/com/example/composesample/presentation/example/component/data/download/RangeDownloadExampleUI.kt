package com.example.composesample.presentation.example.component.data.download

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader
import org.koin.androidx.compose.koinViewModel

/**
 * 이어받기·분할 다운로드(HTTP Range) 예제
 *
 * 서버는 MockEngine 위의 FakeFileServer(2MiB, ETag "v<버전>") — Range·If-Range 를 지키고 Range 미지원·HEAD 거부·도중 끊김·파일 교체를
 * 스위치로 재현한다. 클라이언트는 Ketch 문서의 단계(Resolve → Plan → Download → Throttle → Persist → Complete → Resume)를
 * Ktor + RandomAccessFile 로 직접 구현한 RangeDownloader. 엔진은 RangeDownload.kt, 출처·실측은 exampleGuide.kt.
 */
@Composable
fun RangeDownloadExampleUI(
    onBackEvent: () -> Unit
) {
    val viewModel: RangeDownloadViewModel = koinViewModel()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Range Download Example",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { ServerCard(viewModel) }
            item { ResolveCard(viewModel) }
            item { DownloadCard(viewModel) }
            item { ResumeCheckCard(viewModel) }
            item { TrapCard(viewModel) }
            item { FilesCard(viewModel) }
        }
    }
}

// ==================== 0. 서버 ====================

@Composable
private fun ServerCard(viewModel: RangeDownloadViewModel) {
    val server by viewModel.serverUi.collectAsState()
    val hits by viewModel.hits.collectAsState()

    SectionCard(
        title = "🗄️ 서버 (MockEngine) — 파일 하나를 Range 로 내준다",
        description = "2MiB 파일. 버전마다 내용이 거의 모든 바이트에서 달라, 섞이면 어느 버전과도 CRC 가 맞지 않는다. " +
            "스위치는 아래 ①·② 카드의 요청에 바로 적용된다.",
        containerColor = Color(0xFFECEFF1)
    ) {
        InfoLine("파일", "v${server.version} · ETag ${server.etag} · ${RANGE_FILE_SIZE} B")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = server.rangeSupported, onClick = { viewModel.setRangeSupported(!server.rangeSupported) }, label = { Text("Range 지원", fontSize = 12.sp) })
            FilterChip(selected = server.headAllowed, onClick = { viewModel.setHeadAllowed(!server.headAllowed) }, label = { Text("HEAD 허용", fontSize = 12.sp) })
            FilterChip(selected = server.dropKiB > 0, onClick = { viewModel.setDrop(server.dropKiB == 0) }, label = { Text("응답마다 300KiB 후 끊기", fontSize = 12.sp) })
        }
        DemoButton("서버 파일 교체 (v${server.version} → v${server.version + 1})", Color(0xFFC62828)) { viewModel.replaceServerFile() }
        Spacer(modifier = Modifier.height(6.dp))
        SubTitle("서버가 받은 요청 (최신이 위)")
        if (hits.isEmpty()) SmallLabel("아직 없음")
        hits.forEach { hit ->
            val range = hit.range?.let { " $it" } ?: ""
            val ifRange = hit.ifRange?.let { " If-Range $it" } ?: ""
            SmallLabel("#${hit.seq} ${hit.method}$range$ifRange → ${hit.status} · ${hit.bodyBytes}B ${hit.note}")
        }
    }
}

// ==================== 1. Resolve ====================

@Composable
private fun ResolveCard(viewModel: RangeDownloadViewModel) {
    val resolve by viewModel.resolve.collectAsState()

    SectionCard(
        title = "🔎 ① Resolve — 크기·Range 지원·ETag 먼저 묻기",
        description = "HEAD 로 Content-Length·Accept-Ranges·ETag·Last-Modified 를 읽는다. HEAD 를 거부하는 서버면 " +
            "Range: bytes=0-0 으로 첫 1바이트만 받아 206 의 Content-Range(bytes 0-0/전체)에서 크기를 안다.",
        containerColor = Color(0xFFE3F2FD)
    ) {
        DemoButton("Resolve 실행", Color(0xFF1565C0)) { viewModel.resolveNow() }
        resolve?.let { info ->
            Spacer(modifier = Modifier.height(6.dp))
            InfoLine("경로", if (info.viaHead) "HEAD" else "HEAD 거부 → GET bytes=0-0")
            InfoLine("판정", "크기 ${info.size ?: "모름"} · Range ${if (info.acceptRanges) "지원 → 분할" else "미지원 → 연결 1개"}")
            info.lines.forEach { SmallLabel(it) }
        }
        Spacer(modifier = Modifier.height(8.dp))
        NoteBox(
            "실측: HEAD → 200 · Content-Length 2097152 · Accept-Ranges bytes · ETag · Last-Modified. 'HEAD 허용' 을 끄면 405 뒤 " +
                "GET bytes=0-0 → 206 · Content-Range bytes 0-0/2097152 — 본문 1B 로 크기와 Range 지원을 함께 안다. " +
                "'Range 지원' 을 끄면 Accept-Ranges 가 빠져 연결 1개로 처음부터 받는다."
        )
    }
}

// ==================== 2. 분할 다운로드 ====================

@Composable
private fun DownloadCard(viewModel: RangeDownloadViewModel) {
    val options by viewModel.options.collectAsState()
    val segments by viewModel.segments.collectAsState()
    val status by viewModel.status.collectAsState()
    val result by viewModel.result.collectAsState()
    val logs by viewModel.logs.collectAsState()

    SectionCard(
        title = "⬇️ ② 분할 · 속도 제한 · 일시정지와 이어받기",
        description = "구간마다 연결 하나가 Range 로 제 몫을 받아 부분 파일(.part)의 제 위치에 쓴다(FileChannel.write(buf, position)). " +
            "연결 전부가 토큰 버킷 하나를 나눠 쓰므로 연결을 늘려도 합계 속도는 제한 값이다. 일시정지하면 구간별 진행이 .state 에 남는다.",
        containerColor = Color(0xFFE8F5E9)
    ) {
        SubTitle("연결 수")
        val connectionValues = listOf(1, 2, 4, 8)
        ChipRow(connectionValues.map { "$it" }, connectionValues.indexOf(options.connections)) { viewModel.setConnections(connectionValues[it]) }
        SubTitle("속도 제한(토큰 버킷)")
        val speedValues = listOf(256L * 1024, 512L * 1024, 1024L * 1024, 0L)
        ChipRow(speedValues.map { if (it == 0L) "무제한" else "${it / 1024}KiB/s" }, speedValues.indexOf(options.bytesPerSecond)) { viewModel.setSpeed(speedValues[it]) }
        SubTitle("재개할 때 확인")
        ChipRow(ResumeCheck.entries.map { it.label }, options.resumeCheck.ordinal) { viewModel.setResumeCheck(ResumeCheck.entries[it]) }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DemoButton("처음부터 받기", Color(0xFF2E7D32)) { viewModel.start() }
            DemoButton("일시정지", Color(0xFFF9A825)) { viewModel.pause() }
            DemoButton("이어받기", Color(0xFF1B5E20)) { viewModel.resume() }
            DemoButton("취소", Color(0xFF6D4C41)) { viewModel.cancel() }
        }
        InfoLine("상태", status)
        segments.forEachIndexed { i, seg ->
            val fraction = (seg.done.coerceAtMost(seg.length).toFloat() / seg.length).coerceIn(0f, 1f)
            SmallLabel("구간 $i · ${seg.start}–${seg.end} · ${seg.done.coerceAtMost(seg.length) / 1024}/${seg.length / 1024}KiB")
            LinearProgressIndicator(progress = { fraction }, modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp))
        }
        result?.let { r ->
            Spacer(modifier = Modifier.height(6.dp))
            InfoLine("결과", r.versionLine)
            SmallLabel("크기 ${r.sizeLine} · ${r.elapsedMs}ms · 평균 ${r.kibPerSecond}KiB/s · 받은 바이트 ${r.receivedKiB}KiB")
            SmallLabel("처음부터 다시 ${r.restarts}회 · 끊긴 구간 재시도 ${r.retries}회")
        }
        if (logs.isNotEmpty()) {
            SubTitle("클라이언트 기록 (최신이 위)")
            logs.forEach { SmallLabel(it) }
        }
        Spacer(modifier = Modifier.height(8.dp))
        NoteBox(
            "실측(SM-A725F): 256KiB/s 에서 연결 1개 7.87초 · 4개 7.86초(둘 다 평균 260KiB/s) — 버킷이 연결 전체에 하나라 합계가 묶인다. " +
                "무제한이면 0.44~0.52초인데, 가짜 서버는 메모리라 연결을 늘려도 빨라지지 않는다(분할의 이득은 연결마다 대역이 막히는 실제 망에서).\n" +
                "512KiB/s 로 받다 1.6초에 멈추면 50% — 구간마다 296·240·264·240KiB 처럼 제각각이다. 이어받기는 남은 1008KiB 만 " +
                "Range + If-Range \"v1\" 로 요청해 206 을 받고 v1 과 CRC 가 같다. 멈춘 뒤 앱 프로세스를 끝내고(am kill) 다시 열어도 " +
                ".state 로 50% 를 되살려 이어받는다."
        )
    }
}

// ==================== 3. 재개 전 확인 ====================

@Composable
private fun ResumeCheckCard(viewModel: RangeDownloadViewModel) {
    val rows by viewModel.scenarioRows.collectAsState()
    val busy by viewModel.busy.collectAsState()

    SectionCard(
        title = "🔐 ③ 이어받기 전에 '서버 파일이 그대로인가'",
        description = "40% 에서 멈춘 뒤 서버 파일을 v2 로 바꾸고 이어받는다. 확인 방법 3가지 × 바꾸는 시점 2가지(멈춘 동안 / 확인과 범위 요청 사이). " +
            "맞는 결과는 v2 전체다. 섞이면 크기는 같아도 앞부분은 v1, 뒷부분은 v2 다.",
        containerColor = Color(0xFFFFF8E1)
    ) {
        DemoButton(if (busy) "실행 중…" else "6가지 실행 (속도 제한 없음 · 연결 4)", Color(0xFFEF6C00)) { viewModel.runScenarioTable() }
        rows.forEach { ScenarioRow(it) }
        Spacer(modifier = Modifier.height(8.dp))
        NoteBox(
            "실측(debug·release 같음): If-Range 는 두 시점 모두 v2 — ETag 가 다르면 서버가 206 대신 200(전체)을 줘서 처음부터 받는다" +
                "(연결 4개가 모두 200). 재개 전 HEAD 비교는 멈춘 동안 바뀐 것은 잡지만, HEAD 와 범위 요청 사이에 바뀌면 206 으로 v2 의 " +
                "뒷부분이 붙어 섞였다 — 크기는 2MiB 그대로, v1 과 같은 바이트 약 85만 · v2 와 같은 바이트 약 124만. 확인 없이 Range 만 보내면 " +
                "두 시점 모두 섞인다.\n→ 확인과 전송을 서버가 한 요청 안에서 판정하는 If-Range 에는 이 틈이 없다. Ketch 문서는 " +
                "'서버 파일이 그대로인지 확인한다'고만 적는다 — 방법은 여기서 비교한 것이다."
        )
    }
}

// ==================== 4. 확인 생략 함정 ====================

@Composable
private fun TrapCard(viewModel: RangeDownloadViewModel) {
    val rows by viewModel.trapRows.collectAsState()
    val busy by viewModel.busy.collectAsState()

    SectionCard(
        title = "🪤 ④ 받은 응답을 확인하지 않으면",
        description = "Range 미지원 서버는 이어받기 요청에도 200(전체)을 준다 — 206 인지 보지 않고 멈춘 위치에 이어 쓰면? " +
            "응답이 도중에 끊겼는데(받은 길이 < 요청 길이) 그 구간을 완료로 치면? 같은 서버에 확인하는 클라이언트와 나란히 돌린다.",
        containerColor = Color(0xFFFCE4EC)
    ) {
        DemoButton(if (busy) "실행 중…" else "4가지 실행", Color(0xFFAD1457)) { viewModel.runTraps() }
        rows.forEach { ScenarioRow(it) }
        Spacer(modifier = Modifier.height(8.dp))
        NoteBox(
            "실측: 확인을 생략하면 Range 미지원 서버가 준 200(전체 2MiB)을 멈춘 위치(843,776B) 뒤에 이어 써 2,940,928B 가 된다. " +
                "확인하는 쪽은 200 을 보고 처음부터(1회) 받아 v1. 응답마다 300KiB 에서 끊기는 서버에서 길이를 보지 않으면 크기는 2MiB 로 " +
                "맞는데(미리 setLength) v1 과 같은 바이트가 약 123만뿐이다 — 끊긴 뒤가 0 인 구멍. 길이를 보는 쪽은 구간마다 1번씩(4회) " +
                "받은 위치부터 다시 요청해 v1.\n→ 크기만 보는 검사는 구멍을 못 잡는다 — 다 받은 뒤 CRC·해시로 확인한다."
        )
    }
}

@Composable
private fun ScenarioRow(row: ScenarioResult) {
    Spacer(modifier = Modifier.height(6.dp))
    Text(
        text = "${if (row.ok) "✅" else "❌"} ${row.title}",
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = if (row.ok) Color(0xFF2E7D32) else Color(0xFFC62828)
    )
    SmallLabel("→ ${row.verdict}")
    SmallLabel("크기 ${row.size}/${row.expectedSize} · v1 과 같은 바이트 ${row.sameAsV1} · v2 와 같은 바이트 ${row.sameAsV2}")
    SmallLabel("처음부터 ${row.restarts}회 · 재시도 ${row.retries}회 · 받은 ${row.receivedBytes / 1024}KiB")
    SmallLabel("요청: ${row.requests}")
}

// ==================== 5. 파일 ====================

@Composable
private fun FilesCard(viewModel: RangeDownloadViewModel) {
    val files by viewModel.files.collectAsState()
    val stateJson by viewModel.stateJson.collectAsState()

    SectionCard(
        title = "📁 ⑤ 부분 파일 · 상태 파일 · 최종 이름",
        description = "받는 동안은 sample.bin.part 에 쓰고, 다 받은 뒤에야 sample.bin 으로 이름을 바꾼다 — 덜 받은 파일이 완성본처럼 보이지 않게. " +
            "구간 진행은 sample.bin.state(JSON)에 300ms 마다·멈출 때 저장해 앱이 죽어도 이어받는다. 취소는 둘 다 지운다.",
        containerColor = Color(0xFFF3E5F5)
    ) {
        DemoButton("새로고침", Color(0xFF6A1B9A)) { viewModel.refreshFiles() }
        SubTitle("cacheDir/range-download")
        if (files.isEmpty()) SmallLabel("파일 없음")
        files.forEach { SmallLabel(it) }
        stateJson?.let {
            SubTitle("sample.bin.state")
            CodeBox(it)
        }
        Spacer(modifier = Modifier.height(8.dp))
        NoteBox(
            "실측: 받는 동안 sample.bin.part(처음에 2,097,152B 로 잡음)·sample.bin.state(약 330B)가 있고, 앞서 받은 sample.bin 은 " +
                "새 다운로드가 끝나 이름을 바꿀 때까지 그대로다. 취소하면 .part·.state 가 지워지고, 일시정지는 둘 다 남긴다. " +
                "상태 파일은 nullable 저장 모델로 읽어 필드가 빠졌으면 이어받지 않고 처음부터 받는다(DataRules.md 의 Gson 규칙)."
        )
    }
}

// ==================== 공통 ====================

// 제네릭으로 두면 HotSwan 이 핫 리로드 불가로 건너뛴다(GENERIC_DECLARATION) — 라벨·인덱스로 받는다
@Composable
private fun ChipRow(labels: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        labels.forEachIndexed { index, label ->
            FilterChip(selected = index == selectedIndex, onClick = { onSelect(index) }, label = { Text(label, fontSize = 12.sp) })
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    description: String,
    containerColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF212121))
            Spacer(modifier = Modifier.height(6.dp))
            Text(description, fontSize = 13.sp, color = Color(0xFF424242), lineHeight = 19.sp)
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun SubTitle(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF455A64),
        modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
    )
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(top = 2.dp)) {
        Text("$label: ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF455A64))
        Text(value, fontSize = 12.sp, color = Color(0xFF424242), fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun SmallLabel(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        color = Color(0xFF616161),
        fontFamily = FontFamily.Monospace,
        lineHeight = 15.sp,
        modifier = Modifier.padding(top = 2.dp)
    )
}

@Composable
private fun CodeBox(code: String) {
    Text(
        text = code,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF263238))
            .horizontalScroll(rememberScrollState())
            .padding(10.dp),
        fontSize = 10.sp,
        lineHeight = 14.sp,
        color = Color(0xFFE0E0E0),
        fontFamily = FontFamily.Monospace
    )
}

@Composable
private fun NoteBox(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0x14000000))
            .padding(10.dp),
        fontSize = 12.sp,
        lineHeight = 17.sp,
        color = Color(0xFF424242)
    )
}

@Composable
private fun DemoButton(text: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text, fontSize = 12.sp)
    }
}
