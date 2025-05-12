package com.example.composesample.presentation.example.component.type

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.example.composesample.presentation.example.component.paging.PagingViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TypeExampleUI(onBackButtonClick: () -> Unit) {
    val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current)
    val viewModel = koinViewModel<TypeExampleViewModel>(owner = viewModelStoreOwner)
    
    // ViewModel에서 데이터 수집
    val a1 = viewModel.a1.collectAsState().value
    val a2 = viewModel.a2.collectAsState().value
    val b1 = viewModel.b1.collectAsState().value
    val b2 = viewModel.b2.collectAsState().value
    val c1 = viewModel.c1.collectAsState().value
    val c2 = viewModel.c2.collectAsState().value
    val d1 = viewModel.d1.collectAsState().value
    val d2 = viewModel.d2.collectAsState().value
    val e1 = viewModel.e1.collectAsState().value
    val e2 = viewModel.e2.collectAsState().value
    
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
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .padding(start = 8.dp),
                        text = "Type 예제",
                        fontSize = 18.sp
                    )
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(20.dp))
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                backgroundColor = Color.White,
                elevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // 첫 번째 행
                    TypeRow(label1 = a1, label2 = a2)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 두 번째 행
                    TypeRow(label1 = b1, label2 = b2)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 세 번째 행
                    TypeRow(label1 = c1, label2 = c2)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 네 번째 행
                    TypeRow(label1 = d1, label2 = d2)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 다섯 번째 행
                    TypeRow(label1 = e1, label2 = e2)
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun TypeRow(label1: String, label2: String) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        // 첫 번째 열
        Text(
            modifier = Modifier
                .weight(1f)
                .background(Color.LightGray.copy(alpha = 0.3f))
                .padding(vertical = 8.dp, horizontal = 16.dp),
            text = label1,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // 두 번째 열
        Text(
            modifier = Modifier
                .weight(1f)
                .background(Color.LightGray.copy(alpha = 0.3f))
                .padding(vertical = 8.dp, horizontal = 16.dp),
            text = label2,
            textAlign = TextAlign.Center
        )
    }
} 