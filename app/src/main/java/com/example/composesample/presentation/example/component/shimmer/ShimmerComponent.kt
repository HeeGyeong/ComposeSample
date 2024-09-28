package com.example.composesample.presentation.example.component.shimmer

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


@Composable
fun ShimmerTestItem(
    colorList: List<Color> = listOf(
        Color.LightGray.copy(alpha = 0.2f),
        Color.LightGray.copy(alpha = 0.9f),
        Color.LightGray.copy(alpha = 0.2f)
    ),
    ratio: Float
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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(8.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clip(RoundedCornerShape(10.dp))
                .shimmer(
                    colorList = colorList,
                    shimmerAnimation = translateAnim,
                    ratio = ratio
                )
        )
    }
}

/**
 * composed Shimmer
 */
@Composable
fun ShimmerItem() {
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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Spacer(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .shimmer(shimmerAnimation = translateAnim)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .shimmer(shimmerAnimation = translateAnim)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(15.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .shimmer(shimmerAnimation = translateAnim)
                )
            }
        }
    }
}

/**
 * use Material Card
 *
 * Shimmer
 */
@Composable
fun BackgroundShimmerItem() {
    androidx.compose.material.Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(8.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Spacer(modifier = Modifier.background(shimmerBrush()))
    }

    // shimmer가 보이지 않는다.
    androidx.compose.material3.Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(8.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Spacer(modifier = Modifier.background(shimmerBrush()))
    }

    // material card에 shimmer 적용한 샘플
    androidx.compose.material.Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(8.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Spacer(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .defaultShimmerBrush()
        )
    }

    androidx.compose.material3.Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(8.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Spacer(
            modifier = Modifier
//                .fillMaxWidth()
//                .height(100.dp)
                .clip(RoundedCornerShape(10.dp))
                .defaultShimmerBrush()
        )
    }
}