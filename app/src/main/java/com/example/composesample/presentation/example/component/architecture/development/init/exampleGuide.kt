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

/**
 * Init Test Example (ViewModel 데이터 로딩 트리거 3종 비교) 참고 자료
 *
 * - 원문: "How to load ViewModel's data without using 'init'" (Mykhailo Vasylenko, Android Weekly #737)
 *
 * - SharingStarted.WhileSubscribed 공식 문서:
 *   https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/-sharing-started/-companion/-while-subscribed.html
 *
 * - StateFlow / stateIn 공식 문서:
 *   https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/state-in.html
 *
 * - Android 공식 가이드(UI 레이어에서 Flow 수집):
 *   https://developer.android.com/topic/architecture/ui-layer#consume-flows
 *
 * 핵심 개념:
 * - 같은 "화면 진입 시 로딩"도 트리거 위치에 따라 발화 시점/횟수가 달라진다. 셋 다 로딩 동작 자체는 동일하고
 *   차이는 오직 "언제, 몇 번" 발화하는가뿐이다.
 *   1) LaunchedEffect(Unit): 컴포지션 진입 시 UI 가 직접 호출 — 화면이 다시 만들어지면 다시 발화
 *   2) init { }: ViewModel 생성 즉시 1회. 구독 여부와 무관하게 실행되고 재구독해도 재발화하지 않음
 *   3) onStart { } + stateIn(WhileSubscribed): 첫 구독자가 붙는 순간 발화, 구독이 끊기고
 *      타임아웃(이 예제는 5000ms)이 지난 뒤 다시 붙으면 재발화
 * - init { } 을 데이터 로딩 트리거로 쓰지 않는 이유: 화면이 실제로 보이지 않아도 네트워크를 타고,
 *   구독자가 없는 사이의 일회성 이벤트를 놓치며, 실패 시 재시도 훅이 없고,
 *   테스트에서 생성자 호출만으로 부수 효과가 발생해 제어하기 어렵다.
 * - WhileSubscribed 의 타임아웃은 화면 회전·앱 전환처럼 짧게 끊기는 구간을 재요청 없이 흡수하기 위한 값이다.
 *
 * 예제 화면 구성 메모:
 * - isInitLoading 은 stateIn(WhileSubscribed) 로 공유되므로 "구독자 존재 여부"가 동작을 좌우한다.
 *   그래서 이 Flow 는 토글 가능한 자식(SubscriberChild)에서만 구독한다 — 카드 레벨에서 함께 구독하면
 *   구독이 영원히 끊기지 않아 재발화 데모 자체가 성립하지 않는다.
 * - 진입 직후 호출 횟수가 3인 이유: LaunchedEffect 1 + init 1 + onStart 1.
 */
