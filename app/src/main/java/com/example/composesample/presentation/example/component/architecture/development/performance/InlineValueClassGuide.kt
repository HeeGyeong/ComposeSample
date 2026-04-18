package com.example.composesample.presentation.example.component.architecture.development.performance

/**
 * Zero-Cost Abstractions in Kotlin: Inline Functions and Value Classes Guide
 *
 * 출처: https://carrion.dev/en/posts/kotlin-inline-functions-value-classes/
 * 
 * === Kotlin의 Zero-Cost Abstractions ===
 * 
 * 1. Zero-Cost Abstractions란?
 *    - 추상화를 제공하면서도 런타임 오버헤드가 없거나 최소화
 *    - 타입 안전성과 표현력을 유지하면서 성능 향상
 *    - Inline Functions: 호출 오버헤드 제거
 *    - Value Classes: 래퍼 객체 할당 제거
 * 
 * 2. Inline Functions의 개념
 *    - 컴파일 타임에 함수 본문을 호출 지점에 복사
 *    - 함수 호출 오버헤드 제거
 *    - 람다를 받는 고차 함수에서 특히 유용
 *    - 람다 객체 할당 제거
 * 
 * 3. Inline Functions 기본 사용
 *    기본 문법:
 *      inline fun <T> measure(label: String, block: () -> T): T {
 *          val start = System.currentTimeMillis()
 *          try {
 *              return block()
 *          } finally {
 *              val elapsed = System.currentTimeMillis() - start
 *              println("$label took ${elapsed}ms")
 *          }
 *      }
 *    
 *    호출 시:
 *      val result = measure("expensive") {
 *          computeHeavy()
 *      }
 *    
 *    컴파일 후 (개념적):
 *      val start = System.currentTimeMillis()
 *      try {
 *          val result = computeHeavy()
 *      } finally {
 *          val elapsed = System.currentTimeMillis() - start
 *          println("expensive took ${elapsed}ms")
 *      }
 * 
 * 4. Inline 수정자들
 *    noinline:
 *      - 특정 람다 파라미터를 인라인하지 않음
 *      - 람다를 변수에 저장하거나 전달할 때 필요
 *      inline fun process(
 *          action: () -> Unit,
 *          noinline onError: (Throwable) -> Unit
 *      ) {
 *          try { action() }
 *          catch (t: Throwable) { onError(t) }
 *      }
 *    
 *    crossinline:
 *      - 람다에서 non-local return 금지
 *      - 람다를 다른 실행 컨텍스트에서 재호출할 때 필요
 *      inline fun process(
 *          crossinline action: () -> Unit
 *      ) {
 *          // action()을 나중에 또는 다른 곳에서 호출
 *      }
 * 
 * 5. Reified Type Parameters
 *    일반 제네릭의 문제:
 *      fun <T> castOrNull(any: Any?): T? {
 *          // T 타입을 런타임에 알 수 없음 (타입 소거)
 *          return if (any is T) any else null  // 컴파일 에러!
 *      }
 *    
 *    Inline + Reified 해결:
 *      inline fun <reified T> castOrNull(any: Any?): T? {
 *          return if (any is T) any else null  // OK!
 *      }
 *    
 *    실제 사용:
 *      val s: String? = castOrNull("hello")  // Works!
 *      val i: Int? = castOrNull("hello")     // null
 * 
 * 6. Inline Functions 사용 시기
 *    ✓ 사용해야 할 때:
 *      - 작은 유틸리티 함수 (hot path)
 *      - 람다를 받는 고차 함수
 *      - Reified type parameters가 필요한 API
 *      - 자주 호출되는 성능 중요 함수
 *    
 *    ✗ 피해야 할 때:
 *      - 큰 함수 (바이트코드 크기 증가)
 *      - 드물게 호출되는 함수
 *      - Public API (모듈 간 인라인 고려)
 *      - 재귀 함수
 * 
 * 7. Value Classes의 개념
 *    - 단일 값을 래핑하여 타입 안전성 제공
 *    - 런타임에 래퍼 객체 할당 최소화 또는 제거
 *    - JVM에서 underlying 값으로 표현
 *    - 컴파일 타임 타입 안전성 + 런타임 성능
 * 
 * 8. Value Classes 기본 사용
 *    선언:
 *      @JvmInline
 *      value class UserId(val value: String) {
 *          init {
 *              require(value.isNotBlank()) {
 *                  "UserId cannot be blank"
 *              }
 *          }
 *      }
 *    
 *    타입 안전성:
 *      @JvmInline
 *      value class ProductId(val value: String)
 *      
 *      @JvmInline
 *      value class OrderId(val value: String)
 *      
 *      fun linkUserToProduct(
 *          userId: UserId,
 *          productId: ProductId
 *      ) { /* ... */ }
 *      
 *      // 컴파일 에러 방지:
 *      linkUserToProduct(userId, orderId)  // Type mismatch!
 * 
 * 9. Value Classes 연산자
 *    도메인 로직 캡슐화:
 *      @JvmInline
 *      value class Money(val cents: Long) {
 *          operator fun plus(other: Money) =
 *              Money(cents + other.cents)
 *          
 *          operator fun minus(other: Money) =
 *              Money(cents - other.cents)
 *          
 *          operator fun times(multiplier: Int) =
 *              Money(cents * multiplier)
 *      }
 *      
 *      val price = Money(1000)
 *      val total = price * 3  // Money(3000)
 * 
 * 10. Value Classes Boxing
 *     Boxing이 발생하는 경우:
 *       - 제네릭으로 사용: List<UserId>
 *       - 인터페이스 구현: UserId implements Comparable
 *       - Nullable: UserId?
 *       - Any로 업캐스트
 *     
 *     예시:
 *       val userId = UserId("123")  // No boxing
 *       val list = listOf(userId)   // Boxing occurs
 *       val nullable: UserId? = userId  // Boxing occurs
 * 
 * 11. 실전 사용 사례
 *     1. 안전한 ID:
 *        @JvmInline value class ProductId(val value: String)
 *        @JvmInline value class UserId(val value: String)
 *        @JvmInline value class OrderId(val value: String)
 *     
 *     2. 단위와 측정:
 *        @JvmInline value class Meters(val value: Double)
 *        @JvmInline value class Kilograms(val value: Double)
 *     
 *     3. 검증된 타입:
 *        @JvmInline
 *        value class Email(val value: String) {
 *            init {
 *                require("@" in value) { "Invalid email" }
 *            }
 *        }
 *     
 *     4. 돈과 통화:
 *        @JvmInline value class Cents(val value: Long)
 *        @JvmInline value class Percentage private constructor(
 *            val value: Int
 *        ) {
 *            companion object {
 *                fun of(raw: Int) = Percentage(raw.coerceIn(0, 100))
 *            }
 *        }
 * 
 * 12. Inline + Reified 실전 예제
 *     JSON 파싱:
 *       inline fun <reified T> parseJson(json: String): T {
 *           // T::class 접근 가능
 *           return gson.fromJson(json, T::class.java)
 *       }
 *     
 *     리플렉션 없는 타입 체크:
 *       inline fun <reified T> isInstance(any: Any?): Boolean {
 *           return any is T
 *       }
 *     
 *     타입 안전 캐스트:
 *       inline fun <reified T> safeCast(any: Any?): T? {
 *           return any as? T
 *       }
 * 
 * 13. 성능 노트
 *     Inline Functions:
 *       - 장점: 함수 호출 오버헤드 제거, 람다 할당 제거
 *       - 단점: 바이트코드 크기 증가 (코드 중복)
 *       - 측정 필요: 실제 성능 향상 확인
 *     
 *     Value Classes:
 *       - 장점: 타입 안전 + 성능 (boxing 없을 때)
 *       - 단점: Boxing 발생 시 오버헤드
 *       - JVM 최적화: 대부분의 경우 primitive처럼 동작
 * 
 * 14. Interoperability
 *     Java와의 호환:
 *       - @JvmInline: Java에서는 underlying 타입으로 보임
 *       - Inline functions: Java에서는 일반 함수로 보임
 *       - 문서화 필요: Java 호출자를 위한 설명
 *     
 *     Multiplatform:
 *       - Value classes: 모든 타겟 지원
 *       - Inline functions: 모든 타겟 지원
 *       - 플랫폼별 boxing 동작 차이 주의
 * 
 * 15. 테스트 전략
 *     Inline Functions:
 *       - 일반 함수처럼 테스트 가능
 *       - 성능 벤치마크: hot path에서 측정
 *       - 바이트코드 크기 모니터링
 *     
 *     Value Classes:
 *       - 불변성 테스트
 *       - 연산자 오버로딩 테스트
 *       - Boxing 시나리오 테스트
 *       - Serialization round-trip 테스트
 * 
 * 16. 안티패턴
 *     ❌ 잘못된 사용:
 *       // 너무 큰 inline 함수
 *       inline fun hugeFunction() { /* 100+ lines */ }
 *       
 *       // 복잡한 상태를 가진 value class
 *       @JvmInline
 *       value class BadClass(val value: Pair<String, Int>)
 *       
 *       // Value class를 data class처럼 사용
 *       @JvmInline
 *       value class NotGood(val data: ComplexObject)
 *     
 *     ✅ 올바른 사용:
 *       // 작은 유틸리티
 *       inline fun <T> T.alsoIf(
 *           condition: Boolean,
 *           crossinline block: (T) -> Unit
 *       ): T {
 *           if (condition) block(this)
 *           return this
 *       }
 *       
 *       // 단일 primitive 래핑
 *       @JvmInline
 *       value class GoodId(val value: String)
 * 
 * 17. 컴파일러 최적화
 *     Inline Functions:
 *       - Dead code elimination
 *       - 상수 폴딩
 *       - 제어 흐름 최적화
 *     
 *     Value Classes:
 *       - Unboxing 최적화
 *       - Escape analysis
 *       - Stack allocation (가능한 경우)
 * 
 * 18. 디버깅 팁
 *     Inline Functions:
 *       - 스택 트레이스가 다를 수 있음
 *       - 브레이크포인트 동작 주의
 *       - @Suppress("NOTHING_TO_INLINE") for debugging
 *     
 *     Value Classes:
 *       - toString() 구현 권장
 *       - 디버거에서 underlying 값 표시
 *       - Logging 시 타입 정보 포함
 * 
 * 19. 베스트 프랙티스
 *     ✓ Inline Functions:
 *       - 작고 자주 호출되는 함수에만 사용
 *       - Reified가 필요한 경우 적극 활용
 *       - 성능 측정으로 효과 검증
 *       - Public API는 신중하게 결정
 *     
 *     ✓ Value Classes:
 *       - 도메인 타입에 적극 활용
 *       - 단일 primitive 값만 래핑
 *       - 불변성 유지
 *       - 초기화 블록에서 검증
 *       - Boxing 발생 케이스 문서화
 * 
 * 20. 요약
 *     Inline Functions:
 *       - Zero-cost 고차 함수
 *       - Reified generics 가능
 *       - 작은 유틸리티에 적합
 *     
 *     Value Classes:
 *       - Zero-cost 타입 안전성
 *       - Domain modeling에 완벽
 *       - Boxing 주의 필요
 *     
 *     함께 사용:
 *       @JvmInline value class UserId(val value: String)
 *       
 *       inline fun <reified T> parseId(str: String): T? {
 *           return when (T::class) {
 *               UserId::class -> UserId(str) as? T
 *               else -> null
 *           }
 *       }
 */

