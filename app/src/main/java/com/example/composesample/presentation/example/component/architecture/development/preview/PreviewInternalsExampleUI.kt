package com.example.composesample.presentation.example.component.architecture.development.preview

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader

@Composable
fun PreviewInternalsExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E))
    ) {
        MainHeader(title = "Compose Preview Internals", onBackEvent = onBackEvent)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Section 1: @Preview 어노테이션 구조 ──────────────────────────
            item { SectionTitle("1. @Preview 어노테이션 구조") }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color(0xFF2D2D2D),
                    shape = RoundedCornerShape(8.dp),
                    elevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "@Preview는 런타임 동작이 없는 순수 메타데이터입니다.",
                            color = Color(0xFFAAAAAA),
                            fontSize = 13.sp
                        )
                        Spacer(Modifier.height(12.dp))
                        AnnotationPropertyCard(
                            property = "@Retention(BINARY)",
                            description = "컴파일된 .class 파일에 어노테이션이 유지됩니다.\nAndroid Studio가 클래스 파일을 스캔해 @Preview를 발견할 수 있는 이유입니다.",
                            color = Color(0xFF4A90D9)
                        )
                        Spacer(Modifier.height(8.dp))
                        AnnotationPropertyCard(
                            property = "@Target(ANNOTATION_CLASS, FUNCTION)",
                            description = "다른 어노테이션을 장식할 수 있어 MultiPreview 어노테이션 정의가 가능합니다.\n예: @FontScalePreviews, @PreviewLightDark",
                            color = Color(0xFF7B68EE)
                        )
                        Spacer(Modifier.height(8.dp))
                        AnnotationPropertyCard(
                            property = "@Repeatable",
                            description = "단일 함수에 여러 @Preview를 선언할 수 있습니다.\n각 선언이 별도 미리보기 패널로 나타납니다.",
                            color = Color(0xFF50C878)
                        )
                    }
                }
            }

            // ── Section 2: 렌더링 파이프라인 ────────────────────────────────
            item { Spacer(Modifier.height(4.dp)) }
            item { SectionTitle("2. 렌더링 파이프라인 (5단계)") }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    PipelineStep(
                        step = 1,
                        title = "PSI/UAST 스캔",
                        description = "Studio가 @Retention(BINARY)로 보존된 바이트코드를 스캔하여 @Preview 어노테이션이 붙은 함수를 발견합니다.",
                        color = Color(0xFF4A90D9)
                    )
                    StepArrow()
                    PipelineStep(
                        step = 2,
                        title = "XML 생성",
                        description = "ComposeViewAdapter를 참조하는 합성 XML 레이아웃을 생성합니다. tools: 네임스페이스 속성으로 composable 정보를 전달합니다.",
                        color = Color(0xFF7B68EE)
                    )
                    StepArrow()
                    PipelineStep(
                        step = 3,
                        title = "ComposeViewAdapter 오케스트레이션",
                        description = "FrameLayout 기반 어댑터가 가짜 Lifecycle을 주입합니다.\n• FakeSavedStateRegistryOwner → RESUMED 상태 즉시 설정\n• Activity result 같은 실제 기능은 명확한 예외로 차단",
                        color = Color(0xFFFF8C00)
                    )
                    StepArrow()
                    PipelineStep(
                        step = 4,
                        title = "ComposableInvoker (Reflection)",
                        description = "Compose 컴파일러가 추가한 \$composer, \$changed, \$default 파라미터를 포함한 변환된 함수 시그니처를 Reflection으로 직접 호출합니다.\n• \$changed = 0 (재평가 강제)\n• \$default 비트 = 1 (기본값 사용)",
                        color = Color(0xFFFF6B6B)
                    )
                    StepArrow()
                    PipelineStep(
                        step = 5,
                        title = "ViewInfo 트리 생성",
                        description = "컴포지션 데이터를 순회하여 ViewInfo 객체를 생성합니다. 각 ViewInfo는 소스 파일, 줄 번호, 픽셀 범위를 포함하여 렌더링 픽셀 ↔ 소스 코드 매핑을 가능하게 합니다.",
                        color = Color(0xFF50C878)
                    )
                }
            }

            // ── Section 3: LocalInspectionMode 인터랙티브 데모 ──────────────
            item { Spacer(Modifier.height(4.dp)) }
            item { SectionTitle("3. LocalInspectionMode 인터랙티브 데모") }
            item { LocalInspectionModeDemo() }

            // ── Section 4: 내장 MultiPreview 어노테이션 ─────────────────────
            item { Spacer(Modifier.height(4.dp)) }
            item { SectionTitle("4. 내장 MultiPreview 어노테이션") }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color(0xFF2D2D2D),
                    shape = RoundedCornerShape(8.dp),
                    elevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "@Target(ANNOTATION_CLASS) 덕분에 내장 MultiPreview 어노테이션을 그대로 활용할 수 있습니다.",
                            color = Color(0xFFAAAAAA),
                            fontSize = 13.sp,
                            lineHeight = 19.sp
                        )
                        Spacer(Modifier.height(12.dp))
                        MultiPreviewCard(
                            annotation = "@PreviewLightDark",
                            import = "androidx.compose.ui.tooling.preview.PreviewLightDark",
                            description = "라이트 / 다크 테마 동시 미리보기"
                        )
                        Spacer(Modifier.height(8.dp))
                        MultiPreviewCard(
                            annotation = "@PreviewScreenSizes",
                            import = "androidx.compose.ui.tooling.preview.PreviewScreenSizes",
                            description = "폰 · 태블릿 · 폴더블 등 다양한 화면 크기 동시 미리보기"
                        )
                        Spacer(Modifier.height(8.dp))
                        MultiPreviewCard(
                            annotation = "@PreviewFontScale",
                            import = "androidx.compose.ui.tooling.preview.PreviewFontScale",
                            description = "0.85 / 1.0 / 1.15 / 1.3 / 1.5 / 1.8 / 2.0 배율 동시 미리보기"
                        )
                        Spacer(Modifier.height(8.dp))
                        MultiPreviewCard(
                            annotation = "@PreviewDynamicColors",
                            import = "androidx.compose.ui.tooling.preview.PreviewDynamicColors",
                            description = "Dynamic Color 색상 변형 동시 미리보기 (Android 12+)"
                        )
                        Spacer(Modifier.height(12.dp))
                        Divider(color = Color(0xFF444444))
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "커스텀 MultiPreview 예시 (기존 PreviewExample.kt 참고):",
                            color = Color(0xFF888888),
                            fontSize = 12.sp
                        )
                        Spacer(Modifier.height(6.dp))
                        CodeSnippet(
                            code = """@Preview(name = "small font", fontScale = 0.5f)
@Preview(name = "large font", fontScale = 1.5f)
annotation class FontScalePreviews

@FontScalePreviews
@Composable
fun MyComposable() { ... }"""
                        )
                    }
                }
            }

            // ── Section 5: PreviewParameter 고급 활용 ───────────────────────
            item { Spacer(Modifier.height(4.dp)) }
            item { SectionTitle("5. PreviewParameter — sealed class 상태 한 번에 미리보기") }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color(0xFF2D2D2D),
                    shape = RoundedCornerShape(8.dp),
                    elevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "여러 UI 상태를 PreviewParameterProvider로 정의하면\n각 상태가 별도 미리보기 패널로 자동 생성됩니다.",
                            color = Color(0xFFAAAAAA),
                            fontSize = 13.sp,
                            lineHeight = 19.sp
                        )
                        Spacer(Modifier.height(10.dp))
                        CodeSnippet(
                            code = """sealed class ContentUiState {
    object Loading : ContentUiState()
    data class Success(val title: String) : ContentUiState()
    data class Error(val msg: String) : ContentUiState()
}

class ContentUiStateProvider
    : PreviewParameterProvider<ContentUiState> {
    override val values = sequenceOf(
        ContentUiState.Loading,
        ContentUiState.Success("제목"),
        ContentUiState.Error("오류 발생")
    )
}

@Preview
@Composable
fun ContentPreview(
    @PreviewParameter(ContentUiStateProvider::class)
    state: ContentUiState
) {
    ContentScreen(state = state)
}"""
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "✓ 상태 추가 시 Provider에 항목만 추가하면 자동으로 미리보기 패널 생성\n✓ 실제 UI 구현 변경 없이 다양한 상태 검증 가능",
                            color = Color(0xFF88CC88),
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

// ─── LocalInspectionMode 인터랙티브 데모 ──────────────────────────────────────

@Composable
private fun LocalInspectionModeDemo() {
    // 실제 앱에서는 LocalInspectionMode가 항상 false
    // 이 데모에서는 토글로 두 모드의 차이를 시뮬레이션
    var simulateInspectionMode by remember { mutableStateOf(false) }
    val isInspectionMode = LocalInspectionMode.current || simulateInspectionMode

    Card(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color(0xFF2D2D2D),
        shape = RoundedCornerShape(8.dp),
        elevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Preview 모드 시뮬레이션",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "LocalInspectionMode.current",
                        color = Color(0xFF888888),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Switch(
                    checked = simulateInspectionMode,
                    onCheckedChange = { simulateInspectionMode = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF50C878),
                        checkedTrackColor = Color(0xFF50C878).copy(alpha = 0.5f)
                    )
                )
            }

            Spacer(Modifier.height(14.dp))

            // 모드에 따라 다르게 렌더링되는 컴포넌트 시뮬레이션
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = if (isInspectionMode) Color(0xFF1A2D1A) else Color(0xFF1A1A2D),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = if (isInspectionMode) Color(0xFF50C878) else Color(0xFF4A90D9),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .padding(14.dp)
            ) {
                Column {
                    Text(
                        text = if (isInspectionMode) "📐 Preview 환경" else "📱 실제 앱 환경",
                        color = if (isInspectionMode) Color(0xFF50C878) else Color(0xFF4A90D9),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(6.dp))
                    if (isInspectionMode) {
                        Text(
                            text = "• 더미 데이터로 즉시 렌더링\n• 실제 API / DB 호출 없음\n• FakeLifecycle 주입 (RESUMED 상태)\n• Activity result 호출 시 예외 발생",
                            color = Color(0xFF88CC88),
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    } else {
                        Text(
                            text = "• 실제 데이터 소스 사용\n• 정상 Lifecycle 적용\n• 네트워크 / DB 접근 가능\n• Activity result 정상 동작",
                            color = Color(0xFF8888CC),
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Divider(color = Color(0xFF444444))
            Spacer(Modifier.height(12.dp))

            Text(text = "코드 패턴:", color = Color(0xFF888888), fontSize = 12.sp)
            Spacer(Modifier.height(6.dp))
            CodeSnippet(
                code = """@Composable
fun MyComponent() {
    val isPreview = LocalInspectionMode.current
    if (isPreview) {
        // Preview 전용: 더미 데이터, 간단한 렌더링
        Text("Preview Placeholder")
    } else {
        // 실제 앱: ViewModel, API 호출 등
        RealContent()
    }
}"""
            )

            Spacer(Modifier.height(8.dp))
            Text(
                text = "⚠ 기존 커스텀 LocalPreviewMode 방식보다 표준 LocalInspectionMode 사용을 권장합니다.",
                color = Color(0xFFFFAA44),
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }
    }
}

// ─── 재사용 컴포넌트 ──────────────────────────────────────────────────────────

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = Color.White,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun AnnotationPropertyCard(property: String, description: String, color: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E1E1E), RoundedCornerShape(6.dp))
            .padding(10.dp)
    ) {
        Text(
            text = property,
            color = color,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Spacer(Modifier.height(4.dp))
        Text(text = description, color = Color(0xFFCCCCCC), fontSize = 12.sp, lineHeight = 18.sp)
    }
}

@Composable
private fun PipelineStep(step: Int, title: String, description: String, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color(0xFF2D2D2D),
        shape = RoundedCornerShape(8.dp),
        elevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(color, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$step",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(text = title, color = color, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(text = description, color = Color(0xFFCCCCCC), fontSize = 12.sp, lineHeight = 18.sp)
            }
        }
    }
}

@Composable
private fun StepArrow() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 22.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(text = "↓", color = Color(0xFF555555), fontSize = 14.sp)
    }
}

@Composable
private fun MultiPreviewCard(annotation: String, import: String, description: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E1E1E), RoundedCornerShape(6.dp))
            .padding(10.dp)
    ) {
        Text(
            text = annotation,
            color = Color(0xFF7B9FFF),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = import,
            color = Color(0xFF555555),
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )
        Spacer(Modifier.height(4.dp))
        Text(text = description, color = Color(0xFFCCCCCC), fontSize = 12.sp)
    }
}

@Composable
private fun CodeSnippet(code: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1A1A1A), RoundedCornerShape(6.dp))
            .padding(10.dp)
    ) {
        Text(
            text = code,
            color = Color(0xFFE0E0E0),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            lineHeight = 17.sp
        )
    }
}
