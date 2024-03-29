package com.example.composesample.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.composesample.example.util.ConstValue
import com.example.composesample.model.ExampleObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update


class BlogExampleViewModel(application: Application) : AndroidViewModel(application) {
    val toast = MutableStateFlow("")
    fun sendToastMessage(message: String) {
        toast.update { message }
    }

    val exampleObjectList = MutableStateFlow(listOf<ExampleObject>())

    fun initExampleObject() {
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
                title = "Work Manager Example",
                description = "WorkManager를 사용하여 Background에서 작업을 수행해보는 예제.",
                blogUrl = "",
                exampleType = ConstValue.WorkManagerExample
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
                title = "LazyColumn FlingBehavior Example",
                description = "LazyList에서 스크롤 이벤트를 커스텀하기 위해 FlingBehavior를 사용해보는 예제",
                blogUrl = "https://heegs.tistory.com/156",
                exampleType = ConstValue.FlingBehaviorExample
            )
        )

        insertExampleObject.add(
            ExampleObject(
                title = "LazyColumn FlingBehavior Example",
                description = "LazyList에서 스크롤 이벤트를 커스텀하기 위해 FlingBehavior를 사용해보는 예제",
                blogUrl = "https://heegs.tistory.com/156",
                exampleType = ConstValue.FlingBehaviorExample
            )
        )

        insertExampleObject.add(
            ExampleObject(
                title = "BottomSheetScaffold Example",
                description = "다양한 방법으로 구현해보는 BottomSheet - BottomSheetScaffold 예제",
                blogUrl = "https://heegs.tistory.com/158",
                exampleType = ConstValue.BottomSheetExample
            )
        )

        insertExampleObject.add(
            ExampleObject(
                title = "Modal Bottom Sheet Example",
                description = "다양한 방법으로 구현해보는 BottomSheet - ModalBottomSheetLayout 예제",
                blogUrl = "https://heegs.tistory.com/158",
                exampleType = ConstValue.ModalBottomSheetExample
            )
        )

        insertExampleObject.add(
            ExampleObject(
                title = "Custom Bottom Sheet Example",
                description = "다양한 방법으로 구현해보는 BottomSheet - CustomBottomSheet 예제",
                blogUrl = "https://heegs.tistory.com/158",
                exampleType = ConstValue.CustomBottomSheetExample
            )
        )

        exampleObjectList.update { insertExampleObject }
    }
}