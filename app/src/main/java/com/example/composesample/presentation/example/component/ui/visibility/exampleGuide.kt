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
 *
 * ## CustomVisibilityModifierCard (PathHitTester)
 * - 출처: https://www.romainguy.dev/posts/2025/arbitrary-shape-tap-detection/
 * 핵심 개념:
 * - PathHitTester는 임의의 Path 내부에 특정 좌표가 포함되는지 확인하는 API
 * - 별 모양 Path를 탭하면 PathHitTester로 hit test 후 색상 변경
 */
