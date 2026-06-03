package com.example.composesample.presentation.example.component.ui.autofill

/**
 * Autofill 예제 참고 자료
 *
 * ## AutofillExampleUI (Compose Foundation 자동완성 — semantics contentType API)
 * - 공식 문서: https://developer.android.com/develop/ui/compose/text/autofill
 * - 출처(Android Weekly #729): Autofilling in the Blanks
 *
 * ### 핵심 개념
 * - `Modifier.semantics { contentType = ContentType.Username }`
 *   → 각 TextField 에 '이 필드는 무엇인가'(사용자명/비밀번호/이메일/우편번호 등) 의미를 부여하면,
 *     OS 자동완성 서비스가 어떤 데이터를 채우거나 저장할지 판단할 수 있음
 * - 여러 힌트 조합: `ContentType.Username + ContentType.EmailAddress`
 *   → 하나의 필드가 사용자명도/이메일도 될 수 있을 때 + 연산자로 합성
 * - 주요 ContentType: Username / Password / NewUsername / NewPassword /
 *   EmailAddress / PostalCode / PhoneNumber / PersonFullName / CreditCardNumber 등
 *   - 신규 가입 폼은 New 계열(NewUsername/NewPassword)을 써야 강력한 비밀번호 생성 제안이 동작
 *
 * ### LocalAutofillManager (commit / cancel)
 * - `val autofill = LocalAutofillManager.current`  // 미지원 환경에서는 null 가능
 * - `autofill?.commit()` : 로그인/회원가입 성공 직후 호출 → '이 자격증명을 저장할까요?' 제안
 * - `autofill?.cancel()` : 사용자가 폼을 떠나거나 초기화할 때 진행 중 세션 정리
 *
 * ### 가용성 (OS / 단말 의존)
 * - 자동완성 UI(저장/채우기 제안)는 OS 에 자동완성 서비스가 활성화되어 있어야 표시됨
 *   (설정 > 비밀번호·패스키 및 자동 완성 에서 서비스 지정)
 * - 서비스가 없으면 LocalAutofillManager 가 null 일 수 있고, 힌트만 부여될 뿐 UI 는 표시되지 않음
 * - 힌트 부여 자체는 항상 안전 — 지원 환경에서만 추가 기능이 켜지는 점진적 향상 패턴
 *
 * ### 버전 메모
 * - Compose Foundation 1.8+ 에서 자동완성이 stable 로 승격
 * - `Modifier.semantics { contentType = ... }` 가 표준 방식.
 *   일부 버전에서는 `Modifier.contentType(...)` 단축 형태도 제공
 * - 본 프로젝트: ComposeBom 2026.05.00 (Foundation 1.11.x)
 */
