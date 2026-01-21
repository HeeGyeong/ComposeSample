package com.example.composesample.presentation.example.component.ui.text

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.min
import kotlin.math.sin

@Composable
fun CustomTextRenderingExampleUI(
    onBackEvent: () -> Unit
) {
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
            item { FadedTextCard() }
            item { WarpedTextCard() }
            item { AnimatedWarpedTextCard() }
            item { TypewriterTextCard() }
        }
    }
}

@Composable
private fun HeaderCard(onBackEvent: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = 4.dp,
        backgroundColor = Color.White,
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

            Column {
                Text(
                    text = "✍️ Custom Text Rendering",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1976D2)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "TextMeasurer와 Canvas로 커스텀 효과",
                    fontSize = 12.sp,
                    color = Color(0xFF666666)
                )
            }
        }
    }
}

@Composable
private fun FadedTextCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color.White,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "1. Faded Text (페이드 효과)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "각 라인마다 점진적으로 알파값이 증가하여 마지막 라인이 완전히 보입니다",
                fontSize = 12.sp,
                color = Color(0xFF666666),
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            FadedText(
                text = "첫 번째 라인은 거의 투명합니다\n" +
                        "두 번째 라인은 조금 더 진해집니다\n" +
                        "세 번째 라인은 더욱 선명해집니다\n" +
                        "네 번째 라인은 거의 보이게 됩니다\n" +
                        "마지막 라인은 완전히 보입니다",
                textStyle = TextStyle(
                    fontSize = 14.sp,
                    color = Color(0xFF333333),
                    lineHeight = 20.sp
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun WarpedTextCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color.White,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "2. Warped Text (웨이브 효과)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "각 문자가 사인파처럼 위아래로 배치되어 물결 효과를 만듭니다",
                fontSize = 12.sp,
                color = Color(0xFF666666),
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            WarpedText(
                text = "Jetpack Compose로 만드는 웨이브 텍스트 효과입니다!",
                textStyle = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2196F3)
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun AnimatedWarpedTextCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color.White,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "3. Animated Warped Text (애니메이션 웨이브)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "웨이브 효과에 애니메이션을 추가하여 위아래로 부드럽게 움직입니다",
                fontSize = 12.sp,
                color = Color(0xFF666666),
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedWarpedText(
                text = "물결처럼 흔들리는 텍스트 애니메이션",
                textStyle = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun TypewriterTextCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color.White,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "4. Typewriter Text (타이핑 효과)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "타자기처럼 문자가 하나씩 나타나는 효과입니다",
                fontSize = 12.sp,
                color = Color(0xFF666666),
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            TypewriterText(
                text = "안녕하세요! Jetpack Compose로 구현한 타이핑 효과입니다. " +
                        "각 글자가 순차적으로 나타나는 것을 확인할 수 있습니다.",
                textStyle = TextStyle(
                    fontSize = 15.sp,
                    color = Color(0xFF9C27B0),
                    lineHeight = 22.sp
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * FadedText - 페이드 효과가 적용된 텍스트
 */
@Composable
fun FadedText(
    text: String,
    textStyle: TextStyle,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier) {
        val density = LocalDensity.current
        val constraints = constraints

        val textMeasurer = rememberTextMeasurer()
        val textLayout = remember(text, textStyle, constraints) {
            textMeasurer.measure(
                text = text,
                style = textStyle,
                constraints = constraints
            )
        }

        val canvasSize = with(density) {
            DpSize(textLayout.size.width.toDp(), textLayout.size.height.toDp())
        }

        Canvas(modifier = Modifier.size(canvasSize)) {
            for (lineIndex in 0 until textLayout.lineCount) {
                val startCharIndex = textLayout.getLineStart(lineIndex)
                val endCharIndex = textLayout.getLineEnd(lineIndex)

                val lineLeftCoordinate = textLayout.getLineLeft(lineIndex)
                val lineTopCoordinate = textLayout.getLineTop(lineIndex)

                // 각 라인의 알파값 계산 (0부터 시작해서 점진적으로 증가)
                val alpha = textStyle.color.alpha * lineIndex.toFloat() / textLayout.lineCount

                val lineText = text.substring(startCharIndex, endCharIndex)
                drawText(
                    textMeasurer = textMeasurer,
                    text = lineText,
                    topLeft = Offset(x = lineLeftCoordinate, y = lineTopCoordinate),
                    style = textStyle.copy(color = textStyle.color.copy(alpha = alpha))
                )
            }
        }
    }
}

/**
 * WarpedText - 웨이브 효과가 적용된 텍스트
 */
@Composable
fun WarpedText(
    text: String,
    textStyle: TextStyle,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier) {
        val density = LocalDensity.current
        val constraints = constraints

        val textMeasurer = rememberTextMeasurer()
        val textLayout = remember(text, textStyle, constraints) {
            textMeasurer.measure(
                text = text,
                style = textStyle,
                constraints = constraints
            )
        }

        val canvasSize = with(density) {
            DpSize(textLayout.size.width.toDp(), (textLayout.size.height + 20).toDp())
        }

        Canvas(modifier = Modifier.size(canvasSize)) {
            for (lineIndex in 0 until textLayout.lineCount) {
                val startCharIndex = textLayout.getLineStart(lineIndex)
                val endCharIndex = textLayout.getLineEnd(lineIndex)

                for (charIndex in startCharIndex until endCharIndex) {
                    val rect = textLayout.getBoundingBox(charIndex)
                    val char = textLayout.layoutInput.text[charIndex].toString()

                    withTransform({
                        translate(
                            left = 0f,
                            top = 5 * sin(charIndex * 0.7).toFloat()
                        )
                    }) {
                        drawText(
                            textMeasurer = textMeasurer,
                            text = char,
                            topLeft = Offset(x = rect.left, y = rect.top),
                            style = textStyle
                        )
                    }
                }
            }
        }
    }
}

/**
 * AnimatedWarpedText - 애니메이션 웨이브 효과가 적용된 텍스트
 */
@Composable
fun AnimatedWarpedText(
    text: String,
    textStyle: TextStyle,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier) {
        val density = LocalDensity.current
        val constraints = constraints

        val infiniteTransition = rememberInfiniteTransition(label = "wave")
        val sinusoidalAmplitude by infiniteTransition.animateFloat(
            initialValue = with(density) { -5.dp.toPx() },
            targetValue = with(density) { 5.dp.toPx() },
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "amplitude"
        )

        val textMeasurer = rememberTextMeasurer()
        val textLayout = remember(text, textStyle, constraints) {
            textMeasurer.measure(
                text = text,
                style = textStyle,
                constraints = constraints
            )
        }

        val canvasSize = with(density) {
            DpSize(textLayout.size.width.toDp(), (textLayout.size.height + 20).toDp())
        }

        Canvas(modifier = Modifier.size(canvasSize)) {
            for (lineIndex in 0 until textLayout.lineCount) {
                val startCharIndex = textLayout.getLineStart(lineIndex)
                val endCharIndex = textLayout.getLineEnd(lineIndex)

                for (charIndex in startCharIndex until endCharIndex) {
                    val rect = textLayout.getBoundingBox(charIndex)
                    val char = textLayout.layoutInput.text[charIndex].toString()

                    withTransform({
                        translate(
                            left = 0f,
                            top = sinusoidalAmplitude * sin(charIndex * 0.7).toFloat()
                        )
                    }) {
                        drawText(
                            textMeasurer = textMeasurer,
                            text = char,
                            topLeft = Offset(x = rect.left, y = rect.top),
                            style = textStyle
                        )
                    }
                }
            }
        }
    }
}

/**
 * TypewriterText - 타이핑 효과가 적용된 텍스트
 */
@Composable
fun TypewriterText(
    text: String,
    textStyle: TextStyle,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier) {
        val density = LocalDensity.current
        val constraints = constraints

        val animatedCharacterCount = remember { Animatable(0f) }
        LaunchedEffect(text) {
            animatedCharacterCount.animateTo(
                targetValue = text.length.toFloat(),
                animationSpec = tween(
                    durationMillis = text.length * 50,
                    easing = LinearEasing
                )
            )
        }

        val textMeasurer = rememberTextMeasurer()
        val textLayout = remember(text, textStyle, constraints) {
            textMeasurer.measure(
                text = text,
                style = textStyle,
                constraints = constraints
            )
        }

        val canvasSize = with(density) {
            DpSize(textLayout.size.width.toDp(), textLayout.size.height.toDp())
        }

        Canvas(modifier = Modifier.size(canvasSize)) {
            val lines = textLayout.lineCount
            val visibleChars = animatedCharacterCount.value.toInt()

            for (lineIndex in 0 until lines) {
                val startCharIndex = textLayout.getLineStart(lineIndex)
                val endCharIndex = textLayout.getLineEnd(lineIndex)

                if (visibleChars > startCharIndex) {
                    val topCoordinate = textLayout.getLineTop(lineIndex)
                    val leftCoordinate = textLayout.getLineLeft(lineIndex)

                    // 현재 라인에서 표시할 마지막 문자 인덱스
                    val displayedEndIndex = min(endCharIndex, visibleChars)
                    val displayedText = textLayout.layoutInput.text.substring(
                        startCharIndex,
                        displayedEndIndex
                    )

                    drawText(
                        textMeasurer = textMeasurer,
                        text = displayedText,
                        topLeft = Offset(x = leftCoordinate, y = topCoordinate),
                        style = textStyle
                    )
                }
            }
        }
    }
}
