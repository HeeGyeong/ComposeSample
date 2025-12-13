package com.example.composesample.presentation.example.component.ui.button

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader

@Composable
fun ButtonGroupExampleUI(
    onBackEvent: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "ButtonGroup",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { BasicButtonGroupCard() }
            item { SingleSelectCard() }
            item { MultiSelectCard() }
            item { IconOnlyButtonGroupCard() }
            item { ExpandedRatioCard() }
            item { ConnectedButtonGroupCard() }
        }
    }
}

@Composable
private fun BasicButtonGroupCard() {
    var clickedButton by remember { mutableStateOf<Int?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFE3F2FD),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🔘 기본 ButtonGroup",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "클릭 가능한 버튼 그룹 (clickableItem)",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (i in 0 until 4) {
                    SimulatedClickableButton(
                        label = "Btn $i",
                        isPressed = clickedButton == i,
                        onClick = { clickedButton = if (clickedButton == i) null else i },
                        modifier = Modifier.weight(if (clickedButton == i) 1.3f else 1f)
                    )
                }
                Surface(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable { },
                    color = Color(0xFF1976D2)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Filled.MoreVert,
                            contentDescription = "More",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            if (clickedButton != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "✅ Button $clickedButton 클릭됨",
                    fontSize = 11.sp,
                    color = Color(0xFF1976D2)
                )
            }
        }
    }
}

@Composable
private fun SimulatedClickableButton(
    label: String,
    isPressed: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressedState by interactionSource.collectIsPressedAsState()

    Surface(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        color = if (isPressed) Color(0xFF1976D2) else Color(0xFFBBDEFB),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (isPressed) Color.White else Color(0xFF1976D2)
            )
        }
    }
}

@Composable
private fun SingleSelectCard() {
    val options = listOf("Home", "Star", "Heart")
    val icons = listOf(Icons.Outlined.Home, Icons.Outlined.Star, Icons.Outlined.FavoriteBorder)
    val selectedIcons = listOf(Icons.Filled.Home, Icons.Filled.Star, Icons.Filled.Favorite)
    var selectedIndex by remember { mutableIntStateOf(0) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFFFF3E0),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🔘 Single-Select 토글",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE65100)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "하나만 선택 가능 (toggleableItem)",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                options.forEachIndexed { index, label ->
                    SimulatedToggleButton(
                        label = label,
                        icon = if (selectedIndex == index) selectedIcons[index] else icons[index],
                        isSelected = selectedIndex == index,
                        onClick = { selectedIndex = index },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "✅ 선택됨: ${options[selectedIndex]}",
                fontSize = 11.sp,
                color = Color(0xFFE65100)
            )
        }
    }
}

@Composable
private fun SimulatedToggleButton(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFFE65100) else Color(0xFFFFE0B2),
        animationSpec = tween(200),
        label = "bgColor"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else Color(0xFFE65100),
        animationSpec = tween(200),
        label = "contentColor"
    )

    Surface(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        color = backgroundColor,
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = contentColor
            )
        }
    }
}

@Composable
private fun IconOnlyButtonGroupCard() {
    val icons = listOf(
        Icons.Outlined.Home,
        Icons.Outlined.Star,
        Icons.Outlined.FavoriteBorder,
        Icons.Outlined.Search,
        Icons.Outlined.Person
    )
    val filledIcons = listOf(
        Icons.Filled.Home,
        Icons.Filled.Star,
        Icons.Filled.Favorite,
        Icons.Filled.Search,
        Icons.Filled.Person
    )
    var selectedIndex by remember { mutableIntStateOf(0) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFE8F5E9),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🎨 아이콘 전용 ButtonGroup",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "텍스트 없이 아이콘만 사용",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                icons.forEachIndexed { index, icon ->
                    IconOnlyButton(
                        icon = if (selectedIndex == index) filledIcons[index] else icon,
                        isSelected = selectedIndex == index,
                        onClick = { selectedIndex = index },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val labels = listOf("Home", "Star", "Heart", "Search", "Person")
            Text(
                text = "✅ 선택됨: ${labels[selectedIndex]}",
                fontSize = 11.sp,
                color = Color(0xFF2E7D32)
            )
        }
    }
}

@Composable
private fun IconOnlyButton(
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF2E7D32) else Color(0xFFC8E6C9),
        animationSpec = tween(200),
        label = "bgColor"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else Color(0xFF2E7D32),
        animationSpec = tween(200),
        label = "contentColor"
    )

    Surface(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        color = backgroundColor,
        shape = CircleShape
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun ExpandedRatioCard() {
    var selectedRatio by remember { mutableIntStateOf(1) }
    val ratios = listOf(0f, 0.5f, 1f)
    val ratioLabels = listOf("0f (비활성)", "0.5f (50%)", "1f (100%)")
    var clickedButton by remember { mutableStateOf<Int?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFFCE4EC),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "📏 ExpandedRatio 비교",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFC2185B)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "버튼 클릭 시 확장 비율 설정",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ratioLabels.forEachIndexed { index, label ->
                    RatioSelectorButton(
                        label = label,
                        isSelected = selectedRatio == index,
                        onClick = { 
                            selectedRatio = index
                            clickedButton = null
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (i in 0 until 3) {
                    ExpandableButton(
                        label = "Btn $i",
                        isPressed = clickedButton == i,
                        expandedRatio = ratios[selectedRatio],
                        onClick = { clickedButton = if (clickedButton == i) null else i },
                        modifier = Modifier.weight(
                            if (clickedButton == i) 1f + ratios[selectedRatio] else 1f
                        )
                    )
                }
            }

            if (clickedButton != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "✅ Button $clickedButton 클릭됨 (확장비율: ${ratioLabels[selectedRatio]})",
                    fontSize = 11.sp,
                    color = Color(0xFFC2185B)
                )
            }
        }
    }
}

