package com.example.composesample.presentation.example.component.architecture.lifecycle

/**
 * AutoCloseable Example 참고 자료
 *
 * - Kotlin AutoCloseable 문서: https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-auto-closeable/
 * - Compose DisposableEffect: https://developer.android.com/develop/ui/compose/side-effects#disposableeffect
 *
 * 핵심 개념:
 * - AutoCloseable: close() 메서드를 가진 Java/Kotlin 인터페이스 (use {} 블록에서 자동 닫힘)
 * - Closeable: AutoCloseable의 하위 타입 (IOException 허용)
 * - Compose에서 AutoCloseable 리소스는 DisposableEffect로 수명주기에 바인딩
 *
 * Compose 패턴:
 * - remember { resource } + DisposableEffect { onDispose { resource.close() } }
 * - ViewModel onCleared()에서 close() 호출 (ViewModel 생명주기와 연동 시)
 * - 코루틴과 함께 사용 시: use { } 블록 또는 try-finally 권장
 *
 * 주의사항:
 * - recomposition 때 리소스가 재생성되지 않도록 remember 필수
 * - rememberCoroutineScope로 생성한 코루틴은 컴포지션 이탈 시 자동 취소됨
 */
