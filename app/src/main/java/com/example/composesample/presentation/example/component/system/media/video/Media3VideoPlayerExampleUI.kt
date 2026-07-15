package com.example.composesample.presentation.example.component.system.media.video

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.composesample.presentation.MainHeader
import com.example.composesample.util.OnLifecycleEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.util.Locale

// Google 공개 샘플 비디오(ExoPlayer 데모에서 널리 쓰이는 테스트 스트림).
private const val SAMPLE_VIDEO_URL =
    "https://storage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"

@Composable
fun Media3VideoPlayerExampleUI(onBackEvent: () -> Unit) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(SAMPLE_VIDEO_URL))
            prepare()
        }
    }
    var isPlaying by remember { mutableStateOf(false) }
    var playbackStateLabel by remember { mutableStateOf("IDLE") }
    var positionMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(0L) }

    // Player.Listener 로 재생 상태를 구독하고, 컴포지션 이탈 시 리스너 해제 + 네이티브 리소스 release.
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlaybackStateChanged(state: Int) {
                playbackStateLabel = when (state) {
                    Player.STATE_IDLE -> "IDLE"
                    Player.STATE_BUFFERING -> "BUFFERING"
                    Player.STATE_READY -> "READY"
                    Player.STATE_ENDED -> "ENDED"
                    else -> "UNKNOWN"
                }
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    // Media3 는 진행률(position/duration)을 콜백이 아닌 폴링으로 조회해야 하므로 500ms 주기로 갱신.
    LaunchedEffect(exoPlayer) {
        while (isActive) {
            positionMs = exoPlayer.currentPosition.coerceAtLeast(0L)
            durationMs = exoPlayer.duration.coerceAtLeast(0L)
            delay(500)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Media3 비디오 재생 (ExoPlayer)",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { ConceptCard() }
            item {
                VideoPlayerCard(
                    exoPlayer = exoPlayer,
                    isPlaying = isPlaying,
                    playbackStateLabel = playbackStateLabel,
                    positionMs = positionMs,
                    durationMs = durationMs
                )
            }
            item { LifecycleManagementCard(exoPlayer = exoPlayer) }
            item { SummaryCard() }
        }
    }
}

@Composable
private fun ConceptCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Media3 란",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "androidx.media3 는 구버전 ExoPlayer(com.google.android.exoplayer2)를 흡수 통합한 Jetpack 공식 미디어 라이브러리입니다. " +
                        "재생 엔진(media3-exoplayer)·재생 컨트롤 UI(media3-ui 의 PlayerView)·공통 모델(media3-common 의 MediaItem/Player)이 " +
                        "모듈로 분리되어 있어 필요한 모듈만 선택적으로 추가합니다. Compose 에는 PlayerView 를 AndroidView 로 임베딩해 사용합니다.",
                fontSize = 13.sp,
                color = Color(0xFF424242),
                lineHeight = 19.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            CodeBlock(
                code = "dependencies {\n" +
                        "    implementation(\"androidx.media3:media3-exoplayer:1.8.0\")\n" +
                        "    implementation(\"androidx.media3:media3-ui:1.8.0\")\n" +
                        "}\n\n" +
                        "val player = ExoPlayer.Builder(context).build().apply {\n" +
                        "    setMediaItem(MediaItem.fromUri(videoUrl))\n" +
                        "    prepare()\n" +
                        "}",
                borderColor = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(8.dp))

            val rows = listOf(
                Triple("media3-exoplayer", "재생 엔진", "디코딩·버퍼링·트랙 선택을 담당하는 ExoPlayer 구현체"),
                Triple("media3-ui", "PlayerView", "재생/일시정지/탐색바 컨트롤 UI — Compose 에는 AndroidView 로 임베딩"),
                Triple("media3-common", "MediaItem / Player", "재생 대상과 재생 상태를 나타내는 공통 인터페이스")
            )
            rows.forEach { (module, role, note) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = module, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium, color = Color(0xFF1976D2), modifier = Modifier.weight(0.34f))
                    Text(text = role, fontSize = 10.sp, color = Color(0xFF757575), modifier = Modifier.weight(0.24f))
                    Text(text = note, fontSize = 10.sp, color = Color(0xFF757575), modifier = Modifier.weight(0.42f))
                }
            }
        }
    }
}

@Composable
private fun VideoPlayerCard(
    exoPlayer: ExoPlayer,
    isPlaying: Boolean,
    playbackStateLabel: String,
    positionMs: Long,
    durationMs: Long
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "실동작 — ExoPlayer + PlayerView",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF388E3C)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "AndroidView 로 PlayerView 를 임베딩하고, remember 로 1회 생성한 ExoPlayer 를 바인딩해 실제 네트워크 비디오를 재생합니다.",
                fontSize = 12.sp,
                color = Color(0xFF757575),
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = true
                    }
                },
                update = { view -> view.player = exoPlayer },
                onRelease = { view -> view.player = null },
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Color.Black, RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                ) {
                    Text(text = if (isPlaying) "일시정지" else "재생", color = Color.White, fontSize = 12.sp)
                }
                Button(
                    onClick = {
                        exoPlayer.seekTo(0L)
                        exoPlayer.play()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF757575))
                ) {
                    Text(text = "처음으로", color = Color.White, fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            Slider(
                value = positionMs.toFloat(),
                onValueChange = { exoPlayer.seekTo(it.toLong()) },
                valueRange = 0f..durationMs.toFloat().coerceAtLeast(1f),
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = formatMs(positionMs), fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF424242))
                Text(text = formatMs(durationMs), fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF424242))
            }
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF212121), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Player.Listener 상태", fontSize = 11.sp, color = Color(0xFF9E9E9E))
                Text(
                    text = "$playbackStateLabel · ${if (isPlaying) "재생 중" else "정지"}",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFFB3E5FC)
                )
            }
        }
    }
}

