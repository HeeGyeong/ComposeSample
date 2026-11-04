package com.example.composesample.presentation.example.component.ui.material3

import android.annotation.SuppressLint
import android.os.Build
import android.text.format.DateFormat
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.TimePickerDialogDefaults
import androidx.compose.material3.TimePickerDisplayMode
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader
import com.example.composesample.util.boldMarkup

/**
 * M3 날짜·시간 선택기 예제 — DatePicker · DateRangePicker · TimePicker 와 그 값의 단위.
 * - M3 의 날짜 값은 모두 **그날 UTC 자정의 epoch millis** 다(DatePickerState.selectedDateMillis KDoc).
 *   읽을 때 기기 시간대로 바꾸면 UTC 보다 늦은 곳(LA 등)에서 하루 전, 넘길 때 로컬 자정을 주면 UTC 보다 빠른 곳(서울 등)에서 하루 전.
 * - SelectableDates 도 칸마다 UTC 자정 millis 를 받는다 → '지금'과 millis 로 비교하면 오늘이 막히고, 기기 시간대로 요일을 읽으면 주말이 밀린다.
 * - TimePickerState.hour 는 늘 0..23 — 날짜 millis 에 시 · 분을 더하면 UTC 벽시계 시각이 된다.
 * - 변환 · 판정은 같은 폴더의 DateTimePickerLab.kt(JVM 단위 테스트 DateTimePickerLabTest 와 공유, java.time = API 26+).
 * - 참고 자료(URL/핵심 개념)는 같은 폴더의 exampleGuide.kt 의 "날짜·시간 선택기" 섹션 참고.
 */
// ObsoleteSdkInt 억제: debug 는 HotSwan 때문에 minSdk 26 이라 아래 분기를 불필요하다고 보지만,
// release 는 minSdk 24 라 java.time 을 쓰는 DateTimePickerLab 을 이 분기가 막아야 한다.
@SuppressLint("ObsoleteSdkInt")
@Composable
fun DateTimePickerExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "M3 날짜 · 시간 선택기",
            onBackIconClicked = onBackEvent
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DateTimePickerContent()
        } else {
            UnsupportedNotice()
        }
    }
}

@Composable
private fun DateTimePickerContent() {
    // 1번 카드에서 고른 날짜 — 5번(시간) 카드가 이 날짜에 시각을 합친다
    var pickedDateMillis by rememberSaveable { mutableStateOf<Long?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { OverviewCard() }
        item { DatePickerCard(pickedDateMillis) { pickedDateMillis = it } }
        item { InitialValueCard() }
        item { SelectableDatesCard() }
        item { DateRangeCard() }
        item { TimePickerCard(pickedDateMillis) }
        item { ChecklistCard() }
    }
}

// ==================== 0. 개요 ====================

@Composable
private fun OverviewCard() {
    SectionCard(title = "날짜 값은 'UTC 자정'") {
        BodyText(
            "M3 날짜 선택기는 날짜를 **그날 UTC 자정의 millis** 로 주고받는다. 날짜(달력의 칸)를 순간(시각)처럼 다루면 " +
                "시간대에 따라 하루가 밀린다 — 서울(UTC+9)과 LA(UTC-8)에서 밀리는 방향이 반대다."
        )
        Spacer(modifier = Modifier.height(6.dp))
        TableRow("API", "material3 1.4.0", isHeader = true)
        TableRow("DatePicker · DatePickerDialog", "안정 — DisplayMode(달력 · 입력), SelectableDates")
        TableRow("DateRangePicker", "안정 — selectedStart/EndDateMillis")
        TableRow("TimePicker · TimeInput", "@ExperimentalMaterial3Api — TimePickerState(hour 0..23)")
        TableRow("TimePickerDialog", "1.4.0 신규 · 안정 — 모드 전환 버튼(TimePickerDialogDefaults.DisplayModeToggle)")
        Spacer(modifier = Modifier.height(6.dp))
        CodeBlock(
            "// 읽기 — UTC 로\n" +
                "Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()\n" +
                "// 넘기기 — 그 날짜의 UTC 자정\n" +
                "date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()"
        )
        CaptionText("이 기기 시간대: ${deviceZoneName()} · 언어: ${LocalConfiguration.current.locales[0].toLanguageTag()}")
    }
}

