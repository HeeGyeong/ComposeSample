package com.example.composesample.util

// TODO(UTIL-01): 파일이 200줄 이상으로 커지면서 예제 추가 시 ExampleRouter.kt(import 150+줄)와 함께 수정 충돌 포인트가 됨.
//  카테고리별 const 파일 분할 또는 sealed class 기반 라우팅 테이블로 재설계 필요 (별도 PR 권장).
object ConstValue {
    // ==================== 기본 설정 ====================
    const val UpdateDate = "26년 6월"

    // ==================== Intent & Code Type ====================
    const val IntentType = "type"
    const val ExampleType = "example"

    // ==================== Shortcut 관련 ====================
    // ShortCutKey: 숏컷 키, ShortCutTypeXML: XML 정의, ShortCutTypeDynamic: 동적 생성, ShortCutTypePin: 핀 고정
    const val ShortCutKey = "shortcut_key"
    const val ShortCutTypeXML = "type_xml"
    const val ShortCutTypeDynamic = "type_dynamic"
    const val ShortCutTypePin = "type_pin"

    // ==================== 외부 URL ====================
    const val SSEWikiURL = "https://stream.wikimedia.org/v2/stream/recentchange"

    // ==================== UI 컴포넌트 예제 ====================
    // LazyColumn(키보드 이슈), LazyStaggeredGrid(폭포수 그리드), ClickEvent(중복 방지), FlexBox(유동 레이아웃), WebView(YouTube), TextStyle(스타일링), StickyHeader(고정 헤더), ReverseLazyColumn(역방향), AutoSizingText(자동 크기), CardCorners(모서리 스타일), ButtonGroup(M3 Expressive), Visibility(가시성 처리), FancyTopAppBar(고급 TopAppBar), CanvasShapes(Canvas 도형 그리기), ResponsiveTabRow(반응형 탭), CustomTextRendering(커스텀 텍스트 렌더링), DialComponent(Dial 컴포넌트), EmbeddedPhotoPicker(임베디드 포토 피커), LargeContentViewer(Large Content Viewer 접근성), MotionBlur(스피닝 휠 모션 블러), EmbeddedPickerCompose(BottomSheet 통합 + URI 수명 + 선택 동기화), AdaptiveLayout(WindowSizeClass 반응형 레이아웃), CustomLayout(MeasurePolicy 커스텀 레이아웃), RichContentTextInput(contentReceiver로 이미지/파일 붙여넣기), FlowRowLayout(FlowRow/FlowColumn 공식 Flexbox 레이아웃), ComposeLoaders(Rose/Lissajous/Lemniscate/Spirograph/Cardioid/Butterfly 수학 곡선 로딩 애니메이션), TextFieldMaxLength(InputTransformation이 프로그래매틱 변경에 미적용되는 숨겨진 버그 + LaunchedEffect 해결책), DocumentEditingTextField(undoState/selection/AnnotatedString 미리보기/멀티 커서 시뮬레이션), SyntaxHighlighting(AnnotatedString + 정규식 토크나이저로 Kotlin 코드 하이라이팅 미니 데모), ParticleEmitter(Canvas + withFrameNanos 기반 물리 파티클: 폭죽/별가루), Material3Expressive(M3 1.4.0 신규 컴포넌트 — SecureTextField/FloatingToolbar), ModifierOrder(modifier 순서가 layout/draw/hit-test에 미치는 영향 비교)
    const val LazyColumnExample = "lazyColumnExample"
    const val LazyStaggeredGridExample = "lazyStaggeredGridExample"
    const val FancyTopAppBarExample = "fancyTopAppBarExample"
    const val CanvasShapesExample = "canvasShapesExample"
    const val ResponsiveTabRowExample = "responsiveTabRowExample"
    const val CustomTextRenderingExample = "customTextRenderingExample"
    const val DialComponentExample = "dialComponentExample"
    const val MonthPickerDialExample = "monthPickerDialExample"
    const val ParticleEmitterExample = "particleEmitterExample"
    const val EmbeddedPhotoPickerExample = "embeddedPhotoPickerExample"
    const val ClickEventExample = "clickEventExample"
    const val FlexBoxLayoutExample = "flexBoxLayoutExample"
    const val WebViewIssueExample = "webViewIssueExample"
    const val TextStyleExample = "textStyleExample"
    const val StickyHeaderExample = "stickyHeaderExample"
    const val ReverseLazyColumnExample = "reverseLazyColumnExample"
    const val AutoSizingTextExample = "autoSizingTextExample"
    const val CardCornersExample = "cardCornersExample"
    const val ButtonGroupExample = "buttonGroupExample"
    const val VisibilityExample = "visibilityExample"
    const val LargeContentViewerExample = "largeContentViewerExample"
    const val MotionBlurExample = "motionBlurExample"
    const val EmbeddedPickerComposeExample = "embeddedPickerComposeExample"
    const val AdaptiveLayoutExample = "adaptiveLayoutExample"
    const val CustomLayoutExample = "customLayoutExample"
    const val RichContentTextInputExample = "richContentTextInputExample"
    const val FlowRowLayoutExample = "flowRowLayoutExample"
    const val ComposeLoadersExample = "composeLoadersExample"
    const val TextFieldMaxLengthExample = "textFieldMaxLengthExample"
    const val DocumentEditingTextFieldExample = "documentEditingTextFieldExample"
    const val SyntaxHighlightingExample = "syntaxHighlightingExample"
    const val Material3ExpressiveExample = "material3ExpressiveExample"
    const val ModifierOrderExample = "modifierOrderExample"

