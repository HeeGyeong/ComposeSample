package com.example.composesample.presentation.example.component.interaction.sticker

/**
 * StickerCanvasExampleUI 참고 자료
 *
 * - 출처: https://aditlal.dev/building-stickerexplode-part-1-gestures-physics-and-making-stickers-feel-real/
 *
 * 핵심 개념:
 * - detectTransformGestures로 드래그(pan)·핀치 줌(zoom 0.5x~3.0x)·회전(rotation)을 동시에 처리하고,
 *   detectTapGestures로 탭(zIndex 증가)·더블 탭(스프링 바운시 2x 줌 토글)을 별도 처리
 * - 필오프(Peel-off) 효과: 드래그 시작 시 scale 1.0→1.1, shadow elevation 4dp→16dp로 증가시켜
 *   스티커를 들어올리는 느낌을 주고, 놓으면 spring(dampingRatio = DampingRatioMediumBouncy)으로 복귀
 * - Die-cut 렌더링: 흰색 배경 + RoundedCornerShape + clip으로 실제 비닐 스티커 같은 테두리 표현
 * - zIndex는 단조 증가 카운터로 관리하여 탭한 스티커가 항상 최상위에 오도록 함
 * - 스티커 위치는 처음부터 픽셀 좌표다 — 캔버스 중앙(onSizeChanged 로 받은 크기의 절반, 측정 전이면 200·300px)에
 *   ±60px 무작위 오프셋을 더해 놓고, 드래그는 그 값에 pan 을 더한다(화면 크기가 바뀌어도 비율로 다시 맞추지 않는다)
 */
