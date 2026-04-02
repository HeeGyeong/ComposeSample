package com.example.composesample.presentation.example.component.ui.text

import android.content.ClipData
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.content.ReceiveContentListener
import androidx.compose.foundation.content.TransferableContent
import androidx.compose.foundation.content.consume
import androidx.compose.foundation.content.contentReceiver
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader

@Composable
fun RichContentTextInputExampleUI(onBackEvent: () -> Unit) {
    var receivedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var lastSource by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(title = "Rich Content in Text Input", onBackIconClicked = onBackEvent)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { RichContentConceptCard() }

            item {
                ReceiveContentTextFieldCard(
                    onContentReceived = { uri, source ->
                        receivedImages = receivedImages + uri
                        lastSource = source
                    }
                )
            }

            if (receivedImages.isNotEmpty()) {
                item {
                    ReceivedContentCard(
                        images = receivedImages,
                        lastSource = lastSource,
                        onClear = {
                            receivedImages = emptyList()
                            lastSource = null
                        }
                    )
                }
            }

            item { ContentSourceInfoCard() }
            item { ConsumePatternCard() }
            item { ApiRequirementCard() }
        }
    }
}

@Composable
private fun RichContentConceptCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "receiveContent 개요",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "TextField에 텍스트 외의 리치 콘텐츠(이미지, 동영상, 파일 등)를 수신할 수 있는 Modifier입니다. " +
                        "키보드(IME), 클립보드, 드래그&드롭 세 가지 출처를 지원합니다.",
                fontSize = 14.sp,
                color = Color(0xFF424242),
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            val comparisons = listOf(
                Triple("일반 TextField", "텍스트 입력만 지원", "기본"),
                Triple("receiveContent 적용", "텍스트 + 이미지/파일 수신", "Foundation 1.7+")
            )
            comparisons.forEach { (name, support, req) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = name, fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                    Text(text = support, fontSize = 11.sp, color = Color(0xFF1976D2), modifier = Modifier.weight(1.2f))
                    Text(text = req, fontSize = 10.sp, color = Color(0xFF757575))
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ReceiveContentTextFieldCard(
    onContentReceived: (Uri, String) -> Unit
) {
    var text by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "receiveContent 적용 TextField",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF388E3C)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "GIF 키보드에서 이미지를 삽입하거나, 이미지를 복사 후 붙여넣기 해보세요.",
                fontSize = 12.sp,
                color = Color(0xFF757575)
            )
            Spacer(modifier = Modifier.height(12.dp))

            // onContentReceived가 변경되어도 listener가 최신 콜백을 참조하도록 rememberUpdatedState 사용
            val latestOnContentReceived by rememberUpdatedState(onContentReceived)

            // ReceiveContentListener: recomposition 시 재생성 방지를 위해 remember로 캐싱
            val receiveContentListener = remember {
                object : ReceiveContentListener {
                    override fun onReceive(transferableContent: TransferableContent): TransferableContent? {
                        val sourceLabel = when {
                            transferableContent.source == TransferableContent.Source.Keyboard -> "키보드(IME)"
                            transferableContent.source == TransferableContent.Source.Clipboard -> "클립보드"
                            transferableContent.source == TransferableContent.Source.DragAndDrop -> "드래그&드롭"
                            else -> "알 수 없음"
                        }
                        // consume: predicate가 true를 반환하는 아이템만 소비
                        // false 반환 아이템은 TextField의 기본 텍스트 처리로 위임
                        return transferableContent.consume { item: ClipData.Item ->
                            val uri = item.uri
                            if (uri != null) {
                                latestOnContentReceived(uri, sourceLabel)
                                true // URI가 있는 아이템(이미지 등)은 소비
                            } else {
                                false // 텍스트는 소비하지 않아 TextField로 전달
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .border(1.dp, Color(0xFF388E3C), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                BasicTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        // contentReceiver: TextField에 리치 콘텐츠(이미지, 파일 등) 수신 기능 추가
                        .contentReceiver(receiveContentListener),
                    textStyle = TextStyle(fontSize = 14.sp, color = Color(0xFF212121)),
                    decorationBox = { innerTextField ->
                        if (text.isEmpty()) {
                            Text(
                                text = "텍스트 입력 또는 이미지 붙여넣기...",
                                fontSize = 14.sp,
                                color = Color(0xFFBDBDBD)
                            )
                        }
                        innerTextField()
                    }
                )
            }
        }
    }
}

@Composable
private fun ReceivedContentCard(
    images: List<Uri>,
    lastSource: String?,
    onClear: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp),
        backgroundColor = Color(0xFFE8F5E9)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "수신된 콘텐츠 (${images.size}개)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
                Button(
                    onClick = onClear,
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFE53935))
                ) {
                    Text(text = "초기화", color = Color.White, fontSize = 12.sp)
                }
            }

            lastSource?.let { source ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "마지막 출처: $source",
                    fontSize = 12.sp,
                    color = Color(0xFF388E3C)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(images) { uri ->
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(Color(0xFFB2DFDB), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFF4CAF50), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🖼️\n${uri.lastPathSegment?.takeLast(10) ?: "이미지"}",
                            fontSize = 10.sp,
                            color = Color(0xFF2E7D32),
                            textAlign = TextAlign.Center,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContentSourceInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "TransferableContent.Source",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF7B1FA2)
            )
            Spacer(modifier = Modifier.height(8.dp))

            val sources = listOf(
                Triple("Keyboard", "GIF 키보드 또는 IME 이미지 삽입", "⌨️"),
                Triple("Clipboard", "복사 후 붙여넣기 (Ctrl+V)", "📋"),
                Triple("DragAndDrop", "파일/이미지를 드래그 앤 드롭", "🖱️")
            )
            sources.forEach { (source, desc, emoji) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(Color(0xFFF3E5F5), RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = emoji, fontSize = 20.sp)
                    Spacer(modifier = Modifier.size(8.dp))
                    Column {
                        Text(
                            text = "Source.${source}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7B1FA2)
                        )
                        Text(text = desc, fontSize = 11.sp, color = Color(0xFF757575))
                    }
                }
            }
        }
    }
}

