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
 * - 스티커는 비율 좌표(0..1)로 시작해 어떤 화면 크기에도 대응하고, 드래그 후에는 픽셀 좌표로 전환(Float.NaN 체크)
 */
