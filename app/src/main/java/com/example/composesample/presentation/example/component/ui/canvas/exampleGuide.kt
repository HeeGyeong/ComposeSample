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
 * ## ParticleEmitterExampleUI (Canvas 기반 물리 파티클 시스템)
 * - 영감: https://github.com/PiotrPrus/ParticleEmitter (자체 구현은 외부 라이브러리 미사용)
 *
 * ### 핵심 개념
 * - 게임 루프: `LaunchedEffect(Unit) { while(true) { withFrameNanos { ... } } }`
 *   → frame 간 dt(초) 측정으로 프레임 레이트 변동에 무관한 일정한 체감 속도 보장
 * - 시간 기반 적분: v += g·dt, p += v·dt — 단순 Euler integration
 * - 중력: GRAVITY(폭죽 강함) / GRAVITY_LIGHT(별가루 약함)
 * - 공기 저항(drag): v *= (1 - DRAG·dt) — 별가루의 부드러운 감쇠
 * - 수명/페이드: life -= dt/maxLife, alpha = life — life ≤ 0이면 listIterator로 제거
 * - 입력: `pointerInput { detectTapGestures(onPress=...) }`로 탭 위치에서 emit
 *
 * ### Canvas vs Layout 트레이드오프
 * - Canvas: 모든 파티클 단일 DrawScope, drawCircle 반복 호출 — 측정/배치 비용 0
 *   → 수백~수천 파티클까지 부드럽게 동작 (선택)
 * - Layout(Box+offset Modifier): 파티클별 Composable — 100개만 넘어도 리컴포지션 비용 급증
 *   → 시각 효과에는 부적합. 디버그/접근성/개별 입력 처리에만 의미
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
 *
 * ## FreehandDrawingExampleUI (자유 곡선 드로잉 / 서명 캔버스)
 * - 영감: DrawBox Goes Multiplatform (Akshay Nandwana, https://ak1.io) — Android Weekly #733
 *   (원문은 CMP 드로잉 라이브러리 + MVI 재설계. 본 예제는 외부 라이브러리 없이 핵심 개념만 자체 구현)
 * - DrawBox 라이브러리: https://github.com/akshay2211/DrawBox
 *
 * ### 핵심 개념
 * - 입력: `pointerInput { detectDragGestures(onDragStart, onDrag, onDragEnd, onDragCancel) }`
 *   → onDrag 의 `change.position` 을 점(Offset)으로 누적해 하나의 스트로크를 구성
 * - 렌더링: 점 리스트를 `Path.moveTo/lineTo` 로 잇고 `drawPath(style = Stroke(cap=Round, join=Round))`
 *   → 더 매끄럽게 하려면 인접 점의 중점으로 `quadraticBezierTo` 적용 가능
 * - 점이 1개뿐(드래그 슬롭 미달)이면 `drawCircle` 로 점 표현
 *
 * ### MVI 단방향 상태 흐름 (DrawBox 아키텍처 차용)
 * - `DrawIntent`(StartStroke/Drag/EndStroke/Undo/Redo/Clear/SetColor/SetWidth) sealed interface
 * - 순수 `reduce(state, intent): DrawBoxState` 리듀서 — 부수효과 없이 다음 상태만 계산
 * - 상태 불변(immutable List) → Undo/Redo 는 strokes ↔ redoStack 리스트 이동만으로 안전하게 구현
 * - 새 스트로크 확정(EndStroke) 시 redoStack 무효화(표준 undo/redo 시맨틱)
 *
 * ### PNG 내보내기 (개념)
 * - `rememberGraphicsLayer()` + `drawWithContent { layer.record { drawContent() }; drawLayer(layer) }`
 * - 저장: `layer.toImageBitmap().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, out)`
 *
 * ## MotionBlurExampleUI (스피닝 휠 모션 블러)
 * - 출처: https://proandroiddev.com/motion-blur-for-a-spinning-wheel-in-jetpack-compose-368c1647224d
 *
 * ### 핵심 개념
 * - Ghost Frames 기법: 동일 휠을 여러 각도로 반투명 겹쳐 그려 모션 블러 흉내 (`withTransform { rotate(...) }` + alpha 감쇠) — API 레벨 제한 없음
 * - BlurMaskFilter: `Paint.asFrameworkPaint().maskFilter = BlurMaskFilter(radius, Blur.NORMAL/SOLID/INNER/OUTER)`로 Paint 레벨 블러
 * - RenderEffect(API 31+): `Modifier.graphicsLayer { renderEffect = BlurEffect(radiusX, radiusY=0f) }`로 GPU 가속 축 방향 블러
 * - 속도 연동: 각속도(angularVelocity)에 비례해 블러 반경·고스트 개수를 동적 조절 → 빠르게 회전할수록 강한 블러
 *
 * ## CanvasShapesExampleUI (Canvas 도형·애니메이션 기초)
 * - 출처: https://proandroiddev.com/compose-canvas-understanding-shapes-and-animations-for-beginners-255653149393
 *
 * ### 핵심 개념
 * - DrawScope 기본 도형: drawCircle/drawRect/drawRoundRect/drawLine/drawArc/drawPath, dp.toPx() 변환 필수
 * - Brush 그라디언트: linearGradient/radialGradient/sweepGradient
 * - Transform: rotate/scale/translate + withTransform 조합, BlendMode·alpha로 겹침 효과 제어
 * - 애니메이션 결합: rememberInfiniteTransition/animateFloatAsState, 복잡한 Path는 drawWithCache로 캐싱 최적화
 *
 * ## DialComponentExampleUI (Canvas 기반 다이얼/원형 슬라이더)
 * - 출처: https://www.sinasamaki.com/how-to-create-dials-in-jetpack-compose/
 *   (원본은 ChromaDial 라이브러리 소개, 본 예제는 외부 라이브러리 없이 Canvas로 직접 구현)
 *
 * ### 핵심 개념
 * - 각도 ↔ 좌표 변환: `x = cx + r·cos(θ)`, `y = cy + r·sin(θ)` (12시 방향 기준 θ = degree - 90)
 * - 터치 → 각도: `atan2(dy, dx)`로 계산 후 `(degrees + 90 + 360) % 360` 보정
 * - startDegrees/sweepDegrees로 범위 제한(예: 270도 부채꼴), sweepDegrees > 360이면 멀티턴 다이얼
 * - interval 스냅: 0=스무스, N이면 가장 가까운 N배수로 반올림 스냅
 */