// ==================== 1. 고른 날짜 읽기 ====================

@Composable
private fun DatePickerCard(pickedDateMillis: Long?, onPicked: (Long?) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }

    SectionCard(title = "1. 고른 날짜를 읽을 때 — 기기 시간대로 바꾸지 않는다") {
        BodyText("날짜를 고른 뒤 같은 millis 를 세 방식으로 읽는다. 달력 · 입력 모드는 다이얼로그 오른쪽 위 버튼으로 바꾼다.")
        Spacer(modifier = Modifier.height(8.dp))
        DemoButton(text = "날짜 고르기", color = Color(0xFF1565C0)) { showDialog = true }
        Spacer(modifier = Modifier.height(6.dp))
        if (pickedDateMillis == null) {
            CaptionText("아직 고른 날짜가 없다.")
        } else {
            val report = readReport(pickedDateMillis)
            ResultRow("millis", "$pickedDateMillis")
            ResultRow("UTC 로", "${report.utc}  ← 고른 날")
            ResultRow("기기 시간대", "${report.device}${report.deviceShift}")
            ResultRow("LA 로", "${report.la}${report.laShift}")
            CaptionText(
                "기기 시간대가 UTC 보다 빠르면(서울) 기기 시간대로 읽어도 같은 날이라 버그가 숨는다. **같은 코드가 LA 사용자에게는 하루 전**을 보인다."
            )
        }
    }

    if (showDialog) {
        val state = rememberDatePickerState(initialSelectedDateMillis = pickedDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        onPicked(state.selectedDateMillis)
                        showDialog = false
                    }
                ) { Text("확인") }
            },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("취소") } }
        ) {
            DatePicker(state = state)
        }
    }
}

// ==================== 2. 초기값 넘기기 ====================

@Composable
private fun InitialValueCard() {
    var zone by remember { mutableStateOf(SampleZone.SEOUL) }
    var openWith by remember { mutableStateOf<DatePickerState?>(null) }
    val locale = LocalConfiguration.current.locales[0]
    val values = remember(zone) { initialValues(zone, System.currentTimeMillis()) }
    // 실제 M3 상태 — 넘긴 값을 라이브러리가 어떻게 '그날의 시작'으로 맞추는지 그대로 본다
    val fromLocal = remember(values, locale) { DatePickerState(locale, initialSelectedDateMillis = values.localMidnight) }
    val fromUtc = remember(values, locale) { DatePickerState(locale, initialSelectedDateMillis = values.utcMidnight) }
    val localPicked = fromLocal.selectedDateMillis ?: 0L
    val utcPicked = fromUtc.selectedDateMillis ?: 0L

    SectionCard(title = "2. 초기값을 넘길 때 — 로컬 자정이 아니라 UTC 자정") {
        BodyText(
            "'오늘'을 미리 골라 두려고 그 시간대의 자정을 넘기면, M3 는 그 순간을 **UTC 날짜**로 맞춘다(CalendarModelImpl.getCanonicalDate). " +
                "서울 자정은 전날 15:00 UTC 라 어제가 선택된다."
        )
        Spacer(modifier = Modifier.height(6.dp))
        ZoneChips(zone) { zone = it }
        ResultRow("오늘", "${values.today} (${zone.label})")
        ResultRow("로컬 자정", "${values.localMidnight} → M3 선택 ${utcDateText(localPicked)}${shiftFrom(localPicked, values.today)}")
        ResultRow("UTC 자정", "${values.utcMidnight} → M3 선택 ${utcDateText(utcPicked)}${shiftFrom(utcPicked, values.today)}")
        CaptionText("오른쪽 날짜는 실제 DatePickerState(locale, initialSelectedDateMillis).selectedDateMillis 를 UTC 로 읽은 값이다.")
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DemoButton(text = "로컬 자정으로 열기", color = Color(0xFFC62828)) { openWith = fromLocal }
            DemoButton(text = "UTC 자정으로 열기", color = Color(0xFF2E7D32)) { openWith = fromUtc }
        }
    }

    openWith?.let { state ->
        DatePickerDialog(
            onDismissRequest = { openWith = null },
            confirmButton = { TextButton(onClick = { openWith = null }) { Text("닫기") } }
        ) {
            DatePicker(state = state)
        }
    }
}

