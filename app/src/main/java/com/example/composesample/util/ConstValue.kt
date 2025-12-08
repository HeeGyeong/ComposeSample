package com.example.composesample.util

class ConstValue {
    companion object {
        // ==================== 기본 설정 ====================
        /** 앱 업데이트 날짜 */
        const val UpdateDate = "25년 12월"

        // ==================== Intent & Code Type ====================
        /** Intent 타입 키 */
        const val IntentType = "type"
        /** 예제 타입 키 */
        const val ExampleType = "example"

        // ==================== Shortcut 관련 ====================
        /** 숏컷 키 */
        const val ShortCutKey = "shortcut_key"
        /** XML 정의 숏컷 */
        const val ShortCutTypeXML = "type_xml"
        /** 동적 숏컷 */
        const val ShortCutTypeDynamic = "type_dynamic"
        /** 핀 숏컷 */
        const val ShortCutTypePin = "type_pin"

        // ==================== 외부 URL ====================
        /** SSE 예제용 Wikipedia 스트림 URL */
        const val SSEWikiURL = "https://stream.wikimedia.org/v2/stream/recentchange"

        // ==================== UI 컴포넌트 예제 ====================
        /** LazyColumn 키보드 이슈 해결 예제 */
        const val LazyColumnExample = "lazyColumnExample"
        /** 클릭 이벤트 중복 방지 예제 */
        const val ClickEventExample = "clickEventExample"
        /** FlexBox 레이아웃 예제 */
        const val FlexBoxLayoutExample = "flexBoxLayoutExample"
        /** WebView YouTube 이슈 해결 예제 */
        const val WebViewIssueExample = "webViewIssueExample"
        /** 텍스트 스타일 예제 */
        const val TextStyleExample = "textStyleExample"
        /** Sticky Header 예제 */
        const val StickyHeaderExample = "StickyHeaderExample"
        /** 역방향 LazyColumn 예제 */
        const val ReverseLazyColumnExample = "reverseLazyColumnExample"
        /** 자동 텍스트 크기 조절 예제 */
        const val AutoSizingTextExample = "autoSizingTextExample"
        /** 카드 모서리 스타일 예제 */
        const val CardCornersExample = "cardCornersExample"

        // ==================== 스크롤 & 제스처 예제 ====================
        /** Pull to Refresh 예제 */
        const val PullToRefreshExample = "pullToRefreshExample"
        /** 풀스크린 페이저 예제 */
        const val PullScreenPager = "pullScreenPager"
        /** FlingBehavior 스크롤 커스텀 예제 */
        const val FlingBehaviorExample = "flingBehaviorExample"
        /** Swipe to Dismiss 예제 */
        const val SwipeToDismissExample = "swipeToDismissExample"
        /** Drag and Drop 예제 */
        const val DragAndDropExample = "dragAndDropExample"
        /** 중첩 스크롤 예제 */
        const val NestedScrollingExample = "nestedScrollingExample"

        // ==================== BottomSheet 예제 ====================
        /** BottomSheetScaffold 예제 */
        const val BottomSheetExample = "bottomSheetExample"
        /** ModalBottomSheetLayout 예제 */
        const val ModalBottomSheetExample = "modalBottomSheetExample"
        /** 커스텀 BottomSheet 예제 */
        const val CustomBottomSheetExample = "customBottomSheetExample"

        // ==================== Navigation Drawer 예제 ====================
        /** Scaffold Drawer 예제 */
        const val ScaffoldDrawExample = "scaffoldDrawExample"
        /** Modal Drawer 예제 */
        const val ModalDrawExample = "modalDrawExample"

        // ==================== 애니메이션 & 효과 예제 ====================
        /** 다양한 애니메이션 예제 */
        const val AnimationExample = "animationExample"
        /** Lottie & GIF 사용 예제 */
        const val LottieExample = "lottieExample"
        /** UI Shimmer 로딩 효과 예제 */
        const val UIShimmerExample = "uiShimmerExample"
        /** Text Shimmer 효과 예제 */
        const val TextShimmerExample = "textShimmerExample"
        /** 새로운 Shadow API 예제 (Compose 1.9.0) */
        const val NewShadowApiExample = "newShadowApiExample"

        // ==================== 네비게이션 예제 ====================
        /** Bottom Navigation 예제 */
        const val BottomNavigationExample = "bottomNavigationExample"
        /** Navigation 3 기본 예제 */
        const val Navigation3Example = "navigation3Example"
        /** Navigation 3 중첩 라우팅 예제 */
        const val NestedRoutesNav3Example = "nestedRoutesNav3Example"

        // ==================== 아키텍처 패턴 예제 ====================
        /** MVI 패턴 예제 */
        const val MVIExample = "mviExample"
        /** Coordinator 패턴 예제 */
        const val CoordinatorExample = "coordinatorExample"
        /** 실용적 모듈화 전략 예제 */
        const val ModularizationExample = "modularizationExample"

        // ==================== Compose 상태 & Side Effect 예제 ====================
        /** Side Effect 함수들 비교 예제 */
        const val SideEffectExample = "sideEffectExample"
        /** CompositionLocal 사용 예제 */
        const val CompositionLocalExample = "compositonLocalExample"
        /** Static vs Dynamic CompositionLocal 비교 예제 */
        const val StaticDynamicCompositionLocalExample = "staticDynamicCompositionLocalExample"
        /** SnapshotFlow vs collectAsState 비교 예제 */
        const val SnapshotFlowExample = "snapshotFlowExample"
        /** 데이터 초기화 케이스 테스트 예제 */
        const val InitTestExample = "initTestExample"

        // ==================== 코루틴 & 동시성 예제 ====================
        /** 코루틴 기본 특징 예제 */
        const val CoroutineExample = "coroutineExample"
        /** 코루틴 내부 동작 원리 예제 (State Machine, Continuation) */
        const val CoroutinesInternalsExample = "coroutinesInternalsExample"
        /** withContext vs launch 비교 예제 */
        const val WithContextExample = "withContextExample"

        // ==================== Flow 예제 ====================
        /** flatMap vs flatMapLatest 비교 예제 */
        const val FlatMapExample = "flatMapExample"

        // ==================== Kotlin 언어 기능 예제 ====================
        /** 변수 타입 활용 예제 */
        const val TypeExample = "typeExample"
        /** Inline 함수 & Value Class 성능 최적화 예제 */
        const val InlineValueClassExample = "inlineValueClassExample"
        /** Sealed Class & Interface 타입 안전성 예제 */
        const val SealedClassInterfaceExample = "sealedClassInterfaceExample"

        // ==================== 네트워크 & API 예제 ====================
        /** Ktor API 호출 예제 */
        const val KtorExample = "KtorExample"
        /** Server-Sent Events 실시간 통신 예제 */
        const val SSEExample = "SSEExample"
        /** API 연결 해제 처리 예제 */
        const val ApiDisconnectExample = "apiDisconnectExample"

        // ==================== 데이터 & 캐시 예제 ====================
        /** 데이터 캐시 예제 */
        const val DataCacheExample = "dataCacheExample"
        /** Paging 라이브러리 무한 스크롤 예제 */
        const val PagingExample = "pagingExample"

        // ==================== 시스템 & 설정 예제 ====================
        /** 절전 모드 처리 예제 */
        const val PowerSaveModeExample = "powerSaveModeExample"
        /** TargetSDK 34 권한 처리 예제 */
        const val TargetSDK34PermissionExample = "targetSDK34PermissionExample"
        /** Intent 데이터 전달 예제 */
        const val PassingIntentDataExample = "passingIntentDataExample"
        /** 시스템 언어 설정 예제 */
        const val LanguageSettingExample = "languageSettingExample"
        /** 앱 내 언어 변경 예제 */
        const val LocalLanguageChangeExample = "localLanguageChangeExample"
        /** Shortcut 앱 실행 예제 */
        const val ShortcutExample = "shortcutExample"

        // ==================== 백그라운드 작업 예제 ====================
        /** WorkManager 사용 예제 */
        const val WorkManagerExample = "workManagerExample"
        /** 음성 녹음 & 재생 예제 */
        const val AudioRecorderExample = "audioRecorderExample"

        // ==================== 파일 & 문서 예제 ====================
        /** SAF 파일 선택 예제 */
        const val SafFileExample = "safFileExample"

        // ==================== 위젯 예제 ====================
        /** Glance 위젯 예제 */
        const val GlanceWidgetExample = "glanceWidgetExample"

        // ==================== 테스트 예제 ====================
        /** UI 테스트 TDD 예제 */
        const val TestExample = "testExample"

        // ==================== 유틸리티 & 라이브러리 예제 ====================
        /** Cursor IDE로 만든 텍스트 길이 예제 */
        const val CursorIDEExample = "CursorIDEExample"
        /** SnapNotify Snackbar 간소화 예제 */
        const val SnapNotifyExample = "snapNotifyExample"
        /** AutoCloseable ViewModel 패턴 예제 */
        const val AutoCloseableExample = "autoCloseableExample"

        // ==================== Compose 1.7 새 기능 예제 ====================
        /** Compose 1.7.6 새 기능 예제 모음 */
        const val Compose17FeaturesExample = "compose17FeaturesExample"
        /** TextOverflow 새 옵션 예제 (StartEllipsis, MiddleEllipsis) */
        const val TextOverflowExample = "textOverflowExample"
        /** GraphicsLayer 향상된 기능 예제 (BlendMode, ColorFilter) */
        const val GraphicsLayerExample = "graphicsLayerExample"
        /** LookaheadScope 자동 애니메이션 예제 */
        const val LookaheadScopeExample = "lookaheadScopeExample"
        /** Focus Restorer 포커스 복원 예제 */
        const val FocusRestorerExample = "focusRestorerExample"
        /** Path Graphics 새 기능 예제 (reverse, contains) */
        const val PathGraphicsExample = "pathGraphicsExample"

        // ==================== SubCategory (하위 카테고리) ====================
        /** FlingBehavior 예제 하위 카테고리 */
        const val FlingBehavior = "flingBehaviorExampleList"
        /** BottomSheet 예제 하위 카테고리 */
        const val BottomSheet = "bottomSheetExampleList"
        /** NavigationDraw 예제 하위 카테고리 */
        const val NavigationDraw = "navigationDrawExampleList"
        /** Shimmer 예제 하위 카테고리 */
        const val Shimmer = "ShimmerExampleList"
    }
}