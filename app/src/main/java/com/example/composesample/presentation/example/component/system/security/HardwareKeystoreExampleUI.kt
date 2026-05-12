package com.example.composesample.presentation.example.component.system.security

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyInfo
import android.security.keystore.KeyProperties
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory

/**
 * Hardware-Backed Keystore 검증 예제
 *
 * AndroidKeyStore 에 생성한 키가 실제로 TEE(Trusted Execution Environment) 또는 StrongBox
 * 하드웨어에 보관되는지 진단합니다. 동일 코드라도 단말에 따라 결과가 달라지므로
 * 결제·MDM·DRM 같은 민감 동작 전에 보안 강도를 런타임 검증해야 합니다.
 *
 * API 분기:
 *  - API 23~30  : KeyInfo.isInsideSecureHardware (Boolean)
 *  - API 31+    : KeyInfo.securityLevel (Int)
 *                 SECURITY_LEVEL_SOFTWARE / TRUSTED_ENVIRONMENT / STRONGBOX / UNKNOWN_SECURE
 *  - StrongBox  : setIsStrongBoxBacked(true) — API 28+, 미지원 단말에서는 StrongBoxUnavailableException
 */
@Composable
fun HardwareKeystoreExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1B2A33))
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF2C3E50))
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
                text = "Hardware-Backed Keystore 검증",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DeviceInfoSection()
            TeeKeyDiagnosticSection()
            StrongBoxDiagnosticSection()
            ApiBranchSection()
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ============================================================
// 1) 단말 정보 — API 레벨 / 분기 가능 여부
// ============================================================

@Composable
private fun DeviceInfoSection() {
    SectionCard(title = "① 단말 / API 정보") {
        BodyText("이 단말의 API 레벨에 따라 사용할 수 있는 진단 API 가 다릅니다.")
        Spacer(modifier = Modifier.height(8.dp))
        MonoText("Build.MODEL              = ${Build.MODEL}")
        MonoText("Build.MANUFACTURER       = ${Build.MANUFACTURER}")
        MonoText("Build.VERSION.SDK_INT    = ${Build.VERSION.SDK_INT}")
        Spacer(modifier = Modifier.height(8.dp))
        val branch = when {
            Build.VERSION.SDK_INT >= 31 -> "API 31+ → KeyInfo.securityLevel 사용 가능"
            Build.VERSION.SDK_INT >= 23 -> "API 23~30 → KeyInfo.isInsideSecureHardware 사용 가능 (deprecated 31+)"
            else -> "API 23 미만 → AndroidKeyStore 진단 API 미지원"
        }
        MonoText("진단 분기: $branch")
    }
}

// ============================================================
// 2) TEE 키 진단 — 일반 AES 키 생성 후 보안 강도 판정
// ============================================================

private const val TEE_KEY_ALIAS = "hardware_keystore_example_tee_key"
private const val STRONGBOX_KEY_ALIAS = "hardware_keystore_example_strongbox_key"

@Composable
private fun TeeKeyDiagnosticSection() {
    var diagnostic by remember { mutableStateOf<KeystoreDiagnostic?>(null) }

    SectionCard(title = "② TEE Keystore 진단") {
        BodyText("AndroidKeyStore 에 AES 키를 생성한 뒤, 해당 키가 TEE(보안 칩) 안에 있는지 KeyInfo 로 확인합니다. 결과가 SOFTWARE 면 ROM 안 메모리에 보관 — 루팅 시 추출 가능.")

        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                diagnostic = runCatching {
                    val key = getOrCreateKey(TEE_KEY_ALIAS, strongBox = false)
                    diagnoseKey(TEE_KEY_ALIAS, key)
                }.getOrElse {
                    KeystoreDiagnostic.Error("키 진단 실패: ${it.message}")
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF455A64))
        ) {
            Text("TEE 키 생성 + 진단", color = Color.White, fontSize = 13.sp)
        }

        diagnostic?.let { RenderDiagnostic(it) }
    }
}

// ============================================================
// 3) StrongBox 진단 — API 28+ 전용 하드웨어 보안 모듈
// ============================================================

@Composable
private fun StrongBoxDiagnosticSection() {
    var diagnostic by remember { mutableStateOf<KeystoreDiagnostic?>(null) }

    SectionCard(title = "③ StrongBox 진단 (API 28+)") {
        BodyText("StrongBox 는 TEE 보다 강한 별도 보안 칩(예: Titan M). setIsStrongBoxBacked(true) 호출 시 미지원 단말에서는 StrongBoxUnavailableException 으로 실패하므로 try/catch 로 가용성 확인 후 폴백해야 합니다.")

        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                diagnostic = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                    KeystoreDiagnostic.Error("StrongBox 는 API 28+ 에서만 시도 가능 (현재 ${Build.VERSION.SDK_INT})")
                } else {
                    runCatching {
                        val key = getOrCreateKey(STRONGBOX_KEY_ALIAS, strongBox = true)
                        diagnoseKey(STRONGBOX_KEY_ALIAS, key)
                    }.getOrElse {
                        // StrongBoxUnavailableException 포함
                        KeystoreDiagnostic.Error("StrongBox 미지원 또는 키 생성 실패: ${it.javaClass.simpleName} — ${it.message}")
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF455A64))
        ) {
            Text("StrongBox 키 생성 + 진단", color = Color.White, fontSize = 13.sp)
        }

        diagnostic?.let { RenderDiagnostic(it) }
    }
}

// ============================================================
// 4) API 분기 코드 미리보기
// ============================================================

