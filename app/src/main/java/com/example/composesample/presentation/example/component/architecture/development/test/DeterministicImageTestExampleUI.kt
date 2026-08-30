package com.example.composesample.presentation.example.component.architecture.development.test

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import com.example.composesample.presentation.MainHeader
import kotlin.time.TimeSource

/**
 * 스크린샷 테스트 이미지 결정론화 예제
 * - 비동기 이미지가 왜 골든 이미지 비교를 깨뜨리는지 실측으로 보여주고,
 *   Coil 3 의 프리뷰 핸들러(LocalAsyncImagePreviewHandler)로 픽셀을 고정하는 방법을 시연한다.
 * - 참고 URL 과 개념 정리는 같은 폴더의 exampleGuide.kt 참조
 */

// picsum 은 seed 별로 같은 이미지를 반환하지만, "그 이미지가 언제 어디서 오는지"(네트워크/메모리 캐시/실패)는 실행마다 달라진다
private const val DETERMINISTIC_DEMO_URL = "https://picsum.photos/seed/deterministic-image/420/240"

// 프리뷰 핸들러가 돌려줄 고정 이미지 색 — 실행 횟수·네트워크 상태와 무관하게 항상 동일한 픽셀
private val FIXED_IMAGE_COLOR = 0xFF66BB6A.toInt()

/** 한 번의 로드 결과 — 상태 이름·출처·소요 시간을 함께 기록해 실행 간 차이를 눈으로 비교한다 */
private data class ImageLoadOutcome(
    val attempt: Int,
    val stateLabel: String,
    val source: String,
    val elapsedMs: Long
)

/**
 * 고정 이미지를 즉시 돌려주는 프리뷰 핸들러.
 * ColorImage(color, width, height) 는 네트워크·디코딩 없이 단색 이미지를 만들어 반환한다.
 */
@OptIn(ExperimentalCoilApi::class)
private val fixedImagePreviewHandler = AsyncImagePreviewHandler {
    ColorImage(FIXED_IMAGE_COLOR, 420, 240)
}

@Composable
fun DeterministicImageTestExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "스크린샷 테스트 이미지 결정론화",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { FlakyPipelineCard() }
            item { NonDeterminismDemoCard() }
            item { PreviewHandlerMechanismCard() }
            item { CoilTestEngineCard() }
            item { ApproachComparisonCard() }
        }
    }
}

// ==================== 1. 개념 ====================

