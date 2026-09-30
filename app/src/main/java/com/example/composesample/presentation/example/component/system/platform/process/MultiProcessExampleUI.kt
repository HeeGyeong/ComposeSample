package com.example.composesample.presentation.example.component.system.platform.process

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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.composesample.presentation.MainHeader
import com.example.composesample.presentation.example.component.system.platform.process.MultiProcessViewModel.LinkStatus
import com.example.composesample.presentation.example.component.system.platform.process.MultiProcessViewModel.Target
import org.koin.androidx.compose.koinViewModel
import java.util.Locale

/**
 * 멀티프로세스 앱 구조 — android:process · isolatedProcess · Messenger
 *
 * 같은 서비스 코드를 기본 프로세스·:remote·:isolated 세 곳에 띄워, 프로세스 경계가 만드는 차이
 * (메모리·싱글턴 분리, 바인더 트랜잭션 한계, 크래시 격리, 격리 프로세스의 권한 박탈)를 직접 잰다.
 * 참고 자료와 핵심 개념은 같은 폴더의 exampleGuide.kt 참고.
 */
@Composable
fun MultiProcessExampleUI(
    onBackEvent: () -> Unit
) {
    val viewModel: MultiProcessViewModel = koinViewModel()
    val running by viewModel.running.collectAsStateWithLifecycle()
    val status by viewModel.status.collectAsStateWithLifecycle()
    val snapshots by viewModel.snapshots.collectAsStateWithLifecycle()
    val mainSnapshot by viewModel.mainSnapshot.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Multi-Process Example",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { OverviewCard(status) }
            item { IdentityCard(viewModel, mainSnapshot, snapshots[Target.REMOTE], running) }
            item { TransactionCard(viewModel, running) }
            item { CrashIsolationCard(viewModel, status[Target.REMOTE]) }
            item { IsolatedCard(viewModel, mainSnapshot, snapshots, running) }
            item { ProcessTableCard(viewModel, snapshots[Target.ISOLATED]) }
            item { TakeawayCard() }
        }
    }
}

// ==================== 1. 개요 ====================

@Composable
private fun OverviewCard(status: Map<Target, LinkStatus>) {
    SectionCard(
        title = "🧩 한 앱, 여러 프로세스",
        description = "매니페스트의 android:process 를 주면 그 컴포넌트는 별도 프로세스(별도 ART 런타임·힙)에서 돈다. " +
            "이 화면은 같은 서비스 코드를 세 곳에 선언해 두고 Messenger 로 왕복하며 차이를 잰다.",
        containerColor = Color(0xFFE3F2FD)
    ) {
        CodeBox(
            code = "<service android:name=\".LocalProcessDemoService\" />       <!-- 기본 프로세스 -->\n" +
                "<service android:name=\".RemoteProcessDemoService\"\n" +
                "         android:process=\":remote\" />                  <!-- 같은 UID -->\n" +
                "<service android:name=\".IsolatedProcessDemoService\"\n" +
                "         android:process=\":isolated\"\n" +
                "         android:isolatedProcess=\"true\" />             <!-- 격리 UID -->"
        )
        Spacer(modifier = Modifier.height(10.dp))
        Target.entries.forEach { target ->
            val linkStatus = status[target] ?: LinkStatus.UNBOUND
            MonoLine("${target.label.padEnd(8)} ${linkStatus.label}")
        }
        Spacer(modifier = Modifier.height(8.dp))
        NoteBox(
            "Application.onCreate 는 프로세스마다 실행된다. 그래서 BaseApplication 은 기본 프로세스에서만 Koin 을 시작하도록 " +
                "프로세스 가드(getProcessName() == applicationInfo.processName)를 둔다 — ①의 'Koin 시작됨' 줄이 그 결과다."
        )
    }
}

// ==================== 2. 프로세스 정체 · 싱글턴 ====================

