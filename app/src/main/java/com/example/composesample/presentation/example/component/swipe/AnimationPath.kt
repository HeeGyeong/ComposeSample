package com.example.composesample.presentation.example.component.swipe

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.sqrt

/**
 * @param visible 0f or 1f. Empty or Full
 * @param startPosition true : Left start, false : Right start
 */
class CircleAnimationPath(private val visible: Float, private val startPosition: Boolean) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {

        val origin = Offset(
            x = if (startPosition) 0f else size.width,
            y = size.center.y,
        )

        // sqrt(height^2 + width^2) * visible
        val radius = sqrt(
            size.height * size.height + size.width * size.width
        ) * visible

        return Outline.Generic(
            Path().apply {
                addOval(
                    Rect(
                        center = origin,
                        radius = radius,
                    )
                )
            }
        )
    }
}