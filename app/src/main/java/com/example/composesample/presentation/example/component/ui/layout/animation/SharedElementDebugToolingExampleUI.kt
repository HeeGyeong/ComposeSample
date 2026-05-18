package com.example.composesample.presentation.example.component.ui.layout.animation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalLookaheadAnimationVisualDebugApi
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.LookaheadAnimationVisualDebugging
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Shared Element Debug Tooling 예제 UI
 *
 * Compose 1.11 신규 API: `LookaheadAnimationVisualDebugging` Composable 와
 * `@ExperimentalLookaheadAnimationVisualDebugApi` opt-in 으로 SharedTransition 의
 * (1) 매칭된 공유 요소 오버레이 (2) 동일 key 다중 매칭 (3) 미매칭 요소 를 화면에서 직접 시각화한다.
 *
 * 의도적으로 만든 3가지 상황을 디버그 모드 on/off 로 비교하여, 잘못된 key 사용·중복 정의·
 * 한쪽 화면에만 존재하는 sharedElement 를 런타임에서 즉시 발견할 수 있는 사용법을 보여준다.
 */
@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalLookaheadAnimationVisualDebugApi::class)
@Composable
fun SharedElementDebugToolingExampleUI(
    onBackEvent: () -> Unit
) {
    var debugEnabled by remember { mutableStateOf(true) }
    var showKeyLabel by remember { mutableStateOf(true) }
    var colorPreset by remember { mutableStateOf(ColorPreset.DEFAULT) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        HeaderCard(onBackEvent = onBackEvent)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ControlsCard(
                    debugEnabled = debugEnabled,
                    onDebugToggle = { debugEnabled = it },
                    showKeyLabel = showKeyLabel,
                    onShowKeyLabelToggle = { showKeyLabel = it },
                    colorPreset = colorPreset,
                    onColorPresetChange = { colorPreset = it }
                )
            }

            // LookaheadAnimationVisualDebugging 은 자식 SharedTransitionLayout 의
            // 매칭 정보를 가로채서 오버레이를 그린다. content 람다 안에서만 디버그가 활성화됨.
            item {
                LookaheadAnimationVisualDebugging(
                    isEnabled = debugEnabled,
                    overlayColor = colorPreset.overlayColor,
                    multipleMatchesColor = colorPreset.multipleMatchesColor,
                    unmatchedElementColor = colorPreset.unmatchedElementColor,
                    isShowKeyLabelEnabled = showKeyLabel
                ) {
                    NormalMatchSection()
                }
            }

            item {
                LookaheadAnimationVisualDebugging(
                    isEnabled = debugEnabled,
                    overlayColor = colorPreset.overlayColor,
                    multipleMatchesColor = colorPreset.multipleMatchesColor,
                    unmatchedElementColor = colorPreset.unmatchedElementColor,
                    isShowKeyLabelEnabled = showKeyLabel
                ) {
                    MultipleMatchesSection()
                }
            }

            item {
                LookaheadAnimationVisualDebugging(
                    isEnabled = debugEnabled,
                    overlayColor = colorPreset.overlayColor,
                    multipleMatchesColor = colorPreset.multipleMatchesColor,
                    unmatchedElementColor = colorPreset.unmatchedElementColor,
                    isShowKeyLabelEnabled = showKeyLabel
                ) {
                    UnmatchedSection()
                }
            }

            item { TestCoroutineApiNoteCard() }
        }
    }
}

@Composable
private fun HeaderCard(onBackEvent: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackEvent) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF1976D2)
                )
            }
            Spacer(modifier = Modifier.size(8.dp))
            Column {
                Text(
                    text = "Shared Element Debug Tooling",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )
                Text(
                    text = "Compose 1.11 LookaheadAnimationVisualDebugging",
                    fontSize = 13.sp,
                    color = Color(0xFF757575)
                )
            }
        }
    }
}

