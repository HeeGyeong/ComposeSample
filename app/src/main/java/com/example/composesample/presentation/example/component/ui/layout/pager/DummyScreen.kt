package com.example.composesample.presentation.example.component.ui.layout.pager

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

data class DisplayItem(
    val display: @Composable () -> Unit
)

@Composable
fun TextPager(text: String) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.align(Alignment.Center)
        ) {
            Text(text = text)
        }
    }
}

@Composable
fun FirstScreen() {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.align(Alignment.Center)
        ) {
            Text(text = "First Page")
        }
    }
}

@Composable
fun SecondScreen() {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.align(Alignment.Center)
        ) {
            Text(text = "Second Page")
            Text(text = "Dummy Data")
        }
    }
}

@Composable
fun ThirdScreen() {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.align(Alignment.Center)
        ) {
            Text(text = "Third Page")
        }
    }
}

@Composable
fun LastScreen() {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.align(Alignment.Center)
        ) {
            Text(text = "Last Page")
            Text(text = "Can't swipe to next page")
        }
    }
}