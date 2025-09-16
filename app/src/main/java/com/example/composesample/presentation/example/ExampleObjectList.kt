package com.example.composesample.presentation.example

import com.example.composesample.util.ConstValue
import com.example.domain.model.ExampleMoveType
import com.example.domain.model.ExampleObject

fun exampleObjectList(): ArrayList<ExampleObject> {
    val insertExampleObject = ArrayList<ExampleObject>()
    insertExampleObject.clear()

    insertExampleObject.add(
        ExampleObject(
            title = "Click Event",
            description = "Button 사용 시 고려할만한 클릭 이벤트 효과 방지 및 다중 클릭 이벤트 방지 방법",
            blogUrl = "https://heegs.tistory.com/143",
            exampleType = ConstValue.ClickEventExample
        )
    )

    insertExampleObject.add(
        ExampleObject(
            title = "FlexBox Layout Example",
            description = "Item의 개수와 크기에 따라서 유동적으로 변하는 layout인 FlexBox Layout에 대한 예제",
            blogUrl = "https://heegs.tistory.com/144",
            exampleType = ConstValue.FlexBoxLayoutExample
        )
    )

    insertExampleObject.add(
        ExampleObject(
            title = "Youtube WebView Issue Example",
            description = "Compose 환경에서 Youtube Player를 Webview로 붙이는 방법과 이슈 해결",
            blogUrl = "https://heegs.tistory.com/145",
            exampleType = ConstValue.WebViewIssueExample
        )
    )

    insertExampleObject.add(
        ExampleObject(
            title = "Text Style Example",
            description = "Text Style을 적용하기 위한 예제",
            blogUrl = "https://heegs.tistory.com/147",
            exampleType = ConstValue.TextStyleExample
        )
    )

    // 2025.06 기준 라이브러리 호환성 문제로 예제 전체 주석처리
//    insertExampleObject.add(
//        ExampleObject(
//            title = "Video Encoding Example",
//            description = "ffmpeg를 사용하여 동영상을 인코딩해보는 예제",
//            blogUrl = "https://heegs.tistory.com/152",
//            exampleType = ConstValue.FfmpegExample
//        )
//    )

    insertExampleObject.add(
        ExampleObject(
            title = "Audio Recorder Example",
            description = "Compose 환경에서 음성을 녹음하고 재생해보는 예제",
            blogUrl = "https://heegs.tistory.com/153",
            exampleType = ConstValue.AudioRecorderExample
        )
    )

    insertExampleObject.add(
        ExampleObject(
            title = "Pull to Refresh example",
            description = "Compose 환경에서 Pull to Refresh를 구현해보는 예제",
            blogUrl = "https://heegs.tistory.com/154",
            exampleType = ConstValue.PullToRefreshExample
        )
    )

    insertExampleObject.add(
        ExampleObject(
            title = "Pull screen pager example",
            description = "Compose 환경에서 Pull Screen의 Pager를 구현해보는 예제",
            blogUrl = "",
            exampleType = ConstValue.PullScreenPager
        )
    )

    insertExampleObject.add(
        ExampleObject(
            subCategory = ConstValue.FlingBehavior,
            title = "LazyColumn FlingBehavior Example",
            description = "LazyList에서 스크롤 이벤트를 커스텀하기 위해 FlingBehavior를 사용해보는 예제",
            blogUrl = "https://heegs.tistory.com/156",
            exampleType = ConstValue.FlingBehavior
        )
    )

    insertExampleObject.add(
        ExampleObject(
            subCategory = ConstValue.BottomSheet,
            title = "BottomSheet Example",
            description = "다양한 방법으로 구현해보는 BottomSheet 예제",
            blogUrl = "https://heegs.tistory.com/156",
            exampleType = ConstValue.BottomSheet
        )
    )

    insertExampleObject.add(
        ExampleObject(
            subCategory = ConstValue.NavigationDraw,
            title = "NavigationDraw Example",
            description = "다양한 방법으로 구현해보는 NavigationDraw 예제",
            blogUrl = "https://heegs.tistory.com/156",
            exampleType = ConstValue.NavigationDraw
        )
    )

    insertExampleObject.add(
        ExampleObject(
            title = "Swipe to Dismiss Example",
            description = "Swipe 하여 아이템을 제거할 수 있는 방법",
            blogUrl = "https://heegs.tistory.com/161",
            exampleType = ConstValue.SwipeToDismissExample
        )
    )

    insertExampleObject.add(
        ExampleObject(
            title = "Side Effect Example",
            description = "다양한 Side Effect 함수와 그 차이를 확인하는 예제.",
            blogUrl = "https://heegs.tistory.com/162",
            exampleType = ConstValue.SideEffectExample
        )
    )

    insertExampleObject.add(
        ExampleObject(
            title = "Data Cache Example",
            description = "Data Cache를 사용해보는 예제.",
            blogUrl = "",
            exampleType = ConstValue.DataCacheExample
        )
    )

    insertExampleObject.add(
        ExampleObject(
            title = "Api Disconnect Example",
            description = "Api를 사용할 때, Network 문제가 발생했을 때 처리 하는 방법.",
            blogUrl = "https://heegs.tistory.com/163",
            exampleType = ConstValue.ApiDisconnectExample
        )
    )

    insertExampleObject.add(
        ExampleObject(
            title = "PowerSave Mode Example",
            description = "절전 모드 설정과 절전 모드 시 고려해야 할 몇 가지 문제.",
            blogUrl = "https://heegs.tistory.com/165",
            exampleType = ConstValue.PowerSaveModeExample
        )
    )

    insertExampleObject.add(
        ExampleObject(
            title = "WorkManager Example",
            description = "WorkManager를 사용하는 방법과, 다양한 옵션에 대해 돌아보기 위한 예제.",
            blogUrl = "https://heegs.tistory.com/166",
            exampleType = ConstValue.WorkManagerExample
        )
    )

    insertExampleObject.add(
        ExampleObject(
            title = "DragAndDrop Example",
            description = "DragAndDrop를 사용하는 방법에 대해 알아보기 위한 예제.",
            blogUrl = "https://heegs.tistory.com/168",
            exampleType = ConstValue.DragAndDropExample
        )
    )

    insertExampleObject.add(
        ExampleObject(
            title = "TargetSdk 34 - Permission",
            description = "TargetSdk 34를 사용할 때 주의점 - Permission",
            blogUrl = "https://heegs.tistory.com/169",
            exampleType = ConstValue.TargetSDK34PermissionExample
        )
    )

    insertExampleObject.add(
        ExampleObject(
            title = "Intent data passing example",
            description = "Intent data를 전달하는 방법",
            blogUrl = "",
            exampleType = ConstValue.PassingIntentDataExample
        )
    )

    insertExampleObject.add(
        ExampleObject(
            title = "Lottie, Gif 사용 방법",
            description = "Lottie, Gif 사용 방법",
            blogUrl = "https://heegs.tistory.com/171",
            exampleType = ConstValue.LottieExample
        )
    )

    insertExampleObject.add(
        ExampleObject(
            title = "Bottom Navigation Example",
            description = "Bottom Navigation을 사용하는 방법에 대해 알아보기 위한 예제.",
            blogUrl = "https://heegs.tistory.com/172",
            moveType = ExampleMoveType.ACTIVITY,
            exampleType = ConstValue.BottomNavigationExample
        )
    )

    insertExampleObject.add(
        ExampleObject(
            lastUpdate = "24. 10. 24",
            title = "Sticky Header Example",
            description = "사용성 높은 Sticky Header를 만들어보기 위한 예제.",
            blogUrl = "https://heegs.tistory.com/175",
            exampleType = ConstValue.StickyHeaderExample
        )
    )

    insertExampleObject.add(
        ExampleObject(
            lastUpdate = "24. 11. 06",
            title = "Cursor IDE Example",
            description = "Cursor IDE를 사용하여 만들어본 예제 - 입력 된 텍스트 길이를 보여주는 예제",
            blogUrl = "https://heegs.tistory.com/177",
            exampleType = ConstValue.CursorIDEExample
        )
    )

    insertExampleObject.add(
        ExampleObject(
            lastUpdate = "24. 11. 19",
            title = "Animation Example",
            description = "Compose에서 다양한 애니메이션을 사용하는 방법에 대한 예제",
            blogUrl = "",
            exampleType = ConstValue.AnimationExample
        )
    )

    insertExampleObject.add(
        ExampleObject(
            lastUpdate = "24. 12. 03",
            title = "Coordinator Example",
            description = "간단한 Coordinator Pattern 예제",
            blogUrl = "",
            exampleType = ConstValue.CoordinatorExample
        )
    )

    insertExampleObject.add(
        ExampleObject(
            lastUpdate = "24. 12. 05",
            title = "MVI Pattern Example",
            description = "MVI 패턴을 사용하여 단방향 통신을 구현하는 예제",
            blogUrl = "https://heegs.tistory.com/179",
            exampleType = ConstValue.MVIExample
        )
    )

    insertExampleObject.add(
        ExampleObject(
            lastUpdate = "25. 01. 07",
            title = "Server-Sent Events Example",
            description = "SSE를 사용하여 서버로부터 실시간 업데이트를 받는 예제",
            blogUrl = "https://heegs.tistory.com/181",
            exampleType = ConstValue.SSEExample
        )
    )

    insertExampleObject.add(
        ExampleObject(
            lastUpdate = "25. 01. 12",
            title = "Reverse LazyColumn Example",
            description = "LazyColumn을 반대로 사용하는 예제",
            blogUrl = "",
            exampleType = ConstValue.ReverseLazyColumnExample
        )
    )

    insertExampleObject.add(
        ExampleObject(
            lastUpdate = "25. 01. 13",
            title = "UI Test Example",
            description = "간단한 UI Test를 작성하여 TDD 구현 방식을 확인해보는 예제",
            blogUrl = "",
            exampleType = ConstValue.TestExample
        )
    )

    insertExampleObject.add(
        ExampleObject(
            lastUpdate = "25. 01. 19",
            title = "Coroutine Example",
            description = "coroutine의 근본적인 특징에 대해 알아보는 예제 추가",
            blogUrl = "",
            exampleType = ConstValue.CoroutineExample
        )
    )

    insertExampleObject.add(
        ExampleObject(
            subCategory = ConstValue.Shimmer,
            lastUpdate = "25. 01. 26",
            title = "Shimmer Example",
            description = "Shimmer를 사용하는 방법에 대해 알아보기 위한 예제.",
            blogUrl = "",
            exampleType = ConstValue.UIShimmerExample,
        )
    )

    insertExampleObject.add(
        ExampleObject(
            lastUpdate = "25. 02. 10",
            title = "CompositionLocal Example",
            description = "CompositionLocal을 사용하는 방법에 대해 알아보기 위한 예제.",
            blogUrl = "",
            exampleType = ConstValue.CompositionLocalExample,
        )
    )

    insertExampleObject.add(
        ExampleObject(
            lastUpdate = "25. 02. 19",
            title = "Lazy Column Keyboard Issue",
            description = "LazyColumn 내부에 TextField Component가 있을 때, keyboard가 정상적으로 보이지 않는 문제. + SDK 35 대응",
            blogUrl = "https://heegs.tistory.com/142",
            exampleType = ConstValue.LazyColumnExample
        )
    )

    insertExampleObject.add(
        ExampleObject(
            lastUpdate = "25. 02. 20",
            title = "Ktor API Example",
            description = "Ktor을 사용하여 API를 호출하는 예제",
            blogUrl = "",
            exampleType = ConstValue.KtorExample
        )
    )

    insertExampleObject.add(
        ExampleObject(
            lastUpdate = "25. 03. 05",
            title = "Paging Example",
            description = "Paging 라이브러리 통해 무한 스크롤을 구현해보는 예제.",
            blogUrl = "",
            exampleType = ConstValue.PagingExample
        )
    )

    insertExampleObject.add(
        ExampleObject(
            lastUpdate = "25. 03. 06",
            title = "Shortcut Example",
            description = "Shortcut을 통해 앱을 실행하는 방법에 대해 알아보기 위한 예제.\n블로그는 22년도 글이지만 Base는 그대로입니다.",
            blogUrl = "https://heegs.tistory.com/130",
            exampleType = ConstValue.ShortcutExample
        )
    )

    insertExampleObject.add(
        ExampleObject(
            lastUpdate = "25. 05. 12",
            title = "Type Example",
            description = "각 변수 타입을 보다 kotlin스럽게 변형하여 사용하는 예제",
            blogUrl = "",
            exampleType = ConstValue.TypeExample
        )
    )

    insertExampleObject.add(
        ExampleObject(
            lastUpdate = "25. 05. 25",
            title = "SAF File Selection Example",
            description = "SAF를 사용하여 txt, doc, docx 파일을 선택하고 파일 정보를 표시하는 예제",
            blogUrl = "",
            exampleType = ConstValue.SafFileExample
        )
    )

    insertExampleObject.add(
        ExampleObject(
            lastUpdate = "25. 05. 29",
            title = "Language Setting Example",
            description = "시스템 언어 설정을 확인하고 변경하는 예제\n언어 설정 변경 후 앱으로 돌아오면 변경사항이 반영됩니다.",
            blogUrl = "",
            exampleType = ConstValue.LanguageSettingExample
        )
    )

    insertExampleObject.add(
        ExampleObject(
            lastUpdate = "25. 06. 25",
            subCategory = ConstValue.Compose17FeaturesExample,
            title = "Compose 1.7.6 새로운 기능들",
            description = "Compose 1.7.6에서 새롭게 추가된 UI 기능들을 확인해보는 예제 모음",
            blogUrl = "",
            exampleType = ConstValue.Compose17FeaturesExample
        )
    )

    insertExampleObject.add(
        ExampleObject(
            lastUpdate = "25. 06. 29",
            title = "Init Case Test Example",
            description = "Compose 환경에서 데이터를 init 하는 케이스를 확인해보는 예제",
            blogUrl = "https://heegs.tistory.com/185",
            exampleType = ConstValue.InitTestExample
        )
    )

    insertExampleObject.add(
        ExampleObject(
            lastUpdate = "25. 07. 09",
            title = "로컬 언어 변경 예제",
            description = "OS 설정이 아닌 앱 내에서 true/false 값으로 한국어/영어를 실시간 변경하는 예제",
            blogUrl = "https://heegs.tistory.com/188",
            exampleType = ConstValue.LocalLanguageChangeExample
        )
    )

    insertExampleObject.add(
        ExampleObject(
            lastUpdate = "25. 07. 22",
            title = "SnapshotFlow vs collectAsState",
            description = "snapshotFlow와 collectAsState 차이점과 올바른 사용법 예제",
            blogUrl = "",
            exampleType = ConstValue.SnapshotFlowExample
        )
    )

    insertExampleObject.add(
        ExampleObject(
            lastUpdate = "25. 08. 02",
            title = "Jetpack Compose for Widgets with Glance",
            description = "Glance를 사용하여 Jetpack Compose로 안드로이드 위젯 만들기",
            blogUrl = "",
            exampleType = ConstValue.GlanceWidgetExample
        )
    )

    insertExampleObject.add(
        ExampleObject(
            lastUpdate = "25. 08. 04",
            title = "Auto-sizing Text with BasicText",
            description = "BasicText의 autoSize 기능으로 동적 텍스트 크기 조절하기",
            blogUrl = "",
            exampleType = ConstValue.AutoSizingTextExample
        )
    )

    insertExampleObject.add(
        ExampleObject(
            lastUpdate = "25. 08. 28",
            title = "Nested Scrolling Example",
            description = "Nested Scroll과 NestedScrollConnection을 활용한 중첩 스크롤 예제",
            blogUrl = "https://heegs.tistory.com/188",
            exampleType = ConstValue.NestedScrollingExample
        )
    )

    insertExampleObject.add(
        ExampleObject(
            lastUpdate = "25. 09. 01",
            title = "Nested Routes with Navigation 3",
            description = "Navigation 3의 중첩 라우팅과 RouteComponent를 활용한 네비게이션 예제",
            blogUrl = "",
            exampleType = ConstValue.NestedRoutesNav3Example
        )
    )

    insertExampleObject.add(
        ExampleObject(
            lastUpdate = "25. 09. 15",
            title = "New Shadow API for Compose",
            description = "Compose 1.9.0의 새로운 dropShadow와 innerShadow API 활용 예제",
            blogUrl = "",
            exampleType = ConstValue.NewShadowApiExample
        )
    )

    insertExampleObject.add(
        ExampleObject(
            lastUpdate = "25. 09. 16",
            title = "SnapNotify: Simplified Snackbars",
            description = "Snackbar 코드를 간소화하는 SnapNotify 라이브러리 예제",
            blogUrl = "",
            exampleType = ConstValue.SnapNotifyExample
        )
    )

    return insertExampleObject
}

