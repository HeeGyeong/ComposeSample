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
 * ## WaveformCanvasExampleUI (실시간 파형 렌더러 — ECG/PPG)
 * - Compose 단계별 상태 읽기: https://developer.android.com/develop/ui/compose/performance/bestpractices#defer-reads
 * - withFrameNanos: https://developer.android.com/reference/kotlin/androidx/compose/runtime/package-summary#withFrameNanos(kotlin.Function1)
 * - ECG 파형(PQRST 복합파) 개요: https://en.wikipedia.org/wiki/Electrocardiography#Theory
 * - PPG(광용적맥파) 개요: https://en.wikipedia.org/wiki/Photoplethysmogram
 *   (신호는 실제 센서 데이터가 아니라 가우시안 합으로 합성한 시뮬레이션. 외부 라이브러리 미사용)
 *
 * ### 고정 샘플레이트 ↔ 가변 프레임레이트 분리
 * - 센서는 250Hz 로 일정하게, 화면은 60/90/120Hz 로 제각각 → "프레임당 1샘플"로 밀면 기기마다 시간축이 달라짐
 * - `carry += dt * SAMPLE_RATE` → `count = carry.toInt()` → `carry -= count` (소수부 이월)
 *   60fps·250Hz 면 프레임당 4.16 샘플 → 4개 넣고 0.16 은 다음 프레임으로
 * - `dt` 가 비정상적으로 크면(백그라운드 복귀 등) 해당 프레임은 폐기, 밀린 샘플은 상한(MAX_SAMPLES_PER_FRAME)으로 절단
 *
 * ### 링 버퍼(FloatArray + head 순환)
 * - `samples[head] = v; head = (head + 1) % capacity` — 쓰기 O(1), 프레임당 할당 0
 * - 스냅샷 리스트(mutableStateListOf)를 쓰지 않는 이유: 초당 250회 스냅샷 쓰기가 발생하지만
 *   화면은 프레임당 1회만 갱신하면 되므로, 갱신 신호는 head 인덱스 상태 하나로 충분
 * - Path 도 `remember { Path() }` 로 재사용하고 매 드로우 `reset()`
 *
 * ### 드로우 단계 상태 읽기 (defer reads)
 * - `Canvas { }` 의 람다 안에서 상태를 읽으면 드로우 단계 구독이 되어 **리컴포지션 없이 재드로우만** 무효화
 * - 같은 상태를 컴포저블 본문에서 읽으면 프레임마다 컴포지션 전체가 재실행 → 예제에서 카운터로 실측 대조
 * - 화면에 띄우는 지표 텍스트는 0.5초 주기로만 갱신(매 프레임 갱신 시 텍스트 한 줄이 캔버스보다 비쌀 수 있음)
 * - 상태 소유자(WaveformController)를 LazyColumn 아이템이 아닌 최상위 컴포저블에 두어, 스크롤로 카드가 폐기돼도 신호 연속성 유지
 *
 * ### 렌더 모드
 * - Sweep(환자 모니터): 버퍼 인덱스 = 화면 x 좌표. 커서 앞 GAP 구간만 비워 '지우개' 표현 → 이동 비용 0
 * - Scroll(오실로스코프): 나이(age) 순으로 이어 최신 샘플을 오른쪽 끝에 배치 → 시간 순서가 직관적
 * - 둘 다 같은 버퍼를 읽으며, 차이는 "인덱스로 읽느냐 / 나이로 읽느냐"뿐
 *
 * ### 신호 합성
 * - 한 박동 내 위치 phase(0~1)를 입력으로 받는 순수 함수 — 가우시안 `amp·exp(-½((p-c)/w)²)` 의 합
 * - ECG: P(0.18) · Q(0.30) · R(0.33 스파이크) · S(0.37) · T(0.56)
 * - PPG: 수축기 피크(0.26) + 중복맥(dicrotic notch) 봉우리(0.50)
 * - phase 를 `phase += (1/SAMPLE_RATE)/(60/bpm)` 로 누적 → BPM 을 바꿔도 파형 불연속 없음
 *   (`t % period` 방식은 주기가 바뀌는 순간 위상이 튄다)
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
