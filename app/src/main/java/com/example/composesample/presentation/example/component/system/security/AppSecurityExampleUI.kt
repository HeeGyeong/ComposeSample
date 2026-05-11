package com.example.composesample.presentation.example.component.system.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.security.KeyStore
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * 앱 보안 실무 예제
 *
 * 3가지 보안 패턴을 한 화면에서 데모합니다.
 * 1) Certificate Pinning  — OkHttp CertificatePinner 매칭/불일치 시뮬레이션
 * 2) Secure Storage      — AndroidKeyStore + AES-GCM 으로 평문 암호화/복호화 (EncryptedSharedPreferences 내부 동작과 동일)
 * 3) Play Integrity Mock — 토큰 디코딩 + verdict 필드 파싱 (실제 호출 없이 Mock JSON)
 */
@Composable
fun AppSecurityExampleUI(onBackEvent: () -> Unit) {
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
                text = "App Security 실무",
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
            CertificatePinningSection()
            SecureStorageSection()
            PlayIntegritySection()
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ============================================================
// 1) Certificate Pinning — OkHttp CertificatePinner 데모
// ============================================================

@Composable
private fun CertificatePinningSection() {
    // 실제 OkHttp CertificatePinner 가 내부에서 수행하는 SHA-256 SPKI 해시 비교를 직접 시뮬레이션
    // (HeldCertificate / okhttp-tls 의존성 없이 핀 매칭 로직만 재현)
    val host = "api.example.com"
    val serverPublicKey = "SERVER_PUBLIC_KEY_v1_2026"
    val attackerPublicKey = "ATTACKER_FORGED_KEY"

    val expectedPin = remember { sha256Pin(serverPublicKey) }
    val serverPin = remember { sha256Pin(serverPublicKey) }
    val attackerPin = remember { sha256Pin(attackerPublicKey) }

    var verifyResult by remember { mutableStateOf<VerifyResult?>(null) }

    SectionCard(title = "① Certificate Pinning (SHA-256 SPKI 매칭)") {
        BodyText("MITM 공격을 차단하기 위해 서버 인증서의 공개키 해시(SHA-256)를 클라이언트에 사전 고정합니다. OkHttp CertificatePinner 가 같은 방식으로 동작 — 핀 불일치 시 SSLPeerUnverifiedException 으로 연결 차단.")

        Spacer(modifier = Modifier.height(8.dp))
        MonoText("host         = $host")
        MonoText("expected pin = sha256/${expectedPin.take(20)}...")
        MonoText("server  pin  = sha256/${serverPin.take(20)}...")
        MonoText("attacker pin = sha256/${attackerPin.take(20)}...")

        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SmallButton("정상 서버 검증") {
                verifyResult = if (serverPin == expectedPin) {
                    VerifyResult.Ok("✅ 핀 일치 — 연결 허용")
                } else {
                    VerifyResult.Err("❌ 핀 불일치 — 차단")
                }
            }
            SmallButton("MITM 공격자 검증") {
                verifyResult = if (attackerPin == expectedPin) {
                    VerifyResult.Ok("✅ 핀 일치 (예상치 못함)")
                } else {
                    VerifyResult.Err("❌ SSLPeerUnverifiedException — MITM 차단")
                }
            }
        }

        verifyResult?.let {
            Spacer(modifier = Modifier.height(8.dp))
            ResultText(it)
        }
    }
}

private fun sha256Pin(input: String): String {
    val digest = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
    return Base64.encodeToString(digest, Base64.NO_WRAP)
}

private sealed interface VerifyResult {
    val text: String
    data class Ok(override val text: String) : VerifyResult
    data class Err(override val text: String) : VerifyResult
}

// ============================================================
// 2) Secure Storage — AndroidKeyStore AES-GCM 암호화/복호화
// ============================================================

private const val SECURE_KEY_ALIAS = "app_security_example_key"
private const val GCM_TAG_LENGTH_BITS = 128

@Composable
private fun SecureStorageSection() {
    var plaintext by remember { mutableStateOf("내 카드 번호 1234-5678-9012-3456") }
    var cipherBase64 by remember { mutableStateOf("") }
    var ivBase64 by remember { mutableStateOf("") }
    var decrypted by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    SectionCard(title = "② Secure Storage (AndroidKeyStore + AES-GCM)") {
        BodyText("EncryptedSharedPreferences 내부 동작과 동일한 패턴 — AndroidKeyStore 에 AES 키를 생성하여 export 불가능한 상태로 보관하고, AES-GCM 으로 평문을 암호화합니다. 키 자체는 앱 외부로 노출되지 않으며, IV 는 매 암호화마다 재생성해 저장해야 합니다.")

        Spacer(modifier = Modifier.height(8.dp))
        Text("평문", color = Color(0xFFB0BEC5), fontSize = 12.sp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF263238), RoundedCornerShape(6.dp))
                .padding(8.dp)
        ) {
            BasicTextField(
                value = plaintext,
                onValueChange = { plaintext = it },
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = Color.White,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SmallButton("암호화") {
                runCatching {
                    val (iv, cipher) = encryptAesGcm(plaintext.toByteArray())
                    ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP)
                    cipherBase64 = Base64.encodeToString(cipher, Base64.NO_WRAP)
                    decrypted = ""
                    error = null
                }.onFailure { error = "암호화 실패: ${it.message}" }
            }
            SmallButton("복호화") {
                runCatching {
                    val iv = Base64.decode(ivBase64, Base64.NO_WRAP)
                    val cipher = Base64.decode(cipherBase64, Base64.NO_WRAP)
                    decrypted = String(decryptAesGcm(iv, cipher))
                    error = null
                }.onFailure { error = "복호화 실패: ${it.message}" }
            }
        }

        if (cipherBase64.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            MonoText("IV (Base64): $ivBase64")
            MonoText("CIPHER (Base64): ${cipherBase64.take(72)}${if (cipherBase64.length > 72) "..." else ""}")
        }

        if (decrypted.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            ResultText(VerifyResult.Ok("✅ 복호화 결과: $decrypted"))
        }

        error?.let {
            Spacer(modifier = Modifier.height(8.dp))
            ResultText(VerifyResult.Err(it))
        }
    }
}

