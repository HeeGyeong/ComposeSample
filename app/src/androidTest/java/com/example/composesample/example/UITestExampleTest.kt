package com.example.composesample.example

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.composesample.presentation.example.component.test.UITestExampleUI
import org.junit.Before
import org.junit.Rule
import org.junit.Test

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
        // 초기 텍스트 확인
        composeTestRule.onNodeWithText("Hello").assertExists()

        // 버튼 클릭
        composeTestRule.onNodeWithText("Hello").performClick()

        // 변경된 텍스트 확인
        composeTestRule.onNodeWithText("Clicked!").assertExists()
    }

    /**
     * 두 번째 테스트
     */
    @Test
    fun button_doubleClick_togglesTextState() {
        // 초기 텍스트 확인
        composeTestRule.onNodeWithText("Hello").assertExists()

        // 버튼 클릭
        composeTestRule.onNodeWithText("Hello").performClick()

        // 변경된 텍스트 확인
        composeTestRule.onNodeWithText("Clicked!").assertExists()

        // 버튼 클릭
        composeTestRule.onNodeWithText("Clicked!").performClick()

        // 변경된 텍스트 확인
        composeTestRule.onNodeWithText("Hello").assertExists()
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