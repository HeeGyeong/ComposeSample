package com.example.composesample.presentation.example.component.system.platform.display

/**
 * System/Platform/Display 예제 참고 자료
 *
 * ## DisplayRefreshRateExampleUI (디스플레이 주사율 제어 & 프레임 간격 측정)
 * - Display.Mode: https://developer.android.com/reference/android/view/Display.Mode
 * - WindowManager.LayoutParams.preferredDisplayModeId:
 *   https://developer.android.com/reference/android/view/WindowManager.LayoutParams#preferredDisplayModeId
 * - 프레임 레이트 가이드(Surface.setFrameRate): https://developer.android.com/media/optimize/performance/frame-rate
 * - 적응형 주사율(Android 15, View.requestedFrameRate):
 *   https://developer.android.com/develop/ui/views/animations/adaptive-refresh-rate
 * - Window.setSustainedPerformanceMode:
 *   https://developer.android.com/reference/android/view/Window#setSustainedPerformanceMode(boolean)
 * - Compose preferredFrameRate 소스: https://dl.google.com/android/maven2/androidx/compose/ui/ui-android/1.12.1/ui-android-1.12.1-sources.jar
 *   (commonMain/androidx/compose/ui/FrameRate.kt · FrameRateCategory.kt · androidMain/.../AndroidComposeView.android.kt)
 * - 출처(Android Weekly #747): BoatBrawl, One Kotlin codebase, five platforms, 60 fps
 *   https://boatbrawl.io/blog/smooth-3d-game-kmp-cmp (§0 측정 방법 · §2 "Android said 60 fps while the game ran at 39")
 *
 * 핵심 개념:
 * - 디스플레이는 여러 모드(해상도 + 주사율)를 지원한다. Display.supportedModes(API 23) 로 목록을,
 *   Display.mode 로 지금 모드를, Display.refreshRate 로 지금 주사율을 읽는다.
 *   Mode.alternativeRefreshRates(API 31)는 "끊김 없이(seamless) 전환할 수 있는 다른 주사율"이다.
 * - 앱이 주사율을 요청하는 길은 세 층이다. 어느 것이든 요청(투표)일 뿐이고 최종 결정은 시스템이 한다.
 *   ① Window: LayoutParams.preferredDisplayModeId(API 23, 0 = 요청 없음) · preferredRefreshRate(API 21)
 *   ② Surface: Surface.setFrameRate(API 30) — SurfaceView·게임·동영상
 *   ③ View: View.requestedFrameRate(API 35) — Compose preferredFrameRate 가 이 길로 간다
 * - 창 속성은 Activity 단위다. 예제처럼 여러 화면이 Activity 하나를 공유하면, 화면을 떠날 때 원래 값으로
 *   되돌리지 않는 한 다음 화면이 그 요청을 물려받는다(이 예제는 DisposableEffect 에서 원복).
 * - 요청이 실제로 반영됐는지는 DisplayManager.DisplayListener.onDisplayChanged 에서 Display.mode 를 다시 읽어 확인한다.
 *
 * Compose Modifier.preferredFrameRate (compose-ui 1.12.1 소스에서 확인):
 * - preferredFrameRate(Float, 0..360) / preferredFrameRate(FrameRateCategory) 두 가지. 실험 API 가 아니다.
 *   FrameRateCategory 는 Float value class — Default = NaN · Normal = -3f · High = -4f
 *   (View.REQUESTED_FRAME_RATE_CATEGORY_NORMAL/HIGH 와 같은 값).
 * - 구현은 graphicsLayer() + FrameRateModifierNode(DrawModifierNode). 노드는 그려질 때 레이어(OwnedLayer.frameRate)에
 *   값을 적어 두고, 투표(owner.voteFrameRate)는 그 레이어가 한다. AndroidComposeView.dispatchDraw 끝에서 그 프레임의
 *   투표를 합쳐 넘긴 뒤 초기화한다 → "그려지는 동안만 유효"하다는 KDoc 문장의 실체다.
 * - 합치는 규칙: 양수(숫자)는 가장 큰 값, 음수(카테고리)는 더 작은 값(High -4 가 Normal -3 을 이긴다).
 *   숫자는 AndroidComposeView 자신에, 카테고리는 Compose 가 붙인 1×1 숨은 자식 View 에 setRequestedFrameRate 한다.
 * - isArrEnabled = SDK_INT >= VANILLA_ICE_CREAM(35). API 35 미만에서는 투표 자체를 모으지 않아 완전히 무동작이다.
 * - 투표가 나가는 지점(GraphicsLayerOwnerLayer — API 23+ 의 기본 레이어): 숫자는 그 레이어가 다시 그려질 때
 *   (updateDisplayList, frameRate != 0)만, 그리고 레이어 위치가 바뀌면 move() 가 · 레이아웃 위치가 바뀌면
 *   NodeCoordinator 가 FrameRateCategory.High 를 자동 투표한다.
 * - 플랫폼 View.setRequestedFrameRate/getRequestedFrameRate 는 toolkitSetFrameRateReadOnly 플래그 뒤에 있다.
 *   플래그가 꺼진 기기에서는 setter 가 무시되고 getter 는 0 을 돌려준다(android-35 소스). NaN 이 읽히면 플래그는 켜져 있다.
 * - API 35 에뮬레이터(Pixel Tablet) 실측, 레이어는 제자리에 두고 Canvas 내용만 매 프레임 다시 그린 원:
 *   30fps → ComposeView 30.0 · 60fps → 60.0 · Normal → 숨은 자식 View -3.0 · High → -4.0 · 지정 안 함 → NaN
 *   (스크롤하는 동안에는 자식 View 가 -4.0). 같은 원을 offset { } 으로 레이어째 옮기면 무엇을 골라도 ComposeView 는 NaN,
 *   자식 View 는 계속 -4.0 — 다시 그리지 않으니 숫자 투표가 안 나가고, 이동이 매 프레임 High 를 투표해 Normal 도 이긴다.
 *
 * 프레임 간격 측정:
 * - withFrameNanos 의 frameTimeNanos(Choreographer 의 vsync 시각) 차이를 잰다. 메인 스레드가 막히면
 *   다음 콜백의 시각이 vsync 여러 개를 건너뛰므로 간격에 그대로 드러난다.
 * - 2초 창마다 FPS(프레임 수 ÷ 흐른 시간) · 늦은 프레임 비율(기대 간격 × 1.5 초과, 60Hz 면 25ms) ·
 *   p95/p99(nearest-rank) · 최악 간격을 낸다. 평균 FPS 는 드문 큰 멈춤을 거의 반영하지 못한다
 *   (60Hz 2초 창에 50ms 멈춤 두 번 → FPS 58.1, 늦은 프레임 2개, p99·최악 50ms — 단위 테스트로 확인).
 * - 1초 넘는 간격은 백그라운드·화면 꺼짐으로 보고 버린다. 모드가 바뀌거나 부하를 바꾸면 창을 새로 시작한다.
 * - 기사 규칙: release 빌드에서, 화면이 아니라 "앱의 루프"에서 잰다. 기사에서는 GL 스레드가 마지막 프레임을
 *   60fps 로 다시 내보내는 동안 게임 상태는 39fps 로 멈춰 있어 표시 쪽 지표가 전부 정상으로 보였다.
 *
 * 지속 성능 모드:
 * - PowerManager.isSustainedPerformanceModeSupported(API 24) 가 false 면 Window.setSustainedPerformanceMode(true) 는 무시된다.
 * - 기사에서는 발열로 클럭이 꺾일 때마다 생기던 늦은 프레임이 절반으로 줄었다. 효과는 수 분 이상 돌려 발열 구간에서 보인다.
 *
 * 실측 (2026-10-06, SM-A725F / Android 13 / One UI, release 디버그 키 서명 + cmd package compile -m speed):
 * - 디스플레이 모드: dumpsys 에는 id1 1080×2400 90Hz · id2 60Hz(서로 alternativeRefreshRates). 그런데 앱이 받는
 *   Display.supportedModes 는 사용자 설정에 따라 걸러진다 — '화면 움직임 부드럽게: 표준'
 *   (settings secure refresh_rate_mode=0, 범위 [60 60])에서는 id2 하나만, 적응형(=1, 범위 [90 90])에서는 두 개.
 *   alternativeRefreshRates 는 두 설정 모두 앱에 빈 배열로 왔다.
 * - 창 요청(적응형): id2 요청 → 90→60Hz, id1 요청 → 60→90Hz 가 1.5초 안에 반영되고 DisplayListener 로 기록됐다.
 *   60Hz 를 요청한 채 헤더 뒤로가기로 목록(같은 BlogExampleActivity)에 돌아오면 DisposableEffect 원복으로 90Hz 복귀
 *   (SurfaceFlinger refresh-rate 로 확인).
 * - isSustainedPerformanceModeSupported = false · 절전 꺼짐 · 발열 NONE.
 * - Compose preferredFrameRate(30f) 를 API 33 에서 골라도 프레임 간격 11.1ms(90Hz) 그대로 — 무동작.
 *
 *   | 부하                    | 60Hz                              | 90Hz                              |
 *   |-------------------------|-----------------------------------|-----------------------------------|
 *   | 없음                    | 59.8fps · 늦은 0% · 최악 16.8ms     | 89.7fps · 늦은 0% · 최악 11.3ms     |
 *   | 60프레임마다 50ms 멈춤   | 57.8fps · 1.7%(2) · p99/최악 50.2ms | 83.7fps · 2.4%(4) · p99/최악 55.7ms |
 *   | 매 프레임 13ms 작업      | 59.8fps · 0% · 최악 17.1ms          | 69.2fps · 29.5%(41) · p95 22.3ms    |
 *
 * 측정 함정 (같은 날 실측):
 * - 결과 표를 key 없이 맨 위에 행을 넣으면 아래 행 Text 18개가 다시 레이아웃돼 그 프레임이 50ms 를 넘겼다
 *   (framestats: 애니메이션 단계 19ms + 그리기 24ms). key(sequence) 로 고정.
 * - 매 프레임 다시 그리는 그래프를 graphicsLayer 로 떼고 5번 카드의 움직이는 원을 컴포지션이 아니라 그리기 단계에서
 *   읽게 바꾸자 평상시(framestats 중앙값) 그리기 약 6ms → 1.0~1.9ms · 애니메이션 단계 3.5ms → 1.0~1.3ms(release).
 *   최종 코드 + AOT 기준선: 60Hz 59.8fps · 늦은 프레임 0 · 최악 16.8ms.
 * - 그래도 release 를 adb 로 설치한 직후(JIT, run-from-apk)에는 2초마다 도는 표 갱신 프레임이 매번 33.5ms 였다.
 *   표 갱신을 끈 임시 빌드에서는 0, cmd package compile -m speed -f 뒤에는 표 갱신이 있어도 0 → 드물게 도는 경로는
 *   JIT 가 데우지 못한다. 실제 사용자 기기에서는 Play 설치·백그라운드 dexopt·Baseline Profile 이 이 역할을 한다.
 * - debug(HotSwan)에서는 그래프 그리기가 중앙값 44.5ms 라 약 20fps 로 무너졌다 → 이 파일과 FramePacingMeter 를
 *   hotSwanCompiler exclude 에 추가(docs/devtools/ComposeHotReloadGuide.md 표 참조).
 */
