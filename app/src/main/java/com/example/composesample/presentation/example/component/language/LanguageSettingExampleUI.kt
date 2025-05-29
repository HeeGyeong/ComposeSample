package com.example.composesample.presentation.example.component.language

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.R

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LanguageSettingExampleUI(onBackButtonClick: () -> Unit) {
    val context = LocalContext.current
    
    // 현재 설정된 로케일 및 언어 정보 가져오기
    var currentLocale by remember { mutableStateOf(getCurrentLocale(context)) }
    var currentLanguage by remember { mutableStateOf(getCurrentLanguage(context)) }
    
    // Compose가 다시 그려질 때마다 현재 언어 설정을 업데이트
    LaunchedEffect(Unit) {
        currentLocale = getCurrentLocale(context)
        currentLanguage = getCurrentLanguage(context)
    }

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
                        Icon(Icons.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }

                    Text(
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .padding(start = 8.dp),
                        text = stringResource(id = R.string.language_setting_title),
                        fontSize = 18.sp
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))

            // 언어 정보 표시 카드
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                backgroundColor = Color.White,
                elevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.current_language_info),
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 16.dp),
                        color = Color.Black
                    )
                    
                    // 현재 지역 정보
                    LanguageInfoRow(
                        label = stringResource(id = R.string.region_label),
                        value = currentLocale
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 현재 언어 정보
                    LanguageInfoRow(
                        label = stringResource(id = R.string.language_label),
                        value = currentLanguage
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 언어 설정 변경 버튼
            Button(
                onClick = {
                    val intent = Intent(Settings.ACTION_LOCALE_SETTINGS)
                    context.startActivity(intent)
                },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.DarkGray,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = stringResource(id = R.string.change_language_settings),
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 설명 텍스트
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                backgroundColor = Color.LightGray.copy(alpha = 0.3f),
                elevation = 2.dp
            ) {
                Text(
                    modifier = Modifier.padding(16.dp),
                    text = stringResource(id = R.string.language_setting_description),
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun LanguageInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = value,
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.weight(2f),
            textAlign = TextAlign.End
        )
    }
}

// 현재 로케일 가져오기
fun getCurrentLocale(context: Context): String {
    return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
        context.resources.configuration.locales[0].displayCountry
    } else {
        @Suppress("DEPRECATION")
        context.resources.configuration.locale.displayCountry
    }
}

// 현재 언어 가져오기
fun getCurrentLanguage(context: Context): String {
    return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
        context.resources.configuration.locales[0].displayLanguage
    } else {
        @Suppress("DEPRECATION")
        context.resources.configuration.locale.displayLanguage
    }
} 