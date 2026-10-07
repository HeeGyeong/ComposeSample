package com.example.composesample.presentation.example.component.ui.material3

/**
 * Material 3 Expressive 예제 참고 자료
 *
 * ## Material3 1.4.0 릴리즈 노트
 * - 공식 릴리즈: https://developer.android.com/jetpack/androidx/releases/compose-material3#1.4.0
 *
 * ### SecureTextField / OutlinedSecureTextField
 * - TextFieldState 기반 비밀번호 입력 전용 컴포넌트
 * - TextObfuscationMode: Hidden(즉시 난독화), RevealLastTyped(마지막 문자 잠시 표시), Visible(그대로 표시)
 * - obfuscationCharacter 파라미터로 난독화 문자 커스텀 가능 (기본값 '•')
 *
 * ### 시스템 '비밀번호 표시' 설정과 TextObfuscationMode.System (foundation 1.12, 2026-10-07 보강)
 * - 릴리스 노트: https://developer.android.com/jetpack/androidx/releases/compose-foundation
 * - foundation 1.12.1 sources jar 기준:
 *   · `TextObfuscationMode.System`(1.12 신규) — 시스템 설정이 켜져 있으면 RevealLastTyped, 꺼져 있으면 Hidden 처럼 동작.
 *     API 37+ 는 `android.text.ShowSecretsSetting`(37.0 신규)으로 터치·물리 키보드를 따로 판정, 37 미만은
 *     `Settings.System.TEXT_SHOW_PASSWORD` 하나로 둘 다 판정. 읽기 실패·값 없음은 켜짐(true)으로 본다
 *   · **`RevealLastTyped` 의 의미가 바뀌었다** — 1.11.4 는 TEXT_SHOW_PASSWORD 가 꺼져 있으면 Hidden 과 같았는데,
 *     1.12.1 은 `RevealLastTyped -> true`(설정 무시, 항상 마지막 글자 노출). BasicSecureTextField 기본값은
 *     RevealLastTyped(internal Default) → System 으로 바뀌었다
 *   · **Material3 1.4.0 SecureTextField/OutlinedSecureTextField 기본값은 여전히 RevealLastTyped** → foundation 1.12 와 함께
 *     쓰면 사용자가 비밀번호 표시를 꺼도 마지막 글자가 보인다. 설정을 따르려면 `textObfuscationMode = System` 을 명시
 *   · `LocalTextFieldContentObserverRegistrationExecutor`(1.12 신규, Executor?) — 비밀번호 필드마다 하는 ContentObserver
 *     등록·해제(IPC)를 넘긴 Executor 에서 실행. null(기본)이면 메인 스레드. 거부되면 호출 스레드에서 바로 실행
 *   · `PasswordVisibilitySetting`·`SplitVisibilitySettings`·팩토리는 **internal** — 앱이 표시 정책을 주입할 수 없다
 *     (후보 기록의 "직접 구현" 전제는 틀렸다 — 공개 표면은 모드와 등록 Executor 뿐)
 * - 실측(SM-A725F/Android 13, debug): 이 기기는 show_password 값이 없어(null) 켜짐으로 해석 →
 *   RevealLastTyped·System 모두 'a' 노출 후 1.5초 뒤 '•'. show_password=0 으로 바꾸면 RevealLastTyped 는 "•b" 노출,
 *   System 은 "••" 즉시 숨김(측정 후 settings delete 로 원래의 '값 없음'으로 복원, 카드가 ContentObserver 로 즉시 갱신).
 *   Executor: 등록 1회 → 필드 숨김(해제) 2회 → 다시 표시(등록) 3회, 모두 전용 스레드 pwd-observer-io
 *
 * ### HorizontalFloatingToolbar
 * - ExperimentalMaterial3ExpressiveApi 필요
 * - leadingContent / content / trailingContent 3영역 구조
 * - FloatingToolbarDefaults.floatingToolbarState()로 확장/축소 상태 관리
 * - 스크롤 연동 시 expanded 파라미터를 스크롤 상태와 바인딩
 *
 * ### VerticalDragHandle
 * - BottomSheet용 시각적 드래그 핸들
 * - DragHandleSizes, DragHandleColors, DragHandleShapes로 커스터마이징
 *
 * ### ButtonGroup 개선 (1.4.0)
 * - verticalAlignment 파라미터 추가
 * - ButtonGroupDefaults 기본 오버플로우 인디케이터 제공
 * - Modifier.align() 확장으로 개별 버튼 정렬 제어
 *
 * ### 참고: Material Icons 지원 중단
 * - androidx.compose.material:material-icons-core/extended는 1.7.8이 마지막 버전
 * - 향후 Material Symbols(Google Fonts Vector Drawable XML)로 마이그레이션 권장
 * - https://fonts.google.com/icons
 */
