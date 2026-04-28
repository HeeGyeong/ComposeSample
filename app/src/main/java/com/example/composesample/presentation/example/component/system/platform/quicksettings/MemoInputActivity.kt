package com.example.composesample.presentation.example.component.system.platform.quicksettings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

class MemoInputActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MemoInputDialog(
                onDismiss = { finish() },
                onSave = { memo ->
                    MemoTileService.addMemo(memo)
                    finish()
                }
            )
        }
    }
}

@Composable
private fun MemoInputDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var memoText by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "📝 빠른 메모",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6A1B9A)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = memoText,
                        onValueChange = { memoText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        placeholder = {
                            Text(
                                "메모를 입력하세요...",
                                color = Color.Gray
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6A1B9A),
                            cursorColor = Color(0xFF6A1B9A)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("취소", color = Color(0xFF666666))
                        }

                        Button(
                            onClick = {
                                if (memoText.isNotBlank()) {
                                    onSave(memoText)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            enabled = memoText.isNotBlank()
                        ) {
                            Text("저장", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

