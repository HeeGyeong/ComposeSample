package com.example.composesample.presentation.example.component.architecture.pattern.remember

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.savedstate.compose.serialization.serializers.SnapshotStateSetSerializer
import com.example.composesample.presentation.MainHeader
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer

@Composable
fun RememberPatternsExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 상단 헤더
        MainHeader(
            title = "Remember Patterns",
            onBackIconClicked = onBackEvent
        )

        HorizontalDivider()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item { RememberSaveableSection() }
            item { HorizontalDivider(thickness = 2.dp) }
            item { RememberSerializableSection() }
            item { HorizontalDivider(thickness = 2.dp) }
            item { RememberUpdatedStateSection() }
            item { HorizontalDivider(thickness = 2.dp) }
            item { DerivedStateOfSection() }
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

// ─────────────────────────────────────────────────────────
// 섹션 1: rememberSaveable
// ─────────────────────────────────────────────────────────

@Composable
private fun RememberSaveableSection() {
    // remember: 리컴포지션 생존 O, 화면 회전 생존 X
    var rememberCount by remember { mutableIntStateOf(0) }
    // rememberSaveable: 리컴포지션 생존 O, 화면 회전 생존 O (Bundle에 저장)
    var saveableCount by rememberSaveable { mutableIntStateOf(0) }

    SectionCard(title = "1. rememberSaveable") {
        Text(
            text = "액티비티가 재생성되면(다크 모드·언어 전환 등) remember 카운터는 0으로 초기화되지만\n" +
                "rememberSaveable 카운터는 값을 유지합니다.\n" +
                "※ 이 앱의 예제 화면은 configChanges 로 회전을 직접 처리해 회전으로는 재생성되지 않는다.\n" +
                "※ LazyColumn 안이라 재생성 순간 이 섹션이 화면에 보여야 저장된다(2번 섹션 참고).",
            fontSize = 13.sp,
            color = Color.DarkGray
        )

        Spacer(modifier = Modifier.height(12.dp))

        // remember 카운터
        CounterRow(
            label = "remember",
            count = rememberCount,
            labelColor = Color(0xFFE53935),
            onIncrement = { rememberCount++ },
            onReset = { rememberCount = 0 }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // rememberSaveable 카운터
        CounterRow(
            label = "rememberSaveable",
            count = saveableCount,
            labelColor = Color(0xFF1E88E5),
            onIncrement = { saveableCount++ },
            onReset = { saveableCount = 0 }
        )

        Spacer(modifier = Modifier.height(12.dp))

        InfoBox(
            text = "rememberSaveable은 내부적으로 savedInstanceState를 활용합니다.\n" +
                    "Bundle에 저장 가능한 타입(Int, String 등)은 자동 직렬화되며,\n" +
                    "커스텀 타입은 Saver를 직접 구현해야 합니다."
        )
    }
}

// ─────────────────────────────────────────────────────────
// 섹션 2: rememberSerializable (runtime-saveable 1.12 · savedstate 1.5)
// ─────────────────────────────────────────────────────────

@Serializable
private enum class SortOrder { NEWEST, PRICE_LOW, PRICE_HIGH }

/** 검색 필터 폼 상태 — kotlinx-serialization 플러그인이 serializer 를 만든다 */
@Serializable
private data class SearchFilter(
    val query: String = "",
    val minPrice: Int = 0,
    val sort: SortOrder = SortOrder.NEWEST,
    // 나중에 추가된 필드라고 가정한다 — 손으로 쓴 Saver 는 이 필드를 아직 모른다
    val tags: List<String> = emptyList()
)

/**
 * rememberSaveable 용 Saver 를 손으로 쓴 경우. tags 필드가 추가됐는데 Saver 갱신을 잊은 상태다.
 * 컴파일도 실행도 그대로 되고, 재생성될 때만 tags 가 조용히 사라진다.
 */
private val SearchFilterManualSaver = mapSaver(
    save = { mapOf("query" to it.query, "minPrice" to it.minPrice, "sort" to it.sort.name) },
    restore = {
        SearchFilter(
            query = it["query"] as String,
            minPrice = it["minPrice"] as Int,
            sort = SortOrder.valueOf(it["sort"] as String)
        )
    }
)

private val QUERY_CYCLE = listOf("", "compose", "kotlin", "android")
private val TAG_CYCLE = listOf("new", "sale", "hot", "pick")
private val COLOR_CHIPS = listOf("빨강", "초록", "파랑")

@Composable
private fun RememberSerializableSection() {
    // A: rememberSaveable + 손으로 쓴 Saver / B: rememberSerializable — 같은 조작을 둘 다에 적용한다
    var filterA by rememberSaveable(stateSaver = SearchFilterManualSaver) { mutableStateOf(SearchFilter()) }
    var filterB by rememberSerializable { mutableStateOf(SearchFilter()) }

    // 기본 타입은 플러그인 없이도 내장 serializer 로 된다
    var serialCount by rememberSerializable { mutableIntStateOf(0) }

    // mutableStateSetOf 는 SnapshotStateSetSerializer(savedstate 1.5)로 kotlinx-serialization 인코딩해 저장한다.
    // ⚠️ rememberSaveable { mutableStateSetOf() } 는 canBeSaved=true(Android 의 SnapshotStateSet 은 Parcelable)라 받아 주지만,
    //    Compose runtime 1.12.1 의 SnapshotStateSet.writeToParcel 이 원소를 첫 번째 하나만 쓴다(if/while 버그, 1.13.0-alpha03 에서 수정).
    //    원소가 2개 이상이면 프로세스 종료 후 복원에서 Bundle 이 어긋난다 — 그래서 이 화면은 그 경로로 저장하지 않는다.
    val colorsSerial = rememberSerializable(serializer = SnapshotStateSetSerializer(String.serializer())) {
        mutableStateSetOf<String>()
    }

    // rememberSaveable 의 기본 Saver 가 이 값들을 받는지 — 직접 넣으면 등록 순간 예외라 질의만 한다
    val registry = LocalSaveableStateRegistry.current
    val setSavable = remember(registry) { registry?.canBeSaved(mutableStateSetOf<String>()) }
    val dataClassSavable = remember(registry) { registry?.canBeSaved(SearchFilter()) }

    fun update(change: (SearchFilter) -> SearchFilter) {
        filterA = change(filterA)
        filterB = change(filterB)
    }

    SectionCard(title = "2. rememberSerializable") {
        Text(
            text = "Saver 를 쓰지 않고 @Serializable 타입을 그대로 저장한다(runtime-saveable 1.12).\n" +
                "아래 버튼으로 필터를 바꾼 뒤 다크 모드를 전환해 액티비티를 재생성해 보라.",
            fontSize = 13.sp,
            color = Color.DarkGray
        )
        Spacer(modifier = Modifier.height(10.dp))
        CodeText(
            "@Serializable data class SearchFilter(…, val tags: List<String>)\n" +
                "var filter by rememberSerializable { mutableStateOf(SearchFilter()) }\n\n" +
                "val set = rememberSerializable(\n" +
                "    serializer = SnapshotStateSetSerializer(String.serializer())\n" +
                ") { mutableStateSetOf<String>() }"
        )

        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SmallButton("검색어") {
                update { it.copy(query = QUERY_CYCLE[(QUERY_CYCLE.indexOf(it.query) + 1) % QUERY_CYCLE.size]) }
            }
            SmallButton("최소가 +1000") { update { it.copy(minPrice = it.minPrice + 1000) } }
            SmallButton("정렬") {
                update { it.copy(sort = SortOrder.entries[(it.sort.ordinal + 1) % SortOrder.entries.size]) }
            }
            SmallButton("태그 +") {
                update { it.copy(tags = it.tags + TAG_CYCLE[it.tags.size % TAG_CYCLE.size]) }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        SmallButton("필터 초기화", color = Color.Gray) { update { SearchFilter() } }

        Spacer(modifier = Modifier.height(10.dp))
        FilterBox("A. rememberSaveable + 손으로 쓴 Saver(tags 누락)", filterA, Color(0xFFFFEBEE))
        Spacer(modifier = Modifier.height(6.dp))
        FilterBox("B. rememberSerializable", filterB, Color(0xFFE8F5E9))

        Spacer(modifier = Modifier.height(12.dp))
        Text("mutableStateSetOf 저장 — SnapshotStateSetSerializer", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF37474F))
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            COLOR_CHIPS.forEach { chip ->
                val selected = chip in colorsSerial
                SmallButton(
                    text = if (selected) "✓ $chip" else chip,
                    color = if (selected) Color(0xFF00897B) else Color(0xFFB0BEC5)
                ) { if (selected) colorsSerial.remove(chip) else colorsSerial.add(chip) }
            }
        }
        Text(
            text = "rememberSerializable(SnapshotStateSetSerializer): ${COLOR_CHIPS.filter { it in colorsSerial }.ifEmpty { listOf("없음") }.joinToString()}",
            fontSize = 12.sp,
            color = Color(0xFF00695C)
        )
        Spacer(modifier = Modifier.height(6.dp))
        WarningBox(
            text = "rememberSaveable { mutableStateSetOf() } 는 받아 주지만(canBeSaved=$setSavable) 1.12.1 에서는 쓰지 말 것.\n" +
                "SnapshotStateSet.writeToParcel 이 개수는 N 으로 쓰고 원소는 첫 번째 하나만 쓴다(1.13.0-alpha03 에서 수정).\n" +
                "실측(원소 2개 → 홈 → 프로세스 종료 → 복귀): release 는 [빨강, null] 로 파랑 유실 + 뒤 항목까지 Parcel 경고, " +
                "debug 는 BadParcelableException 으로 화면이 비었다. 원소 1개·다크 모드 재생성(직렬화 없음)은 멀쩡하다."
        )

        Spacer(modifier = Modifier.height(8.dp))
        CounterRow(
            label = "rememberSerializable(Int)",
            count = serialCount,
            labelColor = Color(0xFF6A1B9A),
            onIncrement = { serialCount++ },
            onReset = { serialCount = 0 }
        )

        Spacer(modifier = Modifier.height(12.dp))
        InfoBox(
            text = "rememberSaveable 의 기본 Saver 가 받는가(canBeSaved):\n" +
                "• @Serializable 데이터 클래스 → $dataClassSavable(Bundle 에 넣을 수 없다 — Saver 를 쓰거나 rememberSerializable)\n" +
                "• mutableStateSetOf() → $setSavable(Android 의 SnapshotStateSet 은 Parcelable — 위 경고 참고)\n" +
                "rememberSerializable 은 kotlinx-serialization 으로 SavedState 에 인코딩하므로 Saver 가 필요 없고, " +
                "필드가 늘어도 serializer 가 같이 생성된다. 직접 만든 클래스는 @Serializable + 컴파일러 플러그인, " +
                "기본 타입은 내장 serializer 로 된다.\n" +
                "※ LazyColumn 항목 안의 saveable 상태는 재생성 순간 화면에 보이는 항목만 저장된다" +
                "(LazySaveableStateHolder — Bundle 크기 제한 대비). 1번 섹션을 화면 밖에 둔 채 재생성하면 그 카운터는 0 이 된다."
        )
    }
}

