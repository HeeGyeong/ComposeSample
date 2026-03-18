package com.example.composesample.presentation.example.component.ui.media.picker

import android.net.Uri
import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Embedded Photo Picker (Compose 통합) - Example UI
 *
 * 블로그의 핵심 패턴을 시뮬레이션합니다:
 * 1. 가용성 체크 (API 34 + SDK Extensions 15)
 * 2. BottomSheet 통합 아키텍처 + setCurrentExpanded
 * 3. 선택 동기화 오너십 모델 (deselectUri 함정)
 * 4. URI 수명 관리 + 5,000 그랜트 제한
 *
 * 실제 구현에는 androidx.photopicker:photopicker-compose:1.0.0-alpha01 라이브러리와
 * minSdk 34이 필요합니다.
 */

private data class FakeMedia(
    val id: Int,
    val color: Color,
    val label: String,
    val uri: Uri = Uri.parse("content://media/fake/$id")
)

private val fakeMediaList = (1..12).map { i ->
    FakeMedia(
        id = i,
        color = Color(
            red = (0.3f + i * 0.06f).coerceIn(0f, 1f),
            green = (0.5f + i * 0.03f).coerceIn(0f, 1f),
            blue = (0.7f - i * 0.04f).coerceIn(0f, 1f)
        ),
        label = "📷 $i"
    )
}

@Composable
fun EmbeddedPickerComposeExampleUI(
    onBackEvent: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
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
                        text = "Compose 통합 · BottomSheet · URI 수명 · 선택 동기화",
                        fontSize = 13.sp,
                        color = Color(0xFF757575)
                    )
                }
            }
        }

        AvailabilityBadge()

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("BottomSheet\n통합", "선택\n동기화", "URI\n수명", "아키텍처").forEachIndexed { i, label ->
                PickerTab(
                    text = label,
                    isSelected = selectedTab == i,
                    onClick = { selectedTab = i }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (selectedTab) {
            0 -> BottomSheetIntegrationDemo()
            1 -> SelectionSyncDemo()
            2 -> UriLifetimeDemo()
            3 -> ArchitectureOverviewDemo()
        }
    }
}

@Composable
private fun AvailabilityBadge() {
    val isAvailable = Build.VERSION.SDK_INT >= 34
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isAvailable) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = if (isAvailable) "✅" else "⚠️",
                fontSize = 16.sp
            )
            Column {
                Text(
                    text = if (isAvailable) "Embedded Picker 가용" else "Embedded Picker 미지원",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isAvailable) Color(0xFF1B5E20) else Color(0xFFB71C1C)
                )
                Text(
                    text = "현재 API ${Build.VERSION.SDK_INT} | 필요: API 34 + SDK Extensions 15",
                    fontSize = 11.sp,
                    color = Color(0xFF757575)
                )
            }
        }
    }
}

@Composable
private fun PickerTab(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(88.dp)
            .clickable { onClick() },
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
                .padding(vertical = 10.dp, horizontal = 4.dp),
            textAlign = TextAlign.Center,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else Color(0xFF616161),
            lineHeight = 15.sp
        )
    }
}

