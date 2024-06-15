package com.example.composesample.movie

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.domain.model.MovieEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@Composable
fun MovieScreen(
    viewModel: MovieViewModel,
    coroutineScope: CoroutineScope = rememberCoroutineScope()
) {
    var apiText by remember { mutableStateOf("null") }
    var flowApiTest by remember { mutableStateOf("null") }

    val data by viewModel.data.observeAsState()
    val flowData by viewModel.flowData.observeAsState()
    val flowData2 by viewModel.flowData2.observeAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        TextField(
            value = apiText,
            onValueChange = { change ->
                apiText = change
                viewModel.apiFunction(apiText)
            },
            label = { Text("Search Movie") }
        )

        DataListSizeText(data)

        TextField(
            value = flowApiTest,
            onValueChange = { change ->
                flowApiTest = change

                coroutineScope.launch {
                    viewModel.apiFlowFunction(flowApiTest)
                }
            },
            label = { Text("Search Movie Flow") }
        )

        DataListSizeText(flowData)
        DataListSizeText(flowData2)

        Spacer(modifier = Modifier.height(50.dp))
    }
}

@Composable
fun DataListSizeText(data: List<MovieEntity>?) {
    val rememberUpdatedData by rememberUpdatedState(data)

    Text("insertData : ${rememberUpdatedData?.size ?: "no data"}")
}