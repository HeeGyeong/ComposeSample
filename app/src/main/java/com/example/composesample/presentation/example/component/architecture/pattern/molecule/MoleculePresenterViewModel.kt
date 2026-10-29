package com.example.composesample.presentation.example.component.architecture.pattern.molecule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.AndroidUiDispatcher
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 같은 이벤트 흐름을 Flow 프리젠터와 Molecule 프리젠터에 동시에 흘려 두 모델을 나란히 보여 준다.
 * 실험 카드(방출 수 · 공유 · 시계 없음)는 매번 새 스코프에서 돌고 끝나면 취소된다.
 */
class MoleculePresenterViewModel : ViewModel() {

    private val events = MutableSharedFlow<CatalogEvent>(extraBufferCapacity = 16)

    val flowCatalog = FakeCatalog()
    val moleculeCatalog = FakeCatalog()
    private val flowPresenter = CatalogFlowPresenter(flowCatalog)
    private val moleculePresenter = CatalogMoleculePresenter(moleculeCatalog)

    val flowModel: StateFlow<CatalogModel> = flowPresenter.present(viewModelScope, events)

    // ContextClock 은 컨텍스트의 MonotonicFrameClock 으로 재구성 시점을 정한다 — viewModelScope 에는 시계가 없어
    // Molecule 의 AndroidUiDispatcher.Main(메인 스레드 디스패처 + Choreographer 프레임 시계)을 더한다
    val moleculeModel: StateFlow<CatalogModel> =
        viewModelScope.launchMolecule(RecompositionMode.ContextClock, AndroidUiDispatcher.Main) {
            moleculePresenter.present(events)
        }

    /** 생성 직후 .value — Flow 쪽은 stateIn 의 자리표시자, Molecule 쪽은 첫 컴포지션이 동기로 만든 값 */
    val flowFirst: String = describeCatalogModel(flowModel.value)
    val moleculeFirst: String = describeCatalogModel(moleculeModel.value)

    val flowComputations: StateFlow<Int> = flowPresenter.computations
    val moleculeCompositions: StateFlow<Int> = moleculePresenter.compositions

    private val _flowLog = MutableStateFlow<List<String>>(emptyList())
    val flowLog: StateFlow<List<String>> = _flowLog.asStateFlow()
    private val _moleculeLog = MutableStateFlow<List<String>>(emptyList())
    val moleculeLog: StateFlow<List<String>> = _moleculeLog.asStateFlow()

    private val _burstResults = MutableStateFlow<List<BurstResult>>(emptyList())
    val burstResults: StateFlow<List<BurstResult>> = _burstResults.asStateFlow()
    private val _sharingResults = MutableStateFlow<List<SharingResult>>(emptyList())
    val sharingResults: StateFlow<List<SharingResult>> = _sharingResults.asStateFlow()
    private val _clockResults = MutableStateFlow<List<MissingClockResult>>(emptyList())
    val clockResults: StateFlow<List<MissingClockResult>> = _clockResults.asStateFlow()
    private val _running = MutableStateFlow(false)
    val running: StateFlow<Boolean> = _running.asStateFlow()

    init {
        // 수집기가 본 값 그대로 기록한다(StateFlow 는 같은 값을 다시 내보내지 않는다)
        viewModelScope.launch { flowModel.collect { m -> _flowLog.update { (it + describeCatalogModel(m)).takeLast(LOG_SIZE) } } }
        viewModelScope.launch { moleculeModel.collect { m -> _moleculeLog.update { (it + describeCatalogModel(m)).takeLast(LOG_SIZE) } } }
    }

    fun onEvent(event: CatalogEvent) {
        events.tryEmit(event)
    }

    fun clearLogs() {
        _flowLog.value = emptyList()
        _moleculeLog.value = emptyList()
    }

    fun runBurstExperiments() = runExclusive {
        val results = mutableListOf<BurstResult>()
        for (pattern in SnapshotWritePattern.entries) {
            for (mode in MoleculeClockMode.entries) {
                results += runBurstExperiment(mode, pattern)
                _burstResults.value = results.toList()
            }
        }
    }

    fun runSharingExperiments() = runExclusive {
        _sharingResults.value = listOf(runSharingExperiment(cold = false))
        _sharingResults.value = _sharingResults.value + runSharingExperiment(cold = true)
    }

    fun runClockExperiments() = runExclusive {
        _clockResults.value = listOf(tryContextClock(withClock = false), tryContextClock(withClock = true))
    }

    private fun runExclusive(block: suspend () -> Unit) {
        if (_running.value) return
        _running.value = true
        viewModelScope.launch {
            try {
                block()
            } finally {
                _running.value = false
            }
        }
    }

    private companion object {
        const val LOG_SIZE = 8
    }
}
