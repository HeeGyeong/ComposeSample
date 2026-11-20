package com.example.composesample.presentation.example.component.ui.visibility

/**
 * Visibility 예제 참고 자료
 *
 * ## VisibilityExampleUI (View.INVISIBLE → Modifier 기반 가시성)
 * - 출처: https://proandroiddev.com/from-view-invisible-to-modifier-visible-rethinking-visibility-%EF%B8%8Fin-jetpack-compose-7957650e4d70
 * 핵심 개념:
 * - View 시스템의 VISIBLE/INVISIBLE/GONE 3단계 개념을 Compose는 별도 속성 없이 패턴으로 대응: GONE → 조건부 컴포지션(if), INVISIBLE → Modifier.alpha(0f), 애니메이션 포함 전환 → AnimatedVisibility
 * - 조건부 컴포지션(if)은 컴포지션 트리에서 완전히 제거되어 가장 효율적이지만 remember 상태가 초기화됨. Modifier.alpha(0f)는 공간과 상태를 유지하지만 클릭 이벤트 차단을 위해 clickable(enabled = false)를 함께 지정해야 함
 * - AnimatedVisibility(visible = ...)는 enter/exit 애니메이션(fadeIn/expandVertically 등)과 함께 표시·숨김 전환 지원
 * - 선택 기준: 공간 유지 필요 → alpha, 애니메이션 필요 → AnimatedVisibility, 효율성 우선 → if(조건부 컴포지션)
 *   단, 무한 애니메이션(indeterminate 스피너 등)을 alpha 로 숨기면 창이 쉬지 않고 다시 그린다 → 아래 HiddenSpinnerFrameCard
 *   (가리거나 창 밖 · 크기 0 에 둬도 같고, Lazy 화면 밖 칸은 컴포지션에 남은 동안 시계만 돈다 → 2번 묶음)
 *
 * ## HiddenSpinnerFrameCard (#177 — 숨긴 무한 애니메이션이 만드는 프레임)
 * - 원문: Jaewoong Eum, "Recalculated, not recomposed: what does one tap cost in a Compose paywall?" 의 'Frames nobody asked for' 절
 *   (RevenueCat, Android Weekly #753) https://www.revenuecat.com/blog/engineering/android-paywall-improve
 *   · 수정 PR: https://github.com/RevenueCat/purchases-android/pull/4442 — `if (animatedProgressAlpha > 0f)` 로 감싸 페이드아웃 동안만 컴포지션에 남긴다
 *   · 처음 보고: https://github.com/RevenueCat/purchases-flutter/issues/1954 — Android 10 폰에서 5초에 307 프레임(dumpsys gfxinfo)
 * - 공식 문서: FrameMetrics https://developer.android.com/reference/android/view/FrameMetrics
 *   · Looper.setMessageLogging https://developer.android.com/reference/android/os/Looper#setMessageLogging(android.util.Printer)
 *   · GPU 렌더링 속도 · gfxinfo https://developer.android.com/topic/performance/rendering/inspect-gpu-rendering
 * - 소스(androidx-main): https://github.com/androidx/androidx/blob/androidx-main/compose/material3/material3/src/commonMain/kotlin/androidx/compose/material3/ProgressIndicator.kt
 *   · https://github.com/androidx/androidx/blob/androidx-main/compose/animation/animation-core/src/commonMain/kotlin/androidx/compose/animation/core/InfiniteTransition.kt
 *   · https://github.com/androidx/androidx/blob/androidx-main/compose/ui/ui/src/androidMain/kotlin/androidx/compose/ui/platform/AndroidUiDispatcher.android.kt
 *   · https://github.com/androidx/androidx/blob/androidx-main/compose/ui/ui/src/commonMain/kotlin/androidx/compose/ui/node/InnerNodeCoordinator.kt
 *   — 아래 규칙은 프로젝트가 쓰는 material3 1.4.0 · animation-core 1.12.1 · ui 1.12.1 의 sources jar 로 확인했다
 * - 규칙:
 *   · indeterminate CircularProgressIndicator = rememberInfiniteTransition + animateFloat 3개, 값은 Canvas 람다(그리기 단계)에서 읽는다
 *     → 그려지는 동안은 값이 바뀔 때마다 그리기 무효화 → 창이 새 프레임(리컴포지션은 없다)
 *   · InfiniteTransition.run: isRunning 이면 LaunchedEffect 안에서 while (true) withInfiniteAnimationFrameNanos — 값을 읽는 곳과 무관하게
 *     컴포지션에 있는 한 매 프레임 다음 프레임을 요청한다. durationScale == 0(개발자 옵션 '애니메이터 길이 배율' 0)이면 끝 값으로 건너뛰고
 *     배율이 0 이 아닐 때까지 멈춘다 → 측정 전 배율 확인
 *   · Modifier.alpha(a) = if (a != 1f) graphicsLayer(alpha = a, clip = true) else this — ui · ui-graphics 에 알파 0 기록 생략 분기가 없다
 *   · 노드는 가장 안쪽 좌표계(InnerNodeCoordinator)가 배치될 때 onNodePlaced(= isPlaced) 가 되고, 부모는 isPlaced 인 자식만 그린다
 *     → 같은 노드의 Modifier.layout 에서 place 를 빼도(흔한 Modifier.visible() 모양) 그리기는 0. 처음엔 NodeCoordinator.draw 에
 *       isPlaced 검사가 없어 '그려진다' 고 읽었다가 프로브(빨간 칸이 비어 있음 · 그린 프레임 변화 없음)로 바로잡았다
 *   · AndroidUiDispatcher.dispatch 는 handler.post 와 함께 choreographer.postFrameCallback 을 건다 — Compose 메인 디스패처에서
 *     코루틴이 한 번 이어지면 Choreographer 프레임 하나가 따라온다
 * - 측정 설계(HiddenSpinnerFrameLab):
 *   · 프레임 콜백 = 메인 Looper 메시지 로그(Printer)의 Choreographer$FrameDisplayEventReceiver 디스패치 수
 *   · 그린 프레임 = Window.OnFrameMetricsAvailableListener(별도 HandlerThread), API 26+ 는 INTENDED_VSYNC_TIMESTAMP 가 창 안인 것만
 *   · 스피너 그리기 = 스피너 노드 안쪽 drawWithContent 의 평범한 Int 필드(상태로 두면 그 쓰기가 프레임을 부른다)
 *   · 창의 시작 · 끝은 메인 Handler 메시지 — 코루틴 delay 로 열면 재개가 프레임 콜백 하나를 만든다(위 디스패처 규칙)
 *   · 안정 대기 1.5초(버튼 물결 · 칩 · 퇴장 애니메이션) → 5초 창 → 늦은 FrameMetrics 보고 0.3초. 세는 동안 화면 글자는 바뀌지 않는다
 *   · 판정은 범주: '계속' = 창 안 VSync 수의 1/4 이상, '0' = 정확히 0. swiftshader 에뮬 debug 는 VSync 의 절반쯤만 그려서 80% 기준은 틀린다
 *   · 측정 창 안 터치는 LazyColumn 의 Initial 패스 포인터 관찰(소비 안 함)로 표시 — 스크롤 · 물결이 프레임을 만든다
 * - 실측(헤드리스 에뮬레이터 · swiftshader, 60 Hz, 실기기는 쓰지 않았다 — 프레임 콜백 / 그린 프레임 / 스피너 그리기, 5초 창):
 *   · Pixel 4 API 30 release(디버그 키 서명) 2회: 보임 290/290/290 · 301/301/301, alpha(0f) 293/293/293 · 292/292/292,
 *     배치 안 함 300/0/0 두 번, if · AnimatedVisibility 0/0/0 — 원문의 '5초에 302~303 → 0' 과 같은 모양
 *   · Pixel 4 API 30 debug 2회: 보임 245 · 283, alpha(0f) 226 · 222(세 수 같음), 배치 안 함 301/0/0 · 296/0/0, 나머지 0/0/0
 *   · Pixel Tablet API 35 debug 2회: 보임 176 · 173, alpha(0f) 178/177/178 · 170/170/170, 배치 안 함 296/0/0 · 299/0/0, 나머지 0/0/0
 *     — debug · 큰 화면은 VSync 마다 못 그려도(약 34~57 fps) 쉬지 않는다. 배치 안 함의 프레임 콜백만은 늘 약 300(그릴 일이 없어 VSync 를 놓치지 않는다)
 *   · dumpsys gfxinfo 대조(앱 창 안쪽 약 4.4초, 결과 표를 고치는 프레임 전에 읽음): API 30 release 보임 239(54/초) ↔ 앱 269/5초(54/초),
 *     alpha(0f) 264 ↔ 295, API 30 debug 228 ↔ 260 · 220 ↔ 245, API 35 debug 136 ↔ 152 · 152 ↔ 169, 숨김 세 줄은 세 기기 모두 gfxinfo 도 0
 *     — FrameMetrics 와 gfxinfo 는 같은 프레임을 센다(초당 수가 1 안쪽으로 같다).
 *     처음엔 gfxinfo 를 5초 뒤에 읽어 0 줄이 1 로 나왔다 — 창이 끝나고 0.3초 뒤 결과 표를 고치는 프레임까지 들어갔다
 *   · 검색창 포커스를 남긴 채 연 경우(API 30 · API 35 debug 같음): 배치 안 함 300/10/0, if · AnimatedVisibility 20/10/0 — 스피너 그리기는 0 이라
 *     스피너가 아닌 다른 것(아래 함정의 커서)이 그린다
 * - 함정:
 *   · 이 앱의 실물 사례 — BlogExampleActivity 는 예제 화면 아래에 목록을 계속 컴포즈한다. 검색으로 예제를 열면 검색창이 포커스를 쥔 채
 *     커서가 0.5초마다 깜빡여, 예제 화면에 가려진 채 5초에 그린 프레임 10 · 프레임 콜백 20 — 이 카드의 if 줄이 0 이 아니게 나온 원인.
 *     PERF-HIDDEN-CURSOR-01(`657c5151`)에서 예제를 열 때 포커스를 지우게 고쳤다(검색 경로로 다시 재면 숨김 줄 0 / 0 / 0)
 *   · 같은 앱의 '시계만 도는' 사례 — Auto Skeleton 예제의 SkeletonContainer 가 로딩이 끝난 뒤에도 무한 트랜지션을 컴포즈해
 *     그린 프레임 0 인데 doFrame 이 5초에 약 296(atrace). PERF-SKELETON-CLOCK-01 에서 shimmer 가 보이는 동안만 컴포즈하게 고쳤다(0 / 0)
 *   · 프레임 콜백 집계는 atrace(gfx view)의 Choreographer#doFrame 과 같다 — 배치 안 함 300 · if 0 · 보임 246(API 35 debug, 앱 코드 변경 없이 교차 확인)
 *   · 측정 스크립트: 검색 뒤 TAB 으로 포커스를 옮기면 예제 위에 키보드가 다시 떴다 → 다크 모드를 켰다 원래대로(액티비티 재생성 2회,
 *     열린 예제는 rememberSaveable 로 남고 포커스는 복원되지 않는다)로 포커스를 지우고 쟀다. 설정은 원래 값(no)으로 되돌렸다
 *   · 측정 스크립트: 한 번에 1,400px 넘게 끌면 LazyColumn 이 플링으로 카드를 지나친다 → 600px 이하 · 1.5초 드래그로 맞춘다
 *
 * ## HiddenSpinnerFrameCard 2번 묶음 (#183 — 안 보이는 곳에 둔 스피너)
 * - 출처: 외부 글이 아니라 #177 실측의 남은 질문 — 예제 화면에 완전히 덮인 목록의 검색창 커서가 5초에 그린 프레임 10 을 만들었다
 *   (가림 컬링이 없다). 덮개 · 창 밖 · 크기 0 · Lazy 화면 밖을 같은 세 계측(프레임 콜백 · 그린 프레임 · 스피너 그리기)으로 잰다
 * - 공식 문서: LazyLayoutCacheWindow https://developer.android.com/reference/kotlin/androidx/compose/foundation/lazy/layout/LazyLayoutCacheWindow
 *   · Lists and grids https://developer.android.com/develop/ui/compose/lists
 *   · ReusableComposition https://developer.android.com/reference/kotlin/androidx/compose/runtime/ReusableComposition
 * - 소스(androidx-main): https://github.com/androidx/androidx/blob/androidx-main/compose/ui/ui/src/commonMain/kotlin/androidx/compose/ui/node/NodeCoordinator.kt
 *   · https://github.com/androidx/androidx/blob/androidx-main/compose/foundation/foundation/src/commonMain/kotlin/androidx/compose/foundation/lazy/LazyListMeasure.kt
 *   · https://github.com/androidx/androidx/blob/androidx-main/compose/ui/ui/src/commonMain/kotlin/androidx/compose/ui/layout/SubcomposeLayout.kt
 *   · https://github.com/androidx/androidx/blob/androidx-main/compose/foundation/foundation/src/commonMain/kotlin/androidx/compose/foundation/lazy/layout/CacheWindowLogic.kt
 *   — 아래 규칙은 프로젝트가 쓰는 ui 1.12.1 · ui-graphics 1.12.1 · foundation 1.12.1 의 sources jar 로 확인했다
 * - 규칙:
 *   · 그리기 경로에 '보이는가' 검사가 없다 — NodeCoordinator.draw → drawContainedDrawModifiers → LayoutNodeDrawScope.draw 는 크기 · 위치를
 *     보지 않고 그리기 Modifier 를 부르고, GraphicsLayer.record 도 크기 0 을 거르지 않는다. 거르는 것은 isPlaced(배치)뿐(#177)
 *     → 같은 창의 불투명 형제로 덮기 · 다른 창(Dialog)으로 앱 창 전체를 덮기 · offset 100,000px · size(0.dp) 는 모두 '보임' 과 같다
 *   · LazyListMeasure 는 스크롤 delta 를 반영할 때 이전 첫 칸부터 getAndMeasure(컴포즈 + 측정)해 보고 화면 밖이면 버린다
 *     ('this item is offscreen and will not be visible. advance firstVisibleItemIndex', remeasureNeeded = true).
 *     SubcomposeLayout 은 이번 측정에서 subcompose 한 칸을 활성으로 두고 나머지만 재사용 풀로 보낸다(disposeOrReuseStartingFromIndex)
 *     → 멈추기 직전 마지막 측정에서 화면 밖으로 나간 칸은 다음 측정까지 컴포지션에 남는다. remeasureNeeded 는 다음 스크롤이
 *       배치만 하는 지름길(copyWithScrollDeltaWithoutRemeasure)을 못 타게 할 뿐, 멈춘 목록을 다시 측정하지는 않는다
 *   · 재사용 풀로 간 칸은 비활성화된다 — Android 는 OutOfFrameExecutor(메인 Handler 큐 맨 앞)로 미뤄 ReusableComposition.deactivate()
 *     → remember 된 값이 잊혀 LaunchedEffect(무한 트랜지션 루프)가 취소된다(JVM 테스트: 비활성화 뒤 60틱 요청 0)
 *   · 기본 LazyListPrefetchStrategy 는 스크롤 방향의 다음 한 칸을 미리 가져온다(compose + apply 까지 — 효과가 돈다, 배치는 안 함)
 *   · 캐시 창(rememberLazyListState(cacheWindow = …), @ExperimentalFoundationApi)은 지나간 쪽을 behind 크기만큼 keepAroundItems 로
 *     매 측정 subcompose 한다 → 그 안의 칸은 재사용 풀로 가지 않는다
 * - 측정 설계: 한 칸 LazyRow(칸 너비 = 뷰포트, 스피너는 3번 칸, 사용자 스크롤 끔). 숨길 때는 scrollToItem(3) 으로 맞춘 뒤
 *   · 지나간 칸 = 칸의 1/8 씩 프레임마다 1.5칸 끌기(칸을 넘은 뒤 측정이 몇 번 더 돈다) · 캐시 창 안 칸 = 같은 끌기 + 캐시 창 앞 · 뒤 56dp(한 칸)
 *   · 한 번에 넘긴 칸 = scrollBy(뷰포트 너비) 한 번 · 미리 가져온 칸 = scrollToItem(6) 으로 건너뛴 뒤 스피너 쪽으로 1.5칸 끌기
 *     (건너뛴 측정은 사이 칸을 컴포즈하지 않는다 → 스피너는 끄는 방향의 다음 칸으로만 다시 컴포즈된다)
 *   · 칸 수명은 임시 DisposableEffect 로그로 먼저 확인했다 — 한 칸을 1번에 넘기면 dispose 없음, 1칸씩 2번이면 두 번째 측정에서 dispose,
 *     캐시 창(한 칸)에서 2칸을 1번에 넘기면 dispose 없음 · 3번이면 dispose
 * - 실측(헤드리스 에뮬레이터 · swiftshader, 60 Hz, 실기기는 쓰지 않았다 — 프레임 콜백 / 그린 프레임 / 스피너 그리기, 5초 창, 2번 묶음 9줄):
 *   · Pixel 4 API 30 release(디버그 키 서명) 2회 18줄 · debug 1회 9줄, Pixel Tablet API 35 debug 1회 9줄 모두 예상과 같음
 *     — 보임 211~229, 덮개 186~280, Dialog 덮개 113~180, 창 밖 158~230, 크기 0 133~215(세 수 같음, 1 차이 이내),
 *       Lazy 지나간 칸 0/0/0, 한 번에 넘긴 칸 · 미리 가져온 칸 · 캐시 창 안 칸 297~301/0/0
 *       (태블릿 debug: 보임 174, 덮개 155, Dialog 107, 창 밖 178, 크기 0 172, Lazy 0 · 301 · 300 · 299 — 같은 모양)
 *   · 캐시 창 안 칸을 보이게 둔 채 '지금 상태 재기': 127/127/127 — LazyRow 칸 안의 그리기 카운터도 센다
 *   · 앱 밖 교차 확인(release, atrace gfx view 4초 + dumpsys gfxinfo): 한 번에 넘긴 칸 doFrame 238 · gfxinfo 0,
 *     창 밖 offset 163 · 194(초당 같음), 지나간 칸 0 · 0 — 시계만 도는 줄은 GPU 렌더링 막대에 안 보인다
 *   · 1번 묶음도 같은 빌드로 다시 쟀다(API 30 release 2회 · debug 1회, 태블릿 debug 1회 20줄 모두 예상과 같음)
 * - 함정:
 *   · 첫 설계(칸을 한 번의 scrollBy 로 미는 것)에서 '지나간 1칸' 과 '캐시 창 밖 2칸' 이 둘 다 시계 300 으로 나와 기대(0)와 달랐다 —
 *     예제가 아니라 측정 방식이 마지막 측정에 칸을 남긴 것. 이 현상을 '한 번에 넘긴 칸' 줄로 따로 세우고, 나머지는 손가락처럼 나눠 끌었다
 *   · Dialog 덮개는 세는 동안 뒤로 가기로 닫지 않는다(닫히면 보임으로 바뀌어 줄이 틀린다). 앱 창의 FrameMetrics 만 센다 — Dialog 창 자체의 프레임은 안 센다
 *   · 창 밖 · 크기 0 은 debug 에서 보임보다 프레임이 많이 나올 수 있다(그릴 호가 없어 프레임이 가볍다) — 범주(계속)는 같다
 *
 * ## CustomVisibilityModifierCard (PathHitTester)
 * - 출처: https://www.romainguy.dev/posts/2025/arbitrary-shape-tap-detection/
 * 핵심 개념:
 * - PathHitTester는 임의의 Path 내부에 특정 좌표가 포함되는지 확인하는 API
 * - 별 모양 Path를 탭하면 PathHitTester로 hit test 후 색상 변경
 */