fun subCategoryList(): ArrayList<ExampleObject> {
    val insertExampleObject = ArrayList<ExampleObject>()
    insertExampleObject.clear()

    insertExampleObject.add(
        ExampleObject(
            subCategory = ConstValue.Shimmer,
            title = "UI Shimmer Example",
            description = "UI Shimmer를 사용하는 방법에 대해 알아보기 위한 예제.",
            blogUrl = "https://heegs.tistory.com/174",
            exampleType = ConstValue.UIShimmerExample
        )
    )

    insertExampleObject.add(
        ExampleObject(
            subCategory = ConstValue.Shimmer,
            title = "Text Shimmer Example",
            description = "text Shimmer를 사용하는 방법에 대해 알아보기 위한 예제.",
            blogUrl = "https://heegs.tistory.com/182",
            exampleType = ConstValue.TextShimmerExample
        )
    )

    insertExampleObject.add(
        ExampleObject(
            subCategory = ConstValue.FlingBehavior,
            title = "LazyColumn FlingBehavior Example",
            description = "LazyList에서 스크롤 이벤트를 커스텀하기 위해 FlingBehavior를 사용해보는 예제",
            blogUrl = "https://heegs.tistory.com/156",
            exampleType = ConstValue.FlingBehaviorExample
        )
    )

    insertExampleObject.add(
        ExampleObject(
            subCategory = ConstValue.FlingBehavior,
            title = "LazyColumn FlingBehavior Example",
            description = "LazyList에서 스크롤 이벤트를 커스텀하기 위해 FlingBehavior를 사용해보는 예제",
            blogUrl = "https://heegs.tistory.com/156",
            exampleType = ConstValue.FlingBehaviorExample
        )
    )

    insertExampleObject.add(
        ExampleObject(
            subCategory = ConstValue.BottomSheet,
            title = "BottomSheetScaffold Example",
            description = "다양한 방법으로 구현해보는 BottomSheet - BottomSheetScaffold 예제",
            blogUrl = "https://heegs.tistory.com/158",
            exampleType = ConstValue.BottomSheetExample
        )
    )

    insertExampleObject.add(
        ExampleObject(
            subCategory = ConstValue.BottomSheet,
            title = "Modal Bottom Sheet Example",
            description = "다양한 방법으로 구현해보는 BottomSheet - ModalBottomSheetLayout 예제",
            blogUrl = "https://heegs.tistory.com/158",
            exampleType = ConstValue.ModalBottomSheetExample
        )
    )

    insertExampleObject.add(
        ExampleObject(
            subCategory = ConstValue.BottomSheet,
            title = "Custom Bottom Sheet Example",
            description = "다양한 방법으로 구현해보는 BottomSheet - CustomBottomSheet 예제",
            blogUrl = "https://heegs.tistory.com/158",
            exampleType = ConstValue.CustomBottomSheetExample
        )
    )

    insertExampleObject.add(
        ExampleObject(
            subCategory = ConstValue.NavigationDraw,
            title = "ScaffoldDrawExample Example",
            description = "다양한 방법으로 구현해보는 NavigationDraw - ScaffoldDrawExample",
            blogUrl = "https://heegs.tistory.com/160",
            exampleType = ConstValue.ScaffoldDrawExample
        )
    )

    insertExampleObject.add(
        ExampleObject(
            subCategory = ConstValue.NavigationDraw,
            title = "ModalDrawExample Example",
            description = "다양한 방법으로 구현해보는 NavigationDraw - ModalDrawExample",
            blogUrl = "https://heegs.tistory.com/160",
            exampleType = ConstValue.ModalDrawExample
        )
    )

    insertExampleObject.add(
        ExampleObject(
            subCategory = ConstValue.Compose17FeaturesExample,
            title = "Text Overflow 새 기능",
            description = "TextOverflow.StartEllipsis와 MiddleEllipsis를 사용한 새로운 텍스트 오버플로우 옵션",
            blogUrl = "",
            exampleType = ConstValue.TextOverflowExample
        )
    )

    insertExampleObject.add(
        ExampleObject(
            subCategory = ConstValue.Compose17FeaturesExample,
            title = "GraphicsLayer 향상된 기능",
            description = "새로운 BlendMode와 ColorFilter를 사용한 고급 그래픽 효과",
            blogUrl = "",
            exampleType = ConstValue.GraphicsLayerExample
        )
    )

    insertExampleObject.add(
        ExampleObject(
            subCategory = ConstValue.Compose17FeaturesExample,
            title = "LookaheadScope 애니메이션",
            description = "안정화된 LookaheadScope를 사용한 자동 크기/위치 애니메이션",
            blogUrl = "",
            exampleType = ConstValue.LookaheadScopeExample
        )
    )

    insertExampleObject.add(
        ExampleObject(
            subCategory = ConstValue.Compose17FeaturesExample,
            title = "Focus Restorer",
            description = "개선된 포커스 복원 기능으로 더 나은 사용자 경험 제공",
            blogUrl = "",
            exampleType = ConstValue.FocusRestorerExample
        )
    )

    insertExampleObject.add(
        ExampleObject(
            subCategory = ConstValue.Compose17FeaturesExample,
            title = "Path Graphics 새 기능",
            description = "Path.reverse()와 Path.contains()를 활용한 새로운 그래픽 기능",
            blogUrl = "",
            exampleType = ConstValue.PathGraphicsExample
        )
    )

    return insertExampleObject
}