object InlineValueClassGuide {
    const val GUIDE_INFO = """
        Zero-Cost Abstractions in Kotlin
        
        핵심 개념:
        - Inline Functions: 호출 오버헤드 제거
        - Value Classes: 래퍼 객체 할당 제거
        - 타입 안전성 + 성능 최적화
        - Reified Generics: 타입 소거 해결
        
        사용 시기:
        - Inline: 작은 고차 함수, reified 필요
        - Value Class: 도메인 타입, ID, 단위
        
        주의사항:
        - Inline: 바이트코드 크기 증가
        - Value Class: Boxing 발생 케이스
        
        출처: https://carrion.dev/en/posts/kotlin-inline-functions-value-classes/
    """
    
    const val INLINE_FUNCTIONS = """
        === Inline Functions ===
        
        1. 개념:
           - 함수 본문을 호출 지점에 복사
           - 함수 호출 오버헤드 제거
           - 람다 객체 할당 제거
        
        2. 기본 문법:
           inline fun <T> measure(block: () -> T): T {
               val start = System.currentTimeMillis()
               try {
                   return block()
               } finally {
                   println("Elapsed: ${'$'}{System.currentTimeMillis() - start}ms")
               }
           }
        
        3. 수정자:
           - noinline: 특정 람다만 인라인 제외
           - crossinline: non-local return 금지
        
        4. Reified:
           inline fun <reified T> isInstance(any: Any?): Boolean {
               return any is T  // 런타임에 T 타입 접근 가능!
           }
    """
    
