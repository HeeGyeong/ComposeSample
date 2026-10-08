package com.example.composesample.presentation.example.component.architecture.pattern.retain

/**
 * Retain API Example 참고 자료
 *
 * - 상태 수명(State lifespans) 공식 가이드: https://developer.android.com/develop/ui/compose/state-lifespans
 * - API 문서(androidx.compose.runtime.retain): https://developer.android.com/reference/kotlin/androidx/compose/runtime/retain/package-summary
 * - Compose runtime 릴리스 노트: https://developer.android.com/jetpack/androidx/releases/compose-runtime
 *
 * Retain API란:
 * - Compose 1.10에서 도입된 ViewModel 없는 인스턴스 보존 메커니즘 (`retain { ... }`, runtime-retain 아티팩트 1.10.0-alpha04~)
 * - 구성 변경(Config Change)으로 액티비티가 재생성돼도 같은 인스턴스를 돌려준다 (remember와의 차이)
 * - 직렬화하지 않으므로 프로세스 종료 후에는 남지 않는다 — 그 경우는 rememberSaveable / SavedStateHandle
 * - Android 기본 저장소(ui-android 의 LifecycleRetainedValuesStoreOwner)가 ViewModel 을 상속한다
 *   → ViewModel 과 같은 범위: 구성 변경엔 살아남고 ViewModelStore 가 정리될 때 해제 (ui-android 1.12.1 javap 확인)
 *
 * retain vs remember vs rememberSaveable:
 * - remember           : 리컴포지션 생존, 액티비티 재생성 시 소멸
 * - rememberSaveable   : 재생성·프로세스 종료 후 복원까지 생존, 직렬화 가능한 값만 저장 (Bundle 한계)
 * - retain             : 재생성 생존(프로세스 종료 후는 X), 직렬화 불필요, 복잡한 객체/스트림 보존 가능
 *
 * 적합한 사용 사례:
 * - 네트워크 연결 객체 (WebSocket, SSE 스트림 등)
 * - 초기화 비용이 높은 객체 (이미지 로더, 암호화 컨텍스트 등)
 * - 직렬화 불가능한 복잡한 상태
 *
 * 주의사항:
 * - 정리 콜백은 RetainObserver(onRetained / onEnteredComposition / onExitedComposition / onRetired / onUnused)로 받는다.
 *   Closeable 을 구현해도 자동으로 close() 해 주지 않는다 (runtime-retain 1.12.1 클래스에 Closeable 참조 없음)
 * - 과도한 사용 시 메모리 누수 가능 → 명시적 해제 전략 필요
 * - 이 예제는 retain 개념을 일반 클래스(Presenter)로 시뮬레이션한다. 프로젝트는 Compose 1.12.1(BOM 2026.09.00)이라
 *   실제 runtime-retain 1.12.1 도 이미 compile classpath 에 있다 (2026-10-08 확인)
 * - 이 앱의 예제 화면(BlogExampleActivity)은 configChanges 로 회전을 직접 처리해 회전으로는 재생성되지 않는다
 *   — 재생성 확인은 다크 모드·언어 전환으로
 */
