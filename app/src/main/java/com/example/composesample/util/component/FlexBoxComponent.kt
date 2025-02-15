package com.example.composesample.util.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.unit.Dp

@Composable
fun FlexBoxLayout(
    modifier: Modifier = Modifier,
    contentHeight: MutableState<Dp>,
    minHeight: Int,
    content: @Composable () -> Unit,
) {
    // layout 정책을 커스텀하기 위해 measurePolicy를 선언하여 구현한다.
    val measurePolicy = flexBoxLayoutMeasurePolicy(contentHeight, minHeight)

    Layout(
        measurePolicy = measurePolicy,
        content = content, // 커스텀한 measurePolicy 가 적용된 상태로 해당 content를 그린다.
        modifier = modifier
    )
}


fun flexBoxLayoutMeasurePolicy(contentHeight: MutableState<Dp>, minHeight: Int) =
    MeasurePolicy { measurables, constraints ->
        layout(constraints.maxWidth, constraints.maxHeight) {
            var tempHeight = minHeight

            val placeables = measurables.map { measurable ->
                measurable.measure(constraints)
            }
            var yPosition = 0
            var xPosition = 0
            var itemHeightSize = 0
            placeables.forEach { placeable ->
                // 아이템을 추가했을 때 최대 너비를 넘어가면
                if (xPosition + placeable.width >
                    constraints.maxWidth
                ) {
                    xPosition = 0
                    yPosition += itemHeightSize // y 포지션 값을 더한다.
                    itemHeightSize = 0
                }

                // 해당 x,y 값에 아이템을 추가한다.
                placeable.placeRelative(
                    x = xPosition,
                    y = yPosition
                )

                // 아이템을 추가했으면, 아이템의 크기만큼 x 좌표를 옮긴다
                xPosition += placeable.width

                // 아이템의 크기가 추가할 아이템의 높이보다 작으면, 아이템의 크기로 설정해준다.
                if (itemHeightSize < placeable.height) {
                    itemHeightSize = placeable.height
                }
            }

            if (tempHeight != yPosition) {
                tempHeight = yPosition

                // 라인이 추가되는 타이밍에만 maxY 값이 yPos 값에 추가되는데, 라인이 넘어가지 않으면
                // 마지막 라인에 대한 height 값이 추가되지 않으므로 추가해준다.
                contentHeight.value = tempHeight.toDp() + itemHeightSize.toDp()
            }
        }
    }