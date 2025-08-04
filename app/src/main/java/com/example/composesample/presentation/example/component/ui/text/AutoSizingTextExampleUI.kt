package com.example.composesample.presentation.example.component.ui.text

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader


@Composable
fun AutoSizingTextExampleUI(
    onBackEvent: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Auto-sizing Text Example",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { AutoSizeOverviewCard() }
            item { BasicComparisonCard() }
            item { AutoSizeWithMaxSizeCard() }
            item { ConstrainedBoxExampleCard() }
            item { MinFontSizeWithEllipsisCard() }
            item { SoftWrapExampleCard() }
            item { MaxLinesExampleCard() }
            item { OnTextLayoutCard() }
            item { PracticalExamplesCard() }
        }
    }
}

@Composable
private fun AutoSizeOverviewCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Auto-sizing 개요",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )

            Spacer(modifier = Modifier.height(8.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .border(1.dp, Color(0xFF1976D2), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                BasicText(
                    text = "BasicText with autoSize feature",
                    style = TextStyle(fontSize = 24.sp),
                    autoSize = TextAutoSize.StepBased(
                        maxFontSize = 20.sp
                    ),
                    color = { Color(0xFF1976D2) }
                )
            }
        }
    }
}



@Composable
private fun BasicComparisonCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFF3E5F5),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Text vs BasicText 비교",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF7B1FA2)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "일반 Text:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Normal Text",
                    color = Color.Black,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "BasicText (AutoSize):",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .border(1.dp, Color(0xFF7B1FA2), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                BasicText(
                    text = "Auto Resizing Text that adjusts to container size",
                    style = TextStyle(fontSize = 24.sp),
                    autoSize = TextAutoSize.StepBased(
                        maxFontSize = 20.sp
                    ),
                    color = { Color(0xFF7B1FA2) }
                )
            }
        }
    }
}

@Composable
private fun AutoSizeWithMaxSizeCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFE8F5E8),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "최대 크기 제한 (maxFontSize)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF388E3C)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .border(1.dp, Color(0xFF388E3C), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                BasicText(
                    text = "Auto Resizing Text but with max size limit of 16sp",
                    style = TextStyle(fontSize = 32.sp),
                    autoSize = TextAutoSize.StepBased(
                        maxFontSize = 16.sp
                    ),
                    color = { Color(0xFF388E3C) }
                )
            }
        }
    }
}

@Composable
private fun ConstrainedBoxExampleCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFFFF3E0),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "제약이 있는 컨테이너",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE65100)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 짧은 텍스트
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .border(1.dp, Color(0xFFE65100), RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    BasicText(
                        text = "Short Text",
                        style = TextStyle(fontSize = 32.sp),
                        autoSize = TextAutoSize.StepBased(
                            maxFontSize = 24.sp
                        ),
                        color = { Color(0xFFE65100) }
                    )
                }

                // 긴 텍스트
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .border(1.dp, Color(0xFFE65100), RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    BasicText(
                        text = "Much longer text that needs to auto-resize to fit in the box properly",
                        style = TextStyle(fontSize = 32.sp),
                        autoSize = TextAutoSize.StepBased(
                            maxFontSize = 14.sp
                        ),
                        color = { Color(0xFFE65100) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MinFontSizeWithEllipsisCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFFFEBEE),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "최소 크기 제한과 Ellipsis",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD32F2F)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .border(1.dp, Color(0xFFD32F2F), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                BasicText(
                    text = "Auto Resizing Text in box but in a smaller box and longer with smallest font size limit that shows ellipsis when too small",
                    style = TextStyle(fontSize = 32.sp),
                    autoSize = TextAutoSize.StepBased(
                        minFontSize = 10.sp
                    ),
                    color = { Color(0xFFD32F2F) },
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun SoftWrapExampleCard() {
    var softWrapEnabled by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFE3F2FD),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "SoftWrap 제어",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "softWrap 활성화:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                Switch(
                    checked = softWrapEnabled,
                    onCheckedChange = { softWrapEnabled = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF1976D2)
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .border(1.dp, Color(0xFF1976D2), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                BasicText(
                    text = "Auto Resizing Text in box but much much longer with soft wrap control - toggle switch to see difference",
                    style = TextStyle(fontSize = 24.sp),
                    autoSize = TextAutoSize.StepBased(
                        maxFontSize = 16.sp
                    ),
                    color = { Color(0xFF1976D2) },
                    softWrap = softWrapEnabled
                )
            }
        }
    }
}

@Composable
private fun MaxLinesExampleCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFF1F8E9),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "라인 수 제한 (maxLines)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF388E3C)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF388E3C), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                BasicText(
                    text = "Auto Resizing Text in box but with line limits on the box to demonstrate maxLines behavior with very long text",
                    style = TextStyle(fontSize = 24.sp),
                    autoSize = TextAutoSize.StepBased(
                        maxFontSize = 16.sp
                    ),
                    color = { Color(0xFF388E3C) },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun OnTextLayoutCard() {
    var measuredWidth by remember { mutableStateOf(0) }
    var measuredHeight by remember { mutableStateOf(0) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFFCE4EC),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "onTextLayout 콜백",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE91E63)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .border(1.dp, Color(0xFFE91E63), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Column {
                    BasicText(
                        text = "Auto Resizing Text with onTextLayout callback to measure actual text size",
                        style = TextStyle(fontSize = 24.sp),
                        autoSize = TextAutoSize.StepBased(
                            maxFontSize = 18.sp
                        ),
                        color = { Color(0xFFE91E63) },
                        onTextLayout = { layoutResult ->
                            measuredWidth = layoutResult.size.width
                            measuredHeight = layoutResult.size.height
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "측정된 크기: ${measuredWidth}px × ${measuredHeight}px",
                        color = Color(0xFFE91E63),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun PracticalExamplesCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "활용 예시",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )

            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                elevation = 2.dp,
                backgroundColor = Color(0xFF6200EE)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    BasicText(
                        text = "카드 제목이 자동으로 크기 조절됩니다 - 매우 긴 제목도 자동으로 맞춰집니다",
                        style = TextStyle(fontSize = 20.sp),
                        autoSize = TextAutoSize.StepBased(
                            maxFontSize = 16.sp
                        ),
                        color = { Color.White }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF03DAC6))
            ) {
                BasicText(
                    text = "버튼 텍스트도 자동 크기 조절 - 매우 긴 버튼 텍스트입니다",
                    style = TextStyle(fontSize = 18.sp),
                    autoSize = TextAutoSize.StepBased(
                        maxFontSize = 14.sp
                    ),
                    color = { Color.Black }
                )
            }


        }
    }
}



@Preview(showBackground = true)
@Composable
private fun AutoSizingTextExamplePreview() {
    AutoSizingTextExampleUI(
        onBackEvent = {}
    )
} 