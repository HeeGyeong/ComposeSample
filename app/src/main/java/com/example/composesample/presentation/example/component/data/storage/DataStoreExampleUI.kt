package com.example.composesample.presentation.example.component.data.storage

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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

/**
 * DataStore ↔ SharedPreferences — 동시 갱신·쓰기 비용·손상 파일·이관·파일당 인스턴스 하나 규칙을
 * 같은 기기에서 직접 돌려 대조한다.
 */
@Composable
fun DataStoreExampleUI(onBackEvent: () -> Unit) {
    val viewModel: DataStoreViewModel = koinViewModel()
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        MainHeader(title = "DataStore Example", onBackIconClicked = onBackEvent)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { OverviewCard(uiState.busy) }
            item { RaceCard(uiState.race) { viewModel.runRace() } }
            item { CostCard(uiState.cost) { viewModel.runCost() } }
            item { CorruptionCard(uiState.corruption) { viewModel.runCorruption() } }
            item { MigrationCard(uiState.migration) { someKeysOnly -> viewModel.runMigration(someKeysOnly) } }
            item { TwinCard(uiState.twin) { viewModel.runTwin() } }
            item { CodeCard() }
            item { ReadingCard() }
        }
    }
}

// ==================== 1. 개요 ====================

@Composable
private fun OverviewCard(busy: String?) {
    StoreCard(title = "1. 무엇이 다른가") {
        BodyText(
            "SharedPreferences 는 메모리 Map 을 바로 고치고 디스크는 나중에(apply) 또는 그 자리에서(commit) 쓴다. " +
                "DataStore 는 값을 Flow 로 내보내고, 바꾸기는 updateData 의 transform 하나로만 — transform 들은 한 줄로 서서 차례로 파일에 반영된다."
        )
        Spacer(modifier = Modifier.height(6.dp))
        BulletText("읽고-고치고-쓰기: SP 는 호출자가 맞춰야 하고, updateData 는 앞 결과를 다음 transform 에 넘긴다")
        BulletText("손상: SP 는 빈 값으로 넘어가고, DataStore 는 기본으로 CorruptionException — 복구는 처리기를 명시해야 한다")
        BulletText("파일 하나에 DataStore 인스턴스 하나 — 같은 파일을 두 번 열면 두 번째가 IllegalStateException")
        BulletText("아래 실험은 이 앱의 filesDir/datastore_demo 와 datastore_demo_* SP 파일만 만든다")
        busy?.let {
            Spacer(modifier = Modifier.height(6.dp))
            WarningText(it)
        }
    }
}

// ==================== 2. 동시 증가 ====================

