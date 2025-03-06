package com.example.composesample.example

import android.content.pm.ActivityInfo
import android.util.Log
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
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
        Log.d("TAG", "setUp Start")
        viewModel = InitTestViewModel()
        Log.d("TAG", "setUp Fin")
    }

    // 초기 UI 요소들이 제대로 존재하는지 검증
    @Test
    fun verify_initial_ui_elements_exist() {
        // When
        composeTestRule.setContent {
            InitTestExampleUI(onBackButtonClick = {})
        }

        // Then
        composeTestRule.onNodeWithText("Init Test Example")
            .assertExists()
            .assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription("Header_back_button")
            .assertExists()
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    // 뒤로가기 버튼 클릭 시 콜백이 정상적으로 동작하는지 검증
    @Test
    fun verify_back_button_triggers_callback() {
        // Given
        var backButtonClicked = false

        // When
        composeTestRule.setContent {
            InitTestExampleUI(onBackButtonClick = { backButtonClicked = true })
        }

        composeTestRule.onNodeWithContentDescription("Header_back_button")
            .performClick()

        // Then
        assert(backButtonClicked) { "Back button click should trigger callback" }
    }

    // LaunchedEffect가 트리거될 때 모든 로딩 상태를 검증
    @Test
    fun verify_all_loading_states_when_launched_effect_triggered() = runTest {
        // Given
        viewModel.changeLaunchedEffectLoading()

        // When
        composeTestRule.setContent {
            InitTestExampleUI(onBackButtonClick = {})
        }
        // isInitLoading을 구독하여 true로 설정
        viewModel.isInitLoading.first { it }

        // Then
        // StateFlow 값 검증
        assert(viewModel.isLaunchedEffectLoading.value) { "LaunchedEffectLoading should be true" }
        assert(viewModel.isViewModelInitLoading.value) { "ViewModelInitLoading should be true" }
        assert(viewModel.isInitLoading.value) { "InitLoading should be true" }

        // isTestLoadingCount 값 검증
        assert(viewModel.testLoadingCount.value == 3) { "TestLoadingCount should be 3" }

        verify_loading_count_increments_on_subsequent_trigger()
    }

    // test 작성 시, test 함수를 체이닝해서 사용하는 것은 좋지 않다. 하지만, 데이터 확인을 위해 추가하였다.
    // 후속 트리거 시 로딩 카운트가 증가하는지 검증
    private fun verify_loading_count_increments_on_subsequent_trigger() = runTest {
        // Given
        viewModel.changeLaunchedEffectLoading() // +1
        viewModel.isInitLoading.first { it } // collect가 해제되지 않으므로 동작 X

        // Then
        assert(viewModel.testLoadingCount.value == 4) { "TestLoadingCount should be 4" }

        verify_loading_count_after_viewmodel_reset()
    }

    // ViewModel 리셋 후 로딩 카운트를 검증
    private fun verify_loading_count_after_viewmodel_reset() = runTest {
        // Given
        val newViewModel = InitTestViewModel()

        // When
        newViewModel.changeLaunchedEffectLoading()
        newViewModel.isInitLoading.first { it }

        // Then
        assert(newViewModel.testLoadingCount.value == 3) { "Loading count should be 3 after ViewModel reset" }
    }

    // 화면 회전 후에도 로딩 상태가 유지되는지 검증
    @OptIn(ExperimentalAnimationApi::class)
    @Test
    fun verify_loading_state_persists_after_rotation() = runTest {
        // Given
        composeTestRule.setContent {
            InitTestExampleUI(onBackButtonClick = {})
        }
        viewModel.changeLaunchedEffectLoading()
        viewModel.isInitLoading.first { it }

        // When
        ActivityScenario.launch(BlogExampleActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }

            // StateFlow 값이 업데이트될 때까지 대기
            viewModel.isInitLoading.first { it }

            // Then
            assert(viewModel.isLaunchedEffectLoading.value) { "LaunchedEffect loading should remain true after rotation" }
            assert(viewModel.testLoadingCount.value == 3) { "Loading count should be 3 after rotation" }
        }
    }

    // 새로운 트리거 발생 시 로딩 카운트가 일관성있게 유지되는지 검증
    @Test
    fun verify_loading_count_remains_consistent_on_new_trigger() = runTest {
        // Given
        viewModel.changeLaunchedEffectLoading()

        // When
        composeTestRule.setContent {
            InitTestExampleUI(onBackButtonClick = {})
        }
        viewModel.isInitLoading.first { it }

        // Then
        assert(viewModel.testLoadingCount.value == 3) { "TestLoadingCount should be 3" }
    }
}
