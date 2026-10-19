package com.example.composesample.presentation.example.component.data.outbox

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader
import org.koin.androidx.compose.koinViewModel

/**
 * 오프라인 아웃박스 전송 예제
 *
 * 보내기 = Room 에 PENDING 행 저장(말풍선이 바로 보인다) → WorkManager 워커가 그 행을 서버로 보낸다.
 * 재시도는 행에 저장된 clientId 를 그대로 써서, 응답만 잃은 경우에도 서버(MockEngine 위 FakeChatServer)가 한 번만 저장한다.
 * 엔진은 OutboxEngine.kt, Room 은 OutboxDatabase.kt, 워커는 OutboxWork.kt, 출처·실측은 exampleGuide.kt.
 */
@Composable
fun OfflineOutboxExampleUI(
    onBackEvent: () -> Unit
) {
    val viewModel: OutboxViewModel = koinViewModel()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Offline Outbox Example",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { ServerCard(viewModel) }
            item { ChatCard(viewModel) }
            item { AckExperimentCard(viewModel) }
            item { OrderExperimentCard(viewModel) }
            item { ChainExperimentCard(viewModel) }
            item { ArrivalCard(viewModel) }
            item { ClassifyCard(viewModel) }
        }
    }
}

// ==================== 0. 서버 ====================

@Composable
private fun ServerCard(viewModel: OutboxViewModel) {
    val server by viewModel.serverUi.collectAsState()
    val stored by viewModel.serverMessages.collectAsState()
    val hits by viewModel.serverHits.collectAsState()

    SectionCard(
        title = "🗄️ 서버 (MockEngine) — 같은 clientId 는 한 번만 저장",
        description = "대화마다 seq 를 매기고, 이미 가진 clientId 가 다시 오면 저장하지 않고 원래 seq 를 돌려준다(멱등). " +
            "저장 내용은 파일에 남아 앱 프로세스가 죽어도 유지된다. 스위치는 아래 채팅 카드의 다음 요청에 한 번씩 적용된다.",
        containerColor = Color(0xFFECEFF1)
    ) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = server.loseNextAck > 0, onClick = { viewModel.loseNextAck() }, label = { Text("다음 응답 유실 ×1", fontSize = 12.sp) })
            FilterChip(selected = server.failNext503 > 0, onClick = { viewModel.failNext503() }, label = { Text("503 ×2", fontSize = 12.sp) })
            FilterChip(selected = server.rejectNext400 > 0, onClick = { viewModel.rejectNext400() }, label = { Text("다음 400 ×1", fontSize = 12.sp) })
            FilterChip(selected = server.expireTokenNext > 0, onClick = { viewModel.expireTokenNext() }, label = { Text("토큰 만료 ×1", fontSize = 12.sp) })
            FilterChip(selected = server.offline, onClick = { viewModel.setOffline(!server.offline) }, label = { Text("오프라인", fontSize = 12.sp) })
            FilterChip(selected = false, onClick = { viewModel.clearSwitches() }, label = { Text("스위치 끄기", fontSize = 12.sp) })
        }
        InfoLine("남은 스위치", "유실 ${server.loseNextAck} · 503 ${server.failNext503} · 400 ${server.rejectNext400} · 만료 ${server.expireTokenNext} · 오프라인 ${if (server.offline) "켜짐" else "꺼짐"}")
        InfoLine("서버 토큰", server.validToken)
        SubTitle("서버가 저장한 메시지 = 상대방이 보는 것 (${stored.size}건)")
        if (stored.isEmpty()) SmallLabel("아직 없음")
        stored.forEach { SmallLabel("seq ${it.seq} · ${it.body} · id ${shortId(it.clientId)}") }
        SubTitle("서버가 받은 요청 (최신이 위, pid = 보낸 앱 프로세스)")
        if (hits.isEmpty()) SmallLabel("아직 없음")
        hits.forEach { hit ->
            val status = if (hit.status == 0) "응답 없음" else "${hit.status}"
            SmallLabel("#${hit.no} pid ${hit.pid} · id ${shortId(hit.clientId)} → $status · ${hit.note}")
        }
    }
}

