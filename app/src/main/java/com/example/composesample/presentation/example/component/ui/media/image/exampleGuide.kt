package com.example.composesample.presentation.example.component.ui.media.image

/**
 * UI/Media/Image 예제 참고 자료
 *
 * ## 이미지 로딩 (Coil Compose)
 * - 공식 문서: https://developer.android.com/develop/ui/compose/graphics/images/loading
 * - Coil: https://coil-kt.github.io/coil/compose/
 * 핵심 개념:
 * - AsyncImage(model = url, contentDescription = ...) 로 네트워크/리소스 이미지 비동기 로딩
 * - placeholder / error / fallback 으로 로딩·실패 상태 처리
 * - contentScale(Crop/Fit/FillBounds) 로 종횡비 처리
 * - GIF는 coil-gif 의 ImageLoader(components { add(GifDecoder.Factory()) }) 구성 필요
 *
 * ## Coil 3 (Coil3ImageExample)
 * - Coil 3: https://coil-kt.github.io/coil/
 * - 네트워크 이미지: coil-network-okhttp / coil-network-ktor 중 택1 (classpath 에 있으면 자동 등록)
 * - 출처: Warm Tyres — Image Loading with Coil 3 (Android Weekly #732, CMP→Android 각색)
 * 핵심 개념:
 * - 패키지 네임스페이스가 coil3.* 로 분리되어 기존 Coil 2(coil.*)와 한 프로젝트에서 공존 가능
 * - Coil 3 의 AsyncImage 는 placeholder/error Painter 인자를 제거 — Box 배경 또는 SubcomposeAsyncImage content 로 직접 구성
 * - onState 콜백으로 Loading/Success/Error 추적, SuccessResult.dataSource 로 출처(NETWORK/MEMORY_CACHE/DISK) 확인
 * - ImageRequest.memoryCachePolicy/diskCachePolicy 로 요청 단위 캐시 제어
 * - ImageLoader.Builder 의 memoryCache(MemoryCache.maxSizePercent) / diskCache(DiskCache.directory(okioPath)) 로 캐시 정책 구성
 */
