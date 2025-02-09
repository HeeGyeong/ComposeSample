package com.example.composesample.presentation.example.component.shimmer

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader

@Composable
fun TextShimmerExampleUI(
    onBackButtonClick: () -> Unit
) {
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Black)
    ) {
        MainHeader(
            title = "Text Shimmer Example",
            onBackIconClicked = onBackButtonClick
        )

        Box(
            modifier = Modifier
                .shimmer(
                    shimmerAnimation = translateAnim
                )
        ) {
            Text(
                text = "Basic Shimmer Effect"
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Shimmer Effect Using Modifier Only",
            modifier = Modifier.shimmer(
                shimmerAnimation = translateAnim
            )
        )

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Shimmer with Modifier + Gradient Style",
            modifier = Modifier.shimmer(
                shimmerAnimation = translateAnim
            ),
            style = TextStyle(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Gray,
                        Color.White,
                        Color.Gray
                    )
                )
            )
        )

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Dynamic Linear Gradient",
            style = TextStyle(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Gray,
                        Color.White,
                        Color.Gray,
                    ),
                    start = Offset(translateAnim, 0f),
                    end = Offset(translateAnim + 100f, 100f),
                )
            ),
        )

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Custom Shimmer with Style",
            style = textShimmer()
        )
    }
}