@Composable
private fun BottomSheetIntegrationDemo() {
    var isSheetVisible by remember { mutableStateOf(false) }
    var isSheetExpanded by remember { mutableStateOf(false) }
    val attachments = remember { mutableStateListOf<FakeMedia>() }
    val maxSelection = 5

    val pickerExpandedState by remember(isSheetExpanded) {
        derivedStateOf { isSheetExpanded }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "BottomSheet 통합 아키텍처",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "EmbeddedPhotoPicker를 BottomSheet 안에 배치하고,\n" +
                                "SideEffect로 피커 확장 상태와 시트를 동기화합니다.",
                        fontSize = 13.sp,
                        color = Color(0xFF757575),
                        lineHeight = 18.sp
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "📱 메시지 작성 시뮬레이션",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (attachments.isNotEmpty()) {
                        Text(
                            text = "첨부 파일 (${attachments.size}/${maxSelection}) — 앱이 소유",
                            fontSize = 12.sp,
                            color = Color(0xFF9E9E9E)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            modifier = Modifier.height(90.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            userScrollEnabled = false
                        ) {
                            itemsIndexed(attachments) { _, media ->
                                Box(
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(media.color)
                                        .clickable {
                                            attachments.remove(media)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(media.label, fontSize = 10.sp, color = Color.White)
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .size(16.dp)
                                            .background(Color(0x99000000), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(10.dp)
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { isSheetVisible = true },
                            modifier = Modifier.weight(1f),
                            enabled = attachments.size < maxSelection
                        ) {
                            Text("갤러리 추가", fontSize = 12.sp)
                        }
                        Button(
                            onClick = { },
                            modifier = Modifier.weight(1f),
                            enabled = attachments.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                        ) {
                            Text("전송", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        item {
            AnimatedVisibility(
                visible = isSheetVisible,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF37474F)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    "📁 시스템 포토 피커 (SurfaceView)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Row {
                                IconButton(
                                    onClick = { isSheetExpanded = !isSheetExpanded },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isSheetExpanded)
                                            Icons.Default.KeyboardArrowDown
                                        else Icons.Default.KeyboardArrowUp,
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                }
                                IconButton(
                                    onClick = { isSheetVisible = false; isSheetExpanded = false },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Close, null, tint = Color.White)
                                }
                            }
                        }

                        Text(
                            text = "setCurrentExpanded(${pickerExpandedState}) ← SideEffect로 동기화",
                            fontSize = 10.sp,
                            color = Color(0xFF80CBC4),
                            fontFamily = FontFamily.Monospace
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val gridHeight = if (isSheetExpanded) 280.dp else 160.dp
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            modifier = Modifier.height(gridHeight),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val available = fakeMediaList.filter { it !in attachments }
                            itemsIndexed(available) { _, media ->
                                val isAtLimit = attachments.size >= maxSelection
                                Box(
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            if (isAtLimit) media.color.copy(alpha = 0.4f)
                                            else media.color
                                        )
                                        .clickable(enabled = !isAtLimit) {
                                            attachments.add(media)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(media.label, fontSize = 10.sp, color = Color.White)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "💡 탭으로 선택 → onUriPermissionGranted 콜백 시뮬레이션",
                            fontSize = 10.sp,
                            color = Color(0xFFB0BEC5)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectionSyncDemo() {
    val pickerSelections = remember { mutableStateListOf<FakeMedia>() }
    val appAttachments = remember { mutableStateListOf<FakeMedia>() }
    var syncLog by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "⚠️ 선택 동기화 함정: deselectUri",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB71C1C)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "deselectUri()는 onUriPermissionRevoked를 자동으로 트리거하지 않습니다.\n" +
                                "앱 state를 직접 업데이트해야 합니다!",
                        fontSize = 13.sp,
                        color = Color(0xFF757575),
                        lineHeight = 18.sp
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "📁 피커 소유",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0D47A1)
                        )
                        Text(
                            text = "선택 가능한 항목들",
                            fontSize = 10.sp,
                            color = Color(0xFF757575)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${pickerSelections.size}개 선택됨",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1565C0)
                        )
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "📱 앱 소유",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20)
                        )
                        Text(
                            text = "표현 + 영속화",
                            fontSize = 10.sp,
                            color = Color(0xFF757575)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${appAttachments.size}개 보유",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "미디어 선택 시뮬레이션",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(6),
                        modifier = Modifier.height(80.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        userScrollEnabled = false
                    ) {
                        itemsIndexed(fakeMediaList.take(6)) { _, media ->
                            val isSelected = media in pickerSelections
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isSelected) media.color else media.color.copy(alpha = 0.4f))
                                    .border(
                                        width = if (isSelected) 2.dp else 0.dp,
                                        color = Color.White,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .clickable {
                                        if (isSelected) {
                                            pickerSelections.remove(media)
                                        } else {
                                            pickerSelections.add(media)
                                            if (media !in appAttachments) {
                                                appAttachments.add(media)
                                                syncLog += "✅ onUriPermissionGranted: ${media.label}\n"
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        Icons.Default.Check,
                                        null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                } else {
                                    Text(media.label, fontSize = 9.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "첨부 파일 해제 (앱 UI에서)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (appAttachments.isEmpty()) {
                        Text(
                            text = "위에서 미디어를 선택하세요",
                            fontSize = 12.sp,
                            color = Color(0xFF9E9E9E),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        appAttachments.toList().forEach { media ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(media.color)
                                    )
                                    Text(media.label, fontSize = 12.sp)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    OutlinedButton(
                                        onClick = {
                                            appAttachments.remove(media)
                                            syncLog += "❌ 앱 state만 삭제 (피커 불일치!): ${media.label}\n"
                                        },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text("앱만", fontSize = 10.sp, color = Color(0xFFE53935))
                                    }
                                    Button(
                                        onClick = {
                                            pickerSelections.remove(media)
                                            appAttachments.remove(media)
                                            syncLog += "✅ deselectUri + 앱 state 모두 업데이트: ${media.label}\n"
                                        },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.height(30.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                                    ) {
                                        Text("정상", fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (syncLog.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF263238)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("이벤트 로그", fontSize = 12.sp, color = Color(0xFF80CBC4), fontWeight = FontWeight.Bold)
                            Text(
                                "지우기",
                                fontSize = 11.sp,
                                color = Color(0xFF80CBC4),
                                modifier = Modifier.clickable { syncLog = "" }
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = syncLog,
                            fontSize = 11.sp,
                            color = Color(0xFFE0E0E0),
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "💡 올바른 해제 방법",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0D47A1)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val points = listOf(
                        "deselectUri()만 호출하면 onUriPermissionRevoked가 자동 호출되지 않습니다.",
                        "해제 시 피커에 알림(deselectUri)과 앱 state 업데이트를 반드시 함께 해야 합니다.",
                        "피커는 선택 가능한 항목을 소유하고, 앱은 선택의 표현과 영속화를 소유합니다.",
                    )
                    points.forEach { point ->
                        Row(
                            modifier = Modifier.padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("•", fontSize = 12.sp, color = Color(0xFF1565C0))
                            Text(point, fontSize = 12.sp, color = Color(0xFF37474F), lineHeight = 17.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UriLifetimeDemo() {
    var grantCount by remember { mutableIntStateOf(0) }
    val maxGrants = 5000

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "URI 수명 관리",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "포토 피커 URI 접근은 기본적으로 기기 재시작 또는 앱 종료 시 만료됩니다.\n" +
                                "드래프트나 업로드 큐가 있는 경우 더 긴 접근이 필요합니다.",
                        fontSize = 13.sp,
                        color = Color(0xFF757575),
                        lineHeight = 18.sp
                    )
                }
            }
        }

        item {
            val lifetimeTypes = listOf(
                Triple("기본 (Default)", "앱 종료 또는 기기 재시작까지", Color(0xFFE3F2FD)),
                Triple("지속 가능 (Persistable)", "takePersistableUriPermission() 호출 후 영구 접근", Color(0xFFE8F5E9)),
                Triple("임시 (Temporary)", "onStop() 이후 접근 불가 (일부 사례)", Color(0xFFFFF3E0)),
            )
            lifetimeTypes.forEach { (title, desc, color) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = color),
                    shape = RoundedCornerShape(10.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF212121))
                        Text(desc, fontSize = 12.sp, color = Color(0xFF616161), lineHeight = 16.sp)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "💡 영구 접근 관리 포인트",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B5E20)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val points = listOf(
                        "드래프트·업로드 큐 등 장기 보관이 필요한 경우 takePersistableUriPermission()을 호출하세요.",
                        "contentResolver.persistedUriPermissions로 현재 영구 grant 목록을 확인할 수 있습니다.",
                        "불필요한 grant는 releasePersistableUriPermission()으로 명시적으로 해제해야 합니다.",
                    )
                    points.forEach { point ->
                        Row(
                            modifier = Modifier.padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("•", fontSize = 12.sp, color = Color(0xFF2E7D32))
                            Text(point, fontSize = 12.sp, color = Color(0xFF37474F), lineHeight = 17.sp)
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "⚠️ 5,000 미디어 그랜트 제한",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Android는 앱당 동시에 최대 5,000개의 미디어 그랜트를 허용합니다.\n" +
                                "초과 시 오래된 그랜트부터 자동 제거됩니다.",
                        fontSize = 12.sp,
                        color = Color(0xFF757575),
                        lineHeight = 17.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val progress = (grantCount.toFloat() / maxGrants).coerceIn(0f, 1f)
                    val barColor = when {
                        progress > 0.9f -> Color(0xFFE53935)
                        progress > 0.7f -> Color(0xFFFF9800)
                        else -> Color(0xFF4CAF50)
                    }
                    val animatedProgress by animateFloatAsState(
                        targetValue = progress,
                        animationSpec = tween(300),
                        label = "grantProgress"
                    )

                    Text(
                        text = "현재 그랜트: $grantCount / $maxGrants",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = barColor
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFE0E0E0))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedProgress)
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(barColor)
                        )
                    }

                    AnimatedContent(
                        targetState = progress > 0.9f,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "warningText"
                    ) { isWarning ->
                        if (isWarning) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "⚠️ 90% 초과! 오래된 그랜트가 곧 자동 제거됩니다.",
                                fontSize = 12.sp,
                                color = Color(0xFFE53935)
                            )
                        } else {
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { grantCount = (grantCount - 100).coerceAtLeast(0) },
                            modifier = Modifier.weight(1f)
                        ) { Text("-100", fontSize = 12.sp) }
                        Button(
                            onClick = { grantCount = (grantCount + 100).coerceAtMost(maxGrants) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = barColor)
                        ) { Text("+100", fontSize = 12.sp) }
                        OutlinedButton(
                            onClick = { grantCount = 0 },
                            modifier = Modifier.weight(1f)
                        ) { Text("초기화", fontSize = 12.sp) }
                    }
                }
            }
        }
    }
}

@Composable
private fun ArchitectureOverviewDemo() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Compose-First 아키텍처",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    Text(
                        text = "피커 로직을 격리하고 나머지 화면을 테스트 가능하게 유지하는 구조입니다.",
                        fontSize = 13.sp,
                        color = Color(0xFF757575),
                        lineHeight = 17.sp
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "🏗 컴포넌트 구조",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0D47A1)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    ArchFlowItem(
                        label = "BottomSheetScaffold",
                        color = Color(0xFF1976D2),
                        textColor = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Row {
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            ArchFlowItem(
                                label = "sheetContent → EmbeddedPhotoPicker (시스템 SurfaceView)",
                                color = Color(0xFF1565C0),
                                textColor = Color.White
                            )
                            Row {
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    ArchFlowItem("콜백: onUriPermissionGranted → attachments 추가", Color(0xFFBBDEFB), Color(0xFF0D47A1))
                                    ArchFlowItem("콜백: onUriPermissionRevoked → attachments 제거", Color(0xFFBBDEFB), Color(0xFF0D47A1))
                                    ArchFlowItem("콜백: onSelectionComplete → 시트 닫기 + 완료 처리", Color(0xFFBBDEFB), Color(0xFF0D47A1))
                                }
                            }
                            ArchFlowItem(
                                label = "content → 앱 UI (첨부 그리드 + 버튼)",
                                color = Color(0xFF42A5F5),
                                textColor = Color.White
                            )
                            Row {
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    ArchFlowItem("항목 제거: deselectUri + attachments 직접 업데이트 필수", Color(0xFFBBDEFB), Color(0xFF0D47A1))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    ArchFlowItem(
                        label = "SideEffect → 피커 확장 상태를 시트 상태와 동기화",
                        color = Color(0xFF0288D1),
                        textColor = Color.White
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "🧪 테스트 지원",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4A148C)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val points = listOf(
                        "androidx.photopicker:photopicker-testing 라이브러리로 테스트 환경을 구성합니다.",
                        "TestEmbeddedPhotoPickerProvider.createRule()로 실제 시스템 피커 없이 가짜 피커를 주입할 수 있습니다.",
                        "피커 로직을 격리된 컴포저블로 분리하면 나머지 화면 로직을 독립적으로 테스트할 수 있습니다.",
                    )
                    points.forEach { point ->
                        Row(
                            modifier = Modifier.padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("•", fontSize = 12.sp, color = Color(0xFF7B1FA2))
                            Text(point, fontSize = 12.sp, color = Color(0xFF37474F), lineHeight = 17.sp)
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "기존 포토 피커 vs Embedded 피커",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B5E20)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    val diffs = listOf(
                        "기존: 별도 화면으로 이동 → Embedded: 앱 화면 내 유지",
                        "기존: Activity onStop → Embedded: Activity resumed 유지",
                        "기존: 완료 후 한 번 결과 → Embedded: 선택 변경마다 콜백",
                        "기존: 모든 API 지원 → Embedded: API 34 + Ext 15 필요",
                        "기존: 권한 요청 없음 (공통) → Embedded: 권한 요청 없음 (공통)",
                    )
                    diffs.forEach { diff ->
                        Row(modifier = Modifier.padding(vertical = 2.dp)) {
                            Text("• ", fontSize = 12.sp, color = Color(0xFF2E7D32))
                            Text(
                                text = diff,
                                fontSize = 12.sp,
                                color = Color(0xFF37474F),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ArchFlowItem(label: String, color: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(color)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(label, fontSize = 11.sp, color = textColor, lineHeight = 15.sp)
    }
}

@Preview(showBackground = true)
@Composable
fun EmbeddedPickerComposeExampleUIPreview() {
    EmbeddedPickerComposeExampleUI(onBackEvent = {})
}