    // Autofill(Compose Foundation 자동완성 — Modifier.semantics { contentType } 로 Username/Password/Email/PostalCode 힌트 부여 + LocalAutofillManager commit/cancel 트리거, 가용성은 OS/단말 의존)
    const val AutofillExample = "autofillExample"

    // AgslShaderTuning(API 33+ RuntimeShader + graphicsLayer renderEffect 로 AGSL 셰이더 실시간 튜닝: uniform 슬라이더 조절 + 셰이더 소스 라이브 편집/재컴파일, 미지원 단말 placeholder)
    const val AgslShaderTuningExample = "agslShaderTuningExample"

    // FoundationStyleApi(Compose 1.11 Foundation Style API: MaterialTheme + CompositionLocal 토큰 확장 vs Style 객체 단일 전달 패턴 비교)
    const val FoundationStyleApiExample = "foundationStyleApiExample"

    // ==================== 스크롤 & 제스처 예제 ====================
    // PullToRefresh(당겨서 새로고침), PullScreenPager(풀스크린 페이저), FlingBehavior(스크롤 커스텀), SwipeToDismiss(스와이프 삭제), SwipeToDismissM3(M3 스와이프 삭제), DragAndDrop(드래그 앤 드롭), NestedScrolling(중첩 스크롤), CustomScrollBehavior(커스텀 TopAppBarScrollBehavior), StickerCanvas(스티커 제스처 캔버스)
    const val PullToRefreshExample = "pullToRefreshExample"
    const val PullScreenPager = "pullScreenPager"
    const val FlingBehaviorExample = "flingBehaviorExample"
    const val SwipeToDismissExample = "swipeToDismissExample"
    const val SwipeToDismissM3Example = "swipeToDismissM3Example"
    const val DragAndDropExample = "dragAndDropExample"
    const val NestedScrollingExample = "nestedScrollingExample"
    const val CustomScrollBehaviorExample = "customScrollBehaviorExample"
    const val StickerCanvasExample = "stickerCanvasExample"

    // ==================== BottomSheet 예제 ====================
    // BottomSheet(Scaffold), ModalBottomSheet(Modal), CustomBottomSheet(커스텀)
    const val BottomSheetExample = "bottomSheetExample"
    const val ModalBottomSheetExample = "modalBottomSheetExample"
    const val CustomBottomSheetExample = "customBottomSheetExample"

    // ==================== Navigation Drawer 예제 ====================
    // ScaffoldDraw(Scaffold 기반), ModalDraw(Modal 기반)
    const val ScaffoldDrawExample = "scaffoldDrawExample"
    const val ModalDrawExample = "modalDrawExample"

    // ==================== 애니메이션 & 효과 예제 ====================
    // Animation(다양한 애니메이션), AnimatedContent(콘텐츠 전환 심화), Lottie(Lottie/GIF), UIShimmer(로딩 효과), TextShimmer(텍스트 효과), NewShadowApi(Compose 1.9 Shadow), SharedElementTransition(공유 요소 전환), SharedElementDebugTooling(Compose 1.11 LookaheadAnimationVisualDebugging 오버레이로 matched/multipleMatches/unmatched 시각화 + test coroutine API 안내), SpringTweenSnap(spring/tween/snap/keyframes 비교), AnimationsShowcase(공통 duration/easing 슬라이더로 5가지 패턴 동시 비교 — animateXxxAsState / AnimatedVisibility+Crossfade / AnimatedContent+updateTransition / InfiniteTransition+Drag spring)
    const val AnimationExample = "animationExample"
    const val AnimatedContentExample = "animatedContentExample"
    const val LottieExample = "lottieExample"
    const val UIShimmerExample = "uiShimmerExample"
    const val TextShimmerExample = "textShimmerExample"
    const val NewShadowApiExample = "newShadowApiExample"
    const val SharedElementTransitionExample = "sharedElementTransitionExample"
    const val SharedElementDebugToolingExample = "sharedElementDebugToolingExample"
    const val SpringTweenSnapExample = "springTweenSnapExample"
    const val AnimationsShowcaseExample = "animationsShowcaseExample"

