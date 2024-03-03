package com.example.composesample.example.ui.lazycolumn

import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LazyColumnFlingBehaviorExample(onBackButtonClick: () -> Unit) {
    val lazyListState = rememberLazyListState()
    val repeatCount = 1000

    LazyColumn(
        state = lazyListState,
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White),
        flingBehavior = maxScrollSpeedFlingBehavior()
//        flingBehavior = rememberSnapFlingBehavior(lazyListState = lazyListState)
    ) {
        stickyHeader {
            Box(
                modifier = Modifier
                    .background(color = Color.Gray)
                    .padding(top = 10.dp, bottom = 10.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    IconButton(
                        onClick = {
                            onBackButtonClick.invoke()
                        }
                    ) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "")
                    }
                }
            }
        }

        repeat(repeatCount) {
            item {
                RepeatDummyItems(it)
            }
        }
    }
}

@Composable
fun RepeatDummyItems(index: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$index Item",
            textAlign = TextAlign.Center,
            fontSize = 20.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Divider(
            thickness = 3.dp,
            color = Color.DarkGray
        )

        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
fun maxScrollSpeedFlingBehavior(): FlingBehavior {
    val splineBasedDecay = rememberSplineBasedDecay<Float>()
    return remember(splineBasedDecay) {
        MaxScrollSpeedFlingBehavior(splineBasedDecay)
    }
}

private class MaxScrollSpeedFlingBehavior(
    private val splineBasedDecay: DecayAnimationSpec<Float>,
) : FlingBehavior {
    override suspend fun ScrollScope.performFling(initialVelocity: Float): Float {
        val setVelocity = if (initialVelocity > 0F) initialVelocity.coerceAtMost(2_000F)
        else initialVelocity.coerceAtLeast(-2_000F)

        return if (abs(setVelocity) > 0f) {
            var velocityLeft = setVelocity
            var lastValue = 0f
            AnimationState(
                initialValue = 0f,
                initialVelocity = setVelocity,
            ).animateDecay(splineBasedDecay) {
                val delta = value - lastValue
                val consumed = scrollBy(delta)
                lastValue = value
                velocityLeft = this.velocity
                if (abs(delta - consumed) > 0.5f) this.cancelAnimation()
            }
            velocityLeft
        } else setVelocity
    }
}