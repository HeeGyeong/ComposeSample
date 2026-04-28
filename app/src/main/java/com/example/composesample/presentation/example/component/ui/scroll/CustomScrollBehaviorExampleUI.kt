package com.example.composesample.presentation.example.component.ui.scroll

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Custom TopAppBarScrollBehavior Example UI
 *
 * RecyclerView 스크롤 이벤트를 커스텀 TopAppBarScrollBehavior로 처리하는 방법을 보여줍니다.
 * 부분 렌더링 없이 완전 숨김/표시, 애니메이션, 스크롤 버퍼, 중복 방지 플래그를 구현합니다.
 */

/**
 * 기본 EnterAlways: 즉시 스냅 (애니메이션 없음)
 * 스크롤 방향에 따라 즉시 완전히 숨기거나 표시합니다.
 */
@OptIn(ExperimentalMaterial3Api::class)
class BasicEnterAlwaysScrollBehavior(
    override val state: TopAppBarState,
) : TopAppBarScrollBehavior {
    override val isPinned: Boolean = false
    override val snapAnimationSpec: AnimationSpec<Float>? = null
    override val flingAnimationSpec: DecayAnimationSpec<Float>? = null
    override val nestedScrollConnection: NestedScrollConnection =
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                if (available.y == 0f) return Offset.Zero

                val previousOffset = state.heightOffset
                val newOffset = if (available.y > 0) 0f else state.heightOffsetLimit
                state.heightOffset = newOffset

                return Offset(0f, newOffset - previousOffset)
            }
        }
}

/**
 * 애니메이션 + 스크롤 버퍼 EnterAlways
 * tween 애니메이션과 스크롤 누적 버퍼를 추가합니다.
 */
@OptIn(ExperimentalMaterial3Api::class)
class AnimatedEnterAlwaysScrollBehavior(
    override val state: TopAppBarState,
    private val coroutineScope: CoroutineScope,
    private val bufferThreshold: Float = 100f,
    private val animationDuration: Int = 150,
) : TopAppBarScrollBehavior {
    override val isPinned: Boolean = false
    override val snapAnimationSpec: AnimationSpec<Float>? = null
    override val flingAnimationSpec: DecayAnimationSpec<Float>? = null
    override val nestedScrollConnection: NestedScrollConnection =
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                if (available.y == 0f) return Offset.Zero
                if (available.y > 0 && state.collapsedFraction == 0f) return Offset.Zero
                if (available.y < 0 && state.collapsedFraction == 1f) return Offset.Zero

                scrollAccumulation += abs(available.y)
                if (scrollAccumulation < bufferThreshold) return Offset.Zero
                scrollAccumulation = 0f

                val previousOffset = state.heightOffset
                val newOffset = if (available.y > 0) 0f else state.heightOffsetLimit
                coroutineScope.launch {
                    animate(
                        initialValue = previousOffset,
                        targetValue = newOffset,
                        animationSpec = tween(durationMillis = animationDuration),
                    ) { value, _ ->
                        state.heightOffset = value
                    }
                }

                return Offset(0f, newOffset - previousOffset)
            }
        }
    private var scrollAccumulation: Float = 0f
}

/**
 * 최종 EnterAlways: 애니메이션 + 스크롤 버퍼 + 중복 방지 플래그
 * animationInProgress 플래그로 느린 스크롤 시 문제를 해결합니다.
 */
@OptIn(ExperimentalMaterial3Api::class)
class FullEnterAlwaysScrollBehavior(
    override val state: TopAppBarState,
    private val coroutineScope: CoroutineScope,
    private val bufferThreshold: Float = 100f,
    private val animationDuration: Int = 150,
) : TopAppBarScrollBehavior {
    override val isPinned: Boolean = false
    override val snapAnimationSpec: AnimationSpec<Float>? = null
    override val flingAnimationSpec: DecayAnimationSpec<Float>? = null
    override val nestedScrollConnection: NestedScrollConnection =
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                if (animationInProgress) return Offset.Zero
                if (available.y == 0f) return Offset.Zero
                if (available.y > 0 && state.collapsedFraction == 0f) return Offset.Zero
                if (available.y < 0 && state.collapsedFraction == 1f) return Offset.Zero

                scrollAccumulation += abs(available.y)
                if (scrollAccumulation < bufferThreshold) return Offset.Zero
                scrollAccumulation = 0f

                val previousOffset = state.heightOffset
                val newOffset = if (available.y > 0) 0f else state.heightOffsetLimit
                coroutineScope.launch {
                    animationInProgress = true
                    try {
                        animate(
                            initialValue = previousOffset,
                            targetValue = newOffset,
                            animationSpec = tween(durationMillis = animationDuration),
                        ) { value, _ ->
                            state.heightOffset = value
                        }
                    } finally {
                        animationInProgress = false
                    }
                }

                return Offset(0f, newOffset - previousOffset)
            }
        }
    private var scrollAccumulation: Float = 0f
    private var animationInProgress: Boolean = false
}

