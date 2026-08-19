package com.example.composesample.presentation.example.component.architecture.development.time

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader
import kotlinx.coroutines.delay
import kotlin.time.Clock
import kotlin.time.ComparableTimeMark
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.time.TestTimeSource
import kotlin.time.TimeSource
import kotlin.time.TimedValue
import kotlin.time.measureTimedValue

// 실측 비교용 실제 연산 — sum of squares. 기기별로 다르지만 보통 서브밀리초라 Long ms 로는 0 이 찍히기 쉽다
private fun sumOfSquares(limit: Int): Long {
    var acc = 0L
    for (i in 1..limit) acc += i.toLong() * i.toLong()
    return acc
}

@Composable
fun KotlinTimeApiExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "kotlin.time 시간 API",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { ConceptCard() }
            item { WallClockCard() }
            item { MonotonicClockCard() }
            item { MeasureTimeCard() }
            item { TestTimeSourceCard() }
            item { SummaryCard() }
        }
    }
}

@Composable
private fun ConceptCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "kotlin.time 개요",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Kotlin stdlib의 kotlin.time 패키지는 \"현재 몇 시인가\"(벽시계)와 \"얼마나 흘렀는가\"(경과 시간)를 " +
                        "서로 다른 타입으로 분리합니다. 이 예제는 세 가지 축을 실제 실행 결과로 비교합니다.",
                fontSize = 13.sp,
                color = Color(0xFF424242),
                lineHeight = 19.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            val axes = listOf(
                Triple("벽시계 vs 단조시계", "Clock.System / TimeSource.Monotonic", "경과 측정엔 왜 단조시계를 써야 하는가"),
                Triple("Long ms vs Duration", "measureTimedValue()", "왜 Long 밀리초 대신 Duration 인가"),
                Triple("실시간 대기 vs 결정론", "TestTimeSource", "테스트에서 시간을 어떻게 직접 제어하는가")
            )
            axes.forEach { (label, api, note) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(0.34f))
                    Text(text = api, fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF1976D2), modifier = Modifier.weight(0.36f))
                    Text(text = note, fontSize = 10.sp, color = Color(0xFF757575), modifier = Modifier.weight(0.3f))
                }
            }
        }
    }
}

// Clock/Instant 는 kotlin.time 의 실험 단계 API 라 @OptIn 필요 — 이 카드 함수에만 붙여 opt-in 경계를 코드 구조로 드러낸다
@OptIn(ExperimentalTime::class)
@Composable
private fun WallClockCard() {
    var now by remember { mutableStateOf<Instant>(Clock.System.now()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            now = Clock.System.now()
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "① 벽시계 — Clock.System.now()",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF388E3C)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Clock.System.now()는 실제 달력 시각을 kotlin.time.Instant로 돌려줍니다. 1초마다 다시 읽어 화면에 표시합니다.",
                fontSize = 12.sp,
                color = Color(0xFF757575),
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            CodeBlock(
                code = "@OptIn(ExperimentalTime::class)\n" +
                        "val now: Instant = Clock.System.now()\n" +
                        "now.epochSeconds       // Long, 초 단위\n" +
                        "now.toEpochMilliseconds() // Long, 밀리초 단위",
                borderColor = Color(0xFF388E3C)
            )
            Spacer(modifier = Modifier.height(12.dp))

            ResultRow(label = "Instant.toString()", value = now.toString())
            Spacer(modifier = Modifier.height(4.dp))
            ResultRow(label = "epochSeconds", value = now.epochSeconds.toString())
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFF3E0), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "⚠ 벽시계는 NTP 동기화·시간대·사용자가 직접 바꾸면 값이 앞뒤로 튈 수 있습니다. " +
                            "\"얼마나 걸렸는가\"를 재는 용도로는 부적합 — 아래 ②를 사용하세요.",
                    fontSize = 11.sp,
                    color = Color(0xFFE65100),
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun MonotonicClockCard() {
    var mark by remember { mutableStateOf<ComparableTimeMark?>(null) }
    var elapsed by remember { mutableStateOf<Duration?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "② 단조시계 — TimeSource.Monotonic",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF388E3C)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "markNow()로 시작점을 찍고 elapsedNow()로 그 이후 흐른 시간만 잽니다. " +
                        "시스템 벽시계가 바뀌어도 이 값은 영향받지 않습니다 — 안정 API, opt-in 불필요.",
                fontSize = 12.sp,
                color = Color(0xFF757575),
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            CodeBlock(
                code = "val mark = TimeSource.Monotonic.markNow() // opt-in 불필요\n" +
                        "// ... 작업 수행 ...\n" +
                        "val elapsed: Duration = mark.elapsedNow()",
                borderColor = Color(0xFF388E3C)
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    mark = TimeSource.Monotonic.markNow()
                    elapsed = null
                }) {
                    Text(text = "시작점 찍기", fontSize = 12.sp)
                }
                Button(
                    onClick = { elapsed = mark?.elapsedNow() },
                    enabled = mark != null,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))
                ) {
                    Text(text = "경과 측정", fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            ResultRow(label = "markNow() 상태", value = if (mark != null) "찍힘" else "대기 중")
            elapsed?.let {
                Spacer(modifier = Modifier.height(4.dp))
                ResultRow(label = "elapsedNow()", value = it.toString())
            }
        }
    }
}

