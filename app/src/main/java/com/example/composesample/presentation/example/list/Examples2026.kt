package com.example.composesample.presentation.example.list

import com.example.composesample.util.ConstValue
import com.example.composesample.presentation.example.model.ExampleObject

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
        blogUrl = "https://heegs.tistory.com/197",
        exampleType = ConstValue.ResponsiveTabRowExample
    ),
    ExampleObject(
        lastUpdate = "26. 01. 25",
        title = "Custom Text Rendering",
        description = "TextMeasurer와 Canvas를 활용한 커스텀 텍스트 렌더링",
        blogUrl = "https://heegs.tistory.com/198",
        exampleType = ConstValue.CustomTextRenderingExample
    ),
    ExampleObject(
        lastUpdate = "26. 01. 26",
        title = "Swipe to Dismiss (Material 3)",
        description = "Material 3의 SwipeToDismissBox를 활용한 스와이프 제스처",
        blogUrl = "https://heegs.tistory.com/199",
        exampleType = ConstValue.SwipeToDismissM3Example
    ),
    ExampleObject(
        lastUpdate = "26. 02. 05",
        title = "Shared Element Transitions",
        description = "화면 간 요소를 부드럽게 전환하는 공유 요소 애니메이션",
        blogUrl = "https://heegs.tistory.com/200",
        exampleType = ConstValue.SharedElementTransitionExample
    ),
    ExampleObject(
        lastUpdate = "26. 02. 07",
        title = "Dial Component",
        description = "Canvas로 구현하는 원형 다이얼: 범위 설정, 스냅, 멀티턴",
        blogUrl = "https://heegs.tistory.com/201",
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
        blogUrl = "https://heegs.tistory.com/202",
        exampleType = ConstValue.StickerCanvasExample
    ),
    ExampleObject(
        lastUpdate = "26. 03. 04",
        title = "Large Content Viewer",
        description = "iOS의 Large Content Viewer를 Compose로 구현하고, 키보드·스크린 리더 내비게이션 지원",
        blogUrl = "https://heegs.tistory.com/203",
        exampleType = ConstValue.LargeContentViewerExample
    ),
    ExampleObject(
        lastUpdate = "26. 03. 11",
        title = "Motion Blur (Spinning Wheel)",
        description = "스피닝 휠에 모션 블러를 적용하는 방법: Ghost Frames, BlurMaskFilter, RenderEffect 비교",
        blogUrl = "https://heegs.tistory.com/204",
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
    )
)