/**
 * ExitUntilCollapsed: 위로 스크롤 시 숨기고, 스크롤 끝 도달 시 표시
 */
@OptIn(ExperimentalMaterial3Api::class)
class FullExitUntilCollapsedScrollBehavior(
    override val state: TopAppBarState,
    private val coroutineScope: CoroutineScope,
    private val bufferThreshold: Float = 100f,
    private val animationDuration: Int = 150,
) : TopAppBarScrollBehavior {
    override val isPinned: Boolean = false
    override val snapAnimationSpec: AnimationSpec<Float>? = null
    override val flingAnimationSpec: DecayAnimationSpec<Float>? = null
    override val nestedScrollConnection: NestedScrollConnection =
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                if (animationInProgress) return Offset.Zero
                if (available.y >= 0f) return Offset.Zero
                if (available.y < 0f && state.collapsedFraction == 1f) return Offset.Zero

                scrollAccumulation += abs(available.y)
                if (scrollAccumulation < bufferThreshold) return Offset.Zero
                scrollAccumulation = 0f

                val previousOffset = state.heightOffset
                val newOffset = state.heightOffsetLimit
                coroutineScope.launch {
                    animationInProgress = true
                    try {
                        animate(
                            initialValue = previousOffset,
                            targetValue = newOffset,
                            animationSpec = tween(durationMillis = animationDuration),
                        ) { value, _ ->
                            state.heightOffset = value
                        }
                    } finally {
                        animationInProgress = false
                    }
                }

                return Offset(0f, newOffset - previousOffset)
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                if (available != Offset.Zero) return consumed

                val previousOffset = state.heightOffset
                val newOffset = 0f
                coroutineScope.launch {
                    animationInProgress = true
                    try {
                        animate(
                            initialValue = previousOffset,
                            targetValue = newOffset,
                            animationSpec = tween(durationMillis = animationDuration),
                        ) { value, _ ->
                            state.heightOffset = value
                        }
                    } finally {
                        animationInProgress = false
                    }
                }

                return consumed
            }
        }
    private var scrollAccumulation: Float = 0f
    private var animationInProgress: Boolean = false
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomScrollBehaviorExampleUI(
    onBackEvent: () -> Unit
) {
    var selectedExample by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        HeaderCard(onBackEvent = onBackEvent)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            TabItem("기본 스냅", selectedExample == 0, { selectedExample = 0 }, Modifier.weight(1f))
            TabItem("애니메이션", selectedExample == 1, { selectedExample = 1 }, Modifier.weight(1f))
            TabItem("최종형", selectedExample == 2, { selectedExample = 2 }, Modifier.weight(1f))
            TabItem("ExitCollapsed", selectedExample == 3, { selectedExample = 3 }, Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (selectedExample) {
            0 -> BasicSnapDemo()
            1 -> AnimatedBufferDemo()
            2 -> FullEnterAlwaysDemo()
            3 -> ExitUntilCollapsedDemo()
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
                    text = "Custom TopAppBarScrollBehavior",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )
                Text(
                    text = "RecyclerView 스크롤 이벤트 커스텀 처리",
                    fontSize = 13.sp,
                    color = Color(0xFF757575)
                )
            }
        }
    }
}

