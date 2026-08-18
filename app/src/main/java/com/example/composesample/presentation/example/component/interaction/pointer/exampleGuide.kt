package com.example.composesample.presentation.example.component.interaction.pointer

/**
 * Pointer 예제 참고 자료
 *
 * ## IndirectPointerExampleUI (간접 포인터 입력 파이프라인)
 * 공식 문서: https://developer.android.com/develop/ui/compose/touch-input/pointer-input/understand-gestures
 *
 * 프로젝트가 실제 해석하는 androidx.compose.ui:ui-android:1.11.1 AAR을 javap -v로 직접 역어셈블해
 * 확정한 사실(추측 아님):
 *
 * ### androidx.compose.ui.input.indirect 패키지 (신규, opt-in 불필요)
 * - `IndirectPointerInputModifierNode : DelegatableNode` — `onIndirectPointerEvent(event, pass)` /
 *   `onCancelIndirectPointerInput()` 두 메서드뿐인 인터페이스. `pointerInput{}` 같은 상위 편의
 *   확장 함수가 없어, `Modifier.Node()` + `ModifierNodeElement`로 직접 델리게이트해야 한다.
 * - `IndirectPointerEvent.changes: List<IndirectPointerInputChange>` / `.type: IndirectPointerEventType`
 *   / `.primaryDirectionalMotionAxis: IndirectPointerEventPrimaryDirectionalMotionAxis`.
 * - `IndirectPointerEventType`(Int 기반 value class): `Unknown`/`Press`/`Release`/`Move`.
 * - `IndirectPointerEventPrimaryDirectionalMotionAxis`(Int 기반 value class): `None`/`X`/`Y`.
 * - `IndirectPointerInputChange`: `id: PointerId`(기존 PointerInputChange.id와 동일 타입 — 맹글링
 *   해시(J3iCeTQ)가 PointerInputChange/PointerInputEventData와 일치함을 대조 확인) / `uptimeMillis: Long`
 *   / `position: Offset`(맹글링 해시 F1C5BW0이 Offset 사용처 전반과 일치) / `pressed: Boolean` /
 *   `pressure: Float` / `previousUptimeMillis`/`previousPosition`/`previousPressed` / `isConsumed` +
 *   `consume()`.
 * - **opt-in 경계가 인터페이스와 테스트 훅 사이에서 갈린다(javap -v로 두 번 확정)**:
 *   `IndirectPointerInputModifierNode`/`IndirectPointerEvent`/`IndirectPointerInputChange`/
 *   `IndirectPointerEventType`/`IndirectPointerEventPrimaryDirectionalMotionAxis` — 즉 프로덕션
 *   코드가 실제로 쓰는 이 예제의 표면 — 는 `RuntimeInvisibleAnnotations`에 opt-in 어노테이션이
 *   0건이라 `@OptIn` 없이 그대로 쓸 수 있다. 반면 **테스트/주입용 진입점인
 *   `androidx.compose.ui.node.RootForTest.sendIndirectPointerEvent(IndirectPointerEvent): Boolean`
 *   (default 메서드)과, 합성 MotionEvent로부터 이벤트를 만드는 공개 팩토리
 *   `AndroidIndirectPointerEvent_androidKt.IndirectPointerEvent(nativeEvent: MotionEvent,
 *   historyIndex: Int = 0, previousEvent: MotionEvent? = null)`은 둘 다
 *   `androidx.compose.ui.ExperimentalIndirectPointerApi`로 게이팅**돼 있다 — 이 두 심볼은
 *   `androidx.compose.ui.input.indirect` 서브패키지가 아니라 `androidx.compose.ui` 루트 패키지에
 *   선언돼 있어 서브패키지만 훑은 1차 스캔에서는 놓쳤고, 실제 임시 계측 테스트를 컴파일해보고 나서야
 *   드러났다(→ 교훈: 어노테이션 게이팅 스캔은 그 API가 선언된 서브패키지만이 아니라, 그 API를
 *   **호출하는** 다른 패키지의 심볼까지 프로브 컴파일로 실제로 건드려봐야 닫힌다).
 * - **런타임 디스패치는 이번 사이클에 검증하지 못했다** — `sendIndirectPointerEvent()`로 합성 이벤트를
 *   주입해 `onIndirectPointerEvent()`까지 실제로 도달하는지 확인하는 임시 계측 테스트를 작성했으나,
 *   연결된 실기기에 이미 설치된 앱이 Secure Folder(별도 사용자 프로필) 소속이라 서명 불일치로 새 디버그
 *   빌드 설치 자체가 막혀 실행하지 못했다(사용자 개인 보안 공간이라 강제 제거하지 않기로 결정). 따라서
 *   이 예제의 원시 캡처 카드는 **프로브 컴파일 + 정적 바이트코드 검증까지만** 닫힌 상태이고, 실제
 *   디스패치 여부는 트랙패드가 연결된 기기이거나 서명이 맞는 테스트 환경에서 추가 확인이 필요하다.
 *
 * ### androidx.compose.ui.input.pointer 패키지의 트랙패드 확장 (기존 파이프라인, 안정 API)
 * - `PointerEventType`에는 이미 안정적으로 `PanStart`/`PanMove`/`PanEnd`/`ScaleStart`/`ScaleChange`/
 *   `ScaleEnd`가 추가돼 있다(Press/Release/Move/Enter/Exit/Scroll과 동일한 companion 소속).
 * - **이 값들이 언제 채워지는지는 `MotionEventAdapter.class` 바이트코드로 확정**: 두 값 모두
 *   `ComposeUiFlags.isTrackpadGestureHandlingEnabled`(정적 초기화 블록에서 `true`로 설정 — 기본
 *   활성화, `isMediaQueryIntegrationEnabled`처럼 별도 켜기 불필요) 가드 뒤에서, 그리고
 *   `android.view.MotionEvent.getClassification()`이 `CLASSIFICATION_TWO_FINGER_SWIPE`(3, API 29+)
 *   / `CLASSIFICATION_PINCH`(5, API 34+)일 때만 만들어진다. 이 classification은 Android 프레임워크가
 *   트랙패드류 장치의 제스처에만 부여하는 값이라, **터치스크린 멀티터치로는 이 타입들이 결코 발생하지
 *   않는다** — 이 예제 화면에서 손가락 2개로 눌러봐도 주황 칩이 계속 꺼져 있는 것이 그 증거다.
 * - `androidx.compose.foundation.gestures.TransformableKt`(`Modifier.transformable`) /
 *   `ScrollableNode`(`Modifier.scrollable`) / `TrackpadScrollingLogic`가 `PointerEvent.type`으로
 *   이 값들을 **읽기만** 한다(생성 코드가 없음 — `javap -c`로 세 클래스 전체에서 PointerEventType 생성
 *   호출부가 0건임을 확인). 즉 이 값을 **만드는 유일한 지점은 `MotionEventAdapter`**이고, 기존 위젯들은
 *   "트랙패드 2손가락 제스처 = 터치 제스처와 동일한 PointerEventType으로 들어오는 이벤트"로 취급해
 *   장치 종류를 구분하지 않고도 재사용하도록 설계돼 있다.
 * - `PointerInputChange.type: PointerType`(맹글링 해시 T8wyACA가 `PointerType.Companion`과 일치)로
 *   Touch/Mouse/Stylus/Eraser/Unknown을 구분할 수 있다 — 이건 터치스크린에서도 바로 관측 가능하다
 *   (`Touch`로 뜬다).
 *
 * ### 요약 — 왜 두 파이프라인이 공존하는가
 * `IndirectPointerInputModifierNode`는 트랙패드 커서 자체(위치 하나, 눌림 상태 하나)를 다루려는
 * 위젯을 위한 신규 저수준 인터페이스이고, `PointerEventType`의 Pan·Scale 계열은 반대로 "이미 멀티터치를
 * 이해하는 기존 위젯이 트랙패드도 공짜로 지원하게" 만들기 위한 상위 호환 계층이다. 둘 다 물리적으로는
 * 같은 트랙패드 하드웨어에서 나오지만 노출 층이 다르다.
 */