@Composable
private fun FlakyPipelineCard() {
    DeterministicSectionCard(title = "1. 비동기 이미지가 골든 이미지를 깨뜨리는 지점") {
        Text(
            text = "스크린샷 테스트는 \"같은 입력 → 같은 픽셀\"을 전제로 한다. " +
                "네트워크 이미지는 아래 4단계 중 어느 하나만 흔들려도 캡처 시점의 픽셀이 달라진다.",
            fontSize = 13.sp,
            color = Color(0xFF424242),
            lineHeight = 19.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        DeterministicTableRow(
            first = "단계",
            second = "실행마다 달라지는 것",
            isHeader = true
        )
        DeterministicTableRow("① 요청 생성", "크기 해석(SizeResolver)이 레이아웃 타이밍에 의존")
        DeterministicTableRow("② 캐시/네트워크", "1회차 NETWORK, 2회차 MEMORY_CACHE, 오프라인이면 Error")
        DeterministicTableRow("③ 디코딩", "완료 시각이 기기 성능·이미지 크기에 따라 변동")
        DeterministicTableRow("④ 첫 프레임", "캡처 순간에 placeholder 였는지 실제 이미지였는지가 갈림")

        Spacer(modifier = Modifier.height(12.dp))

        DeterministicNoticeBox(
            text = "결과적으로 골든 이미지와의 diff 가 \"코드가 바뀌어서\"가 아니라 " +
                "\"이번엔 캐시에서 왔기 때문에\" 생긴다. 이것이 스크린샷 테스트 flaky 의 대표 원인이다."
        )
    }
}

// ==================== 2. 실측 데모 ====================

// LocalAsyncImagePreviewHandler 는 @ExperimentalCoilApi 라 사용처에도 opt-in 이 필요하다
@OptIn(ExperimentalCoilApi::class)
@Composable
private fun NonDeterminismDemoCard() {
    // 다시 로드 버튼을 누를 때마다 증가 — 새 ImageRequest 를 만들어 로드를 재실행시키는 키
    var reloadKey by remember { mutableIntStateOf(0) }
    val plainHistory = remember { mutableStateListOf<ImageLoadOutcome>() }
    val fixedHistory = remember { mutableStateListOf<ImageLoadOutcome>() }

    DeterministicSectionCard(title = "2. 같은 이미지를 두 방식으로 로드해 비교") {
        Text(
            text = "왼쪽은 평범한 AsyncImage, 오른쪽은 같은 코드에 프리뷰 핸들러만 주입했다. " +
                "\"다시 로드\"를 여러 번 눌러 두 열의 상태·출처·소요 시간이 어떻게 갈리는지 확인한다.",
            fontSize = 13.sp,
            color = Color(0xFF424242),
            lineHeight = 19.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                DeterministicColumnHeader(
                    label = "기본 AsyncImage",
                    badge = "비결정적",
                    color = Color(0xFFC62828)
                )
                Spacer(modifier = Modifier.height(8.dp))
                DeterministicDemoImage(reloadKey = reloadKey) { outcome ->
                    recordOutcome(plainHistory, outcome)
                }
                Spacer(modifier = Modifier.height(8.dp))
                DeterministicOutcomeHistory(history = plainHistory, color = Color(0xFFC62828))
            }

            Column(modifier = Modifier.weight(1f)) {
                DeterministicColumnHeader(
                    label = "프리뷰 핸들러 주입",
                    badge = "결정론",
                    color = Color(0xFF2E7D32)
                )
                Spacer(modifier = Modifier.height(8.dp))
                // 두 CompositionLocal 을 함께 제공해야 핸들러가 실제로 사용된다 (아래 3번 카드 참조)
                CompositionLocalProvider(
                    LocalInspectionMode provides true,
                    LocalAsyncImagePreviewHandler provides fixedImagePreviewHandler
                ) {
                    DeterministicDemoImage(reloadKey = reloadKey) { outcome ->
                        recordOutcome(fixedHistory, outcome)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                DeterministicOutcomeHistory(history = fixedHistory, color = Color(0xFF2E7D32))
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = { reloadKey++ },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
        ) {
            Text(text = "다시 로드 (${reloadKey + 1}회차)", fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        DeterministicNoticeBox(
            text = "왼쪽은 회차마다 출처(NETWORK/MEMORY_CACHE)와 소요 시간이 바뀌고 오프라인이면 Error 로 떨어진다. " +
                "오른쪽은 네트워크를 아예 타지 않아 언제 눌러도 같은 픽셀이다. " +
                "다만 상태 라벨은 Success 가 아니라 Loading 으로 남는데, 그 이유는 아래 3번 카드에서 다룬다."
        )
    }
}

/**
 * 데모용 이미지 한 장.
 * reloadKey 가 바뀔 때마다 새 ImageRequest 와 새 측정 시점을 만들어 "이번 회차"의 결과를 관찰한다.
 */
@Composable
private fun DeterministicDemoImage(
    reloadKey: Int,
    onOutcome: (ImageLoadOutcome) -> Unit
) {
    val context = LocalPlatformContext.current

    // remember(reloadKey) 라서 버튼을 누를 때만 새 요청이 만들어진다 — 리컴포지션만으로는 재로드되지 않는다
    val request = remember(reloadKey) {
        ImageRequest.Builder(context)
            .data(DETERMINISTIC_DEMO_URL)
            .build()
    }
    val startMark = remember(reloadKey) { TimeSource.Monotonic.markNow() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFE0E0E0))
            .border(1.dp, Color(0xFFBDBDBD), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = request,
            contentDescription = "결정론 비교용 데모 이미지",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            onState = { state ->
                val elapsed = startMark.elapsedNow().inWholeMilliseconds
                when (state) {
                    // 팩토리로 만든 프리뷰 핸들러는 painter 를 Loading 에 실어 돌려준다 — 픽셀은 이미 고정된 상태
                    is AsyncImagePainter.State.Loading -> onOutcome(
                        ImageLoadOutcome(reloadKey + 1, "Loading", "-", elapsed)
                    )

                    is AsyncImagePainter.State.Success -> onOutcome(
                        ImageLoadOutcome(
                            reloadKey + 1,
                            "Success",
                            state.result.dataSource.name,
                            elapsed
                        )
                    )

                    is AsyncImagePainter.State.Error -> onOutcome(
                        ImageLoadOutcome(reloadKey + 1, "Error", "실패", elapsed)
                    )

                    AsyncImagePainter.State.Empty -> Unit
                }
            }
        )
    }
}

/** 회차별 결과를 최근 3건만 유지 — 같은 회차의 중간 상태는 마지막 값으로 갱신한다 */
private fun recordOutcome(
    history: MutableList<ImageLoadOutcome>,
    outcome: ImageLoadOutcome
) {
    val lastIndex = history.indexOfLast { it.attempt == outcome.attempt }
    if (lastIndex >= 0) {
        history[lastIndex] = outcome
    } else {
        history.add(outcome)
        if (history.size > 3) {
            history.removeAt(0)
        }
    }
}

@Composable
private fun DeterministicOutcomeHistory(
    history: List<ImageLoadOutcome>,
    color: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        if (history.isEmpty()) {
            Text(text = "로드 대기 중…", fontSize = 11.sp, color = Color(0xFF757575))
        } else {
            history.forEach { outcome ->
                Text(
                    text = "${outcome.attempt}회차 · ${outcome.stateLabel} · " +
                        "${outcome.source} · ${outcome.elapsedMs}ms",
                    fontSize = 11.sp,
                    color = color,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

// ==================== 3. 동작 메커니즘 ====================

@Composable
private fun PreviewHandlerMechanismCard() {
    DeterministicSectionCard(title = "3. 프리뷰 핸들러가 적용되는 조건") {
        DeterministicBullet(
            "AsyncImage 내부는 LocalInspectionMode.current 가 true 일 때만 " +
                "LocalAsyncImagePreviewHandler 를 읽는다. 즉 일반 앱 실행에는 아무 영향이 없고, " +
                "@Preview 와 스크린샷 러너가 이 모드를 켜준다."
        )
        DeterministicBullet(
            "inspection 모드만 켜고 핸들러를 주지 않으면 기본값 AsyncImagePreviewHandler.Default 가 " +
                "실제 ImageLoader.execute() 를 그대로 수행한다 — 결정론이 생기지 않는다. 두 CompositionLocal 을 함께 제공해야 한다."
        )
        DeterministicBullet(
            "팩토리 AsyncImagePreviewHandler { image } 가 만드는 상태는 State.Success 가 아니라 " +
                "painter 를 실은 State.Loading 이다. 픽셀은 고정되지만 onState 로 Success 를 기다리는 " +
                "대기 로직은 그대로 두면 끝나지 않는다(2번 카드의 상태 라벨이 그 증거)."
        )

        Spacer(modifier = Modifier.height(12.dp))

        DeterministicCodeBlock(
            code = "@OptIn(ExperimentalCoilApi::class)\n" +
                "val handler = AsyncImagePreviewHandler {\n" +
                "    ColorImage(0xFF66BB6A.toInt(), 420, 240)\n" +
                "}\n\n" +
                "CompositionLocalProvider(\n" +
                "    LocalInspectionMode provides true,       // 이게 false 면 핸들러는 조회조차 안 됨\n" +
                "    LocalAsyncImagePreviewHandler provides handler\n" +
                ") {\n" +
                "    AsyncImage(model = url, contentDescription = null)\n" +
                "}"
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(FIXED_IMAGE_COLOR))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "위 색이 ColorImage 로 고정된 픽셀 — 오른쪽 데모가 항상 이 색이다",
                fontSize = 12.sp,
                color = Color(0xFF616161)
            )
        }
    }
}

// ==================== 4. 테스트 소스셋 방식 ====================

@Composable
private fun CoilTestEngineCard() {
    DeterministicSectionCard(title = "4. 테스트 소스셋에서 ImageLoader 자체를 갈아끼우기") {
        Text(
            text = "프리뷰 핸들러는 컴포지션 트리 일부에만 적용된다. 화면 전체·테스트 전체를 덮으려면 " +
                "coil-test 의 FakeImageLoaderEngine 으로 ImageLoader 를 통째로 교체하는 편이 낫다.",
            fontSize = 13.sp,
            color = Color(0xFF424242),
            lineHeight = 19.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        DeterministicCodeBlock(
            code = "// testImplementation(\"io.coil-kt.coil3:coil-test:3.1.0\")\n\n" +
                "val engine = FakeImageLoaderEngine.Builder()\n" +
                "    .intercept(\"https://example.com/a.jpg\", ColorImage(GREEN))\n" +
                "    .default(ColorImage(BLUE))\n" +
                "    .build()\n\n" +
                "val loader = ImageLoader.Builder(context)\n" +
                "    .components { add(engine) }\n" +
                "    .build()\n\n" +
                "SingletonImageLoader.setUnsafe(loader)   // TestWatcher 룰의 starting() 에서 설정"
        )

        Spacer(modifier = Modifier.height(12.dp))

        DeterministicNoticeBox(
            text = "이 프로젝트는 coil-test 와 스크린샷 러너(Paparazzi/Roborazzi)를 의존성으로 두지 않으므로 " +
                "이 카드는 코드 스니펫으로만 시연한다. 실행되는 데모는 2번 카드의 프리뷰 핸들러 방식이며, " +
                "그쪽은 추가 의존성 없이 현재 coil3 만으로 동작한다."
        )
    }
}

// ==================== 5. 정리 ====================

@Composable
private fun ApproachComparisonCard() {
    DeterministicSectionCard(title = "5. 세 가지 접근 비교") {
        DeterministicTableRow(
            first = "방식",
            second = "적용 범위 / 프로덕션 코드 영향",
            isHeader = true
        )
        DeterministicTableRow(
            "프리뷰 핸들러",
            "CompositionLocal 을 제공한 서브트리 / 영향 없음(inspection 모드에서만 동작)"
        )
        DeterministicTableRow(
            "coil-test 엔진",
            "테스트가 쓰는 ImageLoader 전체 / 영향 없음(테스트 소스셋에만 존재)"
        )
        DeterministicTableRow(
            "직접 분기",
            "해당 컴포저블만 / 영향 있음(if (LocalInspectionMode.current) 분기가 프로덕션에 남음)"
        )

        Spacer(modifier = Modifier.height(12.dp))

        DeterministicBullet("가짜 이미지 주입은 픽셀 고정이 목적이지 로딩 로직 검증이 아니다 — 로딩·에러 상태 전이는 별도 테스트로 다룬다.")
        DeterministicBullet("골든 이미지에는 실제 사진 대신 단색이 찍히므로, 레이아웃·크기 회귀 검출에는 오히려 유리하다.")
        DeterministicBullet("Paparazzi/Roborazzi 실행 메커니즘은 ScreenshotTestingExampleUI, 매트릭스 파생은 PreviewDrivenScreenshotExampleUI 참조.")
    }
}

// ==================== 공통 UI 헬퍼 ====================

@Composable
private fun DeterministicSectionCard(
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
private fun DeterministicColumnHeader(
    label: String,
    badge: String,
    color: Color
) {
    Column {
        Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF212121))
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .background(color.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(text = badge, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = color)
        }
    }
}

@Composable
private fun DeterministicTableRow(
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
            modifier = Modifier.weight(0.32f),
            fontSize = 12.sp,
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Medium,
            color = Color(0xFF37474F)
        )
        Text(
            text = second,
            modifier = Modifier.weight(0.68f),
            fontSize = 12.sp,
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
            color = Color(0xFF546E7A),
            lineHeight = 17.sp
        )
    }
}

@Composable
private fun DeterministicBullet(text: String) {
    Row(modifier = Modifier.padding(vertical = 3.dp)) {
        Text(text = "• ", fontSize = 13.sp, color = Color(0xFF1976D2))
        Text(text = text, fontSize = 12.sp, color = Color(0xFF424242), lineHeight = 18.sp)
    }
}

@Composable
private fun DeterministicNoticeBox(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFF3E0), RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFFFCC80), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(text = text, fontSize = 12.sp, color = Color(0xFFE65100), lineHeight = 18.sp)
    }
}

@Composable
private fun DeterministicCodeBlock(code: String) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF263238))
    ) {
        Text(
            text = code,
            modifier = Modifier.padding(12.dp),
            color = Color(0xFFECEFF1),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            lineHeight = 16.sp
        )
    }
}
