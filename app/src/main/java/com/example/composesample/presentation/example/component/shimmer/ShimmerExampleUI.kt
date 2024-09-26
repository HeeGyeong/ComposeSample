package com.example.composesample.presentation.example.component.shimmer

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ShimmerExampleUI(
    onBackButtonClick: () -> Unit
) {
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
                        Icon(Icons.Filled.ArrowBack, contentDescription = "")
                    }
                }
            }
        }

        item {
            ShimmerItem()

            Divider(
                color = Color.Black,
                thickness = 4.dp,
            )

            ShimmerTestItem(
                colorList = listOf(
                    Color.Green.copy(alpha = 0.2f),
                    Color.Black.copy(alpha = 0.9f),
                    Color.Red.copy(alpha = 0.2f)
                ),
                ratio = 0.6f
            )

            Divider(
                color = Color.Black,
                thickness = 4.dp,
            )

            ShimmerTestItem(ratio = 0.2f)
            ShimmerTestItem(ratio = 0.6f)
            ShimmerTestItem(ratio = 1.0f)

            Divider(
                color = Color.Black,
                thickness = 4.dp,
            )

            BackgroundShimmerItem()
        }
    }
}