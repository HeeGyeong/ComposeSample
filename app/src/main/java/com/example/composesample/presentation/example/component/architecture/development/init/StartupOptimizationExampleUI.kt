package com.example.composesample.presentation.example.component.architecture.development.init

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun StartupOptimizationExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackEvent) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "뒤로가기")
            }
            Text(
                text = "Startup Optimization",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        HorizontalDivider()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item { AppStartupSection() }
            item { HorizontalDivider(thickness = 2.dp) }
            item { KoinLazySection() }
            item { HorizontalDivider(thickness = 2.dp) }
            item { BaselineProfileSection() }
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

// ─────────────────────────────────────────────────────────
// 섹션 1: App Startup 라이브러리
// ─────────────────────────────────────────────────────────

@Composable
private fun AppStartupSection() {
    SectionCard(title = "1. App Startup 라이브러리") {
        Text(
            text = "기존 ContentProvider 남용 문제와 App Startup 라이브러리의 해결 방법",
            fontSize = 13.sp,
            color = Color.DarkGray
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 문제: ContentProvider 남용
        SubTitle("❌ 문제: 라이브러리마다 ContentProvider 등록")
        CodeBlock(
            """// 라이브러리 A의 자동 초기화
class LibraryAInitProvider : ContentProvider() {
    override fun onCreate(): Boolean {
        LibraryA.init(context!!) // 앱 시작 시 강제 실행
        return true
    }
}

// 라이브러리 B, C, D... 모두 동일 패턴
// → ContentProvider가 많을수록 시작 시간 증가
// → 초기화 순서 제어 불가"""
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 해결: App Startup Initializer
        SubTitle("✅ 해결: App Startup Initializer 체인")
        CodeBlock(
            """// Initializer 구현
class TimberInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        Timber.plant(Timber.DebugTree())
    }
    // 의존성 명시: 이 Initializer 먼저 실행
    override fun dependencies() = emptyList()
}

class AnalyticsInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        Analytics.init(context)
    }
    // TimberInitializer 완료 후 실행
    override fun dependencies() =
        listOf(TimberInitializer::class.java)
}"""
        )

        Spacer(modifier = Modifier.height(8.dp))

        CodeBlock(
            """// AndroidManifest.xml — ContentProvider 단 1개
<provider
    android:name="androidx.startup.InitializationProvider"
    android:authorities="\${applicationId}.startup">
    <meta-data
        android:name=".TimberInitializer"
        android:value="androidx.startup" />
    <meta-data
        android:name=".AnalyticsInitializer"
        android:value="androidx.startup" />
</provider>"""
        )

        Spacer(modifier = Modifier.height(8.dp))

        InfoBox(
            "ContentProvider를 여러 개 등록하면 각각 Application.onCreate() 이전에 실행됩니다.\n" +
                    "App Startup은 단 하나의 ContentProvider만 사용해 모든 초기화를 순서대로 관리합니다."
        )
    }
}

// ─────────────────────────────────────────────────────────
// 섹션 2: Koin 지연 초기화
// ─────────────────────────────────────────────────────────

@Composable
private fun KoinLazySection() {
    val scope = rememberCoroutineScope()

    // 즉시 초기화 시뮬레이션 상태
    var eagerTime by remember { mutableLongStateOf(-1L) }
    var eagerRunning by remember { mutableStateOf(false) }

    // 지연 초기화 시뮬레이션 상태
    var lazyTime by remember { mutableLongStateOf(-1L) }
    var lazyRunning by remember { mutableStateOf(false) }

    SectionCard(title = "2. Koin 지연 초기화 (by inject())") {
        Text(
            text = "모듈 선언 방식에 따른 초기화 타이밍 차이를 비교합니다.",
            fontSize = 13.sp,
            color = Color.DarkGray
        )

        Spacer(modifier = Modifier.height(12.dp))

        SubTitle("❌ 즉시 초기화 — get()")
        CodeBlock(
            """// Koin 모듈 선언
val appModule = module {
    single { HeavyRepository(get()) }  // 앱 시작 시 즉시 생성
}

// ViewModel에서 사용
class MyViewModel : ViewModel() {
    // Application.onCreate() 시점에 이미 생성됨
    private val repo: HeavyRepository = get()
}"""
        )

        Spacer(modifier = Modifier.height(12.dp))

        SubTitle("✅ 지연 초기화 — by inject()")
        CodeBlock(
            """// ViewModel에서 사용
class MyViewModel : ViewModel() {
    // 실제로 repo를 처음 사용하는 시점에 생성
    private val repo: HeavyRepository by inject()

    fun loadData() {
        repo.fetch() // 여기서 처음 초기화됨
    }
}"""
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 인터랙티브 시뮬레이션
        SubTitle("시뮬레이션: 무거운 의존성 초기화 타이밍")

        Text(
            text = "각 버튼을 눌러 초기화 시점의 차이를 확인해보세요.\n(실제 무거운 작업 300ms 시뮬레이션)",
            fontSize = 12.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 즉시 초기화
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    scope.launch {
                        eagerRunning = true
                        eagerTime = -1L
                        val start = System.currentTimeMillis()
                        // 앱 시작 시점에 무거운 초기화 발생 시뮬레이션
                        delay(300)
                        eagerTime = System.currentTimeMillis() - start
                        eagerRunning = false
                    }
                },
                enabled = !eagerRunning,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                modifier = Modifier.weight(1f)
            ) { Text("앱 시작 (즉시 초기화)", fontSize = 11.sp) }

            Column(modifier = Modifier.weight(1f)) {
                if (eagerRunning) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Text("초기화 중...", fontSize = 11.sp, color = Color.Gray)
                } else if (eagerTime >= 0) {
                    Text(
                        "시작 소요: ${eagerTime}ms",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE53935)
                    )
                    Text("⚠ 사용 전에 이미 대기", fontSize = 11.sp, color = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 지연 초기화
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    scope.launch {
                        lazyRunning = true
                        lazyTime = -1L
                        val start = System.currentTimeMillis()
                        // 실제 사용 시점에 초기화 발생 시뮬레이션
                        delay(300)
                        lazyTime = System.currentTimeMillis() - start
                        lazyRunning = false
                    }
                },
                enabled = !lazyRunning,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                modifier = Modifier.weight(1f)
            ) { Text("첫 사용 (지연 초기화)", fontSize = 11.sp) }

            Column(modifier = Modifier.weight(1f)) {
                if (lazyRunning) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Text("초기화 중...", fontSize = 11.sp, color = Color.Gray)
                } else if (lazyTime >= 0) {
                    Text(
                        "사용 시 소요: ${lazyTime}ms",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E88E5)
                    )
                    Text("✅ 앱 시작은 즉각적", fontSize = 11.sp, color = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        InfoBox(
            "즉시 초기화: 앱 시작 시 모든 의존성을 미리 생성 → 첫 화면 렌더링 지연\n" +
                    "지연 초기화: 실제 사용 시점까지 생성 미룸 → 앱 시작은 빠르나 첫 사용 시 약간 지연\n\n" +
                    "무거운 의존성(DB, Network 등)은 by inject()로 지연 초기화를 권장합니다."
        )
    }
}

// ─────────────────────────────────────────────────────────
// 섹션 3: Baseline Profile
// ─────────────────────────────────────────────────────────

@Composable
private fun BaselineProfileSection() {
    SectionCard(title = "3. Baseline Profile") {
        Text(
            text = "AOT(Ahead-Of-Time) 컴파일로 JIT 워밍업 시간을 제거하는 방법",
            fontSize = 13.sp,
            color = Color.DarkGray
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 동작 원리
        SubTitle("동작 원리")

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PhaseCard(
                title = "JIT (기본)",
                description = "앱 실행 시\n코드 해석\n→ 느린 시작",
                color = Color(0xFFFFEBEE)
            )
            Text("→", modifier = Modifier.align(Alignment.CenterVertically), fontSize = 20.sp)
            PhaseCard(
                title = "AOT 컴파일",
                description = "설치/업데이트 시\n자주 쓰는 코드\n미리 컴파일",
                color = Color(0xFFE3F2FD)
            )
            Text("→", modifier = Modifier.align(Alignment.CenterVertically), fontSize = 20.sp)
            PhaseCard(
                title = "Baseline\nProfile",
                description = "첫 실행부터\n컴파일된 코드\n즉시 실행",
                color = Color(0xFFE8F5E9)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 적용 방법
        SubTitle("✅ 적용 방법")
        CodeBlock(
            """// 1. 의존성 추가 (build.gradle)
dependencies {
    implementation("androidx.profileinstaller:profileinstaller:1.3.1")
    androidTestImplementation("androidx.benchmark:benchmark-macro-junit4:1.2.3")
}

// 2. BaselineProfileGenerator 작성
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() = rule.collect(
        packageName = "com.example.app"
    ) {
        pressHome()
        startActivityAndWait() // 앱 시작 경로 기록
        // 주요 화면 탐색 경로 추가
    }
}

// 3. 생성 후 src/main/baseline-prof.txt 에 자동 저장"""
        )

        Spacer(modifier = Modifier.height(8.dp))

        InfoBox(
            "Baseline Profile은 앱 설치 후 첫 실행 속도를 최적화합니다.\n" +
                    "Google Play는 앱 배포 시 baseline-prof.txt를 자동으로 활용합니다.\n" +
                    "일반적으로 앱 시작 속도 20~40% 향상 효과를 기대할 수 있습니다."
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 세 가지 기법 요약
        SubTitle("세 가지 기법 비교")

        SummaryTable()
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
private fun SubTitle(text: String) {
    Text(
        text = text,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color.DarkGray,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
private fun CodeBlock(code: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(
            text = code,
            fontSize = 11.sp,
            color = Color(0xFFD4D4D4),
            fontFamily = FontFamily.Monospace,
            lineHeight = 17.sp
        )
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

@Composable
private fun PhaseCard(title: String, description: String, color: Color) {
    Column(
        modifier = Modifier
            .background(color, RoundedCornerShape(8.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
        Spacer(modifier = Modifier.height(4.dp))
        Text(description, fontSize = 10.sp, color = Color.DarkGray, lineHeight = 14.sp)
    }
}

@Composable
private fun SummaryTable() {
    val rows = listOf(
        Triple("App Startup", "초기화 순서 제어", "즉시"),
        Triple("Koin by inject()", "의존성 생성 지연", "즉시"),
        Triple("Baseline Profile", "AOT 컴파일 적용", "빌드 설정 필요")
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 헤더
        Row {
            Text("기법", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f))
            Text("효과", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f))
            Text("적용 난이도", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.3f))
        }
        HorizontalDivider()
        rows.forEach { (tech, effect, difficulty) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(tech, fontSize = 11.sp, modifier = Modifier.weight(1.2f))
                Text(effect, fontSize = 11.sp, color = Color.DarkGray, modifier = Modifier.weight(1.5f))
                Text(
                    difficulty,
                    fontSize = 11.sp,
                    color = if (difficulty == "즉시") Color(0xFF43A047) else Color(0xFFE53935),
                    modifier = Modifier.weight(1.3f)
                )
            }
        }
    }
}
