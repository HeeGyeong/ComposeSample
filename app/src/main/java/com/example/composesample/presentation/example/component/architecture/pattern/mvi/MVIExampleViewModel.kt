package com.example.composesample.presentation.example.component.architecture.pattern.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.useCase.FetchDataUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

// MVI 패턴의 Event sealed class - UI에서 발생하는 모든 이벤트 정의
sealed class MVIExampleEvent {
    object ButtonClicked1 : MVIExampleEvent()
    object ButtonClicked2 : MVIExampleEvent()
    object ButtonClicked3 : MVIExampleEvent()
    object ButtonClicked4 : MVIExampleEvent()
    object FetchData : MVIExampleEvent()
    object NavigateBack : MVIExampleEvent()
}

// Side Effect를 처리하기 위한 sealed class - 토스트, 에러, 네비게이션 등 일회성 이벤트
sealed class MVISideEffect {
    data class ShowToast(val message: String) : MVISideEffect()
    data class ShowError(val error: String) : MVISideEffect()
    object NavigateBack : MVISideEffect()
}

// UI 상태를 나타내는 데이터 클래스
data class MVIExampleState(
    val items: List<String> = listOf("Item 1", "Item 2", "Item 3", "Item 4"),
    val apiData: String = "",
    val counter: Int = 0
)

class MVIExampleViewModel(private val fetchDataUseCase: FetchDataUseCase) : ViewModel() {
    private val _state = MutableStateFlow(MVIExampleState())
    val state: StateFlow<MVIExampleState> = _state

    // Side Effect를 위한 Channel - 일회성 이벤트이기 때문에 Channel 사용
    private val _sideEffect = Channel<MVISideEffect>()
    val sideEffect = _sideEffect.receiveAsFlow()

    // UI로부터 전달받은 이벤트 처리
    fun onEvent(event: MVIExampleEvent) {
        when (event) {
            is MVIExampleEvent.ButtonClicked1 -> updateItem(0, "Item 1 Changed!")
            is MVIExampleEvent.ButtonClicked2 -> updateItem(1, "Item 2 Changed!")
            is MVIExampleEvent.ButtonClicked3 -> updateItem(2, "Item 3 Changed!")
            is MVIExampleEvent.ButtonClicked4 -> updateItem(3, "Item 4 Changed!")
            is MVIExampleEvent.FetchData -> fetchData()
            is MVIExampleEvent.NavigateBack -> sendEffect(MVISideEffect.NavigateBack)
        }
    }

    // 특정 인덱스의 아이템 값을 업데이트
    private fun updateItem(index: Int, newValue: String) {
        val updatedItems = state.value.items.toMutableList().apply {
            this[index] = newValue
        }
        reduce { it.copy(items = updatedItems) }
        sendEffect(MVISideEffect.ShowToast("아이템 $index 변경됨"))
    }

    // API 데이터 요청 및 처리
    private fun fetchData() {
        viewModelScope.launch {
            try {
                val data = fetchDataUseCase.execute()  // API 호출
                sendEffect(MVISideEffect.ShowToast("API 데이터 변경됨 !"))
                reduce {
                    it.copy(
                        apiData = "$data - Count: ${it.counter + 1}",
                        counter = it.counter + 1
                    )
                }
            } catch (e: Exception) {
                sendEffect(MVISideEffect.ShowError(e.message ?: "알 수 없는 에러"))
            }
        }
    }

    // 상태 업데이트를 위한 헬퍼 함수
    private fun reduce(reducer: (MVIExampleState) -> MVIExampleState) {
        _state.value = reducer(_state.value)
    }

    // Side Effect 전달을 위한 헬퍼 함수
    private fun sendEffect(effect: MVISideEffect) {
        viewModelScope.launch {
            _sideEffect.send(effect)  // Channel을 통해 effect 전달
        }
    }
}