@Composable
private fun FilterBox(label: String, filter: SearchFilter, color: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
        Text(
            text = "query=\"${filter.query}\" · minPrice=${filter.minPrice} · sort=${filter.sort}\n" +
                "tags=${filter.tags.ifEmpty { listOf("없음") }.joinToString()}",
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            color = Color.DarkGray,
            lineHeight = 17.sp
        )
    }
}

@Composable
private fun WarningBox(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFEBEE), RoundedCornerShape(8.dp))
            .padding(10.dp),
        fontSize = 11.sp,
        color = Color(0xFFB71C1C),
        lineHeight = 16.sp
    )
}

@Composable
private fun SmallButton(text: String, color: Color = Color(0xFF1E88E5), onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
    ) { Text(text, fontSize = 11.sp) }
}

@Composable
private fun CodeText(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF263238), RoundedCornerShape(6.dp))
            .padding(10.dp),
        fontSize = 10.sp,
        fontFamily = FontFamily.Monospace,
        color = Color(0xFFECEFF1),
        lineHeight = 15.sp
    )
}

// ─────────────────────────────────────────────────────────
// 섹션 2: rememberUpdatedState
// ─────────────────────────────────────────────────────────

@Composable
private fun RememberUpdatedStateSection() {
    var message by remember { mutableStateOf("초기 메시지") }
    var logWithoutUpdated by remember { mutableStateOf("대기 중...") }
    var logWithUpdated by remember { mutableStateOf("대기 중...") }
    var triggerCount by remember { mutableIntStateOf(0) }

    /**
     * ❌ 문제 패턴: LaunchedEffect 안에서 message를 직접 캡처
     * key=Unit → 최초 1회만 실행. 이후 message가 바뀌어도 이펙트 안의 값은 업데이트되지 않음.
     */
    val capturedMessage = message // 캡처 시점의 값
    LaunchedEffect(triggerCount) {
        delay(2000)
        logWithoutUpdated = "2초 후 캡처된 값: \"$capturedMessage\""
    }

    /**
     * ✅ 올바른 패턴: rememberUpdatedState로 감싸면
     * LaunchedEffect가 재시작되지 않아도 항상 최신 message를 참조
     */
    val updatedMessage by rememberUpdatedState(message)
    LaunchedEffect(triggerCount) {
        delay(2000)
        logWithUpdated = "2초 후 최신 값: \"$updatedMessage\""
    }

    SectionCard(title = "3. rememberUpdatedState") {
        Text(
            text = "LaunchedEffect 실행 중에 외부 값이 바뀌면 어떻게 될까요?\n" +
                    "아래에서 메시지를 바꾸고 '이펙트 트리거' 버튼을 누른 뒤,\n" +
                    "2초 안에 메시지를 다시 변경해보세요.",
            fontSize = 13.sp,
            color = Color.DarkGray
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { message = "변경된 메시지 A" },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B1FA2))
            ) { Text("메시지 A로 변경", fontSize = 12.sp) }
            Button(
                onClick = { message = "변경된 메시지 B" },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B1FA2))
            ) { Text("메시지 B로 변경", fontSize = 12.sp) }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Button(
            onClick = { triggerCount++ },
            modifier = Modifier.fillMaxWidth()
        ) { Text("이펙트 트리거 (2초 후 결과 표시)") }

        Spacer(modifier = Modifier.height(8.dp))

        Text("현재 메시지: \"$message\"", fontSize = 13.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(8.dp))

        ResultBox(label = "❌ 일반 캡처", result = logWithoutUpdated, color = Color(0xFFFFEBEE))
        Spacer(modifier = Modifier.height(4.dp))
        ResultBox(label = "✅ rememberUpdatedState", result = logWithUpdated, color = Color(0xFFE3F2FD))

        Spacer(modifier = Modifier.height(12.dp))

        InfoBox(
            text = "rememberUpdatedState는 내부적으로 SideEffect로 값을 업데이트합니다.\n" +
                    "타이머, 애니메이션, 콜백 등 오래 실행되는 이펙트에서 유용합니다."
        )
    }
}

