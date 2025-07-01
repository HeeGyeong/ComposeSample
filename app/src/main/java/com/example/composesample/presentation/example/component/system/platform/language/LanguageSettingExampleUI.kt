package com.example.composesample.presentation.example.component.system.platform.language

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.composesample.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import com.example.composesample.util.EmulatorUtil

data class LocationInfo(
    val country: String = "",
    val region: String = "",
    val city: String = "",
    val isLoading: Boolean = true,
    val error: String? = null
)

@Composable
fun LanguageSettingExampleUI(onBackButtonClick: () -> Unit) {
    val context = LocalContext.current
    val isEmulator = remember { EmulatorUtil.isEmulator() }

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
            val info = getLocationInfo(hasLocationPermission, context, isEmulator)
            locationInfo = info
        } catch (e: Exception) {
            locationInfo = locationInfo.copy(
                isLoading = false,
                error = e.message ?: "알 수 없는 오류"
            )
        }
    }

    // 권한 요청 콜백
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasLocationPermission = isGranted
        if (isGranted) {
            // 권한이 허용되면 위치 정보 가져오기
            MainScope().launch {
                fetchLocationInfo()
            }
        }
    }

    // 초기 위치 정보 가져오기
    LaunchedEffect(hasLocationPermission) {
        fetchLocationInfo()
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
                            text = "현재 위치 정보" + if (isEmulator) " (에뮬레이터)" else "",
                            fontSize = 16.sp,
                            color = Color.Black
                        )

                        if (!isEmulator && hasLocationPermission) {
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
                        !hasLocationPermission && !isEmulator -> {
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
                                        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
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
@SuppressLint("ObsoleteSdkInt")
fun getCurrentLocale(context: Context): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        context.resources.configuration.locales[0].displayCountry
    } else {
        @Suppress("DEPRECATION")
        context.resources.configuration.locale.displayCountry
    }
}

// 현재 언어 가져오기 (언어명)
@SuppressLint("ObsoleteSdkInt")
fun getCurrentLanguage(context: Context): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        context.resources.configuration.locales[0].displayLanguage
    } else {
        @Suppress("DEPRECATION")
        context.resources.configuration.locale.displayLanguage
    }
}

/**
 * 로케일 코드 (예: ko-KR, en-US)
 *
 * 주의: Android 시스템에서 언어 설정을 변경하면, 시스템이 자동으로 해당 언어의 기본 국가 코드를 매칭합니다.
 * 예: 한국어(ko) → ko-KR, 태국어(th) → th-TH, 영어(en) → en-US
 * 따라서 언어만 변경해도 국가 코드가 함께 변경될 수 있습니다.
 */
@SuppressLint("ObsoleteSdkInt")
fun getLocaleCode(context: Context): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        context.resources.configuration.locales[0].toString()
    } else {
        @Suppress("DEPRECATION")
        context.resources.configuration.locale.toString()
    }
}

// 언어 코드만 (예: ko, en)
@SuppressLint("ObsoleteSdkInt")
fun getLanguageCode(context: Context): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        context.resources.configuration.locales[0].language
    } else {
        @Suppress("DEPRECATION")
        context.resources.configuration.locale.language
    }
}

// 국가 코드만 (예: KR, US)
@SuppressLint("ObsoleteSdkInt")
fun getCountryCode(context: Context): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        context.resources.configuration.locales[0].country
    } else {
        @Suppress("DEPRECATION")
        context.resources.configuration.locale.country
    }
}

// 위치 정보 가져오기
@SuppressLint("MissingPermission")
suspend fun getLocationInfo(
    hasLocationPermission: Boolean,
    context: Context,
    isEmulator: Boolean
): LocationInfo {
    return withContext(Dispatchers.IO) {
        try {
            if (isEmulator) {
                return@withContext LocationInfo(
                    country = "에뮬레이터",
                    region = "가상 지역",
                    city = "가상 도시",
                    isLoading = false,
                    error = null
                )
            }

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