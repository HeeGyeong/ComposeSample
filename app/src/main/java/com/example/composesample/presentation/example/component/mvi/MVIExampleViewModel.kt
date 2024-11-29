package com.example.composesample.presentation.example.component.mvi

import androidx.lifecycle.ViewModel
import com.example.composesample.domain.usecase.FetchDataUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

sealed class MVIExampleEvent {
    object ButtonClicked1 : MVIExampleEvent()
    object ButtonClicked2 : MVIExampleEvent()
    object ButtonClicked3 : MVIExampleEvent()
    object ButtonClicked4 : MVIExampleEvent()
    object FetchData : MVIExampleEvent()
}

data class MVIExampleState(
    val items: List<String> = listOf("Item 1", "Item 2", "Item 3", "Item 4"),
    val apiData: String = ""
)

class MVIExampleViewModel(private val fetchDataUseCase: FetchDataUseCase) : ViewModel() {
    private val _state = MutableStateFlow(MVIExampleState())
    val state: StateFlow<MVIExampleState> = _state

    fun onEvent(event: MVIExampleEvent) {
        val updatedItems = _state.value.items.toMutableList()
        when (event) {
            is MVIExampleEvent.ButtonClicked1 -> {
                updatedItems[0] = "Item 1 Changed!"
            }
            is MVIExampleEvent.ButtonClicked2 -> {
                updatedItems[1] = "Item 2 Changed!"
            }
            is MVIExampleEvent.ButtonClicked3 -> {
                updatedItems[2] = "Item 3 Changed!"
            }
            is MVIExampleEvent.ButtonClicked4 -> {
                updatedItems[3] = "Item 4 Changed!"
            }
            is MVIExampleEvent.FetchData -> {
                val data = fetchDataUseCase.execute()
                _state.value = _state.value.copy(apiData = data)
            }
        }
        _state.value = _state.value.copy(items = updatedItems)
    }
} 