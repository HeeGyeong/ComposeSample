package com.example.composesample.presentation.example.component.ui.media.image

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.compose.LocalPlatformContext
import coil3.memory.MemoryCache
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.composesample.presentation.MainHeader

// 데모용 이미지 URL — picsum 은 seed 별로 고정 이미지를 반환하므로 캐시 동작(NETWORK→MEMORY_CACHE) 관찰에 적합
private const val VALID_IMAGE_URL = "https://picsum.photos/seed/coil3-sample/420/240"
private const val BROKEN_IMAGE_URL = "https://invalid.example.com/not-exist.png"
private const val CACHE_IMAGE_URL = "https://picsum.photos/seed/coil3-cache/420/240"

@Composable
fun Coil3ImageExampleUI(onBackEvent: () -> Unit) {
    // 화면 전체가 공유하는 커스텀 ImageLoader — 메모리 캐시를 직접 구성해 캐시 사용량/비우기를 시연한다.
    val context = LocalPlatformContext.current
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, 0.25) // 앱 가용 메모리의 25% 를 메모리 캐시로 사용
                    .build()
            }
            .crossfade(true)
            .build()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Coil 3 Image Loading",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { ConceptCard() }
            item { AsyncImageStateCard(imageLoader) }
            item { CachePolicyCard(imageLoader) }
            item { ImageLoaderCustomizationCard(imageLoader) }
            item { SummaryCard() }
        }
    }
}

@Composable
private fun ConceptCard() {
    SectionCard {
        Text(
            text = "Coil 3 이미지 로딩",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1976D2)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Coil 3 는 코루틴 기반 이미지 로딩 라이브러리입니다. AsyncImage 로 네트워크 이미지를 비동기 로딩하고, " +
                    "내부적으로 ImageRequest → ImageLoader(fetch/decode) → 메모리/디스크 캐시 단계를 거칩니다. " +
                    "이 예제는 기존 Coil 2(coil.*)와 충돌 없이 별도 coil3.* 네임스페이스로 동작합니다.",
            fontSize = 13.sp,
            color = Color(0xFF424242),
            lineHeight = 19.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        val pieces = listOf(
            Triple("ImageRequest", "data/policy", "URL · 캐시 정책 · crossfade 설정"),
            Triple("ImageLoader", "fetch/decode", "네트워크 fetcher + 디코더 + 캐시 보유"),
            Triple("MemoryCache", "in-memory", "디코드된 Bitmap 캐시(가장 빠름)"),
            Triple("DiskCache", "okio", "원본 바이트 디스크 캐시"),
            Triple("AsyncImage", "@Composable", "onState 로 Loading/Success/Error 처리")
        )
        pieces.forEach { (name, tag, note) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = name, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium, color = Color(0xFF1976D2), modifier = Modifier.weight(0.36f))
                Text(text = tag, fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF757575), modifier = Modifier.weight(0.24f))
                Text(text = note, fontSize = 10.sp, color = Color(0xFF757575), modifier = Modifier.weight(0.4f))
            }
        }
    }
}

