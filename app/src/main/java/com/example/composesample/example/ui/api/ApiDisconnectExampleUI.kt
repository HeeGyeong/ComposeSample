package com.example.composesample.example.ui.api

import android.util.Log
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.example.composesample.example.util.NetworkUtil
import org.koin.androidx.compose.get
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ApiDisconnectExampleUI(
    onBackButtonClick: () -> Unit
) {
    val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current)
    val apiExampleViewModel =
        koinViewModel<ApiExampleViewModel>(owner = viewModelStoreOwner)
    val posts by apiExampleViewModel.posts.observeAsState(initial = emptyList())

    val networkUtil: NetworkUtil = get()
    val isConnectNetwork = remember { mutableStateOf(networkUtil.isNetworkAvailable()) }

    Log.d("NetworkLog", "isConnectNetwork : ${isConnectNetwork.value}")

    // 네트워크 연결 정보를 옵저빙한다.
    apiExampleViewModel.getNetworkStatus()
        .observe(LocalLifecycleOwner.current) { isConnected ->
            if (isConnected) {
                Log.d("NetworkLog", "isConnected")
                isConnectNetwork.value = true
            } else {
                Log.d("NetworkLog", "Network is lost")
                isConnectNetwork.value = false
            }
        }

//    LaunchedEffect(key1 = Unit, block = {
//        apiExampleViewModel.fetchPosts()
//    })

    LaunchedEffect(key1 = isConnectNetwork.value, block = {
        if (isConnectNetwork.value) {
            apiExampleViewModel.fetchPosts()
        }
    })

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

                    if (!isConnectNetwork.value) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                                .background(color = Color.Black),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "The network is disconnected.\nPlease check the network status.",
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        itemsIndexed(posts) { index, item ->
            Text(text = "[$index] : ${item.title}")
        }
    }
}