package com.example.composesample.presentation.example.component.system.platform.contacts

/**
 * System/Platform/Contacts 예제 참고 자료
 *
 * ## ContactEditorPickerExampleUI (연락처 편집기 인텐트와 Contact Picker)
 * - 출처: Android Developers Blog 2026-10-21 "A more reliable way to add and edit contacts on Android"
 *   (다음 릴리스 타깃 시 WRITE_CONTACTS 직접 쓰기 거부 · 편집기 인텐트 표준화 · MANAGE_CONTACTS · Before/After 코드):
 *   https://developer.android.com/blog/posts/reliable-way-to-add-and-edit-contacts-android
 * - 공식 문서(Android 17 Contact Picker — ACTION_PICK_CONTACTS · 세션 URI · 이전 타깃 시험):
 *   https://developer.android.com/about/versions/17/features/contact-picker
 * - 공식 문서(인텐트로 연락처 수정 — ACTION_INSERT · ACTION_EDIT · finishActivityOnSaveCompleted · ACTION_INSERT_OR_EDIT):
 *   https://developer.android.com/identity/providers/contacts-provider/modify-data
 * - API: https://developer.android.com/reference/android/provider/ContactsContract.Intents.Insert
 * - API: https://developer.android.com/reference/android/provider/ContactsPickerSessionContract
 * - API(레거시 피커 계약): https://developer.android.com/reference/androidx/activity/result/contract/ActivityResultContracts.PickContact
 * - 공식 문서(매니페스트 uses-permission maxSdkVersion): https://developer.android.com/guide/topics/manifest/uses-permission-element
 * 핵심 개념(실측: Pixel 4 API 37 에뮬레이터 · Pixel Tablet API 35 에뮬레이터 debug·release / Galaxy A72 SM-A725F API 33 debug,
 * 실기기는 연락처를 쓰지 않고 편집기·피커를 열고 닫기만 — 이 앱 targetSdk 35):
 * - SDK 37 에 있는 것: ContactsPickerSessionContract(ACTION_PICK_CONTACTS · EXTRA_PICK_CONTACTS_* · Session) ·
 *   Intent.EXTRA_USE_SYSTEM_CONTACTS_PICKER. 없는 것(다음 릴리스 · 17 QPR2): Insert.EXTRA_PHOTO_URI · Settings.ACTION_MANAGE_CONTACTS ·
 *   MANAGE_CONTACTS · People Directory — 화면 문구로만 다룬다
 * - 직접 쓰기(원문 Before): WRITE_CONTACTS 만 있으면 화면 없이 applyBatch(RawContacts + back-reference Data 2행)가 끝난다(API 35).
 *   같은 CONTACTS 그룹이라 WRITE 를 허용한 뒤 READ 요청은 대화상자 없이 허용됐다. 권한이 없으면 "opening provider … requires
 *   READ_CONTACTS or WRITE_CONTACTS", READ 만 있으면 "Permission Denial: writing … raw_contacts" 의 SecurityException
 * - maxSdkVersion: WRITE_CONTACTS 를 36 으로 좁혀 두면 API 37 기기에서는 요청 목록(PackageInfo.requestedPermissions·dumpsys)에서 빠지고,
 *   요청은 대화상자 없이 거부된다 — 다음 릴리스 타깃 앱이 겪을 거부를 지금 기기에서 미리 본다. 실제 이전이라면 다음 릴리스 직전 버전까지
 * - 편집기 인텐트(Google 연락처 4.74 · API 37 / 4.88 · API 35): ACTION_INSERT 는 반 시트 'Add to contacts', Save 하면
 *   finishActivityOnSaveCompleted 유무와 상관없이 곧바로 호출 앱으로 돌아오고 RESULT_OK + 연락처 lookup URI(읽기 권한 플래그 없음).
 *   연락처 앱을 처음 쓴 직후(pm clear 직후 포함) 첫 저장만 RESULT_CANCELED·data null 인데 저장돼 있었다 — 결과 코드로 저장을 판단하지 않는다.
 *   BACK 은 대화상자 없이 닫히고 저장 안 함
 * - Insert.DATA: 표준 Website 행은 남고, 표준 밖 MIME 행과 Photo 행은 Google 편집기에서 보이지도 않고 사라졌다(원문의 '조용히 버려진다').
 *   Samsung 편집기(API 33)는 Photo 행을 편집기에 보여 줬다(저장은 실기기라 안 함) — '기기마다 다름' 의 실례
 * - 이름: provider 가 'CS152 편집기 N' 을 성·이름으로 나눠 표시 이름을 'CS152편집기 N' 으로 만든다(직접 넣은 행도 같다) →
 *   예제 연락처는 '포함' 으로 찾고, 편집기 결과는 요청마다 다른 전화번호로 찾는다
 * - ACTION_EDIT(lookup URI + Insert.PHONE)은 'Add to existing contact' 시트, Save → 기존 연락처에 번호 추가, RESULT_OK + 같은 URI.
 *   ACTION_INSERT_OR_EDIT 은 검색 시트 → 고른 연락처 URI. 피커 결과의 LOOKUP_KEY 로 만든 lookup URI(id 없음)도 ACTION_EDIT 에 그대로 쓰여
 *   READ·WRITE 없이 '골라서 고치기' 가 된다(API 37)
 * - Contact Picker(API 37): 반 시트, 요청 필드를 가진 연락처만, 한 명 모드도 Done 필요, 전화가 둘이면 번호 하나를 고른다.
 *   결과 content://0@com.android.contacts.picker.sessions/sessions/<uuid> — authority 앞 사용자 id 때문에 authority 비교는 빗나가 host 로 가른다.
 *   행은 필드 하나당 하나(3명 4행) → LOOKUP_KEY 로 묶는다. SELECTION_LIMIT 3 에서 4번째는 'Can share only 3 contacts at a time',
 *   MATCH_ALL_DATA_FIELDS 는 모든 필드를 가진 연락처만 남긴다. selection 을 넣으면 UnsupportedOperationException, 투영 null 이면 열 81개·계정 열 0개
 * - targetSdk 37 미만 앱: ACTION_PICK_CONTACTS 에 EXTRA_USE_SYSTEM_CONTACTS_PICKER 가 없으면 '처리할 앱 없음' 선택 화면 → RESULT_CANCELED
 *   (예외 없음, resolveActivity 는 시스템 피커를 가리킨다). ACTION_PICK 은 연락처 앱 피커로 열리고, 엑스트라를 붙이면 새 피커 화면이지만
 *   결과는 연락처 lookup URI(content://0@com.android.contacts/…)
 * - URI 임시 권한의 수명(세션·레거시 모두, API 35·37, debug·release 같음): 받은 액티비티가 같으면 am kill → 최근 앱 복원(새 pid) 뒤에도 읽히고,
 *   같은 프로세스라도 액티비티를 끝내고 새로 연 화면·force-stop 뒤에는 SecurityException — 프로세스가 아니라 액티비티 기록에 묶인다.
 *   레거시 연락처 URI 의 하위 data 경로는 READ_CONTACTS 없이 SecurityException(권한은 그 URI 하나에만)
 * - Samsung 연락처 피커(API 33)는 BACK 에 resultCode 1100 을 돌려줬다(2회) — 같은 화면이 INSERT_OR_EDIT 로 열렸을 때는 0. 'RESULT_OK 인가' 로 판정한다
 * - 구현 메모: 번호는 AtomicInteger(무작위 시작) — 프로세스가 다시 떠도 전화번호가 겹치지 않게. 받은 URI 는 SharedPreferences 에 적어
 *   새 액티비티·새 프로세스에서 다시 조회한다(rememberSaveable 은 같은 액티비티 복원에만 남는다)
 */
