package com.example.composesample.presentation.example.component.architecture.development.init

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.composesample.presentation.MainHeader
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

// stateIn(WhileSubscribed) 의 구독 유지 시간. InitTestViewModel 의 설정값과 동일하게 맞춘다.
private const val SUBSCRIPTION_TIMEOUT_MS = 5000L

@Composable
fun InitTestExampleUI(
    onBackEvent: () -> Unit
) {
    val initTestViewModel: InitTestViewModel = koinViewModel()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Init Test Example",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { ConceptCard() }
            item { TimingDemoCard(viewModel = initTestViewModel) }
            item { WhileSubscribedCard() }
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
                text = "ViewModel 데이터 로딩을 언제 시작할 것인가",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "같은 \"화면 진입 시 데이터 로딩\"이라도 트리거를 어디에 두느냐에 따라 발화 시점과 횟수가 달라집니다. " +
                        "코드만 읽어서는 구분되지 않고, 실제로 구독을 붙였다 떼봐야 차이가 드러납니다.",
                fontSize = 13.sp,
                color = Color(0xFF424242),
                lineHeight = 19.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            PatternRow(
                color = Color(0xFF7B1FA2),
                title = "LaunchedEffect(Unit)",
                desc = "컴포저블이 컴포지션에 진입할 때 UI 가 직접 호출. 화면이 다시 만들어지면 다시 발화"
            )
            Spacer(modifier = Modifier.height(8.dp))
            PatternRow(
                color = Color(0xFFD32F2F),
                title = "init { }  ❌",
                desc = "ViewModel 생성 즉시 1회. 구독 여부와 무관하게 실행되고, 재구독해도 다시 실행되지 않음"
            )
            Spacer(modifier = Modifier.height(8.dp))
            PatternRow(
                color = Color(0xFF388E3C),
                title = "onStart + stateIn(WhileSubscribed)  ✅",
                desc = "첫 구독자가 붙는 순간 발화. 구독이 끊기고 타임아웃이 지난 뒤 다시 붙으면 재발화"
            )
        }
    }
}

@Composable
private fun PatternRow(color: Color, title: String, desc: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Spacer(
            modifier = Modifier
                .width(4.dp)
                .height(44.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
            Text(text = desc, fontSize = 12.sp, color = Color(0xFF616161), lineHeight = 16.sp)
        }
    }
}

/**
 * 핵심 실동작 카드.
 *
 * isInitLoading 은 stateIn(WhileSubscribed) 로 공유되므로 "구독자가 있는지"가 동작을 좌우한다.
 * 따라서 이 Flow 는 토글 가능한 자식(SubscriberChild)에서만 구독한다 —
 * 여기(카드 레벨)에서도 같이 구독해 버리면 구독이 영원히 끊기지 않아 데모가 성립하지 않는다.
 */
