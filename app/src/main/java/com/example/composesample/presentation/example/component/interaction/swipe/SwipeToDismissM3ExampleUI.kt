package com.example.composesample.presentation.example.component.interaction.swipe

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Swipe to Dismiss with Material 3 Example UI
 *
 * Material 3의 SwipeToDismissBox를 사용한 다양한 스와이프 제스처 예제를 시연합니다.
 */
@Composable
fun SwipeToDismissM3ExampleUI(
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
            item { BasicSwipeToDismissCard() }
            item { TwoWaySwipeCard() }
            item { ConditionalSwipeCard() }
            item { M2StyleAnimationCard() }
        }
    }
}

@Composable
private fun HeaderCard(onBackEvent: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
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
            Spacer(modifier = Modifier.size(8.dp))
            Column {
                Text(
                    text = "Swipe to Dismiss (M3)",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )
                Text(
                    text = "Material 3 SwipeToDismissBox",
                    fontSize = 14.sp,
                    color = Color(0xFF757575)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BasicSwipeToDismissCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "1. Basic Swipe to Dismiss",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
            Text(
                text = "왼쪽으로 스와이프하여 아이템을 삭제하세요.",
                fontSize = 14.sp,
                color = Color(0xFF757575)
            )
            Spacer(modifier = Modifier.height(16.dp))

            val items = remember {
                mutableStateListOf(
                    EmailItem(1, "Alice", "Meeting tomorrow", "Let's discuss the project..."),
                    EmailItem(2, "Bob", "Lunch plans", "Are you free for lunch?"),
                    EmailItem(3, "Charlie", "Weekend trip", "Let's plan our trip..."),
                    EmailItem(4, "Diana", "Important update", "Please review this document..."),
                    EmailItem(5, "Eve", "Happy Birthday!", "Wishing you a wonderful day!")
                )
            }

            items.forEach { item ->
                var show by remember { mutableStateOf(true) }
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { dismissValue ->
                        when (dismissValue) {
                            SwipeToDismissBoxValue.EndToStart -> {
                                show = false
                                true
                            }

                            else -> false
                        }
                    }
                )

                AnimatedVisibility(
                    visible = show,
                    exit = fadeOut() + slideOutVertically()
                ) {
                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = false,
                        enableDismissFromEndToStart = true,
                        backgroundContent = {
                            DismissBackground(
                                dismissState.currentValue,
                                dismissState.targetValue,
                                icon = Icons.Default.Delete,
                                color = Color(0xFFD32F2F),
                                alignment = Alignment.CenterEnd
                            )
                        },
                        content = {
                            EmailItemCard(item)
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TwoWaySwipeCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "2. Two-Way Swipe",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
            Text(
                text = "오른쪽: 즐겨찾기, 왼쪽: 전송",
                fontSize = 14.sp,
                color = Color(0xFF757575)
            )
            Spacer(modifier = Modifier.height(16.dp))

            val items = remember {
                mutableStateListOf(
                    EmailItem(
                        6,
                        "Frank",
                        "Project update",
                        "Here's the latest progress...",
                        isFavorite = false
                    ),
                    EmailItem(
                        7,
                        "Grace",
                        "Team meeting",
                        "Don't forget tomorrow's meeting...",
                        isFavorite = true
                    ),
                    EmailItem(8, "Henry", "Code review", "Please review my PR..."),
                )
            }

            items.forEach { item ->
                var show by remember { mutableStateOf(true) }
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { dismissValue ->
                        when (dismissValue) {
                            SwipeToDismissBoxValue.StartToEnd -> {
                                item.isFavorite = !item.isFavorite
                                false
                            }

                            SwipeToDismissBoxValue.EndToStart -> {
                                show = false
                                true
                            }

                            else -> false
                        }
                    }
                )

                LaunchedEffect(dismissState.currentValue) {
                    if (dismissState.currentValue == SwipeToDismissBoxValue.StartToEnd) {
                        delay(500)
                        dismissState.reset()
                    }
                }

                AnimatedVisibility(
                    visible = show,
                    exit = fadeOut() + slideOutVertically()
                ) {
                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = true,
                        enableDismissFromEndToStart = true,
                        backgroundContent = {
                            TwoWayDismissBackground(
                                currentValue = dismissState.currentValue,
                                targetValue = dismissState.targetValue
                            )
                        },
                        content = {
                            EmailItemCard(item)
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConditionalSwipeCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "3. Conditional Swipe",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
            Text(
                text = "잠긴 아이템은 스와이프할 수 없습니다.",
                fontSize = 14.sp,
                color = Color(0xFF757575)
            )
            Spacer(modifier = Modifier.height(16.dp))

            val items = remember {
                mutableStateListOf(
                    EmailItem(9, "Ivy", "Normal message", "This can be deleted", isLocked = false),
                    EmailItem(10, "Jack", "Locked message", "This is important!", isLocked = true),
                    EmailItem(11, "Kate", "Another normal", "Swipe me away", isLocked = false),
                )
            }

            items.forEach { item ->
                var show by remember { mutableStateOf(true) }
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { dismissValue ->
                        if (item.isLocked) {
                            false
                        } else {
                            when (dismissValue) {
                                SwipeToDismissBoxValue.EndToStart -> {
                                    show = false
                                    true
                                }

                                else -> false
                            }
                        }
                    }
                )

                AnimatedVisibility(
                    visible = show,
                    exit = fadeOut() + slideOutVertically()
                ) {
                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = false,
                        enableDismissFromEndToStart = !item.isLocked,
                        backgroundContent = {
                            if (item.isLocked) {
                                DismissBackground(
                                    dismissState.currentValue,
                                    dismissState.targetValue,
                                    icon = Icons.Default.Lock,
                                    color = Color(0xFF757575),
                                    alignment = Alignment.CenterEnd
                                )
                            } else {
                                DismissBackground(
                                    dismissState.currentValue,
                                    dismissState.targetValue,
                                    icon = Icons.Default.Delete,
                                    color = Color(0xFFD32F2F),
                                    alignment = Alignment.CenterEnd
                                )
                            }
                        },
                        content = {
                            EmailItemCard(item)
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun M2StyleAnimationCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "4. M2 Style Animation with M3",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
            Text(
                text = "M2의 복잡한 애니메이션을 M3 API로 구현",
                fontSize = 14.sp,
                color = Color(0xFF757575)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFF3E0), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "💡 M2 vs M3 차이점",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE65100)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "• M2: SwipeToDismiss + DismissState + FractionalThreshold",
                    fontSize = 12.sp,
                    color = Color(0xFF5D4037)
                )
                Text(
                    text = "• M3: SwipeToDismissBox + rememberSwipeToDismissBoxState",
                    fontSize = 12.sp,
                    color = Color(0xFF5D4037)
                )
                Text(
                    text = "• M2: Animatable로 multi-stage 애니메이션",
                    fontSize = 12.sp,
                    color = Color(0xFF5D4037)
                )
                Text(
                    text = "• M3: 간단한 animateFloatAsState 권장",
                    fontSize = 12.sp,
                    color = Color(0xFF5D4037)
                )
                Text(
                    text = "• 아래 예제는 M2의 복잡한 방식을 M3로 재현",
                    fontSize = 12.sp,
                    color = Color(0xFF5D4037),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val items = remember {
                mutableStateListOf(
                    EmailItem(12, "Leo", "M2 Style Animation", "원형 확장 + Bounce 효과"),
                    EmailItem(13, "Mia", "Complex Animation", "Animatable 사용"),
                    EmailItem(14, "Noah", "Color Inversion", "색상 반전 애니메이션"),
                )
            }

            items.forEach { item ->
                var show by remember { mutableStateOf(true) }
                val coroutineScope = rememberCoroutineScope()

                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { dismissValue ->
                        when (dismissValue) {
                            SwipeToDismissBoxValue.StartToEnd -> {
                                item.isFavorite = !item.isFavorite
                                false
                            }

                            SwipeToDismissBoxValue.EndToStart -> {
                                coroutineScope.launch {
                                    delay(500)
                                    show = false
                                }
                                true
                            }

                            else -> false
                        }
                    }
                )

                var thresholdReached by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    snapshotFlow { dismissState.currentValue }
                        .collect { value ->
                            thresholdReached = value != SwipeToDismissBoxValue.Settled
                        }
                }

                AnimatedVisibility(
                    visible = show,
                    exit = fadeOut() + slideOutVertically()
                ) {
                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = true,
                        enableDismissFromEndToStart = true,
                        backgroundContent = {
                            M2StyleBackground(
                                currentValue = dismissState.currentValue,
                                thresholdReached = thresholdReached
                            )
                        },
                        content = {
                            EmailItemCard(item)
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun M2StyleBackground(
    currentValue: SwipeToDismissBoxValue,
    thresholdReached: Boolean
) {
    val backgroundScale = remember { Animatable(0f) }
    val iconScale = remember { Animatable(0.8f) }

    LaunchedEffect(thresholdReached) {
        if (thresholdReached) {
            backgroundScale.snapTo(0f)
            launch {
                backgroundScale.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 400)
                )
            }
            iconScale.snapTo(0.8f)
            iconScale.animateTo(
                targetValue = 1.25f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            )
            iconScale.animateTo(targetValue = 1f)
        } else {
            backgroundScale.snapTo(0f)
            iconScale.snapTo(0.8f)
        }
    }

    val backgroundColor = when (currentValue) {
        SwipeToDismissBoxValue.StartToEnd -> if (thresholdReached) Color(0xFFFFFF00) else Color.Black
        SwipeToDismissBoxValue.EndToStart -> if (thresholdReached) Color(0xFF8B0000) else Color.Black
        else -> Color.Transparent
    }

    val iconColor = when (currentValue) {
        SwipeToDismissBoxValue.StartToEnd -> if (thresholdReached) Color.Black else Color(0xFFFFFF00)
        SwipeToDismissBoxValue.EndToStart -> if (thresholdReached) Color.Black else Color(0xFF8B0000)
        else -> Color.White
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(
                CircleAnimationPath(
                    visible = backgroundScale.value,
                    startPosition = currentValue == SwipeToDismissBoxValue.StartToEnd
                )
            )
            .background(backgroundColor)
            .padding(horizontal = 20.dp)
    ) {
        val alignment = when (currentValue) {
            SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
            SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
            else -> Alignment.Center
        }

        Box(
            modifier = Modifier
                .align(alignment)
                .scale(iconScale.value),
            contentAlignment = Alignment.Center
        ) {
            when (currentValue) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Favorite",
                        modifier = Modifier.size(32.dp),
                        tint = iconColor
                    )
                }

                SwipeToDismissBoxValue.EndToStart -> {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        modifier = Modifier.size(32.dp),
                        tint = iconColor
                    )
                }

                else -> {}
            }
        }
    }
}

@Composable
private fun EmailItemCard(item: EmailItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = null,
                tint = Color(0xFF1976D2),
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.sender,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    if (item.isLocked) {
                        Spacer(modifier = Modifier.size(8.dp))
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = Color(0xFF757575),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    if (item.isFavorite) {
                        Spacer(modifier = Modifier.size(8.dp))
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Favorite",
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text(
                    text = item.subject,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF424242),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.preview,
                    fontSize = 12.sp,
                    color = Color(0xFF757575),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DismissBackground(
    currentValue: SwipeToDismissBoxValue,
    targetValue: SwipeToDismissBoxValue,
    icon: ImageVector,
    color: Color,
    alignment: Alignment
) {
    val scale by animateFloatAsState(
        targetValue = if (currentValue == SwipeToDismissBoxValue.Settled) 0.8f else 1.2f,
        animationSpec = tween(durationMillis = 300),
        label = "scale"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (currentValue != SwipeToDismissBoxValue.Settled) color else Color.Transparent,
        animationSpec = tween(durationMillis = 300),
        label = "backgroundColor"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(horizontal = 20.dp),
        contentAlignment = alignment
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier
                .size(32.dp)
                .scale(scale),
            tint = Color.White
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TwoWayDismissBackground(
    currentValue: SwipeToDismissBoxValue,
    targetValue: SwipeToDismissBoxValue
) {
    val color by animateColorAsState(
        targetValue = when (currentValue) {
            SwipeToDismissBoxValue.StartToEnd -> Color(0xFFFFC107)
            SwipeToDismissBoxValue.EndToStart -> Color(0xFF1976D2)
            else -> Color.Transparent
        },
        animationSpec = tween(durationMillis = 300),
        label = "backgroundColor"
    )

    val scale by animateFloatAsState(
        targetValue = if (currentValue == SwipeToDismissBoxValue.Settled) 0.8f else 1.2f,
        animationSpec = tween(durationMillis = 300),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color)
            .padding(horizontal = 20.dp)
    ) {
        if (currentValue == SwipeToDismissBoxValue.StartToEnd) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Favorite",
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(32.dp)
                    .scale(scale),
                tint = Color.White
            )
        }
        if (currentValue == SwipeToDismissBoxValue.EndToStart) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send",
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(32.dp)
                    .scale(scale),
                tint = Color.White
            )
        }
    }
}

data class EmailItem(
    val id: Int,
    val sender: String,
    val subject: String,
    val preview: String,
    var isFavorite: Boolean = false,
    val isLocked: Boolean = false
)

@Preview(showBackground = true)
@Composable
fun SwipeToDismissM3ExampleUIPreview() {
    SwipeToDismissM3ExampleUI(onBackEvent = {})
}
