package com.example.composesample.presentation.example.component.mvi

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.composesample.presentation.MainHeader
import org.koin.androidx.compose.koinViewModel

@Composable
fun MVIExampleUI(onBackEvent: () -> Unit) {
    val viewModel: MVIExampleViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        MainHeader(title = "MVI Example", onBackIconClicked = onBackEvent)

        Spacer(modifier = Modifier.height(16.dp))

        state.items.forEachIndexed { index, item ->
            Text(text = "Current State: $item")
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(onClick = { viewModel.onEvent(MVIExampleEvent.ButtonClicked1) }) {
            Text("Change Item 1")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { viewModel.onEvent(MVIExampleEvent.ButtonClicked2) }) {
            Text("Change Item 2")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { viewModel.onEvent(MVIExampleEvent.ButtonClicked3) }) {
            Text("Change Item 3")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { viewModel.onEvent(MVIExampleEvent.ButtonClicked4) }) {
            Text("Change Item 4")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { viewModel.onEvent(MVIExampleEvent.FetchData) }) {
            Text("Fetch Data")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "API Data: ${state.apiData}")
    }
} 