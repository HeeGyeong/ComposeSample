package com.example.composesample.presentation.example.component.ui.style

/**
 * Foundation Style API 예제 참고 자료
 *
 * ## 개요
 * `androidx.compose.foundation.style` 는 Compose 1.11 에 실험 단계로 들어온 API 로,
 * **한 컴포넌트의 상태별 모양을 하나의 Style 객체에 선언**하고
 * `Modifier.styleable(state, style)` 로 붙이는 구조다.
 * CSS 의 `:hover` / `:active` / `:focus` / `:checked` 에 대응한다고 보면 된다.
 *
 * ## 서술 정정 이력 (중요)
 * 이 예제의 이전 버전은 Style API 를 "디자인 토큰(typography/colors/shapes/spacing)을
 * 단일 Immutable 객체로 묶어 하나의 CompositionLocal 로 전파하는 패턴"이라고 설명했다.
 * **그 설명은 실제 API 와 다르다.**
 * - 실제 API 의 축: 컴포넌트 단위의 상태별 스타일 + 선언적 전이
 * - 이전 설명의 축: 앱 전역 디자인 토큰 전파 (Style API 와 무관한 별개 패턴)
 *
 * 토큰 전파 패턴 자체는 유용하므로 삭제하지 않고 "부록: 디자인 토큰 전파 패턴" 카드로
 * 분리해 남겼고, 혼동을 막기 위해 타입 이름에서 Style 을 제거했다
 * (`AppStyle` → `AppTokens`, `LocalAppStyle` → `LocalAppTokens`, `StylePreset` → `TokenPreset`).
 *
 * ## API 표면 (1.11.1 기준, AAR javap + 실제 컴파일로 확인)
 * - 진입점: `Modifier.styleable(state: StyleState, style: Style)` /
 *   `styleable(state, vararg styles)`.
 *   `styleable(state)` 단독 오버로드는 **deprecated** (스타일이 없으면 효과가 없음).
 * - `Style { }` 은 `fun interface Style` 의 SAM 생성자이고, SAM 메서드가
 *   `StyleScope` 확장이라 **리시버 람다**로 쓴다 (`it.` 을 붙이면 컴파일 에러).
 * - `StyleScope` 는 `CompositionLocalAccessorScope` 와 `Density` 를 상속한다 →
 *   블록 안에서 `currentValueOf(SomeLocal)` 로 CompositionLocal 을 읽고 dp 변환도 된다.
 *   단, 컴포저블 함수는 호출할 수 없다 (컴포지션이 아니라 노드가 해석하는 람다).
 * - 선언 가능한 속성: contentPadding(내부 여백) / externalPadding(외부 여백) /
 *   border / size·min·max / alpha·scale·translation·rotation·transformOrigin·clip·zIndex /
 *   background·foreground·shape / dropShadow·innerShadow /
 *   텍스트 계열(textStyle·contentColor·fontSize·lineHeight·fontWeight·textAlign 등).
 * - 상태 변형 확장(top-level): `pressed` / `hovered` / `focused` / `checked` /
 *   `selected` / `disabled` / `triStateToggleOn·Off·Indeterminate`.
 * - 전이: `animate(style)` / `animate(spec, style)` / `animate(enterSpec, exitSpec, style)`.
 *   인자 1개짜리 spec 오버로드는 내부에서 `animate(spec, spec, style)` 로 위임한다.
 * - 커스텀 상태: `StyleStateKey<T>(defaultValue)` + `state(key, style) { key, state -> 판정 }`.
 * - 상태 소스: `rememberUpdatedStyleState(interactionSource) { block }`.
 *   블록은 `@Composable (MutableStyleState) -> Unit` 으로 **리시버가 아니라 `it`** 로 받는다.
 *
 * ## 동작상 알아 둘 점 (바이트코드 확인)
 * - `MutableStyleState` 의 초기 predefined 상태는 `Enabled` 비트만 켜져 있다
 *   (= `isEnabled` 기본 true, 나머지 false).
 * - 포인터 상태는 `styleable` 노드가 `StyleState` 의 InteractionSource 를 직접 수집해
 *   갱신한다. `rememberUpdatedStyleState` 자체는 코루틴을 띄우지 않는다.
 *   즉 같은 InteractionSource 를 `clickable`/`hoverable`/`focusable` 에 넘기면 연결된다.
 * - `MutableStyleState.processInteractions` 가 다루는 것은
 *   Press(Press/Release/Cancel) · Hover · Focus 계열이다.
 * - **상태 변형에는 CSS 같은 특이도(specificity)가 없다.** 조건이 참인 블록을
 *   선언된 순서대로 그 자리에서 적용하므로, 같은 속성을 여러 상태가 건드리면
 *   마지막에 선언한 값이 최종값이다.
 *
 * ## 참고 링크
 * - Compose Foundation 릴리즈 노트:
 *   https://developer.android.com/jetpack/androidx/releases/compose-foundation
 * - Jetpack Compose April '26 업데이트(1.11 실험 API 소개):
 *   https://android-developers.googleblog.com/2026/04/jetpack-compose-april-2026-updates.html
 * - Style API 소개 글:
 *   https://simtop.medium.com/compose-styling-is-changing-heres-what-google-s-new-style-api-gets-right-9cb52f5065ef
 *
 * ## 본 예제의 검증 범위
 * - 빌드 · 경고 · 단위테스트까지 확인. **실기기/에뮬레이터 미실행.**
 * - 특히 6번 카드(리컴포지션 대조)는 "styleable 은 컴포지션을 깨우지 않는다"는 가설을
 *   화면의 카운터로 직접 재도록 만든 것이며, 저자가 런타임으로 확인한 결과는 아니다.
 * - 텍스트 계열 속성(textStyle/contentColor 등)이 Material3 `Text` 까지
 *   전파되는지도 런타임 미검증이라 실동작 데모에서는 사용하지 않았다.
 *
 * ## 프로덕션 적용 시 주의
 * - 전부 `@OptIn(ExperimentalFoundationStyleApi::class)` 가 필요하며 1.11 단계에서는
 *   네이밍/시그니처가 바뀔 수 있다.
 * - `Style` 객체는 값 객체이므로 `remember` 로 유지한다. 매 컴포지션마다 새로 만들면
 *   노드가 매번 스타일을 다시 해석한다.
 */