    // ==================== 네비게이션 예제 ====================
    // BottomNavigation(하단 탭), Navigation3(새 Navigation), NestedRoutesNav3(중첩 라우팅), Nav3ViewModelScope(Nav3 ViewModel 스코프 변화 + 복원 패턴), Nav3SavedStateHandle(복합 객체 NavKey 역직렬화 크래시 재현 + id+Repository 복원)
    const val BottomNavigationExample = "bottomNavigationExample"
    const val Navigation3Example = "navigation3Example"
    const val NestedRoutesNav3Example = "nestedRoutesNav3Example"
    const val Nav3ViewModelScopeExample = "nav3ViewModelScopeExample"
    const val Nav3SavedStateHandleExample = "nav3SavedStateHandleExample"

    // ==================== 앱 시작 최적화 예제 ====================
    // StartupOptimization(App Startup / Baseline Profile / Koin 지연 초기화 비교)
    const val StartupOptimizationExample = "startupOptimizationExample"

    // ==================== 개발 도구 / 정책 위반 감지 예제 ====================
    // StrictMode(메인 스레드 디스크/네트워크 I/O ThreadPolicy + leaked Closeable VmPolicy 위반을 의도적으로 재현하고 penaltyListener 로 감지 — API 28+ 분기)
    const val StrictModeExample = "strictModeExample"

    // ==================== 아키텍처 패턴 예제 ====================
    // MVI(단방향 데이터 흐름), Coordinator(화면 전환 패턴), Modularization(모듈화 전략), PreviewInternals(@Preview 내부 동작 원리), StabilityAnnotations(@Stable/@Immutable 리컴포지션 최적화), PreviewOnlyAnnotation(@RequiresOptIn으로 Preview 전용 Composable 제한), KoinCompilerPlugin(Koin Annotations KSP로 컴파일 타임 DI 검증), FeatureFlag(sealed class type-safe flag 레지스트리 + StateFlow reactive 토글 + ModalBottomSheet 디버그 메뉴 + Remote Config 시뮬레이션), HowComposeWorks(Compose 내부 동작 통합 워크스루 — 컴파일러 변환/SlotTable/snapshot 읽기 추적/layout pipeline)
    const val MVIExample = "mviExample"
    const val CoordinatorExample = "coordinatorExample"
    const val ModularizationExample = "modularizationExample"
    const val PreviewInternalsExample = "previewInternalsExample"
    const val StabilityAnnotationsExample = "stabilityAnnotationsExample"
    const val PreviewOnlyAnnotationExample = "previewOnlyAnnotationExample"
    const val KoinCompilerPluginExample = "koinCompilerPluginExample"
    const val FeatureFlagExample = "featureFlagExample"
    const val HowComposeWorksExample = "howComposeWorksExample"

    // ==================== Compose 상태 & Side Effect 예제 ====================
    // SideEffect(부수 효과 함수들), CompositionLocal(암시적 전달), StaticDynamicCompositionLocal(Static vs Dynamic), CompositionLocalTree(트리 시각화), RetainApi(retain API), SnapshotFlow(Flow 변환), InitTest(초기화 케이스), LocalContextStrings(LocalContext 문자열 안티패턴), RememberPatterns(rememberSaveable/rememberUpdatedState/derivedStateOf 비교), ComposeSnapshot(Snapshot 시스템 내부 동작), PerItemViewModel(LazyColumn 아이템별 독립 ViewModelStoreOwner)
    const val SideEffectExample = "sideEffectExample"
    const val CompositionLocalExample = "compositionLocalExample"
    const val StaticDynamicCompositionLocalExample = "staticDynamicCompositionLocalExample"
    const val CompositionLocalTreeExample = "compositionLocalTreeExample"
    const val RetainApiExample = "retainApiExample"
    const val SnapshotFlowExample = "snapshotFlowExample"
    const val InitTestExample = "initTestExample"
    const val LocalContextStringsExample = "localContextStringsExample"
    const val RememberPatternsExample = "rememberPatternsExample"
    const val ComposeSnapshotExample = "composeSnapshotExample"
    const val PerItemViewModelExample = "perItemViewModelExample"

