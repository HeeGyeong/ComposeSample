package com.example.composesample.presentation.example.component.ui.media.shimmer

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.composesample.presentation.MainHeader

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ShimmerExampleUI(
    onBackEvent: () -> Unit
) {
    LazyColumn(modifier = Modifier.padding(horizontal = 20.dp)) {
        stickyHeader {
            MainHeader(
                title = "UI Shimmer Example",
                onBackIconClicked = onBackEvent
            )
        }

        item {
            ShimmerItem()

            HorizontalDivider(
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

            HorizontalDivider(
                color = Color.Black,
                thickness = 4.dp,
            )

            ShimmerTestItem(ratio = 0.2f)
            ShimmerTestItem(ratio = 0.6f)
            ShimmerTestItem(ratio = 1.0f)

            HorizontalDivider(
                color = Color.Black,
                thickness = 4.dp,
            )

            BackgroundShimmerItem()
        }
    }
}