// ==================== 1. 아웃박스 채팅 ====================

@Composable
private fun ChatCard(viewModel: OutboxViewModel) {
    val chat by viewModel.chat.collectAsState()
    val work by viewModel.work.collectAsState()
    val options by viewModel.options.collectAsState()
    val log by viewModel.log.collectAsState()
    var text by rememberSaveable { mutableStateOf("") }

    SectionCard(
        title = "💬 아웃박스 채팅 — 보내기는 요청이 아니라 행이다",
        description = "보내기를 누르면 Room 에 PENDING 행(기기에서 만든 clientId)을 저장하고 작업을 건다. 말풍선은 그 행을 그리므로 " +
            "서버 응답을 기다리지 않고 바로 보인다. 워커가 성공하면 서버 seq 가 채워지고 목록은 seq 순으로 정렬된다.",
        containerColor = Color(0xFFE8F5E9)
    ) {
        SubTitle("작업 등록 방식")
        ChipRow(EnqueueMode.entries.map { it.label }, EnqueueMode.entries.indexOf(options.mode)) { viewModel.setMode(EnqueueMode.entries[it]) }
        SubTitle("영구 실패를 워커가 끝내는 방식")
        ChipRow(FailureHandling.entries.map { it.label }, FailureHandling.entries.indexOf(options.failure)) { viewModel.setFailure(FailureHandling.entries[it]) }
        SubTitle("재시도 때 보낼 id")
        ChipRow(listOf("행의 clientId 그대로", "시도마다 새 id (함정)"), if (options.newIdEveryAttempt) 1 else 0) { viewModel.setNewIdEveryAttempt(it == 1) }

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("메시지", fontSize = 12.sp) },
            singleLine = true
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DemoButton("보내기", Color(0xFF2E7D32)) {
                viewModel.send(text)
                text = ""
            }
            DemoButton("연속 3개", Color(0xFF00695C)) { viewModel.sendBurst() }
            DemoButton("초기화", Color(0xFF616161)) { viewModel.resetChat() }
        }

        SubTitle("아웃박스 (Room 의 행 — 내 화면)")
        if (chat.isEmpty()) SmallLabel("아직 없음")
        chat.forEach { row -> Bubble(row, work[row.clientId], viewModel) }

        Spacer(modifier = Modifier.height(8.dp))
        NoteBox(
            "앱을 죽여 보기: '다음 응답 유실' 을 켜고 보낸 뒤 HOME 으로 나가, 다음 시도(10초 뒤) 전에 " +
                "adb shell am kill com.example.composesample 를 실행한다. 행과 작업은 디스크에 남아 있어 다음 시도 시각에 시스템이 " +
                "앱 프로세스를 새로 띄워 워커를 돌리고, 같은 clientId 로 보낸 요청이 원래 seq 를 돌려받는다(서버 카드에 pid 가 다른 두 요청). " +
                "다시 열면 화면은 행을 읽어 SENT 로 보여준다. 실측(에뮬레이터 API 30): HOME 직후 1초에 am kill 하면 프로세스가 남았고, 3초 뒤에는 종료됐다."
        )
        SubTitle("기록 (최신이 위, 이 프로세스에서만)")
        if (log.isEmpty()) SmallLabel("아직 없음")
        log.take(14).forEach { SmallLabel(it) }
    }
}

