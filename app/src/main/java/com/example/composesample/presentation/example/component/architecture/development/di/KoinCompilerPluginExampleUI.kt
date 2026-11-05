package com.example.composesample.presentation.example.component.architecture.development.di

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader
import com.example.composesample.util.boldMarkup

@Composable
fun KoinCompilerPluginExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Koin Compiler Plugin",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { OverviewCard() }
            item { PluginDslCard() }
            item { BuildErrorCard() }
            item { RuntimeGapCard() }
            item { AppWideCard() }
            item { KotlinCompatCard() }
            item { ChecklistCard() }
        }
    }
}

@Composable
private fun OverviewCard() {
    SectionCard(title = "빌드가 DI 그래프를 검사한다") {
        BodyText(
            "Koin Compiler Plugin 1.2.3 은 KSP 가 아니라 **Kotlin 컴파일러 플러그인**이다. app/build.gradle 에 플러그인 한 줄을 " +
                "걸면 compileDebugKotlin · compileReleaseKotlin 이 startKoin · koinApplication 진입점마다 그래프를 맞춰 보고, " +
                "빠진 정의를 **빌드 오류**로 낸다. 이 화면의 그래프는 모두 이 앱에서 실제로 컴파일 · 실행된 것이다."
        )
        Spacer(modifier = Modifier.height(8.dp))
        TableRow("항목", "내용", isHeader = true)
        TableRow("적용", "plugins { alias(libs.plugins.koin.compiler) } — 런타임 의존성 추가 없음(koin-core 4.2.2 안의 org.koin.plugin.module.dsl)")
        TableRow("KOIN-D001", "진입점 그래프에 생성자 매개변수의 정의가 없다(플러그인 DSL)")
        TableRow("KOIN-D002", "get() · koinViewModel() 호출 위치의 타입이 그래프에 없다")
        TableRow("KOIN-D004", "순환 의존")
        TableRow("KOIN-W003", "모듈 목록을 실행 중에 고른다 — 검증할 수 없다는 경고")
        TableRow("애노테이션 문법", "@Singleton · @Factory 도 KSP 없이 읽지만 koin-annotations 의존성이 따로 필요 — 이 앱은 DSL 만 쓴다")
    }
}

@Composable
private fun PluginDslCard() {
    var report by remember { mutableStateOf<GraphReport?>(null) }

    SectionCard(title = "1. 플러그인 DSL — 생성자는 컴파일러가 채운다") {
        BodyText(
            "single<T>() · factory<T>() 는 **org.koin.plugin.module.dsl** 의 것이다. 플러그인이 컴파일 중에 T 의 생성자를 읽어 " +
                "매개변수마다 get() 을 넣은 람다로 바꾸고, 매개변수의 정의가 진입점 그래프에 있는지 함께 검사한다."
        )
        CodeBlock(
            "import org.koin.plugin.module.dsl.factory\n" +
                "import org.koin.plugin.module.dsl.single\n\n" +
                "val checkoutModule = module {\n" +
                "    single<OrderRepository>()\n" +
                "    single<CartClock>()\n" +
                "    single<PriceCalculator>()   // (clock: CartClock)\n" +
                "    single<CheckoutService>()   // (repository, calculator)\n" +
                "    factory<ReceiptFormatter>()\n" +
                "    single<RetryPolicy>()       // (cache: LabCache?, maxRetries: Int = 3)\n" +
                "}\n\n" +
                "koinApplication { modules(checkoutModule) }  // 격리 그래프 — 이것도 검사되는 진입점"
        )
        Button(onClick = { report = resolveCheckoutGraph() }) {
            Text("격리 그래프에서 풀기")
        }
        report?.let { r ->
            Spacer(modifier = Modifier.height(6.dp))
            TableRow("확인", "결과", isHeader = true)
            TableRow("service.repository", if (r.serviceRepositoryIsShared) "그래프의 OrderRepository 와 같은 인스턴스 — single" else "다른 인스턴스")
            TableRow("calculator.clock", if (r.calculatorClockIsShared) "그래프의 CartClock 과 같은 인스턴스 — single" else "다른 인스턴스")
            TableRow("factory 두 번", if (r.factoryGivesNewInstance) "서로 다른 인스턴스 — factory" else "같은 인스턴스")
            TableRow("retry.cache", if (r.retryCacheIsNull) "null — LabCache 정의가 없고 nullable 이라 통과" else "주입됨")
            TableRow("retry.maxRetries", "${r.retryMaxRetries} — 기본값을 그대로 쓴다(정의를 찾지 않음)")
        }
        Spacer(modifier = Modifier.height(6.dp))
        CaptionText(
            "플러그인 없이 같은 코드를 컴파일하면 빌드는 되지만 모듈을 올리는 순간 NotImplementedError: \"Error while using " +
                "'single<T>()'. The Koin Compiler plugin is missing\" — single<T>() 의 본문은 플러그인이 바꿔 넣을 자리 표시일 뿐이다" +
                "(Kotlin 2.4.21 단독 프로젝트 측정)."
        )
    }
}

