package com.example.composesample.presentation.example.component.architecture.pattern.retain

/**
 * Retain API Example 참고 자료
 *
 * - 공식 발표: https://android-developers.googleblog.com/2025/05/compose-1-10-retain-api.html
 * - API 문서: https://developer.android.com/reference/kotlin/androidx/compose/runtime/package-summary#rememberRetained
 *
 * Retain API란:
 * - Compose 1.10에서 도입된 ViewModel 없는 상태 보존 메커니즘
 * - 화면 회전 / 프로세스 재시작 후에도 인스턴스를 유지 (remember와의 차이)
 * - ViewModel 생명주기 범위 내에서 인스턴스를 캐싱
 *
 * rememberRetained vs remember vs rememberSaveable:
 * - remember           : 리컴포지션 생존, 화면 회전 시 소멸
 * - rememberSaveable   : 화면 회전 생존, 직렬화 가능한 값만 저장 (Bundle 한계)
 * - rememberRetained   : 화면 회전 생존, 직렬화 불필요, 복잡한 객체/스트림 보존 가능
 *
 * 적합한 사용 사례:
 * - 네트워크 연결 객체 (WebSocket, SSE 스트림 등)
 * - 초기화 비용이 높은 객체 (이미지 로더, 암호화 컨텍스트 등)
 * - 직렬화 불가능한 복잡한 상태
 *
 * 주의사항:
 * - 내부적으로 ViewModelStore에 바인딩 → Activity/Fragment 파괴 시 해제
 * - Closeable을 구현하면 생명주기 종료 시 자동 close() 호출
 * - 과도한 사용 시 메모리 누수 가능 → 명시적 해제 전략 필요
 */
