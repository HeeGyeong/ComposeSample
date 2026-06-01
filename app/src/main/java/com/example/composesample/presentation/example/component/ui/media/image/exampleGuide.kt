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
 */
