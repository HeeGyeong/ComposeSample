package com.example.composesample.presentation.example.component.language

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.net.wifi.WifiManager
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.Locale

data class LocationInfo(
    val country: String = "",
    val region: String = "",
    val city: String = "",
    val isLoading: Boolean = true,
    val error: String? = null
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LanguageSettingExampleUI(onBackButtonClick: () -> Unit) {
    val context = LocalContext.current

    // 현재 설정된 로케일 정보들
    var currentLocale by remember { mutableStateOf(getCurrentLocale(context)) }
    var currentLanguage by remember { mutableStateOf(getCurrentLanguage(context)) }
    var localeCode by remember { mutableStateOf(getLocaleCode(context)) }
    var languageCode by remember { mutableStateOf(getLanguageCode(context)) }
    var countryCode by remember { mutableStateOf(getCountryCode(context)) }

    // 위치 정보
    var locationInfo by remember { mutableStateOf(LocationInfo()) }

    // 권한 상태
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // 위치 정보 가져오기
    suspend fun fetchLocationInfo() {
        locationInfo = locationInfo.copy(isLoading = true, error = null)
        try {
            val info = getLocationInfo(hasLocationPermission, context)
            locationInfo = info
        } catch (e: Exception) {
            locationInfo = locationInfo.copy(
                isLoading = false,
                error = e.message ?: "알 수 없는 오류"
            )
        }
    }

    // 초기 위치 정보 가져오기
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            fetchLocationInfo()
        }
    }

    LazyColumn {
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

            // 기본 언어 정보 표시 카드
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

            Spacer(modifier = Modifier.height(16.dp))

            // 상세 로케일 정보 표시 카드
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
                        text = stringResource(id = R.string.detailed_locale_info),
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 16.dp),
                        color = Color.Black
                    )

                    // 로케일 코드
                    LanguageInfoRow(
                        label = stringResource(id = R.string.locale_code_label),
                        value = localeCode
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 언어 코드
                    LanguageInfoRow(
                        label = stringResource(id = R.string.language_code_label),
                        value = languageCode
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 국가 코드
                    LanguageInfoRow(
                        label = stringResource(id = R.string.country_code_label),
                        value = countryCode
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 위치 정보 카드
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "현재 위치 정보",
                            fontSize = 16.sp,
                            color = Color.Black
                        )

                        if (hasLocationPermission) {
                            IconButton(
                                onClick = {
                                    kotlinx.coroutines.MainScope().launch {
                                        fetchLocationInfo()
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Filled.Refresh,
                                    contentDescription = "새로고침",
                                    tint = Color.Gray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    when {
                        !hasLocationPermission -> {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "위치 권한이 필요합니다",
                                    color = Color.Red,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    onClick = {
                                        ActivityCompat.requestPermissions(
                                            context as android.app.Activity,
                                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                            1000
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        backgroundColor = Color.Blue,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text("권한 요청")
                                }
                            }
                        }

                        locationInfo.isLoading -> {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }

                        locationInfo.error != null -> {
                            Text(
                                text = locationInfo.error ?: "",
                                color = Color.Red,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        else -> {
                            // 국가
                            LanguageInfoRow(
                                label = "국가",
                                value = locationInfo.country
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // 지역
                            LanguageInfoRow(
                                label = "지역",
                                value = locationInfo.region
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // 도시
                            LanguageInfoRow(
                                label = "도시",
                                value = locationInfo.city
                            )
                        }
                    }
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

// 현재 로케일 가져오기 (지역명)
fun getCurrentLocale(context: Context): String {
    return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
        context.resources.configuration.locales[0].displayCountry
    } else {
        @Suppress("DEPRECATION")
        context.resources.configuration.locale.displayCountry
    }
}

// 현재 언어 가져오기 (언어명)
fun getCurrentLanguage(context: Context): String {
    return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
        context.resources.configuration.locales[0].displayLanguage
    } else {
        @Suppress("DEPRECATION")
        context.resources.configuration.locale.displayLanguage
    }
}

// 로케일 코드 (예: ko-KR, en-US)
fun getLocaleCode(context: Context): String {
    return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
        context.resources.configuration.locales[0].toString()
    } else {
        @Suppress("DEPRECATION")
        context.resources.configuration.locale.toString()
    }
}

// 언어 코드만 (예: ko, en)
fun getLanguageCode(context: Context): String {
    return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
        context.resources.configuration.locales[0].language
    } else {
        @Suppress("DEPRECATION")
        context.resources.configuration.locale.language
    }
}

// 국가 코드만 (예: KR, US)
fun getCountryCode(context: Context): String {
    return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
        context.resources.configuration.locales[0].country
    } else {
        @Suppress("DEPRECATION")
        context.resources.configuration.locale.country
    }
}

// 위치 정보 가져오기
@SuppressLint("MissingPermission")
suspend fun getLocationInfo(hasLocationPermission: Boolean, context: Context): LocationInfo {
    return withContext(Dispatchers.IO) {
        try {
            if (!hasLocationPermission) {
                throw Exception("위치 권한이 필요합니다")
            }

            val locationManager =
                context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager

            if (!wifiManager.isWifiEnabled) {
                throw Exception("WiFi가 비활성화되어 있습니다")
            }

            val location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                ?: throw Exception("위치 정보를 가져올 수 없습니다")

            val geocoder = Geocoder(context, Locale.getDefault())

            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            if (addresses.isNullOrEmpty()) {
                throw Exception("주소 정보를 가져올 수 없습니다")
            }

            val address = addresses[0]
            LocationInfo(
                country = address.countryName ?: "알 수 없음",
                region = address.adminArea ?: "알 수 없음",
                city = address.locality ?: "알 수 없음",
                isLoading = false,
                error = null
            )
        } catch (e: Exception) {
            throw e
        }
    }
} 