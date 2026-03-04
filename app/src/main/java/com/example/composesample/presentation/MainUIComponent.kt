package com.example.composesample.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun MainHeader(
    title: String,
    onBackIconClicked: () -> Unit,
    onLeftIconContent: @Composable () -> Unit = {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back"
        )
    },
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                maxLines = 1,
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackIconClicked) {
                onLeftIconContent()
            }
        },
        backgroundColor = Color.White,
        contentColor = Color.Black,
        elevation = 0.dp
    )
}

@Composable
fun MainScreenContent(
    onExampleCodeClick: () -> Unit,
    onLegacyCodeClick: () -> Unit,
    onWebViewClick: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Compose Sample",
            style = MaterialTheme.typography.h5,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "실험 예제와 레거시 샘플을 선택하세요.",
            style = MaterialTheme.typography.body2,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(8.dp))

        MainMenuCard(
            title = "Example Code",
            description = "최신 Compose 예제 화면",
            onClick = onExampleCodeClick
        )

        MainMenuCard(
            title = "Legacy Code",
            description = "기존 레거시 예제 화면",
            onClick = onLegacyCodeClick
        )

        MainMenuCard(
            title = "Open Blog",
            description = "예제 설명 글 페이지 열기",
            onClick = { onWebViewClick("https://johyun-dev.tistory.com") }
        )
    }
}

@Composable
private fun MainMenuCard(
    title: String,
    description: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = 0.dp,
    ) {
        Box(
            modifier = Modifier
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.subtitle1,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.body2,
                        color = Color.Gray,
                        textAlign = TextAlign.Start
                    )
                }
            }
        }
    }
}
