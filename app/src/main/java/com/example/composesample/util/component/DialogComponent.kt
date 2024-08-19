package com.example.composesample.util.component

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.composesample.util.noRippleClickable


@Composable
fun PermissionDialog(
    context: Context,
    permissionDialog: MutableState<Boolean>,
    permissionText: String = "해당 권한",
) {
    TwoButtonDialogUI(
        title = "권한이 없습니다.",
        subTitle = "${permissionText}이 없습니다.",
        leftButtonText = "취소하기",
        rightButtonText = "설정하기",
        visible = permissionDialog,
        onLeftButtonClick = {
            permissionDialog.value = false
        },
        onRightButtonClick = {
            permissionDialog.value = false
            context.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(
                    Uri.parse(
                        "package:${context.packageName}"
                    )
                )
            )
        }
    )
}

@Composable
fun TwoButtonDialogUI(
    title: String,
    subTitle: String,
    leftButtonText: String,
    rightButtonText: String,
    visible: MutableState<Boolean>,
    onLeftButtonClick: () -> Unit = { },
    onRightButtonClick: () -> Unit = { },
) {
    BackHandler(enabled = true, onBack = {
        visible.value = false
    })

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Black.copy(alpha = 0.6f))
            .noRippleClickable {
                visible.value = false
            },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.8f),
            shape = RoundedCornerShape(20.dp),
            backgroundColor = Color.LightGray
        ) {
            Row(modifier = Modifier.padding(horizontal = 16.dp)) {
                Column {
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        modifier = Modifier
                            .padding(vertical = 10.dp)
                            .fillMaxWidth(),
                        text = title,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        modifier = Modifier
                            .padding(
                                top = 6.dp,
                                bottom = 10.dp,
                            )
                            .fillMaxWidth(),
                        text = subTitle,
                        textAlign = TextAlign.Center
                    )

                    Row(
                        modifier = Modifier
                            .height(88.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Button(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                onClick = {
                                    visible.value = false
                                    onLeftButtonClick.invoke()
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = leftButtonText,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }

                        Column(Modifier.weight(1f)) {
                            Button(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                onClick = {
                                    visible.value = false
                                    onRightButtonClick.invoke()
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = rightButtonText,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}