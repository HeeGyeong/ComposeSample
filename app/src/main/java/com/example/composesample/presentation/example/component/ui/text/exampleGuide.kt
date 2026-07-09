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
 * ## DocumentEditingTextFieldExampleUI (문서 편집 수준 패턴)
 * - 출처: https://medium.com/proandroiddev/when-text-input-becomes-document-editing-in-jetpack-compose-fa90be6ff013
 *
 * 핵심 개념:
 * - `state.undoState.undo() / redo()`: TextFieldState에 내장된 편집 이력. 키보드 입력 + edit{} 변경이 동일 히스토리에 누적
 * - `state.selection: TextRange`: collapsed=커서 단일 위치, !collapsed=범위 선택. edit{} 안에서 selection 직접 대입으로 제어
 * - `edit { replace(start, end, str) }`: mutating buffer 조작. substring은 외부에서 미리 추출(buffer 내 인덱싱은 시점 의존적)
 * - AnnotatedString 미리보기: 입력은 평문 유지, snapshotFlow로 관찰하여 별도 영역에 마크다운 토큰을 SpanStyle로 렌더링
 * - 멀티 커서 시뮬레이션: 오프셋을 sortedDescending 순으로 삽입해야 인덱스 시프트로 깨지지 않음
 *
 * ## SyntaxHighlightingExampleUI (간소화 데모)
 * - 출처: https://hossain.dev/posts/syntax-highlighting-on-android-bringing-shiki-engine-to-compose/
 *
 * 핵심 개념:
 * - 우선순위 정규식 토크나이저: 패턴을 순서대로 매칭하고, 이미 매칭된 영역(BooleanArray)은 후순위 패턴이 침범하지 못하도록 함
 *   → 주석/문자열 안 키워드가 잘못 강조되는 문제 회피
 * - AnnotatedString.Builder + addStyle(SpanStyle(color)) 로 토큰별 색상 적용
 * - 라이브 편집은 입력 평문 + 별도 미리보기 영역으로 분리, snapshotFlow { state.text }로 갱신
 * - 한계: 컨텍스트 무지(중첩 보간/타입 인자 등 정확 분석 불가). 본격 엔진은 Shiki(TextMate Grammar) / TreeSitter
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
 * - 출처: https://proandroiddev.com/jetpack-compose-why-you-shouldnt-use-localcontext-for-strings-4d4c372b14ab
 * - 핵심: Compose에서 문자열 리소스는 `stringResource()` 또는 UiText sealed class 패턴 사용
 * - LocalContext를 직접 사용하면 프리뷰/테스트 환경에서 문제 발생 가능(Preview 미동작·locale 변경 미반영)
 * - ViewModel에서는 UiText sealed class(DynamicString/StringResource)로 감싸고, UI 레이어에서만 stringResource() 호출
 *
 * ## CustomTextRenderingExampleUI (TextMeasurer 기반 커스텀 텍스트 효과)
 * - 출처: https://segunfamisa.com/posts/exploring-custom-text-rendering-in-compose
 *
 * ### 핵심 개념
 * - TextMeasurer.measure() + Canvas.drawText()로 저수준 텍스트 렌더링 (FadedText/WarpedText/TypewriterText 등)
 * - TextLayoutResult: lineCount/getBoundingBox/getLineLeft·Right·Top·Bottom으로 라인·문자 단위 제어
 * - withTransform { translate/rotate/scale }으로 문자별 웨이브·흔들림 효과 적용
 * - TextLayoutResult는 remember로 캐싱 필수, 컨테이너 제약은 BoxWithConstraints로 전달
 *
 * ## AutoSizingTextExampleUI (BasicText autoSize)
 *
 * ### 핵심 개념
 * - Compose BOM 2025.04.01+의 `BasicText(autoSize = TextAutoSize.StepBased(...))`로 컨테이너에 맞춰 폰트 크기 자동 조절
 * - maxFontSize/minFontSize로 범위 제한, minFontSize 도달 후에도 넘치면 TextOverflow.Ellipsis로 처리
 * - softWrap/maxLines와 조합 가능, onTextLayout 콜백으로 실측 크기·라인 수 확인
 * - 반드시 명확한 크기 제약이 있는 컨테이너에서만 동작(width/height 무제한이면 미작동)
 *
 * ## TextStyleUI
 * - 각 예제 파일 내 주석 참고
 */
