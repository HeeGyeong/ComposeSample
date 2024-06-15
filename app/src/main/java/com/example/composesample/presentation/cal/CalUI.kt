package com.example.composesample.presentation.cal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AddCounter(viewModel: CalViewModel) {
//    val viewModel = viewModel<CalViewModel>()
    val counter = viewModel.counter.collectAsState()

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "${counter.value}", fontSize = 40.sp)
        Button(onClick = { viewModel.addCounter() }) {
            Text(text = "Add Counter")
        }
        Button(onClick = { viewModel.minusCounter() }) {
            Text(text = "Minus Counter")
        }
        Button(onClick = { viewModel.multiCounter() }) {
            Text(text = "*2 Counter")
        }
        Button(onClick = { viewModel.divCounter() }) {
            Text(text = "/2 Counter")
        }
    }
}