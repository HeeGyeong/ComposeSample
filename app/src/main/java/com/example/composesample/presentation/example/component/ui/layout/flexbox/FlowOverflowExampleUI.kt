package com.example.composesample.presentation.example.component.ui.layout.flexbox

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ContextualFlowRow
import androidx.compose.foundation.layout.ContextualFlowRowOverflow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowOverflow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader

/**
 * FlowRow/FlowColumn 의 오버플로 제어 예제.
 *
 * 기존 [FlowRowLayoutExampleUI] 는 "아이템이 어떻게 줄바꿈되는가"를 다루고,
 * 이 예제는 "줄 수 제한(maxLines)에 걸려 들어가지 못하는 아이템을 어떻게 보여줄 것인가"를 다룬다.
 *
 * ⚠️ `overflow: FlowRowOverflow` / `overflow: ContextualFlowRowOverflow` 파라미터는
 * 이 프로젝트가 해석하는 foundation-layout 1.11.1 기준 `@Deprecated("The overflow parameter has been deprecated")`
 * 로 표시돼 있으나, 이 버전에서 오버플로 인디케이터를 붙일 수 있는 **유일한 진입점**이라 대체재가 없다.
 * 그래서 이 파라미터를 직접 쓰는 함수에만 `@Suppress("DEPRECATION")` 을 최소 범위로 달았다.
 */
@Composable
fun FlowOverflowExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(title = "Flow 레이아웃 오버플로 제어", onBackIconClicked = onBackEvent)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { OverflowConceptCard() }
            item { ExpandIndicatorCard() }
            item { ExpandCollapseIndicatorCard() }
            item { ComposeCountComparisonCard() }
            item { PracticalRecipientChipsCard() }
        }
    }
}

@Composable
private fun OverflowConceptCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "오버플로 제어 개요",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "FlowRow/FlowColumn 은 아이템이 넘치면 자동으로 줄바꿈하지만, 줄 수 자체를 " +
                        "제한(maxLines)하면 넘치는 아이템을 어떻게 처리할지 별도로 정해야 합니다.",
                fontSize = 14.sp,
                color = Color(0xFF424242),
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            val features = listOf(
                "maxLines" to "표시할 최대 줄 수 제한",
                "FlowRowOverflow.expandIndicator" to "넘치면 커스텀 \"+N개\" 인디케이터 표시",
                "expandOrCollapseIndicator" to "펼침/접힘 인디케이터를 한 쌍으로 지정",
                "ContextualFlowRow" to "인덱스 기반 지연 생성 — 보이는 만큼만 컴포즈",
                "totalItemCount/shownItemCount" to "오버플로 스코프에서 전체/표시 개수 조회"
            )
            features.forEach { (api, desc) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = api,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1565C0),
                        modifier = Modifier.width(150.dp)
                    )
                    Text(text = desc, fontSize = 12.sp, color = Color(0xFF424242))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Suppress("DEPRECATION") // FlowRowOverflow 를 넘기는 유일한 진입점(1.11.1 기준 deprecated, 대체 API 없음)