@Composable
private fun LifecycleManagementCard(exoPlayer: ExoPlayer) {
    var lastLifecycleEvent by remember { mutableStateOf("(아직 이벤트 없음 — 홈 버튼으로 앱을 백그라운드로 보내보세요)") }

    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_STOP -> {
                exoPlayer.pause()
                lastLifecycleEvent = "⏸ ON_STOP 수신 — exoPlayer.pause() 자동 호출(백그라운드 재생 방지)"
            }
            Lifecycle.Event.ON_START -> {
                lastLifecycleEvent = "▶ ON_START 수신 — 자동 재생은 하지 않고 사용자의 재생 버튼 입력을 대기"
            }
            else -> {}
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "생명주기 관리 — 백그라운드 재생 & 리소스 누수 방지",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF388E3C)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "ExoPlayer 는 네이티브 디코더 리소스를 점유합니다. 화면을 벗어나면 release() 로 정리하고, " +
                        "앱이 백그라운드로 전환되면(ON_STOP) 자동으로 일시정지해야 오디오가 계속 흐르는 것을 막을 수 있습니다.",
                fontSize = 12.sp,
                color = Color(0xFF757575),
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            CodeBlock(
                code = "// 화면 이탈 시 리스너 해제 + release()\n" +
                        "DisposableEffect(exoPlayer) {\n" +
                        "    onDispose { exoPlayer.release() }\n" +
                        "}\n\n" +
                        "// AndroidView 이탈 시 View 쪽 참조 해제(WebViewIssueUI 와 동일 관례)\n" +
                        "AndroidView(\n" +
                        "    factory = { PlayerView(it).apply { player = exoPlayer } },\n" +
                        "    onRelease = { view -> view.player = null }\n" +
                        ")\n\n" +
                        "// 앱이 백그라운드로 전환되면 자동 일시정지\n" +
                        "OnLifecycleEvent { _, event ->\n" +
                        "    if (event == Lifecycle.Event.ON_STOP) exoPlayer.pause()\n" +
                        "}",
                borderColor = Color(0xFF388E3C)
            )
            Spacer(modifier = Modifier.height(8.dp))

            val rows = listOf(
                Triple("정리 누락 시", "release() 미호출", "네이티브 디코더/버퍼 리소스 누수, 앱 백그라운드에도 오디오 계속 재생"),
                Triple("정리 시", "release() + ON_STOP pause()", "화면 이탈 즉시 리소스 반환, 백그라운드 전환 시 재생 자동 중단")
            )
            rows.forEach { (aspect, action, note) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                        .background(Color(0xFFEDE7F6), RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = aspect, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF5E35B1), modifier = Modifier.weight(0.28f))
                    Text(text = action, fontSize = 10.sp, color = Color(0xFF757575), modifier = Modifier.weight(0.32f))
                    Text(text = note, fontSize = 10.sp, color = Color(0xFF757575), modifier = Modifier.weight(0.4f))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF212121), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(
                    text = lastLifecycleEvent,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFFB3E5FC),
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun SummaryCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "핵심 정리",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(8.dp))
            val bullets = listOf(
                "androidx.media3 는 구버전 ExoPlayer(com.google.android.exoplayer2)를 흡수 통합한 Jetpack 공식 미디어 라이브러리다",
                "media3-exoplayer(재생 엔진) / media3-ui(PlayerView) / media3-common(MediaItem·Player) 이 모듈로 분리되어 필요한 것만 추가한다",
                "Compose 에는 PlayerView 를 AndroidView 로 임베딩하고, ExoPlayer 인스턴스는 remember 로 1회만 생성해 재구성 시 재생성을 막는다",
                "Player.Listener(onIsPlayingChanged/onPlaybackStateChanged)로 재생 상태를 구독하고, 진행률(position/duration)은 콜백이 아닌 폴링으로 조회한다",
                "네이티브 리소스이므로 화면 이탈 시 release(), 앱 백그라운드 전환(ON_STOP) 시 pause() 를 반드시 처리해야 누수와 백그라운드 재생을 막을 수 있다"
            )
            bullets.forEach { bullet ->
                Row(modifier = Modifier.padding(vertical = 3.dp)) {
                    Text(text = "• ", fontSize = 13.sp, color = Color(0xFF1976D2))
                    Text(text = bullet, fontSize = 12.sp, color = Color(0xFF424242), lineHeight = 17.sp)
                }
            }
        }
    }
}

@Composable
private fun CodeBlock(code: String, borderColor: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, borderColor, RoundedCornerShape(8.dp))
            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(
            text = code,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF212121),
            lineHeight = 16.sp
        )
    }
}

private fun formatMs(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%02d:%02d", minutes, seconds)
}