@Composable
private fun RatioSelectorButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFFC2185B) else Color(0xFFF8BBD0),
        animationSpec = tween(200),
        label = "bgColor"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else Color(0xFFC2185B),
        animationSpec = tween(200),
        label = "contentColor"
    )

    Surface(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        color = backgroundColor,
        shape = RoundedCornerShape(18.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor
            )
        }
    }
}

@Composable
private fun ExpandableButton(
    label: String,
    isPressed: Boolean,
    expandedRatio: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        color = if (isPressed) Color(0xFFC2185B) else Color(0xFFF8BBD0),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (isPressed) Color.White else Color(0xFFC2185B)
            )
        }
    }
}

@Composable
private fun MultiSelectCard() {
    val options = listOf("Home", "Star", "Heart")
    val icons = listOf(Icons.Outlined.Home, Icons.Outlined.Star, Icons.Outlined.FavoriteBorder)
    val selectedIcons = listOf(Icons.Filled.Home, Icons.Filled.Star, Icons.Filled.Favorite)
    val checked = remember { mutableStateListOf(false, true, false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFF3E5F5),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🔘 Multi-Select 토글",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF7B1FA2)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "여러 개 선택 가능",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                options.forEachIndexed { index, label ->
                    SimulatedMultiToggleButton(
                        label = label,
                        icon = if (checked[index]) selectedIcons[index] else icons[index],
                        isSelected = checked[index],
                        onClick = { checked[index] = !checked[index] },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val selectedItems = options.filterIndexed { index, _ -> checked[index] }
            Text(
                text = if (selectedItems.isEmpty()) "선택 없음" else "✅ 선택됨: ${selectedItems.joinToString(", ")}",
                fontSize = 11.sp,
                color = Color(0xFF7B1FA2)
            )
        }
    }
}

@Composable
private fun SimulatedMultiToggleButton(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF7B1FA2) else Color(0xFFE1BEE7),
        animationSpec = tween(200),
        label = "bgColor"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else Color(0xFF7B1FA2),
        animationSpec = tween(200),
        label = "contentColor"
    )

    Surface(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        color = backgroundColor,
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = contentColor
            )
            if (isSelected) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ConnectedButtonGroupCard() {
    val options = listOf("Home", "Star", "Heart", "Search", "Person")
    val icons = listOf(
        Icons.Outlined.Home,
        Icons.Outlined.Star,
        Icons.Outlined.FavoriteBorder,
        Icons.Outlined.Search,
        Icons.Outlined.Person
    )
    val selectedIcons = listOf(
        Icons.Filled.Home,
        Icons.Filled.Star,
        Icons.Filled.Favorite,
        Icons.Filled.Search,
        Icons.Filled.Person
    )
    var selectedIndex by remember { mutableIntStateOf(0) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFE1F5FE),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🔗 Connected ButtonGroup",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0277BD)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "세그먼트 컨트롤 스타일 (SegmentedButton 대체)",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                options.take(3).forEachIndexed { index, label ->
                    ConnectedSegmentButton(
                        label = label,
                        icon = if (selectedIndex == index) selectedIcons[index] else icons[index],
                        isSelected = selectedIndex == index,
                        position = when (index) {
                            0 -> SegmentPosition.LEADING
                            2 -> SegmentPosition.TRAILING
                            else -> SegmentPosition.MIDDLE
                        },
                        onClick = { selectedIndex = index },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                options.forEachIndexed { index, label ->
                    ConnectedSegmentButton(
                        label = label,
                        icon = if (selectedIndex == index) selectedIcons[index] else icons[index],
                        isSelected = selectedIndex == index,
                        position = when (index) {
                            0 -> SegmentPosition.LEADING
                            options.lastIndex -> SegmentPosition.TRAILING
                            else -> SegmentPosition.MIDDLE
                        },
                        onClick = { selectedIndex = index },
                        modifier = Modifier
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "✅ 선택됨: ${options[selectedIndex]}",
                fontSize = 11.sp,
                color = Color(0xFF0277BD)
            )
        }
    }
}

enum class SegmentPosition {
    LEADING, MIDDLE, TRAILING
}

@Composable
private fun ConnectedSegmentButton(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    position: SegmentPosition,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = if (isSelected) {
        RoundedCornerShape(24.dp)
    } else {
        when (position) {
            SegmentPosition.LEADING -> RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp)
            SegmentPosition.TRAILING -> RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
            SegmentPosition.MIDDLE -> RoundedCornerShape(0.dp)
        }
    }

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF0277BD) else Color(0xFFB3E5FC),
        animationSpec = tween(200),
        label = "bgColor"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else Color(0xFF0277BD),
        animationSpec = tween(200),
        label = "contentColor"
    )

    val elevation by animateDpAsState(
        targetValue = if (isSelected) 4.dp else 0.dp,
        animationSpec = tween(200),
        label = "elevation"
    )

    Surface(
        modifier = modifier
            .height(44.dp)
            .clip(shape)
            .clickable(onClick = onClick),
        color = backgroundColor,
        shape = shape,
        elevation = elevation
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor
            )
        }
    }
}