@Composable
private fun BuildErrorCard() {
    SectionCard(title = "2. 빌드가 막는 실수 — 이 앱에서 실제로 낸 오류") {
        BodyText(
            "정의를 하나씩 지우고 **:app:compileDebugKotlin**(HotSwan 포함)을 돌린 기록이다. 실패하면 앱이 만들어지지 않으므로 " +
                "화면에서 재현할 수는 없다. 손으로 쓴 람다(single { X(get()) })도 람다 안의 get() 호출 위치에서 검사된다."
        )
        MeasuredError(
            title = "checkoutModule 에서 single<CartClock>() 삭제",
            log = "e: KoinCompilerPluginLab.kt:21:1 [Koin][KOIN-D001] Missing dependency: …di.CartClock\n" +
                "  required by: dsl:…di.PriceCalculator (parameter 'clock')\n" +
                "  in module: …di.checkoutModule\n" +
                "e: KoinCompilerPluginLab.kt:55:73 [Koin][KOIN-D002] Missing definition: …di.CartClock\n" +
                "  resolved by: get<CartClock>()"
        )
        MeasuredError(
            title = "apiModule 에서 single<GsonConverterFactory> { … } 삭제",
            log = "e: di/ApiModule.kt:24:34 [Koin][KOIN-D002] Missing definition: retrofit2.converter.gson.GsonConverterFactory\n" +
                "  resolved by: get<GsonConverterFactory>()"
        )
        MeasuredError(
            title = "viewModelModule 에서 viewModel { SSEViewModel() } 삭제 — 화면 3곳",
            log = "e: …/data/sse/SSEExampleUI.kt:51:38 [Koin][KOIN-D002] Missing definition: …SSEViewModel\n" +
                "e: …/lazycolumn/ReverseLazyColumnExampleUI.kt:51:38 [Koin][KOIN-D002] Missing definition: …SSEViewModel\n" +
                "e: …/lazycolumn/ReverseLazyColumnExampleUI.kt:175:38 [Koin][KOIN-D002] Missing definition: …SSEViewModel\n" +
                "  resolved by: koinViewModel<SSEViewModel>()"
        )
        MeasuredError(
            title = "A(b: B) · B(a: A) 순환 — 단독 프로젝트",
            log = "e: [Koin][KOIN-D004] Circular dependency detected:\n" +
                "  dsl:k106.A → dsl:k106.B → dsl:k106.A\n" +
                "  Break with Lazy<T> injection or refactor to remove the cycle."
        )
    }
}

@Composable
private fun MeasuredError(title: String, log: String) {
    Spacer(modifier = Modifier.height(6.dp))
    Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
    CodeBlock(log)
}

