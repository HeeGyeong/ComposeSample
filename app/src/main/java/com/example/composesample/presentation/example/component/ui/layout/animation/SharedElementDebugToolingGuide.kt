package com.example.composesample.presentation.example.component.ui.layout.animation

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

/**
 * Shared Element Debug Tooling Guide
 *
 * 출처:
 * - 공식 공지: https://android-developers.googleblog.com/2026/04/jetpack-compose-april-2026-updates.html
 * - Compose 1.11 릴리스 노트: https://developer.android.com/jetpack/androidx/releases/compose-animation
 *
 * === 도입 배경 ===
 *
 * Compose 1.11 이전에는 SharedTransitionLayout 사용 시 잘못된 key 작명, 한쪽 화면에만 존재하는
 * sharedElement, 동일 key 중복 정의 같은 실수를 런타임에 즉시 발견하기 어려웠다.
 * 1.11 부터는 `LookaheadAnimationVisualDebugging` Composable 로 디버그 오버레이를 켜
 * 화면 위에서 직접 매칭 상태를 시각화할 수 있다.
 *
 * === 핵심 API (1.11) ===
 *
 * 1. @ExperimentalLookaheadAnimationVisualDebugApi
 *    - opt-in 어노테이션. 디버그 API 호출부에 모두 필요.
 *
 * 2. LookaheadAnimationVisualDebugging(
 *        isEnabled: Boolean,
 *        overlayColor: Color,
 *        multipleMatchesColor: Color,
 *        unmatchedElementColor: Color,
 *        isShowKeyLabelEnabled: Boolean,
 *        content: @Composable () -> Unit
 *    )
 *    - content 람다 내부의 SharedTransitionLayout 매칭 상태를 가로채 오버레이 렌더링.
 *    - 색상 3종 의미:
 *      · overlayColor: 정상 매칭된 sharedElement 영역.
 *      · multipleMatchesColor: 같은 key 가 둘 이상 발견된 충돌 요소.
 *      · unmatchedElementColor: 한쪽 상태에만 존재해 짝이 없는 요소.
 *
 * 3. CustomizedLookaheadAnimationVisualDebugging(color, content)
 *    - 단일 색상만 지정하여 빠르게 디버그.
 *
 * 4. LocalLookaheadAnimationVisualDebugConfig / LocalLookaheadAnimationVisualDebugColor
 *    - CompositionLocal 로 트리 하단에서 설정 조회 가능.
 *
 * 5. LookaheadAnimationVisualDebugConfig(isEnabled, overlayColor, multipleMatchesColor,
 *    unmatchedElementColor, isShowKeyLabelEnabled)
 *    - 디버그 설정값을 한 번에 묶어 전달하는 데이터 holder.
 *
 * === 사용 패턴 ===
 *
 * - 정상 매칭 한 케이스, 다중 매칭 한 케이스, 미매칭 한 케이스를 한 화면에 두고 디버그를
 *   토글하며 비교하면 색상 규칙이 즉시 이해된다.
 * - isShowKeyLabelEnabled = true 로 두면 매칭된 key 문자열이 라벨로 그려져, 디자이너가
 *   디자인 스펙과 함께 검토할 때도 유용하다.
 * - 프로덕션에서는 빌드 플래그(BuildConfig.DEBUG)와 결합해 디버그 빌드에서만 활성화.
 *
 * === Compose 1.11 SharedTransition Test Coroutine API ===
 *
 * runComposeUiTest 에서 mainClock.autoAdvance = false 로 둔 뒤 advanceTimeBy / awaitFrame
 * 로 sharedElement 전이의 중간 프레임을 검증할 수 있다. 이전에는 spring 종료 시점을 정확히
 * 짚기 어려웠으나 1.11 의 coroutine 친화 API 로 결정론적 검증이 가능해졌다.
 *
 * === 주의 ===
 *
 * - Experimental API 이므로 @OptIn(ExperimentalLookaheadAnimationVisualDebugApi::class) 필요.
 * - SharedTransitionLayout 의 부모로 LookaheadAnimationVisualDebugging 을 두어야 한다.
 *   (자식 SharedTransitionScope 내부에서 디버그 정보를 수집하기 때문)
 */
object SharedElementDebugToolingGuide {
    const val GUIDE_INFO = """
        Shared Element Debug Tooling - Compose 1.11

        핵심 API:
        - @ExperimentalLookaheadAnimationVisualDebugApi opt-in
        - LookaheadAnimationVisualDebugging(isEnabled, colors, isShowKeyLabelEnabled, content)
        - CustomizedLookaheadAnimationVisualDebugging(color, content)

        오버레이 색상 의미:
        - overlayColor          : 정상 매칭된 sharedElement
        - multipleMatchesColor  : 같은 key 가 둘 이상 충돌
        - unmatchedElementColor : 한쪽 상태에만 존재해 짝이 없음

        함께 도입된 항목:
        - SharedTransition test coroutine API (runComposeUiTest 안에서
          mainClock.advanceTimeBy / awaitFrame 로 결정론적 프레임 검증)
    """
}

@Preview
@Composable
fun SharedElementDebugToolingGuidePreview() {
    // 가이드 미리보기 placeholder
}
