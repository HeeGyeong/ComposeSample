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
 * ### 서식 편집(Formatted Text) — 5~9번 카드(#164 보강, foundation 1.12.1)
 * - 참고: Arda Kazancı, Building Reliable Text Editing in Compose(Android Weekly #753):
 *   https://ardakazanci.com/posts/building-reliable-text-editing-in-compose/
 * - API: https://developer.android.com/reference/kotlin/androidx/compose/foundation/text/input/TextFieldBuffer
 * - 릴리스 노트: https://developer.android.com/jetpack/androidx/releases/compose-foundation
 *   (1.11.0-alpha01: addStyle 이 OutputTransformation 전용으로 TextFieldBuffer 에 · 1.12.0-beta01: TrackedRange 수정 항목 ·
 *   1.13.0-alpha01: "TextFieldState now saves and restores text style information")
 * - 1.13 이 아니라 지금 쓰는 1.12.1 에 있다: addStyle(SpanStyle|ParagraphStyle, …) · getSpanStyles · removeStyle ·
 *   ExpandPolicy · TextFieldTextStyles 가 aar 에 있고 opt-in 이 없다(javap + 컴파일로 확인). 원문 코드의 `span.range`·`span.start`
 *   는 1.12.1 에 없다 — TrackedRange 의 범위는 버퍼 안 확장 프로퍼티 `textRange`(그 밖에 expandPolicy · spanStyle · isValid).
 *
 * 핵심 개념(실측: JVM 단위 테스트 · Pixel 4 API 30 에뮬 debug · release 같은 결과):
 * - 쓰기는 `state.edit { addStyle(style, TextRange, ExpandPolicy) }`, 읽기는 `state.textStyles`(읽기 전용).
 *   `state.text` 는 서식 없는 String. 정책 없는 addStyle(style, start, end) 는 AtEnd. 길이 0 범위는 예외 없이 무시된다.
 * - 조회 결과는 붙인 순서다(위치 순 아님) — 뒤에서부터 붙이면 뒤가 먼저 나온다. 화면 표시는 위치 순으로 정렬했다.
 * - `ComposeFoundationFlags.isBasicTextFieldStyledTextEnabled` 기본 true(1.12.1) — 끄면 addStyle 은 OutputTransformation 안에서만.
 *   전역 플래그라 예제에서는 바꾸지 않는다.
 * - ExpandPolicy: 안쪽 입력은 늘 포함, 경계 입력만 갈린다(InsideOnly 둘 다 제외 · AtStart 앞 · AtEnd 뒤 · AtBoth 둘 다).
 *   키 입력도 같다 — 끝 경계에 친 'Z' 가 AtEnd 밑줄은 [0,7)→[0,8) 로 늘리고 InsideOnly 굵게는 그대로 뒀다.
 * - 범위 길이가 0 이 되면 서식이 사라진다 — 같은 자리에 다시 쳐도 서식 없음.
 * - undo/redo 는 살아 있는 서식의 범위만 함께 되돌린다(일부 삭제 → undo 로 [0,5) 복원, 끝 경계 입력 undo/redo).
 *   사라진 서식은 다시 만들지 못한다: 붙이기를 undo 한 뒤 redo · 전부 지운 글자를 undo(글자만 돌아옴) · 글자와 서식을
 *   한 edit 에서 넣은 것을 redo(글자만). 툴바의 Undo → Redo 로도 그대로 보인다.
 * - InputTransformation 안 addStyle 은 상태에 남는다(입력 중 별표 두 개 쌍 → 별표 삭제 + 굵게 InsideOnly, 정규식은 한 번만 컴파일).
 *   OutputTransformation 안 addStyle 은 그리기만 — 노랗게 칠해져도 textStyles 는 비어 있다.
 * - 저장: 1.12.1 `TextFieldState.Saver` 는 [텍스트, 선택, undo 이력] 만 저장한다(항목 4). 다크 모드 재생성 · am kill 뒤 복원
 *   모두 서식만 사라지고 선택은 남는다. undo 이력도 남아 canUndo=true 지만 되돌려도 보이는 변화는 없다.
 *   서식까지 지키려면 범위를 저장하는 Saver(FormattedTextStateSaver: 종류 · 시작 · 끝) — ExpandPolicy 는 textStyles 로 못 읽어
 *   AtEnd 로 되살리고, 복원하며 붙인 서식이 undo 대상이 되지 않게 이력을 비운다.
 * - FormattedTextLabTest 의 undo · Saver 테스트는 1.12.1 동작을 고정한다 — 1.13 상향 때 실패하면 7 · 9번 카드 문구를 다시 잰다.
 * - **Compose 1.13.0 상향(2026-11-20, BOM 2026.11.01 — DEP-CURRENCY-35)**: 예고대로 Saver 테스트 하나만 실패했다 —
 *   기본 TextFieldState.Saver 가 [텍스트, 선택 시작, 선택 끝, undo, 서식(TextStyleBuffer)] 5항목을 저장해 서식이 남는다.
 *   JVM 프로브: InsideOnly 굵게 · AtEnd 빨강을 붙여 저장 → 복원하면 둘 다 남고, 경계에 친 글자는 정책대로(InsideOnly 그대로 ·
 *   AtEnd 늘어남) — ExpandPolicy 까지 되살린다(직접 만든 Saver 는 AtEnd 로만). 같은 프로브에서 복원 뒤 친 글자를 undo 하면
 *   글자는 돌아오고 서식은 둘 다 사라졌다(복원 전 상태에서의 undo 는 살아 있는 서식을 지킨다 — 7번 카드). 9번 카드 제목 · 설명 ·
 *   요약을 1.13 동작으로 고치고 테스트를 1.13 기대값 + ExpandPolicy 테스트로 바꿨다. undo 시나리오(7번 카드) 테스트는 그대로 통과
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
 * - 공식 문서(Modifier.contentReceiver): https://developer.android.com/develop/ui/compose/touch-input/copy-and-paste#paste_a_rich_content
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
 * ## TextStyleExampleUI
 * - 각 예제 파일 내 주석 참고
  *
 * ## Text Selection Control (읽기 전용 텍스트 선택 + 컨텍스트 메뉴)
 * - 공식 문서: https://developer.android.com/develop/ui/compose/text/user-interactions
 * - API: https://developer.android.com/reference/kotlin/androidx/compose/foundation/text/selection/package-summary
 * 핵심 개념:
 * - `Text` 는 기본적으로 선택 불가 → `SelectionContainer` 로 감싸야 선택이 열린다. 컨테이너 하나가
 *   그 안의 모든 선택 가능 텍스트를 **하나의 선택 영역**으로 묶는다
 * - `DisableSelection { }` 은 같은 컨테이너 안에서 일부만 선택에서 제외한다(숨기는 것이 아니라 선택만 막는다)
 * - **선택 내용을 읽는 공개 경로가 없었다(1.11.x — 1.12 의 SelectionState 로 해소)**: `SelectionContainer(modifier, selection, onSelectionChange, children)`
 *   오버로드와 `Selection`/`Selection.AnchorInfo` 가 바이트코드에는 public 으로 보이지만 Kotlin `internal` 이라
 *   앱 모듈에서 컴파일되지 않는다(`Cannot access 'data class Selection': it is internal in file.`).
 *   → **교훈: Kotlin internal 은 JVM public 으로 컴파일되므로 javap 결과만 보고 사용 가능하다고 판단하면 안 된다**
 * - `TextToolbar` / `LocalTextToolbar`: 인터페이스는 `showMenu(rect, onCopyRequested, onPasteRequested,
 *   onCutRequested, onSelectAllRequested)`(+ onAutofillRequested 오버로드) · `hide()` · `status`(Shown/Hidden)
 * - **⚠️ 1.11 부터 LocalTextToolbar 교체는 기본값에서 동작하지 않는다**: foundation 이 선택 컨텍스트 메뉴를
 *   `androidx.compose.foundation.text.contextmenu` 로 옮겼고 `ComposeFoundationFlags.isNewContextMenuEnabled`
 *   기본값이 **true**(바이트코드 정적 초기화에서 확인). true 인 동안 `SelectionManager` 는 `TextToolbar.showMenu` 를
 *   호출하지 않는다. 플래그를 false 로 두면 예전 경로로 돌아간다(전역 가변 상태이므로 원복 필요)
 * - 실기기 계측(SM-A725F/Android 13, Compose UI 테스트에서 longClick 으로 선택):
 *   ① 기본값(true) → showMenu **0회**, status=Hidden
 *   ② 런타임에 false 로 바꾸고 `key()` 로 서브트리를 재생성 → showMenu **2회**, status=Shown
 *   ③ 넘어오는 액션은 **copy, selectAll 뿐**(읽기 전용 선택이라 cut/paste 는 null)
 * - (1.11 까지) 선택된 문자열을 얻는 우회: 커스텀 툴바가 받은 `onCopyRequested` 를 실행한 뒤 `LocalClipboard` 로
 *   `getClipEntry()?.clipData` 를 되읽는다. 단 Android 12+ 는 클립보드 읽기 시 사용자에게 토스트를 띄운다
 * - **Compose 1.12 의 `SelectionState`(3번 카드, foundation 1.12.1 sources jar 기준)**: `rememberSelectionState()`(rememberSaveable +
 *   `SelectionState.Saver`) → `SelectionContainer(state = state) { }`. `selectedTexts: List<AnnotatedString>`(스냅샷 상태, Text 하나당
 *   한 조각) · `selectAll()` · `clear()` · `extendSelectionByWord()` · `select(TextRange)`(컨테이너 전체 기준) ·
 *   `getSelectableTexts()`(함수 — 레이아웃 필요, 컴포지션 중 호출 금지, DisableSelection 제외). 같은 상태를 두 컨테이너에
 *   넘기면 error(). `Selection` 자체는 1.12.1 에서도 internal. 기본 `SelectionContainer(modifier, content)` 도 내부에서
 *   rememberSelectionState() 를 쓴다(1.11.4 는 `remember { mutableStateOf<Selection?>(null) }`).
 * - 실측(SM-A725F/Android 13, 1.12.1): 전체 선택 2조각·160자(83+77, DisableSelection 2줄 제외) · getSelectableTexts 2개·160자 ·
 *   select(0,12) "Compose 에서 화" → 단어 확장 14자 "…화면에" → 18자 "…보이는" · clear 0조각. 코드로 바꾼 선택도 하이라이트·핸들·
 *   복사/모두 선택 메뉴가 뜬다. 1~4번 카드의 1.11.4 실측(그냥 Text 무반응·컨테이너는 단어 선택+메뉴, 플래그 true showMenu 0회 /
 *   false 2회·copy·selectAll)은 1.12.1 에서 그대로였다.
 * - **재생성(다크 모드 전환) 복원 — Lazy 항목 안에서는 깨진다**(debug·release 동일):
 *   ① Column(리스트 밖) — 1조각·하이라이트·핸들 복원, 단어 확장 12→14 정상(이 화면이 Column 인 이유)
 *   ② LazyColumn 항목 안에서 상태 생성 — 0조각. Saver 를 로그로 감싸 보니 onStop 직후 1조각 저장 → onDestroy 해체 중 선택이
 *      풀린 0조각이 다시 저장 → 복원 0조각. SaveableStateHolderImpl.saveAll() 이 내부 savedStates 맵 객체를 그대로 돌려주고
 *      항목 onDispose 가 같은 맵에 다시 쓰는데, 구성 변경 재생성은 Bundle 을 직렬화하지 않고 넘기므로 나중 값이 이긴다(소스 판독)
 *   ③ 상태를 LazyColumn 밖에서 만들어 넘김 — selectedTexts 만 1조각, 하이라이트 없음·단어 확장 무반응(레지스트라는 incrementId 만
 *      저장하고 subselections 는 조작 때만 갱신) ④ 재생성 없이 Lazy 항목을 화면 밖으로 보냈다 오면 유지(포커스 항목 고정)
 *   ⑤ 창 포커스만 잃는 것(알림 패널 내렸다 접기)으로는 선택이 풀리지 않는다
*/
