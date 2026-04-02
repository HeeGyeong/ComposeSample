package com.example.composesample.presentation.example.component.architecture.pattern.compositionLocal

/**
 * CompositionLocal Example 참고 자료
 *
 * - 공식 문서: https://developer.android.com/develop/ui/compose/compositionlocal
 *
 * CompositionLocal이란:
 * - 컴포지션 트리를 통해 데이터를 암시적으로 전달하는 메커니즘
 * - prop drilling(파라미터를 깊게 전달하는 문제) 해결
 * - 내장 예시: LocalContext, LocalDensity, MaterialTheme.colorScheme
 *
 * compositionLocalOf vs staticCompositionLocalOf:
 * - compositionLocalOf   : 값 변경 시 current를 읽는 컴포저블만 리컴포지션
 *                          → 자주 바뀌는 값에 적합
 * - staticCompositionLocalOf: 값 변경 시 LocalProvider 전체 콘텐츠 리컴포지션
 *                              → 거의 바뀌지 않는 값에 적합 (테마, Dispatcher 등)
 *
 * CompositionLocalProvider 사용:
 * ```kotlin
 * CompositionLocalProvider(LocalMyValue provides myValue) {
 *     // 이 블록 안에서 LocalMyValue.current = myValue
 * }
 * ```
 *
 * 섀도잉(Shadowing):
 * - 중첩된 Provider에서 같은 Local을 재정의하면 안쪽 값이 우선
 * - 외부 값은 Provider 블록 밖에서만 접근 가능
 *
 * 주의사항:
 * - 너무 많이 사용하면 데이터 흐름 추적이 어려워짐
 * - ViewModel이나 파라미터 전달로 해결 가능한 경우 우선 고려
 * - 주로 UI 관련 설정(테마, 지역화, 접근성)에 적합
 */
