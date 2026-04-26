package com.example.composesample.presentation.example.component.ui.layout.modifier

/**
 * Modifier Order in Compose 예제 참고 자료
 *
 * - kt.academy 원문: https://kt.academy/article/compose_modifier_order
 * - 공식 문서: https://developer.android.com/develop/ui/compose/modifiers
 * - Compose 단계 (Composition / Layout / Drawing):
 *   https://developer.android.com/develop/ui/compose/phases
 *
 * 핵심 개념:
 * - Modifier 체인은 "왼쪽(위)에서 오른쪽(아래)" 순서로 적용된다.
 * - Layout phase에서 영향을 주는 modifier(padding, size, offset)는 순서가 결과 크기/위치를 결정.
 *   - padding → background : padding 영역은 배경 밖 (배경이 작아짐)
 *   - background → padding : padding 영역도 배경 안 (배경이 커짐)
 * - Drawing phase에 영향을 주는 modifier(background, border, clip, shadow)도 순서대로 그려짐.
 *   - clip → border : 테두리도 잘림
 *   - border → clip : 테두리는 그대로, 내용만 잘림
 * - clickable의 위치는 "터치 가능한 영역"을 결정.
 *   - clickable → padding : padding 영역은 클릭 안 됨 (클릭 영역이 작음)
 *   - padding → clickable : padding 영역도 클릭 됨 (클릭 영역이 큼)
 * - size 위치도 중요:
 *   - size → padding : 지정 size 안에서 padding 적용 (실제 콘텐츠 영역이 작아짐)
 *   - padding → size : 지정 size + padding (전체가 더 커짐)
 *
 * 디버깅 팁:
 * - Layout Inspector로 padding/border 영역을 시각화.
 * - 의도와 다른 결과가 나오면 modifier 순서를 한 줄씩 뒤집어 비교.
 */