@Composable
private fun IdentityCard(
    viewModel: MultiProcessViewModel,
    main: ProcessSnapshot?,
    remote: ProcessSnapshot?,
    running: String?
) {
    SectionCard(
        title = "① 같은 클래스, 다른 메모리",
        description = "각 프로세스가 스스로 잰 값을 나란히 놓는다. ProcessLocalState 는 object(싱글턴)지만 프로세스마다 따로 있어서, " +
            "한쪽 카운터를 올려도 다른 쪽은 그대로다."
    ) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RunButton("메인 +1", running == null) { viewModel.incrementMain() }
            RunButton(":remote +1", running == null && remote != null) { viewModel.incrementRemote() }
            RunButton("새로고침", running == null) { viewModel.refreshAll() }
        }
        Spacer(modifier = Modifier.height(10.dp))
        ComparisonTable(
            headers = listOf("메인", ":remote"),
            rows = listOf(
                "PID" to listOf(main?.pid?.toString(), remote?.pid?.toString()),
                "UID" to listOf(main?.uid?.toString(), remote?.uid?.toString()),
                "getProcessName()" to listOf(main?.processName?.shortProcessName(), remote?.processName?.shortProcessName()),
                "/proc/self/cmdline" to listOf(main?.procFsName?.shortProcessName(), remote?.procFsName?.shortProcessName()),
                "살아 있은 시간" to listOf(main?.aliveMs?.formatDuration(), remote?.aliveMs?.formatDuration()),
                "싱글턴 카운터" to listOf(main?.counter?.toString(), remote?.counter?.toString()),
                "Koin 시작됨" to listOf(main?.koinStarted?.toString(), remote?.koinStarted?.toString()),
                "PSS" to listOf(main?.pssKb?.formatKb(), remote?.pssKb?.formatKb())
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        NoteBox(
            "UID 는 같고 PID 는 다르다 — 같은 앱(같은 샌드박스)의 다른 프로세스. 첫 화면까지 나가면 ViewModel 이 정리되며 bind 를 모두 푸는데, " +
                "다시 들어와도 :remote 의 PID·카운터·살아 있은 시간이 이어진다 — bind 가 풀려도 프로세스는 캐시 상태로 남았다가 재사용된다" +
                "(시스템이 메모리가 필요할 때 정리한다). :isolated 는 bind 가 풀리면 곧바로 사라진다."
        )
    }
}

// ==================== 3. 바인더 트랜잭션 한계 ====================

@Composable
private fun TransactionCard(viewModel: MultiProcessViewModel, running: String?) {
    val rows by viewModel.payloadRows.collectAsStateWithLifecycle()
    val limit by viewModel.limit.collectAsStateWithLifecycle()

    SectionCard(
        title = "② 프로세스를 넘는 순간 크기 제한이 생긴다",
        description = "같은 Messenger 코드로 같은 바이트 배열을 보낸다. 같은 프로세스의 서비스는 Message 객체를 그대로 넘겨받고, " +
            ":remote 는 Message 를 파셀로 직렬화해 바인더 버퍼로 받는다."
    ) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RunButton("크기별 전송", running == null) { viewModel.runPayloadComparison() }
            RunButton("경계 찾기(이진 탐색)", running == null) { viewModel.findRemoteLimit() }
        }
        Spacer(modifier = Modifier.height(10.dp))
        rows.forEach { row ->
            ResultRow(
                condition = "${row.payloadBytes.formatBytes()} → 파셀 ${row.parcelBytes.formatBytes()}",
                lines = listOf(
                    "같은 프로세스: ${row.local?.text ?: "…"}",
                    ":remote     : ${row.remote?.text ?: "…"}"
                ),
                ok = row.remote?.ok
            )
        }
        limit?.let { result ->
            Spacer(modifier = Modifier.height(8.dp))
            SummaryText("경계(:remote, Messenger = oneway) — ${result.attempts}회 시도")
            MonoLine("성공 최대 페이로드 ${result.maxOkPayload.formatBytes()}")
            MonoLine("  └ 그때 Message 파셀 ${result.maxOkParcel.formatBytes()}")
            MonoLine("실패 최소 페이로드 ${result.minFailPayload.formatBytes()}")
            MonoLine("  └ ${result.failMessage}")
        }
        Spacer(modifier = Modifier.height(8.dp))
        NoteBox(
            "받는 프로세스마다 바인더 버퍼가 1MB − 2페이지(4KB 페이지면 1,040,384 B) 있고, 진행 중인 모든 트랜잭션이 이 버퍼를 나눠 쓴다. " +
                "Messenger.send 는 oneway(응답 없는) 호출이라 그 절반까지만 쓸 수 있다. 원시 transact 로 잰 값(Galaxy A72, Android 13): " +
                "동기 1,040,384 B · oneway 520,096 B — 위 경계는 Message 파셀에 인터페이스 토큰 등 64 B 와 replyTo 바인더 오프셋 8 B 를 더하면 정확히 520,096 B 다. " +
                "같은 프로세스 안에서는 직렬화 자체가 없어 한계도 없다."
        )
    }
}

