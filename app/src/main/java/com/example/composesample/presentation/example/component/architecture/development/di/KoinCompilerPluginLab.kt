package com.example.composesample.presentation.example.component.architecture.development.di

import org.koin.core.module.Module
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import org.koin.plugin.module.dsl.factory
import org.koin.plugin.module.dsl.single

/**
 * Koin Compiler Plugin 예제의 실제 그래프 — 화면(KoinCompilerPluginExampleUI)과 JVM 단위 테스트(KoinCompilerPluginLabTest)가 함께 쓴다.
 * 앱 그래프(BaseApplication 의 startKoin)와 섞이지 않게 부를 때마다 격리된 koinApplication 을 만들고 닫는다.
 * `koinApplication { modules(...) }` 도 플러그인이 전체 그래프를 검사하는 진입점이다 — checkoutModule 에서 정의 하나를 지우면 빌드가 실패한다.
 */

// ==================== 1. 플러그인 DSL — 생성자는 컴파일러가 채운다 ====================

class OrderRepository
class CartClock
class PriceCalculator(val clock: CartClock)
class CheckoutService(val repository: OrderRepository, val calculator: PriceCalculator)
class ReceiptFormatter
class LabCache

/** cache 는 nullable — 정의가 없으면 null. maxRetries 는 기본값 — 정의를 찾지 않고 기본값을 쓴다(skipDefaultValues 기본 true). */
class RetryPolicy(val cache: LabCache?, val maxRetries: Int = 3)

/** single<T>() · factory<T>() 는 org.koin.plugin.module.dsl 의 것 — 플러그인이 컴파일 중에 생성자 호출 람다로 바꾼다. */
val checkoutModule: Module = module {
    single<OrderRepository>()
    single<CartClock>()
    single<PriceCalculator>()
    single<CheckoutService>()
    factory<ReceiptFormatter>()
    single<RetryPolicy>()
}

data class GraphReport(
    val serviceRepositoryIsShared: Boolean,
    val calculatorClockIsShared: Boolean,
    val factoryGivesNewInstance: Boolean,
    val retryCacheIsNull: Boolean,
    val retryMaxRetries: Int
)

fun resolveCheckoutGraph(): GraphReport {
    val app = koinApplication { modules(checkoutModule) }
    try {
        val koin = app.koin
        val service = koin.get<CheckoutService>()
        val retry = koin.get<RetryPolicy>()
        return GraphReport(
            serviceRepositoryIsShared = service.repository === koin.get<OrderRepository>(),
            calculatorClockIsShared = service.calculator.clock === koin.get<CartClock>(),
            factoryGivesNewInstance = koin.get<ReceiptFormatter>() !== koin.get<ReceiptFormatter>(),
            retryCacheIsNull = retry.cache == null,
            retryMaxRetries = retry.maxRetries
        )
    } finally {
        app.close()
    }
}

// ==================== 3. 빌드는 통과하고 실행 중에 터지는 것 ====================

// 실행 중에 고르는 모듈 목록(`modules(if (flag) a else b)`)은 여기 두지 않는다 — 컴파일 단위에 그런 진입점이 하나라도 있으면
// 플러그인이 KOIN-W003 경고를 내고 다른 파일의 진입점까지 D001 · D002 검사를 끈다(1.2.2 측정: 임시 파일에 하나를 두자 checkoutModule 의
// single<CartClock>() 이나 apiModule 의 Gson 정의를 지워도 빌드가 통과했다). 그 결과는 화면과 가이드에 측정값으로만 적는다.
enum class RuntimeGap(val label: String) {
    QUALIFIER("한정자 불일치"),
    PARAMETERS("parametersOf 누락")
}

class RemoteClient(val cache: LabCache)
class OrderLookup(val orderId: String)

/** 정의는 named("local"), 요청은 named("remote") — 손으로 쓴 람다 안의 한정자는 빌드가 대조하지 않는다(측정). */
val qualifierModule: Module = module {
    single(named("local")) { LabCache() }
    single { RemoteClient(get(named("remote"))) }
}

/** 실행 시 매개변수를 받는 정의 — 부르는 쪽이 parametersOf 를 빠뜨려도 빌드는 모른다. */
val lookupModule: Module = module {
    factory { (orderId: String) -> OrderLookup(orderId) }
}

data class GapResult(val wrong: String, val right: String)

/** 잘못된 호출과 바른 호출을 같은 격리 그래프에서 나란히 실행한다. */
fun runGap(gap: RuntimeGap): GapResult = when (gap) {
    RuntimeGap.QUALIFIER -> {
        val app = koinApplication { modules(qualifierModule) }
        try {
            GapResult(
                wrong = outcomeOf { app.koin.get<RemoteClient>() },
                right = outcomeOf { app.koin.get<LabCache>(named("local")) }
            )
        } finally {
            app.close()
        }
    }
    RuntimeGap.PARAMETERS -> {
        val app = koinApplication { modules(lookupModule) }
        try {
            GapResult(
                wrong = outcomeOf { app.koin.get<OrderLookup>() },
                right = outcomeOf { "orderId=" + app.koin.get<OrderLookup> { parametersOf("A-1001") }.orderId }
            )
        } finally {
            app.close()
        }
    }
}

/** 성공이면 결과 문자열, 실패면 예외 사슬(바깥 ← 원인)과 가장 안쪽 메시지의 첫 줄(이 예제의 패키지 이름은 줄인다). */
private fun outcomeOf(block: () -> Any): String = try {
    "성공 · " + when (val value = block()) {
        is String -> value
        else -> value::class.simpleName
    }
} catch (e: Exception) {
    val chain = generateSequence<Throwable>(e) { it.cause }.toList()
    val names = chain.joinToString(" ← ") { it::class.simpleName.orEmpty() }
    val message = chain.last().message.orEmpty().lineSequence().first()
        .replace(LAB_PACKAGE, "")
        .take(MESSAGE_LIMIT)
    "실패 · $names\n  $message"
}

private val LAB_PACKAGE = OrderRepository::class.java.name.substringBeforeLast('.') + "."
private const val MESSAGE_LIMIT = 160
