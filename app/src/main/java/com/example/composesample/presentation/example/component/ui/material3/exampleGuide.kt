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
 *     → **Material3 1.5.0(2026-11-20 BOM 2026.11.01 상향 — DEP-CURRENCY-35)부터 기본값이 System** 으로 바뀌었다(I76c7e).
 *     이 예제는 두 카드 모두 모드를 칩으로 명시해 넘기므로 화면 동작은 그대로다
 *   · `LocalTextFieldContentObserverRegistrationExecutor`(1.12 신규, Executor?) — 비밀번호 필드마다 하는 ContentObserver
 *     등록·해제(IPC)를 넘긴 Executor 에서 실행. null(기본)이면 메인 스레드. 거부되면 호출 스레드에서 바로 실행
 *   · `PasswordVisibilitySetting`·`SplitVisibilitySettings`·팩토리는 **internal** — 앱이 표시 정책을 주입할 수 없다
 *     (후보 기록의 "직접 구현" 전제는 틀렸다 — 공개 표면은 모드와 등록 Executor 뿐)
 * - 실측(SM-A725F/Android 13, debug): 이 기기는 show_password 값이 없어(null) 켜짐으로 해석 →
 *   RevealLastTyped·System 모두 'a' 노출 후 1.5초 뒤 '•'. show_password=0 으로 바꾸면 RevealLastTyped 는 "•b" 노출,
 *   System 은 "••" 즉시 숨김(측정 후 settings delete 로 원래의 '값 없음'으로 복원, 카드가 ContentObserver 로 즉시 갱신).
 *   Executor: 등록 1회 → 필드 숨김(해제) 2회 → 다시 표시(등록) 3회, 모두 전용 스레드 pwd-observer-io
 *
 * ## Material3 1.5.0 정식 Expressive 컴포넌트 (2026-11-26 보강 #188 — ExpressiveComponentCards.kt · ExpressiveComponentLab.kt)
 * - 후보 출처: Compose November '26 블로그(2026-11-18) https://developer.android.com/blog/posts/jetpack-compose-november-2026-release
 * - 릴리스 노트: https://developer.android.com/jetpack/androidx/releases/compose-material3#1.5.0
 * - 근거는 material3-android 1.5.0 sources jar. 실험 표시 수: SplitButton.kt · FloatingToolbar.kt · WavyProgressIndicator.kt ·
 *   carousel/Carousel.kt · CarouselState.kt 0, LoadingIndicator.kt 7 · MaterialShapes.kt 4(@ExperimentalMaterial3ExpressiveApi),
 *   carouselParallaxScrollEffect 는 @ExperimentalMaterial3Api
 * - 측정: Pixel Tablet API 35 에뮬(2560×1600, density 2.0, debug) — 앱 카드 + 계측 테스트 Material3ExpressiveTest 7개,
 *   Pixel_3_API_26 AVD(실제 API 28) 계측 테스트 7개
 *
 * ### Wavy 진행 표시 — Linear/CircularWavyProgressIndicator
 * - 진행값은 `() -> Float` 람다, 그리기 캐시 단계(CacheDrawModifierNode)에서 읽는다 → 값이 바뀌어도 부르는 쪽 리컴포지션 0
 *   (카드: 칩 4번 바꿔도 '미리보기 리컴포지션 0회', 계측 테스트로 확인)
 * - 기본 파도 높이 `WavyProgressIndicatorDefaults.indicatorAmplitude` = 10% 이하 · 95% 이상이면 0, 사이는 1
 *   (높이 바뀜은 DurationLong2 tween). 값 사이 애니메이션은 기본으로 없다 — ProgressAnimationSpec 을 직접 건다
 * - **선형과 원형의 비대칭**: 선형(LinearWavyProgressModifiers.updateOffsetAnimation)은 waveSpeed · wavelength > 0 이면
 *   파도 오프셋 무한 애니메이션을 띄우고 파도 높이를 보지 않는다 — 높이 0 이면 그리기에서 오프셋을 안 읽어 그리지는 않지만
 *   매 프레임 오프셋 상태를 쓴다(시계가 돈다). 원형(CircularWavyProgressModifiers.startOffsetAnimation)은 `!isDrawingWave()` 면
 *   시작하지 않고, 높이 애니메이션이 0 에 닿으면 stopOffsetAnimation
 * - 앱 측정(목록을 컴포지션에서 빼고 지시기 하나만 둔 무대, 창 2초, 그린 프레임 = FrameMetrics · 프레임 콜백 = 메인 Looper
 *   VSync 디스패치 — #177 계측 방식): 선형 50% 15/15 · 선형 5% 0/49 · 선형 100% 0/51 · 선형 50% waveSpeed 0 0/0 ·
 *   원형 50% 14/14 · 원형 100% 0/0 · 기존 LinearProgressIndicator 50% 0/0 (그린 프레임/콜백). 7줄 모두 기대와 같음.
 *   Pixel_3_API_26 AVD(실제 API 28, 1080×2160): 76/76 · 0/117 · 0/116 · 0/0 · 71/71 · 0/0 · 0/0 — 같은 결론
 * - 계측 테스트(시계 정지): 프레임 1초를 넘긴 뒤 Recomposer.runningRecomposers 의 hasPendingWork(다음 프레임을 기다리는
 *   코루틴)가 선형 5% · 100% 에서도 참, 원형 100% · waveSpeed 0 · 기존 표시는 거짓. 250ms 사이 캡처 비교로 그림이 바뀌는 줄은
 *   선형 50% · 원형 50% 뿐
 * - 측정 함정 3가지:
 *   · 지시기를 감싼 레이어의 그리기 람다로 세면 선형은 0 — 선형 안에 clipToBounds(= graphicsLayer(clip = true)) 레이어가 있어
 *     파도가 움직여도 그 레이어만 다시 기록한다(원형은 레이어가 없어 바깥에서 103회 보였다) → FrameMetrics 로 바꿈
 *   · 앱에서 Recomposer.hasPendingWork 를 20ms 마다 찍으면 그리는 줄도 22~37% 로 흔들려 '계속'과 '0'을 못 가른다 → 프레임 콜백으로
 *   · 이 에뮬은 파도를 그리는 동안 초당 7 프레임 남짓(소프트웨어 렌더) — #177 의 'VSync 의 25%' 문턱을 못 넘어 '초당 3' 으로
 *   · 테스트에서 autoAdvance 를 켜 두면 테스트 환경의 InfiniteAnimationPolicy 가 무한 애니메이션을 취소한다(ComposeUiTest.android.kt)
 *     → setContent 전에 끈다. 테스트 환경은 뷰 트리에 Recomposer 태그를 달지 않아 findViewTreeCompositionContext() 는 null
 *
 * ### SplitButtonLayout
 * - measurePolicy: trailing 의 maxIntrinsicWidth 로 남는 폭을 어림 → 두 버튼이 원하는 높이 중 큰 값으로 둘 다 고정 →
 *   trailing 을 먼저 재고 leading 은 나머지 폭. 사이 간격 SplitButtonDefaults.Spacing(2dp)
 * - 측정(무대 320 · 220 · 150dp, 안쪽 패딩 4dp): leading 151.5 · 151.5 · 92dp, trailing 48dp 그대로, 높이 40 · 40 · 80dp
 *   (150dp 에서 'PDF로 내보내기'가 네 줄로 꺾여 leading 이 커지고 trailing 도 같이 80dp)
 * - trailing 은 checked 오버로드 — checked 면 모서리가 full 로 morph(TrailingCheckedShape), 바깥 테두리 선(상태 레이어 알파)
 * - 광학 중앙(내부 horizontalCenterOptically, CenterOpticallyCoefficient 0.11): 내용 x 보정 = 0.11 × (시작 모서리 평균 − 끝 모서리 평균),
 *   안쪽 패딩으로 제한. trailing 해제 = 0.11 × (4 − 20) = −1.76dp → 실측 −2.0dp(px 반올림, density 2), checked 0.0dp. leading 은 보정 없음
 *
 * ### HorizontalFloatingToolbar
 * - opt-in 없음(1.5.0). expanded · leadingContent · content · trailingContent, FAB 겸용 오버로드(floatingActionButton)
 * - FloatingToolbarDefaults.exitAlwaysScrollBehavior(exitDirection, state = rememberFloatingToolbarState()) 를
 *   스크롤 목록의 조상에 Modifier.nestedScroll(behavior) 로 붙인다. onPostScroll 이 consumed.y 만 offset 에 더한다
 * - 숨는 거리 offsetLimit = −(부모 높이 − 툴바 위쪽) — 부모 밖으로 완전히 나간다. 측정: offset −92.2dp = offsetLimit
 *   (240dp 상자 · 바닥 12dp 띄움 · FAB 판). clipToBounds 없이 카드 안에 두면 나간 툴바가 상자 아래 글자를 덮는다(화면 캡처 · 테스트로 겹침 확인),
 *   clip 하면 보이는 영역 0
 * - 손을 떼면 settleFloatingToolbar 가 collapsedFraction 0.5 를 기준으로 0 또는 offsetLimit 로 맞춘다 — 30dp 남짓 내리면 도로 숨고
 *   (offset −91.8dp), 크게 내려야 0. KDoc 의 'immediately appear when the content is pulled down' 은 반을 넘겨 내렸을 때다
 * - 소스: 터치 탐색(TalkBack)이 켜져 있으면 expanded 를 무시하고 펼친다 · offset ≠ 0 이면 focusProperties { canFocus = false }
 *
 * ### HorizontalCenteredHeroCarousel
 * - 선택 칸을 큰 칸으로 가운데, 양옆 작은 칸(MinSmallItemSize 40dp ~ MaxSmallItemSize 56dp). 양 끝에서는 시작 · 끝에 붙는다,
 *   3칸 미만이면 시작 정렬. 칸은 **모두 큰 칸 폭으로 배치**하고 maskClip 으로 잘라 보인다(측정: 배치 폭 3칸 모두 1120dp,
 *   마스크 40 · 1120 · 40dp — 태블릿이라 큰 칸이 넓다)
 * - carouselItemDrawInfo(size · minSize · maxSize · maskRect)는 스크롤마다 바뀌는 상태 — KDoc 이 컴포지션에서 읽지 말라고 한다.
 *   측정(0 → 1 animateScrollToItem, tween 1500ms 선형): 앱 — 컴포지션에서 읽기 칸 컴포지션 61회 · 그리기에서 읽기 0회,
 *   계측 테스트(가상 시계 16ms 프레임) — 205회 · 0회. 기본 spring 으로 넘기면 이 에뮬은 몇 프레임에 끝나 4~6회 대 0~1회로 비교가 약했다.
 *   컴포지션에서 Text 로 그리면 큰 칸 폭 가운데라 작은 칸에서 글자가 잘린다 — 그리기에서 maskRect 안 가운데에 두고, 넓으면 번호만
 *
 * ### LoadingIndicator (1.5.0 에서도 실험)
 * - LoadingIndicator · ContainedLoadingIndicator(진행값 판 포함) — MaterialShapes 다각형(RoundedPolygon) 사이를 morph.
 *   같은 화면에서 opt-in 경계를 보이려고 @OptIn(ExperimentalMaterial3ExpressiveApi::class) 카드 하나만 둔다
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