@Composable
private fun RuntimeGapCard() {
    var selected by rememberSaveable { mutableStateOf(RuntimeGap.QUALIFIER) }
    var result by remember { mutableStateOf<Pair<RuntimeGap, GapResult>?>(null) }

    SectionCard(title = "3. 빌드는 통과하고 실행 중에 터지는 것") {
        BodyText(
            "아래 두 모듈은 지금 이 앱에 컴파일돼 있다 — 빌드가 통과했다는 뜻이다. 누르면 격리 그래프에서 잘못된 호출과 " +
                "바른 호출을 나란히 실행한다."
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RuntimeGap.entries.forEach { gap ->
                FilterChip(
                    selected = selected == gap,
                    onClick = { selected = gap },
                    label = { Text(gap.label, fontSize = 12.sp) }
                )
            }
        }
        CodeBlock(
            when (selected) {
                RuntimeGap.QUALIFIER ->
                    "val qualifierModule = module {\n" +
                        "    single(named(\"local\")) { LabCache() }\n" +
                        "    single { RemoteClient(get(named(\"remote\"))) }  // 이름이 다르다\n" +
                        "}\n" +
                        "잘못: get<RemoteClient>()\n" +
                        "바름: get<LabCache>(named(\"local\"))"
                RuntimeGap.PARAMETERS ->
                    "val lookupModule = module {\n" +
                        "    factory { (orderId: String) -> OrderLookup(orderId) }\n" +
                        "}\n" +
                        "잘못: get<OrderLookup>()\n" +
                        "바름: get<OrderLookup> { parametersOf(\"A-1001\") }"
            }
        )
        Button(onClick = { result = selected to runGap(selected) }) {
            Text("실행")
        }
        result?.takeIf { it.first == selected }?.second?.let { r ->
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "잘못된 호출", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
            CodeBlock(r.wrong)
            Text(text = "바른 호출", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
            CodeBlock(r.right)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "실행 중에 고른 모듈 목록 — 이 앱에 넣지 않은 이유", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF212121))
        Spacer(modifier = Modifier.height(4.dp))
        BodyText(
            "modules(if (flag) a else b) 는 KOIN-W003 경고만 내고 통과한다. 그런데 컴파일 단위에 이런 진입점이 **하나라도** 있으면 " +
                "다른 파일의 검사까지 꺼졌다 — 임시 파일에 하나를 두자 checkoutModule 에서 single<CartClock>() 을 지워도(D001), " +
                "apiModule 에서 Gson 정의를 지워도(D002) 빌드가 통과했고, 경고는 엉뚱하게 'the entry point loading apiModule' 을 " +
                "가리켰다(1.2.2). 목록은 listOf · + 로 고정해 둔다."
        )
    }
}

@Composable
private fun AppWideCard() {
    SectionCard(title = "4. 앱 전체에 걸었을 때") {
        TableRow("항목", "측정", isHeader = true)
        TableRow("기존 정의 58개", "람다 57개는 provider-only(생성자를 추측하지 않고 람다 안의 get() 을 검사) · singleOf 1개는 생성자 검사")
        TableRow("기존 클래스", "BaseApplication · 목록 항목 · 이 화면 외에는 바이트 동일 — 플러그인은 기존 DSL 을 바꾸지 않는다")
        TableRow("추가 클래스", "org.koin.plugin.hints 힌트 — 모듈마다 1개, 앱 8 + 이 예제 3 = 11개(release 는 minify 를 끄고 있어 dex 에 그대로 들어간다)")
        TableRow("진입점", "startKoin { loadKoinModules(…) } → modules(KoinModules + CleanArchitectureAddModules)")
        TableRow("HotSwan", "SKIPPED 84 → 85 — 늘어난 1개는 예외 사슬이 release 와 달라 일부러 제외한 이 Lab 파일")
        TableRow("빌드 로그", "컴파일마다 'Kotlin 2.4.21 is newer than the newest verified version' 경고 한 줄")
        Spacer(modifier = Modifier.height(6.dp))
        CaptionText(
            "진입점을 바꾼 이유: loadKoinModules 형태도 동작은 같다(둘 다 koin.loadModules(allowOverride = true) 뒤 eager 생성 — " +
                "koin-core 4.2.2 소스). 그런데 플러그인 문서는 loadKoinModules 로 온 모듈을 '컴파일 시점에 증명할 수 없는 런타임 로딩'으로 " +
                "본다. 진입점이 startKoin 하나일 때는 통과했지만, 이 예제의 koinApplication 이 더해지자 앱의 get() · koinViewModel() " +
                "**63곳**이 D002 로 막혔다(단독 프로젝트에서도 이 조합에서만 재현)."
        )
        Spacer(modifier = Modifier.height(4.dp))
        CaptionText(
            "debug 의 HotSwan 은 Koin 예외(자바 기준 checked Exception)를 UndeclaredThrowableException 으로 한 겹 감쌌다 — " +
                "3번 카드의 예외 사슬이 release 와 달라 Lab 파일을 hotSwanCompiler.exclude 에 넣었다(단위 테스트가 사슬 전체를 비교해 잡았다)."
        )
    }
}

