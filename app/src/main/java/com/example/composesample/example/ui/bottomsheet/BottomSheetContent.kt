package com.example.composesample.example.ui.bottomsheet

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.composesample.R
import com.example.composesample.example.util.noRippleClickable
import com.example.composesample.main.MainHeader

@Composable
fun ExpandedBottomSheet() {
    Surface {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.8f)
                    .background(color = Color.DarkGray)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 36.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        repeat(10) {
                            Text(
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .padding(horizontal = 20.dp),
                                text = "BS Contents 채워넣기",
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.2f)
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 36.dp)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    MainHeader(
                        title = "Compose Example Project",
                        onBackIconClicked = { },
                        onLeftIconContent = { }
                    )
                }
            }
        }
    }
}


@Composable
fun CollapsedBottomSheet(
    currentFraction: Float,
    onSheetClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp) // sheetPeekHeight 과 동일하다.
            .background(MaterialTheme.colors.primary)
            .graphicsLayer(alpha = 1f - currentFraction)
            .noRippleClickable(
                onClick = onSheetClick,
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            text = "BottomSheet를 올릴 수 있는 View 입니다. 클릭 해도 올라가도록 해두었어요.",
        )

        IconButton(
            onClick = {
                Log.d("IconButtonClick", "여기서 버튼도 클릭 가능합니다.")
            },
            modifier = Modifier.padding(
                start = 8.dp,
                top = 8.dp,
                bottom = 8.dp,
                end = 16.dp
            )
        ) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = null
            )
        }
    }
}