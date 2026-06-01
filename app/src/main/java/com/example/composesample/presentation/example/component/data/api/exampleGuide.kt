package com.example.composesample.presentation.example.component.data.api

/**
 * Data/API 예제 참고 자료
 *
 * ## ApiExampleViewModel / ApiExampleUseCaseViewModel (직접 호출 vs UseCase 대조쌍)
 * - 공식 문서: https://developer.android.com/topic/architecture/domain-layer
 * 핵심 개념:
 * - ApiExampleViewModel: ViewModel이 API 인터페이스를 직접 호출하는 형태(교육용 안티/대조 패턴)
 * - ApiExampleUseCaseViewModel: domain UseCase를 경유하는 클린 아키텍처 형태
 * - 두 예제는 "도메인 레이어 유무" 차이를 비교하기 위한 의도적 대조쌍 (ARCHITECTURE.md 참고)
 * - domain Repository는 suspend fun이 도메인 타입을 반환 (retrofit2.Call 미노출, ARCH-01/02)
 *
 * ## KtorExampleUI (Ktor Client)
 * - 공식 문서: https://ktor.io/docs/client-create-and-configure.html
 * 핵심 개념:
 * - HttpClient + ContentNegotiation(Gson) + Logging + DefaultRequest + timeout 구성
 * - Retrofit과의 비교: suspend 기반 동일하나 엔진/플러그인 구성 방식이 다름
 * - 고급 구성(Auth 토큰 리프레시, HttpRequestRetry 지수 백오프)은 추후 보강 예정
 *
 * ## ApiDisconnectExampleUI (연결 해제 처리)
 * - 공식 문서: https://developer.android.com/training/monitoring-device-state/connectivity-status-type
 * 핵심 개념:
 * - 네트워크 상태 감지 + 요청 중 연결 해제 시나리오 처리
 * - 코루틴 취소/예외 전파를 통한 안전한 정리
 */