@Composable
private fun Bubble(row: OutboxMessageEntity, work: OutboxViewModel.WorkUi?, viewModel: OutboxViewModel) {
    val stuck = row.status == OutboxStatus.PENDING && work != null && work.state.isFinished
    val color = when {
        stuck -> Color(0xFFFFCDD2)
        row.status == OutboxStatus.SENT -> Color(0xFFC8E6C9)
        row.status == OutboxStatus.FAILED -> Color(0xFFFFE0B2)
        else -> Color(0xFFFFF9C4)
    }
    val status = when {
        stuck -> "⛔ 행은 PENDING 인데 작업 ${work.state} — 아무도 다시 보내지 않는다"
        row.status == OutboxStatus.SENT -> "✓ seq ${row.seq} · 시도 ${row.attempts}"
        row.status == OutboxStatus.FAILED -> "⚠ FAILED · ${row.lastError}"
        else -> "⏳ PENDING · 시도 ${row.attempts}" + (row.lastError?.let { " · 마지막 $it" } ?: "")
    }
    val workLine = when {
        work == null -> "작업 없음"
        work.nextRunAt != null -> "작업 ${work.state} · 다음 시도 ${viewModel.formatTime(work.nextRunAt)}"
        else -> "작업 ${work.state} · runAttemptCount ${work.runAttemptCount}"
    }
    Row(modifier = Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.End) {
        Column(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(color)
                .padding(10.dp)
        ) {
            Text(row.body, fontSize = 14.sp, color = Color(0xFF212121))
            Spacer(modifier = Modifier.height(4.dp))
            Text(status, fontSize = 11.sp, color = Color(0xFF424242), lineHeight = 15.sp)
            Text("id ${shortId(row.clientId)} · $workLine", fontSize = 10.sp, color = Color(0xFF616161), fontFamily = FontFamily.Monospace, lineHeight = 14.sp)
            if (row.status == OutboxStatus.FAILED || stuck) {
                TextButton(onClick = { viewModel.resend(row.clientId) }) { Text("다시 보내기 (같은 행)", fontSize = 12.sp) }
            }
        }
    }
}

// ==================== 2. 실험 ① 응답 유실 ====================

@Composable
private fun AckExperimentCard(viewModel: OutboxViewModel) {
    val runs by viewModel.ackRuns.collectAsState()
    val busy by viewModel.busy.collectAsState()

    SectionCard(
        title = "🧪 ① 응답 유실 — 같은 id 로 재시도 vs 시도마다 새 id",
        description = "서버는 첫 요청을 저장한 뒤 응답만 잃는다. 클라이언트는 IOException 을 받아 재시도(10초 뒤)한다. " +
            "같은 clientId 면 서버가 원래 seq 를 돌려주고, 워커 안에서 id 를 새로 만들면 서버는 다른 메시지로 알고 한 번 더 저장한다.",
        containerColor = Color(0xFFE3F2FD)
    ) {
        ExperimentButton("두 가지 동시에 실행 (재시도 대기 10초 포함)", busy, Color(0xFF1565C0)) { viewModel.runAckExperiment() }
        runs.forEach { run ->
            SubTitle(run.label)
            InfoLine("서버 저장", "${run.serverCopies}건")
            SmallLabel(run.result)
            run.serverLines.forEach { SmallLabel("  $it") }
        }
        Spacer(modifier = Modifier.height(8.dp))
        NoteBox(
            "실측: 둘 다 시도 2 에 SENT. 같은 id 는 두 번째 요청에 서버가 '같은 client_id — 원래 seq 1 반환' 으로 답해 저장 1건, " +
                "새 id 는 서버가 다른 메시지로 알아 seq 1·2 로 2건 — 내 화면은 1개인데 상대방은 같은 말을 두 번 본다."
        )
        Spacer(modifier = Modifier.height(8.dp))
        CodeBox(
            "// 올바른 방식 — id 는 행을 만들 때 한 번, 재시도는 행에서 읽는다\n" +
                "dao.insert(row.copy(clientId = UUID.randomUUID().toString()))\n" +
                "client.send(row.conversationId, row.clientId, row.body)\n\n" +
                "// 함정 — 워커 안에서 만든 id 는 재시작·재시도 때마다 달라진다\n" +
                "client.send(row.conversationId, UUID.randomUUID().toString(), row.body)"
        )
    }
}

