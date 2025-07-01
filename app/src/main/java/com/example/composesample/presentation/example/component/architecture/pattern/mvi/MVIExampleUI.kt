package com.example.composesample.presentation.example.component.architecture.pattern.mvi

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.composesample.presentation.MainHeader
import org.koin.androidx.compose.koinViewModel

@Composable
fun MVIExampleUI(onBackEvent: () -> Unit) {
    val viewModel: MVIExampleViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    // SideEffect를 구독
    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is MVISideEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }

                is MVISideEffect.ShowError -> {
                    Toast.makeText(context, effect.error, Toast.LENGTH_LONG).show()
                }

                MVISideEffect.NavigateBack -> onBackEvent()
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        MainHeader(title = "MVI Example", onBackIconClicked = {
            viewModel.onEvent(MVIExampleEvent.NavigateBack)
        })

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