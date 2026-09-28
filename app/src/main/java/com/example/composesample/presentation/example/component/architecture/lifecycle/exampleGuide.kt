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

/**
 * ComposeLifecycleOwner Example 참고 자료
 *
 * - lifecycle 2.10.0 릴리스 노트: https://developer.android.com/jetpack/androidx/releases/lifecycle#2.10.0
 * - Compose 에서 Lifecycle 다루기: https://developer.android.com/topic/libraries/architecture/compose
 *
 * 핵심 개념:
 * - `rememberLifecycleOwner(maxLifecycle = RESUMED, parent = LocalLifecycleOwner.current)` 가 하위 트리 전용
 *   LifecycleOwner 를 만든다. 반환값을 `CompositionLocalProvider(LocalLifecycleOwner provides owner)` 로 내려야
 *   안쪽의 LifecycleStartEffect/LifecycleResumeEffect/repeatOnLifecycle/collectAsStateWithLifecycle 이 따른다
 * - 실제 상태 = min(부모 상태, maxLifecycle). 상한을 부모보다 높게 줘도 부모를 넘지 않는다
 * - 컴포지션을 떠나면 부모 관찰을 해제하고 ON_DESTROY 를 받아 DESTROYED 로 끝난다
 * - 구현 클래스 `ComposeLifecycleOwner` 는 internal 이라 공개 API 는 이 함수 하나다(2.10.0 aar javap 확인)
 *
 * 예제 구성:
 * - HorizontalPager(beyondViewportPageCount = 1) 의 각 페이지에 settledPage 면 RESUMED, 아니면 STARTED 상한을 건다.
 *   페이지 작업(틱)은 repeatOnLifecycle(RESUMED) 안에서 돌아, 상한 유무에 따라 이웃 페이지 틱이 오르는지로 차이를 센다
 * - 상한 선택(CREATED/STARTED/RESUMED)·자식 표시/숨기기로 자식 owner 이벤트를 로그로 남긴다.
 *   옵저버를 onDispose 에서 떼지 않는 이유: 이탈 시 오는 ON_PAUSE/ON_STOP/ON_DESTROY 까지 받기 위해서
 *
 * 실기기 실측(SM-A725F / Android 13, 틱 200ms · 2초):
 * - 상한 적용 · 페이지 0: P0 10 / P1 0 / P2 0 — 이웃 페이지는 STARTED 라 틱이 멈춘다
 * - 상한 없음 · 페이지 0: P0 10 / P1 10 / P2 10 — 컴포즈된 페이지(보이는 이웃 + 미리 컴포즈) 전부 RESUMED
 * - 상한 적용 · 페이지 1: P0 0 / P1 10 / P2 0
 * - 백그라운드 왕복: 전 페이지 StartEffect ■ → 복귀 시 StartEffect ▶ 는 전부, ResumeEffect ▶ 는 현재 페이지만
 * - 자식 상한 RESUMED→STARTED→CREATED→RESUMED: ON_PAUSE → ON_STOP → ON_START·ON_RESUME,
 *   숨기기: ON_PAUSE → ON_STOP → ON_DESTROY (onDispose 에서 옵저버를 떼지 않아야 받는다)
 * - LazyColumn 아이템이 스크롤로 컴포지션을 떠나도 같은 DESTROYED 경로를 탄다
 *
 * 주의사항:
 * - 상한 변경은 LaunchedEffect 로 반영되므로 같은 프레임이 아니라 다음 프레임에 적용된다
 * - 상한으로 INITIALIZED/DESTROYED 를 주는 경우는 다루지 않는다(예제의 선택지에서 제외)
 */
