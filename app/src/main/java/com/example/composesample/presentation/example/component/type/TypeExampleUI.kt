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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TypeExampleUI(onBackButtonClick: () -> Unit) {
    val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current)
    val viewModel = koinViewModel<TypeExampleViewModel>(owner = viewModelStoreOwner)

    val intListMutable = viewModel.intListMutable
    val intListImmutable = viewModel.intListImmutable
    val floatListMutable = viewModel.floatListMutable
    val floatListImmutable = viewModel.floatListImmutable
    val stringBuilderResult = viewModel.stringBuilderResult
    val buildStringResult = viewModel.buildStringResult
    val numberSet = viewModel.numberSet
    val emptySet = viewModel.emptySet
    val stringMap = viewModel.stringMap
    val emptyMap = viewModel.emptyMap

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
                    Text(
                        text = "Int 리스트",
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    TypeRow(label1 = "$intListMutable", label2 = "$intListImmutable")

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Float 리스트",
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    TypeRow(label1 = "$floatListMutable", label2 = "$floatListImmutable")

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "문자열 빌더",
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    TypeRow(label1 = stringBuilderResult, label2 = buildStringResult)

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Set 컬렉션",
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    TypeRow(label1 = "$numberSet", label2 = "$emptySet")

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Map 컬렉션",
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    TypeRow(label1 = "$stringMap", label2 = "$emptyMap")
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
        Text(
            modifier = Modifier
                .weight(1f)
                .background(Color.LightGray.copy(alpha = 0.3f))
                .padding(vertical = 8.dp, horizontal = 16.dp),
            text = label1,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.width(16.dp))

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