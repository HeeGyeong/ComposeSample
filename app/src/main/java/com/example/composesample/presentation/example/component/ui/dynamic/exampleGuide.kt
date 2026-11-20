package com.example.composesample.presentation.example.component.ui.dynamic

/**
 * Dynamic UI 예제 참고 자료
 *
 * ## A2uiRenderingExampleUI (#120 — A2UI 컴포넌트 트리 렌더링, androidx.a2ui 1.0.0-alpha02)
 * - 출처: Android Developers Blog, In-app agentic workflows(2026-09-28):
 *   https://android-developers.googleblog.com/2026/09/android-agentic-workflows.html
 *   · John O'Reilly, Rendering AI agent generated UI(Android Weekly #746): https://johnoreilly.dev/posts/a2ui-compose-koog/
 * - 프로토콜: https://a2ui.org · 저장소 https://github.com/google/A2UI
 *   · 기본 카탈로그(v0.9): https://a2ui.org/specification/v0_9/catalogs/basic/catalog.json
 * - androidx 릴리스 노트: https://developer.android.com/jetpack/androidx/releases/a2ui
 *   · API: https://developer.android.com/reference/kotlin/androidx/a2ui/compose/ui/package-summary
 *   · https://developer.android.com/reference/kotlin/androidx/compose/material3/a2ui/package-summary
 *   — 아래 규칙은 a2ui-model · a2ui-engine · compose-runtime · compose-ui · material3-a2ui 1.0.0-alpha02 sources jar 로 확인했다
 * - 의존성(2026-11-20 사용자 승인): androidx.a2ui:a2ui-model · androidx.a2ui.compose:compose-runtime · compose-ui ·
 *   androidx.compose.material3:material3-a2ui(전이 a2ui-engine · core-i18n 1.0.0). aar minCompileSdk 37, compose 계열은
 *   minCompileMinorSdk 1 · minAGP 9.1.0. module 이 compose 1.13.0-alpha03 · material3 1.5.0-alpha28 을 요구 — 그대로 넣으면 앱 전체
 *   Compose 가 알파로 끌려 올라가서 BOM 2026.11.01(1.13.0 · 1.5.0 정식, DEP-CURRENCY-35)을 먼저 올렸다. 그 뒤 해석 diff 는 A2UI 5좌표 + core-i18n 뿐
 * - 흐름(KDoc 그대로):
 *   1) 카탈로그 — materialA2uiBasicCatalogV1(image, video, audioPlayer, functions = A2uiBasicCatalogV1.Functions(context)).
 *      Image · Video · AudioPlayer 는 렌더러(A2uiImageRenderer 등 fun interface)를 앱이 꼭 준다 — 이 예제는 URL 을 받지 않고 자리만 그린다
 *   2) 처리기 — A2uiMessageProcessor(listOf(catalog)), 보통 ViewModel 에
 *   3) collectMessages() 를 백그라운드 디스패처에서 — 이것이 돌아야 processMessage 로 넣은 메시지가 처리된다
 *   4) activeSurfaces(StateFlow) 를 모아 A2uiSurface(surface) — 로딩 · 오류 · 성공을 AnimatedContent 로 전환
 * - 메시지(v0.9): JSON 하나 = {"version":"v0.9", 봉투 하나} — createSurface(surfaceId, catalogId) · updateComponents(평평한 목록,
 *   {"id", "component": 타입, …속성}, 자식은 id, 루트는 "root") · updateDataModel(path, value) · deleteSurface. 값은 리터럴 또는 {"path": …}
 *   바인딩이고, 바인딩한 값이 아직 없으면 그 컴포넌트는 준비 중(그리지 않음) — 실측: 2 단계 뒤 제목 · 카드 · 이름 칸 · 버튼만 보이고
 *   잔 수 Slider · 포장 CheckBox · 상태 줄은 3 단계(데이터 모델)에서 나타났다
 * - 사용자 동작: Button 의 action.event(name, context) 가 outboundEvents 로 A2uiClientEventMessage(type = 이벤트 이름) 로 나간다.
 *   context 의 {"path"} 값은 데이터 모델에서 풀려 나온다 — 실측 {name=Kim, cups=3.0, takeout=true}(Slider 값은 Double).
 *   TextField · Slider · CheckBox 의 value 바인딩은 입력하는 대로 데이터 모델에 쓴다
 * - 오류:
 *   · A2uiJsonMessageParser.parse 는 버전을 먼저 보고(v0.9 · v0.9.1), 모든 예외를 A2uiValidationException(VALIDATION_FAILED, 문맥 path)으로
 *     감싼다. processInput 은 그것을 A2uiClientErrorMessage 로 바꿔 outboundEvents 로 보낸다(A2uiException 밖은 잡지 않는다고 KDoc 에 적혀 있지만
 *     파서가 이미 감싼다). A2uiException 은 sealed — 하위는 A2uiValidationException(path 만) · A2uiRuntimeException 둘뿐이라
 *     A2uiClientErrorMessage init 의 require(VALIDATION_FAILED → 문맥 키 {path}) 는 이 경로에서 걸리지 않는다
 *     (처음 'processInput 은 IllegalArgumentException 을 던질 수 있다' 고 적었다가 소스로 바로잡음)
 *   · 엔진의 surface 처리(A2uiCoreSurfaceActor)는 A2uiException 만 잡아 클라이언트 오류로 내보낸다. 그 밖의 예외는 collectMessages 를
 *     끝낸다 → 예제는 ViewModel 에서 잡아 '처음부터' 로 새 처리기를 만든다
 *   · 실측(4번 카드): 깨진 JSON → 'Malformed JSON message: End of input', v0.8 → 'Unsupported protocol version', id 없음 →
 *     'Missing or empty id in component payload' 는 파서에서 거절, 카탈로그 밖 타입(Carousel) 은 처리 중 클라이언트 오류 VALIDATION_FAILED
 *     'Component type Carousel not found in catalog' + surface 가 오류 화면(A2uiSurfaceDefaults.ErrorFallback)으로 바뀜. 처리 루프는 살아 있었다
 * - 실측(헤드리스 Pixel 4 API 30 에뮬, 실기기 미사용, debug · release 디버그 키 서명): 1 → 5 단계 · 입력(Kim · 3잔 · 포장) → 주문 →
 *   0.4초 뒤 '주문 접수: Kim · 3잔 · 포장' · 안내 카드 추가 · surface 닫힘 · 틀린 메시지 4종 · 다크 모드 재생성(ViewModel 이라 surface 유지)
 *   모두 크래시 0. 프로세스가 죽으면 ViewModel 과 함께 surface 도 사라진다(A2UI 상태는 저장하지 않는다 — 에이전트가 다시 보내는 모델)
 * - 함정:
 *   · 기기 스크립트: adb input text 는 한글을 못 쳐서 이름은 ASCII 로 · 대본 JSON 의 한글은 그대로 렌더된다
 *   · alpha01 → 02 에서 카탈로그 함수 인자가 urlOpener · messageFormatter · localeProvider → functions 로 바뀌었다(알파 사이 API 변경)
 */