// ─────────────────────────────────────────────────────────
// 섹션 3: derivedStateOf
// ─────────────────────────────────────────────────────────

@Composable
private fun DerivedStateOfSection() {
    var count by remember { mutableIntStateOf(0) }

    /**
     * ✅ 올바른 패턴: remember { derivedStateOf { } }
     * count가 바뀌어도 isEven의 결과가 실제로 달라질 때만 리컴포지션 발생
     */
    val isEven by remember { derivedStateOf { count % 2 == 0 } }

    /**
     * ✅ 올바른 패턴: 조건 계산 최적화
     * count가 0~4 구간에서 변할 때 buttonEnabled 값이 바뀌지 않으면 리컴포지션 안 함
     */
    val buttonEnabled by remember { derivedStateOf { count >= 5 } }

    /**
     * ❌ 잘못된 패턴 (주석): remember(count) { derivedStateOf { } }
     * key로 count를 넘기면 count가 바뀔 때마다 derivedStateOf 블록 자체가 재생성됨.
     * derivedStateOf의 스마트 재계산 효과가 사라지고, 오히려 일반 remember(count)와 동일해짐.
     */

    SectionCard(title = "4. derivedStateOf") {
        Text(
            text = "다른 State에서 계산된 값은 derivedStateOf로 감싸세요.\n" +
                    "결과가 실제로 달라질 때만 리컴포지션이 발생합니다.",
            fontSize = 13.sp,
            color = Color.DarkGray
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text("현재 카운트: $count", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))

        // isEven: count가 홀수↔짝수 경계를 넘을 때만 리컴포지션
        Text(
            text = "짝수 여부 (isEven): $isEven",
            fontSize = 14.sp,
            color = if (isEven) Color(0xFF1E88E5) else Color(0xFFE53935)
        )
        Spacer(modifier = Modifier.height(4.dp))

        // buttonEnabled: count < 5 구간에서는 아무리 변해도 리컴포지션 없음
        Text(
            text = "버튼 활성화 (count >= 5): $buttonEnabled",
            fontSize = 14.sp,
            color = if (buttonEnabled) Color(0xFF43A047) else Color.Gray
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { count++ }) { Text("+1") }
            Button(onClick = { count-- }) { Text("-1") }
            Button(
                onClick = { count = 0 },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) { Text("리셋") }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { /* count >= 5 일 때만 활성화 */ },
            enabled = buttonEnabled,
            modifier = Modifier.fillMaxWidth()
        ) { Text("5 이상일 때만 활성화되는 버튼") }

        Spacer(modifier = Modifier.height(12.dp))

        InfoBox(
            text = "❌ 잘못된 패턴:\n" +
                    "  remember(count) { derivedStateOf { count % 2 == 0 } }\n" +
                    "  → key가 바뀔 때마다 블록이 재생성되어 최적화 효과 없음\n\n" +
                    "✅ 올바른 패턴:\n" +
                    "  remember { derivedStateOf { count % 2 == 0 } }\n" +
                    "  → count 변화를 내부에서 감지, 결과가 바뀔 때만 리컴포지션"
        )
    }
}

// ─────────────────────────────────────────────────────────
// 공통 컴포넌트
// ─────────────────────────────────────────────────────────

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8F9FA), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun CounterRow(
    label: String,
    count: Int,
    labelColor: Color,
    onIncrement: () -> Unit,
    onReset: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label: $count",
            fontSize = 14.sp,
            color = labelColor,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Button(
                onClick = onIncrement,
                colors = ButtonDefaults.buttonColors(containerColor = labelColor)
            ) { Text("+1", fontSize = 12.sp) }
            Button(
                onClick = onReset,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) { Text("리셋", fontSize = 12.sp) }
        }
    }
}

@Composable
private fun ResultBox(label: String, result: String, color: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
        Text(result, fontSize = 13.sp, color = Color.DarkGray)
    }
}

@Composable
private fun InfoBox(text: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFF9C4), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            color = Color(0xFF5D4037),
            lineHeight = 18.sp
        )
    }
}
