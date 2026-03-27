package com.example.composesample.presentation.example.component.architecture.development.init

/**
 * Startup Optimization 참고 자료
 *
 * - 원문: "How I Found a 34% Startup Win in a Modern Compose App" (Android Weekly #719)
 *   https://programminghard.dev/how-i-found-a-34-startup-win-in-a-modern-compose-app/
 *
 * - App Startup 공식 문서:
 *   https://developer.android.com/topic/libraries/app-startup
 *
 * - Baseline Profile 공식 문서:
 *   https://developer.android.com/topic/performance/baselineprofiles/overview
 *
 * - Koin Lazy Injection:
 *   https://insert-koin.io/docs/reference/koin-android/get-instances
 *
 * 핵심 개념:
 * - App Startup: ContentProvider 남용 대신 Initializer 체인으로 초기화 순서 명시적 관리
 * - Baseline Profile: AOT 컴파일로 JIT 워밍업 시간 제거 → 첫 실행 속도 향상
 * - Koin 지연 초기화: by inject()로 실제 사용 시점까지 의존성 주입 지연
 */
