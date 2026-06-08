package com.example.composesample.presentation.example.component.architecture.development.strictmode

/**
 * StrictMode 위반 감지 예제 참고 자료
 *
 * ## StrictMode (정책 위반 감지)
 * - 공식 문서: https://developer.android.com/reference/android/os/StrictMode
 * - 출처: Strictly (GitHub, debug-only StrictMode 업그레이드, Android Weekly #728)
 * 핵심 개념:
 * - StrictMode: 디버그 빌드에서 메인 스레드 I/O·리소스 누수 등 "느린/잘못된" 작업을 감지하는 개발 도구. 릴리스 빌드에서는 설정하지 않는다.
 * - ThreadPolicy(스레드 단위): detectDiskReads() / detectDiskWrites() / detectNetwork() / detectCustomSlowCalls() — 메인 스레드를 막는 I/O 를 감지
 * - VmPolicy(프로세스 단위): detectLeakedClosableObjects() / detectActivityLeaks() / detectLeakedSqlLiteObjects() / detectLeakedRegistrationObjects() — 닫지 않은 리소스/누수 감지
 * - penaltyListener(executor, listener) : API 28(P)+ 에서 위반을 Violation 콜백으로 전달 → 로깅/리포팅 가능. 본 예제는 이 콜백으로 위반을 화면에 수집
 * - penaltyLog() : 모든 버전에서 사용 가능하나 logcat 에만 기록. penaltyDeath() 는 위반 시 앱 강제 종료(회귀 차단용)
 * - 설정 위치: Application.onCreate() 에서 if (BuildConfig.DEBUG) { StrictMode.setThreadPolicy(...) / setVmPolicy(...) }
 * - SDK 분기 주의: penaltyListener·일부 detectXxx API 는 버전별 가용성이 다름 → Build.VERSION.SDK_INT 분기 필요
 * - Violation 객체(android.os.strictmode.Violation, API 28+): getStackTrace() 로 위반 발생 위치 추적 가능. 서브클래스명(DiskWriteViolation/LeakedClosableViolation 등)으로 종류 식별
 * - 누수 감지 타이밍: detectLeakedClosableObjects 위반은 GC/파이널라이즈 시점에 보고되므로 즉시성이 없음 → 데모에서는 gc()+runFinalization() 반복 유도
 */