    const val VALUE_CLASSES = """
        === Value Classes ===
        
        1. 개념:
           - 단일 값 래핑
           - 컴파일 타임 타입 안전성
           - 런타임 오버헤드 최소화
        
        2. 기본 문법:
           @JvmInline
           value class UserId(val value: String) {
               init {
                   require(value.isNotBlank())
               }
           }
        
        3. 이점:
           - 타입 혼동 방지
           - 도메인 로직 캡슐화
           - 연산자 오버로딩 가능
        
        4. Boxing:
           val userId = UserId("123")  // No boxing
           val list = listOf(userId)   // Boxing
           val nullable: UserId? = userId  // Boxing
    """
    
    const val PRACTICAL_EXAMPLES = """
        === 실전 예제 ===
        
        1. 안전한 ID 타입:
           @JvmInline value class UserId(val value: String)
           @JvmInline value class ProductId(val value: String)
           @JvmInline value class OrderId(val value: String)
           
           fun link(userId: UserId, productId: ProductId) { }
           // link(productId, userId)  // 컴파일 에러!
        
        2. 돈과 단위:
           @JvmInline
           value class Money(val cents: Long) {
               operator fun plus(other: Money) =
                   Money(cents + other.cents)
           }
           
           @JvmInline
           value class Meters(val value: Double)
        
        3. 검증된 타입:
           @JvmInline
           value class Email(val value: String) {
               init { require("@" in value) }
           }
        
        4. Inline + Reified:
           inline fun <reified T> parseJson(json: String): T {
               return gson.fromJson(json, T::class.java)
           }
    """
    
