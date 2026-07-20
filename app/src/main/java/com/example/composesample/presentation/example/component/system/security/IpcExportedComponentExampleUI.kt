package com.example.composesample.presentation.example.component.system.security

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
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

/**
 * IPC / Exported Component 보안 진단 예제
 *
 * exported/permission 은 매니페스트에 고정되는 값이라 다른 예제처럼 "런타임에 토글"하는 데모는 불가능하다.
 * 대신 이 화면은 각 섹션마다 성격이 다르다 — 텍스트 설명보다 코드/주석을 직접 보는 걸 우선한다.
 * 1) Manifest 진단   — PackageManager 로 이 앱 자신의 컴포넌트를 실시간 스캔(실동작)
 * 2) PendingIntent   — FLAG_MUTABLE vs FLAG_IMMUTABLE 로 실제 payload 변조 여부 비교(실동작)
 * 3) Permission 강제 — signature 권한 선언 패턴(CodeBlock, 실행 없음)
 */
@Composable
fun IpcExportedComponentExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1B2A33))
    ) {
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
                text = "IPC / Exported Component",
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
            ManifestScanSection()
            PendingIntentMutabilitySection()
            PermissionEnforcementSection()
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ============================================================
// 1) Manifest 진단 — PackageManager 런타임 스캔
// ============================================================

private data class ExportedComponentInfo(
    val name: String,
    val type: String,
    val exported: Boolean,
    val permission: String?
)

/**
 * 이 앱 자신의 매니페스트 컴포넌트를 PackageManager로 실시간 조회한다.
 * exported/permission 값 자체는 컴파일 타임에 고정되지만, "그 고정값을 코드로 읽어서 진단"하는 것은
 * 런타임에 매번 가능하다 — 즉 매니페스트 파일을 직접 열어보지 않아도 자가 진단 도구를 만들 수 있다는 뜻.
 *
 * 판단 기준(왜 ⚠️ 인지):
 * - exported=true 인데 permission=null 이면, 이 컴포넌트를 다른 앱이 제한 없이 startActivity/startService/
 *   sendBroadcast로 직접 호출할 수 있다는 뜻 — LAUNCHER Activity처럼 의도된 경우가 아니라면 재검토 대상.
 * - exported=true 인데 permission!=null 이면, 시스템/커스텀 권한이 호출자를 걸러주므로 상대적으로 안전
 *   (예: 이 프로젝트의 QuickSettingsTile 서비스들은 BIND_QUICK_SETTINGS_TILE 권한으로 보호됨).
 * - exported=false 는 명시적 인텐트(같은 앱 또는 알고 있는 컴포넌트)로만 호출 가능해 가장 안전.
 */
private fun scanExportedComponents(context: Context): List<ExportedComponentInfo> {
    val pm = context.packageManager
    val flagBits = (
        PackageManager.GET_ACTIVITIES or
            PackageManager.GET_SERVICES or
            PackageManager.GET_RECEIVERS or
            PackageManager.GET_PROVIDERS
        )
    val pkgInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        pm.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(flagBits.toLong()))
    } else {
        @Suppress("DEPRECATION")
        pm.getPackageInfo(context.packageName, flagBits)
    }

    val activities = pkgInfo.activities?.map {
        ExportedComponentInfo(it.name.substringAfterLast('.'), "Activity", it.exported, it.permission)
    }.orEmpty()
    val services = pkgInfo.services?.map {
        ExportedComponentInfo(it.name.substringAfterLast('.'), "Service", it.exported, it.permission)
    }.orEmpty()
    val receivers = pkgInfo.receivers?.map {
        ExportedComponentInfo(it.name.substringAfterLast('.'), "Receiver", it.exported, it.permission)
    }.orEmpty()
    val providers = pkgInfo.providers?.map {
        ExportedComponentInfo(
            it.name.substringAfterLast('.'),
            "Provider",
            it.exported,
            it.readPermission ?: it.writePermission
        )
    }.orEmpty()

    return (activities + services + receivers + providers)
        .sortedWith(compareByDescending<ExportedComponentInfo> { it.exported }.thenBy { it.type })
}

@Composable
private fun ManifestScanSection() {
    val context = LocalContext.current
    var components by remember { mutableStateOf<List<ExportedComponentInfo>?>(null) }

    SectionCard(title = "① Manifest 진단 (PackageManager 런타임 스캔)") {
        MonoText("판단 기준·근거는 scanExportedComponents() 함수 주석 참고")
        Spacer(modifier = Modifier.height(8.dp))
        SmallButton("이 앱 스캔 실행") {
            components = scanExportedComponents(context)
        }
        Spacer(modifier = Modifier.height(8.dp))
        components?.let { list ->
            val exported = list.filter { it.exported }
            Text(
                text = "exported 컴포넌트 ${exported.size} / 전체 ${list.size}",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 60.dp)
                    .background(Color(0xFF17232B), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Column {
                    exported.forEach { c ->
                        val badge = if (c.permission != null) "✅" else "⚠️"
                        MonoText("$badge ${c.type} · ${c.name} (permission: ${c.permission ?: "없음"})")
                    }
                }
            }
        }
    }
}

// ============================================================
// 2) PendingIntent Mutability — FLAG_MUTABLE vs FLAG_IMMUTABLE
// ============================================================

