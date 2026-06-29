package com.example.composesample.presentation.example

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.AnimationConstants
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.example.component.navigation.BottomNavigationActivity
import com.example.composesample.presentation.example.model.ExampleMoveType
import com.example.composesample.util.ConstValue.BottomNavigationExample

/**
 * 예제 라우터.
 *
 * UTIL-01 리팩터: 146분기 when 모놀리식을 [exampleUiRegistry] (ExampleUiRegistry.kt) 로 분리.
 * 이 함수는 이동 타입(UI / ACTIVITY / EMPTY) 분기와 레지스트리 조회만 담당한다.
 * - UI: 레지스트리에서 onBackEvent 디스패처를 찾아 실행, 없으면 Dummy 폴백(기존 동작 보존)
 * - ACTIVITY: BottomNavigation 등 Activity 시작 후 상태 초기화
 * - EMPTY: 표시할 화면 없음
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ExampleCaseUI(
    exampleType: MutableState<String>,
    exampleMoveType: MutableState<ExampleMoveType>,
    onBackEvent: () -> Unit
) {
    val context = LocalContext.current

    when (exampleMoveType.value) {
        ExampleMoveType.UI -> {
            AnimatedVisibility(
                visible = exampleType.value.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut(animationSpec = tween(durationMillis = AnimationConstants.DefaultDurationMillis / 2))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = Color.DarkGray)
                        .animateEnterExit(
                            enter = slideInVertically(
                                initialOffsetY = { height -> height },
                                animationSpec = tween()
                            ),
                            exit = slideOutVertically(
                                targetOffsetY = { height -> height },
                                animationSpec = tween(durationMillis = AnimationConstants.DefaultDurationMillis / 2)
                            )
                        )
                ) {
                    // UTIL-01: 146분기 when -> 레지스트리(exampleUiRegistry) 조회로 대체.
                    // 등록된 예제면 해당 UI 디스패처를, 없으면 기존과 동일하게 Dummy 폴백.
                    val exampleContent = exampleUiRegistry[exampleType.value]
                    if (exampleContent != null) {
                        exampleContent(onBackEvent)
                    } else {
                        Text(
                            text = "Dummy",
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                        )
                    }
                }
            }
        }

        ExampleMoveType.ACTIVITY -> {
            when (exampleType.value) {
                BottomNavigationExample -> {
                    val intent =
                        Intent(
                            context,
                            BottomNavigationActivity::class.java
                        ).apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP }
                    context.startActivity(intent)
                }

                else -> {}
            }

            exampleMoveType.value = ExampleMoveType.EMPTY
        }

        ExampleMoveType.EMPTY -> {}
    }
}
