package com.example.composesample.presentation.example.component.ui.layout.animation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateOffset
import androidx.compose.animation.core.animateSize
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.R
import com.example.composesample.presentation.MainHeader
import kotlin.math.roundToInt

@Composable
fun AnimationExampleUI(
    onBackEvent: () -> Unit,
) {
    var isExpanded1 by remember { mutableStateOf(false) }
    var isExpanded2 by remember { mutableStateOf(false) }
    var isExpanded3 by remember { mutableStateOf(false) }
    var isExpanded4 by remember { mutableStateOf(false) }
    var isExpanded5 by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Animation Example",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                FadeTransitionExpandableCard(
                    isExpanded = isExpanded1,
                    onExpandedChange = { isExpanded1 = it }
                )
            }

            item {
                MultiPropertyTransitionCard(
                    isExpanded = isExpanded2,
                    onExpandedChange = { isExpanded2 = it }
                )
            }

            item {
                SharedElementTransitionCard(
                    isExpanded = isExpanded3,
                    onExpandedChange = { isExpanded3 = it }
                )
            }

            item {
                OffsetAndSizeTransitionCard(
                    isExpanded = isExpanded4,
                    onExpandedChange = { isExpanded4 = it }
                )
            }

            item {
                ScaleAndDimensionTransitionCard(
                    isExpanded = isExpanded5,
                    onExpandedChange = { isExpanded5 = it }
                )
            }


            item {
                Spacer(modifier = Modifier.height(400.dp))
            }
        }
    }
}

@Composable
fun FadeTransitionExpandableCard(
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
) {
    AnimatedContent(
        targetState = isExpanded,
        transitionSpec = {
            ContentTransform(
                targetContentEnter = fadeIn(animationSpec = tween(700)),
                initialContentExit = fadeOut(animationSpec = tween(700))
            ) using SizeTransform(clip = false)
        }
    ) { expanded ->
        if (!expanded) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .clickable { onExpandedChange(!isExpanded) }
            ) {
                Image(
                    modifier = Modifier.size(130.dp),
                    painter = painterResource(id = R.drawable.profile_picture),
                    contentDescription = null
                )
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    text = "First Item Text",
                    fontSize = 12.sp,
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExpandedChange(!isExpanded) }
            ) {
                Image(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp),
                    painter = painterResource(id = R.drawable.profile_picture),
                    contentDescription = null
                )
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    text = "First Item Text - expanded",
                    fontSize = 12.sp,
                )
            }
        }
    }
}

@Composable
fun MultiPropertyTransitionCard(
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
) {
    val transition = updateTransition(targetState = isExpanded, label = "expandTransition")

    val imageSize by transition.animateDp(
        label = "imageSize",
        transitionSpec = { tween(1000) }
    ) { expanded ->
        if (expanded) 320.dp else 130.dp
    }

    val imageHeight by transition.animateDp(
        label = "imageHeight",
        transitionSpec = { tween(1000) }
    ) { expanded ->
        if (expanded) 320.dp else 130.dp
    }

    val padding by transition.animateDp(
        label = "padding",
        transitionSpec = { tween(1000) }
    ) { expanded ->
        if (expanded) 20.dp else 20.dp
    }

    val arrangement by transition.animateFloat(
        label = "arrangement",
        transitionSpec = { tween(1000) }
    ) { expanded ->
        if (expanded) 1f else 0f
    }

    if (arrangement < 0.5f) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .clickable { onExpandedChange(!isExpanded) }
        ) {
            Image(
                modifier = Modifier.size(imageSize),
                painter = painterResource(id = R.drawable.profile_picture),
                contentDescription = null
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(padding),
                text = "Second Item Text",
                fontSize = 12.sp,
            )
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onExpandedChange(!isExpanded) }
        ) {
            Image(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(imageHeight),
                painter = painterResource(id = R.drawable.profile_picture),
                contentDescription = null
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(padding),
                text = "Second Item Text - expanded",
                fontSize = 12.sp,
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedElementTransitionCard(
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
) {
    SharedTransitionLayout {
        val boundsTransform = { initial: Rect, target: Rect -> 
            spring<Rect>(
                dampingRatio = 0.8f,
                stiffness = 400f
            )
        }

        AnimatedContent(targetState = isExpanded) { target ->
            if (!target) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .clickable { onExpandedChange(!isExpanded) }
                ) {
                    Image(
                        modifier = Modifier
                            .sharedElement(
                                sharedContentState = rememberSharedContentState(key = "image"),
                                animatedVisibilityScope = this@AnimatedContent,
                                boundsTransform = boundsTransform,
                            )
                            .size(130.dp),
                        painter = painterResource(id = R.drawable.profile_picture),
                        contentDescription = null
                    )

                    Text(
                        modifier = Modifier
                            .sharedElement(
                                sharedContentState = rememberSharedContentState(key = "name"),
                                animatedVisibilityScope = this@AnimatedContent,
                                boundsTransform = boundsTransform,
                            )
                            .fillMaxWidth()
                            .padding(20.dp),
                        text = "Third Item Text",
                        fontSize = 12.sp,
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onExpandedChange(!isExpanded) }
                ) {
                    Image(
                        modifier = Modifier
                            .sharedElement(
                                sharedContentState = rememberSharedContentState(key = "image"),
                                animatedVisibilityScope = this@AnimatedContent,
                                boundsTransform = boundsTransform,
                            )
                            .fillMaxWidth()
                            .height(320.dp),
                        painter = painterResource(id = R.drawable.profile_picture),
                        contentDescription = null,
                        contentScale = ContentScale.Crop
                    )

                    Text(
                        modifier = Modifier
                            .sharedElement(
                                sharedContentState = rememberSharedContentState(key = "name"),
                                animatedVisibilityScope = this@AnimatedContent,
                                boundsTransform = boundsTransform,
                            )
                            .fillMaxWidth()
                            .padding(20.dp),
                        text = "Third Item Text - expanded",
                        fontSize = 12.sp,
                    )
                }
            }
        }
    }
}


