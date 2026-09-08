package com.example.composesample.presentation.example.component.ui.form

/**
 * UI/Form 예제 참고 자료
 *
 * ## FormValidationExampleUI (폼 상태와 검증)
 * - 텍스트 입력 개요: https://developer.android.com/develop/ui/compose/text/user-input
 * - TextFieldState(새 API): https://developer.android.com/reference/kotlin/androidx/compose/foundation/text/input/TextFieldState
 * - Material3 TextField(isError/supportingText): https://developer.android.com/reference/kotlin/androidx/compose/material3/package-summary#OutlinedTextField(androidx.compose.foundation.text.input.TextFieldState,androidx.compose.ui.Modifier)
 * - 출처(Android Weekly #743): https://medium.com/@wassim_ltaief/why-is-form-state-management-in-jetpack-compose-still-this-painful-098c8d59f29a
 *
 * 핵심 개념:
 * - 이 예제의 축은 "필드 하나"가 아니라 **여러 필드가 하나의 폼으로 묶일 때**다.
 *   프로젝트의 기존 `ui/text/` 예제 8개는 전부 단일 필드 축이고(최대 길이·구문 강조·리치 콘텐츠·자동 크기 등),
 *   착수 시점 기준 `ImeAction`/`KeyboardActions` 0건, TextField 의 `isError` 사용 0건, `supportingText` 0건이었다.
 * - 검증 시점 3종의 성격이 다르다:
 *   · 입력 즉시(EAGER) — 글자마다 검사. 다 치기 전에 빨개져 사용자를 재촉한다.
 *   · 포커스 이탈(ON_BLUR) — 필드를 벗어날 때 검사. 입력 중에는 조용하다. 단 "한 번이라도 만진 필드"만 검사해야
 *     화면 진입 직후 빈 폼이 전부 빨개지는 일을 막는다(touched 플래그).
 *   · 제출 시(ON_SUBMIT) — 어디가 틀렸는지 늦게 알지만, 입력 흐름을 방해하지 않는다.
 *   실무 조합은 보통 "처음엔 blur, 한 번 에러가 난 뒤부터는 즉시"다.
 * - ⚠️ `TextFieldState` 에는 `onValueChange` 가 없다. 값 변화를 구독하려면 `snapshotFlow { state.text }` 를 쓴다.
 *   컴포지션 본문에서 `state.text` 를 직접 읽으면 그 컴포저블이 글자마다 리컴포지션된다.
 * - ⚠️ `clearText()`/`setTextAndPlaceCursorAtEnd()` 는 `TextFieldState` 의 멤버가 아니라
 *   `androidx.compose.foundation.text.input` 패키지의 **확장 함수**다 — import 를 빠뜨리면 Unresolved reference 가 난다.
 * - ⚠️ `KeyboardOptions(imeAction = ImeAction.Next)` 는 **키보드 버튼 모양만** 바꾼다. 실제 포커스 이동은
 *   `onKeyboardAction` 에서 `FocusRequester.requestFocus()` 를 직접 호출해야 일어난다.
 *   (state 기반 material3 오버로드의 콜백 파라미터는 `KeyboardActions` 가 아니라 `onKeyboardAction`/`KeyboardActionHandler` 다.)
 * - ⚠️ `supportingText` 를 에러일 때만 채우면 에러가 뜨는 순간 필드 높이가 늘어 아래 내용이 밀린다(레이아웃 점프).
 *   해법 ① 항상 채우기(에러가 없으면 도움말을 넣는다 — 3번 카드가 쓰는 방식) ② 에러 영역 높이를 미리 확보.
 *   예제 5번 카드는 `onGloballyPositioned` 로 높이를 실측해 차이를 화면에 숫자로 보여준다.
 * - 제출 버튼 활성 조건은 `derivedStateOf` 로 감싼다. 조건 계산 자체는 글자마다 일어나지만 **결과가 뒤집히는
 *   순간은 몇 번 안 되기 때문**이며, 4번 카드가 두 방식의 리컴포지션 횟수를 나란히 센다.
 * - 다른 필드에 의존하는 규칙(비밀번호 확인)은 필드 단위로 계산할 수 없다 → 폼 단위 계산이 필요한 이유.
 * - 검증 규칙은 컴포저블이 아닌 **순수 함수**로 둔다(이 파일의 validateName/validateEmail/validatePassword/
 *   validateConfirm). 화면 없이 단위 테스트할 수 있고 ViewModel 로 옮겨도 그대로 따라간다.
 * - ⚠️ 이 사이클의 검증 범위: 컴파일과 화면 구성까지만 확인했고 **실기기 계측은 하지 못했다**.
 *   연결된 SM-A725F 는 키가드 잠금 상태에서 시스템 업데이트 확인 대화상자가 최상단을 점유해
 *   테스트 액티비티가 뜨지 못했고("No compose hierarchies found"), 대체로 띄운 API 34 에뮬레이터는
 *   계측 실행 중 앱 APK 가 유지되지 않았다(#89 에서와 같은 증상). 대신 **화면이 스스로 측정한다** —
 *   에러 전환 횟수·리컴포지션 횟수·필드 높이는 전부 런타임에 계산해 표시하므로 기기에서 열면 실제 값이 보인다.
 * - ⚠️ 이 사이클의 검증 범위: 컴파일과 화면 구성까지만 확인했고 **실기기 계측은 하지 못했다**.
 *   연결된 SM-A725F 는 키가드 잠금 상태에서 시스템 업데이트 확인 대화상자가 최상단을 점유해 테스트
 *   액티비티가 뜨지 못했고("No compose hierarchies found"), 대체로 띄운 API 34 에뮬레이터는 계측 실행 중
 *   앱 APK 가 유지되지 않았다(#89 PiP 사이클과 같은 증상). 대신 **화면이 스스로 측정한다** —
 *   에러 전환 횟수·리컴포지션 횟수·필드 높이는 전부 런타임 계산이라 기기에서 열면 실제 값이 보인다.
 * - 출처의 Formidable 은 KMP + KSP 로 폼 컨트롤러를 생성하는 라이브러리라 이 프로젝트(non-KMP)에는 채택하지 않고
 *   **기법만 순수 Compose 로 재구성**했다(#80 자동 스켈레톤에서 KMP 전용 라이브러리를 같은 방식으로 처리한 선례).
 */
