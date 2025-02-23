package com.example.composesample.example

import android.content.pm.ActivityInfo
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.composesample.presentation.example.BlogExampleActivity
import com.example.composesample.presentation.example.component.init.InitTestExampleUI
import com.example.composesample.presentation.example.component.init.InitTestViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * InitTestExampleUITest Class를 실행하는 경우,
 * 이하의 @Test 함수는 순차적으로 실행되지 않는다.
 */
@RunWith(AndroidJUnit4::class)
class InitTestExampleUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var viewModel: InitTestViewModel

    // @Test가 실행되기 전에 반드시 실행된다
    @Before
    fun setUp() {
        // ViewModel 초기화
        viewModel = InitTestViewModel()
    }

    // UI가 제대로 그려지는지 확인
    @Test
    fun testUIElements() {
        // Given
        var backButtonClicked = false

        // When
        composeTestRule.setContent {
            InitTestExampleUI(onBackButtonClick = { backButtonClicked = true })
        }

        composeTestRule.onNodeWithContentDescription("").performClick()

        // Then
        // 헤더 타이틀 확인
        composeTestRule.onNodeWithText("Init Test Example").assertExists()

        // 뒤로가기 버튼 확인
        composeTestRule.onNodeWithContentDescription("").assertExists()
        composeTestRule.onNodeWithContentDescription("").assertHasClickAction()

        // 버튼 클릭 여부 확인
        assert(backButtonClicked) { "Back button click was not registered" }
    }

    @OptIn(ExperimentalAnimationApi::class)
    @Test
    fun testInitLoadingFlag() = runTest {
        // Given
        viewModel.changeLaunchedEffectLoading()

        // When
        composeTestRule.setContent {
            InitTestExampleUI(onBackButtonClick = {})
        }
        // isInitLoading을 구독하여 true로 설정
        viewModel.isInitLoading.first { it }

        // ActivityScenario를 사용하여 화면 회전
        val scenario = ActivityScenario.launch(BlogExampleActivity::class.java)
        scenario.onActivity { activity ->
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }

        Thread.sleep(2000)

        // Then
        // StateFlow 값 검증
        assert(viewModel.isLaunchedEffectLoading.value) { "LaunchedEffectLoading should be true" }
        assert(viewModel.isViewModelInitLoading.value) { "ViewModelInitLoading should be true" }
        assert(viewModel.isInitLoading.value) { "InitLoading should be true" }

        // isTestLoadingCount 값 검증
        assert(viewModel.isTestLoadingCount.value == 3) { "TestLoadingCount should be 3" }

        testInitLoadingFlag2()
    }

    private fun testInitLoadingFlag2() = runTest {
        // Given
        viewModel.changeLaunchedEffectLoading() // +1
        viewModel.isInitLoading.first { it } // collect가 해제되지 않으므로 동작 X

        // Then
        assert(viewModel.isTestLoadingCount.value == 4) { "TestLoadingCount should be 4" }

        testInitLoadingFlag2_1()
    }

    private fun testInitLoadingFlag2_1() = runTest {
        viewModel = InitTestViewModel() // viewModel 초기화. +1

        // Given
        viewModel.changeLaunchedEffectLoading() // +1
        viewModel.isInitLoading.first { it } // +1

        // Then
        assert(viewModel.isTestLoadingCount.value == 3) { "TestLoadingCount should be 3" }
    }

    @Test
    fun testInitLoadingFlag3() = runTest {
        // Given
        viewModel.changeLaunchedEffectLoading()

        // When
        composeTestRule.setContent {
            InitTestExampleUI(onBackButtonClick = {})
        }
        viewModel.isInitLoading.first { it }

        // Then
        assert(viewModel.isTestLoadingCount.value == 3) { "TestLoadingCount should be 3" }
    }
}
