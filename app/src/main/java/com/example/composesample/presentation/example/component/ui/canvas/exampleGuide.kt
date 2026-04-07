package com.example.composesample.presentation.example.component.ui.canvas

/**
 * Canvas 예제 참고 자료
 *
 * ## ComposeLoadersExampleUI (수학 곡선 기반 로딩 애니메이션)
 * - 출처: https://composeinternals.com/composeloaders
 *
 * ### 수학 곡선 종류
 * - Rose Curve:     r = cos(kθ)  — 극좌표 장미 곡선, k=홀수이면 k개 꽃잎
 * - Lissajous:      x = sin(at+δ), y = sin(bt)  — 위상 변화로 형태 변형
 * - Lemniscate:     (x²+y²)² = a²(x²-y²)  — 베르누이 무한대(∞) 곡선
 * - Spirograph:     Hypotrochoid, x = (R-r)cos(t)+d·cos((R-r)/r·t)
 * - Cardioid:       r = a(1 - cosθ)  — 하트 모양 곡선
 *
 * ### 애니메이션 패턴
 * - `rememberInfiniteTransition` + `animateFloat` (0→1, LinearEasing)
 * - 사전 계산된 곡선 점(List<Offset>)을 `remember`로 캐싱
 * - 헤드 인덱스 기준 꼬리(trail) 방식: 뒤로 갈수록 alpha/radius 감소
 * - `Canvas.drawCircle` 반복 호출로 점 기반 트레일 렌더링
 */