@Composable
private fun ZoneChips(selected: SampleZone, onSelect: (SampleZone) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SampleZone.entries.forEach { z ->
            FilterChip(
                selected = selected == z,
                onClick = { onSelect(z) },
                label = { Text(z.label, fontSize = 11.sp) }
            )
        }
    }
}

// ==================== 3. SelectableDates ====================

/** 흔한 잘못 — 칸 millis 와 '지금' 비교 + 지정 시간대로 요일. */
private class NowCompareDates(private val nowMillis: Long, private val zone: SampleZone) : SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long): Boolean = isSelectableWrong(utcTimeMillis, nowMillis, zone)
}

/** 바름 — 칸을 UTC 날짜로 바꿔 '오늘'(지정 시간대의 날짜)과 날짜끼리. */
private class DateCompareDates(private val nowMillis: Long, private val zone: SampleZone) : SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long): Boolean = isSelectableRight(utcTimeMillis, nowMillis, zone)
}

@Composable
private fun SelectableDatesCard() {
    var zone by remember { mutableStateOf(SampleZone.SEOUL) }
    var openRule by remember { mutableStateOf<SelectableDates?>(null) }
    val now = remember(zone) { System.currentTimeMillis() }
    val rules = remember(zone, now) { weekRules(now, zone) }
    val wrongRule = remember(zone, now) { NowCompareDates(now, zone) }
    val rightRule = remember(zone, now) { DateCompareDates(now, zone) }

    SectionCard(title = "3. SelectableDates — 지난 날짜 · 주말 막기") {
        BodyText(
            "isSelectableDate(utcTimeMillis) 는 칸마다 **그날 UTC 자정**을 받는다. 'utcTimeMillis >= 지금' 은 오늘 칸을 막고(UTC 자정은 대개 지금보다 이르다), " +
                "요일을 기기 시간대로 읽으면 UTC 보다 늦은 곳에서 주말이 하루 앞당겨진다."
        )
        Spacer(modifier = Modifier.height(6.dp))
        ZoneChips(zone) { zone = it }
        ResultRow("지금", "${wallClockOf(now, zone)} (${zone.label})")
        TableRow("날짜 · 요일", "잘못된 규칙 / 바른 규칙", isHeader = true)
        rules.forEach { r ->
            TableRow(
                "${r.date} ${r.dayOfWeek}",
                "${mark(r.wrong)} / ${mark(r.right)}${if (r.wrong != r.right) "   ← 다름" else ""}"
            )
        }
        CaptionText("○ 고를 수 있음 · ✕ 막힘. '바른 규칙'은 오늘부터 열고 토 · 일을 막는다.")
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DemoButton(text = "잘못된 규칙으로 열기", color = Color(0xFFC62828)) { openRule = wrongRule }
            DemoButton(text = "바른 규칙으로 열기", color = Color(0xFF2E7D32)) { openRule = rightRule }
        }
    }

    openRule?.let { rule ->
        val state = rememberDatePickerState(selectableDates = rule)
        DatePickerDialog(
            onDismissRequest = { openRule = null },
            confirmButton = { TextButton(onClick = { openRule = null }) { Text("닫기") } }
        ) {
            DatePicker(state = state)
        }
    }
}

private fun mark(selectable: Boolean) = if (selectable) "○" else "✕"

// ==================== 4. DateRangePicker ====================