@Composable
private fun ExpandIndicatorCard() {
    val allTags = remember { (1..16).map { "태그%02d".format(it) } }
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "FlowRowOverflow.expandIndicator",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE65100)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "maxLines=2 로 제한하고, 넘치는 자리에 \"+N개 더보기\" 칩을 배치합니다.",
                fontSize = 12.sp,
                color = Color(0xFF757575)
            )
            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFFFCC80), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    maxLines = if (expanded) Int.MAX_VALUE else 2,
                    overflow = FlowRowOverflow.expandIndicator {
                        // this: FlowRowOverflowScope — totalItemCount/shownItemCount 조회 가능
                        OverflowIndicatorChip(
                            label = "+${totalItemCount - shownItemCount}개 더보기",
                            color = Color(0xFFEF6C00),
                            onClick = { expanded = true }
                        )
                    }
                ) {
                    allTags.forEach { tag -> TagChip(label = tag, color = Color(0xFFFF9800)) }
                }
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { expanded = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF6C00))
                ) {
                    Text(text = "접기", color = Color.White, fontSize = 13.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Suppress("DEPRECATION") // FlowRowOverflow 를 넘기는 유일한 진입점(1.11.1 기준 deprecated, 대체 API 없음)
@Composable
private fun ExpandCollapseIndicatorCard() {
    val allTags = remember {
        listOf("Android", "Compose", "Jetpack", "Kotlin", "Material3", "Coroutines", "Flow", "Ktor")
    }
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "FlowRowOverflow.expandOrCollapseIndicator",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF7B1FA2)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "펼침/접힘 인디케이터를 한 쌍으로 등록합니다. minRowsToShowCollapse 는 " +
                        "펼쳤을 때 실제 줄 수가 이 값 이상일 때만 \"접기\" 를 보여줍니다.",
                fontSize = 12.sp,
                color = Color(0xFF757575),
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFCE93D8), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    maxLines = if (expanded) Int.MAX_VALUE else 1,
                    overflow = FlowRowOverflow.expandOrCollapseIndicator(
                        minRowsToShowCollapse = 3,
                        minHeightToShowCollapse = 0.dp,
                        expandIndicator = {
                            OverflowIndicatorChip(
                                label = "펼치기 (+${totalItemCount - shownItemCount})",
                                color = Color(0xFF9C27B0),
                                onClick = { expanded = true }
                            )
                        },
                        collapseIndicator = {
                            OverflowIndicatorChip(
                                label = "접기",
                                color = Color(0xFF6A1B9A),
                                onClick = { expanded = false }
                            )
                        }
                    )
                ) {
                    allTags.forEach { tag -> TagChip(label = tag, color = Color(0xFF9C27B0)) }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Suppress("DEPRECATION") // ContextualFlowRowOverflow 를 넘기는 유일한 진입점(1.11.1 기준 deprecated, 대체 API 없음)
@Composable
private fun ComposeCountComparisonCard() {
    val items = remember { (1..40).map { "아이템%02d".format(it) } }
    val maxLines = 2

    // 대조군: 일반 FlowRow — content 람다가 forEach 로 전체를 미리 호출하므로 화면에 안 보여도 전부 컴포즈된다.
    var flowRowComposed by remember { mutableStateOf(setOf<Int>()) }
    // 실험군: ContextualFlowRow — 인덱스 기반 지연 생성이라 실제 배치되는 만큼만 컴포즈된다.
    var contextualComposed by remember { mutableStateOf(setOf<Int>()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F7FF))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "실측 대조: FlowRow vs ContextualFlowRow",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1565C0)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "두 레이아웃 모두 maxLines=$maxLines 로 화면엔 같은 줄 수만 보이지만, " +
                        "실제로 컴포즈되는 아이템 개수는 다릅니다.",
                fontSize = 12.sp,
                color = Color(0xFF757575),
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "FlowRow — 컴포즈됨: ${flowRowComposed.size} / ${items.size}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD32F2F)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFEF9A9A), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    maxLines = maxLines
                ) {
                    items.forEachIndexed { index, label ->
                        SideEffect {
                            if (index !in flowRowComposed) flowRowComposed = flowRowComposed + index
                        }
                        TagChip(label = label, color = Color(0xFFE57373))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "ContextualFlowRow — 컴포즈됨: ${contextualComposed.size} / ${items.size}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFA5D6A7), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                ContextualFlowRow(
                    itemCount = items.size,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    maxLines = maxLines,
                    overflow = ContextualFlowRowOverflow.expandIndicator {
                        // this: ContextualFlowRowOverflowScope
                        OverflowIndicatorChip(
                            label = "+${totalItemCount - shownItemCount}",
                            color = Color(0xFF43A047),
                            onClick = {}
                        )
                    }
                ) { index ->
                    SideEffect {
                        if (index !in contextualComposed) contextualComposed = contextualComposed + index
                    }
                    TagChip(label = items[index], color = Color(0xFF81C784))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "숫자 차이가 \"화면에 보이는 만큼만 컴포즈한다\"는 지연 생성의 증거입니다.",
                fontSize = 11.sp,
                color = Color(0xFF757575)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Suppress("DEPRECATION") // FlowRowOverflow 를 넘기는 유일한 진입점(1.11.1 기준 deprecated, 대체 API 없음)
@Composable
private fun PracticalRecipientChipsCard() {
    val recipients = remember {
        listOf(
            "kim@example.com", "lee@example.com", "park@example.com", "choi@example.com",
            "jung@example.com", "kang@example.com", "yoon@example.com", "song@example.com",
            "han@example.com", "seo@example.com"
        )
    }
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "실용 예제: 수신자 칩 목록",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF57F17)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "메일 수신자처럼 개수가 가변적인 목록에서, 한 줄만 보여주고 나머지는 \"+N명\" 으로 접어둡니다.",
                fontSize = 12.sp,
                color = Color(0xFF757575),
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                maxLines = if (expanded) Int.MAX_VALUE else 1,
                overflow = FlowRowOverflow.expandIndicator {
                    OverflowIndicatorChip(
                        label = "+${totalItemCount - shownItemCount}명",
                        color = Color(0xFFF9A825),
                        onClick = { expanded = true }
                    )
                }
            ) {
                recipients.forEach { email -> TagChip(label = email, color = Color(0xFFFBC02D)) }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (expanded) "전체 ${recipients.size}명 표시 중" else "일부만 표시 중 — 칩을 눌러 전체 보기",
                fontSize = 12.sp,
                color = Color(0xFFF57F17),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ── 재사용 컴포넌트 ────────────────────────────────────────────────────────

@Composable
private fun TagChip(label: String, color: Color) {
    Box(
        modifier = Modifier
            .wrapContentHeight()
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text = label, fontSize = 12.sp, color = color, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun OverflowIndicatorChip(label: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(20.dp),
        elevation = null,
        modifier = Modifier.height(32.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
    ) {
        Text(text = label, fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Medium)
    }
}

@Preview(showBackground = true)
@Composable
private fun FlowOverflowExamplePreview() {
    FlowOverflowExampleUI(onBackEvent = {})
}