    const val PERFORMANCE_TIPS = """
        === 성능 최적화 팁 ===
        
        1. Inline Functions:
           ✓ 작은 함수에만 사용 (< 10 lines)
           ✓ Hot path의 고차 함수
           ✓ Reified가 필요한 API
           ✗ 큰 함수 (바이트코드 증가)
           ✗ 드물게 호출되는 함수
        
        2. Value Classes:
           ✓ 단일 primitive 래핑
           ✓ 도메인 ID, 단위, 검증된 타입
           ✓ 연산자 오버로딩으로 편의성
           ✗ 복잡한 상태 저장
           ✗ 여러 프로퍼티 (data class 사용)
        
        3. Boxing 최소화:
           - 제네릭 대신 구체적 타입
           - Nullable 사용 최소화
           - 인터페이스 구현 신중히
        
        4. 측정:
           - 벤치마크로 실제 효과 검증
           - 바이트코드 크기 모니터링
           - 프로파일러로 hot path 확인
    """
    
    const val BEST_PRACTICES = """
        === 모범 사례 ===
        
        1. Inline Functions:
           @Suppress("NOTHING_TO_INLINE")  // 작은 함수
           inline fun <T> T.alsoIf(
               condition: Boolean,
               crossinline block: (T) -> Unit
           ): T {
               if (condition) block(this)
               return this
           }
        
        2. Value Classes:
           @JvmInline
           value class Money(val cents: Long) {
               init { require(cents >= 0) }
               
               operator fun plus(other: Money) =
                   Money(cents + other.cents)
               
               override fun toString() = "${'$'}${'$'}{cents / 100}.${'$'}{cents % 100}"
           }
        
        3. 조합:
           @JvmInline value class UserId(val value: String)
           
           inline fun <reified T : Any> parseId(str: String): T? {
               return when (T::class) {
                   UserId::class -> UserId(str) as? T
                   else -> null
               }
           }
        
        4. 문서화:
           /**
            * @JvmInline value class
            * - Represents a user identifier
            * - Boxing occurs in: List<UserId>, UserId?
            * - Zero-cost in most other cases
            */
    """
}