@Composable
private fun ApiBranchSection() {
    SectionCard(title = "④ API 분기 핵심 코드") {
        MonoText(
            """
            // 1) 키 생성 (TEE 우선 + StrongBox 옵션)
            val spec = KeyGenParameterSpec.Builder(alias,
                PURPOSE_ENCRYPT or PURPOSE_DECRYPT)
                .setBlockModes(BLOCK_MODE_GCM)
                .setEncryptionPaddings(ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .apply {
                    if (Build.VERSION.SDK_INT >= 28 && useStrongBox) {
                        setIsStrongBoxBacked(true)
                    }
                }
                .build()

            // 2) KeyInfo 조회
            val factory = SecretKeyFactory.getInstance(
                key.algorithm, "AndroidKeyStore")
            val info = factory.getKeySpec(key, KeyInfo::class.java) as KeyInfo

            // 3) API 분기 판정
            val isHardware = if (Build.VERSION.SDK_INT >= 31) {
                info.securityLevel == SECURITY_LEVEL_TRUSTED_ENVIRONMENT ||
                info.securityLevel == SECURITY_LEVEL_STRONGBOX
            } else {
                @Suppress("DEPRECATION")
                info.isInsideSecureHardware
            }
            """.trimIndent()
        )
    }
}

// ============================================================
// Keystore 헬퍼
// ============================================================

private sealed interface KeystoreDiagnostic {
    data class Success(
        val alias: String,
        val algorithm: String,
        val keySize: Int,
        val securityLabel: String,
        val isHardwareBacked: Boolean
    ) : KeystoreDiagnostic

    data class Error(val message: String) : KeystoreDiagnostic
}

private fun getOrCreateKey(alias: String, strongBox: Boolean): SecretKey {
    // 매번 새 키를 생성해 환경(StrongBox on/off) 변화에 따라 다시 진단 가능하도록 함
    val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
    if (keyStore.containsAlias(alias)) {
        keyStore.deleteEntry(alias)
    }

    val generator = KeyGenerator.getInstance(
        KeyProperties.KEY_ALGORITHM_AES,
        "AndroidKeyStore"
    )
    val builder = KeyGenParameterSpec.Builder(
        alias,
        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
    )
        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
        .setKeySize(256)

    if (strongBox && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        builder.setIsStrongBoxBacked(true)
    }
    generator.init(builder.build())
    return generator.generateKey()
}

private fun diagnoseKey(alias: String, key: SecretKey): KeystoreDiagnostic {
    val factory = SecretKeyFactory.getInstance(key.algorithm, "AndroidKeyStore")
    val info = factory.getKeySpec(key, KeyInfo::class.java) as KeyInfo

    val (label, isHw) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        // API 31+ : securityLevel 사용
        val level = info.securityLevel
        val name = when (level) {
            KeyProperties.SECURITY_LEVEL_SOFTWARE -> "SECURITY_LEVEL_SOFTWARE"
            KeyProperties.SECURITY_LEVEL_TRUSTED_ENVIRONMENT -> "SECURITY_LEVEL_TRUSTED_ENVIRONMENT (TEE)"
            KeyProperties.SECURITY_LEVEL_STRONGBOX -> "SECURITY_LEVEL_STRONGBOX"
            KeyProperties.SECURITY_LEVEL_UNKNOWN_SECURE -> "SECURITY_LEVEL_UNKNOWN_SECURE"
            else -> "UNKNOWN($level)"
        }
        val hw = level == KeyProperties.SECURITY_LEVEL_TRUSTED_ENVIRONMENT ||
            level == KeyProperties.SECURITY_LEVEL_STRONGBOX ||
            level == KeyProperties.SECURITY_LEVEL_UNKNOWN_SECURE
        name to hw
    } else {
        // API 23~30 : isInsideSecureHardware (deprecated 31+)
        @Suppress("DEPRECATION")
        val inside = info.isInsideSecureHardware
        val name = if (inside) "isInsideSecureHardware = true" else "isInsideSecureHardware = false (SOFTWARE)"
        name to inside
    }

    return KeystoreDiagnostic.Success(
        alias = alias,
        algorithm = key.algorithm,
        keySize = info.keySize,
        securityLabel = label,
        isHardwareBacked = isHw
    )
}

// ============================================================
// 공통 UI 컴포넌트
// ============================================================

@Composable
private fun RenderDiagnostic(diagnostic: KeystoreDiagnostic) {
    Spacer(modifier = Modifier.height(8.dp))
    when (diagnostic) {
        is KeystoreDiagnostic.Success -> {
            MonoText("alias       = ${diagnostic.alias}")
            MonoText("algorithm   = ${diagnostic.algorithm}")
            MonoText("keySize     = ${diagnostic.keySize} bits")
            MonoText("level       = ${diagnostic.securityLabel}")
            Spacer(modifier = Modifier.height(4.dp))
            val verdict = if (diagnostic.isHardwareBacked) {
                "✅ 하드웨어 백업 — 민감 동작 허용 가능"
            } else {
                "❌ 소프트웨어 보관 — 루팅 단말에서 키 추출 가능, 민감 동작 차단 권장"
            }
            Text(
                text = verdict,
                color = if (diagnostic.isHardwareBacked) Color(0xFF81C784) else Color(0xFFEF9A9A),
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
        is KeystoreDiagnostic.Error -> {
            Text(
                text = "⚠ ${diagnostic.message}",
                color = Color(0xFFEF9A9A),
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C3E50))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color(0xFF455A64))
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun BodyText(text: String) {
    Text(text = text, color = Color(0xFFCFD8DC), fontSize = 13.sp)
}

@Composable
private fun MonoText(text: String) {
    Text(
        text = text,
        color = Color(0xFFB0BEC5),
        fontSize = 12.sp,
        fontFamily = FontFamily.Monospace,
        modifier = Modifier.padding(vertical = 1.dp)
    )
}
