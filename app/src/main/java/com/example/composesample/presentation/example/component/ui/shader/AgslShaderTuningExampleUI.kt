package com.example.composesample.presentation.example.component.ui.shader

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader

/**
 * AGSL Shader Live Tuning 예제
 *
 * Android 13(API 33)에서 도입된 `RuntimeShader`(AGSL/SkSL) 를 Compose 의
 * `graphicsLayer { renderEffect = ... }` 에 연결해 실시간으로 튜닝한다.
 *
 * - uniform float(noiseScale / colorShift / time) 값을 슬라이더로 조절 → 즉시 반영
 * - 셰이더 소스 코드 자체를 TextField 에서 편집 → `remember(shaderSource)` 키잉으로
 *   재컴파일, 컴파일 실패 시 에러 메시지 표시
 * - 셰이더는 하위 콘텐츠(uniform shader contents)를 sample 해 색을 변조 → renderEffect 가
 *   "그려진 픽셀 위에" 동작함을 보여줌
 *
 * minSdk 24 환경이므로 API 33 미만 단말에서는 placeholder UI 로 분기한다.
 * (참고 자료/핵심 개념은 exampleGuide.kt 참조)
 */
@Composable
fun AgslShaderTuningExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "AGSL Shader Live Tuning",
            onBackIconClicked = onBackEvent
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            AgslSupportedContent()
        } else {
            AgslUnsupportedPlaceholder()
        }
    }
}

/** 기본 셰이더 소스 — uniform 4종(resolution/time/noiseScale/colorShift) + 하위 콘텐츠 샘플링 */
private const val DEFAULT_SHADER = """uniform float2 resolution;
uniform float time;
uniform float noiseScale;
uniform float colorShift;
uniform shader contents;

// 간단한 해시 기반 의사 난수
float hash(float2 p) {
    return fract(sin(dot(p, float2(127.1, 311.7))) * 43758.5453);
}

half4 main(float2 fragCoord) {
    float2 uv = fragCoord / resolution;
    half4 src = contents.eval(fragCoord);

    // 격자 단위 노이즈 + 시간에 따라 변하는 색 파동
    float n = hash(floor(uv * noiseScale) + floor(time));
    float wave = 0.5 + 0.5 * sin(time + uv.x * 10.0 + colorShift * 6.2831);

    half3 tint = half3(wave, uv.y, 1.0 - wave);
    half3 mixed = mix(src.rgb, tint, 0.45) * (0.7 + 0.3 * n);
    return half4(mixed, src.a);
}"""

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun AgslSupportedContent() {
    // uniform 슬라이더 값
    var noiseScale by remember { mutableFloatStateOf(20f) }
    var colorShift by remember { mutableFloatStateOf(0f) }
    var speed by remember { mutableFloatStateOf(1f) }

    // 라이브 편집 중인 셰이더 소스 (재컴파일 트리거)
    var shaderSource by remember { mutableStateOf(DEFAULT_SHADER) }

    // 애니메이션용 누적 시간(초)
    var time by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        var last = 0L
        while (true) {
            withFrameNanos { now ->
                if (last != 0L) {
                    time += (now - last) / 1_000_000_000f * speed
                }
                last = now
            }
        }
    }

    // shaderSource 가 바뀔 때만 재컴파일 — 실패 시 에러 메시지 보관
    val compileResult = remember(shaderSource) {
        runCatching { RuntimeShader(shaderSource) }
    }
    val shader = compileResult.getOrNull()
    val compileError = compileResult.exceptionOrNull()?.message

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { OverviewCard() }

        // 셰이더 미리보기 — graphicsLayer renderEffect 에 RuntimeShader 연결
        item {
            ShaderPreview(
                shader = shader,
                time = time,
                noiseScale = noiseScale,
                colorShift = colorShift
            )
        }

        // uniform 슬라이더
        item {
            SectionCard(title = "Uniform 실시간 조절") {
                LabeledSlider(
                    label = "noiseScale",
                    value = noiseScale,
                    range = 2f..80f,
                    onValueChange = { noiseScale = it }
                )
                LabeledSlider(
                    label = "colorShift",
                    value = colorShift,
                    range = 0f..1f,
                    onValueChange = { colorShift = it }
                )
                LabeledSlider(
                    label = "speed(시간 배속)",
                    value = speed,
                    range = 0f..4f,
                    onValueChange = { speed = it }
                )
            }
        }

        // 셰이더 소스 라이브 편집
        item {
            SectionCard(title = "셰이더 소스 편집 (편집 즉시 재컴파일)") {
                Text(
                    text = "uniform 이름(resolution/time/noiseScale/colorShift/contents)을 " +
                        "지우면 컴파일/적용에 실패할 수 있습니다.",
                    fontSize = 12.sp,
                    color = Color(0xFF757575)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    BasicTextField(
                        value = shaderSource,
                        onValueChange = { shaderSource = it },
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = Color(0xFFD4D4D4),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (compileError != null) {
                    Text(
                        text = "❌ 컴파일 실패: $compileError",
                        fontSize = 12.sp,
                        color = Color(0xFFD32F2F)
                    )
                } else {
                    Text(
                        text = "✅ 컴파일 성공 — 미리보기에 즉시 반영됨",
                        fontSize = 12.sp,
                        color = Color(0xFF2E7D32)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { shaderSource = DEFAULT_SHADER },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF455A64))
                ) {
                    Text("기본 셰이더로 초기화")
                }
            }
        }
    }
}

