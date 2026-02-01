package com.example.composesample.presentation.example

import com.example.composesample.presentation.example.list.blogUrl
import com.example.composesample.presentation.example.list.examples2024
import com.example.composesample.presentation.example.list.examples2025
import com.example.composesample.presentation.example.list.examples2026
import com.example.composesample.presentation.example.list.examplesWithoutDate
import com.example.composesample.util.ConstValue
import com.example.domain.model.ExampleObject

fun exampleObjectList(): ArrayList<ExampleObject> {
    val insertExampleObject = ArrayList<ExampleObject>()
    insertExampleObject.clear()

    insertExampleObject.addAll(examplesWithoutDate)
    insertExampleObject.addAll(examples2024)
    insertExampleObject.addAll(examples2025)
    insertExampleObject.addAll(examples2026)

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
            blogUrl = blogUrl(174),
            exampleType = ConstValue.UIShimmerExample
        )
    )

    insertExampleObject.add(
        ExampleObject(
            subCategory = ConstValue.Shimmer,
            title = "Text Shimmer Example",
            description = "text Shimmer를 사용하는 방법에 대해 알아보기 위한 예제.",
            blogUrl = blogUrl(182),
            exampleType = ConstValue.TextShimmerExample
        )
    )

    insertExampleObject.add(
        ExampleObject(
            subCategory = ConstValue.FlingBehavior,
            title = "LazyColumn FlingBehavior Example",
            description = "LazyList에서 스크롤 이벤트를 커스텀하기 위해 FlingBehavior를 사용해보는 예제",
            blogUrl = blogUrl(156),
            exampleType = ConstValue.FlingBehaviorExample
        )
    )

    insertExampleObject.add(
        ExampleObject(
            subCategory = ConstValue.FlingBehavior,
            title = "LazyColumn FlingBehavior Example",
            description = "LazyList에서 스크롤 이벤트를 커스텀하기 위해 FlingBehavior를 사용해보는 예제",
            blogUrl = blogUrl(156),
            exampleType = ConstValue.FlingBehaviorExample
        )
    )

    insertExampleObject.add(
        ExampleObject(
            subCategory = ConstValue.BottomSheet,
            title = "BottomSheetScaffold Example",
            description = "다양한 방법으로 구현해보는 BottomSheet - BottomSheetScaffold 예제",
            blogUrl = blogUrl(158),
            exampleType = ConstValue.BottomSheetExample
        )
    )

    insertExampleObject.add(
        ExampleObject(
            subCategory = ConstValue.BottomSheet,
            title = "Modal Bottom Sheet Example",
            description = "다양한 방법으로 구현해보는 BottomSheet - ModalBottomSheetLayout 예제",
            blogUrl = blogUrl(158),
            exampleType = ConstValue.ModalBottomSheetExample
        )
    )

    insertExampleObject.add(
        ExampleObject(
            subCategory = ConstValue.BottomSheet,
            title = "Custom Bottom Sheet Example",
            description = "다양한 방법으로 구현해보는 BottomSheet - CustomBottomSheet 예제",
            blogUrl = blogUrl(158),
            exampleType = ConstValue.CustomBottomSheetExample
        )
    )

    insertExampleObject.add(
        ExampleObject(
            subCategory = ConstValue.NavigationDraw,
            title = "ScaffoldDrawExample Example",
            description = "다양한 방법으로 구현해보는 NavigationDraw - ScaffoldDrawExample",
            blogUrl = blogUrl(160),
            exampleType = ConstValue.ScaffoldDrawExample
        )
    )

    insertExampleObject.add(
        ExampleObject(
            subCategory = ConstValue.NavigationDraw,
            title = "ModalDrawExample Example",
            description = "다양한 방법으로 구현해보는 NavigationDraw - ModalDrawExample",
            blogUrl = blogUrl(160),
            exampleType = ConstValue.ModalDrawExample
        )
    )

    insertExampleObject.add(
        ExampleObject(
            subCategory = ConstValue.Compose17FeaturesExample,
            title = "Text Overflow 새 기능",
            description = "TextOverflow.StartEllipsis와 MiddleEllipsis를 사용한 새로운 텍스트 오버플로우 옵션",
            exampleType = ConstValue.TextOverflowExample
        )
    )

    insertExampleObject.add(
        ExampleObject(
            subCategory = ConstValue.Compose17FeaturesExample,
            title = "GraphicsLayer 향상된 기능",
            description = "새로운 BlendMode와 ColorFilter를 사용한 고급 그래픽 효과",
            exampleType = ConstValue.GraphicsLayerExample
        )
    )

    insertExampleObject.add(
        ExampleObject(
            subCategory = ConstValue.Compose17FeaturesExample,
            title = "LookaheadScope 애니메이션",
            description = "안정화된 LookaheadScope를 사용한 자동 크기/위치 애니메이션",
            exampleType = ConstValue.LookaheadScopeExample
        )
    )

    insertExampleObject.add(
        ExampleObject(
            subCategory = ConstValue.Compose17FeaturesExample,
            title = "Focus Restorer",
            description = "개선된 포커스 복원 기능으로 더 나은 사용자 경험 제공",
            exampleType = ConstValue.FocusRestorerExample
        )
    )

    insertExampleObject.add(
        ExampleObject(
            subCategory = ConstValue.Compose17FeaturesExample,
            title = "Path Graphics 새 기능",
            description = "Path.reverse()와 Path.contains()를 활용한 새로운 그래픽 기능",
            exampleType = ConstValue.PathGraphicsExample
        )
    )

    return insertExampleObject
}
