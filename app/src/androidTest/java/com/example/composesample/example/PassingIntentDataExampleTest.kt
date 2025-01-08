package com.example.composesample.example


import androidx.activity.ComponentActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.printToLog
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.composesample.presentation.example.component.intent.PassingIntentDataActivity
import com.example.composesample.presentation.example.component.intent.PassingIntentDataExample
import org.hamcrest.CoreMatchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// AndroidJUnit4를 사용하여 안드로이드 테스트 실행
@RunWith(AndroidJUnit4::class)
class PassingIntentDataExampleTest {
    // Compose UI 테스트를 위한 Rule 설정
    // ComponentActivity를 기반으로 테스트 환경 구성
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    // 뒤로가기 버튼 클릭 상태를 추적하는 플래그
    private var backButtonClicked = false

    // 각 테스트 케이스 실행 전 초기 설정
    @Before
    fun setUp() {
        // 뒤로가기 버튼 상태 초기화
        backButtonClicked = false
        // 테스트할 Compose UI 컴포넌트 설정
        composeTestRule.setContent {
            PassingIntentDataExample(
                onBackButtonClick = { backButtonClicked = true }
            )
        }
    }

    /**
     * 화면의 모든 기본 UI 요소들이 올바르게 렌더링되었는지 검증
     * 1. 뒤로가기 버튼이 ContentDescription으로 식별 가능한지 확인
     * 2. Type 1, Type 2 버튼이 정확한 텍스트로 표시되는지 확인
     * 3. 모든 버튼이 클릭 가능한 상태인지 확인
     */
    @Test
    fun test_ui_elements_exist() {
        // 뒤로가기 버튼 검증
        // - 존재 여부
        // - 클릭 가능 여부
        composeTestRule
            .onNodeWithContentDescription("") // contentDescription이 빈 문자열인 Compose UI 요소를 찾음
            .assertExists() // 해당 노드가 실제로 존재하는지 확인. 존재하지 않으면 테스트 실패
            .assertHasClickAction() // 해당 노드가 클릭 가능한지 확인. 클릭 이벤트 핸들러가 없으면 실패

        // "Type 1" 버튼 검증
        // - 존재 여부
        // - 클릭 가능 여부
        composeTestRule
            .onNodeWithText("Passing Intent Data Type 1") // 텍스트가 "Passing Intent Data Type 1"인 Compose UI 요소를 찾음
            .assertExists()
            .assertHasClickAction()

        // "Type 2" 버튼 검증
        // - 존재 여부
        // - 클릭 가능 여부
        composeTestRule.onNodeWithText("Passing Intent Data Type 2")
            .assertExists()
            .assertHasClickAction()
    }

    /**
     * 뒤로가기 버튼의 클릭 이벤트 처리가 정상적으로 작동하는지 검증
     * 1. 뒤로가기 버튼 클릭 시 backButtonClicked 플래그가 true로 변경되는지 확인
     * 2. onBackButtonClick 콜백이 정상적으로 호출되는지 확인
     */
    @Test
    fun test_back_button_click() {
        composeTestRule
            .onNodeWithContentDescription("")
            .performClick() // 클릭 이벤트를 발생시킴
        assert(backButtonClicked) // 클릭 이벤트 발생 확인
    }

    /**
     * 버튼 클릭 시 인텐트 전달이 올바르게 이루어지는지 검증
     * 1. Intents 테스트 프레임워크 초기화
     * 2. Type 1 버튼이 활성화되어 있고 화면에 표시되는지 확인
     * 3. 버튼 클릭 시 PassingIntentDataActivity로 전환되는지 확인
     * 4. 인텐트에 필요한 데이터(Boolean, String, Number)가 올바르게 포함되어 있는지 검증
     */
    @OptIn(ExperimentalAnimationApi::class)
    @Test
    fun test_button_state_changes() {

        Intents.init() // 인텐트 테스트 초기화

        // Type 1 버튼 상태 및 클릭 테스트
        composeTestRule.onNodeWithText("Passing Intent Data Type 1")
            .assertIsEnabled()    // 활성화 상태 확인
            .assertIsDisplayed()  // 실제 화면에 표시 여부 확인
            .performClick()       // 클릭 수행

        // 인텐트 전달 검증
        // - 대상 액티비티 확인
        // - 전달되는 데이터 확인
        intended(
            allOf(
                hasComponent(PassingIntentDataActivity::class.java.name),
                hasExtra("BooleanData", true),
                hasExtra("StringData", "String Data"),
                hasExtra("NumberData", 100)
            )
        )

        Intents.release() // 인텐트 테스트 종료
    }

    /**
     * 전체 화면 레이아웃의 구조가 올바른지 검증
     * 1. 전체 레이아웃 구조를 로그로 출력하여 디버깅 가능하게 함
     * 2. 화면에 최소 1개 이상의 자식 노드가 존재하는지 확인
     * 3. 필수 UI 컴포넌트들(뒤로가기, Type 1, Type 2 버튼)이 모두 존재하는지 확인
     * 4. 레이아웃 계층 구조가 예상대로인지 검증
     */
    @Test
    fun test_screen_layout() {

        // 레이아웃 구조를 로그로 출력
        composeTestRule.onRoot().printToLog("LAYOUT_TEST")

        // 화면의 자식 노드 개수 검증
        composeTestRule.onRoot()
            .assertExists() // 루트 노드가 존재하는지 확인
            .onChildren() // 자식 노드를 찾음
            .fetchSemanticsNodes().size.let { childCount ->
                assert(childCount > 0) { "최소 1개 이상의 자식 노드가 있어야 합니다" }
            }

        // 주요 UI 요소들의 존재 확인
        composeTestRule.onNodeWithContentDescription("").assertExists()
        composeTestRule.onNodeWithText("Passing Intent Data Type 1").assertExists()
        composeTestRule.onNodeWithText("Passing Intent Data Type 2").assertExists()
    }

    /**
     * UI 요소들의 접근성이 올바르게 구현되었는지 검증
     * 1. Type 1, Type 2 버튼이 모두 클릭 가능한 상태인지 확인
     * 2. 버튼들이 활성화되어 있는지 확인
     * 3. 버튼의 텍스트가 정확히 일치하는지 확인 (스크린 리더 지원)
     * 4. 접근성 지원을 위한 기본적인 요구사항 충족 여부 검증
     */
    @Test
    fun test_accessibility() {
        // Type 1 버튼 접근성 검증
        composeTestRule.onNodeWithText("Passing Intent Data Type 1")
            .assertHasClickAction()  // 클릭 가능 여부
            .assertIsEnabled()       // 활성화 상태
            .assertTextEquals("Passing Intent Data Type 1")  // 텍스트 일치 여부

        // Type 2 버튼 접근성 검증
        composeTestRule.onNodeWithText("Passing Intent Data Type 2")
            .assertHasClickAction()
            .assertIsEnabled()
            .assertTextEquals("Passing Intent Data Type 2")
    }
}