private fun getOrCreateSecretKey(): SecretKey {
    val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
    keyStore.getKey(SECURE_KEY_ALIAS, null)?.let { return it as SecretKey }

    val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
    val spec = KeyGenParameterSpec.Builder(
        SECURE_KEY_ALIAS,
        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
    )
        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
        .setKeySize(256)
        .build()
    generator.init(spec)
    return generator.generateKey()
}

private fun encryptAesGcm(plain: ByteArray): Pair<ByteArray, ByteArray> {
    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())
    val encrypted = cipher.doFinal(plain)
    // KeyStore 가 자동 생성한 IV (매 호출마다 재생성됨 — 직접 주입 금지)
    return cipher.iv to encrypted
}

private fun decryptAesGcm(iv: ByteArray, cipher: ByteArray): ByteArray {
    val c = Cipher.getInstance("AES/GCM/NoPadding")
    c.init(Cipher.DECRYPT_MODE, getOrCreateSecretKey(), GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
    return c.doFinal(cipher)
}

// ============================================================
// 3) Play Integrity Mock — verdict 페이로드 디코딩
// ============================================================

@Composable
private fun PlayIntegritySection() {
    var verdict by remember { mutableStateOf<MockIntegrityVerdict?>(null) }

    SectionCard(title = "③ Play Integrity API (Mock 응답 디코딩)") {
        BodyText("실제 호출은 Google Play 서비스 + 백엔드 디코딩 키가 필요합니다. 여기서는 응답 페이로드 형태를 Mock 으로 재현해, 어떤 verdict 필드를 검증해야 하는지 보여줍니다.")

        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SmallButton("정상 단말") { verdict = mockVerdict(genuine = true) }
            SmallButton("루팅/에뮬레이터") { verdict = mockVerdict(genuine = false) }
        }

        verdict?.let { v ->
            Spacer(modifier = Modifier.height(8.dp))
            MonoText("appRecognitionVerdict: ${v.appRecognitionVerdict}")
            MonoText("deviceRecognitionVerdict: ${v.deviceRecognitionVerdict}")
            MonoText("appLicensingVerdict: ${v.appLicensingVerdict}")
            MonoText("nonceMatched: ${v.nonceMatched}")

            Spacer(modifier = Modifier.height(8.dp))
            val ok = v.appRecognitionVerdict == "PLAY_RECOGNIZED" &&
                v.deviceRecognitionVerdict == "MEETS_DEVICE_INTEGRITY" &&
                v.nonceMatched
            ResultText(
                if (ok) VerifyResult.Ok("✅ 모든 verdict 통과 — 민감 동작 허용")
                else VerifyResult.Err("❌ verdict 실패 — 민감 동작 차단 권장")
            )
        }
    }
}

private data class MockIntegrityVerdict(
    val appRecognitionVerdict: String,
    val deviceRecognitionVerdict: String,
    val appLicensingVerdict: String,
    val nonceMatched: Boolean
)

private fun mockVerdict(genuine: Boolean): MockIntegrityVerdict {
    val nonce = "nonce-${System.currentTimeMillis()}"
    val expectedHash = MessageDigest.getInstance("SHA-256")
        .digest(nonce.toByteArray())
        .joinToString("") { "%02x".format(it) }
    // 검증 시연을 위해 서버 nonce 와 동일하다고 가정 (실제로는 서버가 응답 nonce 와 비교)
    return if (genuine) {
        MockIntegrityVerdict(
            appRecognitionVerdict = "PLAY_RECOGNIZED",
            deviceRecognitionVerdict = "MEETS_DEVICE_INTEGRITY",
            appLicensingVerdict = "LICENSED",
            nonceMatched = expectedHash.isNotEmpty()
        )
    } else {
        MockIntegrityVerdict(
            appRecognitionVerdict = "UNRECOGNIZED_VERSION",
            deviceRecognitionVerdict = "MEETS_BASIC_INTEGRITY",
            appLicensingVerdict = "UNLICENSED",
            nonceMatched = false
        )
    }
}

// ============================================================
// 공통 UI 컴포넌트
// ============================================================

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

@Composable
private fun ResultText(result: VerifyResult) {
    val color = when (result) {
        is VerifyResult.Ok -> Color(0xFF81C784)
        is VerifyResult.Err -> Color(0xFFEF9A9A)
    }
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(color = color, fontWeight = FontWeight.Bold)) {
                append(result.text)
            }
        },
        fontSize = 13.sp,
        fontFamily = FontFamily.Monospace
    )
}

@Composable
private fun SmallButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF455A64))
    ) {
        Text(label, color = Color.White, fontSize = 13.sp)
    }
}
