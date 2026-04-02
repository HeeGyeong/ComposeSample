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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * ScreenshotTesting Example 참고 자료
 * - Paparazzi (Cash App): https://github.com/cashapp/paparazzi
 * - Roborazzi (takahirom): https://github.com/takahirom/roborazzi
 * - 공식 Compose 테스트 가이드: https://developer.android.com/develop/ui/compose/testing
 * - Screenshot Testing 블로그: https://medium.com/androiddevelopers/screenshot-testing-jetpack-compose-with-paparazzi-11d38feecef6
 */

@Composable
fun ScreenshotTestingExampleUI(onBackEvent: () -> Unit) {
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
                    text = "Screenshot Testing",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        item {
            OverviewCard()
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            ComparisonCard()
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            PaparazziCard()
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            RoborazziCard()
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            WorkflowCard()
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            BestPracticesCard()
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/** 개요 카드 */
@Composable
private fun OverviewCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1565C0))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "스크린샷 테스트란?",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "UI 컴포넌트를 렌더링하여 이미지(골든 파일)로 저장하고,\n" +
                        "이후 변경 시 골든 파일과 비교하여 의도치 않은 UI 회귀를 감지.",
                color = Color.White,
                fontSize = 13.sp
            )
        }
    }
}

/** Paparazzi vs Roborazzi 비교 */
@Composable
private fun ComparisonCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Paparazzi vs Roborazzi 비교",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            ComparisonRow(label = "실기기 필요", paparazzi = "불필요", roborazzi = "불필요")
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            ComparisonRow(label = "에뮬레이터 필요", paparazzi = "불필요", roborazzi = "불필요(Robolectric)")
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            ComparisonRow(label = "렌더링 엔진", paparazzi = "LayoutLib", roborazzi = "Robolectric + LayoutLib")
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            ComparisonRow(label = "애니메이션 캡처", paparazzi = "불가", roborazzi = "GIF/동영상 지원")
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            ComparisonRow(label = "CI 속도", paparazzi = "빠름", roborazzi = "빠름")
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            ComparisonRow(label = "멀티 디바이스", paparazzi = "DeviceConfig 직접 설정", roborazzi = "@RoborazziConfig 어노테이션")
        }
    }
}

@Composable
private fun ComparisonRow(label: String, paparazzi: String, roborazzi: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, modifier = Modifier.weight(1.2f))
        Text(
            text = "Paparazzi: $paparazzi",
            fontSize = 12.sp,
            color = Color(0xFF1565C0),
            modifier = Modifier.weight(1.5f)
        )
        Text(
            text = "Roborazzi: $roborazzi",
            fontSize = 12.sp,
            color = Color(0xFF2E7D32),
            modifier = Modifier.weight(1.5f)
        )
    }
}

/** Paparazzi 사용법 */
@Composable
private fun PaparazziCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Paparazzi 기본 사용법",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color(0xFF1565C0)
            )
            Spacer(modifier = Modifier.height(8.dp))
            CodeSnippet(
                code = """
// build.gradle (테스트 모듈)
plugins {
    id("app.cash.paparazzi")
}

// 테스트 코드
class MyComposableTest {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
        theme = "android:Theme.Material.Light"
    )

    @Test
    fun snapshot_button() {
        paparazzi.snapshot {
            MyButton(text = "확인")
        }
    }
}

// 골든 이미지 기록: ./gradlew recordPaparazziDebug
// 회귀 검사:       ./gradlew verifyPaparazziDebug
                """.trimIndent()
            )
        }
    }
}

/** Roborazzi 사용법 */
@Composable
private fun RoborazziCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Roborazzi 기본 사용법",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color(0xFF2E7D32)
            )
            Spacer(modifier = Modifier.height(8.dp))
            CodeSnippet(
                code = """
// build.gradle
testImplementation("io.github.takahirom.roborazzi:roborazzi:x.x.x")
testImplementation("io.github.takahirom.roborazzi:roborazzi-compose:x.x.x")

// 테스트 코드 (@RunWith(RobolectricTestRunner::class) 필요)
@RunWith(RobolectricTestRunner::class)
class MyComposableRoborazziTest {

    @Test
    fun capture_button() {
        val composeTestRule = createComposeRule()
        composeTestRule.setContent {
            MyButton(text = "확인")
        }
        composeTestRule.onRoot()
            .captureRoboImage("src/test/snapshots/MyButton.png")
    }
}

// 골든 이미지 기록: ./gradlew recordRoborazziDebug
// 회귀 검사:       ./gradlew verifyRoborazziDebug
                """.trimIndent()
            )
        }
    }
}

/** 워크플로우 카드 */
@Composable
private fun WorkflowCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "스크린샷 테스트 워크플로우",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            listOf(
                "1. 테스트 코드 작성 (Composable + Paparazzi/Roborazzi)",
                "2. record 태스크로 골든 이미지 생성 → Git 커밋",
                "3. UI 변경 후 verify 태스크로 차이 감지",
                "4. 의도적 변경 시 record 재실행으로 골든 업데이트",
                "5. CI에서 verify 태스크 자동 실행 → PR 리뷰에 diff 이미지 첨부"
            ).forEach { step ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = step, fontSize = 13.sp)
            }
        }
    }
}

/** 모범 사례 카드 */
@Composable
private fun BestPracticesCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "모범 사례",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            listOf(
                "골든 파일은 VCS(Git)에 포함하여 팀 공유",
                "단위 컴포넌트 단위 테스트 → 변경 범위 최소화",
                "Dark/Light 테마 각각 캡처 권장",
                "다양한 폰트 스케일(0.85x, 1.0x, 1.3x) 테스트",
                "동적 데이터 대신 고정 Fake 데이터 사용",
                "애니메이션은 idle 상태에서 캡처 (Paparazzi) 또는 GIF(Roborazzi)"
            ).forEach { practice ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "• $practice", fontSize = 13.sp)
            }
        }
    }
}

/** 코드 스니펫 표시용 박스 */
@Composable
private fun CodeSnippet(code: String) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF263238))
    ) {
        Text(
            text = code,
            modifier = Modifier.padding(12.dp),
            color = Color(0xFFECEFF1),
            fontSize = 11.sp,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
        )
    }
}
