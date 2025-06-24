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

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
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

@Composable
private fun BasicFocusRestorerExample() {
    var showDialog by remember { mutableStateOf(false) }
    var text1 by remember { mutableStateOf("") }
    var text2 by remember { mutableStateOf("") }
    var text3 by remember { mutableStateOf("") }
    val focusRequester1 = remember { FocusRequester() }
    
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
                text = "다이얼로그 열었다 닫으면 이전 포커스된 필드로 복원됩니다.",
                style = getTextStyle(14),
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 포커스 복원 적용된 입력 필드들
            Column(
                modifier = Modifier.focusRestorer()
            ) {
                TextField(
                    value = text1,
                    onValueChange = { text1 = it },
                    label = { Text("첫 번째 필드") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester1)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                TextField(
                    value = text2,
                    onValueChange = { text2 = it },
                    label = { Text("두 번째 필드") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                TextField(
                    value = text3,
                    onValueChange = { text3 = it },
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
                    onClick = { focusRequester1.requestFocus() }
                ) {
                    Text("첫 번째 필드 포커스", style = getTextStyle(12))
                }
            }
            
            if (showDialog) {
                androidx.compose.material.AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("포커스 복원 테스트") },
                    text = { Text("이 다이얼로그를 닫으면 이전에 포커스된 필드로 포커스가 복원됩니다.") },
                    confirmButton = {
                        Button(onClick = { showDialog = false }) {
                            Text("확인")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun LazyListFocusRestorerExample() {
    var items by remember { mutableStateOf((1..6).map { "아이템 $it" }) }
    var removedItem by remember { mutableStateOf<String?>(null) }
    var focusedIndex by remember { mutableStateOf(-1) }
    
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
                text = "아이템 제거 시 가장 가까운 아이템으로 포커스가 복원됩니다.",
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
                                if (focusedIndex == index) 2.dp else 1.dp,
                                if (focusedIndex == index) Color.Red else Color.Blue.copy(alpha = 0.3f),
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
                                    focusedIndex = index
                                    removedItem = item
                                    items = items.filterIndexed { i, _ -> i != index }
                                }
                            ) {
                                Text("제거", style = getTextStyle(10))
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (items.isEmpty()) {
                Text(
                    text = "모든 아이템이 제거되었습니다",
                    style = getTextStyle(14),
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = {
                        items = (1..6).map { "아이템 $it" }
                        focusedIndex = -1
                        removedItem = null
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("전체 복원", style = getTextStyle(12))
                }
            }
            
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
                            items = items + item
                            removedItem = null
                            focusedIndex = -1
                        }
                    ) {
                        Text("복원", style = getTextStyle(10))
                    }
                }
            }
        }
    }
} 