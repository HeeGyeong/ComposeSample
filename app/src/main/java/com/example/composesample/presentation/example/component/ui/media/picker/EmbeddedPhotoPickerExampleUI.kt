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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
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

        // Example selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ExampleTab(
                text = "BottomSheet",
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
                text = "Concepts",
                isSelected = selectedExample == 2,
                onClick = { selectedExample = 2 },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (selectedExample) {
            0 -> BottomSheetPickerDemo()
            1 -> OrderedSelectionDemo()
            2 -> ConceptsExplanation()
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

// ==================== 1. BottomSheet Picker Demo ====================

/**
 * BottomSheetScaffold에 포토 피커를 임베드하는 패턴을 시뮬레이션합니다.
 *
 * 실제 구현에서는 EmbeddedPhotoPicker composable이 sheetContent에 배치되지만,
 * 여기서는 Canvas 기반 시뮬레이션된 사진 그리드를 사용합니다.
 *
 * 실제 코드:
 * ```
 * val photoPickerState = rememberEmbeddedPhotoPickerState(
 *     onSelectionComplete = { scaffoldState.bottomSheetState.hide() },
 *     onUriPermissionGranted = { attachments += it },
 *     onUriPermissionRevoked = { attachments -= it }
 * )
 *
 * BottomSheetScaffold(
 *     sheetContent = {
 *         EmbeddedPhotoPicker(
 *             state = photoPickerState,
 *             embeddedPhotoPickerFeatureInfo = photoPickerInfo
 *         )
 *     }
 * ) { ... }
 * ```
 */
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

    // 시뮬레이션된 사진 데이터
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
            // === 시뮬레이션된 Embedded Photo Picker ===
            // 실제로는 EmbeddedPhotoPicker composable이 이 위치에 배치됩니다.
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Sheet handle indicator
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

                // Header
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

                // 시뮬레이션된 포토 그리드
                // 실제로는 시스템이 렌더링하는 SurfaceView가 여기에 표시됩니다.
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
                                    // URI 권한 해제 시뮬레이션
                                    // 실제: photoPickerState.deselectUri(uri)
                                    selectedPhotos.remove(photo)
                                } else if (selectedPhotos.size < maxSelection) {
                                    // URI 권한 부여 시뮬레이션
                                    // 실제: onUriPermissionGranted 콜백
                                    selectedPhotos.add(photo)
                                }
                            }
                        )
                    }
                }

                // Done 버튼
                Button(
                    onClick = {
                        // 실제: onSelectionComplete 콜백 → scaffoldState.bottomSheetState.hide()
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
        // === 호스트 앱 콘텐츠 ===
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

            // 선택된 사진 표시 영역
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
                        // 호스트 앱에서 선택된 사진 표시
                        // 실제로는 AsyncImage(model = uri, ...)로 렌더링
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    // 호스트 앱에서 선택 해제 시
                                    // 실제: photoPickerState.deselectUri(uri)
                                    selectedPhotos.remove(photo)
                                }
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawRoundRect(
                                    color = photo.color,
                                    cornerRadius = CornerRadius(8.dp.toPx())
                                )
                            }
                            // Remove badge
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

// ==================== 2. Ordered Selection Demo ====================

/**
 * 순서가 있는 다중 선택 패턴을 시뮬레이션합니다.
 *
 * EmbeddedPhotoPickerFeatureInfo.Builder()
 *     .setMaxSelectionLimit(5)
 *     .setOrderedSelection(true) // 순서 표시
 *     .build()
 */
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

                    // Accent color selector
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

        // Simulated photo grid
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

        // Code reference
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF263238)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "실제 구현 코드",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF80CBC4)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = """val photoPickerInfo = EmbeddedPhotoPickerFeatureInfo
    .Builder()
    .setMaxSelectionLimit($maxSelection)
    .setOrderedSelection(true)
    .setAccentColor(0x${
                            Integer.toHexString(
                                android.graphics.Color.rgb(
                                    (currentAccent.red * 255).toInt(),
                                    (currentAccent.green * 255).toInt(),
                                    (currentAccent.blue * 255).toInt()
                                )
                            ).uppercase()
                        })
    .build()""",
                        fontSize = 12.sp,
                        color = Color(0xFFE0E0E0),
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

