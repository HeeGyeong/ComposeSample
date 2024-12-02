package com.example.composesample.presentation.example.component.coordinator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.composesample.presentation.MainHeader
import com.example.composesample.presentation.example.BlogExampleViewModel

@Composable
fun CoordinatorExampleUI(
    onBackEvent: () -> Unit,
) {
    val blogExampleViewModel = viewModel<BlogExampleViewModel>()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Coordinator Example",
            onBackIconClicked = onBackEvent
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = {
                    blogExampleViewModel.changeToActivity(
                        context = context,
                        fromActivity = "CoordinatorModuleUI"
                    )
                }
            ) {
                Text(text = "이동하기")
            }
        }
    }
} 