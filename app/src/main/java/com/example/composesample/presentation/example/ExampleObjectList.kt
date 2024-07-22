package com.example.composesample.presentation.example

import com.example.composesample.util.ConstValue
import com.example.domain.model.ExampleObject

fun exampleObjectList(): ArrayList<ExampleObject> {
    val insertExampleObject = ArrayList<ExampleObject>()
    insertExampleObject.clear()

    insertExampleObject.add(
        ExampleObject(
            title = "Lazy Column Keyboard Issue",
            description = "LazyColumn 내부에 TextField Component가 있을 때, keyboard가 정상적으로 보이지 않는 문제.",
            blogUrl = "https://heegs.tistory.com/142",
            exampleType = ConstValue.LazyColumnExample
        )
    )

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

    insertExampleObject.add(
        ExampleObject(
            title = "Video Encoding Example",
            description = "ffmpeg를 사용하여 동영상을 인코딩해보는 예제",
            blogUrl = "https://heegs.tistory.com/152",
            exampleType = ConstValue.FfmpegExample
        )
    )

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
            description = "WorkManager를 사용하는 방법과, 다양한 옵션에 대해 알아보기 위한 예제.",
            blogUrl = "https://heegs.tistory.com/166",
            exampleType = ConstValue.WorkManagerExample
        )
    )

    insertExampleObject.add(
        ExampleObject(
            title = "DragAndDrop Example (작업 중)",
            description = "DragAndDrop를 사용하는 방법에 대해 알아보기 위한 예제.",
            blogUrl = "",
            exampleType = ConstValue.DragAndDropExample
        )
    )

    return insertExampleObject
}

fun subCategoryList(): ArrayList<ExampleObject> {
    val insertExampleObject = ArrayList<ExampleObject>()
    insertExampleObject.clear()

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

    return insertExampleObject
}