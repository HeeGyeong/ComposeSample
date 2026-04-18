package com.example.composesample.presentation.example.component.ui.scroll

/**
 * 📚 Custom TopAppBarScrollBehavior 학습 가이드
 *
 * 출처: https://le0nidas.gr/2026/02/08/handle-recyclerviews-scroll-events-in-custom-topappbarscrollbehavior/
 *
 * 이 문서는 RecyclerView 스크롤 이벤트를 커스텀 TopAppBarScrollBehavior로 처리하는 방법에 대한
 * 상세 가이드를 제공합니다.
 *
 * =================================================================================================
 * 🎯 이 예제로 학습할 수 있는 내용
 * =================================================================================================
 *
 * 1. TopAppBarScrollBehavior의 구조와 커스텀 구현 방법
 * 2. NestedScrollConnection을 활용한 스크롤 이벤트 제어
 * 3. 부분 렌더링 없이 완전 숨김/표시를 위한 스냅 동작 구현
 * 4. animate()와 tween()을 활용한 스크롤 애니메이션
 * 5. 스크롤 버퍼를 사용한 제스처 필터링
 * 6. animationInProgress 플래그로 느린 스크롤 문제 해결
 * 7. EnterAlways와 ExitUntilCollapsed 두 가지 동작 구현
 *
 * =================================================================================================
 * 🔍 핵심 개념: TopAppBarScrollBehavior
 * =================================================================================================
 *
 * TopAppBarScrollBehavior는 앱 바가 콘텐츠 스크롤에 어떻게 반응할지 정의합니다.
 * 내부적으로 NestedScrollConnection을 통해 스크롤/플링 이벤트 체인에서 호출되는 메서드를 제공합니다.
 *
 * 주요 속성:
 * - state: TopAppBarState (heightOffset, heightOffsetLimit, collapsedFraction)
 * - isPinned: 고정 여부
 * - nestedScrollConnection: 스크롤 이벤트 처리 연결
 * - snapAnimationSpec: 스냅 애니메이션 설정
 * - flingAnimationSpec: 플링 애니메이션 설정
 *
 * =================================================================================================
 * 📱 구현 1: MyEnterAlwaysScrollBehavior
 * =================================================================================================
 *
 * 🎯 목표: 스크롤 방향에 따라 TopAppBar를 완전히 숨기거나 표시
 *         (부분 렌더링 없이 즉시 전환)
 *
 * 📝 진화 과정:
 *
 * 1단계 - 기본 구현 (즉시 스냅):
 * ────────────────────────────────────────────────────────────────────────────────────
 * ```kotlin
 * override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
 *     if (available.y == 0f) return Offset.Zero
 *     val previousOffset = state.heightOffset
 *     val newOffset = if (available.y > 0) 0f else state.heightOffsetLimit
 *     state.heightOffset = newOffset
 *     return Offset(0f, newOffset - previousOffset)
 * }
 * ```
 * ⚠️ 문제점: 애니메이션 없이 즉시 전환되며, 모든 미세한 스크롤에 반응함
 *
 * 2단계 - 애니메이션 + 스크롤 버퍼 추가:
 * ────────────────────────────────────────────────────────────────────────────────────
 * ```kotlin
 * // 이미 완전히 펼쳐졌거나 접혀있으면 무시
 * if (available.y > 0 && state.collapsedFraction == 0f) return Offset.Zero
 * if (available.y < 0 && state.collapsedFraction == 1f) return Offset.Zero
 *
 * // 스크롤 버퍼: 100px 이상 누적되어야 반응
 * scrollAccumulation += abs(available.y)
 * if (scrollAccumulation < 100f) return Offset.Zero
 * scrollAccumulation = 0f
 *
 * // tween 애니메이션으로 부드러운 전환
 * coroutineScope.launch {
 *     animate(initialValue, targetValue, tween(150)) { value, _ ->
 *         state.heightOffset = value
 *     }
 * }
 * ```
 * ⚠️ 문제점: 느린 스크롤 시 애니메이션 중에 새 이벤트가 들어옴
 *
 * 3단계 - animationInProgress 플래그 (최종):
 * ────────────────────────────────────────────────────────────────────────────────────
 * ```kotlin
 * if (animationInProgress) return Offset.Zero  // 애니메이션 중이면 무시
 *
 * coroutineScope.launch {
 *     animationInProgress = true
 *     try {
 *         animate(...) { ... }
 *     } finally {
 *         animationInProgress = false
 *     }
 * }
 * ```
 * ✅ 완성: 부드러운 애니메이션 + 스크롤 버퍼 + 중복 방지
 *
 * =================================================================================================
 * 📱 구현 2: MyExitUntilCollapsedScrollBehavior
 * =================================================================================================
 *
 * 🎯 목표: 위로 스크롤하면 숨기고, 사용자가 더 이상 아래로 스크롤할 수 없을 때만 다시 표시
 *
 * 핵심 차이점:
 * - onPreScroll: 위로 스크롤(available.y < 0)일 때만 숨기기 처리
 * - onPostScroll: 사용자가 더 이상 스크롤할 수 없을 때(available == Offset.Zero) 다시 표시
 *
 * ```kotlin
 * // onPreScroll - 위로 스크롤만 처리
 * if (available.y >= 0f) return Offset.Zero  // 아래로 스크롤은 무시
 *
 * // onPostScroll - 스크롤 끝 도달 시 표시
 * if (available != Offset.Zero) return consumed  // 아직 스크롤 가능하면 무시
 * // available == Offset.Zero → 더 이상 스크롤 불가 → 바 표시
 * ```
 *
 * 💡 RecyclerView에서의 활용:
 *    onScrolled() 내부에서 recyclerView.canScrollVertically(-1) 체크 후
 *    dispatcher.dispatchPostScroll()으로 Compose에 이벤트 전달
 *
 * =================================================================================================
 * 🔧 구현 핵심 포인트 정리
 * =================================================================================================
 *
 * ✅ 부분 렌더링 방지:
 *    - state.heightOffset을 0 또는 heightOffsetLimit으로만 설정
 *    - 중간 값 없이 완전히 보이거나 완전히 숨김
 *
 * ✅ 스크롤 버퍼 (scrollAccumulation):
 *    - 작은 제스처에 즉시 반응하지 않도록 임계값 설정
 *    - 사용자의 의도적인 스크롤만 감지
 *    - 임계값: 100px (조절 가능)
 *
 * ✅ 애니메이션 (animate + tween):
 *    - tween(durationMillis = 150)으로 150ms 전환 애니메이션
 *    - coroutineScope.launch로 비동기 실행
 *    - state.heightOffset을 점진적으로 변경
 *
 * ✅ 중복 방지 (animationInProgress):
 *    - 애니메이션 실행 중 새 스크롤 이벤트 무시
 *    - try-finally로 플래그 안전하게 관리
 *    - 느린 스크롤 시 깜빡임/떨림 방지
 *
 * ✅ 얼리 리턴 패턴:
 *    - available.y == 0f: 스크롤 없음 → 무시
 *    - collapsedFraction == 0f && scrolling down: 이미 펼쳐짐 → 무시
 *    - collapsedFraction == 1f && scrolling up: 이미 접혀짐 → 무시
 *
 * =================================================================================================
 * 🚀 실무 적용 가이드
 * =================================================================================================
 *
 * ✅ RecyclerView + Compose 혼합 프로젝트:
 *    - NestedScrollDispatcher로 RecyclerView 이벤트를 Compose로 전달
 *    - 커스텀 TopAppBarScrollBehavior로 부분 렌더링 문제 해결
 *
 * ✅ 순수 Compose 프로젝트:
 *    - 기본 제공 scrollBehavior 대신 커스텀 구현으로 세밀한 제어
 *    - 스크롤 버퍼와 애니메이션으로 UX 향상
 *
 * ✅ UX 향상 포인트:
 *    - 즉시 반응 대신 의도적 제스처에만 반응
 *    - 부드러운 애니메이션으로 시각적 품질 향상
 *    - 두 가지 스크롤 동작 선택 가능 (EnterAlways / ExitUntilCollapsed)
 *
 * ❌ 주의사항:
 *    - scrollAccumulation 임계값이 너무 크면 반응이 느려짐
 *    - 애니메이션 duration이 너무 길면 사용자 입력과 어긋남
 *    - animationInProgress 플래그를 finally에서 반드시 해제
 */
object CustomScrollBehaviorGuide
