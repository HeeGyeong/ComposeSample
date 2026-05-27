package com.example.composesample.presentation.example.component.architecture.development.featureflag

/**
 * Type-Safe Feature Flag (Switchboard 유사) 예제 참고 자료
 *
 * ## 출처
 * - Switchboard (GitHub): https://github.com/megh-lath-1012/switchboard
 * - Android Weekly Issue #727
 *
 * ## 핵심 개념
 * - **문자열 키의 문제**: `flags.getBoolean("new_profile_ui")` 처럼 문자열로 접근하면
 *   오타·존재하지 않는 키·타입 불일치가 런타임에야 드러난다.
 * - **타입 안전 레지스트리**: sealed class 로 플래그를 정의하면 IDE 자동완성 + 컴파일 타임 검증을 얻는다.
 *   - `FeatureFlag.NewProfileUi`(Boolean), `FeatureFlag.HomeLayout`(Choice) 처럼 값의 종류까지 타입으로 표현.
 *
 * ## 4가지 패턴
 * 1. **sealed class 레지스트리** — `FeatureFlag.all` 로 전체 플래그 열거, 새 플래그는 object 추가만으로 등록
 * 2. **StateFlow reactive 토글** — `FeatureFlagStore.flags: StateFlow<Map<String, FlagValue>>`,
 *    Compose 는 `collectAsState()` 로 구독 → 값 변경 시 자동 재구성
 * 3. **디버그 메뉴(ModalBottomSheet)** — QA/개발 빌드에서 런타임 강제 오버라이드, 출처를 DEBUG 로 고정
 * 4. **Remote Config 시뮬레이션** — 원격 응답을 fetch 해 덮어쓰되 DEBUG 오버라이드는 보존
 *    (우선순위: DEBUG > REMOTE > LOCAL)
 *
 * ## 제약 / 본 예제의 선택
 * - Switchboard 자체는 KSP 코드 생성 + Firebase Remote Config 의존성이 필요하다.
 * - 본 예제는 외부 의존성 없이 동일 패턴을 직접 구현했다(교육 목적).
 * - 실제 프로덕션에서는 fetch 를 Firebase Remote Config / 자체 백엔드 호출로 교체하고,
 *   디버그 시트는 BuildConfig.DEBUG 가드로 릴리스 빌드에서 제외한다.
 */
