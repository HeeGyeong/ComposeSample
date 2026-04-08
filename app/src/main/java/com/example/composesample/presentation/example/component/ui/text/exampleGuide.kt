package com.example.composesample.presentation.example.component.ui.text

/**
 * Text 예제 참고 자료
 *
 * ## TextFieldMaxLengthExampleUI (Max Length 숨겨진 버그)
 * - 출처: https://hackernoon.com/a-hidden-problem-in-jetpack-compose-textfield-max-length
 *
 * ### 문제의 본질
 * - 새로운 BasicTextField(TextFieldState API)에서 `InputTransformation.maxLength(N)`은
 *   IME/키보드 입력 파이프라인에만 훅(hook)되어 있어, 코드에서 `state.edit { ... }`로
 *   직접 상태를 변경하면 길이 제한이 적용되지 않음
 * - 결과적으로 API 응답을 state에 주입하거나, 뷰모델에서 상태를 세팅할 때
 *   길이 제한 불변식이 깨짐
 *
 * ### 올바른 해결 패턴
 * ```
 * LaunchedEffect(state) {
 *     snapshotFlow { state.text.toString() }
 *         .collectLatest { current ->
 *             if (current.length > MAX) {
 *                 state.edit { replace(MAX, length, "") }
 *             }
 *         }
 * }
 * ```
 * - `snapshotFlow { state.text }` — 상태 변경 경로와 무관하게 관찰 가능
 * - `collectLatest` — 연속 변경 시 이전 재진입 작업을 취소
 * - 불변식(invariant) 강제는 UI 변환이 아닌 상태 관찰 레이어가 담당
 *
 * ## RichContentTextInputExampleUI (리치 콘텐츠 수신)
 * - 공식 문서: https://developer.android.com/jetpack/compose/text/receive-content
 * 핵심 개념:
 * - `contentReceiver` modifier: TextField에 이미지/파일 붙여넣기 처리
 * - 세 가지 출처: 키보드(IME), 클립보드, 드래그&드롭
 * - `TransferableContent.consume { predicate }`: true 반환 아이템만 소비, false는 기본 텍스트 처리로 위임
 * - API 33+ 필요
 *
 * ## LocalContextStringsExampleUI (LocalContext 안티패턴)
 * - 핵심: Compose에서 문자열 리소스는 `stringResource()` 또는 UiText sealed class 패턴 사용
 * - LocalContext를 직접 사용하면 프리뷰/테스트 환경에서 문제 발생 가능
 *
 * ## AutoSizingTextExampleUI / CustomTextRenderingExampleUI / TextStyleUI
 * - 각 예제 파일 내 주석 참고
 */
