package com.example.composesample.presentation.example

import com.example.composesample.util.ConstValue
import com.example.domain.model.ExampleObject


fun algorithmObjectList(): ArrayList<ExampleObject> {
    val insertExampleObject = ArrayList<ExampleObject>()
    insertExampleObject.clear()

    insertExampleObject.add(
        ExampleObject(
            title = "SAMPLE",
            description = "SAMPLE",
            blogUrl = "https://heegs.tistory.com/142",
            exampleType = ConstValue.LazyColumnExample
        )
    )

    return insertExampleObject
}