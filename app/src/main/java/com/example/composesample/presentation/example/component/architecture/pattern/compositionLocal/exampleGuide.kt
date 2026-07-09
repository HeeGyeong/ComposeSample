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
 *
 * ## StaticDynamicCompositionLocalExampleUI 참고 자료 (StaticDynamicCompositionLocalGuide.kt에서 이관)
 * - 출처: https://proandroiddev.com/jetpack-compose-static-vs-dynamic-compositionlocals-its-not-about-change-frequency-81f56b3dd991
 *
 * 핵심 개념:
 * - Static vs Dynamic 선택 기준은 "변경 빈도"가 아니라 "리컴포지션 전파 범위"의 차이
 * - staticCompositionLocalOf: 값 변경 시 Provider 이하 전체 하위 트리가 리컴포지션 (읽기 위치 추적 안 함)
 * - compositionLocalOf(Dynamic): 값 변경 시 실제로 .current를 읽는 컴포저블만 정밀하게 리컴포지션
 * - 조건부로 .current를 읽으면(if문 안에서만 호출) lazy subscription이 되어 불필요한 리컴포지션 방지
 * - 확신이 없으면 Dynamic을 기본으로 사용하고, 프로파일링 후 필요 시 Static으로 최적화 권장
 */