    // ==================== 코루틴 & 동시성 예제 ====================
    // Coroutine(기본 특징), CoroutinesInternals(내부 동작 원리), WithContext(withContext vs launch), CoroutineBridges(콜백→suspend 변환 패턴), RaceCondition(공유 가변 상태 보호 4전략 비교 — 비보호/Atomic/Mutex/단일스레드 confinement), SelectExpression(select{}로 여러 suspending 작업 경쟁 — onAwait 최속 미러/onTimeout 폴백/onReceiveCatching 다중 채널 멀티플렉싱)
    const val CoroutineExample = "coroutineExample"
    const val CoroutinesInternalsExample = "coroutinesInternalsExample"
    const val WithContextExample = "withContextExample"
    const val CoroutineBridgesExample = "coroutineBridgesExample"
    const val RaceConditionExample = "raceConditionExample"
    const val SelectExpressionExample = "selectExpressionExample"

    // ==================== Flow 예제 ====================
    // FlatMap(flatMap vs flatMapLatest 비교), FlowOperators(buffer/conflate/debounce/sample 속도 제어 비교), FlowBatching(onEachBatch 크기/시간 기준 배치 집계)
    const val FlatMapExample = "flatMapExample"
    const val FlowOperatorsExample = "flowOperatorsExample"
    const val FlowBatchingExample = "flowBatchingExample"

    // ==================== Kotlin 언어 기능 예제 ====================
    // Type(변수 타입 활용), InlineValueClass(성능 최적화), SealedClassInterface(타입 안전 계층), NameBasedDestructuring(이름 기반 구조 분해 — Kotlin 2.3.20), Kotlin24Features(Kotlin 2.4 컬렉션 리터럴 [ ] / 컨텍스트 파라미터 context(...))
    const val TypeExample = "typeExample"
    const val InlineValueClassExample = "inlineValueClassExample"
    const val SealedClassInterfaceExample = "sealedClassInterfaceExample"
    const val NameBasedDestructuringExample = "nameBasedDestructuringExample"
    const val Kotlin24FeaturesExample = "kotlin24FeaturesExample"

    // ==================== 네트워크 & API 예제 ====================
    // Ktor(API 호출), SSE(실시간 통신), ApiDisconnect(연결 해제 처리), KtorAdvancedConfig(MockEngine 위에서 Auth bearer 토큰 리프레시 / HttpRequestRetry 지수 백오프 재시도 시연)
    const val KtorExample = "ktorExample"
    const val SSEExample = "sseExample"
    const val ApiDisconnectExample = "apiDisconnectExample"
    const val KtorAdvancedConfigExample = "ktorAdvancedConfigExample"

    // ==================== 데이터 & 캐시 예제 ====================
    // DataCache(캐시 관리), Paging(무한 스크롤), MultiTableInsert(Room DAO 인터페이스 상속 + withTransaction 다중 테이블 insert), RoomFtsSearch(Room @Fts4 가상 테이블의 MATCH 검색 vs LIKE '%q%' 전체 스캔 성능 비교), RoomIndex(@Index 단일/복합 인덱스의 범위/등호+정렬 쿼리 성능 비교)
    const val DataCacheExample = "dataCacheExample"
    const val PagingExample = "pagingExample"
    const val MultiTableInsertExample = "multiTableInsertExample"
    const val RoomFtsSearchExample = "roomFtsSearchExample"
    const val RoomIndexExample = "roomIndexExample"

    // ==================== 시스템 & 설정 예제 ====================
    // PowerSaveMode(절전 모드), TargetSDK34Permission(권한 처리), PassingIntentData(Intent 전달), LanguageSetting(시스템 언어), LocalLanguageChange(앱 내 언어), Shortcut(앱 숏컷), QuickSettingsTile(빠른 설정 타일), DynamicAppLinks(동적 앱 링크), PredictiveBack(예측형 뒤로가기 제스처), HapticFeedback(햅틱 피드백), BiometricAuth(생체 인증 — biometric-compose alpha)
    const val PowerSaveModeExample = "powerSaveModeExample"
    const val QuickSettingsTileExample = "quickSettingsTileExample"
    const val TargetSDK34PermissionExample = "targetSDK34PermissionExample"
    const val PassingIntentDataExample = "passingIntentDataExample"
    const val LanguageSettingExample = "languageSettingExample"
    const val LocalLanguageChangeExample = "localLanguageChangeExample"
    const val ShortcutExample = "shortcutExample"
    const val DynamicAppLinksExample = "dynamicAppLinksExample"
    const val PredictiveBackExample = "predictiveBackExample"
    const val HapticFeedbackExample = "hapticFeedbackExample"
    const val BiometricAuthExample = "biometricAuthExample"

