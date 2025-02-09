package com.example.composesample.presentation.example.component.shimmer

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.material3.Text
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