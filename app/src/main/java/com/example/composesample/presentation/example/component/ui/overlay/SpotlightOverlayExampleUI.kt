package com.example.composesample.presentation.example.component.ui.overlay

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.example.composesample.presentation.MainHeader
import kotlin.math.roundToInt

// 스포트라이트가 순서대로 강조할 타깃 — 라벨(미니 아이콘 표시용)과 설명(툴팁 문구)
private data class SpotlightStep(val label: String, val description: String)

private val spotlightSteps = listOf(
    SpotlightStep("홈", "홈 화면으로 돌아가는 버튼입니다."),
    SpotlightStep("검색", "콘텐츠를 검색할 때 사용합니다."),
    SpotlightStep("설정", "알림/테마 등 앱 설정을 변경합니다.")
)

@Composable
fun SpotlightOverlayExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "좌표 기반 스포트라이트 오버레이",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { ConceptCard() }
            item { SpotlightDemoCard() }
            item { TechniqueCard() }
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
                text = "코치마크 오버레이 개요",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "기능 소개(코치마크)는 화면 위 특정 요소만 어두운 배경 속에서 도려내듯 강조합니다. " +
                        "핵심은 대상 요소의 화면 좌표를 알아내는 것과, 그 좌표에 맞춰 화면 전체를 덮는 오버레이를 " +
                        "부모의 클리핑 영역에 갇히지 않고 띄우는 것입니다.",
                fontSize = 13.sp,
                color = Color(0xFF424242),
                lineHeight = 19.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            val steps = listOf(
                Triple("① 좌표 수집", "onGloballyPositioned", "타깃 컴포저블의 boundsInWindow()를 기록"),
                Triple("② 전체화면 오버레이", "Popup", "부모 Composable의 크기/클리핑에 갇히지 않고 별도 창처럼 렌더"),
                Triple("③ 구멍 뚫기", "Path.op + clipPath", "전체 영역 Path에서 타깃 영역 Path를 Difference로 빼 스크림에 구멍을 냄"),
                Triple("④ 스텝 전환", "animateFloatAsState", "다음 타깃으로 넘어갈 때 구멍의 좌표를 애니메이션으로 이동")
            )
            steps.forEach { (label, api, note) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(0.22f))
                    Text(text = api, fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF1976D2), modifier = Modifier.weight(0.3f))
                    Text(text = note, fontSize = 10.sp, color = Color(0xFF757575), modifier = Modifier.weight(0.48f))
                }
            }
        }
    }
}

@Composable
private fun SpotlightDemoCard() {
    var screenBoundsInWindow by remember { mutableStateOf(Rect.Zero) }
    val targetBoundsInWindow = remember { mutableStateListOf(Rect.Zero, Rect.Zero, Rect.Zero) }
    var currentStep by remember { mutableStateOf(-1) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "데모 — 미니 화면에서 코치마크 재생",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF388E3C)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "아이콘 3개의 좌표를 onGloballyPositioned로 미리 수집해 둡니다. " +
                        "'코치마크 시작'을 누르면 그 좌표를 그대로 재사용해 Popup 오버레이가 순서대로 강조합니다.",
                fontSize = 12.sp,
                color = Color(0xFF757575),
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF263238))
                    .onGloballyPositioned { coordinates ->
                        screenBoundsInWindow = coordinates.boundsInWindow()
                    }
            ) {
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    spotlightSteps.forEachIndexed { index, step ->
                        MockIconTarget(
                            label = step.label,
                            highlighted = currentStep == index,
                            modifier = Modifier.onGloballyPositioned { coordinates ->
                                targetBoundsInWindow[index] = coordinates.boundsInWindow()
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { currentStep = 0 },
                enabled = currentStep == -1,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))
            ) {
                Text(text = "코치마크 시작", fontSize = 12.sp)
            }
        }
    }

    if (currentStep in spotlightSteps.indices && screenBoundsInWindow != Rect.Zero) {
        SpotlightPopup(
            screenBoundsInWindow = screenBoundsInWindow,
            targetBoundsInWindow = targetBoundsInWindow,
            currentStep = currentStep,
            onNext = {
                currentStep = if (currentStep + 1 < spotlightSteps.size) currentStep + 1 else -1
            },
            onSkip = { currentStep = -1 }
        )
    }
}

@Composable
private fun MockIconTarget(label: String, highlighted: Boolean, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (highlighted) Color(0xFFFFC107) else Color(0xFF37474F)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = label.take(1), color = Color.White, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, fontSize = 10.sp, color = Color.White)
    }
}

