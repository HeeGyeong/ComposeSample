package com.example.composesample.presentation.example.component.ui.layout.lazycolumn

/**
 * UI/Layout/LazyColumn 예제 참고 자료
 *
 * ## LazyColumn 성능/이슈 (LazyColumnFlingBehaviorExampleUI, LazyColumnIssueExampleUI)
 * - 공식 문서: https://developer.android.com/develop/ui/compose/lists
 * - 성능: https://developer.android.com/develop/ui/compose/lists#item-keys
 * 핵심 개념:
 * - items(list, key = { it.id }): 안정적 key로 아이템 이동/삭제 시 재사용·애니메이션 보장
 * - contentType 지정으로 이종 아이템 재사용 효율 향상
 * - FlingBehavior 커스터마이징으로 스크롤 감속/스냅 동작 변경
 * - targetSDK 35 edge-to-edge 대응 시 contentPadding/WindowInsets 처리
 *
 * ## LazyStaggeredGridExampleUI (폭포수 그리드)
 * - 공식 문서: https://developer.android.com/develop/ui/compose/lists#lazy-staggered-grid
 * 핵심 개념:
 * - LazyVerticalStaggeredGrid + StaggeredGridCells.Fixed/Adaptive 로 동적 높이 폭포수 배치
 * - 필터링 시 key 기반 itemPlacement 애니메이션
 *
 * ## ReverseLazyColumnExampleUI (역방향 리스트)
 * 핵심 개념:
 * - reverseLayout = true 로 채팅처럼 하단부터 쌓이는 리스트 구현
 *
 * ## LazyListReusePoolExampleUI (contentType 재사용 풀 함정)
 * - 출처(아티클): https://touchlab.co/the-one-liner-that-was-eating-our-memory
 * - contentType 공식 문서: https://developer.android.com/develop/ui/compose/lists#content-type
 * - 아이템 key: https://developer.android.com/develop/ui/compose/lists#item-keys
 * 핵심 개념:
 * - 재사용 풀 정리 규칙은 contentType 별로 7개까지 유지이고, 전체 슬롯 수에는 상한이 없다
 *   (androidx.compose.foundation.lazy.layout.LazyLayoutItemReusePolicy.getSlotsToRetain, 1.11.1 기준 내부 구현)
 * - contentType 에 아이템 고유값을 넘기면 버킷이 아이템 수만큼 생겨 정리 조건에 도달하지 못한다
 * - 남은 슬롯은 remember 값과 modifier 람다가 캡처한 객체까지 도달 가능한 상태로 붙들고 있다
 * - key(식별자, 아이템마다 달라야 함) vs contentType(분류, 레이아웃 종류만큼만) 의 역할 구분
 *
 * ## LazyListCacheWindowExampleUI (캐시 윈도우 + 노출 추적, Compose 1.12)
 * - 출처(블로그): https://android-developers.googleblog.com/2026/09/jetpack-compose-ai-native-ui-instagram-direct.html
 * - foundation 릴리스 노트: https://developer.android.com/jetpack/androidx/releases/compose-foundation
 * - ui 릴리스 노트: https://developer.android.com/jetpack/androidx/releases/compose-ui
 * - LazyLayoutCacheWindow: https://developer.android.com/reference/kotlin/androidx/compose/foundation/lazy/layout/LazyLayoutCacheWindow
 * - 소스(1.12.1 sources jar): https://dl.google.com/dl/android/maven2/androidx/compose/foundation/foundation-android/1.12.1/foundation-android-1.12.1-sources.jar
 * 핵심 개념(1.12.1 소스·바이트코드 확인):
 * - LazyLayoutCacheWindow(@ExperimentalFoundationApi)는 인터페이스다 — Density.calculateAheadWindow(viewport) /
 *   calculateBehindWindow(viewport) 를 직접 구현할 수도 있고, 팩토리는 Dp(ahead, behind) 와 비율(aheadFraction,
 *   behindFraction) 두 가지. ahead = 스크롤 방향 앞쪽을 미리 컴포즈(prefetch), behind = 지나간 쪽을 버리지 않고 유지.
 * - 1.12.1 에서는 rememberLazyListState(cacheWindow) / rememberLazyGridState(cacheWindow) 로 건다.
 *   LazyColumn(cacheWindow = …) 파라미터는 1.13.0-alpha03 신규라 없다. rememberSaveable 이 윈도우를 키로 쓴다.
 * - 미리 컴포즈는 prefetch 단계에서 compose + apply 까지 끝난다(LazyLayoutPrefetchState.performApply) →
 *   LaunchedEffect·DisposableEffect 가 화면에 나오기 전에 실행된다 → 이것으로 노출을 세면 가짜 노출.
 * - Modifier.onVisibilityChanged(minDurationMs = 0, minFractionVisible = 1f, viewportBounds = null, callback):
 *   보임 판정은 fraction > min 또는 == 1f. 보임은 minDurationMs 만큼 기다렸다 알리고 안 보임은 즉시.
 *   배치된 뒤에만 위치 감시가 시작되므로 미리 컴포즈된 아이템에는 오지 않고, behind 로 유지되다 배치에서 빠지면
 *   onUnplaced 로 false 가 온다. viewportBounds 를 안 주면 앱 윈도우 기준(리스트 경계에 잘린 아이템도 '전부 보임'),
 *   주면 fractionVisibleIn(viewport) 로 그 경계만 본다(윈도우와 겹치지 않는다 — 페이지 스크롤로 리스트가 화면 밖이어도 보임).
 * - Modifier.onFirstVisible 은 ui 1.11.0 부터 deprecated(1.10.0 까지 경고 없음). 노드가 다시 부착될 때마다
 *   위치 감시를 다시 등록해서, 내려놓았다가 돌아온 아이템에 또 온다. 공식 대안 = onVisibilityChanged 의 true 를
 *   앱이 이미 본 key 로 거르기.
 * - ComposeFoundationFlags(@ExperimentalFoundationApi).isPausableCompositionInPrefetchEnabled 기본 true.
 *   문서: 라이브러리 로드 후 변경은 정의되지 않은 동작 → Application.onCreate 에서. prefetch 실행 때마다 읽는다.
 * - Pausable 중단 지점: 새로 삽입(또는 재사용)되는 restartable 컴포저블 호출의 shouldExecute. prefetch 의
 *   중단 조건은 남은 시간 ≤ 평균(resume + pause). Android 스케줄러는 View 가 최근 2프레임 동안 그리지 않았으면
 *   예산을 Long.MAX_VALUE 로 준다 → 정지 화면에서는 나뉘지 않는다. 시작 조건도 남은 시간 > 같은 contentType 의
 *   평균 소요 시간이라, 정지 상태에서 통째로 컴포즈하며 평균을 크게 배우면 스크롤 중에는 시작하지 못하고
 *   측정 패스에서 한 번에 컴포즈된다.
 * - 실측(SM-A725F / Android 13, 아이템 56dp · 리스트 300dp, debug):
 *   · 정지 상태 컴포즈 / 화면 밖: 기본 6 / 0 · Dp 150/100 9 / ahead 3 · 비율 1/1 11 / ahead 5.
 *   · 한 화면 아래: 기본 7(ahead 1, behind 0) · Dp 11(3, 2) · 비율 17(6, 5).
 *   · LaunchedEffect 로그 중 전부 보인 적 없는 아이템(초기): 기본 1 · Dp 4 · 비율 6.
 *   · 리스트 기준 보임 5개(#0–#4) vs 윈도우 기준 6개(20dp 만 걸친 #5 포함). 페이지 900px 스크롤 뒤 윈도우 기준은
 *     #1–#5, 리스트 기준은 #0–#4 그대로.
 *   · onFirstVisible 중복(한 화면 ▼▲ / 왕복 ▼▼▲▲ 누적): 기본 5 / 19 · Dp 3 / 13 · 비율 0 / 5. 본 key 집합 방식은 항상 0.
 *   · 페이지를 스크롤하면 20dp 만 걸친 #5 에도 onFirstVisible 이 왔다(같은 viewport 의 onVisibilityChanged 는 안 옴, 원인 미확정).
 *   · 4초 스크롤 중 무거운 아이템(자식 12 × 1.5ms): release prefetch 8개 중 6개 2조각(가장 긴 조각 15–18ms,
 *     안 나뉜 것 약 20.5ms) · debug(HotSwan 제외) 켜짐 5개 중 3개 2조각 · Application.onCreate 에서 끈 빌드 4개 전부 1조각.
 *     같은 리스트에 캐시 윈도우(ahead 한 화면)를 걸면 스크롤 중 아이템이 전부 측정 패스로 컴포즈됐다.
 * - ⚠️ HotSwan 의 디스패치 재작성 아래에서는 prefetch 가 멈추지 않는다(첫 아이템도 50ms 한 덩어리) →
 *   이 파일은 app/build.gradle 의 hotSwanCompiler { exclude } 대상(ComposeHotReloadGuide.md). 계측 테스트에서는
 *   제외해도 나뉘지 않으므로 실제 앱에서 잰다.
 */

/**
 * LazyColumnIssueExampleUI 참고 자료
 * - 설명 글: https://heegs.tistory.com/142
 *   (URL 은 프로젝트 규칙상 exampleGuide.kt 에만 둔다 — UI 파일 KDoc 에 있던 것을 옮겼다)
 */
