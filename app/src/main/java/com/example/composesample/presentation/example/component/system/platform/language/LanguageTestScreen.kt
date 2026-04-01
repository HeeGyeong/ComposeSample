package com.example.composesample.presentation.example.component.system.platform.language

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.R
import com.example.composesample.util.LanguageManager

@Composable
fun LanguageTestScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    
    // 언어 변경 상태를 추적하기 위한 state
    var refreshKey by remember { mutableStateOf(0) }
    val currentLocalizedContext = remember(refreshKey) { 
        LanguageManager.createLocalizedContext(context) 
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // 헤더
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
            }
            
            Text(
                text = currentLocalizedContext.getString(R.string.local_language_title),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 다른 화면 테스트 카드
        Card(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = Color(0xFFFFF3E0),
            elevation = 4.dp,
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "📱 다른 화면 테스트",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF8F00)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = currentLocalizedContext.getString(R.string.welcome_message),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF9800)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = currentLocalizedContext.getString(R.string.sample_content),
                    fontSize = 16.sp,
                    color = Color.Black
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = currentLocalizedContext.getString(R.string.description_text),
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // 현재 언어 정보 카드
        Card(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = Color(0xFFF5F5F5),
            elevation = 4.dp,
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = currentLocalizedContext.getString(R.string.current_language),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = LanguageManager.getCurrentLanguageDisplayName(context),
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // 언어 변경 테스트 버튼
        Button(
            onClick = { 
                val currentIsKorean = LanguageManager.getLanguagePreference(context)
                LanguageManager.saveLanguagePreference(context, !currentIsKorean)
                refreshKey++ // Context 새로고침을 위한 key 변경
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(0xFFFF9800),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = currentLocalizedContext.getString(R.string.language_toggle_button),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 설명 카드
        Card(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = Color(0xFFFFF8E1),
            elevation = 4.dp,
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "✅ 화면 간 언어 설정 유지 테스트",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF8F00)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "이 화면은 같은 Activity 내의 다른 Composable입니다.\n" +
                            "이전 화면에서 설정한 언어가 그대로 유지되어야 합니다.\n" +
                            "위 버튼으로 여기서도 언어를 변경할 수 있습니다.",
                    fontSize = 14.sp,
                    color = Color(0xFFE65100),
                    lineHeight = 20.sp
                )
            }
        }
    }
} 