// ==================== 3. 실험 ② 순서 ====================

@Composable
private fun OrderExperimentCard(viewModel: OutboxViewModel) {
    val runs by viewModel.orderRuns.collectAsState()
    val busy by viewModel.busy.collectAsState()

    SectionCard(
        title = "🧪 ② 일시 장애와 순서 — 대화별 체인 vs 메시지마다 따로",
        description = "'첫째' 를 보내자마자 '둘째' 를 보낸다. 서버는 첫째를 한 번 503 으로 돌려보낸다(저장 안 함). " +
            "순서는 서버가 받은 순서대로 매기는 seq 가 정한다 — 상대방도 이 순서로 본다.",
        containerColor = Color(0xFFF3E5F5)
    ) {
        ExperimentButton("두 가지 동시에 실행 (재시도 대기 10초 포함)", busy, Color(0xFF6A1B9A)) { viewModel.runOrderExperiment() }
        runs.forEach { run ->
            SubTitle(run.label)
            InfoLine("서버 순서", run.serverOrder)
            InfoLine("둘째 끝난 시각", run.secondSentAfter)
            SmallLabel(run.note)
        }
        Spacer(modifier = Modifier.height(8.dp))
        NoteBox(
            "실측: 체인은 첫째 → 둘째 순서를 지키지만 둘째가 첫째의 재시도(10초 대기)를 기다려 약 11초 뒤에 간다. 따로 걸면 둘째가 1초 안에 " +
                "먼저 저장돼 서버 순서가 둘째 → 첫째로 뒤집힌다. 응답만 잃은 경우는 첫째가 이미 저장돼 있어 순서가 뒤집히지 않는다(단위 테스트)."
        )
    }
}

// ==================== 4. 실험 ③ 체인 ====================

@Composable
private fun ChainExperimentCard(viewModel: OutboxViewModel) {
    val runs by viewModel.chainRuns.collectAsState()
    val busy by viewModel.busy.collectAsState()

    SectionCard(
        title = "🧪 ③ 영구 실패 뒤의 체인 — failure() 와 APPEND 의 함정",
        description = "첫째는 서버가 400 으로 거부한다. 둘째는 첫째와 함께 등록해 첫째 뒤에 매달리고, 셋째는 체인이 끝난 뒤 등록한다. " +
            "워커가 실패를 어떻게 끝내는지와 등록 정책에 따라 뒤 메시지가 보내지는지 본다.",
        containerColor = Color(0xFFFFF3E0)
    ) {
        ExperimentButton("세 가지 동시에 실행", busy, Color(0xFFE65100)) { viewModel.runChainExperiment() }
        runs.forEach { run ->
            SubTitle(run.label)
            run.lines.forEach { SmallLabel(it) }
        }
        Spacer(modifier = Modifier.height(8.dp))
        NoteBox(
            "실측: 워커가 failure() 를 돌려주면 함께 등록한 둘째는 등록 정책과 상관없이 작업 FAILED — 보내지도 않았는데 행은 PENDING 으로 남는다. " +
                "끝난 뒤 등록한 셋째는 APPEND 면 곧바로 FAILED(실패한 체인 뒤에 붙음), APPEND_OR_REPLACE 면 새 체인으로 SENT. " +
                "행만 FAILED 로 내리고 success() 를 돌려주면 둘째·셋째 모두 SENT 다 — 실패는 행에 남기고 체인은 막지 않는다."
        )
    }
}

// ==================== 5. 중복 도착 ====================

