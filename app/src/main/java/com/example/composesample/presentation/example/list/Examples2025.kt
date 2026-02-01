package com.example.composesample.presentation.example.list

import com.example.composesample.util.ConstValue
import com.example.domain.model.ExampleObject

val examples2025 = listOf(
    ExampleObject(
        lastUpdate = "25. 01. 07",
        title = "Server-Sent Events Example",
        description = "SSE를 사용하여 서버로부터 실시간 업데이트를 받는 예제",
        blogUrl = blogUrl(181),
        exampleType = ConstValue.SSEExample
    ),
    ExampleObject(
        lastUpdate = "25. 01. 12",
        title = "Reverse LazyColumn Example",
        description = "LazyColumn을 반대로 사용하는 예제",
        exampleType = ConstValue.ReverseLazyColumnExample
    ),
    ExampleObject(
        lastUpdate = "25. 01. 13",
        title = "UI Test Example",
        description = "간단한 UI Test를 작성하여 TDD 구현 방식을 확인해보는 예제",
        exampleType = ConstValue.TestExample
    ),
    ExampleObject(
        lastUpdate = "25. 01. 19",
        title = "Coroutine Example",
        description = "coroutine의 근본적인 특징에 대해 알아보는 예제 추가",
        exampleType = ConstValue.CoroutineExample
    ),
    ExampleObject(
        subCategory = ConstValue.Shimmer,
        lastUpdate = "25. 01. 26",
        title = "Shimmer Example",
        description = "Shimmer를 사용하는 방법에 대해 알아보기 위한 예제.",
        exampleType = ConstValue.UIShimmerExample,
    ),
    ExampleObject(
        lastUpdate = "25. 02. 10",
        title = "CompositionLocal Example",
        description = "CompositionLocal을 사용하는 방법에 대해 알아보기 위한 예제.",
        exampleType = ConstValue.CompositionLocalExample,
    ),
    ExampleObject(
        lastUpdate = "25. 02. 19",
        title = "Lazy Column Keyboard Issue",
        description = "LazyColumn 내부에 TextField Component가 있을 때, keyboard가 정상적으로 보이지 않는 문제. + SDK 35 대응",
        blogUrl = blogUrl(142),
        exampleType = ConstValue.LazyColumnExample
    ),
    ExampleObject(
        lastUpdate = "25. 02. 20",
        title = "Ktor API Example",
        description = "Ktor을 사용하여 API를 호출하는 예제",
        exampleType = ConstValue.KtorExample
    ),
    ExampleObject(
        lastUpdate = "25. 03. 05",
        title = "Paging Example",
        description = "Paging 라이브러리 통해 무한 스크롤을 구현해보는 예제.",
        exampleType = ConstValue.PagingExample
    ),
    ExampleObject(
        lastUpdate = "25. 03. 06",
        title = "Shortcut Example",
        description = "Shortcut을 통해 앱을 실행하는 방법에 대해 알아보기 위한 예제.\n블로그는 22년도 글이지만 Base는 그대로입니다.",
        blogUrl = blogUrl(130),
        exampleType = ConstValue.ShortcutExample
    ),
    ExampleObject(
        lastUpdate = "25. 05. 12",
        title = "Type Example",
        description = "각 변수 타입을 보다 kotlin스럽게 변형하여 사용하는 예제",
        exampleType = ConstValue.TypeExample
    ),
    ExampleObject(
        lastUpdate = "25. 05. 25",
        title = "SAF File Selection Example",
        description = "SAF를 사용하여 txt, doc, docx 파일을 선택하고 파일 정보를 표시하는 예제",
        exampleType = ConstValue.SafFileExample
    ),
    ExampleObject(
        lastUpdate = "25. 05. 29",
        title = "Language Setting Example",
        description = "시스템 언어 설정을 확인하고 변경하는 예제\n언어 설정 변경 후 앱으로 돌아오면 변경사항이 반영됩니다.",
        exampleType = ConstValue.LanguageSettingExample
    ),
    ExampleObject(
        lastUpdate = "25. 06. 25",
        subCategory = ConstValue.Compose17FeaturesExample,
        title = "Compose 1.7.6 새로운 기능들",
        description = "Compose 1.7.6에서 새롭게 추가된 UI 기능들을 확인해보는 예제 모음",
        exampleType = ConstValue.Compose17FeaturesExample
    ),
    ExampleObject(
        lastUpdate = "25. 06. 29",
        title = "Init Case Test Example",
        description = "Compose 환경에서 데이터를 init 하는 케이스를 확인해보는 예제",
        blogUrl = blogUrl(185),
        exampleType = ConstValue.InitTestExample
    ),
    ExampleObject(
        lastUpdate = "25. 07. 09",
        title = "로컬 언어 변경 예제",
        description = "OS 설정이 아닌 앱 내에서 true/false 값으로 한국어/영어를 실시간 변경하는 예제",
        blogUrl = blogUrl(188),
        exampleType = ConstValue.LocalLanguageChangeExample
    ),
    ExampleObject(
        lastUpdate = "25. 07. 22",
        title = "SnapshotFlow vs collectAsState",
        description = "snapshotFlow와 collectAsState 차이점과 올바른 사용법 예제",
        exampleType = ConstValue.SnapshotFlowExample
    ),
    ExampleObject(
        lastUpdate = "25. 08. 02",
        title = "Jetpack Compose for Widgets with Glance",
        description = "Glance를 사용하여 Jetpack Compose로 안드로이드 위젯 만들기",
        exampleType = ConstValue.GlanceWidgetExample
    ),
    ExampleObject(
        lastUpdate = "25. 08. 04",
        title = "Auto-sizing Text with BasicText",
        description = "BasicText의 autoSize 기능으로 동적 텍스트 크기 조절하기",
        exampleType = ConstValue.AutoSizingTextExample
    ),
    ExampleObject(
        lastUpdate = "25. 08. 28",
        title = "Nested Scrolling Example",
        description = "Nested Scroll과 NestedScrollConnection을 활용한 중첩 스크롤 예제",
        blogUrl = blogUrl(188),
        exampleType = ConstValue.NestedScrollingExample
    ),
    ExampleObject(
        lastUpdate = "25. 09. 01",
        title = "Nested Routes with Navigation 3",
        description = "Navigation 3의 중첩 라우팅과 RouteComponent를 활용한 네비게이션 예제",
        exampleType = ConstValue.NestedRoutesNav3Example
    ),
    ExampleObject(
        lastUpdate = "25. 09. 15",
        title = "New Shadow API for Compose",
        description = "Compose 1.9.0의 새로운 dropShadow와 innerShadow API 활용 예제",
        exampleType = ConstValue.NewShadowApiExample
    ),
    ExampleObject(
        lastUpdate = "25. 09. 23",
        title = "SnapNotify: Simplified Snackbars",
        description = "Snackbar 코드를 간소화하는 SnapNotify 라이브러리 예제",
        exampleType = ConstValue.SnapNotifyExample
    ),
    ExampleObject(
        lastUpdate = "25. 09. 27",
        title = "Card Corners in Compose",
        description = "Convex, Concave, Cut, Sharp 등 다양한 카드 모서리 스타일 구현 예제",
        exampleType = ConstValue.CardCornersExample
    ),
    ExampleObject(
        lastUpdate = "25. 10. 16",
        title = "AutoCloseable ViewModel Pattern",
        description = "ViewModel에서 AutoCloseable을 활용한 자동 리소스 정리 패턴",
        exampleType = ConstValue.AutoCloseableExample
    ),
    ExampleObject(
        lastUpdate = "25. 10. 26",
        title = "Static vs Dynamic CompositionLocal",
        description = "staticCompositionLocalOf와 compositionLocalOf의 차이와 올바른 사용법",
        exampleType = ConstValue.StaticDynamicCompositionLocalExample
    ),
    ExampleObject(
        lastUpdate = "25. 11. 03",
        title = "Inline Functions & Value Classes",
        description = "Kotlin의 Zero-Cost Abstractions: inline 함수와 value class로 성능 최적화",
        exampleType = ConstValue.InlineValueClassExample
    ),
    ExampleObject(
        lastUpdate = "25. 11. 12",
        title = "Sealed Classes & Interfaces",
        description = "Sealed class와 interface로 타입 안전한 계층 구조 설계하기",
        exampleType = ConstValue.SealedClassInterfaceExample
    ),
    ExampleObject(
        lastUpdate = "25. 11. 16",
        title = "Coroutines Internals",
        description = "코루틴 내부: State Machines, Continuations, Structured Concurrency",
        exampleType = ConstValue.CoroutinesInternalsExample
    ),
    ExampleObject(
        lastUpdate = "25. 11. 23",
        title = "flatMap vs flatMapLatest",
        description = "Flow 변환 연산자의 차이점과 사용 시나리오 완벽 이해",
        exampleType = ConstValue.FlatMapExample
    ),
    ExampleObject(
        lastUpdate = "25. 11. 27",
        title = "Jetpack Navigation 3",
        description = "새로운 Navigation 라이브러리: Compose State 기반 네비게이션",
        exampleType = ConstValue.Navigation3Example
    ),
    ExampleObject(
        lastUpdate = "25. 12. 01",
        title = "Pragmatic Modularization",
        description = "Wiring Modules: 실용적인 모듈화 전략과 의존성 연결",
        exampleType = ConstValue.ModularizationExample
    ),
    ExampleObject(
        lastUpdate = "25. 12. 07",
        title = "withContext vs launch",
        description = "Dispatchers.IO 사용 시 withContext와 launch의 실제 차이",
        exampleType = ConstValue.WithContextExample
    ),
    ExampleObject(
        lastUpdate = "25. 12. 14",
        title = "ButtonGroup (M3 Expressive)",
        description = "Material 3 Expressive의 ButtonGroup: 애니메이션, 오버플로우, 토글 지원",
        exampleType = ConstValue.ButtonGroupExample
    ),
    ExampleObject(
        lastUpdate = "25. 12. 16",
        title = "Visibility in Compose",
        description = "View.INVISIBLE/GONE에서 Modifier.visible로: Compose 가시성 처리",
        exampleType = ConstValue.VisibilityExample
    ),
    ExampleObject(
        lastUpdate = "25. 12. 31",
        title = "Catching Excessive Recompositions",
        description = "테스트로 과도한 Recomposition 감지: RecompositionCounter와 assertRecompositionCount",
        exampleType = ConstValue.RecompositionTestExample
    )
)
