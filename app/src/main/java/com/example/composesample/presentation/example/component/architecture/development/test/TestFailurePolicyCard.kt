package com.example.composesample.presentation.example.component.architecture.development.test

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/*
 * UI Test Example 보강(#191) — 실패 순간 자동 저장 TestFailurePolicy(ui-test 1.13).
 * 앱 화면에서는 테스트를 돌릴 수 없으므로 설정 모양과 계측 테스트(UITestFailurePolicyTest)로 잰 결과를 보인다.
 * 위의 버튼 · 입력 칸이 그 테스트가 일부러 틀리게 단언하는 대상이다.
 */

private val Accent = Color(0xFF1565C0)
private val Body = Color(0xFF37474F)
private val Muted = Color(0xFF607D8B)

/** 측정 표의 한 줄 — 조건 · 산출물 */
private data class PolicyRow(val condition: String, val result: String)

/** Pixel Tablet API 35 · Pixel 4 API 30 에뮬에서 am instrument 로 잰 결과(두 기기 같음) */
private val policyRows = listOf(
    PolicyRow("기본값(Unspecified) · 인자 없음", "0개 — 핸들러만 불린다(받은 error 는 던져진 그 AssertionError)"),
    PolicyRow("기본값 + 스위트 인자 두 개 true", "2개 — Unspecified 는 러너 인자를 따른다"),
    PolicyRow("Enabled · Enabled", "2개 — <nanoTime>_screenshot.png · <nanoTime>_ui.txt(같은 앞머리)"),
    PolicyRow("Disabled · Disabled + 스위트 인자 true", "0개 — 테스트에 적은 값이 인자를 이긴다"),
    PolicyRow("예외를 던지는 핸들러", "원래 AssertionError 가 그대로 · 핸들러 예외는 suppressed · 뒤 핸들러도 불린다")
)

@Composable
fun TestFailurePolicyCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text("실패 순간 자동 저장 — TestFailurePolicy (ui-test 1.13)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Accent)
            Spacer(Modifier.height(4.dp))
            Note(
                "ComposeUiTestConfig(failurePolicy = …) 를 createComposeRule(config) 나 runComposeUiTest(config) 에 넘기면, " +
                    "테스트가 실패할 때 스크린샷 · UI 트리를 저장하고 failureHandlers 를 차례로 부른 뒤 원래 실패를 다시 던진다. opt-in 없음."
            )
            Spacer(Modifier.height(8.dp))
            CodeBlock(
                "val config = ComposeUiTestConfig(\n" +
                    "    failurePolicy = TestFailurePolicy(\n" +
                    "        screenshotCaptureMode = CaptureMode.Enabled,\n" +
                    "        uiHierarchyCaptureMode = CaptureMode.Enabled,\n" +
                    "        failureHandlers = listOf(TestFailureHandler { ctx ->\n" +
                    "            ctx.artifacts.forEach { Log.e(\"UiTest\", it.fileName) }\n" +
                    "        })\n" +
                    "    )\n" +
                    ")\n" +
                    "@get:Rule val rule = createComposeRule(config)\n\n" +
                    "// 스위트 전체는 러너 인자로(기본값 Unspecified 일 때만)\n" +
                    "// androidx.compose.ui.test.failure.isScreenshotCaptureEnabled = true\n" +
                    "// androidx.compose.ui.test.failure.isUiHierarchyCaptureEnabled = true"
            )
            Spacer(Modifier.height(10.dp))
            Text("이 화면을 일부러 틀리게 단언해 본 결과", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Body)
            Note("10 + 20 을 계산하고 '결과: 31' 이라고 단언(실제 30). 실패하는 실행은 정상 테스트 안에서 runCatching 으로 잡는다 — 스위트에 실패 테스트가 남지 않는다.")
            Spacer(Modifier.height(4.dp))
            policyRows.forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                        .background(Color(0xFFF1F5F9), RoundedCornerShape(6.dp))
                        .padding(8.dp)
                ) {
                    Text(row.condition, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Body, modifier = Modifier.weight(0.42f))
                    Text(row.result, fontSize = 11.sp, color = Body, modifier = Modifier.weight(0.58f), lineHeight = 15.sp)
                }
            }
            Spacer(Modifier.height(10.dp))
            Text("파일은 어디에 무엇으로", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Body)
            Note(
                "저장소는 PlatformTestStorageRegistry — useTestStorageService 를 켜지 않은 am instrument 에서는 FileTestStorage. " +
                    "러너 인자 additionalTestOutputDir 가 있으면 그 폴더(실측), 없으면 API 29+ 는 /sdcard/Android/media/<패키지>/additionalTestOutputDir(API 30 · 35 실측), " +
                    "그 아래 버전은 외부 캐시 폴더 아래 additionalTestOutputDir(monitor 1.8.0 TestDirCalculator 소스 — 기기로는 확인 못 함). adb pull 로 꺼낸다."
            )
            Note(
                "screenshot.png 는 UiAutomation.takeScreenshot — 컴포즈 노드가 아니라 기기 화면 전체(태블릿 2560×1600 · Pixel 4 1080×2280). " +
                    "실측에서 막 부팅한 에뮬의 'System UI isn't responding' 창이 함께 찍혔다 — 시스템 창이 가린 실패를 알아보는 데 쓸모 있다."
            )
            Note(
                "ui.txt 는 View 계층(Espresso 표기) 사이에 컴포즈 시맨틱 트리를 끼워 넣은 글 — 실패한 노드가 Tag: 'resultText' · Text = '[결과: 30]' 로 남는다."
            )
            Note(
                "핸들러는 컴포즈 계층이 이미 내려간 뒤 테스트 스레드에서 차례로 돈다 — onNodeWithTag · waitForIdle 를 부를 수 없고, " +
                    "테스트 시간 제한 밖이라 무거운 IO 는 스스로 시간을 제한해야 한다(KDoc)."
            )
            Note(
                "AGP 9.5.3 의 connected · 관리 기기 테스트 태스크에도 additionalTestOutputDir 처리가 들어 있다(클래스 문자열로 확인). " +
                    "이 프로젝트 측정은 실기기가 연결된 환경이라 connected 태스크 대신 에뮬에 am instrument 로 했다."
            )
        }
    }
}

@Composable
private fun Note(text: String) {
    Text(text, fontSize = 11.sp, color = Muted, lineHeight = 16.sp, modifier = Modifier.padding(top = 4.dp))
}

@Composable
private fun CodeBlock(code: String) {
    Text(
        code,
        fontSize = 10.sp,
        fontFamily = FontFamily.Monospace,
        color = Color(0xFF263238),
        lineHeight = 14.sp,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFECEFF1), RoundedCornerShape(8.dp))
            .padding(10.dp)
    )
}
