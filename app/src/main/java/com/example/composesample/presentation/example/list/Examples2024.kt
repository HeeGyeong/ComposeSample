package com.example.composesample.presentation.example.list

import com.example.composesample.util.ConstValue
import com.example.domain.model.ExampleObject

val examples2024 = listOf(
    ExampleObject(
        lastUpdate = "24. 10. 24",
        title = "Sticky Header Example",
        description = "사용성 높은 Sticky Header를 만들어보기 위한 예제.",
        blogUrl = blogUrl(175),
        exampleType = ConstValue.StickyHeaderExample
    ),
    ExampleObject(
        lastUpdate = "24. 11. 06",
        title = "Cursor IDE Example",
        description = "Cursor IDE를 사용하여 만들어본 예제 - 입력 된 텍스트 길이를 보여주는 예제",
        blogUrl = blogUrl(177),
        exampleType = ConstValue.CursorIDEExample
    ),
    ExampleObject(
        lastUpdate = "24. 11. 19",
        title = "Animation Example",
        description = "Compose에서 다양한 애니메이션을 사용하는 방법에 대한 예제",
        exampleType = ConstValue.AnimationExample
    ),
    ExampleObject(
        lastUpdate = "24. 12. 03",
        title = "Coordinator Example",
        description = "간단한 Coordinator Pattern 예제",
        exampleType = ConstValue.CoordinatorExample
    ),
    ExampleObject(
        lastUpdate = "24. 12. 05",
        title = "MVI Pattern Example",
        description = "MVI 패턴을 사용하여 단방향 통신을 구현하는 예제",
        blogUrl = blogUrl(179),
        exampleType = ConstValue.MVIExample
    )
)