@Composable
private fun MeasureTimeCard() {
    var result by remember { mutableStateOf<TimedValue<Long>?>(null) }
    var millisFallback by remember { mutableStateOf<Long?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "③ measureTimedValue — Duration 정밀도",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF388E3C)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "같은 연산을 System.currentTimeMillis() 뺄셈과 measureTimedValue()로 동시에 측정해 " +
                        "결과를 나란히 비교합니다. 이 프로젝트는 아직 System.currentTimeMillis()/measureTimeMillis 계열만 " +
                        "쓰고 있어 나노초~마이크로초 구간은 항상 0ms로 잘려 보입니다.",
                fontSize = 12.sp,
                color = Color(0xFF757575),
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            CodeBlock(
                code = "// 기존(Long, 밀리초 단위)\n" +
                        "val start = System.currentTimeMillis()\n" +
                        "doWork()\n" +
                        "val elapsedMs = System.currentTimeMillis() - start\n\n" +
                        "// kotlin.time (Duration, 나노초 정밀도 + 자동 단위 표기)\n" +
                        "val timed: TimedValue<Long> = measureTimedValue { doWork() }\n" +
                        "timed.value    // 연산 결과\n" +
                        "timed.duration // Duration, 예: \"312us\"",
                borderColor = Color(0xFF388E3C)
            )
            Spacer(modifier = Modifier.height(12.dp))

            Button(onClick = {
                val start = System.currentTimeMillis()
                sumOfSquares(500_000)
                millisFallback = System.currentTimeMillis() - start

                result = measureTimedValue { sumOfSquares(500_000) }
            }) {
                Text(text = "1~500,000 제곱합 계산 + 시간 측정", fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))

            millisFallback?.let {
                ResultRow(label = "System.currentTimeMillis() 차이", value = "${it}ms")
                Spacer(modifier = Modifier.height(4.dp))
            }
            result?.let {
                ResultRow(label = "measureTimedValue().duration", value = it.duration.toString())
                Spacer(modifier = Modifier.height(4.dp))
                ResultRow(label = "measureTimedValue().value", value = it.value.toString())
            }
        }
    }
}

// TestTimeSource 는 kotlin.time 의 실험 단계 API 라 @OptIn 필요
@OptIn(ExperimentalTime::class)
@Composable
private fun TestTimeSourceCard() {
    val testTimeSource = remember { TestTimeSource() }
    var mark by remember { mutableStateOf(testTimeSource.markNow()) }
    var elapsed by remember { mutableStateOf(mark.elapsedNow()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "④ TestTimeSource — 실시간 대기 없이 시간 전진",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF388E3C)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "테스트 코드에서 재시도·타임아웃 로직을 실제 delay() 없이 검증하고 싶을 때, " +
                        "TestTimeSource에 원하는 만큼 시간을 더해 elapsedNow() 값을 결정론적으로 바꿀 수 있습니다.",
                fontSize = 12.sp,
                color = Color(0xFF757575),
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            CodeBlock(
                code = "@OptIn(ExperimentalTime::class)\n" +
                        "val testTimeSource = TestTimeSource()\n" +
                        "val mark = testTimeSource.markNow()\n" +
                        "testTimeSource += 5.seconds // 실시간 대기 없이 5초 전진\n" +
                        "mark.elapsedNow() // 5s",
                borderColor = Color(0xFF388E3C)
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    testTimeSource += 1.seconds
                    elapsed = mark.elapsedNow()
                }) {
                    Text(text = "+1초 전진", fontSize = 12.sp)
                }
                Button(onClick = {
                    testTimeSource += 5.seconds
                    elapsed = mark.elapsedNow()
                }) {
                    Text(text = "+5초 전진", fontSize = 12.sp)
                }
                Button(
                    onClick = {
                        mark = testTimeSource.markNow()
                        elapsed = mark.elapsedNow()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF757575))
                ) {
                    Text(text = "기준점 재설정", fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            ResultRow(label = "mark.elapsedNow()", value = elapsed.toString())
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "버튼만 눌렀을 뿐 실제로 몇 초씩 기다리지 않았는데도 경과 시간이 바뀝니다.",
                fontSize = 11.sp,
                color = Color(0xFF757575)
            )
        }
    }
}

@Composable
private fun SummaryCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "핵심 정리",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(8.dp))
            val bullets = listOf(
                "Clock.System.now()/Instant 는 \"지금 몇 시인가\"(벽시계) — 값이 바뀔 수 있어 경과 측정엔 부적합. @OptIn(ExperimentalTime::class) 필요",
                "TimeSource.Monotonic.markNow()/elapsedNow() 는 \"얼마나 흘렀는가\" 전용 단조시계 — 안정 API, opt-in 불필요",
                "measureTimedValue { }는 실행 결과(value)와 소요 시간(duration)을 함께 반환 — Duration은 나노초 단위까지 표현하고 단위를 자동으로 골라 표기",
                "TestTimeSource로 실시간 delay() 없이 시간을 직접 전진시켜 타임아웃/재시도 로직을 결정론적으로 테스트 가능. 마찬가지로 opt-in 필요",
                "새 시간 측정 코드는 System.currentTimeMillis()/kotlin.system.measureTimeMillis 대신 kotlin.time 계열을 우선 검토"
            )
            bullets.forEach { bullet ->
                Row(modifier = Modifier.padding(vertical = 3.dp)) {
                    Text(text = "• ", fontSize = 13.sp, color = Color(0xFF1976D2))
                    Text(text = bullet, fontSize = 12.sp, color = Color(0xFF424242), lineHeight = 17.sp)
                }
            }
        }
    }
}

@Composable
private fun ResultRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFE8F5E9), RoundedCornerShape(6.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "✅ $label", fontSize = 12.sp, color = Color(0xFF388E3C), fontWeight = FontWeight.Medium)
        Text(text = value, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF388E3C))
    }
}

@Composable
private fun CodeBlock(code: String, borderColor: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, borderColor, RoundedCornerShape(8.dp))
            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(
            text = code,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF212121),
            lineHeight = 16.sp
        )
    }
}
