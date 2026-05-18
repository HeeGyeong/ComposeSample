package com.example.composesample.presentation.example.component.ui.style

/**
 * Foundation Style API 예제 참고 자료
 *
 * ## 개요
 * Compose 1.11 에서 실험적으로 도입된 Foundation Style API 는 디자인 토큰
 * (typography / colors / shapes / spacing 등) 을 단일 Immutable 객체로 묶고
 * 하나의 CompositionLocal 을 통해 트리에 전파한다.
 *
 * - 기존: 토큰 종류마다 별도 CompositionLocal + Provider 가 필요했음
 * - 신규: AppStyle 1개 = Local 1개 → copy() 로 부분 오버라이드
 *
 * ## 참고 링크
 * - 소개 글: https://simtop.medium.com/compose-styling-is-changing-heres-what-google-s-new-style-api-gets-right-9cb52f5065ef
 * - Compose Foundation 1.11 릴리즈 노트:
 *   https://developer.android.com/jetpack/androidx/releases/compose-foundation
 *
 * ## 본 예제의 구현 선택
 * - 실험 API 의존성을 추가하지 않고 핵심 패턴(단일 Immutable Style + 단일
 *   CompositionLocal + copy() 오버라이드) 만 자체 구현으로 재현한다.
 * - `staticCompositionLocalOf` 사용 — Style 값은 자주 바뀌지 않는다는 가정.
 *   값이 바뀌면 사용처 전체가 invalidate 되지만 그만큼 읽기 비용이 0 에 가깝다.
 *   동적 토큰(예: 사용자 입력에 따라 빠르게 바뀌는 색상)이 필요하면
 *   `compositionLocalOf` 로 바꿔야 한다.
 * - MaterialTheme 과는 공존 — Foundation 레이어 토큰은 Style API,
 *   Material 디자인 시스템 토큰은 MaterialTheme.* 로 계속 사용 가능.
 *
 * ## 프로덕션 적용 시 주의
 * - Compose 1.11 의 공식 Foundation Style API 는 Experimental 단계로,
 *   네이밍 / 시그니처가 변경될 수 있다. 본 예제처럼 패턴만 따르고
 *   안정화 후 공식 API 로 마이그레이션하는 것이 안전하다.
 */
