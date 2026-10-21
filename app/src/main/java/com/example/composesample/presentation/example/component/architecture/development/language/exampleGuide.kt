package com.example.composesample.presentation.example.component.architecture.development.language

/**
 * Kotlin 언어 기능 예제 참고 자료
 *
 * ## Sealed Class vs Sealed Interface
 * - 공식 문서: https://kotlinlang.org/docs/sealed-classes.html
 * - 출처: https://proandroiddev.com/kotlin-sealed-classes-and-interface-9a90f80d4983
 * 핵심 개념:
 * - sealed class: 단일 파일·패키지 내 하위 클래스 제한. 생성자 인자 가능
 * - sealed interface: 다중 상속 가능. data class/object가 여러 sealed interface 구현 가능
 * - when 표현식과 함께 사용 시 else 분기 불필요 → 컴파일러가 완전성 보장
 *
 * 활용 패턴:
 * - UI 상태 모델링: sealed class UiState { Loading, Success(data), Error(msg) }
 * - 이벤트 처리: sealed interface UiEvent { data class Click(id: Int), object Refresh }
 * - 다중 상태 조합: sealed interface 다중 구현으로 복합 상태 표현
 * - ApiResult<T> 제네릭 래퍼로 Success/Error/Loading/Empty 상태를 재사용 가능하게 표현 (timestamp·code 등 부가 정보 포함)
 * - Enum 대비 장점: 각 하위 타입이 서로 다른 데이터를 보유 가능 + 제네릭 타입 파라미터 지원
 *
 * Kotlin 1.9+ 변경:
 * - sealed class 하위 클래스가 같은 패키지 전체로 확장 (파일 제한 완화)
 *
 * ## Name-Based Destructuring (Kotlin 2.3.20)
 * - Kotlin KEEP: https://youtrack.jetbrains.com/issue/KT-19627
 * - What's New: https://kotlinlang.org/docs/whatsnew2320.html
 * 핵심 개념:
 * - 위치 기반 구조 분해(componentN)는 프로퍼티 순서 변경 시 조용한 버그 유발
 * - 이름 기반: val (local = propertyName) 형태로 프로퍼티 이름을 명시적으로 매칭
 * - 컴파일러 플래그: -Xname-based-destructuring={only-syntax|name-mismatch|complete}
 * - only-syntax: 명시적 형태만 허용 (기존 코드 영향 없음, 점진적 마이그레이션에 적합)
 * - name-mismatch: 변수명과 프로퍼티명 불일치 시 경고 발생
 * - complete: 이름 기반이 기본, 위치 기반은 [] 구문으로 전환
 * - 현재 Experimental 상태. 향후 이름 기반이 기본 동작이 될 예정
 *
 * ## Kotlin 2.4 Language Features (Collection Literals / Context Parameters)
 * - What's New 2.4: https://kotlinlang.org/docs/whatsnew24.html
 * - Collection literals KEEP: https://github.com/Kotlin/KEEP/issues/112
 * - Context parameters KEEP: https://github.com/Kotlin/KEEP/blob/main/proposals/KEEP-0367-context-parameters.md
 * 핵심 개념:
 * - 컬렉션 리터럴: [1, 2, 3] 대괄호 문법으로 컬렉션 생성. 기대 타입에 따라 List/Set/Map 추론
 * - 커스텀 타입은 companion 의 operator fun of(vararg ...) 로 리터럴 생성을 지원할 수 있음
 * - 컨텍스트 파라미터: context(name: Type) 선언으로 의존성을 암시적 주입(인자 전달 보일러플레이트 제거)
 * - context parameters 는 deprecated 된 context receivers 의 후속 — 컨텍스트에 변수명을 부여해 모호성 해소
 * - 컴파일러 플래그(Experimental): -Xcollection-literals / -Xcontext-parameters
 * - 두 기능 모두 Experimental → 전역 적용 금지, 예제(모듈/파일) 단위 opt-in 권장
 *
 * ## 민감 값·불법 상태를 타입으로 막기 (SensitiveTypesExampleUI)
 * - 본 예제 출처: https://blog.jetbrains.com/kotlin/2026/10/kotlinconf-recap-lessons-from-verifone-s-architect-on-kotlin-in-mission-critical-payments/
 *   (Yuri Geronimus, KotlinConf 2026 "Kotlin as a Safety Net" 정리 — 패턴 2·4·6·9 를 옮겼다)
 * - 공식 문서(inline value class): https://kotlinlang.org/docs/inline-classes.html
 * - 공식 문서(copy 가시성 변경, 2.0.20): https://kotlinlang.org/docs/whatsnew2020.html#data-class-copy-function-to-have-the-same-visibility-as-constructor
 * - copy 가시성 변경 이슈: https://youtrack.jetbrains.com/issue/KT-11914
 * 핵심 개념(Kotlin 2.4.21 + Gson 2.14.0 단독 컴파일·단위 테스트·Galaxy A72 debug·release 실측):
 * - 가림 value class: private 생성자 + toString 재정의 + 이름 있는 출구(toNetwork). 문자열 템플릿·data class toString·
 *   Pan?·List<Pan>·예외 메시지(스택 트레이스 포함 logcat)가 전부 PAN[****1111] — 박싱돼도 박스의 toString 이 재정의를 부른다
 * - 거부 결과에 입력을 그대로 실으면 거기서 샌다 → 원문의 RawStan 처럼 입력도 타입(RawPan)으로 감싼다
 * - Gson 직렬화는 toString 을 거치지 않는다: toJson(request) = 원문, toJson(pan) = {"value":원문}
 * - **Pan 전용 TypeAdapter 를 등록해도 data class 필드에는 안 걸린다** — value class 필드는 JVM 에서 String 필드로 지워져
 *   Gson 이 String 어댑터를 고른다(직렬화·역직렬화 모두). 단독 Pan 에만 걸린다
 * - Gson 역직렬화는 private 생성자·검증 팩토리를 건너뛴다(인자 없는 생성자가 없으면 Unsafe) → 응답은 nullable DTO 로 받고
 *   매퍼가 팩토리로 도메인 타입을 만든다(CLAUDE.md 데이터 클래스 규칙, Gson 누락 필드 예제)
 * - JSON 필드 순서는 ART(기기)와 JVM 이 다르다 — getDeclaredFields 순서는 명세가 정하지 않는다
 * - data class 의 private 생성자: copy() 는 2.4 까지 public(바이트코드 public final). 표시 없으면 선언 경고,
 *   @ExposedCopyVisibility 는 선언 경고만 없애고 바깥 copy() 호출 경고는 남는다 — 둘 다 "language version 2.5 부터 오류".
 *   @ConsistentCopyVisibility 면 copy 도 private(리플렉션 invoke 는 IllegalAccessException)
 * - 타임아웃을 결과(TimedOut)로: 예외로 잡고 새 요청으로 재시도하면 매입사 승인 2건, 결과로 받아 같은 STAN 으로 조회하면 1건
 *
 * 본 예제의 단순화 포인트
 *  - 카드번호는 결제 테스트 번호(4111…)만 쓴다. 실제 시스템의 PCI 범위·암호화·키 관리는 다루지 않는다(원문도 "실제로는 훨씬 깊이 숨긴다").
 *  - copy() 우회는 소스 호출 대신 리플렉션으로 보인다 — 소스 호출은 2.4 에서 경고, 2.5 부터 오류라 빌드 경고 0 을 지키기 위해서다.
 *    리플렉션 코드(CopyVisibilityProbe)는 HotSwan 인터프리터가 caller-sensitive 리플렉션을 못 해 debug 핫 리로드에서 뺐다.
 *  - 원문의 다른 패턴은 기존 예제로: ID value class → InlineValueClassExample · sealed 결과·else 삭제 → SealedDomainErrorExample ·
 *    coroutineScope/supervisorScope → StructuredConcurrencyGuardrailExample · 경계 requireNotNull → GsonMissingFieldExample
 */