@Composable
private fun KotlinCompatCard() {
    SectionCard(title = "5. Kotlin 2.4.21 — 검증 목록 밖이지만 바뀐 곳이 없다") {
        TableRow("확인", "결과", isHeader = true)
        TableRow("검증 목록", "2.3.20 · 2.4.0 · 2.4.10 · 2.4.20 — 목록 밖 버전은 경고 후 가장 가까운 어댑터(2.4.20)로 진행")
        TableRow("컴파일러 jar 비교", "2.4.20 → 2.4.21: 클래스 26,016개 중 8개만 다름(모두 JS · Wasm 백엔드), 추가 · 삭제 0")
        TableRow("ABI 검사", "플러그인의 Kotlin 멤버 참조 476개 — 2.4.21 누락 0. 대조로 1.2.0 ↔ 2.4.20 은 노트대로 4건 누락")
        TableRow("실제 실행 jar", "kotlin-compiler-embeddable 은 두 버전 모두 getPsi 1건만 다름 — 플러그인이 LinkageError 를 잡아 넘기는 의도된 경로")
        TableRow("실행", "단독 프로젝트 2.4.20 · 2.4.21 에서 같은 D001 · 같은 실행 결과")
        Spacer(modifier = Modifier.height(6.dp))
        CaptionText(
            "Kotlin 은 .20 패치 자리에 기능 릴리스를 낸다 — 2.4.20 이 플러그인 1.2.0 이 쓰던 컴파일러 API 4개를 깨뜨렸다. " +
                "Kotlin 을 올릴 때는 이 플러그인의 검증 목록부터 확인한다."
        )
    }
}

@Composable
private fun ChecklistCard() {
    SectionCard(title = "정리") {
        listOf(
            "생성자 주입은 single<T>() · factory<T>()(org.koin.plugin.module.dsl) — 매개변수까지 **D001** 로 검사된다",
            "손으로 쓴 람다는 get() 호출 위치만 검사된다 — 한정자 이름 · parametersOf 는 실행 중에야 드러난다",
            "nullable · 기본값 매개변수는 정의가 없어도 통과한다 — 의도한 것인지 확인",
            "모듈 목록은 listOf · + 로 고정 — 조건부 목록 하나가 컴파일 단위 전체의 검사를 끈다",
            "진입점은 startKoin { modules(…) } — loadKoinModules 로 넣은 모듈은 검사되는 그래프로 치지 않는다",
            "Kotlin 상향 전에 플러그인 검증 목록 확인"
        ).forEach { line ->
            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                Text(text = "• ", fontSize = 13.sp, color = Color(0xFF1976D2))
                BodyText(line)
            }
        }
    }
}

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

/** 코드 · 빌드 로그는 줄이 길어 접히면 읽기 어렵다 → 가로 스크롤로 원문 모양을 유지한다. */
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
            modifier = Modifier.width(112.dp),
            fontSize = 11.sp,
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
            color = Color(0xFF424242)
        )
        Text(
            text = second,
            modifier = Modifier.weight(1f),
            fontSize = 11.sp,
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
            color = Color(0xFF424242)
        )
    }
}
