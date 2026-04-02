package com.example.composesample.presentation.example.component.architecture.modularization

/**
 * Modularization Example 참고 자료
 *
 * - 공식 가이드: https://developer.android.com/topic/modularization
 * - Now in Android (모듈화 레퍼런스 앱): https://github.com/android/nowinandroid
 *
 * 모듈 유형:
 * - :app          — 앱 진입점, 다른 모든 모듈을 조합
 * - :feature:xxx  — 기능 단위 모듈 (화면 + ViewModel + UI 상태)
 * - :core:xxx     — 공유 유틸리티 (network, data, ui, testing 등)
 * - :domain       — UseCase + Repository 인터페이스 (순수 Kotlin)
 *
 * 핵심 원칙:
 * - 의존성 방향: :app → :feature → :core → :domain (역방향 금지)
 * - :feature 모듈 간 직접 의존 금지 → :core 또는 :app을 통해 통신
 * - 공유 UI 컴포넌트는 :core:ui 또는 :core:designsystem으로 분리
 *
 * 장점:
 * - 빌드 병렬화로 빌드 속도 향상
 * - 팀별 코드 소유권 명확화
 * - 기능 단위 동적 딜리버리(Dynamic Feature Module) 가능
 *
 * Gradle Convention Plugin 패턴:
 * - build-logic/convention 모듈에 공통 빌드 설정을 플러그인으로 캡슐화
 * - androidLibrary, androidFeature, androidCompose 등 재사용 가능한 플러그인 작성
 */
