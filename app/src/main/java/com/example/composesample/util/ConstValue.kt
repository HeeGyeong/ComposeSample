package com.example.composesample.util

class ConstValue {
    companion object {
        // ==================== 기본 설정 ====================
        const val UpdateDate = "26년 2월"

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
        // LazyColumn(키보드 이슈), ClickEvent(중복 방지), FlexBox(유동 레이아웃), WebView(YouTube), TextStyle(스타일링), StickyHeader(고정 헤더), ReverseLazyColumn(역방향), AutoSizingText(자동 크기), CardCorners(모서리 스타일), ButtonGroup(M3 Expressive), Visibility(가시성 처리), FancyTopAppBar(고급 TopAppBar), CanvasShapes(Canvas 도형 그리기), ResponsiveTabRow(반응형 탭), CustomTextRendering(커스텀 텍스트 렌더링), DialComponent(Dial 컴포넌트), EmbeddedPhotoPicker(임베디드 포토 피커)
        const val LazyColumnExample = "lazyColumnExample"
        const val FancyTopAppBarExample = "fancyTopAppBarExample"
        const val CanvasShapesExample = "canvasShapesExample"
        const val ResponsiveTabRowExample = "responsiveTabRowExample"
        const val CustomTextRenderingExample = "customTextRenderingExample"
        const val DialComponentExample = "dialComponentExample"
        const val EmbeddedPhotoPickerExample = "embeddedPhotoPickerExample"
        const val ClickEventExample = "clickEventExample"
        const val FlexBoxLayoutExample = "flexBoxLayoutExample"
        const val WebViewIssueExample = "webViewIssueExample"
        const val TextStyleExample = "textStyleExample"
        const val StickyHeaderExample = "StickyHeaderExample"
        const val ReverseLazyColumnExample = "reverseLazyColumnExample"
        const val AutoSizingTextExample = "autoSizingTextExample"
        const val CardCornersExample = "cardCornersExample"
        const val ButtonGroupExample = "buttonGroupExample"
        const val VisibilityExample = "visibilityExample"

        // ==================== 스크롤 & 제스처 예제 ====================
        // PullToRefresh(당겨서 새로고침), PullScreenPager(풀스크린 페이저), FlingBehavior(스크롤 커스텀), SwipeToDismiss(스와이프 삭제), SwipeToDismissM3(M3 스와이프 삭제), DragAndDrop(드래그 앤 드롭), NestedScrolling(중첩 스크롤)
        const val PullToRefreshExample = "pullToRefreshExample"
        const val PullScreenPager = "pullScreenPager"
        const val FlingBehaviorExample = "flingBehaviorExample"
        const val SwipeToDismissExample = "swipeToDismissExample"
        const val SwipeToDismissM3Example = "swipeToDismissM3Example"
        const val DragAndDropExample = "dragAndDropExample"
        const val NestedScrollingExample = "nestedScrollingExample"

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
        // Animation(다양한 애니메이션), Lottie(Lottie/GIF), UIShimmer(로딩 효과), TextShimmer(텍스트 효과), NewShadowApi(Compose 1.9 Shadow), SharedElementTransition(공유 요소 전환)
        const val AnimationExample = "animationExample"
        const val LottieExample = "lottieExample"
        const val UIShimmerExample = "uiShimmerExample"
        const val TextShimmerExample = "textShimmerExample"
        const val NewShadowApiExample = "newShadowApiExample"
        const val SharedElementTransitionExample = "sharedElementTransitionExample"

        // ==================== 네비게이션 예제 ====================
        // BottomNavigation(하단 탭), Navigation3(새 Navigation), NestedRoutesNav3(중첩 라우팅)
        const val BottomNavigationExample = "bottomNavigationExample"
        const val Navigation3Example = "navigation3Example"
        const val NestedRoutesNav3Example = "nestedRoutesNav3Example"

        // ==================== 아키텍처 패턴 예제 ====================
        // MVI(단방향 데이터 흐름), Coordinator(화면 전환 패턴), Modularization(모듈화 전략)
        const val MVIExample = "mviExample"
        const val CoordinatorExample = "coordinatorExample"
        const val ModularizationExample = "modularizationExample"

        // ==================== Compose 상태 & Side Effect 예제 ====================
        // SideEffect(부수 효과 함수들), CompositionLocal(암시적 전달), StaticDynamicCompositionLocal(Static vs Dynamic), CompositionLocalTree(트리 시각화), SnapshotFlow(Flow 변환), InitTest(초기화 케이스)
        const val SideEffectExample = "sideEffectExample"
        const val CompositionLocalExample = "compositonLocalExample"
        const val StaticDynamicCompositionLocalExample = "staticDynamicCompositionLocalExample"
        const val CompositionLocalTreeExample = "compositionLocalTreeExample"
        const val SnapshotFlowExample = "snapshotFlowExample"
        const val InitTestExample = "initTestExample"

        // ==================== 코루틴 & 동시성 예제 ====================
        // Coroutine(기본 특징), CoroutinesInternals(내부 동작 원리), WithContext(withContext vs launch)
        const val CoroutineExample = "coroutineExample"
        const val CoroutinesInternalsExample = "coroutinesInternalsExample"
        const val WithContextExample = "withContextExample"

        // ==================== Flow 예제 ====================
        // FlatMap(flatMap vs flatMapLatest 비교)
        const val FlatMapExample = "flatMapExample"

        // ==================== Kotlin 언어 기능 예제 ====================
        // Type(변수 타입 활용), InlineValueClass(성능 최적화), SealedClassInterface(타입 안전 계층)
        const val TypeExample = "typeExample"
        const val InlineValueClassExample = "inlineValueClassExample"
        const val SealedClassInterfaceExample = "sealedClassInterfaceExample"

        // ==================== 네트워크 & API 예제 ====================
        // Ktor(API 호출), SSE(실시간 통신), ApiDisconnect(연결 해제 처리)
        const val KtorExample = "KtorExample"
        const val SSEExample = "SSEExample"
        const val ApiDisconnectExample = "apiDisconnectExample"

        // ==================== 데이터 & 캐시 예제 ====================
        // DataCache(캐시 관리), Paging(무한 스크롤)
        const val DataCacheExample = "dataCacheExample"
        const val PagingExample = "pagingExample"

        // ==================== 시스템 & 설정 예제 ====================
        // PowerSaveMode(절전 모드), TargetSDK34Permission(권한 처리), PassingIntentData(Intent 전달), LanguageSetting(시스템 언어), LocalLanguageChange(앱 내 언어), Shortcut(앱 숏컷), QuickSettingsTile(빠른 설정 타일)
        const val PowerSaveModeExample = "powerSaveModeExample"
        const val QuickSettingsTileExample = "quickSettingsTileExample"
        const val TargetSDK34PermissionExample = "targetSDK34PermissionExample"
        const val PassingIntentDataExample = "passingIntentDataExample"
        const val LanguageSettingExample = "languageSettingExample"
        const val LocalLanguageChangeExample = "localLanguageChangeExample"
        const val ShortcutExample = "shortcutExample"

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
        // Test(UI 테스트 TDD), RecompositionTest(과도한 Recomposition 감지)
        const val TestExample = "testExample"
        const val RecompositionTestExample = "recompositionTestExample"

        // ==================== 유틸리티 & 라이브러리 예제 ====================
        // CursorIDE(AI IDE 활용), SnapNotify(Snackbar 간소화), AutoCloseable(자동 리소스 정리)
        const val CursorIDEExample = "CursorIDEExample"
        const val SnapNotifyExample = "snapNotifyExample"
        const val AutoCloseableExample = "autoCloseableExample"

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
        const val Shimmer = "ShimmerExampleList"
    }
}