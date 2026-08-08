package com.example.composesample.presentation.example.list

import com.example.composesample.util.ConstValue
import com.example.composesample.presentation.example.model.ExampleObject

val examples2026 = listOf(
    ExampleObject(
        lastUpdate = "26. 01. 10",
        title = "Quick Settings Tile",
        description = "빠른 설정 타일을 활용한 마이크로 인터랙션 패턴 구현",
        blogUrl = blogUrl(194),
        exampleType = ConstValue.QuickSettingsTileExample
    ),
    ExampleObject(
        lastUpdate = "26. 01. 17",
        title = "Fancy TopAppBar",
        description = "Collapsing Toolbar와 다양한 스크롤 동작을 가진 고급 TopAppBar 구현",
        blogUrl = blogUrl(195),
        exampleType = ConstValue.FancyTopAppBarExample
    ),
    ExampleObject(
        lastUpdate = "26. 01. 19",
        title = "Canvas Shapes & Animations",
        description = "Canvas를 활용한 도형 그리기와 애니메이션 기초",
        blogUrl = blogUrl(196),
        exampleType = ConstValue.CanvasShapesExample
    ),
    ExampleObject(
        lastUpdate = "26. 01. 20",
        title = "Responsive TabRow",
        description = "SubcomposeLayout을 활용한 반응형 탭 구현",
        blogUrl = blogUrl(197),
        exampleType = ConstValue.ResponsiveTabRowExample
    ),
    ExampleObject(
        lastUpdate = "26. 01. 25",
        title = "Custom Text Rendering",
        description = "TextMeasurer와 Canvas를 활용한 커스텀 텍스트 렌더링",
        blogUrl = blogUrl(198),
        exampleType = ConstValue.CustomTextRenderingExample
    ),
    ExampleObject(
        lastUpdate = "26. 01. 26",
        title = "Swipe to Dismiss (Material 3)",
        description = "Material 3의 SwipeToDismissBox를 활용한 스와이프 제스처",
        blogUrl = blogUrl(199),
        exampleType = ConstValue.SwipeToDismissM3Example
    ),
    ExampleObject(
        lastUpdate = "26. 02. 05",
        title = "Shared Element Transitions",
        description = "화면 간 요소를 부드럽게 전환하는 공유 요소 애니메이션",
        blogUrl = blogUrl(200),
        exampleType = ConstValue.SharedElementTransitionExample
    ),
    ExampleObject(
        lastUpdate = "26. 02. 07",
        title = "Dial Component",
        description = "Canvas로 구현하는 원형 다이얼: 범위 설정, 스냅, 멀티턴",
        blogUrl = blogUrl(201),
        exampleType = ConstValue.DialComponentExample
    ),
    ExampleObject(
        lastUpdate = "26. 02. 13",
        title = "Embedded Photo Picker",
        description = "앱 내에서 직접 포토 피커를 임베드하여 사진/영상을 선택하는 방법",
        blogUrl = blogUrl(190),
        exampleType = ConstValue.EmbeddedPhotoPickerExample
    ),
    ExampleObject(
        lastUpdate = "26. 02. 16",
        title = "CompositionLocal Tree Visualization",
        description = "Composition Tree에서 CompositionLocal의 데이터 흐름, 룩업, 섀도잉을 시각화",
        blogUrl = blogUrl(191),
        exampleType = ConstValue.CompositionLocalTreeExample
    ),
    ExampleObject(
        lastUpdate = "26. 02. 19",
        title = "Retain API (Goodbye ViewModel)",
        description = "Compose 1.10 retain API로 ViewModel 없이 상태 보존하는 패턴",
        blogUrl = blogUrl(192),
        exampleType = ConstValue.RetainApiExample
    ),
    ExampleObject(
        lastUpdate = "26. 02. 22",
        title = "Custom TopAppBarScrollBehavior",
        description = "RecyclerView 스크롤 이벤트를 커스텀 TopAppBarScrollBehavior로 처리하는 패턴",
        blogUrl = blogUrl(193),
        exampleType = ConstValue.CustomScrollBehaviorExample
    ),
    ExampleObject(
        lastUpdate = "26. 02. 28",
        title = "Sticker Canvas (Gestures & Physics)",
        description = "드래그, 핀치 리사이즈, 회전, 스프링 물리, 필오프 애니메이션을 구현한 스티커 캔버스",
        blogUrl = blogUrl(202),
        exampleType = ConstValue.StickerCanvasExample
    ),
    ExampleObject(
        lastUpdate = "26. 03. 04",
        title = "Large Content Viewer",
        description = "iOS의 Large Content Viewer를 Compose로 구현하고, 키보드·스크린 리더 내비게이션 지원",
        blogUrl = blogUrl(203),
        exampleType = ConstValue.LargeContentViewerExample
    ),
    ExampleObject(
        lastUpdate = "26. 03. 11",
        title = "Motion Blur (Spinning Wheel)",
        description = "스피닝 휠에 모션 블러를 적용하는 방법: Ghost Frames, BlurMaskFilter, RenderEffect 비교",
        blogUrl = blogUrl(204),
        exampleType = ConstValue.MotionBlurExample
    ),
    ExampleObject(
        lastUpdate = "26. 03. 15",
        title = "LocalContext for Strings (Anti-Pattern)",
        description = "Compose에서 문자열에 LocalContext 사용 금지: stringResource vs UiText sealed class 패턴",
        blogUrl = blogUrl(205),
        exampleType = ConstValue.LocalContextStringsExample
    ),
    ExampleObject(
        lastUpdate = "26. 03. 17",
        title = "Embedded Photo Picker (Compose 통합)",
        description = "BottomSheet 통합 아키텍처, 선택 동기화 오너십 모델, URI 수명 관리, setCurrentExpanded 패턴",
        blogUrl = "",
        exampleType = ConstValue.EmbeddedPickerComposeExample
    ),
    ExampleObject(
        lastUpdate = "26. 03. 22",
        title = "Rebound - 리컴포지션 모니터링",
        description = "역할 기반 리컴포지션 예산 할당: Screen·Leaf·Animated 등 6가지 역할별 기준으로 과도한 리컴포지션을 감지",
        blogUrl = "",
        exampleType = ConstValue.ReboundExample
    ),
    ExampleObject(
        lastUpdate = "26. 03. 23",
        title = "Coroutine Flow Testing (Turbine)",
        description = "StateFlow는 상태별 독립 테스트로, SharedFlow 이벤트는 Turbine으로: 과명세화 없는 코루틴 테스트 패턴",
        blogUrl = "",
        exampleType = ConstValue.TurbineFlowTestExample
    ),
    ExampleObject(
        lastUpdate = "26. 03. 24",
        title = "Compose Preview Internals",
        description = "@Preview 렌더링 파이프라인 5단계, LocalInspectionMode, 내장 MultiPreview 어노테이션, PreviewParameter 고급 활용",
        blogUrl = "",
        exampleType = ConstValue.PreviewInternalsExample
    ),
    ExampleObject(
        lastUpdate = "26. 03. 30",
        title = "AnimatedContent 심화",
        description = "탭 전환, 숫자 카운터, 상태 전환 UI 등 AnimatedContent의 다양한 transitionSpec 패턴 비교",
        blogUrl = "",
        exampleType = ConstValue.AnimatedContentExample
    ),
    ExampleObject(
        lastUpdate = "26. 03. 27",
        title = "Startup Optimization",
        description = "App Startup 라이브러리, Baseline Profile, Koin 지연 초기화로 앱 시작 속도를 최적화하는 패턴",
        blogUrl = "",
        exampleType = ConstValue.StartupOptimizationExample
    ),
    ExampleObject(
        lastUpdate = "26. 03. 27",
        title = "Remember Patterns",
        description = "rememberSaveable(회전 생존), rememberUpdatedState(콜백 최신화), derivedStateOf(계산 최적화) 비교",
        blogUrl = "",
        exampleType = ConstValue.RememberPatternsExample
    ),
    ExampleObject(
        lastUpdate = "26. 04. 01",
        title = "LazyStaggeredGrid (폭포수 그리드)",
        description = "LazyVerticalStaggeredGrid로 Pinterest 스타일 폭포수 레이아웃 구현: 동적 높이, 스팬 제어, 필터링 애니메이션",
        blogUrl = "",
        exampleType = ConstValue.LazyStaggeredGridExample
    ),
    ExampleObject(
        lastUpdate = "26. 04. 02",
        title = "Adaptive Layout (WindowSizeClass)",
        description = "WindowSizeClass(Compact/Medium/Expanded)로 폰·태블릿·폴더블 화면 크기에 반응하는 적응형 레이아웃 구현",
        blogUrl = "",
        exampleType = ConstValue.AdaptiveLayoutExample
    ),
    ExampleObject(
        lastUpdate = "26. 04. 02",
        title = "Custom Layout (MeasurePolicy)",
        description = "Layout composable과 MeasurePolicy로 SubcomposeLayout 없이 직접 측정·배치하는 커스텀 레이아웃 구현",
        blogUrl = "",
        exampleType = ConstValue.CustomLayoutExample
    ),
    ExampleObject(
        lastUpdate = "26. 04. 02",
        title = "Dynamic App Links",
        description = "Android 15+에서 서버의 Digital Asset Links JSON으로 앱 업데이트 없이 딥링킹 동작을 실시간 제어하는 패턴",
        blogUrl = "",
        exampleType = ConstValue.DynamicAppLinksExample
    ),
    ExampleObject(
        lastUpdate = "26. 04. 02",
        title = "Screenshot Testing (Paparazzi / Roborazzi)",
        description = "Paparazzi와 Roborazzi를 활용한 Compose UI 스크린샷 테스트: 회귀 방지, 골든 이미지 관리, 실기기 없는 렌더링 검증",
        blogUrl = "",
        exampleType = ConstValue.ScreenshotTestingExample
    ),
    ExampleObject(
        lastUpdate = "26. 04. 02",
        title = "Compose Snapshot System",
        description = "State<T> 내부 동작 원리: Snapshot 격리 모델, derivedStateOf 최적화, withMutableSnapshot 원자적 상태 변경",
        blogUrl = "",
        exampleType = ConstValue.ComposeSnapshotExample
    ),
    ExampleObject(
        lastUpdate = "26. 04. 02",
        title = "Compose UI Testing",
        description = "createComposeRule, onNodeWithTag, performClick, assertIsDisplayed 등 Compose UI 테스트 패턴 가이드",
        blogUrl = "",
        exampleType = ConstValue.ComposeTestingExample
    ),
    ExampleObject(
        lastUpdate = "26. 04. 02",
        title = "Predictive Back Gesture",
        description = "Android 14+ PredictiveBackHandler로 엣지 스와이프 진행률을 Flow로 수신하여 실시간 애니메이션과 연동하는 패턴",
        blogUrl = "",
        exampleType = ConstValue.PredictiveBackExample
    ),
    ExampleObject(
        lastUpdate = "26. 04. 02",
        title = "Spring / Tween / Snap 애니메이션",
        description = "AnimationSpec 4종(spring/tween/snap/keyframes)의 움직임 차이를 한 화면에서 비교",
        blogUrl = "",
        exampleType = ConstValue.SpringTweenSnapExample
    ),
    ExampleObject(
        lastUpdate = "26. 04. 02",
        title = "Haptic Feedback",
        description = "LocalHapticFeedback과 HapticFeedbackConstants의 진동 피드백 타입·API 레벨별 지원 범위 비교",
        blogUrl = "",
        exampleType = ConstValue.HapticFeedbackExample
    ),
    ExampleObject(
        lastUpdate = "26. 04. 02",
        title = "Stability Annotations (@Stable / @Immutable)",
        description = "Compose 컴파일러의 안정성 분석 원리, @Stable과 @Immutable 어노테이션으로 불필요한 리컴포지션을 방지하는 패턴",
        blogUrl = "",
        exampleType = ConstValue.StabilityAnnotationsExample
    ),
    ExampleObject(
        lastUpdate = "26. 04. 03",
        title = "Rich Content in Text Input",
        description = "receiveContent modifier로 TextField에 이미지·파일 붙여넣기 처리 — IME·클립보드·드래그&드롭 출처별 소비 패턴",
        blogUrl = "",
        exampleType = ConstValue.RichContentTextInputExample
    ),
    ExampleObject(
        lastUpdate = "26. 04. 03",
        title = "FlowRow / FlowColumn (Compose Flexbox)",
        description = "CSS Flexbox에서 영감받은 공식 레이아웃 — FlowRow 줄바꿈·weight 분배, FlowColumn 세로 흐름 비교",
        blogUrl = "",
        exampleType = ConstValue.FlowRowLayoutExample
    ),
    ExampleObject(
        lastUpdate = "26. 04. 03",
        title = "Preview-only Composable (@RequiresOptIn)",
        description = "@RequiresOptIn으로 Preview 전용 Composable 오사용을 컴파일 타임에 차단하는 패턴",
        blogUrl = "",
        exampleType = ConstValue.PreviewOnlyAnnotationExample
    ),
    ExampleObject(
        lastUpdate = "26. 04. 06",
        title = "Coroutine Bridges (콜백 → suspend 변환)",
        description = "suspendCancellableCoroutine으로 콜백 기반 Android API를 suspend 함수로 변환: 취소 전파·리소스 정리",
        blogUrl = "",
        exampleType = ConstValue.CoroutineBridgesExample
    ),
    ExampleObject(
        lastUpdate = "26. 04. 08",
        title = "Compose Loaders (수학 곡선 애니메이션)",
        description = "Canvas와 삼각함수로 구현하는 수학 로딩 애니메이션 6종 — Rose Curve, Lissajous, Spirograph 등",
        blogUrl = "",
        exampleType = ConstValue.ComposeLoadersExample
    ),
    ExampleObject(
        lastUpdate = "26. 04. 09",
        title = "TextField Max Length 숨겨진 버그",
        description = "InputTransformation.maxLength()가 프로그래매틱 state 변경엔 적용 안 되는 버그와 snapshotFlow 해결책",
        blogUrl = "",
        exampleType = ConstValue.TextFieldMaxLengthExample
    ),
    ExampleObject(
        lastUpdate = "26. 04. 14",
        title = "Kotlin Name-Based Destructuring",
        description = "Kotlin 2.3.20의 이름 기반 구조 분해 — 위치 대신 프로퍼티 이름으로 매칭해 리팩토링 안전성 확보",
        blogUrl = "",
        exampleType = ConstValue.NameBasedDestructuringExample
    ),
    ExampleObject(
        lastUpdate = "26. 08. 07",
        title = "Foundation Style API (Compose 1.11)",
        description = "Modifier.styleable + Style{} DSL로 상태별 스타일을 선언하는 실험 API — 선언 순서 우선순위 함정과 animate 전이",
        blogUrl = "",
        exampleType = ConstValue.FoundationStyleApiExample
    ),
    ExampleObject(
        lastUpdate = "26. 04. 15",
        title = "Material 3 Expressive (1.4.0 신규)",
        description = "Material3 1.4.0 신규 컴포넌트 — SecureTextField, FloatingToolbar, VerticalDragHandle, ButtonGroup",
        blogUrl = "",
        exampleType = ConstValue.Material3ExpressiveExample
    ),
    ExampleObject(
        lastUpdate = "26. 04. 28",
        title = "Flow Operators (Buffer/Conflate/Debounce/Sample)",
        description = "Flow 속도 제어 연산자 4종(buffer/conflate/debounce/sample)의 동작 차이를 타임라인 로그로 비교",
        blogUrl = "",
        exampleType = ConstValue.FlowOperatorsExample
    ),
    ExampleObject(
        lastUpdate = "26. 06. 05",
        title = "Flow onEachBatch (배치 집계)",
        description = "커스텀 Flow 확장 onEachBatch로 원소를 배치(List)로 묶어 처리 — 단건 vs bulk insert 처리량 비교",
        blogUrl = "",
        exampleType = ConstValue.FlowBatchingExample
    ),
    ExampleObject(
        lastUpdate = "26. 06. 08",
        title = "코루틴 Race Condition 방지",
        description = "공유 가변 상태의 race condition을 재현하고 비보호/Atomic/Mutex/단일스레드 4가지 보호 전략 비교",
        blogUrl = "",
        exampleType = ConstValue.RaceConditionExample
    ),
    ExampleObject(
        lastUpdate = "26. 06. 15",
        title = "Kotlin Select Expressions",
        description = "select { }로 여러 suspending 작업을 경쟁시켜 최속 결과만 채택 — onAwait/onTimeout/onReceiveCatching 비교",
        blogUrl = "",
        exampleType = ConstValue.SelectExpressionExample
    ),
    ExampleObject(
        lastUpdate = "26. 04. 27",
        title = "Modifier Order in Compose",
        description = "동일 modifier 조합도 순서에 따라 layout/draw/hit-test 결과가 달라짐을 4가지 시나리오로 비교",
        blogUrl = "",
        exampleType = ConstValue.ModifierOrderExample
    ),
    ExampleObject(
        lastUpdate = "26. 04. 23",
        title = "Gemini Nano (ML Kit GenAI 온디바이스)",
        description = "ML Kit GenAI Feature Availability 플로우와 Nano 실패 시 Cloud fallback 라우팅 패턴 Mock 데모",
        blogUrl = "",
        exampleType = ConstValue.GeminiNanoExample
    ),
    ExampleObject(
        lastUpdate = "26. 04. 22",
        title = "Month Picker Dial (Airbnb ChromaDial)",
        description = "Canvas + atan2 각도 계산으로 Airbnb 스타일 원형 월 선택기 구현: 드래그 회전·스냅 애니메이션",
        blogUrl = "",
        exampleType = ConstValue.MonthPickerDialExample
    ),
    ExampleObject(
        lastUpdate = "26. 04. 21",
        title = "Koin Compiler Plugin (Annotations)",
        description = "Koin Annotations(KSP)의 @Module·@Single·@Factory로 DI를 컴파일 타임 검증, 수동 DSL과 비교",
        blogUrl = "",
        exampleType = ConstValue.KoinCompilerPluginExample
    ),
    ExampleObject(
        lastUpdate = "26. 05. 26",
        title = "Room Database Indices 성능 비교",
        description = "@Index 단일/복합 인덱스와 무인덱스 테이블의 응답 시간을 비교하고 leftmost prefix 규칙을 시연",
        blogUrl = "",
        exampleType = ConstValue.RoomIndexExample
    ),
    ExampleObject(
        lastUpdate = "26. 05. 21",
        title = "Room FTS4 vs LIKE 검색 성능 비교",
        description = "@Fts4 MATCH(역색인)와 LIKE 전체 스캔의 응답 시간·결과 수를 동일 시드로 측정 비교",
        blogUrl = "",
        exampleType = ConstValue.RoomFtsSearchExample
    ),
    ExampleObject(
        lastUpdate = "26. 04. 29",
        title = "Multi-Table Inserts in Room",
        description = "BaseInsertDao<T> + withTransaction { }으로 4개 테이블 원자적 insert, 실패 시 롤백 검증",
        blogUrl = "",
        exampleType = ConstValue.MultiTableInsertExample
    ),
    ExampleObject(
        lastUpdate = "26. 04. 30",
        title = "Biometric Auth in Compose",
        description = "biometric-compose의 rememberAuthenticationLauncher로 생체 인증 처리 — 가용성 진단과 폴백 분기",
        blogUrl = "",
        exampleType = ConstValue.BiometricAuthExample
    ),
    ExampleObject(
        lastUpdate = "26. 05. 04",
        title = "Accessible Focus Indicator (Indication API)",
        description = "키보드/D-pad 사용자를 위한 포커스 시각화 4가지(기본/border/scale/펄스 애니메이션) 비교",
        blogUrl = "",
        exampleType = ConstValue.AccessibleFocusIndicatorExample
    ),
    ExampleObject(
        lastUpdate = "26. 05. 06",
        title = "Document Editing TextField",
        description = "TextFieldState 심화 — undoState Undo/Redo, selection 직접 조작, 마크다운 미리보기, 멀티 커서 시뮬레이션",
        blogUrl = "",
        exampleType = ConstValue.DocumentEditingTextFieldExample
    ),
    ExampleObject(
        lastUpdate = "26. 05. 08",
        title = "Particle Emitter (물리 기반 파티클)",
        description = "Canvas + withFrameNanos로 구현한 물리 기반 파티클 시스템 — 폭죽/별가루 트리거, dt 기반 프레임 보정",
        blogUrl = "",
        exampleType = ConstValue.ParticleEmitterExample
    ),
    ExampleObject(
        lastUpdate = "26. 05. 07",
        title = "Syntax Highlighting (간소화 데모)",
        description = "정규식 토크나이저로 Kotlin 코드 하이라이팅 미니 데모 — 주석/문자열 안 키워드 오인식 방지, 라이브 편집 분리",
        blogUrl = "",
        exampleType = ConstValue.SyntaxHighlightingExample
    ),
    ExampleObject(
        lastUpdate = "26. 05. 11",
        title = "App Security 실무 (Cert Pinning + KeyStore AES-GCM + Play Integrity)",
        description = "Android 앱 보안 3대 패턴 비교 — CertificatePinner MITM 매칭, KeyStore AES-GCM, Play Integrity Mock",
        blogUrl = "",
        exampleType = ConstValue.AppSecurityExample
    ),
    ExampleObject(
        lastUpdate = "26. 04. 20",
        title = "Nav3 ViewModel Scope",
        description = "Navigation 3의 ViewModel 스코프 변화를 시뮬레이션 — Nav2 Auto-Scope vs Nav3 기본 vs NavKey 매핑",
        blogUrl = "",
        exampleType = ConstValue.Nav3ViewModelScopeExample
    ),
    ExampleObject(
        lastUpdate = "26. 06. 10",
        title = "Nav3 SavedStateHandle 크래시 & 복원",
        description = "NavKey에 복합 객체를 담으면 역직렬화 크래시 발생 — 식별자만 담고 Repository로 재조회하는 안전한 대안",
        blogUrl = "",
        exampleType = ConstValue.Nav3SavedStateHandleExample
    ),
    ExampleObject(
        lastUpdate = "26. 05. 13",
        title = "Hardware-Backed Keystore 검증",
        description = "AndroidKeyStore 키의 TEE/StrongBox 하드웨어 보관 여부를 API 버전별로 런타임 진단",
        blogUrl = "",
        exampleType = ConstValue.HardwareKeystoreExample
    ),
    ExampleObject(
        lastUpdate = "26. 07. 09",
        title = "Screenshot Detection (Android 14 콜백 vs 레거시 MediaStore)",
        description = "화면 캡처 감지 두 방식 비교 — Android 14+ registerScreenCaptureCallback과 레거시 MediaStore ContentObserver",
        blogUrl = "",
        exampleType = ConstValue.ScreenshotDetectionExample
    ),
    ExampleObject(
        lastUpdate = "26. 07. 21",
        title = "IPC / Exported Component 보안 진단",
        description = "앱 자신의 exported 컴포넌트를 스캔·진단하고 FLAG_MUTABLE PendingIntent 변조 가능성을 비교",
        blogUrl = "",
        exampleType = ConstValue.IpcExportedComponentExample
    ),
    ExampleObject(
        lastUpdate = "26. 05. 12",
        title = "Compose Animations Showcase (카탈로그)",
        description = "공통 duration/easing 슬라이더로 5가지 모션 패턴을 한 화면에서 동시 비교",
        blogUrl = "",
        exampleType = ConstValue.AnimationsShowcaseExample
    ),
    ExampleObject(
        lastUpdate = "26. 05. 22",
        title = "Per-Item ViewModels in Compose",
        description = "LazyColumn 각 아이템에 독립 ViewModel 스코프를 부여 — 상태 결합 문제와 키별 ViewModelStore 대안 비교",
        blogUrl = "",
        exampleType = ConstValue.PerItemViewModelExample
    ),
    ExampleObject(
        lastUpdate = "26. 05. 18",
        title = "Shared Element Debug Tooling (Compose 1.11)",
        description = "LookaheadAnimationVisualDebugging으로 SharedTransition 매칭 상태(정상/다중/미매칭)를 시각화",
        blogUrl = "",
        exampleType = ConstValue.SharedElementDebugToolingExample
    ),
    ExampleObject(
        lastUpdate = "26. 05. 27",
        title = "AGSL Shader Live Tuning",
        description = "RuntimeShader + renderEffect로 AGSL 셰이더를 실시간 튜닝 — uniform 슬라이더와 소스 즉시 재컴파일",
        blogUrl = "",
        exampleType = ConstValue.AgslShaderTuningExample
    ),
    ExampleObject(
        lastUpdate = "26. 05. 28",
        title = "Type-Safe Feature Flag",
        description = "외부 라이브러리 없이 sealed class flag 레지스트리, StateFlow 토글, 디버그 오버라이드, Remote Config 구현",
        blogUrl = "",
        exampleType = ConstValue.FeatureFlagExample
    ),
    ExampleObject(
        lastUpdate = "26. 06. 04",
        title = "Compose Autofill (semantics API)",
        description = "semantics contentType으로 TextField에 자동완성 힌트 부여, LocalAutofillManager로 commit/cancel 트리거",
        blogUrl = "",
        exampleType = ConstValue.AutofillExample
    ),
    ExampleObject(
        lastUpdate = "26. 06. 09",
        title = "StrictMode 위반 감지",
        description = "메인 스레드 디스크/네트워크 I/O와 Closeable 누수를 재현하고 penaltyListener로 실시간 수집·표시",
        blogUrl = "",
        exampleType = ConstValue.StrictModeExample
    ),
    ExampleObject(
        lastUpdate = "26. 06. 11",
        title = "Advanced Ktor Config (Auth/Retry)",
        description = "Ktor MockEngine으로 Auth bearer 토큰 갱신, HttpRequestRetry 백오프 재시도를 대조군과 비교",
        blogUrl = "",
        exampleType = ConstValue.KtorAdvancedConfigExample
    ),
    ExampleObject(
        lastUpdate = "26. 06. 19",
        title = "Kotlin 2.4 Language Features",
        description = "Kotlin 2.4 신규 문법 — 컬렉션 리터럴([1, 2, 3])과 컨텍스트 파라미터, 둘 다 Experimental",
        blogUrl = "",
        exampleType = ConstValue.Kotlin24FeaturesExample
    ),
    ExampleObject(
        lastUpdate = "26. 06. 22",
        title = "How Compose Works (내부 동작)",
        description = "Compose 선언형 UI의 4단계(컴파일러 변환/SlotTable/Snapshot/Layout Pipeline)를 통합 시연",
        blogUrl = "",
        exampleType = ConstValue.HowComposeWorksExample
    ),
    ExampleObject(
        lastUpdate = "26. 07. 15",
        title = "RememberObserver / Composition Lifecycle",
        description = "remember 객체의 RememberObserver 콜백이 컴포지션 진입·이탈·폐기 시 자동 호출됨을 실동작 시연",
        blogUrl = "",
        exampleType = ConstValue.RememberObserverExample
    ),
    ExampleObject(
        lastUpdate = "26. 06. 23",
        title = "Coil 3 이미지 로딩 & 캐시",
        description = "Coil 3의 AsyncImage 상태 표시, 캐시 정책, ImageLoader 커스터마이징 핵심 패턴 시연",
        blogUrl = "",
        exampleType = ConstValue.Coil3ImageExample
    ),
    ExampleObject(
        lastUpdate = "26. 06. 24",
        title = "Preview-Driven Screenshot Testing",
        description = "@Preview를 단일 진실 공급원 삼아 locale×fontScale×theme 매트릭스를 자동 파생하는 패턴 시연",
        blogUrl = "",
        exampleType = ConstValue.PreviewDrivenScreenshotExample
    ),
    ExampleObject(
        lastUpdate = "26. 06. 29",
        title = "Freehand Drawing (Signature Canvas)",
        description = "Canvas + pointerInput만으로 자유 곡선 드로잉 구현, MVI 아키텍처로 Undo/Redo·PNG 내보내기 관리",
        blogUrl = "",
        exampleType = ConstValue.FreehandDrawingExample
    ),
    ExampleObject(
        lastUpdate = "26. 07. 10",
        title = "Advanced Repository Pattern",
        description = "Memory→Disk→Network 우선순위 Repository 패턴 — cache population과 강제 새로고침 비교",
        blogUrl = "",
        exampleType = ConstValue.AdvancedRepositoryPatternExample
    ),
    ExampleObject(
        lastUpdate = "26. 07. 16",
        title = "Media3 비디오 재생 (ExoPlayer)",
        description = "ExoPlayer + PlayerView를 Compose에 통합해 네트워크 비디오 재생 — 상태 추적, seekTo, 백그라운드 자동 정지",
        blogUrl = "",
        exampleType = ConstValue.Media3VideoPlayerExample
    ),
    ExampleObject(
        lastUpdate = "26. 07. 30",
        title = "Realtime Waveform Canvas (ECG/PPG)",
        description = "생체신호 파형(ECG/PPG)을 Canvas로 렌더링 — 고정 샘플레이트, 링 버퍼, Sweep/Scroll 두 렌더 모드",
        blogUrl = "",
        exampleType = ConstValue.WaveformCanvasExample
    ),
    ExampleObject(
        lastUpdate = "26. 07. 31",
        title = "백그라운드 위치 추적 (Foreground Service + WorkManager)",
        description = "Foreground Service로 끊기지 않는 위치 추적 구현, 단발 스냅샷인 WorkManager와의 한계를 대조",
        blogUrl = "",
        exampleType = ConstValue.BackgroundLocationExample
    ),
    ExampleObject(
        lastUpdate = "26. 08. 03",
        title = "LazyList contentType 재사용 풀 함정",
        description = "LazyColumn contentType에 고유값을 넘기면 재사용 풀이 무력화돼 메모리 누수 발생 — key와의 역할 차이 정리",
        blogUrl = blogUrl(206),
        exampleType = ConstValue.LazyListReusePoolExample
    ),
    ExampleObject(
        lastUpdate = "26. 08. 05",
        title = "Compose Grid API (non-lazy 2D 레이아웃)",
        description = "Compose 1.11 실험 API Grid로 CSS Grid 닮은 2차원 레이아웃 구성 — 트랙 크기, fr 비율, LazyVerticalGrid 대조",
        blogUrl = "",
        exampleType = ConstValue.GridLayoutExample
    ),
    ExampleObject(
        lastUpdate = "26. 08. 06",
        title = "Compose MediaQuery API (선언적 환경 적응)",
        description = "Compose 1.11 실험 API MediaQuery로 환경 조건을 람다로 질의 — 활성화 함정과 리컴포지션 범위 차이 실측",
        blogUrl = "",
        exampleType = ConstValue.MediaQueryExample
    )
)
