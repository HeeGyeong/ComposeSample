package com.example.composesample.presentation.example.component.compose17

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.composesample.presentation.getTextStyle

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FocusRestorerExampleUI(onBackButtonClick: () -> Unit) {
    val focusManager = LocalFocusManager.current
    
    LazyColumn(modifier = Modifier.padding(horizontal = 20.dp)) {
        stickyHeader {
            Box(
                modifier = Modifier
                    .background(color = Color.White)
                    .padding(top = 10.dp, bottom = 10.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    IconButton(
                        onClick = {
                            onBackButtonClick.invoke()
                        }
                    ) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                    
                    Text(
                        text = "Focus Restorer",
                        modifier = Modifier.align(Alignment.CenterVertically),
                        style = getTextStyle(20)
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
            
            Text(
                text = "✨ 개선된 포커스 복원 기능",
                style = getTextStyle(18),
                color = Color.Blue
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            // 기본 포커스 복원 예제
            BasicFocusRestorerExample()

            Spacer(modifier = Modifier.height(24.dp))

            // LazyList 포커스 복원 예제
            LazyListFocusRestorerExample()

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun BasicFocusRestorerExample() {
    var showDialog by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🎯 기본 포커스 복원",
                style = getTextStyle(16),
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "다이얼로그나 다른 화면으로 이동 후 돌아왔을 때 이전 포커스를 복원합니다.",
                style = getTextStyle(14),
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 포커스 복원 적용된 입력 필드들
            Column(
                modifier = Modifier.focusRestorer()
            ) {
                TextField(
                    value = "",
                    onValueChange = { },
                    label = { Text("첫 번째 필드") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                TextField(
                    value = "",
                    onValueChange = { },
                    label = { Text("두 번째 필드") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                TextField(
                    value = "",
                    onValueChange = { },
                    label = { Text("세 번째 필드") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { showDialog = true }
                ) {
                    Text("다이얼로그 열기", style = getTextStyle(12))
                }
                
                Button(
                    onClick = { focusRequester.requestFocus() }
                ) {
                    Text("첫 번째 필드 포커스", style = getTextStyle(12))
                }
            }
            
            if (showDialog) {
                // 간단한 다이얼로그 시뮬레이션
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "다이얼로그",
                            color = Color.White,
                            style = getTextStyle(16)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Button(
                            onClick = { showDialog = false }
                        ) {
                            Text("닫기", style = getTextStyle(12))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun LazyListFocusRestorerExample() {
    val items = remember { (1..10).map { "아이템 $it" }.toMutableList() }
    var removedItem by remember { mutableStateOf<String?>(null) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📋 LazyList 포커스 복원",
                style = getTextStyle(16),
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "리스트에서 아이템 제거 후 적절한 아이템으로 포커스가 복원됩니다.",
                style = getTextStyle(14),
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 포커스 복원이 적용된 LazyList
            LazyRow(
                modifier = Modifier.focusRestorer(),
                contentPadding = PaddingValues(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(items, key = { _, item -> item }) { index, item ->
                    Card(
                        modifier = Modifier
                            .padding(4.dp)
                            .border(
                                1.dp,
                                Color.Blue.copy(alpha = 0.3f),
                                RoundedCornerShape(8.dp)
                            ),
                        elevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = item,
                                style = getTextStyle(12),
                                textAlign = TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Button(
                                onClick = {
                                    removedItem = item
                                    items.removeAt(index)
                                }
                            ) {
                                Text("제거", style = getTextStyle(10))
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            removedItem?.let { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "'$item' 제거됨",
                        style = getTextStyle(12),
                        color = Color.Red
                    )
                    
                    Button(
                        onClick = {
                            items.add(item)
                            removedItem = null
                        }
                    ) {
                        Text("복원", style = getTextStyle(10))
                    }
                }
            }
        }
    }
} 