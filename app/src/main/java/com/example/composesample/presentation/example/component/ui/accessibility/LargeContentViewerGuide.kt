package com.example.composesample.presentation.example.component.ui.accessibility

/**
 * 📚 Large Content Viewer with Navigation Support 학습 가이드
 *
 * 출처: https://eevis.codes/blog/2026-02-28/adding-navigation-support-to-large-content-viewer-with-compose/
 * 이전 글: https://eevis.codes/blog/beyond-font-scaling-large-content-viewer-with-compose/
 *
 * 이 문서는 iOS의 Large Content Viewer를 Jetpack Compose로 구현하고,
 * 키보드 내비게이션, 스크린 리더, Voice Access 지원을 추가하는 방법을 다룹니다.
 *
 * =================================================================================================
 * 🎯 이 예제로 학습할 수 있는 내용
 * =================================================================================================
 *
 * 1. Large Content Viewer 개념 (iOS 접근성 기능을 Compose로)
 * 2. Long-press를 통한 아이콘 프리뷰 표시
 * 3. onFocusChanged를 활용한 키보드 내비게이션 지원
 * 4. semantics의 customActions를 활용한 스크린 리더 지원
 * 5. Voice Access 자동 지원
 *
 * =================================================================================================
 * 🔍 핵심 개념 1: Large Content Viewer란?
 * =================================================================================================
 *
 * iOS에는 "Large Content Viewer"라는 접근성 기능이 있습니다.
 * 작은 아이콘이나 버튼을 길게 누르면 해당 아이콘의 확대된 프리뷰가 표시됩니다.
 *
 * 이 기능이 필요한 이유:
 * - 시력이 약한 사용자에게 작은 UI 요소를 확인하는 데 도움
 * - 탭 바/네비게이션 바의 아이콘은 시스템 글꼴 크기에 영향받지 않음
 * - Dynamic Type으로 크기 조정이 불가능한 아이콘에 대한 보완책
 *
 * =================================================================================================
 * 🔍 핵심 개념 2: pointerInput으로 Long-Press 구현
 * =================================================================================================
 *
 * ```kotlin
 * Modifier.pointerInput(Unit) {
 *     detectTapGestures(
 *         onLongPress = {
 *             previewedItem = item
 *         }
 *     )
 * }
 * ```
 *
 * - detectTapGestures의 onLongPress로 길게 누르기 감지
 * - 길게 누른 아이템의 프리뷰를 Popup 또는 Overlay로 표시
 * - 터치가 끝나면 프리뷰 해제
 *
 * =================================================================================================
 * 🔍 핵심 개념 3: 키보드 내비게이션 지원
 * =================================================================================================
 *
 * 키보드 사용자는 long-press를 할 수 없으므로 대안이 필요합니다.
 * 포커스가 머무르는 시간으로 long-press를 대체합니다.
 *
 * ```kotlin
 * NavigationBarItem(
 *     modifier = Modifier
 *         .onFocusChanged {
 *             if (it.isFocused) {
 *                 scope.launch {
 *                     delay(viewConfiguration.longPressTimeoutMillis)
 *                     previewedItem = item
 *                 }
 *             } else {
 *                 previewedItem = null
 *             }
 *         },
 *     ...
 * )
 * ```
 *
 * 핵심 포인트:
 * - onFocusChanged 모디파이어로 포커스 상태 변경 감지
 * - it.isFocused가 true이면 코루틴으로 delay 후 프리뷰 표시
 * - viewConfiguration.longPressTimeoutMillis로 시스템 long-press 시간 사용
 * - 포커스가 떠나면 previewedItem = null로 프리뷰 해제
 *
 * =================================================================================================
 * 🔍 핵심 개념 4: 스크린 리더 (TalkBack) 지원
 * =================================================================================================
 *
 * 스크린 리더 사용자를 위해 Custom Accessibility Action을 추가합니다.
 * 모든 스크린 리더 사용자가 시각 장애인은 아닙니다 (시력 스펙트럼).
 *
 * ```kotlin
 * NavigationBarItem(
 *     modifier = Modifier
 *         .semantics {
 *             customActions = listOf(
 *                 CustomAccessibilityAction(
 *                     label = "Preview item",
 *                     action = {
 *                         scope.launch {
 *                             previewedItem = item
 *                         }
 *                         true
 *                     }
 *                 )
 *             )
 *         },
 *     ...
 * )
 * ```
 *
 * 핵심 포인트:
 * - semantics { customActions = listOf(...) }로 커스텀 액션 등록
 * - CustomAccessibilityAction의 label이 TalkBack 메뉴에 표시됨
 * - action 블록에서 true 반환 → 성공적으로 처리됨을 알림
 * - 스크린 리더 사용자는 의도적으로 액션을 트리거하므로 delay 불필요
 *
 * =================================================================================================
 * 🔍 핵심 개념 5: Voice Access
 * =================================================================================================
 *
 * Voice Access는 음성 명령으로 UI를 조작하는 보조 기술입니다.
 * 이미 long-press가 구현되어 있으므로 별도 작업이 불필요합니다.
 *
 * Voice Access 명령:
 * - "Long press [아이템 이름]" → 기존 long-press 동작이 트리거됨
 * - 이미 pointerInput에서 처리하고 있으므로 추가 코드 불필요
 *
 * =================================================================================================
 * 🚀 실무 적용 가이드
 * =================================================================================================
 *
 * ✅ 적용 가능한 시나리오:
 *    - Bottom Navigation Bar 아이콘 프리뷰
 *    - Tab Bar 아이콘 확대 표시
 *    - Toolbar 버튼 설명 표시
 *    - 이모지 피커에서 이모지 확대 프리뷰
 *    - 아이콘 그리드에서 아이콘 식별 도우미
 *
 * ✅ 접근성 모범 사례:
 *    - 포인터(터치), 키보드, 스크린 리더, 음성 모두 지원
 *    - viewConfiguration.longPressTimeoutMillis 사용 (시스템 설정 존중)
 *    - 스크린 리더 액션에는 딜레이를 주지 않기 (의도적 동작이므로)
 *    - 프리뷰 해제 조건을 명확히 처리
 *
 * ❌ 주의사항:
 *    - Android에서는 Large Content Viewer가 표준이 아니므로 사용자 가이드 고려
 *    - 프리뷰 팝업이 다른 UI를 가리지 않도록 위치 계산 필요
 *    - 코루틴 scope leak 방지 (포커스 해제 시 Job 취소)
 */
object LargeContentViewerGuide
