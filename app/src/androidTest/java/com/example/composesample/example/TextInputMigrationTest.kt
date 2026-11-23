package com.example.composesample.example

import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import com.example.composesample.presentation.example.component.system.security.AppSecurityExampleUI
import com.example.composesample.presentation.example.component.ui.autofill.AutofillExampleUI
import com.example.composesample.presentation.example.component.ui.text.RichContentTextInputExampleUI
import com.example.composesample.presentation.example.component.ui.text.TextFieldMaxLengthExampleUI
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * DEPRECATION-113-01 의 텍스트 입력 이관이 화면의 주장 · 동작을 지키는지(실제 예제 화면으로).
 * given : TextFieldMaxLength 화면(maxLength → maxLengthReject + 새 maxLengthTrim 카드) · Autofill 화면(비밀번호 → OutlinedSecureTextField) ·
 *         AppSecurity · RichContent 화면(값 기반 BasicTextField → TextFieldState). AGSL 편집기는 매 프레임 애니메이션이라
 *         테스트 규칙의 idle 대기가 끝나지 않아 여기서 빼고 API 35 에뮬에서 손으로 확인(편집 → 컴파일 실패 → 초기화 → 성공)
 * when : 8자 뒤 다섯 글자 한 번에 넣기(붙여넣기) · 한 글자씩 넣기 · state.edit 주입 / 편집 노드의 자동완성 힌트 읽기
 * then : Reject 8자 · Trim "12" 만 들어가 10자 · 한 글자씩은 둘 다 10자 · 주입은 제한을 넘는다 /
 *        SecureTextField 가 안에서 거는 Password 가 가입 폼의 NewPassword 힌트를 덮지 않는다
 */
class TextInputMigrationTest {

    @get:Rule
    val rule = createComposeRule()

    private fun editableText(index: Int, text: String): String {
        val nodes = rule.onAllNodesWithText(text).fetchSemanticsNodes().filter {
            it.config.getOrNull(SemanticsProperties.EditableText) != null
        }
        return nodes[index].config[SemanticsProperties.EditableText].text
    }

    @Test
    fun maxLengthReject_는_붙여넣기_전체를_되돌리고_Trim_은_넘친_만큼만_자른다() {
        rule.setContent { TextFieldMaxLengthExampleUI(onBackEvent = {}) }
        rule.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("8자 채우기"))
        rule.onNodeWithText("8자 채우기").performSemanticsAction(SemanticsActions.OnClick)
        rule.waitForIdle()

        val fields = rule.onAllNodesWithText("abcdefgh")
        assertEquals("Reject · Trim 두 칸", 2, fields.fetchSemanticsNodes().size)
        // 붙여넣기 = 다섯 글자가 한 번에 들어오는 입력
        fields[0].performTextInput("12345")
        fields[1].performTextInput("12345")
        rule.waitForIdle()
        assertEquals("abcdefgh", editableText(0, "abcdefgh"))
        assertEquals("abcdefgh12", rule.onAllNodesWithText("abcdefgh12").fetchSemanticsNodes().single()
            .config[SemanticsProperties.EditableText].text)

        // 한 글자씩은 둘 다 10자에서 멈춘다(Reject 칸: 8 → 10)
        repeat(3) { rule.onAllNodesWithText("abcdefgh", substring = true)[0].performTextInput("x") }
        rule.waitForIdle()
        assertEquals(1, rule.onAllNodesWithText("abcdefghxx").fetchSemanticsNodes().size)
    }

    @Test
    fun 두_변환_모두_state_edit_주입은_막지_못한다() {
        rule.setContent { TextFieldMaxLengthExampleUI(onBackEvent = {}) }
        val list = rule.onNode(hasScrollToNodeAction())
        list.performScrollToNode(hasText("긴 텍스트 주입"))
        // 앞 테스트의 키보드 창이 남아 있으면 터치 주입이 막힌다 — 시맨틱 클릭으로 누른다.
        // 같은 이름 버튼이 둘(❌ 버그 재현 카드 · ✅ snapshotFlow 카드) — 화면 순서대로 0, 1
        rule.onAllNodesWithText("긴 텍스트 주입")[0].performSemanticsAction(SemanticsActions.OnClick)
        rule.waitForIdle()
        assertTrue(
            "❌ 카드: maxLengthReject 를 건 칸에 38자가 그대로",
            rule.onAllNodesWithText("This is a long text that exceeds limit").fetchSemanticsNodes().isNotEmpty()
        )

        list.performScrollToNode(hasText("✅ 올바른 해결: LaunchedEffect + snapshotFlow"))
        rule.onAllNodesWithText("긴 텍스트 주입")[1].performSemanticsAction(SemanticsActions.OnClick)
        rule.waitUntil(timeoutMillis = 3_000) {
            rule.onAllNodesWithText("This is a ").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun SecureTextField_로_바꿔도_자동완성_힌트가_필드마다_그대로다() {
        rule.setContent { AutofillExampleUI(onBackEvent = {}) }
        val list = rule.onNode(hasScrollToNodeAction())
        list.performScrollToNode(hasText("비밀번호"))
        val login = rule.onAllNodes(hasSetTextAction(), useUnmergedTree = true).fetchSemanticsNodes()
            .mapNotNull { it.config.getOrNull(SemanticsProperties.ContentType) }
        assertTrue("로그인 폼: Username · Password $login", login.containsAll(listOf(ContentType.Username, ContentType.Password)))

        list.performScrollToNode(hasText("새 비밀번호"))
        val signUp = rule.onAllNodes(hasSetTextAction(), useUnmergedTree = true).fetchSemanticsNodes()
            .mapNotNull { it.config.getOrNull(SemanticsProperties.ContentType) }
        assertTrue("가입 폼: NewPassword 가 남아 있어야 한다 $signUp", ContentType.NewPassword in signUp)
    }

    @Test
    fun AppSecurity_평문_칸을_바꾸면_그_값이_암호화_복호화된다() {
        rule.setContent { AppSecurityExampleUI(onBackEvent = {}) }
        // 이 화면은 LazyColumn 이 아니라 verticalScroll — 노드 쪽에서 performScrollTo
        rule.onNodeWithText("내 카드 번호 1234-5678-9012-3456").performScrollTo().performTextReplacement("hello-113")
        rule.onNodeWithText("암호화").performSemanticsAction(SemanticsActions.OnClick)
        rule.onNodeWithText("복호화").performSemanticsAction(SemanticsActions.OnClick)
        rule.waitForIdle()
        rule.onNodeWithText("✅ 복호화 결과: hello-113").assertExists()
    }

    @Test
    fun RichContent_빈_칸에만_안내_문구가_보인다() {
        rule.setContent { RichContentTextInputExampleUI(onBackEvent = {}) }
        val list = rule.onNode(hasScrollToNodeAction())
        list.performScrollToNode(hasText("receiveContent 적용 TextField"))
        rule.onNodeWithText("텍스트 입력 또는 이미지 붙여넣기...").assertExists()
        rule.onNode(hasSetTextAction()).performTextInput("hi")
        rule.waitForIdle()
        rule.onNodeWithText("텍스트 입력 또는 이미지 붙여넣기...").assertDoesNotExist()
    }
}
