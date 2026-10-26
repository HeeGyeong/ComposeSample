package com.example.composesample.presentation.example.list

import com.example.composesample.util.ConstValue
import com.example.composesample.presentation.example.model.ExampleMoveType
import com.example.composesample.presentation.example.model.ExampleObject

val examplesWithoutDate = listOf(
    ExampleObject(
        title = "Click Event",
        description = "Button 사용 시 고려할만한 클릭 이벤트 효과 방지 및 다중 클릭 이벤트 방지 방법",
        blogUrl = blogUrl(143),
        exampleType = ConstValue.ClickEventExample
    ),
    ExampleObject(
        title = "FlexBox Layout Example",
        description = "아이템 개수·크기에 따라 유동적으로 변하는 FlexBox Layout 예제",
        blogUrl = blogUrl(144),
        exampleType = ConstValue.FlexBoxLayoutExample
    ),
    ExampleObject(
        title = "Youtube WebView Issue Example",
        description = "Compose 환경에서 Youtube Player를 Webview로 붙이는 방법과 이슈 해결",
        blogUrl = blogUrl(145),
        exampleType = ConstValue.WebViewIssueExample
    ),
    ExampleObject(
        title = "Text Style Example",
        description = "Text Style을 적용하기 위한 예제",
        blogUrl = blogUrl(147),
        exampleType = ConstValue.TextStyleExample
    ),
    ExampleObject(
        title = "Audio Recorder Example",
        description = "Compose 환경에서 음성을 녹음하고 재생해보는 예제",
        blogUrl = blogUrl(153),
        exampleType = ConstValue.AudioRecorderExample
    ),
    ExampleObject(
        title = "Pull to Refresh example",
        description = "Compose 환경에서 Pull to Refresh를 구현해보는 예제",
        blogUrl = blogUrl(154),
        exampleType = ConstValue.PullToRefreshExample
    ),
    ExampleObject(
        title = "Pull screen pager example",
        description = "Compose 환경에서 Pull Screen의 Pager를 구현해보는 예제",
        exampleType = ConstValue.PullScreenPager
    ),
    ExampleObject(
        subCategory = ConstValue.FlingBehavior,
        title = "LazyColumn FlingBehavior Example",
        description = "LazyList에서 스크롤 이벤트를 커스텀하기 위해 FlingBehavior를 사용해보는 예제",
        blogUrl = blogUrl(156),
        exampleType = ConstValue.FlingBehavior
    ),
    ExampleObject(
        subCategory = ConstValue.BottomSheet,
        title = "BottomSheet Example",
        description = "다양한 방법으로 구현해보는 BottomSheet 예제",
        blogUrl = blogUrl(156),
        exampleType = ConstValue.BottomSheet
    ),
    ExampleObject(
        subCategory = ConstValue.NavigationDraw,
        title = "NavigationDraw Example",
        description = "다양한 방법으로 구현해보는 NavigationDraw 예제",
        blogUrl = blogUrl(156),
        exampleType = ConstValue.NavigationDraw
    ),
    ExampleObject(
        title = "Swipe to Dismiss Example",
        description = "Swipe 하여 아이템을 제거할 수 있는 방법",
        blogUrl = blogUrl(161),
        exampleType = ConstValue.SwipeToDismissExample
    ),
    ExampleObject(
        title = "Side Effect Example",
        description = "다양한 Side Effect 함수와 그 차이를 확인하는 예제.",
        blogUrl = blogUrl(162),
        exampleType = ConstValue.SideEffectExample
    ),
    ExampleObject(
        title = "Data Cache Example",
        description = "Data Cache를 사용해보는 예제.",
        exampleType = ConstValue.DataCacheExample
    ),
    ExampleObject(
        title = "Api Disconnect Example",
        description = "Api를 사용할 때, Network 문제가 발생했을 때 처리 하는 방법.",
        blogUrl = blogUrl(163),
        exampleType = ConstValue.ApiDisconnectExample
    ),
    ExampleObject(
        lastUpdate = "26. 10. 26",
        title = "PowerSave Mode Example",
        description = "절전 모드 감지와 백그라운드 제한(버킷·Doze·강제 중지) 자가 진단",
        blogUrl = blogUrl(165),
        exampleType = ConstValue.PowerSaveModeExample
    ),
    ExampleObject(
        lastUpdate = "26. 10. 22",
        title = "WorkManager Example",
        description = "WorkManager 사용법, 주기 작업 재등록 정책과 다음 실행 시각 지정",
        blogUrl = blogUrl(166),
        exampleType = ConstValue.WorkManagerExample
    ),
    ExampleObject(
        title = "DragAndDrop Example",
        description = "DragAndDrop를 사용하는 방법에 대해 알아보기 위한 예제.",
        blogUrl = blogUrl(168),
        exampleType = ConstValue.DragAndDropExample
    ),
    ExampleObject(
        title = "TargetSdk 34 - Permission",
        description = "TargetSdk 34를 사용할 때 주의점 - Permission",
        blogUrl = blogUrl(169),
        exampleType = ConstValue.TargetSDK34PermissionExample
    ),
    ExampleObject(
        title = "Intent data passing example",
        description = "Intent data를 전달하는 방법",
        exampleType = ConstValue.PassingIntentDataExample
    ),
    ExampleObject(
        title = "Lottie, Gif 사용 방법",
        description = "Lottie, Gif 사용 방법",
        blogUrl = blogUrl(171),
        exampleType = ConstValue.LottieExample
    ),
    ExampleObject(
        title = "Bottom Navigation Example",
        description = "Bottom Navigation을 사용하는 방법에 대해 알아보기 위한 예제.",
        blogUrl = blogUrl(172),
        moveType = ExampleMoveType.ACTIVITY,
        exampleType = ConstValue.BottomNavigationExample
    )
)
