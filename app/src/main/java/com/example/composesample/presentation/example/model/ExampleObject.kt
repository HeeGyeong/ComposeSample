package com.example.composesample.presentation.example.model

// 예제 목록을 표현하는 presentation 레이어 모델.
// 목록 항목/이동 타입은 UI·앱 전용 개념이므로 domain(순수 비즈니스 로직) 대신 app 모듈에 둔다.
data class ExampleObject(
    val subCategory: String = "",
    val moveType: ExampleMoveType = ExampleMoveType.UI,
    val lastUpdate: String = "",
    val title: String,
    val description: String,
    val blogUrl: String = "",
    val exampleType: String,
)

enum class ExampleMoveType {
    UI,
    ACTIVITY,
    EMPTY,
}
