package com.example.composesample.presentation.example.component.mvi

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.composesample.presentation.MainHeader

@Composable
fun MVIExampleUI(onBackEvent: () -> Unit) {
    val viewModel: MVIExampleViewModel = viewModel()
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
    }
} 