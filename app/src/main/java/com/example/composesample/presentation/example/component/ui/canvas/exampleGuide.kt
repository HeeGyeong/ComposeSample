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
 * - Butterfly:      r = e^(cosθ) - 2·cos(4θ) - sin⁵(θ/12), θ ∈ [0, 24π]  — Temple H. Fay 나비 곡선
 *
 * ### 애니메이션 패턴
 * - `rememberInfiniteTransition` + `animateFloat` (0→1, LinearEasing)
 * - 사전 계산된 곡선 점(List<Offset>)을 `remember`로 캐싱
 * - 헤드 인덱스 기준 꼬리(trail) 방식: 뒤로 갈수록 alpha/radius 감소
 * - `Canvas.drawCircle` 반복 호출로 점 기반 트레일 렌더링
 *
 * ## MonthPickerDialExampleUI (Airbnb ChromaDial — Month Picker)
 * - 출처: https://www.sinasamaki.com/month-picker-dial/
 *
 * ### 핵심 개념
 * - 폴라 좌표: `x = cx + r·cos(θ)`, `y = cy + r·sin(θ)`로 12개 월 라벨을 원주에 등간격 배치 (30° 단위)
 * - atan2(dy, dx): 중심→터치 벡터의 각도(-π~π)를 구해 드래그 방향 추적
 * - 각도 delta 누적: 이전 프레임과의 차이(±180° 경계 보정 포함)를 rotation에 더해 회전
 * - 스냅 애니메이션: 드래그 종료 시 `roundToInt() * 30f`로 가장 가까운 월로 `animateTo` + spring(MediumBouncy)
 * - 선택 동기화: `snapshotFlow { rotation.value }.collectLatest`로 회전 변화를 선택된 월 인덱스로 변환
 *
 * ### 제스처 패턴
 * - `detectDragGestures { onDragStart, onDrag, onDragEnd }`
 * - 원형 회전은 `Animatable<Float>` + `snapTo` (드래그 중) / `animateTo` (스냅)로 구현
 */