/** RuntimeShader 를 graphicsLayer.renderEffect 로 적용한 미리보기 박스 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun ShaderPreview(
    shader: RuntimeShader?,
    time: Float,
    noiseScale: Float,
    colorShift: Float
) {
    SectionCard(title = "미리보기 (renderEffect)") {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.6f)
                .clip(RoundedCornerShape(12.dp))
                // 하위에 그려질 그라데이션 콘텐츠 — 셰이더가 이 픽셀을 sample 해 변조
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF1565C0), Color(0xFF7B1FA2), Color(0xFFD81B60))
                    )
                )
                .graphicsLayer {
                    val rs = shader ?: return@graphicsLayer
                    // uniform 설정 — 소스에 없는 uniform 을 set 하면 예외가 나므로 runCatching 으로 방어
                    runCatching {
                        rs.setFloatUniform("resolution", size.width, size.height)
                        rs.setFloatUniform("time", time)
                        rs.setFloatUniform("noiseScale", noiseScale)
                        rs.setFloatUniform("colorShift", colorShift)
                        renderEffect = RenderEffect
                            .createRuntimeShaderEffect(rs, "contents")
                            .asComposeRenderEffect()
                    }.onFailure {
                        // uniform 불일치 등으로 적용 불가 시 원본 콘텐츠 그대로 표시
                        renderEffect = null
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "AGSL",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun OverviewCard() {
    SectionCard(title = "AGSL Shader Live Tuning") {
        Text(
            text = "Android Graphics Shading Language(AGSL)로 작성한 RuntimeShader 를 " +
                "Compose 의 graphicsLayer.renderEffect 에 연결합니다. uniform 값을 슬라이더로 " +
                "바꾸면 매 프레임 재적용되고, 셰이더 소스를 편집하면 remember 키잉으로 재컴파일됩니다.",
            fontSize = 13.sp,
            color = Color(0xFF424242)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "• RuntimeShader / RenderEffect.createRuntimeShaderEffect → API 33+\n" +
                "• uniform shader contents 로 하위 콘텐츠를 sample 해 색 변조\n" +
                "• 소스 편집 → runCatching 으로 컴파일 에러 캡처",
            fontSize = 12.sp,
            color = Color(0xFF616161)
        )
    }
}

@Composable
private fun AgslUnsupportedPlaceholder() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SectionCard(title = "이 단말에서는 지원되지 않습니다") {
                Text(
                    text = "AGSL RuntimeShader 와 RenderEffect.createRuntimeShaderEffect 는 " +
                        "Android 13(API 33) 이상에서만 사용할 수 있습니다.\n" +
                        "현재 기기: API ${Build.VERSION.SDK_INT}",
                    fontSize = 13.sp,
                    color = Color(0xFF424242)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "API 33+ 단말 또는 에뮬레이터에서 다시 실행하면 셰이더 미리보기와 " +
                        "uniform/소스 라이브 편집 데모를 확인할 수 있습니다.",
                    fontSize = 12.sp,
                    color = Color(0xFF757575)
                )
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun LabeledSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, fontSize = 13.sp, color = Color(0xFF424242))
            Text(
                text = String.format("%.2f", value),
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF1565C0)
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range
        )
    }
}