@Composable
private fun AsyncImageStateCard(imageLoader: ImageLoader) {
    // 정상 URL ↔ 깨진 URL 토글로 Success/Error 폴백을 확인. onState 콜백으로 현재 상태를 배지에 표시.
    var useBrokenUrl by remember { mutableStateOf(false) }
    var reloadKey by remember { mutableIntStateOf(0) }
    var stateLabel by remember { mutableStateOf("대기 중") }
    var stateColor by remember { mutableStateOf(Color(0xFF757575)) }

    val url = if (useBrokenUrl) BROKEN_IMAGE_URL else VALID_IMAGE_URL
    val context = LocalPlatformContext.current
    // reloadKey 가 바뀌면 새 ImageRequest 인스턴스를 만들어 재로딩을 트리거한다.
    val request = remember(url, reloadKey) {
        ImageRequest.Builder(context)
            .data(url)
            .crossfade(true)
            .build()
    }

    SectionCard {
        Text(
            text = "① AsyncImage 상태 (crossfade / placeholder / error)",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF388E3C)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "AsyncImage 는 로딩 중에는 회색 배경(placeholder)을, 실패 시 붉은 배경(error)을 보여주고, " +
                    "crossfade 로 부드럽게 전환합니다. onState 콜백으로 Loading→Success/Error 전이를 관찰합니다.",
            fontSize = 12.sp,
            color = Color(0xFF757575),
            lineHeight = 16.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        DemoImage(
            request = request,
            imageLoader = imageLoader,
            height = 180.dp,
            onState = { state ->
                when (state) {
                    is AsyncImagePainter.State.Loading -> {
                        stateLabel = "Loading…"
                        stateColor = Color(0xFFF9A825)
                    }

                    is AsyncImagePainter.State.Success -> {
                        stateLabel = "Success (${describeSource(state.result.dataSource.name)})"
                        stateColor = Color(0xFF2E7D32)
                    }

                    is AsyncImagePainter.State.Error -> {
                        stateLabel = "Error — error 배경 표시"
                        stateColor = Color(0xFFC62828)
                    }

                    AsyncImagePainter.State.Empty -> {
                        stateLabel = "Empty"
                        stateColor = Color(0xFF757575)
                    }
                }
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        StatusBadge(label = "상태: $stateLabel", color = stateColor)
        Spacer(modifier = Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { reloadKey++ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C)),
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "다시 로드", fontSize = 13.sp)
            }
            Button(
                onClick = { useBrokenUrl = !useBrokenUrl },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (useBrokenUrl) Color(0xFFC62828) else Color(0xFF607D8B)
                ),
                modifier = Modifier.weight(1.3f)
            ) {
                Text(text = if (useBrokenUrl) "깨진 URL (에러)" else "정상 URL", fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun CachePolicyCard(imageLoader: ImageLoader) {
    // 같은 URL 을 memoryCachePolicy ENABLED/DISABLED 로 재요청해 dataSource 변화를 추적.
    var memoryEnabled by remember { mutableStateOf(true) }
    var reloadKey by remember { mutableIntStateOf(0) }
    var sourceLabel by remember { mutableStateOf("아직 로드 안 함") }
    var sourceColor by remember { mutableStateOf(Color(0xFF757575)) }

    val context = LocalPlatformContext.current
    val request = remember(memoryEnabled, reloadKey) {
        ImageRequest.Builder(context)
            .data(CACHE_IMAGE_URL)
            .memoryCachePolicy(if (memoryEnabled) CachePolicy.ENABLED else CachePolicy.DISABLED)
            .crossfade(false)
            .build()
    }

    SectionCard {
        Text(
            text = "② 캐시 정책 (memoryCachePolicy)",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF5E35B1)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "처음 로드는 NETWORK 에서, 같은 URL 을 다시 로드하면 메모리 캐시가 켜진 경우 MEMORY_CACHE 에서 즉시 제공됩니다. " +
                    "memoryCachePolicy 를 DISABLED 로 두면 캐시를 건너뛰고 항상 NETWORK 로 요청합니다. " +
                    "SuccessResult.dataSource 로 어디서 왔는지 확인합니다.",
            fontSize = 12.sp,
            color = Color(0xFF757575),
            lineHeight = 16.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        DemoImage(
            request = request,
            imageLoader = imageLoader,
            height = 150.dp,
            onState = { state ->
                if (state is AsyncImagePainter.State.Success) {
                    val source = state.result.dataSource.name
                    sourceLabel = "출처: ${describeSource(source)} ($source)"
                    sourceColor = if (source.contains("CACHE") || source == "MEMORY") {
                        Color(0xFF2E7D32)
                    } else {
                        Color(0xFF1565C0)
                    }
                } else if (state is AsyncImagePainter.State.Error) {
                    sourceLabel = "에러 — 네트워크 미연결 시 캐시 시연 불가(개념은 위 설명 참고)"
                    sourceColor = Color(0xFFC62828)
                }
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        StatusBadge(label = sourceLabel, color = sourceColor)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "메모리 캐시 ${if (memoryEnabled) "ENABLED" else "DISABLED"}",
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF424242)
            )
            Switch(checked = memoryEnabled, onCheckedChange = { memoryEnabled = it })
        }
        Spacer(modifier = Modifier.height(4.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { reloadKey++ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5E35B1)),
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "같은 이미지 다시 로드", fontSize = 13.sp)
            }
            Button(
                onClick = {
                    imageLoader.memoryCache?.clear()
                    reloadKey++
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF607D8B)),
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "메모리 캐시 비우기", fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun ImageLoaderCustomizationCard(imageLoader: ImageLoader) {
    var cacheInfo by remember { mutableStateOf("아래 이미지를 로드하면 표시됩니다") }

    val context = LocalPlatformContext.current
    val request = remember {
        ImageRequest.Builder(context)
            .data("https://picsum.photos/seed/coil3-loader/420/200")
            .crossfade(true)
            .build()
    }

    SectionCard {
        Text(
            text = "③ ImageLoader 커스터마이징",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFEF6C00)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "이 화면의 모든 이미지는 아래처럼 직접 구성한 ImageLoader 를 사용합니다. " +
                    "메모리 캐시 비율, 디스크 캐시 크기/위치, crossfade 등을 한곳에서 제어합니다.",
            fontSize = 12.sp,
            color = Color(0xFF757575),
            lineHeight = 16.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        CodeBlock(
            code = "val imageLoader = ImageLoader.Builder(context)\n" +
                    "    .memoryCache {\n" +
                    "        MemoryCache.Builder()\n" +
                    "            .maxSizePercent(context, 0.25) // 가용 메모리 25%\n" +
                    "            .build()\n" +
                    "    }\n" +
                    "    .diskCache {\n" +
                    "        DiskCache.Builder()\n" +
                    "            .directory(context.cacheDir.resolve(\"image_cache\").toOkioPath())\n" +
                    "            .maxSizeBytes(50L * 1024 * 1024) // 50MB\n" +
                    "            .build()\n" +
                    "    }\n" +
                    "    .crossfade(true)\n" +
                    "    .build()\n\n" +
                    "// AsyncImage(model = url, imageLoader = imageLoader, onState = { ... })",
            borderColor = Color(0xFFEF6C00)
        )
        Spacer(modifier = Modifier.height(12.dp))

        DemoImage(
            request = request,
            imageLoader = imageLoader,
            height = 150.dp,
            onState = { state ->
                if (state is AsyncImagePainter.State.Success) {
                    val cache = imageLoader.memoryCache
                    val sizeKb = (cache?.size ?: 0L) / 1024
                    val maxKb = (cache?.maxSize ?: 0L) / 1024
                    cacheInfo = "메모리 캐시 사용량: ${sizeKb}KB / ${maxKb}KB"
                }
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        StatusBadge(label = cacheInfo, color = Color(0xFFEF6C00))
    }
}

@Composable
private fun SummaryCard() {
    SectionCard {
        Text(
            text = "핵심 정리",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1976D2)
        )
        Spacer(modifier = Modifier.height(8.dp))
        val bullets = listOf(
            "Coil 3 는 AsyncImage 의 placeholder/error Painter 인자를 제거했다 — placeholder·에러 UI 는 Box 배경이나 SubcomposeAsyncImage content 로 직접 구성한다",
            "onState 콜백으로 Loading/Success/Error 전이를 추적하고, Success.result.dataSource 로 출처(NETWORK/MEMORY_CACHE/DISK)를 알 수 있다",
            "memoryCachePolicy/diskCachePolicy 로 요청 단위로 캐시 사용 여부를 제어한다",
            "ImageLoader.Builder 로 메모리·디스크 캐시 정책과 crossfade 를 한곳에서 구성하고 imageLoader 인자로 주입한다",
            "Coil 3 는 coil3.* 네임스페이스라 기존 Coil 2(coil.*)와 한 프로젝트에서 공존 가능하다",
            "네트워크가 없으면 error 상태로 폴백되므로 오프라인에서도 상태 전이 시연이 동작한다"
        )
        bullets.forEach { bullet ->
            Row(modifier = Modifier.padding(vertical = 3.dp)) {
                Text(text = "• ", fontSize = 13.sp, color = Color(0xFF1976D2))
                Text(text = bullet, fontSize = 12.sp, color = Color(0xFF424242), lineHeight = 17.sp)
            }
        }
    }
}

// 공통 이미지 표시 — Coil 3 의 AsyncImage 는 placeholder/error Painter 인자를 제거했으므로,
// Box 배경색으로 로딩(회색)·에러(붉은색) 상태를 시각화하고 onState 로 상태를 전달한다.
@Composable
private fun DemoImage(
    request: ImageRequest,
    imageLoader: ImageLoader,
    height: Dp,
    onState: (AsyncImagePainter.State) -> Unit
) {
    var isError by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .border(1.dp, Color(0xFFBDBDBD), RoundedCornerShape(8.dp))
            .background(
                if (isError) Color(0xFFFFCDD2) else Color(0xFFE0E0E0),
                RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        // 로딩 중에는 AsyncImage 가 아무것도 그리지 않아 회색 배경이 placeholder 역할을 한다.
        AsyncImage(
            model = request,
            imageLoader = imageLoader,
            contentDescription = "Coil 3 demo image",
            contentScale = ContentScale.Crop,
            onState = { state ->
                isError = state is AsyncImagePainter.State.Error
                onState(state)
            },
            modifier = Modifier.fillMaxSize()
        )
        if (isError) {
            Text(text = "이미지 로드 실패", color = Color(0xFFC62828), fontSize = 13.sp)
        }
    }
}

// dataSource enum 이름을 한국어 설명으로 변환
private fun describeSource(name: String): String = when (name) {
    "MEMORY_CACHE", "MEMORY" -> "메모리 캐시"
    "DISK" -> "디스크 캐시"
    "NETWORK" -> "네트워크"
    else -> name
}

@Composable
private fun StatusBadge(label: String, color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
            .border(1.dp, color, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = color)
    }
}

@Composable
private fun SectionCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
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
