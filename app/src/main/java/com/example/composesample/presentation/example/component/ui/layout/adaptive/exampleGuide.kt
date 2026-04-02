package com.example.composesample.presentation.example.component.ui.layout.adaptive

/**
 * Adaptive Layout (WindowSizeClass) 예제 참고 자료
 *
 * - 공식 문서: https://developer.android.com/develop/ui/compose/layouts/adaptive
 * - WindowSizeClass API: https://developer.android.com/reference/kotlin/androidx/window/core/layout/WindowSizeClass
 *
 * 핵심 개념:
 * - WindowSizeClass: 화면 너비/높이를 Compact/Medium/Expanded 3단계로 분류
 *   - Compact: 600dp 미만 (일반 폰 세로 모드)
 *   - Medium: 600~840dp (폴더블 반 접힘, 태블릿 세로)
 *   - Expanded: 840dp 이상 (태블릿 가로, 데스크탑)
 * - calculateWindowSizeClass(): Activity 컨텍스트에서 현재 WindowSizeClass 계산
 *
 * 패턴:
 * - Compact → 단일 컬럼 (ListDetail 순차 표시)
 * - Medium → 2컬럼 그리드
 * - Expanded → 사이드바 + 메인 영역 (ListDetail 동시 표시)
 *
 * 의존성:
 * implementation(libs.material3WindowSizeClass)
 */
