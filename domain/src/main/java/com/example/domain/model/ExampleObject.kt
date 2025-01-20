package com.example.domain.model

data class ExampleObject(
    val subCategory: String = "",
    val moveType: ExampleMoveType = ExampleMoveType.UI,
    val lastUpdate: String = "",
    val title: String,
    val description: String,
    val blogUrl: String,
    val exampleType: String,
)

enum class ExampleMoveType {
    UI,
    ACTIVITY,
    ALGORITHM,
    EMPTY,
}