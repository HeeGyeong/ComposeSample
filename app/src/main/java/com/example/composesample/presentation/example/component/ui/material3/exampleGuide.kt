package com.example.composesample.presentation.example.component.ui.material3

/**
 * Material 3 Expressive 예제 참고 자료
 *
 * ## Material3 1.4.0 릴리즈 노트
 * - 공식 릴리즈: https://developer.android.com/jetpack/androidx/releases/compose-material3#1.4.0
 *
 * ### SecureTextField / OutlinedSecureTextField
 * - TextFieldState 기반 비밀번호 입력 전용 컴포넌트
 * - TextObfuscationMode: Hidden(즉시 난독화), RevealLastTyped(마지막 문자 잠시 표시), Visible(그대로 표시)
 * - obfuscationCharacter 파라미터로 난독화 문자 커스텀 가능 (기본값 '•')
 *
 * ### 시스템 '비밀번호 표시' 설정과 TextObfuscationMode.System (foundation 1.12, 2026-10-07 보강)
 * - 릴리스 노트: https://developer.android.com/jetpack/androidx/releases/compose-foundation
 * - foundation 1.12.1 sources jar 기준:
 *   · `TextObfuscationMode.System`(1.12 신규) — 시스템 설정이 켜져 있으면 RevealLastTyped, 꺼져 있으면 Hidden 처럼 동작.
 *     API 37+ 는 `android.text.ShowSecretsSetting`(37.0 신규)으로 터치·물리 키보드를 따로 판정, 37 미만은
 *     `Settings.System.TEXT_SHOW_PASSWORD` 하나로 둘 다 판정. 읽기 실패·값 없음은 켜짐(true)으로 본다
 *   · **`RevealLastTyped` 의 의미가 바뀌었다** — 1.11.4 는 TEXT_SHOW_PASSWORD 가 꺼져 있으면 Hidden 과 같았는데,
 *     1.12.1 은 `RevealLastTyped -> true`(설정 무시, 항상 마지막 글자 노출). BasicSecureTextField 기본값은
 *     RevealLastTyped(internal Default) → System 으로 바뀌었다
 *   · **Material3 1.4.0 SecureTextField/OutlinedSecureTextField 기본값은 여전히 RevealLastTyped** → foundation 1.12 와 함께
 *     쓰면 사용자가 비밀번호 표시를 꺼도 마지막 글자가 보인다. 설정을 따르려면 `textObfuscationMode = System` 을 명시
 *   · `LocalTextFieldContentObserverRegistrationExecutor`(1.12 신규, Executor?) — 비밀번호 필드마다 하는 ContentObserver
 *     등록·해제(IPC)를 넘긴 Executor 에서 실행. null(기본)이면 메인 스레드. 거부되면 호출 스레드에서 바로 실행
 *   · `PasswordVisibilitySetting`·`SplitVisibilitySettings`·팩토리는 **internal** — 앱이 표시 정책을 주입할 수 없다
 *     (후보 기록의 "직접 구현" 전제는 틀렸다 — 공개 표면은 모드와 등록 Executor 뿐)
 * - 실측(SM-A725F/Android 13, debug): 이 기기는 show_password 값이 없어(null) 켜짐으로 해석 →
 *   RevealLastTyped·System 모두 'a' 노출 후 1.5초 뒤 '•'. show_password=0 으로 바꾸면 RevealLastTyped 는 "•b" 노출,
 *   System 은 "••" 즉시 숨김(측정 후 settings delete 로 원래의 '값 없음'으로 복원, 카드가 ContentObserver 로 즉시 갱신).
 *   Executor: 등록 1회 → 필드 숨김(해제) 2회 → 다시 표시(등록) 3회, 모두 전용 스레드 pwd-observer-io
 *
 * ### HorizontalFloatingToolbar
 * - ExperimentalMaterial3ExpressiveApi 필요
 * - leadingContent / content / trailingContent 3영역 구조
 * - FloatingToolbarDefaults.floatingToolbarState()로 확장/축소 상태 관리
 * - 스크롤 연동 시 expanded 파라미터를 스크롤 상태와 바인딩
 *
 * ### VerticalDragHandle
 * - BottomSheet용 시각적 드래그 핸들
 * - DragHandleSizes, DragHandleColors, DragHandleShapes로 커스터마이징
 *
 * ### ButtonGroup 개선 (1.4.0)
 * - verticalAlignment 파라미터 추가
 * - ButtonGroupDefaults 기본 오버플로우 인디케이터 제공
 * - Modifier.align() 확장으로 개별 버튼 정렬 제어
 *
 * ### 참고: Material Icons 지원 중단
 * - androidx.compose.material:material-icons-core/extended는 1.7.8이 마지막 버전
 * - 향후 Material Symbols(Google Fonts Vector Drawable XML)로 마이그레이션 권장
 * - https://fonts.google.com/icons
 */