@Composable
private fun ControlsCard(
    debugEnabled: Boolean,
    onDebugToggle: (Boolean) -> Unit,
    showKeyLabel: Boolean,
    onShowKeyLabelToggle: (Boolean) -> Unit,
    colorPreset: ColorPreset,
    onColorPresetChange: (ColorPreset) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Debug Controls",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Debug 오버레이", modifier = Modifier.weight(1f), fontSize = 14.sp)
                Switch(checked = debugEnabled, onCheckedChange = onDebugToggle)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Key 라벨 표시", modifier = Modifier.weight(1f), fontSize = 14.sp)
                Switch(checked = showKeyLabel, onCheckedChange = onShowKeyLabelToggle)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "색상 프리셋",
                fontSize = 13.sp,
                color = Color(0xFF616161)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ColorPreset.entries.forEach { preset ->
                    FilterChip(
                        selected = colorPreset == preset,
                        onClick = { onColorPresetChange(preset) },
                        label = { Text(text = preset.label, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFBBDEFB)
                        )
                    )
                }
            }
        }
    }
}

// 1. 정상 매칭 — 같은 key 가 양쪽 상태에 정확히 하나씩 존재한다. 디버그 오버레이는 overlayColor 로 표시.
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun NormalMatchSection() {
    var expanded by remember { mutableStateOf(false) }

    SectionCard(
        title = "1. 정상 매칭 (Matched)",
        description = "동일 key 를 가진 sharedElement 가 양쪽에 정확히 하나씩 존재. 오버레이는 overlayColor 로 표시됨."
    ) {
        SharedTransitionLayout {
            AnimatedContent(
                targetState = expanded,
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
                label = "normal_match"
            ) { isExpanded ->
                if (isExpanded) {
                    NormalExpanded(
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedContentScope = this@AnimatedContent,
                        onCollapse = { expanded = false }
                    )
                } else {
                    NormalCollapsed(
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedContentScope = this@AnimatedContent,
                        onExpand = { expanded = true }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun NormalCollapsed(
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onExpand: () -> Unit
) {
    with(sharedTransitionScope) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .sharedBounds(
                    rememberSharedContentState(key = "match_container"),
                    animatedVisibilityScope = animatedContentScope,
                    boundsTransform = { _, _ ->
                        spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    }
                )
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF42A5F5))
                .clickable(onClick = onExpand),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .sharedElement(
                            rememberSharedContentState(key = "match_avatar"),
                            animatedVisibilityScope = animatedContentScope
                        )
                        .clip(CircleShape)
                        .background(Color.White)
                )
                Spacer(modifier = Modifier.size(12.dp))
                Text(text = "탭하여 펼치기", color = Color.White, fontSize = 14.sp)
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun NormalExpanded(
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onCollapse: () -> Unit
) {
    with(sharedTransitionScope) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .sharedBounds(
                    rememberSharedContentState(key = "match_container"),
                    animatedVisibilityScope = animatedContentScope,
                    boundsTransform = { _, _ ->
                        spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    }
                )
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1976D2))
                .clickable(onClick = onCollapse)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .sharedElement(
                        rememberSharedContentState(key = "match_avatar"),
                        animatedVisibilityScope = animatedContentScope
                    )
                    .clip(CircleShape)
                    .background(Color.White)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "탭하여 접기", color = Color.White, fontSize = 14.sp)
        }
    }
}

// 2. 동일 key 다중 매칭 — 같은 key 의 sharedElement 를 한 상태에 두 개 두는 안티패턴.
// multipleMatchesColor 로 강조되어 어떤 요소가 충돌 중인지 즉시 확인 가능.
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun MultipleMatchesSection() {
    var swapped by remember { mutableStateOf(false) }

    SectionCard(
        title = "2. 동일 key 다중 매칭 (Multiple Matches)",
        description = "같은 key 의 sharedElement 가 한 화면에 둘 이상. multipleMatchesColor 로 강조됨."
    ) {
        SharedTransitionLayout {
            AnimatedContent(
                targetState = swapped,
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
                label = "multiple_matches"
            ) { isSwapped ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { swapped = !swapped },
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 의도적으로 같은 key 를 두 박스에 동시 적용 → 디버그 툴이 강조
                    with(this@SharedTransitionLayout) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .sharedElement(
                                    rememberSharedContentState(key = "duplicate_key"),
                                    animatedVisibilityScope = this@AnimatedContent
                                )
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSwapped) Color(0xFFE57373) else Color(0xFF81C784))
                        )
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .sharedElement(
                                    rememberSharedContentState(key = "duplicate_key"),
                                    animatedVisibilityScope = this@AnimatedContent
                                )
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSwapped) Color(0xFF81C784) else Color(0xFFE57373))
                        )
                    }
                    Text(
                        text = "탭하여 색 토글",
                        fontSize = 13.sp,
                        color = Color(0xFF424242)
                    )
                }
            }
        }
    }
}

