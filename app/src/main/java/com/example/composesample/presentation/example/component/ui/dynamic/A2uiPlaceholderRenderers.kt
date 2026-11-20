package com.example.composesample.presentation.example.component.ui.dynamic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.a2ui.catalog.A2uiAudioPlayerRenderer
import androidx.compose.material3.a2ui.catalog.A2uiImageRenderer
import androidx.compose.material3.a2ui.catalog.A2uiVideoRenderer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 기본 카탈로그의 Image · Video · AudioPlayer 는 렌더러를 앱이 꼭 넘겨야 한다(materialA2uiBasicCatalogV1 의 필수 인자).
 * 이 예제는 네트워크를 쓰지 않으므로 URL 을 내려받지 않고 자리만 그린다 — 실제 앱이라면 Coil · Media3 로 구현한다.
 */
internal object PlaceholderImageRenderer : A2uiImageRenderer {
    @Composable
    override fun Image(
        url: String,
        contentDescription: String?,
        contentScale: ContentScale,
        modifier: Modifier,
        onError: (throwable: Throwable?) -> Unit
    ) {
        MediaPlaceholder("이미지 자리 — $url", modifier)
    }
}

internal object PlaceholderVideoRenderer : A2uiVideoRenderer {
    @Composable
    override fun Video(url: String, modifier: Modifier, onError: (throwable: Throwable?) -> Unit) {
        MediaPlaceholder("영상 자리 — $url", modifier)
    }
}

internal object PlaceholderAudioRenderer : A2uiAudioPlayerRenderer {
    @Composable
    override fun AudioPlayer(
        url: String,
        contentDescription: String?,
        modifier: Modifier,
        onError: (throwable: Throwable?) -> Unit
    ) {
        MediaPlaceholder("오디오 자리 — $url", modifier)
    }
}

@Composable
private fun MediaPlaceholder(text: String, modifier: Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(Color(0xFFECEFF1), RoundedCornerShape(8.dp))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, fontSize = 11.sp, color = Color(0xFF607D8B))
    }
}
