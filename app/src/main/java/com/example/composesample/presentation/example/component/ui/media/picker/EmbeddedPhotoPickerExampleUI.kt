package com.example.composesample.presentation.example.component.ui.media.picker

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Embedded Photo Picker Example UI
 *
 * Android의 Embedded Photo Picker 패턴을 시뮬레이션하여 보여주는 예제입니다.
 * 실제 구현에는 androidx.photopicker:photopicker-compose 라이브러리가 필요하지만,
 * 여기서는 동일한 UX 패턴을 직접 구현하여 개념을 시연합니다.
 */
@Composable
fun EmbeddedPhotoPickerExampleUI(
    onBackEvent: () -> Unit
) {
    var selectedExample by remember { mutableStateOf(0) }

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
            ExampleTab(
                text = "Sheet",
                isSelected = selectedExample == 0,
                onClick = { selectedExample = 0 },
                modifier = Modifier.weight(1f)
            )
            ExampleTab(
                text = "Ordered",
                isSelected = selectedExample == 1,
                onClick = { selectedExample = 1 },
                modifier = Modifier.weight(1f)
            )
            ExampleTab(
                text = "Filter",
                isSelected = selectedExample == 2,
                onClick = { selectedExample = 2 },
                modifier = Modifier.weight(1f)
            )
            ExampleTab(
                text = "Config",
                isSelected = selectedExample == 3,
                onClick = { selectedExample = 3 },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (selectedExample) {
            0 -> BottomSheetPickerDemo()
            1 -> OrderedSelectionDemo()
            2 -> MimeTypeFilterDemo()
            3 -> ConfigurationPlaygroundDemo()
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
                    text = "Embedded Photo Picker",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )
                Text(
                    text = "앱 내에서 사진/영상을 선택하는 임베디드 피커",
                    fontSize = 14.sp,
                    color = Color(0xFF757575)
                )
            }
        }
    }
}

