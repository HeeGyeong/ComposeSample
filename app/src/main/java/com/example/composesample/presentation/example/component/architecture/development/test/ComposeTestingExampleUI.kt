package com.example.composesample.presentation.example.component.architecture.development.test

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ComposeTestingExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F8E9))
    ) {
        // 헤더
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF2E7D32))
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackEvent) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "뒤로가기",
                    tint = Color.White
                )
            }
            Text(
                text = "Compose UI Testing",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                // 개요 카드
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Compose UI Testing",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Compose UI 테스트는 실기기 없이 JVM에서 실행 가능합니다.\n" +
                                    "Semantics 트리를 통해 UI 요소를 찾고 상호작용합니다.",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            item {
                // 의존성 카드
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "📦 의존성 설정",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        CodeBlock(
                            code = "// build.gradle\n" +
                                    "androidTestImplementation(\n" +
                                    "    libs.androidx.compose.ui.test.junit4\n" +
                                    ")\n" +
                                    "debugImplementation(\n" +
                                    "    libs.androidx.compose.ui.test.manifest\n" +
                                    ")"
                        )
                    }
                }
            }

            item {
                // 기본 구조 카드
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "🧪 기본 테스트 구조",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        CodeBlock(
                            code = "@RunWith(AndroidJUnit4::class)\n" +
                                    "class MyScreenTest {\n\n" +
                                    "    @get:Rule\n" +
                                    "    val rule = createComposeRule()\n\n" +
                                    "    @Test\n" +
                                    "    fun buttonClick_updatesText() {\n" +
                                    "        rule.setContent {\n" +
                                    "            MyScreen()\n" +
                                    "        }\n" +
                                    "        rule.onNodeWithText(\"Click\")\n" +
                                    "            .performClick()\n" +
                                    "        rule.onNodeWithText(\"Clicked!\")\n" +
                                    "            .assertIsDisplayed()\n" +
                                    "    }\n" +
                                    "}"
                        )
                    }
                }
            }

            item {
                // Finder API 카드
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "🔍 Finder API (노드 탐색)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        FinderRow("onNodeWithText(\"텍스트\")", "텍스트로 노드 탐색")
                        FinderRow("onNodeWithTag(\"testTag\")", "testTag로 탐색 (권장)")
                        FinderRow("onNodeWithContentDescription(\"desc\")", "접근성 설명으로 탐색")
                        FinderRow("onAllNodesWithText(\"텍스트\")", "조건 맞는 모든 노드")
                        FinderRow("onNodeWithText(\"\").useUnmergedTree()", "병합 전 Semantics 트리 접근")

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "💡 testTag 사용 권장",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF555555)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        CodeBlock(
                            code = "// UI 코드\n" +
                                    "Button(\n" +
                                    "    modifier = Modifier\n" +
                                    "        .semantics { testTag = \"submitBtn\" }\n" +
                                    ") { Text(\"제출\") }\n\n" +
                                    "// 테스트 코드\n" +
                                    "rule.onNodeWithTag(\"submitBtn\")\n" +
                                    "    .assertIsEnabled()"
                        )
                    }
                }
            }

            item {
                // Action + Assertion 카드
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "▶ Action API",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FinderRow("performClick()", "클릭 이벤트 발생")
                        FinderRow("performTextInput(\"text\")", "텍스트 입력")
                        FinderRow("performScrollTo()", "해당 노드까지 스크롤")
                        FinderRow("performTouchInput { swipeLeft() }", "스와이프 제스처")

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "✅ Assertion API",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FinderRow("assertIsDisplayed()", "화면에 표시 중인지 검증")
                        FinderRow("assertIsEnabled()", "활성화 상태 검증")
                        FinderRow("assertTextEquals(\"text\")", "텍스트 일치 검증")
                        FinderRow("assertDoesNotExist()", "노드 미존재 검증")
                        FinderRow("assertCountEquals(3)", "노드 개수 검증")
                    }
                }
            }

            item {
                // 팁 카드
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "💡 Best Practices",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TipRow("testTag를 UI 코드에 심어두면 텍스트 변경에 테스트가 깨지지 않음")
                        TipRow("waitUntil { condition } 으로 비동기 상태 변화 대기 가능")
                        TipRow("createAndroidComposeRule<Activity>()로 실제 Activity 컨텍스트 활용")
                        TipRow("ViewModel 목 주입 시 Koin testKoin {} 또는 Hilt @TestInstallIn 활용")
                        TipRow("Screenshot Testing과 조합하면 회귀 방지 커버리지 극대화")
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun CodeBlock(code: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2A1B))
    ) {
        Text(
            text = code,
            fontSize = 11.sp,
            color = Color(0xFF80CBC4),
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(12.dp),
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun FinderRow(api: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = api,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1565C0),
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.weight(0.55f)
        )
        Text(
            text = description,
            fontSize = 12.sp,
            color = Color(0xFF555555),
            modifier = Modifier.weight(0.45f)
        )
    }
}

@Composable
private fun TipRow(tip: String) {
    Row(modifier = Modifier.padding(vertical = 3.dp)) {
        Text(text = "• ", fontSize = 13.sp, color = Color(0xFF2E7D32))
        Text(text = tip, fontSize = 13.sp, color = Color(0xFF444444), lineHeight = 18.sp)
    }
}