// ==================== 3. Concepts Explanation ====================

@Composable
private fun ConceptsExplanation() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Guide card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "💡 가이드",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = EmbeddedPhotoPickerGuide.GUIDE_INFO.trimIndent(),
                        fontSize = 13.sp,
                        color = Color(0xFF424242),
                        lineHeight = 20.sp
                    )
                }
            }
        }

        item {
            ConceptCard(
                title = "1. 기존 Photo Picker vs Embedded Photo Picker",
                description = "기존 Photo Picker는 전체 화면 Activity로 실행되어 앱을 벗어나지만, " +
                        "Embedded Photo Picker는 앱의 UI 안에 직접 임베드됩니다.\n\n" +
                        "BottomSheet, Dialog, 또는 다른 컨테이너에 넣어서 " +
                        "앱을 떠나지 않는 심리스한 UX를 제공합니다.",
                codeSnippet = """// 기존 Photo Picker (전체 화면)
val launcher = rememberLauncherForActivityResult(
    ActivityResultContracts.PickMultipleVisualMedia()
) { uris -> /* handle */ }

// Embedded Photo Picker (앱 내 임베드)
EmbeddedPhotoPicker(
    state = photoPickerState,
    embeddedPhotoPickerFeatureInfo = info
)"""
            )
        }

        item {
            ConceptCard(
                title = "2. 프라이버시 모델",
                description = "Embedded Photo Picker는 사용자가 선택한 사진/영상에만 접근 권한을 부여합니다.\n\n" +
                        "• onUriPermissionGranted: 사진 선택 시 URI 권한 부여\n" +
                        "• onUriPermissionRevoked: 사진 해제 시 URI 권한 해제\n\n" +
                        "앱은 READ_MEDIA_IMAGES/VIDEO 권한을 요청할 필요가 없습니다.",
                codeSnippet = """val state = rememberEmbeddedPhotoPickerState(
    onUriPermissionGranted = { uris ->
        // 사진 선택 → URI 접근 권한 부여
        attachments += uris
    },
    onUriPermissionRevoked = { uris ->
        // 사진 해제 → URI 접근 권한 해제
        attachments -= uris
    },
    onSelectionComplete = {
        // Done 버튼 클릭 → 바텀시트 닫기
        scope.launch { sheetState.hide() }
    }
)"""
            )
        }

        item {
            ConceptCard(
                title = "3. FeatureInfo 커스터마이징",
                description = "Builder 패턴으로 포토 피커의 동작을 커스터마이징할 수 있습니다.\n\n" +
                        "• setMaxSelectionLimit: 최대 선택 수 (기본 무제한)\n" +
                        "• setOrderedSelection: 선택 순서 표시 (인덱스 오버레이)\n" +
                        "• setAccentColor: 강조 색상 (기본: 기기 색상)",
                codeSnippet = """val photoPickerInfo = EmbeddedPhotoPickerFeatureInfo
    .Builder()
    .setMaxSelectionLimit(5)
    .setOrderedSelection(true)
    .setAccentColor(0xFF0000) // Red
    .build()"""
            )
        }

        item {
            ConceptCard(
                title = "4. SideEffect 동기화",
                description = "BottomSheet의 확장/축소 상태와 Embedded Photo Picker의 표시 상태를 " +
                        "SideEffect를 사용하여 동기화합니다.\n\n" +
                        "이는 시스템이 렌더링하는 SurfaceView와 Compose 상태를 연결하는 핵심 패턴입니다.",
                codeSnippet = """SideEffect {
    val isExpanded = scaffoldState
        .bottomSheetState.targetValue == 
        SheetValue.Expanded

    // 바텀시트 확장/축소에 맞춰 포토 피커 동기화
    photoPickerState.setCurrentExpanded(isExpanded)
}"""
            )
        }

        item {
            ConceptCard(
                title = "5. 호스트 앱에서 선택 해제",
                description = "호스트 앱의 UI에서도 사진을 해제할 수 있습니다.\n" +
                        "deselectUri()를 호출하면 Embedded Photo Picker 내에서도 " +
                        "해당 사진의 선택이 해제됩니다.",
                codeSnippet = """// 호스트 앱의 이미지 클릭 시 해제
AsyncImage(
    model = uri,
    modifier = Modifier.clickable {
        scope.launch {
            // 피커에서도 선택 해제 동기화
            photoPickerState.deselectUri(uri)
        }
    }
)"""
            )
        }

        item {
            ConceptCard(
                title = "6. Views API (비 Compose)",
                description = "Compose를 사용하지 않는 프로젝트에서는 " +
                        "EmbeddedPhotoPickerView를 XML 레이아웃에 추가하고 " +
                        "EmbeddedPhotoPickerSession으로 상호작용합니다.",
                codeSnippet = """<!-- XML Layout -->
<view 
    class="androidx.photopicker
        .EmbeddedPhotoPickerView"
    android:id="@+id/photopicker"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />

// Session 조작
session.notifyPhotoPickerExpanded(true)
session.notifyResized(512, 256)
session.notifyVisibilityChanged(false)
session.requestRevokeUriPermission(uris)"""
            )
        }

        item {
            // 요구사항 카드
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFCE4EC)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "⚠️ 요구사항",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC62828)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Android 14 (API 34) 이상 + SDK Extensions 15+\n" +
                                "• 의존성: androidx.photopicker:photopicker-compose:1.0.0-alpha01\n" +
                                "• 시스템이 렌더링하므로 오버레이/드로잉 불가\n" +
                                "• 포토 피커 영역은 전용 영역으로 취급 (광고 배너처럼)",
                        fontSize = 13.sp,
                        color = Color(0xFF424242),
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ConceptCard(
    title: String,
    description: String,
    codeSnippet: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                fontSize = 14.sp,
                color = Color(0xFF616161),
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Code block
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF263238)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = codeSnippet,
                    modifier = Modifier.padding(12.dp),
                    fontSize = 11.sp,
                    color = Color(0xFFE0E0E0),
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