@Composable
private fun ArrivalCard(viewModel: OutboxViewModel) {
    val tables by viewModel.arrivals.collectAsState()
    val busy by viewModel.busy.collectAsState()

    SectionCard(
        title = "🧩 같은 메시지가 네 번 도착 — @Upsert 와 키 설계",
        description = "내 보내기(seq 없음) 뒤에 소켓 이벤트·푸시 뒤 동기화·보내기 응답이 같은 메시지를 seq 7 로 다시 가져온다. " +
            "세 가지 키 설계의 테이블에 같은 순서로 넣고 남은 행을 본다.",
        containerColor = Color(0xFFE0F7FA)
    ) {
        ExperimentButton("네 번 넣기", busy, Color(0xFF00838F)) { viewModel.runArrivalExperiment() }
        tables.forEach { table ->
            SubTitle(table.title)
            table.rows.forEach { SmallLabel(it) }
            InfoLine("판정", table.verdict)
        }
        Spacer(modifier = Modifier.height(8.dp))
        NoteBox(
            "실측: ① 은 1행·seq 7. ② 는 1행이지만 seq 가 null 로 남는다 — 두 번째부터 insert 가 고유 인덱스에 막히고, 이어지는 UPDATE 는 " +
                "새 객체의 local_id(0)로 찾아 0행을 바꾼다. 예외도 경고도 없다. ③ 은 seq 7 이 남지만 local_id 가 1 → 4 로 바뀐다(지우고 새로 넣음) — " +
                "다른 테이블이 local_id 를 참조하면 끊긴다. 중복을 합치려면 clientId 를 기본 키로 둔다."
        )
        Spacer(modifier = Modifier.height(8.dp))
        CodeBox(
            "// Room 2.8.5 EntityUpsertAdapter.upsert(): insert → 고유성 위반이면 updateAdapter.handle(entity)\n" +
                "// update 는 기본 키로 찾는다 → 자동 증가 키가 0 인 새 객체면 WHERE local_id = 0 → 0행\n" +
                "@Entity(indices = [Index(\"client_id\", unique = true)])\n" +
                "data class Row(@PrimaryKey(autoGenerate = true) val localId: Long = 0, val clientId: String, …)"
        )
    }
}

// ==================== 6. 재시도 분류 ====================

@Composable
private fun ClassifyCard(viewModel: OutboxViewModel) {
    SectionCard(
        title = "📋 재시도 분류와 백오프 — 워커가 쓰는 표 그대로",
        description = "IOException·408·429·5xx 는 다시 시도하고, 그 밖의 4xx 는 영구 실패로 행을 FAILED 로 내린다. " +
            "401 은 토큰을 한 번 갱신하고 다시 보낸다. 예외는 타입이 아니라 원인 사슬로 본다.",
        containerColor = Color(0xFFFCE4EC)
    ) {
        viewModel.classifyRows.forEach { InfoLine(it.input, it.decision) }
        SubTitle("EXPONENTIAL · 최소 10초 (WorkRequest.MIN_BACKOFF_MILLIS)")
        val waits = viewModel.backoffMillis
        SmallLabel(
            waits.mapIndexed { index, millis -> "${index + 2}번째 시도 전 ${millis / 1000}초" }.joinToString(" → ") +
                " → ${OUTBOX_MAX_ATTEMPTS}번째도 재시도 사유면 FAILED (대기 합 ${waits.sum() / 1000}초)"
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
private fun ExperimentButton(text: String, busy: String?, color: Color, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Button(
            onClick = onClick,
            enabled = busy == null,
            colors = ButtonDefaults.buttonColors(containerColor = color),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(text, fontSize = 12.sp)
        }
        if (busy != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Text("실행 중: $busy", fontSize = 11.sp, color = Color(0xFF616161))
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

// 라벨과 값을 한 텍스트로 — Row 로 나누면 라벨이 긴 행(분류표)에서 값 칸이 거의 0 폭이 되어 한 글자씩 세로로 밀렸다
@Composable
private fun InfoLine(label: String, value: String) {
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFF455A64))) { append("$label: ") }
            withStyle(SpanStyle(color = Color(0xFF424242), fontFamily = FontFamily.Monospace)) { append(value) }
        },
        fontSize = 12.sp,
        lineHeight = 17.sp,
        modifier = Modifier.fillMaxWidth().padding(top = 2.dp)
    )
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