// ==================== 4. 크래시 격리 · 재연결 ====================

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CrashIsolationCard(viewModel: MultiProcessViewModel, remoteStatus: LinkStatus?) {
    val timeline by viewModel.timeline.collectAsStateWithLifecycle()
    val connected = remoteStatus == LinkStatus.CONNECTED

    SectionCard(
        title = "③ 원격 프로세스가 죽어도 화면은 산다",
        description = ":remote 를 예외로 죽이거나(크래시) PID 로 kill 한다(저메모리 킬과 같은 상황). 기본 프로세스는 그대로이고, " +
            "BIND_AUTO_CREATE 바인딩이 남아 있어 시스템이 몇 초 뒤 서비스를 다시 띄우면 onServiceConnected 가 다시 온다."
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            RunButton("원격에서 예외", connected) { viewModel.crashRemote() }
            RunButton("원격 kill", connected) { viewModel.killRemote() }
            RunButton("이전 Messenger 로 보내기", true) { viewModel.sendToStaleMessenger() }
            RunButton("다시 bind", true) { viewModel.rebindRemote() }
        }
        Spacer(modifier = Modifier.height(8.dp))
        MonoLine(":remote 연결 상태: ${remoteStatus?.label ?: "-"}")
        Spacer(modifier = Modifier.height(6.dp))
        if (timeline.isEmpty()) {
            MonoLine("아직 실행한 시나리오가 없다")
        } else {
            timeline.forEach { event ->
                MonoLine("+${event.atMs.toString().padStart(5)}ms  ${event.text}")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        NoteBox(
            "끊긴 뒤 재연결은 새 프로세스다 — PID 가 바뀌고 원격 카운터는 0 으로 돌아온다. 죽기 전에 받아 둔 Messenger(바인더 참조)는 " +
                "새 프로세스가 떠도 되살아나지 않으므로 DeadObjectException 이 난다. 재연결 뒤에는 새로 받은 Messenger 를 써야 한다."
        )
        Spacer(modifier = Modifier.height(6.dp))
        NoteBox(
            "첫 크래시는 조용히 재연결되지만, 1분 안에 다시 크래시시키면 시스템이 재시작을 포기한다 — 오류 대화상자가 뜨고" +
                "(Galaxy 는 '캐시 파일을 삭제할까요?' — 취소를 누르면 된다) onBindingDied 가 온 뒤 자동 재연결이 없다. " +
                "이때는 unbind 후 다시 bind 해야 한다(다시 bind 버튼). kill 은 크래시로 세지 않아 연달아 해도 매번 자동 재연결된다."
        )
    }
}

// ==================== 5. isolatedProcess ====================

@Composable
private fun IsolatedCard(
    viewModel: MultiProcessViewModel,
    main: ProcessSnapshot?,
    snapshots: Map<Target, ProcessSnapshot>,
    running: String?
) {
    val bindings by viewModel.isolatedBindings.collectAsStateWithLifecycle()
    val probes by viewModel.probes.collectAsStateWithLifecycle()
    val columns = listOf("main" to main, ":remote" to snapshots[Target.REMOTE], ":isolated" to snapshots[Target.ISOLATED])

    SectionCard(
        title = "④ isolatedProcess — 앱이지만 앱 권한이 없다",
        description = "isolatedProcess 서비스는 앱 UID 가 아니라 시스템이 그때그때 내주는 격리 UID 로 돈다. " +
            "같은 점검 함수를 세 프로세스에서 실행해 결과를 비교한다(루프백 소켓은 아무도 듣지 않는 127.0.0.1:9)."
    ) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RunButton("격리 프로세스 다시 bind", running == null) { viewModel.rebindIsolated() }
            RunButton("세 프로세스 점검", running == null) { viewModel.runProbes() }
        }
        if (bindings.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            SummaryText("bind 이력")
            bindings.forEach { MonoLine("#${it.index}  PID ${it.pid} · UID ${it.uid}") }
        }
        if (probes.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            ProbeBlock(
                "UID · isIsolated",
                columns.map { (name, snapshot) -> name to snapshot?.let { "${it.uid} · ${it.isolated}" } }
            )
            ProbeBlock("getProcessName()", columns.map { (name, snapshot) -> name to snapshot?.processName })
            ProbeBlock("INTERNET 권한", columns.map { (name, _) -> name to probes[name]?.internetPermission })
            ProbeBlock("filesDir 의 파일 읽기", columns.map { (name, _) -> name to probes[name]?.fileRead })
            ProbeBlock("루프백 소켓", columns.map { (name, _) -> name to probes[name]?.socket })
            ProbeBlock("getRunningAppProcesses()", columns.map { (name, _) -> name to probes[name]?.visibleProcesses })
        }
        Spacer(modifier = Modifier.height(8.dp))
        NoteBox(
            "격리 프로세스는 마지막 바인딩이 풀리면 곧바로 정리되고, 다시 bind 하면 새 프로세스와 새 UID 를 받는다. " +
                "프로세스 이름에는 서비스 클래스명이 붙는다. 앱이 쓴 파일은 권한 거부(EACCES)가 아니라 아예 없는 것(ENOENT)으로 보인다. " +
                "앱 데이터·네트워크·권한이 필요 없는 신뢰할 수 없는 입력 처리(파서·렌더러)를 떼어 놓는 용도다 — 통신은 바인더뿐이다."
        )
    }
}