    // ==================== 보안 예제 ====================
    // AppSecurity(Certificate Pinning + AndroidKeyStore AES-GCM 암호화 저장 + Play Integrity Mock 응답 디코딩), HardwareKeystore(TEE/StrongBox 하드웨어 키 보관 검증 — API 23+ isInsideSecureHardware / API 31+ SECURITY_LEVEL)
    const val AppSecurityExample = "appSecurityExample"
    const val HardwareKeystoreExample = "hardwareKeystoreExample"

    // ==================== AI / 온디바이스 ML 예제 ====================
    // GeminiNano(ML Kit GenAI 온디바이스 추론 + Feature 가용성 + Cloud fallback)
    const val GeminiNanoExample = "geminiNanoExample"

    // ==================== 백그라운드 작업 예제 ====================
    // WorkManager(백그라운드 작업), AudioRecorder(음성 녹음/재생)
    const val WorkManagerExample = "workManagerExample"
    const val AudioRecorderExample = "audioRecorderExample"

    // ==================== 파일 & 문서 예제 ====================
    // SafFile(SAF 파일 선택)
    const val SafFileExample = "safFileExample"

    // ==================== 위젯 예제 ====================
    // GlanceWidget(Glance 위젯)
    const val GlanceWidgetExample = "glanceWidgetExample"

    // ==================== 테스트 예제 ====================
    // Test(UI 테스트 TDD), RecompositionTest(과도한 Recomposition 감지), Rebound(역할 기반 리컴포지션 예산 모니터링), TurbineFlowTest(StateFlow/SharedFlow 테스트 패턴 비교), ScreenshotTesting(Paparazzi/Roborazzi 스크린샷 테스트), ComposeTesting(Compose UI 테스트 패턴)
    const val TestExample = "testExample"
    const val RecompositionTestExample = "recompositionTestExample"
    const val ReboundExample = "reboundExample"
    const val TurbineFlowTestExample = "turbineFlowTestExample"
    const val ScreenshotTestingExample = "screenshotTestingExample"
    const val ComposeTestingExample = "composeTestingExample"

    // ==================== 유틸리티 & 라이브러리 예제 ====================
    // CursorIDE(AI IDE 활용), SnapNotify(Snackbar 간소화), AutoCloseable(자동 리소스 정리)
    const val CursorIDEExample = "cursorIDEExample"
    const val SnapNotifyExample = "snapNotifyExample"
    const val AutoCloseableExample = "autoCloseableExample"

    // ==================== 접근성 예제 ====================
    // AccessibleFocusIndicator(키보드/D-pad 포커스 시각화 — 기본/Outline/Scale/Pulse 4가지 + Indication API 커스텀)
    const val AccessibleFocusIndicatorExample = "accessibleFocusIndicatorExample"

    // ==================== Compose 1.7 새 기능 예제 ====================
    // Compose17Features(새 기능 모음), TextOverflow(Start/Middle Ellipsis), GraphicsLayer(BlendMode/ColorFilter), LookaheadScope(자동 애니메이션), FocusRestorer(포커스 복원), PathGraphics(reverse/contains)
    const val Compose17FeaturesExample = "compose17FeaturesExample"
    const val TextOverflowExample = "textOverflowExample"
    const val GraphicsLayerExample = "graphicsLayerExample"
    const val LookaheadScopeExample = "lookaheadScopeExample"
    const val FocusRestorerExample = "focusRestorerExample"
    const val PathGraphicsExample = "pathGraphicsExample"

    // ==================== SubCategory (하위 카테고리) ====================
    // FlingBehavior(스크롤 예제 그룹), BottomSheet(바텀시트 그룹), NavigationDraw(드로어 그룹), Shimmer(시머 그룹)
    const val FlingBehavior = "flingBehaviorExampleList"
    const val BottomSheet = "bottomSheetExampleList"
    const val NavigationDraw = "navigationDrawExampleList"
    const val Shimmer = "shimmerExampleList"
}
