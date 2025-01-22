package com.example.composesample.presentation.example

import com.example.composesample.util.ConstValue
import com.example.domain.model.ExampleObject


fun algorithmObjectList(): ArrayList<ExampleObject> {
    val insertExampleObject = ArrayList<ExampleObject>()
    insertExampleObject.clear()

    insertExampleObject.add(
        ExampleObject(
            title = "TwoSum Algorithm",
            description = "Hash - Two Sum 알고리즘",
            blogUrl = "",
            exampleType = ConstValue.TwoSumAlgorithm
        )
    )

    return insertExampleObject
}