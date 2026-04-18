package com.example.composesample.presentation.example.component.architecture.development.test

/**
 * Catching Excessive Recompositions in Jetpack Compose with Tests
 *
 * 출처: https://proandroiddev.com/catching-excessive-recompositions-in-jetpack-compose-with-tests-8d0b952e2853
 *
 * === 개요 ===
 *
 * Jetpack Compose에서 과도한 Recomposition은 성능 저하의 주요 원인입니다.
 * 이 가이드는 테스트를 통해 불필요한 Recomposition을 감지하고 방지하는 방법을 다룹니다.
 *
 * === Recomposition이란? ===
 *
 * Recomposition은 Compose가 상태 변경에 따라 UI를 다시 그리는 과정입니다.
 * - 효율적인 Recomposition: 변경된 부분만 다시 그림
 * - 과도한 Recomposition: 불필요하게 많은 컴포저블이 다시 그려짐
 *
 * === 과도한 Recomposition의 원인 ===
 *
 * 1. 불안정한(Unstable) 타입 사용
 *    - List, Map 등 컬렉션 타입
 *    - data class가 아닌 클래스
 *    - 가변(var) 프로퍼티가 있는 클래스
 *
 * 2. 람다 캡처 문제
 *    - 매번 새로운 람다 인스턴스 생성
 *    - remember로 감싸지 않은 람다
 *
 * 3. 비효율적인 상태 관리
 *    - 너무 높은 레벨에서 상태 관리
 *    - 불필요한 상태 호이스팅
 *
 * === RecompositionCounter 구현 ===
 *
 * 테스트에서 Recomposition 횟수를 추적하기 위한 유틸리티:
 *
 * ```kotlin
 * class RecompositionCounter {
 *     private var count = 0
 *
 *     fun increment() {
 *         count++
 *     }
 *
 *     fun getCount(): Int = count
 *
 *     fun reset() {
 *         count = 0
 *     }
 * }
 *
 * @Composable
 * fun TrackRecomposition(counter: RecompositionCounter) {
 *     SideEffect {
 *         counter.increment()
 *     }
 * }
 * ```
 *
 * === 테스트 작성 방법 ===
 *
 * ```kotlin
 * @get:Rule
 * val composeTestRule = createComposeRule()
 *
 * @Test
 * fun testRecompositionCount() {
 *     val counter = RecompositionCounter()
 *
 *     composeTestRule.setContent {
 *         var state by remember { mutableStateOf(0) }
 *
 *         MyComposable(
 *             value = state,
 *             onValueChange = { state = it }
 *         )
 *
 *         TrackRecomposition(counter)
 *     }
 *
 *     // 초기 컴포지션
 *     assertEquals(1, counter.getCount())
 *
 *     // 상태 변경
 *     composeTestRule.onNodeWithTag("button").performClick()
 *
 *     // Recomposition 횟수 검증
 *     composeTestRule.waitForIdle()
 *     assertEquals(2, counter.getCount())
 * }
 * ```
 *
 * === Stability 어노테이션 ===
 *
 * 1. @Stable
 *    - 객체가 변경되지 않음을 보장
 *    - Compose가 스마트 리컴포지션 수행 가능
 *
 * ```kotlin
 * @Stable
 * data class UserState(
 *     val name: String,
 *     val age: Int
 * )
 * ```
 *
 * 2. @Immutable
 *    - 객체가 완전히 불변임을 보장
 *    - @Stable보다 강한 보장
 *
 * ```kotlin
 * @Immutable
 * data class Config(
 *     val theme: String,
 *     val language: String
 * )
 * ```
 *
 * === 최적화 기법 ===
 *
 * 1. remember 사용
 * ```kotlin
 * // Bad
 * @Composable
 * fun BadExample(items: List<String>) {
 *     val sortedItems = items.sorted() // 매번 새 리스트 생성
 *     // ...
 * }
 *
 * // Good
 * @Composable
 * fun GoodExample(items: List<String>) {
 *     val sortedItems = remember(items) { items.sorted() }
 *     // ...
 * }
 * ```
 *
 * 2. derivedStateOf 사용
 * ```kotlin
 * @Composable
 * fun SearchExample(query: String, items: List<String>) {
 *     val filteredItems by remember(query, items) {
 *         derivedStateOf {
 *             items.filter { it.contains(query) }
 *         }
 *     }
 * }
 * ```
 *
 * 3. 람다 안정화
 * ```kotlin
 * // Bad
 * @Composable
 * fun BadLambda(onClick: () -> Unit) {
 *     Button(onClick = { onClick() }) // 매번 새 람다
 * }
 *
 * // Good
 * @Composable
 * fun GoodLambda(onClick: () -> Unit) {
 *     val stableOnClick = rememberUpdatedState(onClick)
 *     Button(onClick = { stableOnClick.value() })
 * }
 * ```
 *
 * 4. key 사용
 * ```kotlin
 * LazyColumn {
 *     items(
 *         items = list,
 *         key = { item -> item.id } // 안정적인 키 제공
 *     ) { item ->
 *         ItemRow(item)
 *     }
 * }
 * ```
 *
 * === 디버깅 도구 ===
 *
 * 1. Layout Inspector
 *    - Android Studio의 Layout Inspector 사용
 *    - Recomposition 횟수 시각화
 *
 * 2. Composition Tracing
 *    - System Trace에서 Composition 이벤트 확인
 *
 * 3. 커스텀 로깅
 * ```kotlin
 * @Composable
 * fun DebugRecomposition(tag: String) {
 *     SideEffect {
 *         Log.d("Recomposition", "$tag recomposed")
 *     }
 * }
 * ```
 *
 * === 테스트 모범 사례 ===
 *
 * 1. 단위 테스트
 *    - 개별 컴포저블의 Recomposition 횟수 검증
 *    - 상태 변경에 따른 예상 횟수 확인
 *
 * 2. 통합 테스트
 *    - 여러 컴포저블 간의 상호작용 검증
 *    - 실제 사용 시나리오 시뮬레이션
 *
 * 3. 성능 회귀 테스트
 *    - CI/CD에 Recomposition 테스트 포함
 *    - 임계값 초과 시 빌드 실패
 *
 * === 요약 ===
 *
 * 1. RecompositionCounter로 횟수 추적
 * 2. SideEffect로 Recomposition 감지
 * 3. @Stable/@Immutable로 안정성 보장
 * 4. remember/derivedStateOf로 계산 캐싱
 * 5. 테스트로 성능 회귀 방지
 */

object RecompositionTestGuide {
    const val GUIDE_INFO = """
        Catching Excessive Recompositions with Tests
        
        핵심 개념:
        - RecompositionCounter: Recomposition 횟수 추적
        - SideEffect: Recomposition 감지
        - @Stable/@Immutable: 안정성 보장
        
        최적화 기법:
        - remember: 값 캐싱
        - derivedStateOf: 파생 상태
        - rememberUpdatedState: 람다 안정화
        - key: LazyColumn 최적화
        
        테스트 방법:
        - 초기 컴포지션 횟수 검증
        - 상태 변경 후 횟수 검증
        - 예상치 초과 시 실패
        
        출처: https://proandroiddev.com/catching-excessive-recompositions-in-jetpack-compose-with-tests-8d0b952e2853
    """
}

