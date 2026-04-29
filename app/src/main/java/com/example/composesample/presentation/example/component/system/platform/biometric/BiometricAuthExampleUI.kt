package com.example.composesample.presentation.example.component.system.platform.biometric

import androidx.biometric.AuthenticationRequest
import androidx.biometric.AuthenticationRequest.Companion.biometricRequest
import androidx.biometric.AuthenticationResult
import androidx.biometric.AuthenticationResultCallback
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators
import androidx.biometric.compose.rememberAuthenticationLauncher
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BiometricAuthExampleUI(onBackEvent: () -> Unit) {
    val context = LocalContext.current

    // 시스템 가용성 진단 (Class2 + DeviceCredential 기준)
    val availability = remember {
        diagnoseAvailability(BiometricManager.from(context))
    }

    // 마지막 결과 / 시도 실패 카운트
    var lastResult by remember { mutableStateOf("아직 시도하지 않음") }
    var lastResultColor by remember { mutableStateOf(Color(0xFFB0BEC5)) }
    val attemptLog = remember { mutableStateListOf<String>() }

    fun pushLog(message: String) {
        attemptLog.add(0, message)
        if (attemptLog.size > 8) attemptLog.removeAt(attemptLog.size - 1)
    }

    val resultCallback = remember {
        object : AuthenticationResultCallback {
            override fun onAuthResult(result: AuthenticationResult) {
                when (result) {
                    is AuthenticationResult.Success -> {
                        lastResult = "✅ 성공 (authType=${result.authType})"
                        lastResultColor = Color(0xFF66BB6A)
                        pushLog("Success — authType=${result.authType}")
                    }
                    is AuthenticationResult.Error -> {
                        lastResult = "❌ 에러 [${result.errorCode}] ${result.errString}"
                        lastResultColor = Color(0xFFEF5350)
                        pushLog("Error[${result.errorCode}]: ${result.errString}")
                    }
                }
            }

            override fun onAuthAttemptFailed() {
                // 시스템 UI에 이미 "Not recognized"가 표시되므로 통계 용도만
                pushLog("Attempt failed (잘못된 지문/얼굴)")
            }
        }
    }

    val launcher = rememberAuthenticationLauncher(resultCallback)

    // 시나리오 1: Class2 + NegativeButton 폴백 (가장 단순)
    val basicRequest = remember {
        biometricRequest(
            title = "기본 생체 인증",
            authFallback = AuthenticationRequest.Biometric.Fallback.NegativeButton("취소")
        ) {
            setSubtitle("등록된 생체 정보로 인증해 주세요")
            setMinStrength(AuthenticationRequest.Biometric.Strength.Class2)
        }
    }

    // 시나리오 2: Class2 + DeviceCredential 폴백 (PIN/Pattern/Password로 폴백)
    val credentialFallbackRequest = remember {
        biometricRequest(
            title = "보안 인증",
            authFallback = AuthenticationRequest.Biometric.Fallback.DeviceCredential
        ) {
            setSubtitle("생체 인증 또는 화면 잠금 자격 증명")
            setMinStrength(AuthenticationRequest.Biometric.Strength.Class2)
        }
    }

    // 시나리오 3: Class3 강력 인증 (Crypto와 결합 가능, 단 API 28/29에서 DeviceCredential과 동시 사용 불가)
    val strongRequest = remember {
        biometricRequest(
            title = "강력 인증 (Class3)",
            authFallback = AuthenticationRequest.Biometric.Fallback.NegativeButton("취소")
        ) {
            setSubtitle("결제·키 보호 등 민감 동작에 사용")
            setMinStrength(AuthenticationRequest.Biometric.Strength.Class3())
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF263238))
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF37474F))
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackEvent) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Text(
                text = "Biometric Auth in Compose",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Default
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            AvailabilityCard(availability)

            Spacer(modifier = Modifier.height(16.dp))

            ResultCard(text = lastResult, accent = lastResultColor)

            Spacer(modifier = Modifier.height(16.dp))

            ScenarioCard(
                title = "1) 기본 인증 (Class2 + NegativeButton)",
                description = "가장 단순한 형태. NegativeButton 폴백은 다이얼로그 좌측에 표시되며 누르면 Error로 종료.",
                buttonLabel = "기본 인증 시작",
                enabled = availability.canAuthenticate,
                onClick = { launcher.launch(basicRequest) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            ScenarioCard(
                title = "2) DeviceCredential 폴백 (PIN/Pattern)",
                description = "지문 등 생체 실패 시 화면 잠금 자격 증명으로 폴백. 생체 미등록 기기에서도 동작 가능.",
                buttonLabel = "자격 증명 폴백 인증",
                enabled = true, // DeviceCredential 폴백이 있으므로 NONE_ENROLLED여도 시도 가능
                onClick = { launcher.launch(credentialFallbackRequest) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            ScenarioCard(
                title = "3) 강력 인증 (Class3)",
                description = "Crypto Object와 결합 가능한 Class3(Strong). 결제·키 보호 등 민감 동작에 사용. API 28/29에서 DeviceCredential 폴백과 동시 사용 불가.",
                buttonLabel = "강력 인증 시작",
                enabled = availability.canAuthenticate,
                onClick = { launcher.launch(strongRequest) }
            )

            Spacer(modifier = Modifier.height(20.dp))

            HorizontalDivider(color = Color(0xFF455A64))

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "이벤트 로그",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (attemptLog.isEmpty()) {
                Text(
                    text = "(아직 이벤트 없음)",
                    color = Color(0xFF90A4AE),
                    fontSize = 13.sp
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    items(attemptLog) { entry ->
                        Text(
                            text = "• $entry",
                            color = Color(0xFFCFD8DC),
                            fontSize = 13.sp,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AvailabilityCard(availability: AvailabilityState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF37474F))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🔐 BiometricManager 진단",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = availability.statusMessage,
                color = if (availability.canAuthenticate) Color(0xFF81C784) else Color(0xFFFFB74D),
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "원시 코드: ${availability.rawCode}",
                color = Color(0xFFB0BEC5),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun ResultCard(text: String, accent: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF37474F))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "최근 결과",
                color = Color(0xFFB0BEC5),
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = text,
                color = accent,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ScenarioCard(
    title: String,
    description: String,
    buttonLabel: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF37474F))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                color = Color(0xFFCFD8DC),
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onClick,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1976D2),
                    disabledContainerColor = Color(0xFF455A64)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = buttonLabel, color = Color.White)
            }
        }
    }
}

private data class AvailabilityState(
    val canAuthenticate: Boolean,
    val statusMessage: String,
    val rawCode: Int
)

private fun diagnoseAvailability(manager: BiometricManager): AvailabilityState {
    val authenticators = Authenticators.BIOMETRIC_WEAK or Authenticators.DEVICE_CREDENTIAL
    val code = manager.canAuthenticate(authenticators)
    val (ok, msg) = when (code) {
        BiometricManager.BIOMETRIC_SUCCESS ->
            true to "사용 가능 — 등록된 생체/자격 증명 확인됨"
        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
            false to "하드웨어 없음 — DeviceCredential 폴백만 사용 가능"
        BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
            false to "하드웨어 일시 불가 — 잠시 후 재시도"
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
            false to "등록된 생체 정보 없음 — 시스템 설정에서 등록 필요"
        BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED ->
            false to "보안 업데이트 필요"
        BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED ->
            false to "현재 인증자 조합 미지원"
        BiometricManager.BIOMETRIC_STATUS_UNKNOWN ->
            false to "상태 알 수 없음"
        else ->
            false to "알 수 없는 코드"
    }
    return AvailabilityState(canAuthenticate = ok, statusMessage = msg, rawCode = code)
}
