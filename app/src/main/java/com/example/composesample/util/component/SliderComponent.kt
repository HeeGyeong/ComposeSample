package com.example.composesample.util.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

/**
 * 바깥 값이 정답인 '제어형' 슬라이더 — M3 1.5 에서 deprecated 된 `Slider(value, onValueChange, …)` 의 대체(DEPRECATION-113-01).
 *
 * 왜 래퍼인가:
 * - IDE 가 제안하는 치환 `Slider(state = rememberSliderState(value, …), onValueChange = onValueChange)` 는
 *   rememberSliderState 가 value 를 **처음 한 번만** 읽는다(rememberSaveable). 상태 기반 Slider 는 드래그 값을
 *   onValueChange 로만 알리고 state 를 직접 바꾸지 않으므로, 그대로 바꾸면 썸이 움직이지 않고 리셋 버튼 ·
 *   플레이어 위치처럼 바깥에서 바꾼 값도 반영되지 않는다.
 * - 본문은 material3 1.5.0 의 deprecated Slider 본문과 같다 — `remember(steps, valueRange)` 로 상태를 만들고
 *   매 컴포지션 `state.value = value` 로 바깥 값을 밀어 넣는다(범위 · 눈금 수가 바뀌면 상태를 새로 만든다).
 *   그래서 이 래퍼로 바꾼 화면은 바꾸기 전과 동작이 같다. 값을 바꾸는 쪽은 여전히 호출자의 onValueChange.
 * - SliderState 를 직접 다루는 모양(드래그 중 값 · isDragging 등)이 필요하면 래퍼 대신 Slider(state, …) 를 쓴다.
 */
@Composable
fun ValueSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
    colors: SliderColors = SliderDefaults.colors(),
    interactionSource: MutableInteractionSource? = null,
) {
    val state = remember(steps, valueRange) { SliderState(value, steps, valueRange) }
    // 바깥 값이 정답 — 같은 값이면 mutableFloatState 가 무효화를 만들지 않는다
    state.value = value
    val source = interactionSource ?: remember { MutableInteractionSource() }

    Slider(
        state = state,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        onValueChangeFinished = onValueChangeFinished,
        colors = colors,
        interactionSource = source,
        thumb = {
            SliderDefaults.Thumb(interactionSource = source, colors = colors, enabled = enabled)
        },
        track = { sliderState ->
            SliderDefaults.Track(colors = colors, enabled = enabled, sliderState = sliderState)
        },
    )
}
