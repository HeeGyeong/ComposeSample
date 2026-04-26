package com.example.composesample.presentation.example.component.ui.layout.modifier

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Modifier Order Example
 * - 같은 modifier 조합도 순서에 따라 layout / draw / 입력 처리 결과가 달라진다.
 * - 좌/우로 동일한 modifier 세트를 순서만 바꿔 배치하고, 결과를 시각적으로 비교한다.
 */
@Composable
fun ModifierOrderExampleUI(onBackEvent: () -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 16.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onBackEvent.invoke() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "뒤로가기"
                    )
                }
                Text(
                    text = "Modifier Order in Compose",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        item { PaddingVsBackgroundSection() }
        item { Spacer(modifier = Modifier.height(20.dp)) }

        item { BorderVsClipSection() }
        item { Spacer(modifier = Modifier.height(20.dp)) }

        item { ClickableVsPaddingSection() }
        item { Spacer(modifier = Modifier.height(20.dp)) }

        item { SizeVsPaddingSection() }
        item { Spacer(modifier = Modifier.height(20.dp)) }

        item { SummaryCard() }
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

/**
 * 1. padding ↔ background
 * - padding → background : padding을 적용한 "더 작은" 영역에만 배경이 칠해진다.
 * - background → padding : 전체 영역에 배경이 칠해진 뒤 안쪽에 padding이 생긴다.
 */
@Composable
private fun PaddingVsBackgroundSection() {
    SectionTitle("1. padding ↔ background")
    SectionDesc("padding이 먼저면 배경이 안쪽으로 들어가고, background가 먼저면 배경이 바깥까지 칠해진다.")
    Spacer(modifier = Modifier.height(8.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        DemoColumn(
            title = "padding → background",
            code = ".padding(16.dp)\n.background(Color.Blue)"
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .padding(16.dp)
                    .background(Color(0xFF42A5F5)),
                contentAlignment = Alignment.Center
            ) { Text("작아짐", color = Color.White, fontSize = 13.sp) }
        }
        DemoColumn(
            title = "background → padding",
            code = ".background(Color.Blue)\n.padding(16.dp)"
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(Color(0xFF42A5F5))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) { Text("그대로", color = Color.White, fontSize = 13.sp) }
        }
    }
}

/**
 * 2. border ↔ clip
 * - clip → border : 모서리가 잘리고, 그 위에 그려진 border도 잘려 사라진 것처럼 보인다.
 * - border → clip : border는 사각형 그대로 그려지고, 그 안의 콘텐츠만 둥글게 잘린다.
 */
@Composable
private fun BorderVsClipSection() {
    SectionTitle("2. border ↔ clip")
    SectionDesc("clip이 먼저면 border까지 잘리고, border가 먼저면 사각 border 안쪽만 잘린다.")
    Spacer(modifier = Modifier.height(8.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        DemoColumn(
            title = "clip → border",
            code = ".clip(CircleShape)\n.border(4.dp, Red, CircleShape)"
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(Color(0xFFFFCDD2))
                    .clip(CircleShape)
                    .border(4.dp, Color(0xFFE53935), CircleShape),
                contentAlignment = Alignment.Center
            ) { Text("원형 border", fontSize = 12.sp) }
        }
        DemoColumn(
            title = "border → clip",
            code = ".border(4.dp, Red)\n.clip(CircleShape)"
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(Color(0xFFFFCDD2))
                    .border(4.dp, Color(0xFFE53935))
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) { Text("사각 border", fontSize = 12.sp) }
        }
    }
}

/**
 * 3. clickable ↔ padding
 * - clickable → padding : padding 영역은 hit-test 대상이 아님. 안쪽만 눌린다.
 * - padding → clickable : padding 포함 전체가 hit-test 대상. 더 넓게 눌린다.
 */
@Composable
private fun ClickableVsPaddingSection() {
    var leftCount by remember { mutableIntStateOf(0) }
    var rightCount by remember { mutableIntStateOf(0) }

    SectionTitle("3. clickable ↔ padding")
    SectionDesc("clickable이 먼저면 padding 영역은 클릭 안 됨. padding이 먼저면 padding까지 클릭 영역.")
    Spacer(modifier = Modifier.height(8.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        DemoColumn(
            title = "clickable → padding\n(클릭: $leftCount)",
            code = ".clickable { ... }\n.padding(20.dp)"
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(Color(0xFFFFE082))
                    .clickable { leftCount++ }
                    .padding(20.dp)
                    .background(Color(0xFFFFA000)),
                contentAlignment = Alignment.Center
            ) { Text("안쪽만\n클릭됨", fontSize = 12.sp) }
        }
        DemoColumn(
            title = "padding → clickable\n(클릭: $rightCount)",
            code = ".padding(20.dp)\n.clickable { ... }"
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(Color(0xFFFFE082))
                    .padding(20.dp)
                    .background(Color(0xFFFFA000))
                    .clickable { rightCount++ },
                contentAlignment = Alignment.Center
            ) { Text("주황만\n클릭됨", fontSize = 12.sp) }
        }
    }
}

/**
 * 4. size ↔ padding
 * - size → padding : 지정 사이즈 안에서 padding 적용 → 콘텐츠 영역이 (size - 2*padding).
 * - padding → size : padding 추가 후 size 적용 → 전체가 (size + 2*padding) 만큼 커짐.
 */
@Composable
private fun SizeVsPaddingSection() {
    SectionTitle("4. size ↔ padding")
    SectionDesc("size 먼저면 외곽 크기가 고정. padding 먼저면 같은 size여도 외곽이 더 커진다.")
    Spacer(modifier = Modifier.height(8.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        DemoColumn(
            title = "size → padding",
            code = ".size(120.dp)\n.padding(16.dp)"
        ) {
            Box(
                modifier = Modifier
                    .background(Color(0xFFC5CAE9))
                    .size(120.dp)
                    .padding(16.dp)
                    .background(Color(0xFF3F51B5)),
                contentAlignment = Alignment.Center
            ) { Text("외곽 120", color = Color.White, fontSize = 12.sp) }
        }
        DemoColumn(
            title = "padding → size",
            code = ".padding(16.dp)\n.size(120.dp)"
        ) {
            Box(
                modifier = Modifier
                    .background(Color(0xFFC5CAE9))
                    .padding(16.dp)
                    .size(120.dp)
                    .background(Color(0xFF3F51B5)),
                contentAlignment = Alignment.Center
            ) { Text("외곽 152", color = Color.White, fontSize = 12.sp) }
        }
    }
}

@Composable
private fun DemoColumn(
    title: String,
    code: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.width(160.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .size(150.dp)
                .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) { content() }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = code,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF555555)
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(text = title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
}

@Composable
private fun SectionDesc(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        color = Color(0xFF555555),
        modifier = Modifier.padding(top = 2.dp)
    )
}

@Composable
private fun SummaryCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Modifier 순서 요약",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            listOf(
                "Modifier는 왼쪽(위) → 오른쪽(아래) 순서로 적용된다.",
                "padding/size/offset 등 layout phase modifier는 자식에 전달되는 제약을 변형한다.",
                "background/border/clip 등 draw phase modifier는 적용 시점의 영역에 그려진다.",
                "clickable의 위치는 hit-test 영역을 결정한다 (padding 전/후가 클릭 범위에 영향).",
                "원하는 결과가 안 나오면 한 줄씩 순서를 바꿔 비교하라."
            ).forEach { line ->
                Text(
                    text = "• $line",
                    fontSize = 13.sp,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}
