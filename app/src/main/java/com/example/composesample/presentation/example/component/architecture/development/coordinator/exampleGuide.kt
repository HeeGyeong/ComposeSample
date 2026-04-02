package com.example.composesample.presentation.example.component.architecture.development.coordinator

/**
 * Coordinator 패턴 예제 참고 자료
 *
 * - 원문: https://medium.com/bumble-tech/the-coordinator-pattern-in-android-f5c9a924a31a
 *
 * 핵심 개념:
 * - Coordinator: 화면 전환 책임을 ViewModel/UI에서 분리하는 패턴
 * - Navigator 인터페이스: 실제 startActivity / navigate 호출을 추상화
 * - 장점: ViewModel이 Android 프레임워크에 의존하지 않아 순수 Kotlin 테스트 가능
 * - 단점: 소규모 프로젝트에서는 오버엔지니어링이 될 수 있음
 *
 * 구조:
 * - Coordinator: 화면 전환 흐름 관리 (상위 조율자)
 * - Navigator 구현체: Activity/Fragment/Compose NavController 기반 실제 전환 수행
 * - ViewModel: Navigator를 통해 화면 전환 요청만 (직접 Intent 생성 금지)
 */