@Composable
private fun TabItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF1976D2) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            textAlign = TextAlign.Center,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else Color(0xFF616161)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BasicSnapDemo() {
    val state = remember { TopAppBarState(0f, 0f, 0f) }
    val scrollBehavior = remember(state) { BasicEnterAlwaysScrollBehavior(state) }

    Column(modifier = Modifier.fillMaxSize()) {
        DescriptionCard(
            title = "1단계: 기본 스냅 (즉시 전환)",
            description = "스크롤 방향에 따라 TopAppBar가 애니메이션 없이 즉시 숨겨지거나 나타납니다.\n" +
                    "모든 미세한 스크롤에도 반응합니다.",
            problemText = "⚠️ 문제: 애니메이션 없이 뚝뚝 끊김, 미세 스크롤에도 반응"
        )

        TopAppBarDemoScaffold(
            scrollBehavior = scrollBehavior,
            barColor = Color(0xFFE53935),
            barTitle = "Basic Snap"
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnimatedBufferDemo() {
    val state = remember { TopAppBarState(0f, 0f, 0f) }
    val coroutineScope = rememberCoroutineScope()
    val scrollBehavior = remember(state) {
        AnimatedEnterAlwaysScrollBehavior(state, coroutineScope)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        DescriptionCard(
            title = "2단계: 애니메이션 + 스크롤 버퍼",
            description = "tween(150ms) 애니메이션으로 부드럽게 전환됩니다.\n" +
                    "100px 이상 누적 스크롤이 있어야 반응합니다.\n" +
                    "이미 완전히 펼쳐졌거나 접혀있으면 무시합니다.",
            problemText = "⚠️ 문제: 느린 스크롤 시 애니메이션이 중복 실행될 수 있음"
        )

        TopAppBarDemoScaffold(
            scrollBehavior = scrollBehavior,
            barColor = Color(0xFFFF8F00),
            barTitle = "Animated + Buffer"
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FullEnterAlwaysDemo() {
    val state = remember { TopAppBarState(0f, 0f, 0f) }
    val coroutineScope = rememberCoroutineScope()
    var bufferValue by remember { mutableStateOf(100f) }
    var durationValue by remember { mutableStateOf(150) }
    val scrollBehavior = remember(state, bufferValue, durationValue) {
        FullEnterAlwaysScrollBehavior(
            state = state,
            coroutineScope = coroutineScope,
            bufferThreshold = bufferValue,
            animationDuration = durationValue
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        DescriptionCard(
            title = "3단계: 최종형 (EnterAlways)",
            description = "animationInProgress 플래그로 애니메이션 중복을 방지합니다.\n" +
                    "느린 스크롤에서도 깔끔하게 동작합니다.\n" +
                    "이것이 블로그에서 제안하는 최종 구현입니다.",
            problemText = "✅ 해결: 부드러운 애니메이션 + 스크롤 버퍼 + 중복 방지"
        )

        ParameterControlCard(
            bufferValue = bufferValue,
            onBufferChange = { bufferValue = it },
            durationValue = durationValue,
            onDurationChange = { durationValue = it },
            onReset = {
                bufferValue = 100f
                durationValue = 150
                state.heightOffset = 0f
            }
        )

        TopAppBarDemoScaffold(
            scrollBehavior = scrollBehavior,
            barColor = Color(0xFF2E7D32),
            barTitle = "Full EnterAlways"
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExitUntilCollapsedDemo() {
    val state = remember { TopAppBarState(0f, 0f, 0f) }
    val coroutineScope = rememberCoroutineScope()
    val scrollBehavior = remember(state) {
        FullExitUntilCollapsedScrollBehavior(state, coroutineScope)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        DescriptionCard(
            title = "보너스: ExitUntilCollapsed",
            description = "위로 스크롤하면 TopAppBar가 숨겨집니다.\n" +
                    "더 이상 아래로 스크롤할 수 없을 때(리스트 맨 위 도달) 다시 나타납니다.\n" +
                    "onPreScroll에서 숨기고, onPostScroll에서 표시합니다.",
            problemText = "💡 핵심: available == Offset.Zero → 스크롤 끝 → 바 표시"
        )

        TopAppBarDemoScaffold(
            scrollBehavior = scrollBehavior,
            barColor = Color(0xFF7B1FA2),
            barTitle = "Exit Until Collapsed"
        )
    }
}

@Composable
private fun DescriptionCard(
    title: String,
    description: String,
    problemText: String,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                fontSize = 13.sp,
                color = Color(0xFF757575),
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = problemText,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (problemText.startsWith("✅")) Color(0xFF2E7D32)
                else if (problemText.startsWith("💡")) Color(0xFF7B1FA2)
                else Color(0xFFE53935),
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun ParameterControlCard(
    bufferValue: Float,
    onBufferChange: (Float) -> Unit,
    durationValue: Int,
    onDurationChange: (Int) -> Unit,
    onReset: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "파라미터 조절",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
                IconButton(onClick = onReset, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Reset",
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "스크롤 버퍼: ${bufferValue.toInt()}px",
                fontSize = 13.sp,
                color = Color(0xFF424242)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(50f, 100f, 200f, 300f).forEach { value ->
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onBufferChange(value) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (bufferValue == value) Color(0xFF2E7D32) else Color.White
                        ),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "${value.toInt()}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            color = if (bufferValue == value) Color.White else Color(0xFF616161)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "애니메이션 시간: ${durationValue}ms",
                fontSize = 13.sp,
                color = Color(0xFF424242)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(50, 150, 300, 500).forEach { value ->
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onDurationChange(value) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (durationValue == value) Color(0xFF2E7D32) else Color.White
                        ),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "${value}ms",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            color = if (durationValue == value) Color.White else Color(0xFF616161)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopAppBarDemoScaffold(
    scrollBehavior: TopAppBarScrollBehavior,
    barColor: Color,
    barTitle: String
) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        barTitle,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = { }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "More",
                            tint = Color.White
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = barColor,
                    scrolledContainerColor = barColor
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = paddingValues.calculateTopPadding() + 8.dp,
                start = 16.dp,
                end = 16.dp,
                bottom = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(40) { index ->
                DemoListItem(index = index + 1, accentColor = barColor)
            }
        }
    }
}

@Composable
private fun DemoListItem(index: Int, accentColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = accentColor.copy(alpha = 0.15f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$index",
                    color = accentColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "항목 $index",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF333333)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "스크롤하여 TopAppBar 동작을 확인해보세요",
                    fontSize = 12.sp,
                    color = Color(0xFF999999)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CustomScrollBehaviorExampleUIPreview() {
    CustomScrollBehaviorExampleUI(onBackEvent = {})
}