@Composable
private fun RaceCard(results: List<RaceResult>?, onRun: () -> Unit) {
    StoreCard(title = "2. 동시 증가 100회 — 잃어버린 갱신") {
        BodyText("코루틴 100개가 Dispatchers.Default 에서 동시에 count 를 읽고 +1 해서 쓴다. SP 는 getInt → apply, DataStore 는 updateData { it.copy(count + 1) }.")
        Spacer(modifier = Modifier.height(8.dp))
        ActionButton("실행", onRun)
        results?.let {
            Spacer(modifier = Modifier.height(8.dp))
            it.forEach { r ->
                LabeledText(r.name, "결과 ${r.actual} / ${r.expected} · 잃음 ${r.lost} · ${r.millis}ms")
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        CaptionText("SP 의 각 호출은 스레드 안전하다 — 잃는 것은 읽기와 쓰기 사이에 다른 쓰기가 끼어드는 '조합'이다.")
    }
}

// ==================== 3. 쓰기 비용 ====================

@Composable
private fun CostCard(results: List<CostResult>?, onRun: () -> Unit) {
    StoreCard(title = "3. 쓰기 200회 — 호출 스레드가 기다리는 시간") {
        BodyText("같은 값을 200번 바꾼다. 시간은 호출한 쪽이 돌려받기까지 걸린 시간이다.")
        Spacer(modifier = Modifier.height(8.dp))
        ActionButton("실행", onRun)
        results?.let {
            Spacer(modifier = Modifier.height(8.dp))
            it.forEach { r -> LabeledText(r.name, "${r.millis}ms · ${r.diskWrites}") }
        }
        Spacer(modifier = Modifier.height(4.dp))
        CaptionText("apply 가 빠른 것은 디스크를 미뤘기 때문이다 — 밀린 쓰기는 ActivityThread 가 onStop·서비스 종료를 처리할 때 QueuedWork.waitToFinish 로 메인 스레드에서 기다린다(AOSP 소스 기준, 이 예제는 그 대기를 재지 않는다).")
    }
}

// ==================== 4. 손상 파일 ====================

@Composable
private fun CorruptionCard(rows: List<Pair<String, String>>?, onRun: () -> Unit) {
    StoreCard(title = "4. 손상 파일 — 조용히 비우기 vs 예외") {
        BodyText("두 저장소의 파일을 같은 임의 바이트(9바이트)로 덮고 다시 연다 — 저장 중 전원 차단·디스크 오류를 흉내 낸다.")
        Spacer(modifier = Modifier.height(8.dp))
        ActionButton("실행", onRun)
        rows?.let {
            Spacer(modifier = Modifier.height(8.dp))
            it.forEach { (label, value) -> LabeledText(label, value) }
        }
        Spacer(modifier = Modifier.height(4.dp))
        CaptionText("ReplaceFileCorruptionHandler 는 파일이 여전히 깨져 있는지 쓰기 잠금 안에서 다시 확인한 뒤 기본값으로 덮어쓴다.")
    }
}

// ==================== 5. 이관 ====================

@Composable
private fun MigrationCard(rows: List<Pair<String, String>>?, onRun: (someKeysOnly: Boolean) -> Unit) {
    StoreCard(title = "5. SharedPreferencesMigration") {
        BodyText("데모용 SP(datastore_demo_legacy)에 int·String·Boolean·StringSet 을 넣고 Preferences DataStore 를 연다. 이관은 첫 읽기 전에 한 번 돈다.")
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ActionButton("전체 키") { onRun(false) }
            ActionButton("count·label 만") { onRun(true) }
        }
        rows?.let {
            Spacer(modifier = Modifier.height(8.dp))
            it.forEach { (label, value) -> LabeledText(label, value) }
        }
    }
}

// ==================== 6. 같은 파일 두 개 ====================

@Composable
private fun TwinCard(rows: List<Pair<String, String>>?, onRun: () -> Unit) {
    StoreCard(title = "6. 같은 파일에 DataStore 두 개") {
        BodyText("A 를 열어 둔 채 같은 파일로 B 를 만들어 읽고, A 의 scope 를 끝낸 뒤 C 로 다시 읽는다.")
        Spacer(modifier = Modifier.height(8.dp))
        ActionButton("실행", onRun)
        rows?.let {
            Spacer(modifier = Modifier.height(8.dp))
            it.forEach { (label, value) -> LabeledText(label, value) }
        }
        Spacer(modifier = Modifier.height(4.dp))
        CaptionText("검사는 생성 시점이 아니라 첫 읽기·쓰기(파일 연결) 때 한다. 'scope 가 살아 있는 동안 활성'이라 화면마다 새로 만들면 두 번째 진입에서 터진다.")
    }
}

// ==================== 7. 코드 ====================

@Composable
private fun CodeCard() {
    StoreCard(title = "7. 코드") {
        CodeBlock(
            "// 파일당 하나 — 최상위 delegate 또는 Koin single\n" +
                "val Context.settings by dataStore(\"settings.json\", SettingsSerializer,\n" +
                "    corruptionHandler = ReplaceFileCorruptionHandler { Settings() })\n" +
                "val Context.prefs by preferencesDataStore(\"prefs\",\n" +
                "    produceMigrations = { listOf(SharedPreferencesMigration(it, \"legacy\")) })\n\n" +
                "// 읽기는 Flow — 기본(ReThrow)이면 손상 시 예외가 Flow 로 온다\n" +
                "context.settings.data.catch { e -> if (e is IOException) emit(Settings()) else throw e }\n\n" +
                "// 쓰기는 transform — 여러 값은 한 번에 묶는다\n" +
                "context.settings.updateData { it.copy(count = it.count + 1) }"
        )
    }
}

// ==================== 8. 읽는 법 ====================

@Composable
private fun ReadingCard() {
    StoreCard(title = "8. 읽는 법") {
        CaptionText("release 빌드 실측 — Galaxy A72(API 33) / Pixel Tablet API 35 에뮬레이터, 각 2~3회")
        Spacer(modifier = Modifier.height(4.dp))
        BulletText(
            "동시 증가: SP 는 읽기-쓰기 사이에 아무것도 없어도 A72 37~44회, 에뮬레이터 1~7회를 잃었고, 1ms 작업이 끼면 둘 다 98~99회를 잃었다. " +
                "updateData 는 매번 100 — 대신 transform 이 한 줄로 서서 518~575ms / 241~1198ms 걸렸다."
        )
        BulletText(
            "쓰기 200회: apply 23ms / 0ms · commit 268~277ms / 191~226ms · updateData 200번 361~381ms / 186~199ms(파일 쓰기 200회) · " +
                "한 transform 에 묶으면 2ms / 0ms(파일 쓰기 1회)."
        )
        BulletText(
            "손상: SP 는 예외 없이 getAll() = {} 로 넘어갔고, 이어진 commit 이 새 값 하나만으로 파일을 다시 써 이전 값은 흔적 없이 사라졌다. " +
                "DataStore 기본 처리기는 파일 경로가 든 CorruptionException 을 올리고 파일(9바이트)을 그대로 두었다 — 복구는 ReplaceFileCorruptionHandler 를 줬을 때만."
        )
        BulletText(
            "이관: 전체 키면 SP 파일까지 지워졌고, keysToMigrate 면 옮긴 키만 빠지고 나머지(dark·tags)는 SP 에 남았다. " +
                "이관은 한 번만 도는 표시가 아니라 열 때마다 'SP 에 옮길 키가 남았나'로 판단한다 — 일부 키만 옮긴 파일을 전체 키 이관으로 다시 열었더니 남아 있던 dark·tags 가 그때 옮겨졌다."
        )
        BulletText("같은 파일: A 가 살아 있는 동안 B 의 첫 읽기가 IllegalStateException(경로 포함). A 의 scope 를 cancelAndJoin 한 뒤에는 C 가 같은 값을 읽었다.")
        BulletText(
            "HotSwan 이 이 ViewModel 을 실행하던 debug 빌드에서는 4번의 ReThrow 결과가 UndeclaredThrowableException ← CorruptionException 으로 한 겹 더 감싸여 왔다 — " +
                "catch (e: IOException) 이 놓치는 모양이라 이 ViewModel 은 HotSwan 에서 뺐다(이제 debug 도 CorruptionException 그대로)."
        )
        BulletText("시간은 debug 빌드가 3~8배 느리다(HotSwan 에서 빼도 같다 — 디버깅 가능 빌드의 차이). 위 값은 release 기준이다.")
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
private fun StoreCard(title: String, content: @Composable () -> Unit) {
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