private const val IPC_DEMO_ACTION = "com.example.composesample.action.IPC_DEMO"
private const val EXTRA_PAYLOAD = "payload"
private const val TAG = "IpcExportedComponentExample"

/**
 * 원본 Intent는 payload="original"로 고정해 PendingIntent를 만든다.
 * 그 뒤 제3자(공격자 역할)가 payload="TAMPERED"로 채운 fillIn Intent를 PendingIntent.send()에 넘긴다.
 *
 * - FLAG_IMMUTABLE 로 만든 PendingIntent: send()에 넘긴 fillIn 인자는 시스템이 통째로 무시 →
 *   리시버는 원본 payload("original")를 그대로 수신한다.
 * - FLAG_MUTABLE(또는 API 31 미만의 기본 동작) 로 만든 PendingIntent: fillIn 이 그대로 적용되어
 *   리시버는 변조된 payload("TAMPERED")를 수신한다.
 *
 * → 앱 밖으로 유출된 PendingIntent가 mutable이면, 그걸 손에 넣은 다른 앱이 내부 Intent를 조작할 수 있다는 뜻.
 * 근거 URL은 exampleGuide.kt 참고.
 */
private fun sendTamperedBroadcast(context: Context, mutable: Boolean) {
    val requestCode = if (mutable) 2 else 1
    val mutabilityFlag = if (mutable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0
    } else {
        PendingIntent.FLAG_IMMUTABLE
    }

    val original = Intent(IPC_DEMO_ACTION)
        .setPackage(context.packageName)
        .putExtra(EXTRA_PAYLOAD, "original")
    val pendingIntent = PendingIntent.getBroadcast(context, requestCode, original, mutabilityFlag)

    val fillIn = Intent().putExtra(EXTRA_PAYLOAD, "TAMPERED")
    runCatching { pendingIntent.send(context, 0, fillIn) }
        .onFailure { Log.e(TAG, "PendingIntent 전송 실패", it) }
}

@Composable
private fun PendingIntentMutabilitySection() {
    val context = LocalContext.current
    val events = remember { mutableStateListOf<String>() }

    // 매니페스트에 등록하지 않은 동적 리시버 — 이 데모 전용이라 앱 프로세스 안에서만 유효하다.
    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                val received = intent?.getStringExtra(EXTRA_PAYLOAD)
                events.add(0, "수신 payload = \"$received\"")
            }
        }
        val filter = IntentFilter(IPC_DEMO_ACTION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(receiver, filter)
        }
        onDispose { runCatching { context.unregisterReceiver(receiver) } }
    }

    SectionCard(title = "② PendingIntent Mutability (FLAG_MUTABLE vs FLAG_IMMUTABLE)") {
        MonoText("판단 근거·시나리오는 sendTamperedBroadcast() 함수 주석 참고")
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SmallButton("Immutable로 변조 시도") {
                events.add(0, "--- FLAG_IMMUTABLE ---")
                sendTamperedBroadcast(context, mutable = false)
            }
            SmallButton("Mutable로 변조 시도") {
                events.add(0, "--- FLAG_MUTABLE ---")
                sendTamperedBroadcast(context, mutable = true)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        EventLog(events)
    }
}

// ============================================================
// 3) Permission Enforcement — 커스텀 signature 권한 (CodeBlock)
// ============================================================

@Composable
private fun PermissionEnforcementSection() {
    SectionCard(title = "③ Permission Enforcement (커스텀 signature 권한)") {
        MonoText("실행 데모 없음 — 아래 코드/주석이 설명의 전부")
        Spacer(modifier = Modifier.height(8.dp))
        CodeBlock(
            """
            <!-- AndroidManifest.xml -->

            <!-- 1) 커스텀 권한 선언 (protectionLevel="signature" → 같은 서명 앱만 통과) -->
            <permission
                android:name="com.example.composesample.permission.CALL_INTERNAL_API"
                android:protectionLevel="signature" />

            <!-- 2) exported 컴포넌트에 권한 강제 -->
            <activity
                android:name=".InternalApiActivity"
                android:exported="true"
                android:permission="com.example.composesample.permission.CALL_INTERNAL_API" />

            // 3) 같은 서명으로 빌드된 앱은 permission 선언 없이도 통과된다.
            //    서명이 다른 앱이 startActivity()로 호출하면 SecurityException 발생.
            //    protectionLevel 후보: "normal"(누구나) / "dangerous"(런타임 동의) /
            //    "signature"(같은 서명만, 이 패턴) / "signatureOrSystem"(서명 또는 시스템 앱, deprecated 권장 안 함)
            """.trimIndent()
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
private fun SmallButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF455A64))
    ) {
        Text(label, color = Color.White, fontSize = 13.sp)
    }
}

@Composable
private fun EventLog(events: List<String>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .background(Color(0xFF17232B), RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        if (events.isEmpty()) {
            Text(
                text = "아직 실행된 이벤트가 없습니다.",
                color = Color(0xFF78909C),
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        } else {
            Column {
                events.take(10).forEach { msg ->
                    Text(
                        text = msg,
                        color = Color(0xFF81C784),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(vertical = 1.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CodeBlock(code: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF455A64), RoundedCornerShape(8.dp))
            .background(Color(0xFF17232B), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(
            text = code,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFFB0BEC5),
            lineHeight = 16.sp
        )
    }
}
