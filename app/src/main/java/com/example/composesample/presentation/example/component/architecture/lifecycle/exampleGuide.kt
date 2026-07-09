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
 *
 * ## AutoCloseableExampleUI 참고 자료 (AutoCloseableGuide.kt에서 이관)
 * - 출처: https://www.paleblueapps.com/rockandnull/automatic-resource-cleanup-android-viewmodel-autocloseable/
 *
 * 핵심 개념:
 * - Service 인터페이스에 AutoCloseable을 상속시키고, ViewModel 생성자에 vararg로 전달하면
 *   프레임워크가 ViewModel 소멸 시 전달 순서의 역순으로 close()를 자동 호출
 * - onCleared() 오버라이드와 서비스별 cleanup() 호출 보일러플레이트를 제거 (BaseViewModel 상속 대신 조합 사용)
 * - close()는 멱등성을 보장해야 하며, 한 서비스의 close() 실패가 다른 서비스 정리에 영향을 주지 않아야 함
 */
