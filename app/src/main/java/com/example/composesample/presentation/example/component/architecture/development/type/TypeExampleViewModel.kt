package com.example.composesample.presentation.example.component.architecture.development.type

import androidx.lifecycle.ViewModel

class TypeExampleViewModel : ViewModel() {
    val isFlag = true

    private val someOtherSet = setOf(2, 3, 4, 5)
    private val someOtherMap = mapOf(
        "language" to "Kotlin",
        "framework" to "Compose",
        "platform" to "Android"
    )

    val intListMutable = mutableListOf<Int>().apply {
        add(1)

        if (isFlag) {
            add(2)
        }
    }

    val intListImmutable = buildList {
        add(1)

        if (isFlag) {
            add(2)
        }
    }

    val floatListMutable = mutableListOf<Float>().apply {
        add(1f)

        if (isFlag) {
            add(2f)
        }
    }

    val floatListImmutable = buildList {
        add(1f)

        if (isFlag) {
            add(2f)
        }
    }

    val stringBuilderResult = StringBuilder().apply {
        append("Some")

        if (isFlag) {
            append(" string")
        }
    }.toString()

    val buildStringResult = buildString {
        append("Some")

        if (isFlag) {
            append(" string")
        }
    }

    val numberSet = buildSet {
        add(1)

        if (isFlag) {
            addAll(someOtherSet)
        }
    }

    val emptySet = buildSet<Int> { }

    val stringMap = buildMap {
        put("key", "value")

        if (isFlag) {
            putAll(someOtherMap)
        }
    }

    val emptyMap = buildMap<String, String> { }
} 