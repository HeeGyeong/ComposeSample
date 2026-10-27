package com.example.composesample.presentation.example.component.ui.media.picker

import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

/**
 * 6번 카드 — 올리기 전 위치 정리. 같은 사진을 시스템 피커·문서 선택기로 받아 앱 캐시로 복사한 뒤
 * 그대로 / GPS 태그 제거 / 재인코딩 / 재인코딩 + 방향 반영 네 가지로 내보내고 같은 판정으로 다시 읽는다.
 * 5번 카드(받은 사진의 위치 측정)와 겹치지 않게 '정리·내보내기' 만 다룬다. 실험 로직은 LocationCleanupLab.kt.
 */
@Composable
internal fun LocationCleanupCard() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lab = remember { LocationCleanupLab(context) }
    var source by remember { mutableStateOf<String?>(null) }
    var checks by remember { mutableStateOf<List<ExportCheck>>(emptyList()) }
    var trials by remember { mutableStateOf<List<FormatTrial>>(emptyList()) }
    var busy by remember { mutableStateOf(false) }
    var launchError by remember { mutableStateOf<String?>(null) }

    // 캐시에 복사한 사본(사용자 사진일 수 있다)은 화면을 떠날 때 지운다
    DisposableEffect(Unit) {
        onDispose { lab.clear() }
    }

    fun export(from: String, uri: Uri?) {
        if (uri == null) {
            source = "$from · 취소"
            return
        }
        busy = true
        scope.launch {
            checks = lab.exportAll(uri)
            source = "$from · ${uri.authority}"
            busy = false
        }
    }

    val pickLauncher = rememberLauncherForActivityResult(PickVisualMedia()) { uri -> export("시스템 피커", uri) }
    val documentLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> export("문서 선택기", uri) }

    ExampleCard(title = "6. 올리기 전 위치 정리 — 0 위치 · 태그 제거 · 재인코딩", accent = Color(0xFF00838F)) {
        BodyText(
            "5번 카드의 피커 결과는 위치가 '없는' 것이 아니라 0 으로 덮여 온다. 프레임워크 getLatLong 은 방향 글자가 비어도 양수로 읽어 " +
                "true 와 (0, 0) 을 돌려주므로, 그 값만 믿는 코드는 적도와 본초자오선이 만나는 기니만 앞바다(Null Island)를 위치로 올린다. " +
                "문서 선택기로 받은 사진은 위치가 원본 그대로다. 받은 사진을 앱 캐시로 복사해 네 가지로 내보내고 같은 판정으로 다시 읽는다."
        )
        CodeBlock(
            "// ✗ 태그만 있으면 위치로 본다 — 피커 사진은 (0, 0)\n" +
                "if (exif.getLatLong(out)) upload(out[0], out[1])\n\n" +
                "// ✓ 0 으로 덮인 값은 위치 없음으로\n" +
                "usableLocation(reading)?.let { (lat, lng) -> upload(lat, lng) }"
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ActionButton("시스템 피커로 받기", enabled = !busy) {
                launchError = launchOrError { pickLauncher.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly)) }
            }
            ActionButton("문서 선택기로 받기", enabled = !busy) {
                launchError = launchOrError { documentLauncher.launch(arrayOf("image/*")) }
            }
        }
        launchError?.let { MonoText(it) }
        if (busy) MonoText("내보내는 중…")
        source?.let { SubTitle("받은 사진 — $it") }
        if (checks.isNotEmpty()) {
            PreviewRow(checks)
            checks.forEach { CheckLines(it) }
        }

        SubTitle("형식별 saveAttributes — 앱이 만든 JPEG · PNG · WebP (API ${Build.VERSION.SDK_INT})")
        SmallButton("GPS 쓰기 → 지우기 시험") {
            scope.launch { trials = lab.formatTrials() }
        }
        trials.forEach { TrialLines(it) }
        Spacer(modifier = Modifier.height(4.dp))
        CaptionText(
            "실측(Pixel Tablet 에뮬레이터 API 35, debug·release 같음 — adb 로 넣은 데모 사진: 1600×1200 으로 저장하고 Orientation 6 을 단 세로 JPEG · PNG · WebP · HEIC, " +
                "모두 GPS 서울 + 고도 38m): 피커로 받은 JPEG·WebP·HEIC 는 GPS 태그 7개(버전·위도·경도·방향 글자·고도)가 자리는 남고 값만 0 이었고, " +
                "파일 크기도 원본과 같았다(제자리에서 덮어씀). ②는 GPS 태그를 0개로 만들면서 방향·촬영 시각·기종은 남겼다. ③은 EXIF 를 전부 잃어 " +
                "세로 사진이 눕고(미리보기 ③) 촬영 시각도 사라졌다 — 태그가 없으면 프레임워크는 Orientation 0 을 돌려준다. ④는 방향을 픽셀에 먼저 반영해 " +
                "1200×1600 으로 바로 섰다. 같은 사진을 문서 선택기로 받으면 ①에 위치와 고도 38/1 이 그대로 있었다."
        )
        CaptionText(
            "형식: HEIC 는 읽기만 되고 saveAttributes 가 IOException('only supports saving attributes for JPEG, PNG, and WebP formats') — 문서 선택기로 받은 " +
                "HEIC 는 ②를 거쳐도 위치가 남아 ③④ 재인코딩만 지운다. 위 시험 버튼은 API 35 에서 셋 다 ✓, Pixel 4 에뮬레이터 API 30 에서는 WebP 만 " +
                "✗('only supports saving attributes on JPEG or PNG formats') — API 30 에서 문서 선택기로 받은 WebP 도 ②에서 위치가 남았다. " +
                "API 30 미만은 재지 못했다(이 환경에서 API 28 이하 에뮬레이터가 붙지 않음)."
        )
        CaptionText(
            "함정 — 피커로 받은 PNG(API 35): 피커가 eXIf 청크 안 GPS 값 20바이트를 0 으로 바꾸면서 청크 CRC 는 그대로 둔다. 프레임워크 ExifInterface 는 " +
                "CRC 가 틀리면('Encountered invalid CRC value for PNG-EXIF chunk', 로그로만) EXIF 전체를 버려 GPS·기종·촬영 시각이 모두 비어 보이고, " +
                "saveAttributes 도 지원하지 않는 형식이라며 거절한다. 위치는 실제로 0 이라 새지 않지만 메타데이터를 다루는 다음 단계가 막힌다 — " +
                "이 카드는 CRC 를 직접 계산해 ⚠ 줄로 보인다."
        )
        CaptionText(
            "받은 URI 에는 쓰지 않는다 — 문서 선택기 결과는 WRITE 권한도 붙어 와서(5번 카드) 그 URI 에 저장하면 사용자 원본이 바뀐다. " +
                "올릴 것은 앱 캐시의 사본이고, 사본은 화면을 떠날 때 지운다."
        )
    }
}

/** 네 사본을 뷰어가 보는 방향으로 나란히 — 방향 태그를 잃은 재인코딩은 눕는다 */
@Composable
private fun PreviewRow(checks: List<ExportCheck>) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 6.dp)) {
        checks.forEach { check ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color(0xFFECEFF1), RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    val preview = check.preview
                    if (preview != null) {
                        Image(
                            bitmap = preview.asImageBitmap(),
                            contentDescription = "${check.variant.mark} 미리보기",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(text = "✗", fontSize = 14.sp, color = Color(0xFFC62828))
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = check.variant.mark, fontSize = 11.sp, color = Color(0xFF424242))
            }
        }
    }
}

@Composable
private fun CheckLines(check: ExportCheck) {
    SubTitle("${check.variant.mark} ${check.variant.label}")
    check.lines().forEach { MonoText(it) }
}

@Composable
private fun TrialLines(trial: FormatTrial) {
    trial.lines().forEach { MonoText(it) }
}
