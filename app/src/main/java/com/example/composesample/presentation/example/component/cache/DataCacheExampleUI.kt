package com.example.composesample.presentation.example.component.cache

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.example.data.db.UserData
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DataCacheExampleUI(
    onBackButtonClick: () -> Unit
) {
    val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current)
    val dataCacheViewModel =
        koinViewModel<DataCacheViewModel>(owner = viewModelStoreOwner)
    val textState = remember { mutableStateOf("") }
    // 저장 된 데이터를 그대로 가져옴. textState의 default 값은 빈 문자열이므로, 전체를 가져온다.
    val userList =
        dataCacheViewModel.searchUserName(textState.value).collectAsState(initial = emptyList())

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White)
    ) {
        stickyHeader {
            Box(
                modifier = Modifier
                    .background(color = Color.White)
                    .padding(top = 10.dp, bottom = 10.dp)
            ) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        IconButton(
                            onClick = {
                                onBackButtonClick.invoke()
                            }
                        ) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(onClick = {
                                dataCacheViewModel.insertUserData(
                                    UserData(
                                        id = null, // PrimaryKey 는 데이터가 없으면 오름차순으로 알아서 들어간다.
                                        userName = generateRandomString()
                                    )
                                )
                            }) { Text(text = "Add User") }

                            Button(onClick = { dataCacheViewModel.allDataDelete() }) {
                                Text(text = "All Clear")
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = if (textState.value.trim().isEmpty()) {
                                "UserData"
                            } else {
                                "UserData start with (${textState.value.trim()})"
                            } + " ${userList.value.size}",
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        TextField(
                            value = textState.value,
                            onValueChange = { textState.value = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(text = "Search User Name") }
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
        itemsIndexed(userList.value) { _, item ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color(0xFFDCE2C9)),
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF8DB600)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${item.id}",
                            style = MaterialTheme.typography.h6
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = item.userName.take(12),
                        style = MaterialTheme.typography.h6
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(onClick = {
                        val updatedItem = UserData(
                            id = item.id,
                            userName = generateRandomString()
                        )
                        dataCacheViewModel.updateUserData(updatedItem)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = ""
                        )
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    IconButton(onClick = {
                        dataCacheViewModel.deleteUserData(item)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "",
                            tint = Color(0xFFD3212D)
                        )
                    }
                }
            }
        }
    }
}

fun generateRandomString(): String {
    val allowedChars = ('A'..'Z') + ('a'..'z')
    return (1..7)
        .map { allowedChars.random() } // 랜덤 문자열 생성
        .joinToString("") // 문자열로 변환
}