@Composable
private fun ConsumePatternCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp),
        backgroundColor = Color(0xFFF3F7FF)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "consume { } 패턴",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1565C0)
            )
            Spacer(modifier = Modifier.height(8.dp))

            val patterns = listOf(
                "true 반환 → 해당 아이템 소비 (TextField에 전달 안 됨)",
                "false 반환 → 소비하지 않음 → TextField 기본 처리로 위임",
                "null 반환 시 → 모든 콘텐츠 소비 완료",
                "item.uri != null → 이미지/미디어 아이템",
                "item.text != null → 텍스트 아이템"
            )
            patterns.forEach { pattern ->
                Row(
                    modifier = Modifier.padding(vertical = 2.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(text = "▸  ", color = Color(0xFF1565C0), fontSize = 12.sp)
                    Text(text = pattern, fontSize = 12.sp, color = Color(0xFF424242))
                }
            }
        }
    }
}

@Composable
private fun ApiRequirementCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp),
        backgroundColor = Color(0xFFFFF3E0)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "API 요구사항",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE65100)
            )
            Spacer(modifier = Modifier.height(8.dp))

            val notes = listOf(
                "Compose Foundation 1.7.0+ 에서 receiveContent 지원",
                "API 33+ (Android 13)에서 키보드 이미지 삽입 지원",
                "하위 버전에서는 클립보드 붙여넣기로만 수신 가능",
                "hintMimeTypes: IME에 수신 가능 타입을 선언하는 힌트",
                "@ExperimentalFoundationApi 옵트인 필요"
            )
            notes.forEach { note ->
                Row(
                    modifier = Modifier.padding(vertical = 2.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(text = "•  ", color = Color(0xFFE65100))
                    Text(text = note, fontSize = 12.sp, color = Color(0xFF424242))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RichContentTextInputPreview() {
    RichContentTextInputExampleUI(onBackEvent = {})
}
