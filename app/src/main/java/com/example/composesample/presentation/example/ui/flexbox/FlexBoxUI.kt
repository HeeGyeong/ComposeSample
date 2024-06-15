package com.example.composesample.presentation.example.ui.flexbox

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import kotlin.random.Random


@OptIn(ExperimentalFoundationApi::class, ExperimentalGlideComposeApi::class)
@Composable
fun FlexBoxUI(onBackButtonClick: () -> Unit) {
    LazyColumn(modifier = Modifier.padding(horizontal = 20.dp)) {
        stickyHeader {
            Box(
                modifier = Modifier
                    .background(color = Color.White)
                    .padding(top = 10.dp, bottom = 10.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    IconButton(
                        onClick = {
                            onBackButtonClick.invoke()
                        }
                    ) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "")
                    }
                }
            }
        }

        item {
            // Device Width, Height Size
            val configuration = LocalConfiguration.current
            val screenWidth = configuration.screenWidthDp.dp

            // Item Margin 값을 Device에 따라 다르게 설정.
            val widthMargin = if (((screenWidth * 45) / 2000) < 8.dp) {
                12.dp
            } else {
                (screenWidth * 45) / 2000
            }

            // Min Size. 안에 들어갈 컴포넌트의 최소 사이즈.
            val minHeight = 30
            val contentHeight = remember { mutableStateOf(minHeight.dp) }

            val itemList = listOf(
                "123", "1452313123", "13123123", "123", "1464565465645646462", "1",
                "123", "12313123", "13123123", "123", "12", "12312234233123131"
            )

            Box(
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
                    .background(color = Color.Blue)
            ) {
                FlexBoxLayout(
                    modifier = Modifier
                        .heightIn(min = minHeight.dp, max = contentHeight.value)
                        .background(color = Color.Yellow),
                    contentHeight = contentHeight,
                    minHeight = minHeight,
                ) {
                    for (item in itemList) {
                        Row(modifier = Modifier.padding(vertical = 5.dp)) {
                            Spacer(modifier = Modifier.width(widthMargin))
                            Text(
                                modifier = Modifier
                                    .height(20.dp),
                                text = "#$item"
                            )
                            Spacer(modifier = Modifier.width(widthMargin))
                        }
                    }

                    repeat(40) {
                        Row(modifier = Modifier.padding(vertical = 5.dp)) {
                            Spacer(modifier = Modifier.width(widthMargin))
                            GlideImage(
                                modifier = Modifier
                                    .width(Random.nextInt(20, 120).dp)
                                    .height(20.dp)
                                    .border(width = 1.dp, color = Color.Blue, shape = CircleShape)
                                    .clip(CircleShape),
                                model = "https://www.researchgate.net/profile/Elif-Bayramoglu/publication/322918596/figure/fig3/AS:669304651530259@1536586072864/Sample-example-of-xeriscape-URL-3.jpg",
                                contentDescription = null,
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(widthMargin))
                        }
                    }
                }
            }
        }
    }
}


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