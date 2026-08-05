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
        description = "AnimationSpec 네 가지(spring, tween, snap, keyframes) 비교: 물리 기반 바운스, 시간 기반 이징, 즉시 전환, 구간별 커스텀 타이밍",
        blogUrl = "",
        exampleType = ConstValue.SpringTweenSnapExample
    ),
    ExampleObject(
        lastUpdate = "26. 04. 02",
        title = "Haptic Feedback",
        description = "LocalHapticFeedback(Compose)과 HapticFeedbackConstants(Android View API)를 사용한 다양한 진동 피드백 타입 비교 및 API 레벨별 지원 범위",
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
        description = "receiveContent modifier로 TextField에서 이미지·파일 붙여넣기 처리: 키보드(IME), 클립보드, 드래그&드롭 출처별 콘텐츠 소비 패턴",
        blogUrl = "",
        exampleType = ConstValue.RichContentTextInputExample
    ),
    ExampleObject(
        lastUpdate = "26. 04. 03",
        title = "FlowRow / FlowColumn (Compose Flexbox)",
        description = "CSS Flexbox에서 영감받은 공식 레이아웃: FlowRow의 줄바꿈 배치, maxItemsInEachRow 제한, weight 공간 분배, FlowColumn 세로 흐름 비교",
        blogUrl = "",
        exampleType = ConstValue.FlowRowLayoutExample
    ),
    ExampleObject(
        lastUpdate = "26. 04. 03",
        title = "Preview-only Composable (@RequiresOptIn)",
        description = "@RequiresOptIn으로 Preview 전용 Composable을 컴파일 타임에 강제 제한: @PreviewOnly 어노테이션 정의, @OptIn 허용 패턴, LocalInspectionMode와의 차이",
        blogUrl = "",
        exampleType = ConstValue.PreviewOnlyAnnotationExample
    ),
    ExampleObject(
        lastUpdate = "26. 04. 06",
        title = "Coroutine Bridges (콜백 → suspend 변환)",
        description = "suspendCoroutine과 suspendCancellableCoroutine으로 콜백 기반 Android API를 suspend 함수로 변환하는 패턴: 성공/실패 처리, 취소 전파, invokeOnCancellation 리소스 정리",
        blogUrl = "",
        exampleType = ConstValue.CoroutineBridgesExample
    ),
    ExampleObject(
        lastUpdate = "26. 04. 08",
        title = "Compose Loaders (수학 곡선 애니메이션)",
        description = "Canvas와 삼각함수로 구현하는 수학 로딩 애니메이션: Rose Curve(r=cos(kθ)), Lissajous, Lemniscate(∞), Spirograph(Hypotrochoid), Cardioid, Butterfly Curve 6가지 곡선",
        blogUrl = "",
        exampleType = ConstValue.ComposeLoadersExample
    ),
    ExampleObject(
        lastUpdate = "26. 04. 09",
        title = "TextField Max Length 숨겨진 버그",
        description = "InputTransformation.maxLength()가 프로그래매틱 state 변경에는 적용되지 않는 버그 재현 + LaunchedEffect + snapshotFlow로 상태를 관찰하여 길이를 강제하는 올바른 해결책",
        blogUrl = "",
        exampleType = ConstValue.TextFieldMaxLengthExample
    ),
    ExampleObject(
        lastUpdate = "26. 04. 14",
        title = "Kotlin Name-Based Destructuring",
        description = "Kotlin 2.3.20의 이름 기반 구조 분해: 위치가 아닌 프로퍼티 이름으로 변수를 매칭하여 리팩토링 안전성과 가독성을 확보하는 패턴",
        blogUrl = "",
        exampleType = ConstValue.NameBasedDestructuringExample
    ),
    ExampleObject(
        lastUpdate = "26. 05. 19",
        title = "Foundation Style API (Compose 1.11)",
        description = "디자인 토큰(typography/colors/shapes/spacing)을 단일 Immutable Style 객체로 묶어 하나의 CompositionLocal 로 전파하는 패턴 — Legacy(개별 Local) vs Style API(단일 Local) 비교 + copy() 부분 오버라이드 데모, Light/Dark/Brand 프리셋 토글",
        blogUrl = "",
        exampleType = ConstValue.FoundationStyleApiExample
    ),
    ExampleObject(
        lastUpdate = "26. 04. 15",
        title = "Material 3 Expressive (1.4.0 신규)",
        description = "Material3 1.4.0 신규 컴포넌트: SecureTextField(비밀번호 입력 + 난독화 모드), FloatingToolbar(플로팅 액션 바), VerticalDragHandle, ButtonGroup 개선사항",
        blogUrl = "",
        exampleType = ConstValue.Material3ExpressiveExample
    ),
    ExampleObject(
        lastUpdate = "26. 04. 28",
        title = "Flow Operators (Buffer/Conflate/Debounce/Sample)",
        description = "빠른 producer + 느린 consumer 환경에서 Flow 속도 제어 연산자 4종 비교: 미적용(직렬), buffer(병렬, 모든 값), conflate(최신값만), debounce(입력 종료 후), sample(주기 샘플링) — 타임라인 로그로 시각화",
        blogUrl = "",
        exampleType = ConstValue.FlowOperatorsExample
    ),
    ExampleObject(
        lastUpdate = "26. 06. 05",
        title = "Flow onEachBatch (배치 집계)",
        description = "커스텀 Flow 확장 onEachBatch — 크기(maxSize)와 시간 윈도우(timeout) 기준으로 원소를 배치(List)로 묶어 일괄 처리. 단건 처리 vs 배치 처리(bulk insert) 처리량 비교 + 미완성 배치 flush + buffer/conflate와의 차이를 타임라인 로그로 시각화",
        blogUrl = "",
        exampleType = ConstValue.FlowBatchingExample
    ),
    ExampleObject(
        lastUpdate = "26. 06. 08",
        title = "코루틴 Race Condition 방지",
        description = "여러 코루틴이 공유 가변 상태를 동시에 증가시킬 때 발생하는 race condition을 재현하고 4가지 보호 전략을 비교: ① 비보호(var, 손실 발생) ② AtomicInteger(CAS) ③ Mutex.withLock(상호 배제) ④ 단일 스레드 confinement(limitedParallelism(1)) — 동일 부하 실행 후 최종값/소요시간 비교",
        blogUrl = "",
        exampleType = ConstValue.RaceConditionExample
    ),
    ExampleObject(
        lastUpdate = "26. 06. 15",
        title = "Kotlin Select Expressions",
        description = "select { } 로 여러 suspending 작업을 경쟁시켜 가장 먼저 끝난 결과만 채택하는 3가지 패턴: ① onAwait — 여러 미러 서버를 동시 요청해 최속 응답 채택 후 나머지 취소, ② onTimeout — 주 작업이 한도를 넘기면 캐시 폴백으로 전환, ③ onReceiveCatching — 고속/저속 두 채널을 도착 순서대로 멀티플렉싱 수신. 각 실행을 타임라인 로그로 시각화",
        blogUrl = "",
        exampleType = ConstValue.SelectExpressionExample
    ),
    ExampleObject(
        lastUpdate = "26. 04. 27",
        title = "Modifier Order in Compose",
        description = "동일한 modifier 조합도 순서에 따라 layout/draw/hit-test 결과가 달라진다: padding↔background, border↔clip, clickable↔padding, size↔padding 4가지 시나리오를 좌우 나란히 시각 비교",
        blogUrl = "",
        exampleType = ConstValue.ModifierOrderExample
    ),
    ExampleObject(
        lastUpdate = "26. 04. 23",
        title = "Gemini Nano (ML Kit GenAI 온디바이스)",
        description = "ML Kit GenAI API 구조와 Feature Availability(AVAILABLE/DOWNLOADABLE/UNAVAILABLE) 플로우, Nano 실패 시 Cloud fallback 하이브리드 라우팅 패턴, 요약(Summarization) Mock 데모 — 실기기 제약을 시뮬레이션으로 재현",
        blogUrl = "",
        exampleType = ConstValue.GeminiNanoExample
    ),
    ExampleObject(
        lastUpdate = "26. 04. 22",
        title = "Month Picker Dial (Airbnb ChromaDial)",
        description = "Canvas + atan2 각도 계산과 제스처 조합으로 Airbnb 스타일 원형 월 선택기 구현: 드래그 회전, 스냅 애니메이션, 선택된 월 하이라이트, 12개월 세그먼트 시각화",
        blogUrl = "",
        exampleType = ConstValue.MonthPickerDialExample
    ),
    ExampleObject(
        lastUpdate = "26. 04. 21",
        title = "Koin Compiler Plugin (Annotations)",
        description = "Koin Annotations(KSP)로 @Module·@Single·@Factory·@KoinViewModel을 사용해 DI를 컴파일 타임에 검증: 수동 DSL vs 애노테이션 방식 비교, 생성 코드 구조, 전환 전략",
        blogUrl = "",
        exampleType = ConstValue.KoinCompilerPluginExample
    ),
    ExampleObject(
        lastUpdate = "26. 05. 26",
        title = "Room Database Indices 성능 비교",
        description = "@Index 단일(age)·복합(city,age) 인덱스를 인덱스 없는 테이블과 동일 시드(최대 100k 행)로 비교 — age 범위 조회/city 등호+정렬 두 시나리오의 응답 시간을 measureNanoTime 으로 측정하고, leftmost prefix 규칙(age 단독은 복합 인덱스 미활용)을 시연",
        blogUrl = "",
        exampleType = ConstValue.RoomIndexExample
    ),
    ExampleObject(
        lastUpdate = "26. 05. 21",
        title = "Room FTS4 vs LIKE 검색 성능 비교",
        description = "@Fts4 가상 테이블의 MATCH 연산자(역색인 기반)와 LIKE '%query%' 전체 스캔의 응답 시간/결과 수를 동일 시드(최대 100k 행)로 측정 — prefix 매칭(kotl*), 시드 행 수 조절, 결과 카드 비교",
        blogUrl = "",
        exampleType = ConstValue.RoomFtsSearchExample
    ),
    ExampleObject(
        lastUpdate = "26. 04. 29",
        title = "Multi-Table Inserts in Room",
        description = "BaseInsertDao<T> 인터페이스 상속으로 @Insert 보일러플레이트 제거 + db.withTransaction { } 으로 Author/Post/Tag/CrossRef 4개 테이블을 원자적으로 insert (의도적 실패 시 전체 롤백 검증 포함)",
        blogUrl = "",
        exampleType = ConstValue.MultiTableInsertExample
    ),
    ExampleObject(
        lastUpdate = "26. 04. 30",
        title = "Biometric Auth in Compose",
        description = "androidx.biometric-compose(1.4.0-alpha05)의 rememberAuthenticationLauncher + biometricRequest로 생체 인증 처리: BiometricManager 가용성 진단, Class2/Class3 강도, NegativeButton/DeviceCredential 폴백, AuthenticationResult Success/Error sealed 분기",
        blogUrl = "",
        exampleType = ConstValue.BiometricAuthExample
    ),
    ExampleObject(
        lastUpdate = "26. 05. 04",
        title = "Accessible Focus Indicator (Indication API)",
        description = "키보드/D-pad 사용자를 위한 포커스 시각화 4가지: 기본 indication, Modifier.border + collectIsFocusedAsState 외곽선, scale 강조, IndicationNodeFactory + DrawModifierNode로 구현한 펄스 애니메이션 — 접근성 모범 사례",
        blogUrl = "",
        exampleType = ConstValue.AccessibleFocusIndicatorExample
    ),
    ExampleObject(
        lastUpdate = "26. 05. 06",
        title = "Document Editing TextField",
        description = "TextFieldState 심화 — undoState로 Undo/Redo, selection(TextRange) 직접 조작으로 전체선택/커서 이동/대문자 변환, snapshotFlow 기반 AnnotatedString 마크다운 미리보기, 멀티 커서 시뮬레이션(뒤에서부터 일괄 삽입)",
        blogUrl = "",
        exampleType = ConstValue.DocumentEditingTextFieldExample
    ),
    ExampleObject(
        lastUpdate = "26. 05. 08",
        title = "Particle Emitter (물리 기반 파티클)",
        description = "외부 라이브러리 없이 Canvas + withFrameNanos로 구현한 물리 기반 파티클 시스템: 폭죽(360° 방사형 폭발 + 강한 중력) / 별가루(위쪽 흩날림 + 약한 중력 + drag 감쇠) 두 가지 트리거 효과, dt 기반 적분으로 프레임 레이트 변동 보정, life 기반 알파 페이드아웃, Canvas vs Layout 렌더링 트레이드오프 설명",
        blogUrl = "",
        exampleType = ConstValue.ParticleEmitterExample
    ),
    ExampleObject(
        lastUpdate = "26. 05. 07",
        title = "Syntax Highlighting (간소화 데모)",
        description = "AnnotatedString + 정규식 토크나이저로 Kotlin 코드 하이라이팅 미니 데모: 우선순위 정규식 패턴 + BooleanArray 점유 마스킹으로 주석/문자열 안 키워드 오인식 방지, snapshotFlow 기반 라이브 편집/미리보기 분리, 다크 테마 토큰 색상(키워드/타입/문자열/숫자/주석/함수/어노테이션)",
        blogUrl = "",
        exampleType = ConstValue.SyntaxHighlightingExample
    ),
    ExampleObject(
        lastUpdate = "26. 05. 11",
        title = "App Security 실무 (Cert Pinning + KeyStore AES-GCM + Play Integrity)",
        description = "Android 앱 보안 3대 패턴을 한 화면에서 비교: OkHttp CertificatePinner 로 자가 서명 인증서를 동적 생성해 정상/MITM 핀 매칭 시뮬레이션, AndroidKeyStore 에서 export 불가 AES-256 키 생성 후 AES-GCM 으로 평문 암호화/복호화(IV 매 호출 재생성), Play Integrity verdict 페이로드(appRecognition/deviceRecognition/appLicensing/nonceMatched) Mock 디코딩",
        blogUrl = "",
        exampleType = ConstValue.AppSecurityExample
    ),
    ExampleObject(
        lastUpdate = "26. 04. 20",
        title = "Nav3 ViewModel Scope",
        description = "Navigation 3 에서 ViewModel 스코프가 어떻게 달라지는지 시뮬레이션: Nav2 Auto-Scope vs Nav3 기본 동작(스코프 없음) vs NavKey 단위 Store 매핑으로 이전 동작을 복원하는 패턴 비교",
        blogUrl = "",
        exampleType = ConstValue.Nav3ViewModelScopeExample
    ),
    ExampleObject(
        lastUpdate = "26. 06. 10",
        title = "Nav3 SavedStateHandle 크래시 & 복원",
        description = "Navigation 3 백스택(NavKey)에 복합 객체를 통째로 담으면 프로세스 종료 후 복원 시 역직렬화 크래시가 발생하는 문제를 시뮬레이션: ❌ 람다/런타임 필드를 가진 객체를 키에 담아 복원 실패 재현 vs ✅ 식별자(id)만 키에 담고 SavedStateHandle+Repository(Koin)로 객체를 다시 조회해 안전하게 복원하는 패턴 비교",
        blogUrl = "",
        exampleType = ConstValue.Nav3SavedStateHandleExample
    ),
    ExampleObject(
        lastUpdate = "26. 05. 13",
        title = "Hardware-Backed Keystore 검증",
        description = "AndroidKeyStore 키가 실제 TEE/StrongBox 하드웨어에 보관되는지 런타임 진단: API 23~30 isInsideSecureHardware vs API 31+ securityLevel(SOFTWARE/TRUSTED_ENVIRONMENT/STRONGBOX) 분기, setIsStrongBoxBacked() StrongBoxUnavailableException 폴백, KeyInfo + SecretKeyFactory 메타데이터 조회 패턴",
        blogUrl = "",
        exampleType = ConstValue.HardwareKeystoreExample
    ),
    ExampleObject(
        lastUpdate = "26. 07. 09",
        title = "Screenshot Detection (Android 14 콜백 vs 레거시 MediaStore)",
        description = "화면 캡처를 실시간으로 감지하는 두 가지 방식을 한 화면에서 비교: ① Android 14(API 34+) Activity.registerScreenCaptureCallback() — 권한 불필요, 화면이 보이는 동안 캡처 시점에만 정확히 발화 ② 레거시 MediaStore ContentObserver — READ_MEDIA_IMAGES/READ_EXTERNAL_STORAGE 권한 필요, 새로 삽입된 이미지의 파일명·경로를 휴리스틱으로 매칭(오탐 가능). 실시간 이벤트 로그 + API 레벨별 실무 가이드",
        blogUrl = "",
        exampleType = ConstValue.ScreenshotDetectionExample
    ),
    ExampleObject(
        lastUpdate = "26. 07. 21",
        title = "IPC / Exported Component 보안 진단",
        description = "exported 컴포넌트는 매니페스트 고정값이라 런타임 토글 대신 코드/주석 중심으로 구성: ① PackageManager로 이 앱 자신의 Activity/Service/Receiver/Provider를 실시간 스캔해 exported+permission 진단 ② FLAG_MUTABLE vs FLAG_IMMUTABLE PendingIntent로 fillIn Intent 변조 시도 시 실제 payload가 바뀌는지 비교 ③ signature 커스텀 권한으로 exported 컴포넌트를 강제하는 패턴(CodeBlock)",
        blogUrl = "",
        exampleType = ConstValue.IpcExportedComponentExample
    ),
    ExampleObject(
        lastUpdate = "26. 05. 12",
        title = "Compose Animations Showcase (카탈로그)",
        description = "공통 duration/easing 슬라이더로 5가지 모션 패턴을 한 화면에서 동시 비교: animateXxxAsState(Dp/Color/scale·alpha), AnimatedVisibility(slide+fade combo) + Crossfade, AnimatedContent(SizeTransform) + updateTransition(다중 속성 동기), rememberInfiniteTransition + Drag-driven Animatable spring 복귀",
        blogUrl = "",
        exampleType = ConstValue.AnimationsShowcaseExample
    ),
    ExampleObject(
        lastUpdate = "26. 05. 22",
        title = "Per-Item ViewModels in Compose",
        description = "LazyColumn 의 각 아이템에 독립 ViewModel 스코프를 부여하는 패턴 — 단일 화면 ViewModel 공유 시 발생하는 상태 결합 vs CompositionLocalProvider(LocalViewModelStoreOwner) + 키별 ViewModelStore 매니저 + DisposableEffect onDispose 의 store.clear() 로 메모리 누수 방지하는 per-key 스코프 비교",
        blogUrl = "",
        exampleType = ConstValue.PerItemViewModelExample
    ),
    ExampleObject(
        lastUpdate = "26. 05. 18",
        title = "Shared Element Debug Tooling (Compose 1.11)",
        description = "Compose 1.11 신규 LookaheadAnimationVisualDebugging Composable 로 SharedTransition 매칭 상태를 화면에서 시각화: 정상 매칭(overlayColor) / 동일 key 다중 매칭(multipleMatchesColor) / 한쪽에만 존재하는 미매칭 요소(unmatchedElementColor) 3가지 시나리오를 디버그 토글·색상 프리셋·Key 라벨 표시로 비교. SharedTransition test coroutine API(mainClock.advanceTimeBy / awaitFrame) 스니펫 포함",
        blogUrl = "",
        exampleType = ConstValue.SharedElementDebugToolingExample
    ),
    ExampleObject(
        lastUpdate = "26. 05. 27",
        title = "AGSL Shader Live Tuning",
        description = "API 33+ RuntimeShader + graphicsLayer renderEffect 로 AGSL(SkSL) 셰이더를 실시간 튜닝 — noiseScale/colorShift/속도 uniform 을 슬라이더로 조절하고, 셰이더 소스 자체를 TextField 에서 편집하면 remember 키잉으로 즉시 재컴파일(컴파일 에러 표시). minSdk 24 환경을 위해 미지원 단말은 placeholder UI 로 분기",
        blogUrl = "",
        exampleType = ConstValue.AgslShaderTuningExample
    ),
    ExampleObject(
        lastUpdate = "26. 05. 28",
        title = "Type-Safe Feature Flag",
        description = "외부 라이브러리(KSP/Firebase) 없이 구현한 4가지 패턴: sealed class 기반 type-safe flag 레지스트리(문자열 키 대신 타입으로 정의), StateFlow reactive 토글(collectAsState 구독 UI 자동 재구성), ModalBottomSheet 디버그 메뉴(런타임 강제 오버라이드), Remote Config 시뮬레이션(우선순위 DEBUG > REMOTE > LOCAL)",
        blogUrl = "",
        exampleType = ConstValue.FeatureFlagExample
    ),
    ExampleObject(
        lastUpdate = "26. 06. 04",
        title = "Compose Autofill (semantics API)",
        description = "Compose Foundation 자동완성: Modifier.semantics { contentType = ... } 로 TextField 에 Username/Password/Email/PostalCode 힌트를 부여하고, LocalAutofillManager 로 commit/cancel 을 트리거 — 로그인/회원가입 폼 시나리오. 실제 자동완성 UI 는 OS/단말 자동완성 서비스에 의존하므로 힌트 부여 패턴과 가용성 설명 중심",
        blogUrl = "",
        exampleType = ConstValue.AutofillExample
    ),
    ExampleObject(
        lastUpdate = "26. 06. 09",
        title = "StrictMode 위반 감지",
        description = "메인 스레드의 디스크/네트워크 I/O(ThreadPolicy)와 닫지 않은 Closeable 누수(VmPolicy)를 의도적으로 재현하고 penaltyListener 로 위반을 실시간 수집해 화면에 표시. detectDiskReads/detectDiskWrites/detectNetwork + detectLeakedClosableObjects 사용, penaltyListener 는 API 28+ 필요(미만은 penaltyLog→logcat). 외부 라이브러리(Strictly) 없이 순수 StrictMode API 로 구현",
        blogUrl = "",
        exampleType = ConstValue.StrictModeExample
    ),
    ExampleObject(
        lastUpdate = "26. 06. 11",
        title = "Advanced Ktor Config (Auth/Retry)",
        description = "Ktor MockEngine 으로 실서버 없이 고급 클라이언트 구성을 시연 — ① Auth bearer 플러그인: 만료 토큰으로 요청 → 401 수신 → refreshTokens() 자동 호출 → 새 토큰으로 재요청 성공, ② HttpRequestRetry 플러그인: 503 두 번 → 지수 백오프(delayMillis) 후 재시도 → 3번째 200 복구, ③ 대조군: 플러그인 없이 401/503 이 그대로 실패로 노출. 각 시나리오를 실시간 타임라인 로그로 비교",
        blogUrl = "",
        exampleType = ConstValue.KtorAdvancedConfigExample
    ),
    ExampleObject(
        lastUpdate = "26. 06. 19",
        title = "Kotlin 2.4 Language Features",
        description = "Kotlin 2.4의 두 가지 신규 문법을 시연: ① 컬렉션 리터럴 — Swift 스타일 대괄호 [1, 2, 3] 로 List/Set/Map 을 기대 타입 추론으로 생성(커스텀 타입은 operator fun of 지원), ② 컨텍스트 파라미터 — context(logger: Logger) 로 의존성을 암시적 주입해 인자 전달 보일러플레이트 제거(deprecated context receivers 대체). 두 기능 모두 Experimental 이라 새 문법은 코드 블록으로 보여주고 실제 실행은 동등한 stable 코드(listOf/명시적 파라미터)로 대체, 전역 적용 금지·예제 단위 opt-in 강조",
        blogUrl = "",
        exampleType = ConstValue.Kotlin24FeaturesExample
    ),
    ExampleObject(
        lastUpdate = "26. 06. 22",
        title = "How Compose Works (내부 동작)",
        description = "Jetpack Compose가 선언형 UI를 실제로 동작시키는 4단계 파이프라인을 한 화면에서 통합 시연: ① 컴파일러 변환 — @Composable 함수에 \$composer 파라미터와 startRestartGroup/endRestartGroup 그룹 호출이 삽입되는 과정, ② SlotTable — 컴포지션 결과가 그룹·슬롯으로 저장되고 리컴포지션 시 위치 기반으로 재사용되는 구조, ③ Snapshot 읽기 추적 — state 를 읽은 Composable 만 무효화되는 read-tracking 을 두 독립 카운터로 실측(컴포지션 횟수 표시), ④ Layout Pipeline — measure → place → draw 3단계를 단계별로 시각화. 내부 구현 대신 개념+CodeBlock+동등 시뮬레이션으로 안전하게 재현",
        blogUrl = "",
        exampleType = ConstValue.HowComposeWorksExample
    ),
    ExampleObject(
        lastUpdate = "26. 07. 15",
        title = "RememberObserver / Composition Lifecycle",
        description = "remember 된 객체가 RememberObserver(onRemembered/onForgotten/onAbandoned)를 구현하면 Compose 런타임이 컴포지션 진입·이탈·폐기 시점에 자동으로 호출해줌을 실동작으로 시연: ① 컴포지션에서 자식을 추가/제거해 onRemembered/onForgotten 이 실제로 발화하는 이벤트 로그, ② '리컴포지션만 유발' 버튼으로 recomposition 은 remember 슬롯을 재생성하지 않아 콜백이 다시 발화하지 않음을 컴포지션 횟수와 대조, ③ DisposableEffect(key 변경마다 반응·onDispose 단일 통합)와의 차이 비교, ④ Compose 런타임 내부에서 rememberCoroutineScope() 가 동일한 패턴으로 onForgotten 시 scope.cancel() 을 호출하는 개념 재현. 외부 라이브러리 미사용, HowComposeWorks 옆에 배치",
        blogUrl = "",
        exampleType = ConstValue.RememberObserverExample
    ),
    ExampleObject(
        lastUpdate = "26. 06. 23",
        title = "Coil 3 이미지 로딩 & 캐시",
        description = "Coil 3로 네트워크 이미지를 비동기 로딩하는 핵심 패턴을 한 화면에서 시연: ① AsyncImage 상태 — crossfade 전환과 placeholder/error Painter 를 적용하고 onState 콜백으로 Loading→Success/Error 를 실시간 배지로 표시(정상 URL ↔ 깨진 URL 토글로 에러 폴백 확인), ② 캐시 정책 — 같은 이미지를 memoryCachePolicy ENABLED/DISABLED 로 재요청해 SuccessResult.dataSource 가 NETWORK 인지 MEMORY_CACHE 인지 추적(캐시 비우기 버튼 포함), ③ ImageLoader 커스터마이징 — MemoryCache.maxSizePercent + DiskCache 구성을 CodeBlock 으로 보여주고 커스텀 ImageLoader 의 메모리 캐시 사용량을 라이브 표시. 기존 Coil 2(coil.*)와 별도 coil3.* 네임스페이스로 공존. 네트워크가 없으면 자동으로 error 상태가 노출되어 오프라인에서도 동작 시연 가능",
        blogUrl = "",
        exampleType = ConstValue.Coil3ImageExample
    ),
    ExampleObject(
        lastUpdate = "26. 06. 24",
        title = "Preview-Driven Screenshot Testing",
        description = "@Preview를 단일 진실 공급원(source of truth)으로 삼아 스크린샷 테스트 매트릭스를 자동 파생하는 각도를 라이브로 시연: ① 하나의 샘플 컴포넌트를 정의하고, ② locale(en/ko/ar-RTL) × fontScale(0.85/1.0/1.3) × theme(Light/Dark) 차원을 FilterChip으로 토글하면, ③ 선택된 차원의 데카르트 곱만큼 변형이 실시간으로 그리드 렌더링되며 'N×M×K = 총 변형 수'가 즉시 갱신됨(CompositionLocalProvider로 LocalDensity의 fontScale·LocalLayoutDirection의 RTL을 실제 적용). 각 매트릭스 셀이 곧 하나의 골든 이미지에 대응한다는 매핑과, @PreviewParameter/멀티프리뷰 애노테이션으로 이 매트릭스를 코드로 표현하는 패턴을 CodeBlock으로 제시. Paparazzi/Roborazzi 실행 메커니즘은 기존 Screenshot Testing 예제 참조",
        blogUrl = "",
        exampleType = ConstValue.PreviewDrivenScreenshotExample
    ),
    ExampleObject(
        lastUpdate = "26. 06. 29",
        title = "Freehand Drawing (Signature Canvas)",
        description = "외부 라이브러리 없이 Compose Canvas + pointerInput(detectDragGestures)만으로 자유 곡선 드로잉을 구현하고, DrawBox의 MVI 아키텍처를 차용해 상태를 관리: ① 드래그 제스처(onDragStart→onDrag→onDragEnd)로 점(Offset)을 누적해 하나의 스트로크를 만들고 drawPath(StrokeCap/Join.Round)로 렌더링, ② DrawIntent(StartStroke/Drag/EndStroke/Undo/Redo/Clear/SetColor/SetWidth) sealed interface + 순수 reduce() 리듀서로 단방향 상태 흐름을 구성, ③ 완료된 스트로크를 불변 List로 보관해 Undo는 strokes를 redoStack으로, Redo는 그 반대로 이동(새 스트로크를 그리면 redoStack 무효화), ④ 색상 팔레트와 굵기 Slider로 currentColor/strokeWidth를 변경. PNG 내보내기는 graphicsLayer.toImageBitmap()→Bitmap.compress 파이프라인을 CodeBlock으로 제시",
        blogUrl = "",
        exampleType = ConstValue.FreehandDrawingExample
    ),
    ExampleObject(
        lastUpdate = "26. 07. 10",
        title = "Advanced Repository Pattern",
        description = "다중 소스를 Memory → Disk → Network 우선순위로 해석하는 Repository 패턴을 한 화면에서 시연: ① 조회 시 가장 빠른 계층(Memory)부터 확인하고 없으면 Disk(300ms)·Network(900ms) 순으로 내려가며, 하위 계층에서 찾은 값은 상위 계층에도 채워 넣어(cache population) 다음 조회를 가속, ② 강제 새로고침은 Memory/Disk를 모두 건너뛰고 Network로 직행, ③ 메모리/디스크 무효화 버튼으로 두 캐시가 서로 독립적임을 실시간 타임라인 로그(MEMORY/DISK/NETWORK 색상 배지 + 소요시간)로 확인. ArticleRepository 인터페이스는 순수 Kotlin 모델(ArticleData)이라 domain 모듈에 위치 — Room @Entity(UserData)라 data 레이어에 둬야 했던 UserCacheRepository 예제와 대조",
        blogUrl = "",
        exampleType = ConstValue.AdvancedRepositoryPatternExample
    ),
    ExampleObject(
        lastUpdate = "26. 07. 16",
        title = "Media3 비디오 재생 (ExoPlayer)",
        description = "androidx.media3 ExoPlayer + PlayerView 를 Compose 에 통합해 실제 네트워크 비디오를 재생: ① AndroidView 로 PlayerView 를 임베딩하고 remember 로 생성한 ExoPlayer 인스턴스를 1회만 바인딩(MediaItem.fromUri 로 스트림 URL 재생), ② Player.Listener(onIsPlayingChanged/onPlaybackStateChanged)로 IDLE/BUFFERING/READY/ENDED 상태와 재생 여부를 실시간 추적하고 LaunchedEffect 폴링으로 현재 위치/전체 길이를 mm:ss 로 표시, ③ 재생/일시정지/처음으로 버튼 + Slider seekTo 탐색, ④ 화면을 벗어나면 AndroidView onRelease 에서 player.release() 호출(WebViewIssueUI 의 리소스 정리 관례와 동일), 앱이 백그라운드로 전환되면(OnLifecycleEvent ON_STOP) 자동 일시정지해 백그라운드 재생/네이티브 리소스 누수 방지",
        blogUrl = "",
        exampleType = ConstValue.Media3VideoPlayerExample
    ),
    ExampleObject(
        lastUpdate = "26. 07. 30",
        title = "Realtime Waveform Canvas (ECG/PPG)",
        description = "환자 모니터처럼 끊임없이 흘러가는 생체신호 파형을 외부 라이브러리 없이 Canvas로 렌더링: ① 고정 샘플레이트(250Hz)와 가변 프레임레이트를 분리 — withFrameNanos의 dt로 이번 프레임에 밀어 넣을 샘플 개수를 계산하고 소수부는 다음 프레임으로 이월해 기기 주사율(60/90/120Hz)과 무관하게 동일한 시간축 유지, ② 고정 크기 FloatArray 링 버퍼(head 인덱스 순환)로 프레임당 할당·리스트 재구성 0 — 스냅샷 리스트 대신 평범한 배열을 쓰는 이유를 대조, ③ Sweep(커서가 좌→우로 훑으며 지난 파형을 덮어씀, 병원 모니터 방식)과 Scroll(파형 전체가 우→좌로 흐름, 오실로스코프 방식) 두 렌더 모드를 같은 버퍼로 구현, ④ ECG는 P-Q-R-S-T 복합파를 가우시안 합으로, PPG는 수축기 피크+중복맥 봉우리로 합성하고 BPM·잡음 슬라이더로 실시간 조절, ⑤ 파형 갱신 상태를 컴포지션이 아닌 드로우 단계에서 읽어 리컴포지션 0회로 재드로우만 유발하는 패턴을 컴포지션/드로우 횟수 카운터로 실측 대조",
        blogUrl = "",
        exampleType = ConstValue.WaveformCanvasExample
    ),
    ExampleObject(
        lastUpdate = "26. 07. 31",
        title = "백그라운드 위치 추적 (Foreground Service + WorkManager)",
        description = "앱을 벗어나도 끊기지 않는 위치 추적을 Foreground Service 로 구현하고, 같은 일을 WorkManager 로 했을 때의 한계를 한 화면에서 대조: ① 권한이 하나가 아니라 절차라는 점을 실동작으로 확인 — 포그라운드 위치(FINE/COARSE) → 알림(POST_NOTIFICATIONS, API 33+) → 백그라운드 위치(ACCESS_BACKGROUND_LOCATION, API 29+) 순서로만 받을 수 있고 Android 11+ 는 마지막 단계를 런타임 다이얼로그로 받을 수 없어 앱 설정 화면으로 유도해야 함(ON_RESUME 마다 권한 재확인), ② foregroundServiceType=\"location\" 서비스를 실제로 시작해 홈 버튼으로 앱을 내려도 알림이 남고 경과 시간·위치 수신 횟수가 계속 증가하는 것을 확인 — LocationManager.requestLocationUpdates 구독 + 첫 fix 전에는 getLastKnownLocation 으로 초기값 표시, 알림은 매초가 아니라 5초 주기로만 갱신, ③ 5초 안에 startForeground 를 부르지 않으면 프로세스가 죽는 제약·START_STICKY 재생성 시 intent 가 null 로 들어오는 분기·Android 12+ 백그라운드 시작 제한·Android 14+ 런타임 권한 요구를 코드와 함께 정리, ④ 대조군으로 CoroutineWorker 를 즉시/15분 주기로 실행해 '구독이 아니라 단발 스냅샷'임을 보이고, PeriodicWorkRequest 최소 주기 15분과 work-runtime 의 SystemForegroundService 가 foregroundServiceType 을 선언하지 않는다는 사실로 지속 추적을 WorkManager 로 대체할 수 없는 이유를 설명",
        blogUrl = "",
        exampleType = ConstValue.BackgroundLocationExample
    ),
    ExampleObject(
        lastUpdate = "26. 08. 03",
        title = "LazyList contentType 재사용 풀 함정",
        description = "LazyColumn 의 contentType 에 아이템 고유값을 넘기면 메모리가 회수되지 않는 이유를 실측으로 확인: ① Compose 는 화면 밖으로 나간 아이템의 컴포지션(슬롯)을 재사용 풀에 넣어 뒀다가 같은 contentType 의 새 아이템에 돌려 쓰는데, 풀 정리 규칙이 'contentType 당 7개까지만 유지'라 전체 슬롯 수에는 상한이 없다(foundation 1.11.1 의 LazyLayoutItemReusePolicy.getSlotsToRetain 을 디스어셈블해 확인), ② 그래서 contentType 에 아이템마다 다른 값을 넘기면 버킷이 아이템 수만큼 생기고 각 버킷에 1개씩만 들어 있어 정리 조건에 영원히 걸리지 않는다 — 스크롤로 지나친 아이템의 슬롯이 전부 남는다, ③ 지정 안 함(null, 버킷 1개) / 클래스 단위(item::class, 버킷 2개) / 아이템 고유값(버킷 200개) 세 모드를 같은 리스트에 적용하고 자동 스크롤로 끝까지 훑은 뒤 System.gc() 를 유도해 WeakReference 로 살아남은 페이로드 수·보유 크기·힙 사용량을 대조, ④ 남은 슬롯이 붙들고 있는 것은 빈 껍데기가 아니라 remember 값과 modifier 람다가 캡처한 객체라는 점을 아이템당 64KB 페이로드를 drawBehind 람다에 캡처시켜 재현, ⑤ key(식별자 — 아이템마다 달라야 함)와 contentType(분류 — 레이아웃 종류만큼만) 의 역할 차이와 '값의 가짓수가 데이터 양에 비례하면 잘못 쓴 것'이라는 판별 기준 정리",
        blogUrl = "",
        exampleType = ConstValue.LazyListReusePoolExample
    ),
    ExampleObject(
        lastUpdate = "26. 08. 05",
        title = "Compose Grid API (non-lazy 2D 레이아웃)",
        description = "Compose 1.11 의 실험 API 인 Grid 로 CSS Grid 를 닮은 2차원 트랙 레이아웃을 구성: ① 트랙 크기 6종(Fixed/Percentage/Flex(fr)/Auto/MinContent/MaxContent)과 minmax 를 같은 격자에 나란히 두고 컨테이너 폭 슬라이더로 줄여가며 '고정은 그대로, 퍼센트는 비례, fr 은 남은 공간만 나눠 갖는다'는 해석 순서를 실시간으로 관측, ② fr 비율(1fr:2fr:3fr)과 gap/columnGap/rowGap 을 슬라이더로 조절해 트랙 간격이 남은 공간 계산에 먼저 반영되는 것을 확인, ③ 자동 배치와 Modifier.gridItem(row, column, rowSpan, columnSpan) 명시 배치를 대조해 헤더-사이드바-본문 대시보드를 구성하고, IntRange 오버로드(gridItem(0..1, 1..2))가 같은 배치의 다른 표기임을 보임 — 명시 배치는 겹침을 막아 주지 않아 좌표가 충돌하면 아이템이 그대로 포개진다는 함정을 토글로 재현, ④ GridFlow.Row/Column 토글로 자동 배치 커서의 진행 방향이 바뀌는 것을 확인, ⑤ Grid 는 lazy 가 아니므로 화면 밖 셀도 전부 컴포즈된다는 점을 LazyVerticalGrid 와 나란히 두고 살아있는 자식 컴포지션 수로 실측(Grid 는 개수 고정, LazyVerticalGrid 는 스크롤에 따라 증감). 이름 붙인 영역(area()/gridItem(area))은 Compose 1.12+ 신규라 이 예제 범위에서 제외",
        blogUrl = "",
        exampleType = ConstValue.GridLayoutExample
    ),
    ExampleObject(
        lastUpdate = "26. 08. 06",
        title = "Compose MediaQuery API (선언적 환경 적응)",
        description = "Compose 1.11 의 실험 API 인 MediaQuery 로 \"지금 환경이 이 조건을 만족하는가\" 를 람다로 질의한다: ① 이 API 의 최대 함정인 활성화 — LocalUiMediaScope 는 기본값이 없고 플랫폼은 ComposeUiFlags.isMediaQueryIntegrationEnabled(기본 false)가 true 일 때만 이 CompositionLocal 을 제공하므로, 끈 채로 mediaQuery { } 를 부르면 컴파일은 통과하고 실행 시점에 IllegalStateException 이 난다. 게다가 플랫폼 구현을 만드는 obtainUiMediaScope() 는 Kotlin internal 이라 직접 제공하는 우회로도 없어, setContent 이전에 플래그를 켜는 것이 유일한 경로다(BlogExampleActivity.onCreate 에서 활성화), ② 플랫폼이 제공한 UiMediaScope 의 8개 속성(windowWidth/windowHeight/windowPosture/pointerPrecision/keyboardKind/viewingDistance/hasCamera/hasMicrophone)을 실제 기기 값으로 표시해 회전·멀티윈도우·키보드 표시에 따라 즉시 갱신되는 것을 확인, ③ mediaQuery 와 derivedMediaQuery 의 리컴포지션 범위 차이를 실측 — 폭만 조작하는 시뮬레이션 UiMediaScope 를 provide 하고 슬라이더를 움직이며 두 자식의 리컴포지션 횟수를 SideEffect 로 세면, 즉시 평가해 Boolean 을 돌려주는 mediaQuery 쪽은 1dp 변화마다 오르고 derivedStateOf 로 감싼 derivedMediaQuery 쪽은 기준선(600dp)을 넘을 때만 오른다, ④ 폭·자세(Tabletop)·포인터 정밀도(Coarse) 세 질의를 조합해 레이아웃을 고르는 선언적 분기를 실제 기기 상태로 시연. 기존 AdaptiveLayout 예제(material3 WindowSizeClass)와 달리 ui 레이어에서 크기 밖의 환경까지 질의한다는 점이 축의 차이",
        blogUrl = "",
        exampleType = ConstValue.MediaQueryExample
    )
)
