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
 *
 * ## 대용량 이미지 타일 딥줌 (TiledImageDeepZoomExample)
 * - BitmapRegionDecoder: https://developer.android.com/reference/android/graphics/BitmapRegionDecoder
 * - 큰 비트맵 효율적으로 로드(inSampleSize): https://developer.android.com/topic/performance/graphics/load-bitmap
 * - RenderNode(녹화 캔버스로 그리기 가능 여부 확인): https://developer.android.com/reference/android/graphics/RenderNode
 * - 같은 기법의 라이브러리: Subsampling Scale Image View — https://github.com/davemorrissey/subsampling-scale-image-view
 * - 출처: gander README "deep zoom into huge photos (tiled decoding)"(AW #747, 앱 소개에서 API 소재만 분리) — https://github.com/mokshablr/gander
 * 핵심 개념:
 * - BitmapRegionDecoder.decodeRegion(rect, options): 파일에서 사각형 영역만 디코딩. API 31+ newInstance(path), 미만 newInstance(path, false)
 * - inSampleSize 는 2의 거듭제곱 — 1/배율을 넘지 않는 가장 큰 값(공식 calculateInSampleSize 와 같은 규칙)이면 화면보다 성기게 디코딩하지 않는다.
 *   8000×6000 을 1080px 폭에 맞추면 배율 0.135 → 4(2000×1500), 이 예제 뷰어(922px, SM-A725F)는 0.115 → 8(1000×750), 1:1 이상은 1
 * - 타일 크기는 디코딩 픽셀 기준 512px 고정 → 배율과 무관하게 한 장 1MB 이하, 원본에서 덮는 넓이는 512 × sample.
 *   1080×945 화면이면 1:1 은 9장(9MB), 전체 보기는 sample 4 의 12장(단위 테스트 TiledImageMathTest)
 * - 바닥에는 전체 보기보다 한 단계 성긴 개요(이 뷰어는 sample 16, 500×375, 0.7MB)를 늘 깔아 타일이 오기 전의 빈칸을 없앤다
 * - 디코딩은 전용 스레드 하나 — 지나간 배율·위치의 대기 요청은 시작 전에 취소(안 하면 지금 화면이 그만큼 늦게 찬다)
 * - 캐시는 바이트 기준 LruCache(sizeOf = allocationByteCount). 밀려난 비트맵은 그리는 중일 수 있어 recycle 하지 않고 GC 에 맡긴다
 * - 타일은 실수 좌표(RectF)로 그린다 — 정수 반올림은 타일 사이 1px 틈을 만든다
 * - 대조군: decodeFile 로 한 번에 → ARGB_8888 183MB(390ms). Android 8+ 비트맵 픽셀은 네이티브라 Java 힙 한도와 무관하게 디코딩되지만,
 *   하드웨어 가속 녹화 캔버스는 그리기를 거부한다 — RenderNode 실측 "Canvas: trying to draw too large(192000000bytes) bitmap."
 * - 디코딩 시간은 파일 안 위치에 달렸다 — 기본 JPEG 는 위에서 아래로 이어진 압축 데이터라 아래쪽 영역은 그 위를 먼저 읽는다.
 *   1:1 타일 평균(SM-A725F release AOT): 위 0~3행 70ms · 가운데 4~7행 224ms · 아래 8~11행 396ms ≈ 통째 디코딩 390ms.
 *   타일 방식이 아끼는 것은 시간이 아니라 메모리(debug 도 84·248·396ms 로 같은 경향 — 디코딩은 네이티브라 HotSwan 영향 없음)
 * - 원본은 APK 에셋 대신 처음 한 번 RGB_565 비트맵(약 92MB)에 그려 캐시 폴더에 JPEG 로 저장 — 예제용 준비 단계
 */