@Composable
private fun SpotlightPopup(
    screenBoundsInWindow: Rect,
    targetBoundsInWindow: List<Rect>,
    currentStep: Int,
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    val density = LocalDensity.current
    val screenWidthDp = with(density) { screenBoundsInWindow.width.toDp() }
    val screenHeightDp = with(density) { screenBoundsInWindow.height.toDp() }

    // 타깃 좌표를 미니 화면(=Popup 콘텐츠) 로컬 좌표로 변환 — window 좌표에서 화면 좌상단 오프셋을 뺀다
    val localTarget = targetBoundsInWindow[currentStep].translate(
        -screenBoundsInWindow.left,
        -screenBoundsInWindow.top
    )

    // 스텝이 바뀔 때 구멍이 이전 타깃에서 다음 타깃으로 미끄러지듯 이동 — Canvas 드로우 단계에서만 값을 읽어 리컴포지션 없이 재드로우만 유발
    val animLeft = animateFloatAsState(targetValue = localTarget.left, label = "spotlightLeft")
    val animTop = animateFloatAsState(targetValue = localTarget.top, label = "spotlightTop")
    val animRight = animateFloatAsState(targetValue = localTarget.right, label = "spotlightRight")
    val animBottom = animateFloatAsState(targetValue = localTarget.bottom, label = "spotlightBottom")

    Popup(
        popupPositionProvider = remember(screenBoundsInWindow) {
            FixedWindowOffsetPositionProvider(
                IntOffset(
                    screenBoundsInWindow.left.roundToInt(),
                    screenBoundsInWindow.top.roundToInt()
                )
            )
        },
        properties = PopupProperties(focusable = true, dismissOnClickOutside = false)
    ) {
        Box(
            modifier = Modifier
                .size(screenWidthDp, screenHeightDp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val holePadding = 8.dp.toPx()
                val cornerRadiusPx = 16.dp.toPx()
                val holeRect = Rect(
                    left = animLeft.value - holePadding,
                    top = animTop.value - holePadding,
                    right = animRight.value + holePadding,
                    bottom = animBottom.value + holePadding
                )

                val fullScreenPath = Path().apply { addRect(Rect(Offset.Zero, size)) }
                val holePath = Path().apply {
                    addRoundRect(RoundRect(holeRect, cornerRadiusPx, cornerRadiusPx))
                }
                // "전체 화면 - 타깃 영역" 모양의 Path — 이 Path로 clip한 뒤 채우면 타깃 영역만 원본이 비쳐 보이는 구멍이 생긴다
                val scrimPath = Path().apply {
                    op(fullScreenPath, holePath, PathOperation.Difference)
                }

                clipPath(scrimPath) {
                    drawRect(color = Color.Black.copy(alpha = 0.75f))
                }

                drawRoundRect(
                    color = Color(0xFFFFC107),
                    topLeft = Offset(holeRect.left, holeRect.top),
                    size = Size(holeRect.width, holeRect.height),
                    cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(12.dp)
                    .background(Color.White, RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "${currentStep + 1} / ${spotlightSteps.size}",
                    fontSize = 11.sp,
                    color = Color(0xFF757575)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = spotlightSteps[currentStep].description,
                    fontSize = 13.sp,
                    color = Color(0xFF212121)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onSkip) {
                        Text(text = "건너뛰기", fontSize = 12.sp)
                    }
                    Button(
                        onClick = onNext,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                    ) {
                        Text(
                            text = if (currentStep == spotlightSteps.size - 1) "완료" else "다음",
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

// Popup을 앵커 위치와 무관하게 window 좌표계의 고정 지점(미니 화면의 좌상단)에 배치하기 위한 위치 제공자
private class FixedWindowOffsetPositionProvider(private val offset: IntOffset) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset = offset
}

@Composable
private fun TechniqueCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "구멍 뚫기 — Path.op(Difference) + clipPath",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "전체 화면 크기의 Path에서 타깃 영역 RoundRect Path를 PathOperation.Difference로 빼면, " +
                        "\"화면 전체 - 타깃 영역\" 모양의 Path가 남습니다. 이 Path로 clipPath를 걸고 어두운 색을 채우면 " +
                        "타깃 영역만 원본 색 그대로 비쳐 보이는 구멍이 생깁니다.",
                fontSize = 12.sp,
                color = Color(0xFF757575),
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            CodeBlock(
                code = "val scrimPath = Path().apply {\n" +
                        "    op(fullScreenPath, holePath, PathOperation.Difference)\n" +
                        "}\n" +
                        "clipPath(scrimPath) {\n" +
                        "    drawRect(color = Color.Black.copy(alpha = 0.75f))\n" +
                        "}"
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "왜 Popup인가: Box 안에 그냥 겹쳐 그리면 부모(Card/LazyColumn 등)의 clip이나 패딩에 갇혀 스크림이 " +
                        "화면 전체를 덮지 못합니다. Popup은 별도 창으로 렌더되어 어떤 부모의 경계에도 갇히지 않습니다.",
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
                text = "핵심 정리",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(8.dp))
            val bullets = listOf(
                "onGloballyPositioned + boundsInWindow()로 타깃 컴포저블의 화면 좌표를 얻는다",
                "Popup은 부모의 클리핑/패딩 경계에 갇히지 않고 별도 창처럼 렌더되어 전체화면 오버레이에 적합하다",
                "Path.op(full, hole, PathOperation.Difference)로 \"전체 - 타깃\" 모양의 Path를 만들고 clipPath로 스크림에 구멍을 낸다",
                "스텝이 바뀔 때 구멍의 좌표(left/top/right/bottom)를 animateFloatAsState로 애니메이션하면 다음 타깃으로 자연스럽게 이동한다",
                "PopupPositionProvider를 직접 구현하면 Popup을 앵커와 무관하게 원하는 좌표에 고정 배치할 수 있다"
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
private fun CodeBlock(code: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
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