// ==================== Simulated Photo Components ====================

data class SimulatedPhoto(
    val id: Int,
    val color: Color,
    val label: String
)

/**
 * 시뮬레이션된 사진 아이템.
 * 실제로는 시스템이 렌더링하는 사진 썸네일이 표시되지만,
 * 여기서는 Canvas로 색상 블록을 그려 사진을 시뮬레이션합니다.
 */
@Composable
private fun SimulatedPhotoItem(
    photo: SimulatedPhoto,
    isSelected: Boolean,
    selectionOrder: Int,
    accentColor: Color = Color(0xFF1976D2),
    onToggle: () -> Unit
) {
    val textMeasurer = rememberTextMeasurer()
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
        // Simulated photo thumbnail
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Photo background
            drawRoundRect(
                color = photo.color,
                cornerRadius = CornerRadius(4.dp.toPx())
            )

            // Simple landscape icon to simulate photo content
            val iconSize = size.minDimension * 0.3f
            val iconX = (size.width - iconSize) / 2f
            val iconY = (size.height - iconSize) / 2f

            // Mountain shape
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

            // Sun
            drawCircle(
                color = Color.White.copy(alpha = 0.4f),
                radius = iconSize * 0.12f,
                center = Offset(iconX + iconSize * 0.25f, iconY + iconSize * 0.25f)
            )
        }

        // Selection indicator with order number
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

        // Checkmark for selected state (when order <= 0, i.e. non-ordered)
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

/**
 * 인덱스 기반으로 시뮬레이션된 사진 색상을 생성합니다.
 */
private fun generatePhotoColor(index: Int): Color {
    val random = Random(index * 42 + 7)
    val hue = (index * 37f + random.nextFloat() * 30f) % 360f
    val saturation = 0.3f + random.nextFloat() * 0.3f
    val lightness = 0.5f + random.nextFloat() * 0.2f

    // HSL to RGB approximation
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