// 3. 미매칭 요소 — 한쪽 상태에만 key 가 존재. unmatchedElementColor 로 표시되어 누락 발견 용이.
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun UnmatchedSection() {
    var showRight by remember { mutableStateOf(false) }

    SectionCard(
        title = "3. 미매칭 요소 (Unmatched)",
        description = "한쪽 상태에만 sharedElement 가 존재. unmatchedElementColor 로 표시됨."
    ) {
        SharedTransitionLayout {
            AnimatedContent(
                targetState = showRight,
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
                label = "unmatched"
            ) { right ->
                with(this@SharedTransitionLayout) {
                    if (right) {
                        // 오른쪽 상태에만 lonely_key 존재 → 토글 시 unmatched 강조
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .sharedElement(
                                    rememberSharedContentState(key = "lonely_key"),
                                    animatedVisibilityScope = this@AnimatedContent
                                )
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFFA726))
                                .clickable { showRight = false },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "오직 이 상태에만 sharedElement", color = Color.White, fontSize = 13.sp)
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFE0E0E0))
                                .clickable { showRight = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "탭하여 다른 상태로 — 매칭 짝 없음", color = Color(0xFF424242), fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    description: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF212121))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = description, fontSize = 13.sp, color = Color(0xFF616161))
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

// Compose 1.11 신규 SharedTransition test coroutine API 안내 — 시연 대신 코드 스니펫만 노출.
// runComposeUiTest 안에서 advanceTimeBy/awaitFrame 으로 sharedElement 전이 중간 프레임을 제어 가능.
@Composable
private fun TestCoroutineApiNoteCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD54F))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Compose 1.11 — SharedTransition Test Coroutine API",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6D4C41)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "runComposeUiTest 안에서 mainClock 의 advanceTimeBy 와 coroutine API 로 sharedElement 전이의 중간 프레임을 검증할 수 있다.",
                fontSize = 13.sp,
                color = Color(0xFF5D4037)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF263238))
                    .border(1.dp, Color(0xFF455A64), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = SAMPLE_TEST_SNIPPET,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFFE0F2F1)
                )
            }
        }
    }
}

private const val SAMPLE_TEST_SNIPPET = """@OptIn(ExperimentalTestApi::class)
@Test
fun sharedElement_animatesBoundsAcrossFrames() = runComposeUiTest {
    mainClock.autoAdvance = false
    setContent { MySharedTransitionScreen(expanded = false) }

    onNodeWithTag("trigger").performClick()
    mainClock.advanceTimeByFrame()      // 1 frame
    mainClock.advanceTimeBy(150L)       // mid-transition
    onNodeWithTag("hero").assertExists()

    mainClock.advanceTimeBy(500L)       // settle
    mainClock.autoAdvance = true
}"""

private enum class ColorPreset(
    val label: String,
    val overlayColor: Color,
    val multipleMatchesColor: Color,
    val unmatchedElementColor: Color
) {
    DEFAULT(
        label = "Default",
        overlayColor = Color(0x4D2196F3),
        multipleMatchesColor = Color(0x80F44336),
        unmatchedElementColor = Color(0x80FF9800)
    ),
    HIGH_CONTRAST(
        label = "High Contrast",
        overlayColor = Color(0x80000000),
        multipleMatchesColor = Color(0xFFFF1744),
        unmatchedElementColor = Color(0xFFFFEA00)
    ),
    PASTEL(
        label = "Pastel",
        overlayColor = Color(0x66B39DDB),
        multipleMatchesColor = Color(0x80EF9A9A),
        unmatchedElementColor = Color(0x80FFCC80)
    );
}

@Preview(showBackground = true)
@Composable
fun SharedElementDebugToolingExampleUIPreview() {
    SharedElementDebugToolingExampleUI(onBackEvent = {})
}
