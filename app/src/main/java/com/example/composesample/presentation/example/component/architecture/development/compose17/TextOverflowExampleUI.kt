package com.example.composesample.presentation.example.component.architecture.development.compose17

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.getTextStyle

/**
 * Text Overflow Example
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TextOverflowExampleUI(onBackButtonClick: () -> Unit) {
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                    
                    Text(
                        text = "Text Overflow 새 기능",
                        modifier = Modifier.align(Alignment.CenterVertically),
                        style = getTextStyle(20)
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
            
            Text(
                text = "✨ Compose 1.7.6 새로운 TextOverflow 옵션",
                style = getTextStyle(18),
                color = Color.Blue
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            TextOverflowCard(
                title = "기본 TextOverflow.Ellipsis",
                description = "기존 방식: 텍스트 끝에 ... 표시",
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            TextOverflowCard(
                title = "TextOverflow.StartEllipsis (Compose 1.8+)",
                description = "텍스트 시작 부분에 ... 표시하는 새로운 기능",
                overflow = TextOverflow.StartEllipsis,
                isSimulation = false
            )

            Spacer(modifier = Modifier.height(12.dp))

            TextOverflowCard(
                title = "TextOverflow.MiddleEllipsis (Compose 1.8+)",
                description = "텍스트 중간 부분에 ... 표시하는 새로운 기능",
                overflow = TextOverflow.MiddleEllipsis,
                isSimulation = false
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "📱 실제 사용 사례",
                style = getTextStyle(18),
                color = Color.Blue
            )

            Spacer(modifier = Modifier.height(16.dp))
            UseCaseExample(
                title = "파일 경로 표시",
                example = "/Users/developer/Projects/ComposeSample/app/src/main/java/com/example/very/long/package/name/Activity.kt",
                overflow = TextOverflow.StartEllipsis,
                description = "파일 경로에서 파일명이 중요할 때"
            )

            Spacer(modifier = Modifier.height(12.dp))

            UseCaseExample(
                title = "URL 표시",
                example = "https://developer.android.com/jetpack/compose/text/overflow/documentation/very/long/url/path",
                overflow = TextOverflow.MiddleEllipsis,
                description = "URL에서 도메인과 끝 부분이 중요할 때"
            )

            Spacer(modifier = Modifier.height(12.dp))

            UseCaseExample(
                title = "이메일 주소",
                example = "very.long.email.address.example@company.co.kr",
                overflow = TextOverflow.MiddleEllipsis,
                description = "이메일에서 아이디와 도메인이 중요할 때"
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun TextOverflowCard(
    title: String,
    description: String,
    overflow: TextOverflow,
    isSimulation: Boolean = false,
    simulationType: String? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = getTextStyle(16),
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = description,
                style = getTextStyle(14),
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 예제 텍스트 - 넓이 제한
            Box(
                modifier = Modifier
                    .width(200.dp)
                    .background(Color.LightGray.copy(alpha = 0.3f))
                    .padding(8.dp)
            ) {
                val displayText = when {
                    isSimulation && simulationType == "Start" -> 
                        "...텍스트는 제한된 공간에서 어떻게 표시되는지 보여줍니다."
                    isSimulation && simulationType == "Middle" -> 
                        "이것은 매우 긴...표시되는지 보여줍니다."
                    else -> 
                        "이것은 매우 긴 텍스트입니다. 이 텍스트는 제한된 공간에서 어떻게 표시되는지 보여줍니다."
                }
                
                Text(
                    text = displayText,
                    maxLines = 1,
                    overflow = if (isSimulation) TextOverflow.Clip else overflow,
                    style = getTextStyle(14)
                )
                
                // 시뮬레이션 표시
                if (isSimulation) {
                    Text(
                        text = "📝 시뮬레이션",
                        style = getTextStyle(8),
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.BottomEnd)
                    )
                }
            }
        }
    }
}

@Composable
private fun UseCaseExample(
    title: String,
    example: String,
    overflow: TextOverflow,
    description: String,
    isSimulation: Boolean = false,
    simulatedText: String? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFF8F9FA)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = getTextStyle(16),
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 실제 예제 - 넓이 제한
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(12.dp)
            ) {
                Column {
                    if (isSimulation && simulatedText != null) {
                        // 시뮬레이션된 결과 표시
                        Text(
                            text = "💡 예상 결과:",
                            style = getTextStyle(10),
                            color = Color.Gray
                        )
                        Text(
                            text = simulatedText,
                            maxLines = 1,
                            overflow = TextOverflow.Clip,
                            style = getTextStyle(14),
                            color = Color.Blue
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "📋 현재 결과:",
                            style = getTextStyle(10),
                            color = Color.Gray
                        )
                    }
                    
                    // 현재 버전의 결과
                    Text(
                        text = example,
                        maxLines = 1,
                        overflow = overflow,
                        style = getTextStyle(14),
                        color = if (isSimulation) Color.Gray else Color.Blue
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = description,
                style = getTextStyle(12),
                color = Color.Gray
            )
        }
    }
} 