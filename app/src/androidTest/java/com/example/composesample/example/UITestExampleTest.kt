package com.example.composesample.example

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.composesample.presentation.example.component.architecture.development.test.UITestExampleUI
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * UITestExampleUI 는 "클릭 이벤트 3가지 작성 방법"을 나란히 보여주는 화면이라
 * 버튼 3개가 **하나의 text 상태를 공유**해 세 개 모두 같은 문구를 그린다.
 * 그래서 문구로 단일 노드를 고르려고 하면
 * "Expected exactly '1' node but found '3' nodes" 로 실패한다 — 화면 문제가 아니라 셀렉터 모호성이다.
 * 문구로 잡아야 하는 곳은 `onAllNodesWithText(...).onFirst()` 로 대상을 명시한다.
 * (세 번째 테스트처럼 testTag 로 잡을 수 있는 곳은 그쪽이 더 안전하다.)
 */
class UITestExampleTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setUp() {
        composeTestRule.setContent {
            UITestExampleUI(onBackEvent = { })
        }
    }

    /**
     * 첫 번째 테스트
     */
    @Test
    fun button_singleClick_changesTextToClicked() {
        // 초기 텍스트 확인 (버튼 3개가 같은 문구를 그리므로 첫 번째를 지정)
        composeTestRule.onAllNodesWithText("Hello").onFirst().assertExists()

        // 버튼 클릭
        composeTestRule.onAllNodesWithText("Hello").onFirst().performClick()

        // 변경된 텍스트 확인
        composeTestRule.onAllNodesWithText("Clicked!").onFirst().assertExists()
    }

    /**
     * 두 번째 테스트
     */
    @Test
    fun button_doubleClick_togglesTextState() {
        // 초기 텍스트 확인 (버튼 3개가 같은 문구를 그리므로 첫 번째를 지정)
        composeTestRule.onAllNodesWithText("Hello").onFirst().assertExists()

        // 버튼 클릭
        composeTestRule.onAllNodesWithText("Hello").onFirst().performClick()

        // 변경된 텍스트 확인
        composeTestRule.onAllNodesWithText("Clicked!").onFirst().assertExists()

        // 버튼 클릭
        composeTestRule.onAllNodesWithText("Clicked!").onFirst().performClick()

        // 변경된 텍스트 확인
        composeTestRule.onAllNodesWithText("Hello").onFirst().assertExists()
    }

    /**
     * 세 번째 테스트
     */
    @Test
    fun numbers_addTwoNumbers_showsCorrectSum() {
        // 첫 번째 숫자 입력
        composeTestRule.onNodeWithTag("firstNumber").performTextInput("10")

        // 두 번째 숫자 입력
        composeTestRule.onNodeWithTag("secondNumber").performTextInput("20")

        // 계산 버튼 클릭
        composeTestRule.onNodeWithTag("calculateButton").performClick()

        // 결과 확인
        composeTestRule.onNodeWithTag("resultText").assertTextEquals("결과: 30")
    }
}

/*
UI Test 함수 이름 패턴

// Given - When - Then 패턴
@Test
fun given_initialState_when_buttonClicked_then_textChangesToClicked()

@Test
fun given_clickedState_when_buttonClickedAgain_then_textReturnsToHello()

// should 패턴
@Test
fun shouldChangeTextToClickedWhenButtonIsPressed()

@Test
fun shouldToggleTextBetweenHelloAndClickedOnButtonPress()

// feature_scenario_expectedResult 패턴
@Test
fun button_singleClick_changesTextToClicked()

@Test
fun button_doubleClick_returnsToOriginalText()
 */