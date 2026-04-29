package com.example.composesample.presentation.example.list

import com.example.composesample.util.ConstValue
import com.example.domain.model.ExampleObject

val examples2026 = listOf(
    ExampleObject(
        lastUpdate = "26. 01. 10",
        title = "Quick Settings Tile",
        description = "빠른 설정 타일을 활용한 마이크로 인터랙션 패턴 구현",
        blogUrl = "https://heegs.tistory.com/194",
        exampleType = ConstValue.QuickSettingsTileExample
    ),
    ExampleObject(
        lastUpdate = "26. 01. 17",
        title = "Fancy TopAppBar",
        description = "Collapsing Toolbar와 다양한 스크롤 동작을 가진 고급 TopAppBar 구현",
        blogUrl = "https://heegs.tistory.com/195",
        exampleType = ConstValue.FancyTopAppBarExample
    ),
    ExampleObject(
        lastUpdate = "26. 01. 19",
        title = "Canvas Shapes & Animations",
        description = "Canvas를 활용한 도형 그리기와 애니메이션 기초",
        blogUrl = "https://heegs.tistory.com/196",
        exampleType = ConstValue.CanvasShapesExample
    ),
    ExampleObject(
        lastUpdate = "26. 01. 20",
        title = "Responsive TabRow",
        description = "SubcomposeLayout을 활용한 반응형 탭 구현",
        exampleType = ConstValue.ResponsiveTabRowExample
    ),
    ExampleObject(
        lastUpdate = "26. 01. 25",
        title = "Custom Text Rendering",
        description = "TextMeasurer와 Canvas를 활용한 커스텀 텍스트 렌더링",
        exampleType = ConstValue.CustomTextRenderingExample
    ),
    ExampleObject(
        lastUpdate = "26. 01. 26",
        title = "Swipe to Dismiss (Material 3)",
        description = "Material 3의 SwipeToDismissBox를 활용한 스와이프 제스처",
        exampleType = ConstValue.SwipeToDismissM3Example
    ),
    ExampleObject(
        lastUpdate = "26. 02. 05",
        title = "Shared Element Transitions",
        description = "화면 간 요소를 부드럽게 전환하는 공유 요소 애니메이션",
        exampleType = ConstValue.SharedElementTransitionExample
    ),
    ExampleObject(
        lastUpdate = "26. 02. 07",
        title = "Dial Component",
        description = "Canvas로 구현하는 원형 다이얼: 범위 설정, 스냅, 멀티턴",
        exampleType = ConstValue.DialComponentExample
    ),
    ExampleObject(
        lastUpdate = "26. 02. 13",
        title = "Embedded Photo Picker",
        description = "앱 내에서 직접 포토 피커를 임베드하여 사진/영상을 선택하는 방법",
        blogUrl = "https://heegs.tistory.com/190",
        exampleType = ConstValue.EmbeddedPhotoPickerExample
    ),
    ExampleObject(
        lastUpdate = "26. 02. 16",
        title = "CompositionLocal Tree Visualization",
        description = "Composition Tree에서 CompositionLocal의 데이터 흐름, 룩업, 섀도잉을 시각화",
        blogUrl = "https://heegs.tistory.com/191",
        exampleType = ConstValue.CompositionLocalTreeExample
    ),
    ExampleObject(
        lastUpdate = "26. 02. 19",
        title = "Retain API (Goodbye ViewModel)",
        description = "Compose 1.10 retain API로 ViewModel 없이 상태 보존하는 패턴",
        blogUrl = "https://heegs.tistory.com/192",
        exampleType = ConstValue.RetainApiExample
    ),
    ExampleObject(
        lastUpdate = "26. 02. 22",
        title = "Custom TopAppBarScrollBehavior",
        description = "RecyclerView 스크롤 이벤트를 커스텀 TopAppBarScrollBehavior로 처리하는 패턴",
        blogUrl = "https://heegs.tistory.com/193",
        exampleType = ConstValue.CustomScrollBehaviorExample
    ),
    ExampleObject(
        lastUpdate = "26. 02. 28",
        title = "Sticker Canvas (Gestures & Physics)",
        description = "드래그, 핀치 리사이즈, 회전, 스프링 물리, 필오프 애니메이션을 구현한 스티커 캔버스",
        blogUrl = "",
        exampleType = ConstValue.StickerCanvasExample
    ),
    ExampleObject(
        lastUpdate = "26. 03. 04",
        title = "Large Content Viewer",
        description = "iOS의 Large Content Viewer를 Compose로 구현하고, 키보드·스크린 리더 내비게이션 지원",
        blogUrl = "",
        exampleType = ConstValue.LargeContentViewerExample
    ),
    ExampleObject(
        lastUpdate = "26. 03. 11",
        title = "Motion Blur (Spinning Wheel)",
        description = "스피닝 휠에 모션 블러를 적용하는 방법: Ghost Frames, BlurMaskFilter, RenderEffect 비교",
        blogUrl = "",
        exampleType = ConstValue.MotionBlurExample
    ),
    ExampleObject(
        lastUpdate = "26. 03. 15",
        title = "LocalContext for Strings (Anti-Pattern)",
        description = "Compose에서 문자열에 LocalContext 사용 금지: stringResource vs UiText sealed class 패턴",
        blogUrl = "",
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
        lastUpdate = "26. 04. 20",
        title = "Nav3 ViewModel Scope",
        description = "Navigation 3 에서 ViewModel 스코프가 어떻게 달라지는지 시뮬레이션: Nav2 Auto-Scope vs Nav3 기본 동작(스코프 없음) vs NavKey 단위 Store 매핑으로 이전 동작을 복원하는 패턴 비교",
        blogUrl = "",
        exampleType = ConstValue.Nav3ViewModelScopeExample
    )
)