@Composable
private fun TimingDemoCard(viewModel: InitTestViewModel) {
    val launchedEffectLoading by viewModel.isLaunchedEffectLoading.collectAsStateWithLifecycle()
    val viewModelInitLoading by viewModel.isViewModelInitLoading.collectAsStateWithLifecycle()
    val loadingCount by viewModel.testLoadingCount.collectAsStateWithLifecycle()

    var subscribed by remember { mutableStateOf(true) }
    var unsubscribedAt by remember { mutableLongStateOf(0L) }
    var elapsedSinceUnsubscribe by remember { mutableLongStateOf(0L) }
    val events = remember { mutableStateListOf<String>() }

    // 화면 진입 시 LaunchedEffect 패턴 트리거(기존 동작 유지) — 진입 로그도 함께 남긴다.
    LaunchedEffect(Unit) {
        viewModel.changeLaunchedEffectLoading()
        events.add(0, "🟣 LaunchedEffect(Unit) → changeLaunchedEffectLoading() 호출")
    }

    // 구독이 끊긴 동안 경과 시간을 실시간으로 보여줘, 타임아웃 전/후 재구독을 직접 골라 누를 수 있게 한다.
    LaunchedEffect(subscribed) {
        if (!subscribed) {
            while (true) {
                elapsedSinceUnsubscribe = System.currentTimeMillis() - unsubscribedAt
                delay(100L)
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "실동작 — 구독을 끊었다 붙여 발화 시점 비교",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF388E3C)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "구독 해제 후 ${SUBSCRIPTION_TIMEOUT_MS / 1000}초가 지나기 전에 다시 구독하면 onStart 는 재발화하지 않고, " +
                        "지난 뒤에 구독하면 재발화합니다. 아래 호출 횟수가 오르는지로 확인하세요.",
                fontSize = 12.sp,
                color = Color(0xFF757575),
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            LoadingBadge(
                label = "LaunchedEffect",
                loading = launchedEffectLoading,
                color = Color(0xFF7B1FA2)
            )
            Spacer(modifier = Modifier.height(6.dp))
            LoadingBadge(
                label = "ViewModel init { }",
                loading = viewModelInitLoading,
                color = Color(0xFFD32F2F)
            )
            Spacer(modifier = Modifier.height(6.dp))
            if (subscribed) {
                SubscriberChild(
                    viewModel = viewModel,
                    onEvent = { events.add(0, it) }
                )
            } else {
                LoadingBadge(
                    label = "onStart + stateIn",
                    loading = false,
                    color = Color(0xFF9E9E9E),
                    suffix = "구독 없음"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1976D2).copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "총 로딩 호출 횟수", fontSize = 12.sp, color = Color(0xFF1976D2))
                Text(
                    text = "${loadingCount}회",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF1976D2)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "진입 직후 3회 = LaunchedEffect 1 + init 1 + onStart 1",
                fontSize = 11.sp,
                color = Color(0xFF9E9E9E)
            )
            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    if (subscribed) {
                        unsubscribedAt = System.currentTimeMillis()
                        elapsedSinceUnsubscribe = 0L
                    }
                    subscribed = !subscribed
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (subscribed) Color(0xFFD32F2F) else Color(0xFF388E3C)
                )
            ) {
                Text(
                    text = if (subscribed) "구독 해제" else "다시 구독",
                    color = Color.White,
                    fontSize = 12.sp
                )
            }

            if (!subscribed) {
                Spacer(modifier = Modifier.height(8.dp))
                val passedTimeout = elapsedSinceUnsubscribe >= SUBSCRIPTION_TIMEOUT_MS
                Text(
                    text = "구독 해제 후 %.1f초 경과 — 지금 다시 구독하면 %s".format(
                        elapsedSinceUnsubscribe / 1000.0,
                        if (passedTimeout) "재발화 O (횟수 증가)" else "재발화 X (횟수 유지)"
                    ),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (passedTimeout) Color(0xFF388E3C) else Color(0xFFD32F2F)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "이벤트 로그 (최신순)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF424242)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF212121), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                if (events.isEmpty()) {
                    Text(
                        text = "(아직 이벤트 없음)",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF9E9E9E)
                    )
                } else {
                    events.take(6).forEach { log ->
                        Text(
                            text = log,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFB3E5FC),
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * isInitLoading 의 유일한 구독자.
 *
 * 이 컴포저블이 컴포지션에서 빠지면 collectAsStateWithLifecycle 구독도 함께 해제되고,
 * WhileSubscribed 타임아웃이 지나면 stateIn 공유가 중단된다. 다시 진입하면 onStart 가 재실행된다.
 */
@Composable
private fun SubscriberChild(viewModel: InitTestViewModel, onEvent: (String) -> Unit) {
    val initLoading by viewModel.isInitLoading.collectAsStateWithLifecycle()

    DisposableEffect(Unit) {
        onEvent("🟢 구독 시작 — isInitLoading collect")
        onDispose {
            onEvent("🔴 구독 해제 — ${SUBSCRIPTION_TIMEOUT_MS / 1000}초 뒤 공유 중단 예정")
        }
    }

    LoadingBadge(
        label = "onStart + stateIn",
        loading = initLoading,
        color = Color(0xFF388E3C),
        suffix = "구독 중"
    )
}

@Composable
private fun LoadingBadge(
    label: String,
    loading: Boolean,
    color: Color,
    suffix: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = color)
            if (suffix != null) {
                Text(text = suffix, fontSize = 10.sp, color = Color(0xFF9E9E9E))
            }
        }
        Text(
            text = if (loading) "loading" else "idle",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = if (loading) color else Color(0xFF9E9E9E)
        )
    }
}

@Composable
private fun WhileSubscribedCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "왜 init { } 대신 onStart + stateIn 인가",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "init { } 은 ViewModel 이 만들어지는 순간 무조건 실행됩니다. 화면이 실제로 보이지 않아도 네트워크를 타고, " +
                        "구독자가 아직 없으면 그 사이 흘러간 일회성 이벤트는 놓치며, 실패해도 다시 시도할 훅이 없습니다. " +
                        "테스트에서도 생성자 호출만으로 부수 효과가 발생해 제어하기 어렵습니다.\n\n" +
                        "onStart + stateIn(WhileSubscribed) 는 \"실제로 보고 있는 구독자가 생겼을 때\" 로딩을 시작하고, " +
                        "화면 회전처럼 짧게 끊기는 구간은 타임아웃으로 흡수해 불필요한 재요청을 막습니다.",
                fontSize = 13.sp,
                color = Color(0xFF424242),
                lineHeight = 19.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            CodeBlock(
                code = "// ❌ 생성 즉시 1회, 재구독과 무관\n" +
                        "init { loadData() }\n\n" +
                        "// ✅ 첫 구독 시 시작, 타임아웃 초과 후 재구독하면 재실행\n" +
                        "val state = _state\n" +
                        "    .onStart { loadData() }\n" +
                        "    .stateIn(\n" +
                        "        scope = viewModelScope,\n" +
                        "        started = SharingStarted.WhileSubscribed(5000L),\n" +
                        "        initialValue = false\n" +
                        "    )",
                borderColor = Color(0xFF388E3C)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "5초라는 값은 화면 회전·앱 전환처럼 잠깐 끊기는 구간을 재요청 없이 넘기기 위한 관례적인 여유값입니다.",
                fontSize = 12.sp,
                color = Color(0xFF757575),
                lineHeight = 16.sp
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
                text = "정리",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "• 화면 진입 1회성 동작이면 LaunchedEffect — UI 사건이므로 UI 가 트리거하는 게 맞습니다\n" +
                        "• 구독 기반 데이터 스트림이면 onStart + stateIn(WhileSubscribed)\n" +
                        "• init { } 은 구독·수명과 무관하게 실행되므로 데이터 로딩 트리거로는 권장되지 않습니다\n" +
                        "• 세 방식 모두 로딩 자체는 동일하게 동작합니다. 차이는 오직 \"언제, 몇 번\" 발화하는가입니다",
                fontSize = 13.sp,
                color = Color(0xFF424242),
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun CodeBlock(code: String, borderColor: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF212121), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(
            text = code,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = borderColor.copy(alpha = 0.9f),
            lineHeight = 16.sp
        )
    }
}
