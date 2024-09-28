package com.example.composesample.presentation.example.component.shimmer

import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.ui.unit.dp

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