@Composable
private fun ProbeBlock(title: String, values: List<Pair<String, String?>>) {
    ResultRow(
        condition = title,
        lines = values.map { (name, value) -> "${name.padEnd(9)} ${value ?: "…"}" },
        ok = null
    )
}

// ==================== 6. 프로세스 표 ====================

@Composable
private fun ProcessTableCard(viewModel: MultiProcessViewModel, isolated: ProcessSnapshot?) {
    val rows by viewModel.processTable.collectAsStateWithLifecycle()

    SectionCard(
        title = "⑤ ActivityManager.getRunningAppProcesses()",
        description = "일반 앱은 자기 UID 의 프로세스만 받는다 — 격리 UID 로 도는 :isolated 는 연결돼 있어도 목록에 없다. " +
            "importance 는 시스템이 매긴 중요도(낮을수록 중요)로 메모리가 부족할 때 정리 순서의 기준이 되며, bind 된 :remote 는 " +
            "bind 한 쪽(화면)의 중요도를 물려받는다. PSS 는 각 프로세스가 스스로 잰 값(Debug.getPss)."
    ) {
        RunButton("새로고침", true) { viewModel.refreshProcessTable() }
        Spacer(modifier = Modifier.height(8.dp))
        rows.forEach { row ->
            ResultRow(
                condition = "${row.name}  (PID ${row.pid})",
                lines = listOf("importance ${row.importance} · UID ${row.uid} · PSS ${row.pssKb?.formatKb() ?: "-"}"),
                ok = null
            )
        }
        if (isolated != null && rows.none { it.pid == isolated.pid }) {
            ResultRow(
                condition = ":isolated  (PID ${isolated.pid}) — 목록에 없음",
                lines = listOf("UID ${isolated.uid} · PSS ${isolated.pssKb.formatKb()} (격리 프로세스가 직접 보고한 값)"),
                ok = false
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        NoteBox(
            "PSS 는 빌드와 dexopt 상태에 크게 좌우된다. release 실측(Galaxy A72): 설치 직후에는 :remote 도 dex 를 메모리에 따로 풀어 " +
                "약 104MB(.dex mmap 76MB)였다가 dexopt(speed-profile) 뒤 20MB 안팎. :isolated 는 dexopt 뒤에도 .dex mmap 76MB 로 약 100MB. " +
                "debug 빌드는 원격 프로세스에도 핫 리로드 런타임이 올라와 더 크다."
        )
    }
}

// ==================== 7. 정리 ====================

@Composable
private fun TakeawayCard() {
    SectionCard(
        title = "📌 정리",
        description = "프로세스를 나누면 얻는 것(격리)과 치르는 것(메모리·직렬화·초기화)을 함께 봐야 한다.",
        containerColor = Color(0xFFFFF8E1)
    ) {
        BulletText("Application.onCreate·object·companion 은 프로세스마다 새로 생긴다 — 전역 초기화에는 프로세스 가드를 둔다")
        BulletText("프로세스 사이 상태 공유는 바인더(Messenger/AIDL)·ContentProvider·파일로만 한다. SharedPreferences 는 프로세스 간 동기화를 보장하지 않는다")
        BulletText("프로세스를 넘는 데이터는 파셀 크기 제한을 받는다 — 큰 데이터는 파일·SharedMemory·ContentProvider 로 넘긴다")
        BulletText("원격이 죽으면 onServiceDisconnected → 자동 재연결(새 프로세스). 옛 Messenger 는 버리고 새로 받은 것을 쓴다")
        BulletText("프로세스마다 런타임과 힙이 따로라 거의 빈 :remote 도 20MB 안팎, :isolated 는 100MB 가까이 쓴다(release·dexopt 뒤) — 꼭 필요할 때만 나눈다")
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

/** 행 = 항목, 열 = 프로세스. 값이 아직 없으면 "…" */
@Composable
private fun ComparisonTable(headers: List<String>, rows: List<Pair<String, List<String?>>>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFECEFF1)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            TableRow(label = "", cells = headers, bold = true)
            rows.forEach { (label, cells) -> TableRow(label = label, cells = cells.map { it ?: "…" }, bold = false) }
        }
    }
}

@Composable
private fun TableRow(label: String, cells: List<String>, bold: Boolean) {
    Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.Top) {
        Text(
            text = label,
            modifier = Modifier.weight(1.3f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF263238)
        )
        cells.forEach { cell ->
            Text(
                text = cell,
                modifier = Modifier.weight(1f),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
                color = Color(0xFF37474F)
            )
        }
    }
}

/** 제목 한 줄 + 결과 여러 줄. ok 가 true 면 초록, false 면 주황, null 이면 회색 */
@Composable
private fun ResultRow(condition: String, lines: List<String>, ok: Boolean?) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        shape = RoundedCornerShape(6.dp),
        color = when (ok) {
            true -> Color(0xFFE8F5E9)
            false -> Color(0xFFFFF3E0)
            null -> Color(0xFFECEFF1)
        }
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(text = condition, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF263238))
            lines.forEach { line ->
                Text(text = line, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF37474F))
            }
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
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFFECEFF1),
            lineHeight = 15.sp
        )
    }
}

private fun Long.formatKb(): String =
    if (this < 0) "-" else String.format(Locale.US, "%,.1f MB", this / 1024.0)

private fun Long.formatDuration(): String =
    if (this < 60_000) String.format(Locale.US, "%.1fs", this / 1000.0) else "${this / 60_000}m ${this % 60_000 / 1000}s"
