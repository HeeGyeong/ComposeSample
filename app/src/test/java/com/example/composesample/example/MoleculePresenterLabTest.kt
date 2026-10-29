package com.example.composesample.example

import com.example.composesample.presentation.example.component.architecture.pattern.molecule.BurstResult
import com.example.composesample.presentation.example.component.architecture.pattern.molecule.CatalogEvent
import com.example.composesample.presentation.example.component.architecture.pattern.molecule.CatalogFlowPresenter
import com.example.composesample.presentation.example.component.architecture.pattern.molecule.CatalogModel
import com.example.composesample.presentation.example.component.architecture.pattern.molecule.CatalogProduct
import com.example.composesample.presentation.example.component.architecture.pattern.molecule.FakeCatalog
import com.example.composesample.presentation.example.component.architecture.pattern.molecule.MoleculeClockMode
import com.example.composesample.presentation.example.component.architecture.pattern.molecule.SnapshotWritePattern
import com.example.composesample.presentation.example.component.architecture.pattern.molecule.catalogModelOf
import com.example.composesample.presentation.example.component.architecture.pattern.molecule.describeBurst
import com.example.composesample.presentation.example.component.architecture.pattern.molecule.describeCatalogModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

/**
 * Molecule 프리젠터 예제 — 모델 규칙 · Flow 프리젠터 · 결과 요약 문장
 *
 * Molecule 프리젠터 자체(moleculeFlow + Turbine)는 여기서 돌리지 않는다 — Android 모듈의 JVM 단위 테스트에서는
 * Compose runtime(runtime-android)이 android.os.Trace.beginSection 을 불러 "Method ... not mocked" 로 실패한다(실측).
 * Molecule 전이·RecompositionMode·시계 없는 ContextClock 은 예제 화면에서 기기로 잰다.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MoleculePresenterLabTest {

    @Test
    fun `재고 필터는 결과를 거르기만 하고 결과가 없으면 검색 중이다`() {
        val results = listOf(CatalogProduct("A", inStock = true), CatalogProduct("B", inStock = false))

        assertEquals(CatalogModel("q", inStockOnly = false, loading = true, items = emptyList()), catalogModelOf("q", false, null))
        assertEquals(listOf("A", "B"), catalogModelOf("q", false, results).items.map { it.name })
        assertEquals(listOf("A"), catalogModelOf("q", true, results).items.map { it.name })
        assertEquals("'q' · 재고만 · 1개", describeCatalogModel(catalogModelOf("q", true, results)))
        assertEquals("(전체) · 검색 중", describeCatalogModel(catalogModelOf("", false, null)))
    }

    @Test
    fun `Flow 프리젠터는 같은 이벤트에 같은 마지막 모델을 만들고 stateIn 자리표시자에서 시작한다`() {
        runTest {
            val catalog = FakeCatalog(latencyMs = 100L)
            val events = MutableSharedFlow<CatalogEvent>(extraBufferCapacity = 8)
            val models = CatalogFlowPresenter(catalog).present(backgroundScope, events)

            // then: 수집기가 처음 보는 값은 stateIn 의 초기값
            assertEquals(CatalogModel.Placeholder, models.value)
            // backgroundScope 작업은 advanceUntilIdle 이 기다리지 않으므로 시간을 직접 넘긴다
            advanceTimeBy(1_000L)
            runCurrent()
            assertEquals("(전체) · 8개", describeCatalogModel(models.value))

            events.emit(CatalogEvent.QueryChanged("pro"))
            events.emit(CatalogEvent.ToggleInStock)
            advanceTimeBy(1_000L)
            runCurrent()
            assertEquals("'pro' · 재고만 · 3개", describeCatalogModel(models.value))
            assertEquals(2, catalog.searches.value)
        }
    }

    @Test
    fun `방출 요약은 첫 값을 빼고 센다`() {
        val result = BurstResult(MoleculeClockMode.Immediate, SnapshotWritePattern.Burst, listOf(0, 4, 10))
        assertEquals("방출 2 · 4→10", describeBurst(result))
        assertEquals("방출 0 · -", describeBurst(result.copy(values = listOf(0))))
    }
}