@Composable
fun OffsetAndSizeTransitionCard(
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        val transition = updateTransition(targetState = isExpanded, label = "expandTransition")

        val offset by transition.animateOffset(
            label = "offset",
            transitionSpec = { tween(700) }
        ) { expanded ->
            if (expanded) Offset(0f, 0f) else Offset(12f, 12f)
        }

        val size by transition.animateSize(
            label = "size",
            transitionSpec = { tween(700) }
        ) { expanded ->
            if (expanded) Size(320f, 320f) else Size(130f, 130f)
        }

        if (!isExpanded) {
            Row(
                modifier = Modifier
                    .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
                    .clickable { onExpandedChange(!isExpanded) }
            ) {
                Image(
                    modifier = Modifier.size(size.width.dp, size.height.dp),
                    painter = painterResource(id = R.drawable.profile_picture),
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )
                Text(
                    modifier = Modifier.padding(20.dp),
                    text = "Fourth Item Text",
                    fontSize = 12.sp
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExpandedChange(!isExpanded) }
            ) {
                Image(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(size.height.dp),
                    painter = painterResource(id = R.drawable.profile_picture),
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )
                Text(
                    modifier = Modifier.padding(20.dp),
                    text = "Fourth Item Text - expanded",
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun ScaleAndDimensionTransitionCard(
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
) {
    val transition = updateTransition(targetState = isExpanded, label = "rectTransition")

    val width by transition.animateDp(
        label = "width",
        transitionSpec = { tween(1000) }
    ) { expanded ->
        if (expanded) 320.dp else 130.dp
    }

    val height by transition.animateDp(
        label = "height",
        transitionSpec = { tween(700) }
    ) { expanded ->
        if (expanded) 320.dp else 130.dp
    }

    val scale by transition.animateFloat(
        label = "scale",
        transitionSpec = { tween(700) }
    ) { expanded ->
        if (expanded) 1.2f else 1f
    }

    if (!isExpanded) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .clickable { onExpandedChange(!isExpanded) }
        ) {
            Box(
                modifier = Modifier
                    .size(width, height)
            ) {
                Image(
                    modifier = Modifier
                        .matchParentSize()
                        .scale(scale),
                    painter = painterResource(id = R.drawable.profile_picture),
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )
            }
            Text(
                modifier = Modifier
                    .padding(start = 20.dp)
                    .align(Alignment.CenterVertically),
                text = "Fifth Item Text",
                fontSize = 12.sp
            )
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onExpandedChange(!isExpanded) }
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(height)
                ) {
                    Image(
                        modifier = Modifier
                            .matchParentSize()
                            .scale(scale),
                        painter = painterResource(id = R.drawable.profile_picture),
                        contentDescription = null,
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    modifier = Modifier.padding(vertical = 20.dp),
                    text = "Fifth Item Text - expanded",
                    fontSize = 12.sp
                )
            }
        }
    }
}