@Composable
private fun DateRangeCard() {
    var showDialog by remember { mutableStateOf(false) }
    var start by rememberSaveable { mutableStateOf<Long?>(null) }
    var end by rememberSaveable { mutableStateOf<Long?>(null) }

    SectionCard(title = "4. DateRangePicker — 포함 일수와 박 수") {
        BodyText("시작 · 끝 두 값도 UTC 자정이다. 11/4~11/7 은 4일 · 3박 — '끝 - 시작' 은 박 수다. 끝을 고르지 않고 확인하면 끝은 null.")
        Spacer(modifier = Modifier.height(8.dp))
        DemoButton(text = "기간 고르기", color = Color(0xFF6A1B9A)) { showDialog = true }
        Spacer(modifier = Modifier.height(6.dp))
        val summary = summarizeRange(start, end)
        if (summary == null) {
            CaptionText("아직 고른 기간이 없다.")
        } else {
            ResultRow("시작", summary.start)
            ResultRow("끝", summary.end ?: "없음 (시작만 고르고 확인)")
            ResultRow("길이", if (summary.days == null) "—" else "${summary.days}일 · ${summary.nights}박")
        }
    }

    if (showDialog) {
        val state = rememberDateRangePickerState(initialSelectedStartDateMillis = start, initialSelectedEndDateMillis = end)
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        start = state.selectedStartDateMillis
                        end = state.selectedEndDateMillis
                        showDialog = false
                    }
                ) { Text("확인") }
            },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("취소") } }
        ) {
            DateRangePicker(state = state)
        }
    }
}

// ==================== 5. TimePicker ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerCard(pickedDateMillis: Long?) {
    val context = LocalContext.current
    val systemIs24Hour = remember { DateFormat.is24HourFormat(context) }
    var is24Hour by remember { mutableStateOf(systemIs24Hour) }
    var showDialog by remember { mutableStateOf(false) }
    var picked by rememberSaveable { mutableStateOf<Pair<Int, Int>?>(null) }

    SectionCard(title = "5. TimePicker — 날짜에 시각을 합칠 때") {
        BodyText(
            "TimePickerState.hour 는 12시간 표기여도 **0..23** 이다. 1번 카드의 날짜(UTC 자정)에 시 · 분을 millis 로 더하면 " +
                "그 시각은 **UTC 벽시계**가 된다 — 기기 시간대 오프셋만큼 어긋난다."
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = is24Hour, onClick = { is24Hour = true }, label = { Text("24시간", fontSize = 11.sp) })
            FilterChip(selected = !is24Hour, onClick = { is24Hour = false }, label = { Text("12시간", fontSize = 11.sp) })
        }
        CaptionText("기기 설정: ${if (systemIs24Hour) "24시간" else "12시간"}(rememberTimePickerState 의 기본값이 따르는 값)")
        Spacer(modifier = Modifier.height(6.dp))
        DemoButton(text = "시간 고르기", color = Color(0xFF00838F)) { showDialog = true }
        Spacer(modifier = Modifier.height(6.dp))
        val time = picked
        if (time == null) {
            CaptionText("아직 고른 시간이 없다.")
        } else {
            val (hour, minute) = time
            val report = timeReport(pickedDateMillis, hour, minute, System.currentTimeMillis())
            ResultRow("state", "hour $hour · minute $minute (${twelveHourLabel(hour, minute)})")
            ResultRow("날짜", "${report.date}${if (pickedDateMillis == null) " (1번 카드 미선택 → 오늘)" else ""}")
            ResultRow("바름", "${report.right}  ← LocalDate.atTime(h, m).atZone(기기)")
            ResultRow("잘못", "${report.wrong}  ← millis + h · 3600000 + m · 60000")
            ResultRow("차이", "${report.diffHours}시간 (${report.zone})")
        }
    }

    if (showDialog) {
        val state = rememberTimePickerState(initialHour = picked?.first ?: 9, initialMinute = picked?.second ?: 0, is24Hour = is24Hour)
        var displayMode by remember { mutableStateOf(TimePickerDisplayMode.Picker) }
        TimePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        picked = state.hour to state.minute
                        showDialog = false
                    }
                ) { Text("확인") }
            },
            title = { TimePickerDialogDefaults.Title(displayMode = displayMode) },
            modeToggleButton = {
                TimePickerDialogDefaults.DisplayModeToggle(
                    onDisplayModeChange = {
                        displayMode = if (displayMode == TimePickerDisplayMode.Picker) TimePickerDisplayMode.Input else TimePickerDisplayMode.Picker
                    },
                    displayMode = displayMode
                )
            },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("취소") } }
        ) {
            if (displayMode == TimePickerDisplayMode.Picker) TimePicker(state = state) else TimeInput(state = state)
        }
    }
}

