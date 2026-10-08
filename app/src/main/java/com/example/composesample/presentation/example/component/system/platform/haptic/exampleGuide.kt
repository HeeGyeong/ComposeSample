package com.example.composesample.presentation.example.component.system.platform.haptic

/**
 * System/Platform/Haptic 예제 참고 자료
 *
 * ## HapticFeedbackExampleUI (햅틱 피드백)
 * - 공식 문서(Views 가이드 — Compose 전용 햅틱 가이드는 없다): https://developer.android.com/develop/ui/views/haptics/haptic-feedback
 * - 출처: https://medium.com (Android Weekly 햅틱 비교)
 * 핵심 개념:
 * - LocalHapticFeedback.current.performHapticFeedback(HapticFeedbackType.LongPress) — Compose 표준 API
 * - View.performHapticFeedback(HapticFeedbackConstants.XXX) — 플랫폼 상수 기반 (더 다양한 종류)
 * - API 레벨별 지원 범위 차이: 일부 HapticFeedbackConstants는 상위 API에서만 동작
 * - Compose HapticFeedbackType vs 플랫폼 Constants 매핑 비교
 *
 * ## 3번 섹션 — 상호작용 사운드 (Compose 1.12, 청각 피드백)
 * - 릴리스 노트: https://developer.android.com/jetpack/androidx/releases/compose-ui
 * - 릴리스 노트: https://developer.android.com/jetpack/androidx/releases/compose-foundation
 * 핵심 개념 (ui·foundation 1.12.1 sources jar 기준):
 * - `SoundEffect` 인터페이스(playClickSound 하나) + `LocalSoundEffect`(staticCompositionLocalOf).
 *   Android 구현은 `View.playSoundEffect(SoundEffectConstants.CLICK)` 로 위임한다(AndroidSoundEffect, internal).
 * - 1.12 부터 clickable·combinedClickable·toggleable·selectable(전부 ClickableNode)이 performClick 에서
 *   `LocalSoundEffect.playClickSound()` 를 자동 호출한다 — 따로 켤 필요 없음. 같은 performClick 을 쓰는
 *   시맨틱 onClick(TalkBack 두 번 탭)·키보드 Enter 도 요청한다. onDoubleClick 이 있으면 첫 탭에서 바로 재생.
 * - `SoundEffectOnInteraction(enabled) { }` — LocalSoundEffect 를 감싸 enabled=false 면 요청을 버린다.
 *   중첩 시 기존 래퍼를 벗겨 원래 구현에 다시 씌우므로 안쪽 값이 이긴다.
 * - 키보드·D-pad 포커스 이동음은 AndroidComposeView 가 View.playSoundEffect 를 직접 부르는 별도 경로라
 *   SoundEffectOnInteraction 으로 끌 수 없다(소스 + 에뮬레이터 실측: enabled=false 구역으로 TAB 해도 재생).
 * - pointerInput(detectTapGestures) 같은 직접 만든 제스처는 클릭 노드가 아니라 자동 재생이 없다 →
 *   필요하면 `LocalSoundEffect.current.playClickSound()` 를 직접 부른다(햅틱과 함께 내면 촉각 + 청각).
 * - 전역 끄기: `ComposeFoundationFlags.isInteractionSoundEffectOnClickEnabled`(@ExperimentalFoundationApi),
 *   `AndroidComposeUiFlags.isInteractionSoundEffectsEnabled`(@ExperimentalComposeUiApi) — 둘 다 기본 true 이고
 *   "soak 후 제거" TODO 가 붙은 임시 플래그라 의존하지 않는다.
 * - 요청이 실제 소리가 되려면 플랫폼 조건 두 가지: 시스템 '터치음'(Settings.System.SOUND_EFFECTS_ENABLED) 켜짐 +
 *   STREAM_SYSTEM 이 음소거 아님(무음·진동 모드면 음소거). 예제는 두 값을 함께 보여 준다.
 * - 실측(Galaxy A72 / Android 13, debug): 기본 구역 탭 4·요청 4, enabled=false 구역 탭 3·요청 0,
 *   false→true 전환 뒤 탭 3·요청 3, pointerInput 만 탭 2·요청 0, 직접 호출 탭 2·요청 2, 키보드 Enter 2·요청 2.
 *   기기가 무음 모드라 터치음을 켜도 SoundPool 재생 이벤트는 0.
 * - 실측(Pixel 4 에뮬레이터 / API 30, 소리 모드): dumpsys audio 의 system_server SoundPool `state:started` 증가량이
 *   화면 계수와 일치 — 기본 2탭 +2, enabled=false 2탭 +0, true 2탭 +2, pointerInput 만 +0, 직접 호출 +2,
 *   TAB 포커스 이동마다 +1(enabled=false 구역 안으로 들어갈 때도), false 구역 Enter +0. 벨소리 볼륨 0 → 진동 모드에서
 *   기본 2탭 +0 이고, 카드가 RINGER_MODE_CHANGED(RECEIVER_NOT_EXPORTED 로 등록해도 수신)로 즉시 '안 들림'으로 바뀐다.
 * - 함정(debug 전용): 컴포저블 안에서 프레임워크 '클래스'를 상속한 익명 객체(ContentObserver·BroadcastReceiver·Handler)가
 *   by 위임 지역 변수에 쓰면, 시스템이 그 콜백을 부르는 순간 합성 접근자 NoSuchMethodError(access$<함수>$lambda$N)로
 *   크래시한다. release·재작성 끈 debug 정상, 인터페이스 구현 콜백(FrameCallback·Handler.Callback)은 정상 — 관찰자를
 *   상태 홀더 클래스로 뺐다. 상세 대조표는 docs/devtools/ComposeHotReloadGuide.md.
 */