/**
 * M3 날짜·시간 선택기 예제 참고 자료 (DateTimePickerExampleUI · DateTimePickerLab.kt)
 *
 * ## 개요
 * DatePicker · DatePickerDialog · DateRangePicker · TimePicker · TimeInput · TimePickerDialog(1.4.0 신규)와,
 * 그 값의 단위 — M3 의 날짜 값은 모두 **그날 UTC 자정의 epoch millis** 다.
 *
 * - DatePicker 문서: https://developer.android.com/develop/ui/compose/components/datepickers
 * - TimePicker 문서: https://developer.android.com/develop/ui/compose/components/time-pickers
 * - DatePickerState: https://developer.android.com/reference/kotlin/androidx/compose/material3/DatePickerState
 * - SelectableDates: https://developer.android.com/reference/kotlin/androidx/compose/material3/SelectableDates
 * - 후보 출처: Android Weekly #751 의 서드파티 캘린더 라이브러리(Orrery)를 대조하다 찾은 공백 — 라이브러리는 쓰지 않는다
 *
 * ## 근거 (material3 1.4.0 소스)
 * - DatePickerState.selectedDateMillis KDoc: "the selected date _start_ of the day in _UTC_ milliseconds"
 * - DatePickerState(locale, initialSelectedDateMillis) KDoc: selectedDateMillis 는 '그날의 시작' 이라 넘긴 값과 다를 수 있다.
 *   맞추는 규칙은 CalendarModelImpl.getCanonicalDate — `Instant.ofEpochMilli(t).atZone(ZoneId.of("UTC")).toLocalDate()`
 * - SelectableDates.isSelectableDate(utcTimeMillis) 는 칸마다 그날 UTC 자정을 받는다
 * - 실험 여부: DatePicker · DatePickerDialog · DateRangePicker 안정, TimePicker · TimeInput · TimePickerState 는
 *   @ExperimentalMaterial3Api, TimePickerDialog(+ TimePickerDialogDefaults.Title · DisplayModeToggle)는 안정
 * - rememberTimePickerState 의 is24Hour 기본값 = DateFormat.is24HourFormat(LocalContext.current)
 *
 * ## 측정 (Pixel 4 API 30 에뮬레이터 · Asia/Seoul · en-US — 09:00 KST 전 debug 3회 · release 1회, 뒤 debug 1회 · release 1회(디버그 키 서명),
 *   같은 시간대 안에서 PROBE 고유 20줄 동일)
 * - 읽기: 11/10 을 고르면 millis 1794268800000 → UTC 11/10 · 기기(서울) 11/10 · LA 11/09. 서울에서는 기기 시간대로 읽어도
 *   같은 날이라 버그가 숨는다
 * - 쓰기: 서울 '오늘'(11/04) 자정 1793718000000 을 넘기면 실제 DatePickerState 가 11/03 을 고르고 다이얼로그도
 *   "Current selection: Tuesday, November 3, 2026" — UTC 자정 1793750400000 은 11/04. LA 자정은 LA 의 오늘(11/03) 그대로
 * - SelectableDates(LA, 지금 11/03 15:28 PST): 'now 비교 + 기기 시간대 요일' 은 오늘(11/03) ✕ · 토(11/07) ○ · 월(11/09) ✕,
 *   바른 규칙은 오늘 ○ · 토 · 일 ✕ — 다이얼로그 칸의 enabled 도 같다. 서울은 09:00 KST(= 00:00 UTC) 전에는 두 규칙이 같았고
 *   (08:52 — 오늘 칸의 UTC 자정이 아직 지금보다 늦다), 09:05 에는 잘못된 규칙만 오늘(11/04)을 막았다 — 시각에 따라 결과가
 *   바뀌는 버그라 서울에서는 아침에 테스트하면 놓친다(단위 테스트는 03:00 · 14:00 두 시각으로 고정)
 * - M3 의 'Today' 표시는 기기 시간대의 날짜다(서울 08:2x 에 UTC 는 11/03 이지만 11/04 에 표시)
 * - 기간: 11/10~11/13 → 4일 · 3박, 시작만 고르고 확인 → 끝 null
 * - 시간: 9:00 → 바름 11/10 09:00 · millis 덧셈 11/10 18:00(9시간), 12시간 입력 모드에서 PM → state.hour 21 ·
 *   millis 덧셈 11/11 06:00(날짜까지 넘어간다)
 * - JVM 단위 테스트 DateTimePickerLabTest(12): 기대값은 달력 사실(11/04 = 수요일 · epoch 20,761일 · LA 11/01 서머타임 종료)로 쓰고,
 *   실제 M3 DatePickerState 도 JVM 에서 돌려(LegacyCalendarModelImpl 경로) 옮긴 규칙과 같은지 확인
 *
 * ## 제약
 * - java.time 은 API 26+ — 프로젝트는 core library desugaring 을 켜지 않았다 → API 24 · 25 는 안내 카드만.
 *   java.time 은 DateTimePickerLab.kt 한 파일에만 두고(`@file:Suppress("NewApi")` + 근거 주석), 화면에는 문자열 · millis ·
 *   SampleZone 만 넘긴다. 실행 가드는 진입부 SDK_INT 분기 1곳(`@SuppressLint("ObsoleteSdkInt")` — debug 변형은 HotSwan 때문에
 *   minSdk 26 이라 lintDebug 가 이 분기를 불필요하다고 보지만 release 는 24). 첫 버전은 화면 함수마다 @RequiresApi(O) 를 달아
 *   lint 경고가 23건 늘었다(#122 와 같은 실수)
 * - 기기 시간대를 바꾸지 않고 비교하려고 시간대(서울 · LA · UTC)를 이름으로 고정해 계산한다
 */

