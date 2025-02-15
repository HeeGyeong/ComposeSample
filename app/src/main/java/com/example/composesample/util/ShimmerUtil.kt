package com.example.composesample.util

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/**
 * 배경에 Shimmer 효과를 추가하는 기본적인 Modifier입니다.
 *
 * 애니메이션 변수를 직접 설정할 필요 없이, 색상 리스트와 너비 비율(ratio)만 지정하면 됩니다.
 *
 * @param colorList Shimmer 효과에 사용할 색상 리스트입니다.
 * @param ratio 화면 너비에 대한 Shimmer 효과의 비율입니다.
 */
fun Modifier.defaultShimmer(
    colorList: List<Color> = listOf(
        Color.LightGray.copy(alpha = 0.2f),
        Color.LightGray.copy(alpha = 0.9f),
        Color.LightGray.copy(alpha = 0.2f)
    ),
    ratio: Float = 0.6f
): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ), label = "Shimmer"
    )

    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp
    val shimmerWidth = screenWidthDp * ratio

    background(
        brush = Brush.linearGradient(
            colors = colorList,
            start = Offset(
                x = translateAnim - shimmerWidth.value,
                y = translateAnim - shimmerWidth.value
            ),
            end = Offset(x = translateAnim, y = translateAnim)
        )
    )
}

fun Modifier.shimmer(
    colorList: List<Color> = listOf(
        Color.LightGray.copy(alpha = 0.2f),
        Color.LightGray.copy(alpha = 0.9f),
        Color.LightGray.copy(alpha = 0.2f)
    ),
    shimmerAnimation: Float,
    ratio: Float = 0.6f
): Modifier = composed {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp
    val shimmerWidth = screenWidthDp * ratio

    background(
        brush = Brush.linearGradient(
            colors = colorList,
            start = Offset(
                x = shimmerAnimation - shimmerWidth.value,
                y = shimmerAnimation - shimmerWidth.value
            ),
            end = Offset(x = shimmerAnimation, y = shimmerAnimation)
        )
    )
}

@Composable
fun shimmerBrush(): Brush {
    val shimmerColors = listOf(
        Color.Black,
        Color.Black.copy(alpha = 0.3f),
        Color.Black,
    )

    val transition = rememberInfiniteTransition(label = "ShimmerTransition")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ), label = "Shimmer"
    )

    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp
    val shimmerWidth = screenWidthDp * 0.6f

    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(
            x = translateAnim - shimmerWidth.value,
            y = translateAnim - shimmerWidth.value
        ),
        end = Offset(x = translateAnim, y = translateAnim)
    )
}

fun Modifier.defaultShimmerBrush(
    colorList: List<Color> = listOf(
        Color.Black,
        Color.Black.copy(alpha = 0.3f),
        Color.Black,
    ),
    ratio: Float = 0.6f
): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ), label = "Shimmer"
    )

    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp
    val shimmerWidth = screenWidthDp * ratio

    background(
        brush = Brush.linearGradient(
            colors = colorList,
            start = Offset(
                x = translateAnim - shimmerWidth.value,
                y = translateAnim - shimmerWidth.value
            ),
            end = Offset(x = translateAnim, y = translateAnim)
        )
    )
}

/**
 * 텍스트에 Shimmer 효과를 적용하는 유틸 함수입니다.
 *
 * 사용 예시: Text(style = textShimmer(), ...)
 *
 * 이 함수는 텍스트의 스타일 속성인 fontWeight, fontSize, letterSpacing, lineHeight를
 * 설정할 수 있습니다. 만약 별도의 스타일 유틸 함수가 있다면, 해당 값을 대체하여 사용할 수 있습니다.
 *
 * @param colors Shimmer 효과에 사용할 색상 리스트입니다.
 * @param durationMillis 애니메이션의 지속 시간(밀리초)입니다.
 * @param gradientWidth 그라데이션의 너비입니다.
 * @param fontWeight 텍스트의 굵기를 설정합니다.
 * @param fontSize 텍스트의 크기를 설정합니다.
 * @param letterSpacing 텍스트의 자간을 설정합니다.
 * @param lineHeight 텍스트의 줄 높이를 설정합니다.
 */
@Composable
fun textShimmer(
    colors: List<Color> = listOf(
        Color.LightGray,
        Color.DarkGray,
        Color.LightGray,
    ),
    durationMillis: Int = 1000,
    gradientWidth: Float = 4000f,
    fontWeight: FontWeight = FontWeight.W400,
    fontSize: TextUnit = 13.sp,
    letterSpacing: TextUnit = 0.01.em,
    lineHeight: TextUnit = 1.40.em
): TextStyle {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnimation = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = durationMillis,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "textShimmer"
    )

    return TextStyle(
        brush = Brush.linearGradient(
            colors = colors,
            start = Offset(0f, 0f),
            end = Offset(translateAnimation.value * gradientWidth, 0f),
        ),
        fontWeight = fontWeight,
        fontSize = fontSize,
        letterSpacing = letterSpacing,
        lineHeight = lineHeight
    )
}