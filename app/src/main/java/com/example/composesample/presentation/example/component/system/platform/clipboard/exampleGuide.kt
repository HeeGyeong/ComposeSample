package com.example.composesample.presentation.example.component.system.platform.clipboard

/**
 * System/Platform/Clipboard 예제 참고 자료
 *
 * ## ClipboardProcessTextExampleUI (클립보드 제한과 텍스트 처리 메뉴)
 * - 출처: CodeDAC "Android 10's clipboard restriction and serverless phone-to-PC sync — building Clipboard+"(Android Weekly #749).
 *   원문의 동기화 앱 대신 원문이 부딪힌 플랫폼 규칙(포커스 없는 읽기 거부 · 다른 앱 메뉴에 항목 추가)만 가져왔다:
 *   https://dev.to/codedac/android-10s-clipboard-restriction-and-serverless-phone-to-pc-sync-building-clipboard-2ba9
 * - 공식 문서(복사·붙여넣기, 민감 콘텐츠·API 33 복사 확인): https://developer.android.com/develop/ui/views/touch-and-input/copy-paste
 * - 공식 문서(Android 10 클립보드 접근 제한): https://developer.android.com/about/versions/10/privacy/changes
 * - 공식 문서(Android 12 클립보드 접근 알림): https://developer.android.com/about/versions/12/behavior-changes-all
 * - API: https://developer.android.com/reference/android/content/Intent#ACTION_PROCESS_TEXT
 * - 공식 문서(패키지 가시성 <queries>): https://developer.android.com/training/package-visibility/declaring
 * - Compose 컨텍스트 메뉴 수정자(appendTextContextMenuComponents · filterTextContextMenuComponents):
 *   https://developer.android.com/reference/kotlin/androidx/compose/foundation/text/contextmenu/modifier/package-summary
 * - API(기본 PROCESS_TEXT 항목의 키): https://developer.android.com/reference/kotlin/androidx/compose/foundation/text/contextmenu/data/ProcessTextKey
 * - AOSP ClipboardService(포커스 검사·접근 알림 예외·분류 후 재알림, android14-release):
 *   https://android.googlesource.com/platform/frameworks/base/+/refs/heads/android14-release/services/core/java/com/android/server/clipboard/ClipboardService.java
 * 핵심 개념(실측: Galaxy A72 SM-A725F API 33 debug · AOSP 에뮬레이터 API 34·35 release):
 * - 읽기는 창 포커스가 조건이다(API 29+). '5초 뒤 읽기' 중 알림창을 내리면 액티비티는 RESUMED 그대로인데 포커스를 잃어 null,
 *   홈으로 나가면 CREATED·null. 예외 없이 null·hasPrimaryClip false 라 '빈 클립'과 구분되지 않는다 — logcat 에만
 *   "Denying clipboard access to …, application is not in focus nor is it a system service"(hasPrimaryClip·getPrimaryClip 각 1줄).
 *   포커스가 돌아온 순간(LocalWindowInfo.isWindowFocused) 다시 읽으면 정상
 * - 쓰기는 포커스 없이도 된다. 변경 리스너는 포커스가 있을 때만 불리고 놓친 호출은 돌아와도 오지 않는다(알림창 아래 쓰기·설정 앱 복사 모두 0회).
 *   API 31+ 는 포커스 있는 쓰기 1회에 리스너가 2회 — 시스템이 텍스트 분류를 끝내고 sendClipChangedBroadcast 를 한 번 더 부른다
 *   (콜백 시점 classificationStatus 가 미완료 → 완료, 약 1초 간격). 리스너 호출 수로 쓰기를 세지 않는다
 * - 다른 앱이 넣은 클립을 getPrimaryClip() 으로 읽으면 "ComposeSample pasted from your clipboard"(API 31+). 같은 클립을 다시 읽거나
 *   자기 앱 클립이면 없다. AOSP 상 알림은 getPrimaryClip 에서만, 기본 IME·같은 앱·이미 알린 앱은 제외 — 붙여넣기 버튼 판단은
 *   hasPrimaryClip·getPrimaryClipDescription 으로
 * - EXTRA_IS_SENSITIVE(API 33 상수, 그 전에는 "android.content.extra.IS_SENSITIVE" 문자열)를 ClipDescription.extras 에 실으면
 *   AOSP 복사 미리보기가 "clip-819" 대신 "••••••". 다시 읽어도 플래그가 남는다. Compose 는 ClipEntry(clipData) 로 같은 ClipData 를 넣는다.
 *   API 33+ 는 시스템이 복사를 알리므로 앱 토스트를 겹쳐 띄우지 않는다
 * - 실험 전 클립 보관·복원: 첫 쓰기 직전에 보관하고, 지금 클립의 라벨이 이 예제 것일 때만 되돌린다(다른 앱 복사가 끼면 거부).
 *   원래 비어 있었으면 clearPrimaryClip(API 28). 다른 앱의 클립 내용은 화면·로그에 옮기지 않고 길이·MIME 만
 * - PROCESS_TEXT 액티비티: exported 라 켜 두면 모든 앱 선택 메뉴에 생긴다 → 매니페스트 enabled="false" + 화면에서
 *   setComponentEnabledSetting(ENABLED/DEFAULT, DONT_KILL_APP). 켜진 상태는 재설치·프로세스 종료에도 남고, 화면을 ← 로 나갈 때 꺼진다.
 *   다른 앱(설정 검색창)에서도 READONLY=false · callingActivity=SearchActivity 로 열려 결과가 그 필드에 반영됐다
 * - <queries> PROCESS_TEXT 없이 이 앱은 다른 처리기를 하나도 못 본다(0 → 선언 후 사전·Gemini·Whale 3개). Compose·View 메뉴 모두 같은 질의라 같이 늘었다
 * - Compose foundation 1.12.1(1.11.4 ~ 1.13.0-beta01 소스 동일) 기본 메뉴: ProcessTextApi23Impl 이 context.startActivity 로 연다 →
 *   callingActivity=null 이라 결과가 돌아오지 않고, EXTRA_PROCESS_TEXT_READONLY 에 editable 을 그대로 넣어 뜻이 뒤집힌다
 *   (TextField true · SelectionContainer false). 프레임워크 TextView 는 !isTextEditable() 이고 startActivityForResult
 * - 해결: 그 필드에서 filterTextContextMenuComponents { it.key !is ProcessTextKey } 로 기본 항목을 빼고 appendTextContextMenuComponents 로
 *   직접 단 항목이 rememberLauncherForActivityResult 로 연 뒤 선택 범위를 TextFieldState.edit { replace } — "connected ECHO"
 * - View 필드는 id 가 있어야 항목이 뜬다(TextView.canProcessText: getId() == NO_ID 면 false). AndroidView 로 만든 EditText 는
 *   View.generateViewId() 를 줘야 한다. 읽기 전용 TextView 는 결과를 토스트("DELTA")로만 보인다.
 *   Galaxy View 메뉴는 앱 이름("ComposeSample"·"Google"·"Whale"), Compose 메뉴는 액티비티 라벨("ComposeSample 변환")
 * - 구현 메모: ProcessTextLog 번호는 AtomicInteger — object 의 `private var nextId = 1` 은 debug(HotSwan 재작성)에서 읽을 때마다 초기값으로 돌아온다
 *   (ComposeHotReloadGuide.md). 리스너는 인터페이스 구현 상태 홀더(ClipboardLab)라 ⑰ 함정과 무관
 */