@Composable
private fun ExampleTab(
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
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else Color(0xFF616161)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BottomSheetPickerDemo() {
    val selectedPhotos = remember { mutableStateListOf<SimulatedPhoto>() }
    val maxSelection = 5
    val coroutineScope = rememberCoroutineScope()

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded
        )
    )

    val photos = remember {
        List(20) { index ->
            SimulatedPhoto(
                id = index,
                color = generatePhotoColor(index),
                label = "Photo ${index + 1}"
            )
        }
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 300.dp,
        sheetContainerColor = Color.White,
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        sheetContent = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0xFFBDBDBD))
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "사진 선택",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    Text(
                        text = "${selectedPhotos.size} / $maxSelection",
                        fontSize = 14.sp,
                        color = if (selectedPhotos.size >= maxSelection)
                            Color(0xFFF44336) else Color(0xFF757575)
                    )
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
                    contentPadding = PaddingValues(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    itemsIndexed(photos) { _, photo ->
                        SimulatedPhotoItem(
                            photo = photo,
                            isSelected = selectedPhotos.contains(photo),
                            selectionOrder = selectedPhotos.indexOf(photo) + 1,
                            onToggle = {
                                if (selectedPhotos.contains(photo)) {
                                    selectedPhotos.remove(photo)
                                } else if (selectedPhotos.size < maxSelection) {
                                    selectedPhotos.add(photo)
                                }
                            }
                        )
                    }
                }

                Button(
                    onClick = {
                        coroutineScope.launch {
                            scaffoldState.bottomSheetState.partialExpand()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1976D2)
                    ),
                    enabled = selectedPhotos.isNotEmpty(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (selectedPhotos.isEmpty()) "사진을 선택하세요"
                        else "${selectedPhotos.size}장 선택 완료",
                        fontSize = 16.sp
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            tint = Color(0xFF1976D2),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "BottomSheet Embedded Picker",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF212121)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "아래 바텀시트를 드래그하여 포토 피커를 확장하세요.\n" +
                                "사진을 탭하여 선택/해제할 수 있습니다.\n" +
                                "최대 ${maxSelection}장까지 선택 가능합니다.",
                        fontSize = 14.sp,
                        color = Color(0xFF757575),
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "선택된 사진",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (selectedPhotos.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "아래 바텀시트에서 사진을 선택하세요",
                            fontSize = 14.sp,
                            color = Color(0xFFBDBDBD)
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 80.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(selectedPhotos.toList()) { _, photo ->
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    selectedPhotos.remove(photo)
                                }
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawRoundRect(
                                    color = photo.color,
                                    cornerRadius = CornerRadius(8.dp.toPx())
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Remove",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderedSelectionDemo() {
    val selectedPhotos = remember { mutableStateListOf<SimulatedPhoto>() }
    val maxSelection = 8
    var accentColorIndex by remember { mutableIntStateOf(0) }

    val accentColors = listOf(
        Color(0xFF1976D2),  // Blue
        Color(0xFFF44336),  // Red
        Color(0xFF4CAF50),  // Green
        Color(0xFFFF9800),  // Orange
        Color(0xFF9C27B0),  // Purple
    )
    val currentAccent = accentColors[accentColorIndex]

    val photos = remember {
        List(24) { index ->
            SimulatedPhoto(
                id = index,
                color = generatePhotoColor(index),
                label = "IMG_${1000 + index}"
            )
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Ordered Selection",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    Text(
                        text = "setOrderedSelection(true) 설정 시, 각 사진에 선택 순서 번호가 표시됩니다.\n" +
                                "setAccentColor()로 강조색을 변경할 수 있습니다.",
                        fontSize = 14.sp,
                        color = Color(0xFF757575),
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Accent Color:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF424242)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        accentColors.forEachIndexed { index, color ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .then(
                                        if (index == accentColorIndex)
                                            Modifier.border(3.dp, Color.Black, CircleShape)
                                        else Modifier.border(1.dp, Color.LightGray, CircleShape)
                                    )
                                    .clickable { accentColorIndex = index }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "${selectedPhotos.size} / $maxSelection 선택됨",
                        fontSize = 14.sp,
                        color = currentAccent,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(480.dp)
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    itemsIndexed(photos) { _, photo ->
                        SimulatedPhotoItem(
                            photo = photo,
                            isSelected = selectedPhotos.contains(photo),
                            selectionOrder = selectedPhotos.indexOf(photo) + 1,
                            accentColor = currentAccent,
                            onToggle = {
                                if (selectedPhotos.contains(photo)) {
                                    selectedPhotos.remove(photo)
                                } else if (selectedPhotos.size < maxSelection) {
                                    selectedPhotos.add(photo)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

private enum class MediaTypeFilter(val label: String) {
    ALL("전체"),
    PHOTO("사진"),
    VIDEO("동영상"),
    GIF("GIF")
}

private data class SimulatedMedia(
    val id: Int,
    val color: Color,
    val label: String,
    val mediaType: MediaTypeFilter
)

@Composable
private fun MimeTypeFilterDemo() {
    var selectedFilter by remember { mutableStateOf(MediaTypeFilter.ALL) }
    val selectedMedia = remember { mutableStateListOf<SimulatedMedia>() }
    val maxSelection = 6

    val allMedia = remember {
        List(30) { index ->
            val type = when {
                index % 7 == 0 -> MediaTypeFilter.GIF
                index % 4 == 0 -> MediaTypeFilter.VIDEO
                else -> MediaTypeFilter.PHOTO
            }
            SimulatedMedia(
                id = index,
                color = generatePhotoColor(index + 50),
                label = when (type) {
                    MediaTypeFilter.PHOTO -> "IMG_${1000 + index}"
                    MediaTypeFilter.VIDEO -> "VID_${2000 + index}"
                    MediaTypeFilter.GIF -> "GIF_${3000 + index}"
                    else -> "FILE_$index"
                },
                mediaType = type
            )
        }
    }

    val filteredMedia = if (selectedFilter == MediaTypeFilter.ALL) allMedia
    else allMedia.filter { it.mediaType == selectedFilter }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "MIME Type Filter",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    Text(
                        text = "setMimeTypes()를 사용하여 표시할 미디어 유형을 필터링합니다.\n" +
                                "사진, 동영상, GIF 등 원하는 유형만 선택적으로 표시할 수 있습니다.",
                        fontSize = 14.sp,
                        color = Color(0xFF757575),
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MediaTypeFilter.entries.forEach { filter ->
                            FilterChipItem(
                                text = filter.label,
                                isSelected = selectedFilter == filter,
                                onClick = {
                                    selectedFilter = filter
                                    selectedMedia.clear()
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "${filteredMedia.size}개 항목 | ${selectedMedia.size} / $maxSelection 선택됨",
                        fontSize = 13.sp,
                        color = Color(0xFF757575)
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(480.dp)
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    itemsIndexed(filteredMedia) { _, media ->
                        SimulatedMediaItem(
                            media = media,
                            isSelected = selectedMedia.contains(media),
                            onToggle = {
                                if (selectedMedia.contains(media)) {
                                    selectedMedia.remove(media)
                                } else if (selectedMedia.size < maxSelection) {
                                    selectedMedia.add(media)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConfigurationPlaygroundDemo() {
    var maxCount by remember { mutableIntStateOf(5) }
    var orderedSelection by remember { mutableStateOf(true) }
    var accentColorIndex by remember { mutableIntStateOf(0) }
    val selectedPhotos = remember { mutableStateListOf<SimulatedPhoto>() }

    val accentColors = listOf(
        Color(0xFF1976D2),
        Color(0xFFF44336),
        Color(0xFF4CAF50),
        Color(0xFFFF9800),
        Color(0xFF9C27B0),
    )
    val currentAccent = accentColors[accentColorIndex]

    val photos = remember {
        List(20) { index ->
            SimulatedPhoto(
                id = index,
                color = generatePhotoColor(index + 100),
                label = "Photo ${index + 1}"
            )
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Configuration Playground",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    Text(
                        text = "EmbeddedPhotoPickerFeatureInfo의 각 설정을 실시간으로 변경하며 동작을 확인합니다.",
                        fontSize = 14.sp,
                        color = Color(0xFF757575),
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "setMaxCount",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF424242)
                        )
                        Text(
                            text = "$maxCount",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = currentAccent
                        )
                    }
                    val minAllowed = maxOf(1, selectedPhotos.size)
                    Slider(
                        value = maxCount.toFloat(),
                        onValueChange = { maxCount = maxOf(it.toInt(), minAllowed) },
                        valueRange = minAllowed.toFloat()..10f,
                        steps = maxOf(0, 10 - minAllowed - 1),
                        colors = SliderDefaults.colors(
                            thumbColor = currentAccent,
                            activeTrackColor = currentAccent
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "setOrderedSelection",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF424242)
                        )
                        Switch(
                            checked = orderedSelection,
                            onCheckedChange = { orderedSelection = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = currentAccent,
                                checkedTrackColor = currentAccent.copy(alpha = 0.5f)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "setAccentColor",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF424242)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        accentColors.forEachIndexed { index, color ->
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .then(
                                        if (index == accentColorIndex)
                                            Modifier.border(3.dp, Color.Black, CircleShape)
                                        else Modifier.border(1.dp, Color.LightGray, CircleShape)
                                    )
                                    .clickable { accentColorIndex = index }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "${selectedPhotos.size} / $maxCount 선택됨",
                        fontSize = 14.sp,
                        color = currentAccent,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    itemsIndexed(photos) { _, photo ->
                        SimulatedPhotoItem(
                            photo = photo,
                            isSelected = selectedPhotos.contains(photo),
                            selectionOrder = if (orderedSelection) selectedPhotos.indexOf(photo) + 1 else 0,
                            accentColor = currentAccent,
                            onToggle = {
                                if (selectedPhotos.contains(photo)) {
                                    selectedPhotos.remove(photo)
                                } else if (selectedPhotos.size < maxCount) {
                                    selectedPhotos.add(photo)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SimulatedMediaItem(
    media: SimulatedMedia,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    val borderAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        label = "mediaBorder"
    )

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(4.dp))
            .then(
                if (isSelected) Modifier.border(
                    3.dp,
                    Color(0xFF1976D2).copy(alpha = borderAlpha),
                    RoundedCornerShape(4.dp)
                )
                else Modifier
            )
            .clickable { onToggle() }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRoundRect(
                color = media.color,
                cornerRadius = CornerRadius(4.dp.toPx())
            )

            val iconSize = size.minDimension * 0.3f
            val iconX = (size.width - iconSize) / 2f
            val iconY = (size.height - iconSize) / 2f

            when (media.mediaType) {
                MediaTypeFilter.VIDEO -> {
                    val playPath = Path().apply {
                        moveTo(iconX, iconY)
                        lineTo(iconX + iconSize, iconY + iconSize / 2f)
                        lineTo(iconX, iconY + iconSize)
                        close()
                    }
                    drawPath(
                        path = playPath,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
                MediaTypeFilter.GIF -> {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.4f),
                        radius = iconSize * 0.5f,
                        center = Offset(size.width / 2f, size.height / 2f)
                    )
                }
                else -> {
                    val mountainPath = Path().apply {
                        moveTo(iconX, iconY + iconSize)
                        lineTo(iconX + iconSize * 0.4f, iconY + iconSize * 0.3f)
                        lineTo(iconX + iconSize * 0.6f, iconY + iconSize * 0.6f)
                        lineTo(iconX + iconSize * 0.8f, iconY + iconSize * 0.2f)
                        lineTo(iconX + iconSize, iconY + iconSize)
                        close()
                    }
                    drawPath(
                        path = mountainPath,
                        color = Color.White.copy(alpha = 0.4f)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.4f),
                        radius = iconSize * 0.12f,
                        center = Offset(iconX + iconSize * 0.25f, iconY + iconSize * 0.25f)
                    )
                }
            }
        }

        if (media.mediaType == MediaTypeFilter.VIDEO) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "0:${(10 + media.id % 50).toString().padStart(2, '0')}",
                    fontSize = 9.sp,
                    color = Color.White
                )
            }
        }

        if (media.mediaType == MediaTypeFilter.GIF) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "GIF",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        AnimatedVisibility(
            visible = isSelected,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1976D2)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Selected",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun FilterChipItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) Color(0xFF1976D2) else Color(0xFFE0E0E0))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else Color(0xFF616161)
        )
    }
}

data class SimulatedPhoto(
    val id: Int,
    val color: Color,
    val label: String
)

@Composable
private fun SimulatedPhotoItem(
    photo: SimulatedPhoto,
    isSelected: Boolean,
    selectionOrder: Int,
    accentColor: Color = Color(0xFF1976D2),
    onToggle: () -> Unit
) {
    val borderAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        label = "border"
    )

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(4.dp))
            .then(
                if (isSelected) Modifier.border(
                    3.dp,
                    accentColor.copy(alpha = borderAlpha),
                    RoundedCornerShape(4.dp)
                )
                else Modifier
            )
            .clickable { onToggle() }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRoundRect(
                color = photo.color,
                cornerRadius = CornerRadius(4.dp.toPx())
            )

            val iconSize = size.minDimension * 0.3f
            val iconX = (size.width - iconSize) / 2f
            val iconY = (size.height - iconSize) / 2f

            val mountainPath = Path().apply {
                moveTo(iconX, iconY + iconSize)
                lineTo(iconX + iconSize * 0.4f, iconY + iconSize * 0.3f)
                lineTo(iconX + iconSize * 0.6f, iconY + iconSize * 0.6f)
                lineTo(iconX + iconSize * 0.8f, iconY + iconSize * 0.2f)
                lineTo(iconX + iconSize, iconY + iconSize)
                close()
            }
            drawPath(
                path = mountainPath,
                color = Color.White.copy(alpha = 0.4f)
            )

            drawCircle(
                color = Color.White.copy(alpha = 0.4f),
                radius = iconSize * 0.12f,
                center = Offset(iconX + iconSize * 0.25f, iconY + iconSize * 0.25f)
            )
        }

        AnimatedVisibility(
            visible = isSelected && selectionOrder > 0,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(accentColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$selectionOrder",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        AnimatedVisibility(
            visible = isSelected && selectionOrder <= 0,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(accentColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Selected",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

private fun generatePhotoColor(index: Int): Color {
    val random = Random(index * 42 + 7)
    val hue = (index * 37f + random.nextFloat() * 30f) % 360f
    val saturation = 0.3f + random.nextFloat() * 0.3f
    val lightness = 0.5f + random.nextFloat() * 0.2f

    val c = (1f - kotlin.math.abs(2f * lightness - 1f)) * saturation
    val x = c * (1f - kotlin.math.abs((hue / 60f) % 2f - 1f))
    val m = lightness - c / 2f

    val (r, g, b) = when {
        hue < 60f -> Triple(c, x, 0f)
        hue < 120f -> Triple(x, c, 0f)
        hue < 180f -> Triple(0f, c, x)
        hue < 240f -> Triple(0f, x, c)
        hue < 300f -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }

    return Color(
        red = (r + m).coerceIn(0f, 1f),
        green = (g + m).coerceIn(0f, 1f),
        blue = (b + m).coerceIn(0f, 1f)
    )
}

@Preview(showBackground = true)
@Composable
fun EmbeddedPhotoPickerExampleUIPreview() {
    EmbeddedPhotoPickerExampleUI(onBackEvent = {})
}