// ==================== 6. 정리 ====================

@Composable
private fun ChecklistCard() {
    SectionCard(title = "정리 — 날짜는 날짜로, 시각은 시간대와 함께") {
        PitfallRow(
            "selectedDateMillis 를 어떻게 읽는가?",
            "UTC 로(ZoneOffset.UTC) LocalDate 를 꺼낸다. 기기 시간대 · 기본 시간대 SimpleDateFormat 은 UTC 보다 늦은 곳에서 하루 전."
        )
        PitfallRow(
            "initialSelectedDateMillis 에 무엇을 넘기는가?",
            "그 날짜의 UTC 자정. 로컬 자정은 UTC 보다 빠른 곳(서울)에서 하루 전이 선택된다."
        )
        PitfallRow(
            "SelectableDates 가 millis 를 '지금'과 비교하지 않는가?",
            "칸을 UTC 날짜로 바꾸고 오늘(사용자 시간대 날짜)과 날짜끼리 비교한다. 요일도 UTC 날짜에서."
        )
        PitfallRow(
            "날짜와 시각을 millis 덧셈으로 합치지 않는가?",
            "LocalDate.atTime(hour, minute).atZone(사용자 시간대) — TimePickerState.hour 는 늘 0..23."
        )
        PitfallRow(
            "기간 길이는 며칠인가, 몇 박인가?",
            "끝 - 시작 = 박 수, 포함 일수는 +1. 끝 날짜 없이 확인하면 끝은 null."
        )
    }
}

@Composable
private fun UnsupportedNotice() {
    Column(modifier = Modifier.padding(16.dp)) {
        SectionCard(title = "API 26 미만") {
            BodyText(
                "이 예제의 변환 코드는 java.time(API 26+)을 쓴다. 프로젝트는 core library desugaring 을 켜지 않았다 — 현재 기기 API ${Build.VERSION.SDK_INT}."
            )
        }
    }
}

// ==================== 공용 UI ====================

@Composable
private fun SectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun BodyText(text: String) {
    Text(text = text.boldMarkup(), fontSize = 13.sp, color = Color(0xFF424242), lineHeight = 19.sp)
}

@Composable
private fun CaptionText(text: String) {
    Text(text = text.boldMarkup(), fontSize = 11.sp, color = Color(0xFF757575), lineHeight = 16.sp)
}

/** 코드는 줄이 길어 접히면 읽기 어렵다 → 가로 스크롤로 원문 모양을 유지한다. */
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

@Composable
private fun TableRow(
    first: String,
    second: String,
    isHeader: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isHeader) Color(0xFFEEEEEE) else Color.Transparent)
            .padding(vertical = 5.dp, horizontal = 6.dp)
    ) {
        Text(
            text = first,
            modifier = Modifier.width(128.dp),
            fontSize = 11.sp,
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
            color = Color(0xFF424242)
        )
        Text(
            text = second,
            modifier = Modifier.weight(1f),
            fontSize = 11.sp,
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
            color = Color(0xFF616161),
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun ResultRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.width(72.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF37474F)
        )
        Text(
            text = value,
            modifier = Modifier.weight(1f),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF616161),
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun PitfallRow(title: String, description: String) {
    Column(modifier = Modifier.padding(bottom = 10.dp)) {
        Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF37474F))
        Spacer(modifier = Modifier.height(2.dp))
        CaptionText(description)
    }
}

@Composable
private fun DemoButton(
    text: String,
    color: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text = text, fontSize = 12.sp, color = Color.White)
    }
}
