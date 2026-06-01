package com.example.composesample.presentation.example.component.ui.media.lottie

/**
 * UI/Media/Lottie 예제 참고 자료
 *
 * ## LottieExampleUI (Lottie 애니메이션 구현 & 제어)
 * - Lottie Compose: https://airbnb.io/lottie/#/android-compose
 * 핵심 개념:
 * - rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.xxx)) 로 애니메이션 로드
 * - animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever) 로 재생 제어
 * - LottieAnimation(composition, progress = { progress }) 로 렌더링
 * - speed / clipSpec / isPlaying 으로 속도·구간·재생 상태 제어
 */
