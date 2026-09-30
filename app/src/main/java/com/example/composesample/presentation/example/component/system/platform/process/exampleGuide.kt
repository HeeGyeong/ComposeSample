package com.example.composesample.presentation.example.component.system.platform.process

/**
 * ## MultiProcessExampleUI (android:process · isolatedProcess · Messenger)
 * - 출처(AW #745): https://returnzero.dev/articles/one-android-app-why-so-many-processes
 * - 프로세스와 스레드: https://developer.android.com/guide/components/processes-and-threads
 * - `<service>` 매니페스트 요소(process · isolatedProcess): https://developer.android.com/guide/topics/manifest/service-element
 * - 바인드된 서비스와 Messenger: https://developer.android.com/develop/background-work/services/bound-services
 * - TransactionTooLargeException: https://developer.android.com/reference/android/os/TransactionTooLargeException
 * - ServiceConnection.onBindingDied: https://developer.android.com/reference/android/content/ServiceConnection#onBindingDied(android.content.ComponentName)
 *
 * ### 구성
 * 같은 서비스 코드([ProcessDemoService])를 빈 하위 클래스 3개로 매니페스트에 선언한다
 * (클래스 하나를 두 번 선언할 수 없어서). Local = 기본 프로세스, Remote = `:remote`, Isolated = `:isolated` + `isolatedProcess`.
 * 통신은 Messenger, 요청 번호는 `arg1`, 응답은 `replyTo` 로 받는다. 서비스 쪽 처리는 전용 HandlerThread
 * (점검의 소켓 연결이 메인 스레드 네트워크 금지에 걸리지 않게).
 *
 * ### 핵심 개념 (Galaxy A72 · Android 13 실측, debug·release 모두)
 * - **Application.onCreate 는 프로세스마다 실행된다.** 가드 없이 두면 `:remote`·`:isolated` 에서도 startKoin 이 돈다
 *   (가드를 잠시 빼고 잰 값: Koin 시작됨 main/remote/isolated = true/true/true → 가드 적용 후 true/false/false).
 *   BaseApplication 의 가드는 `util/ProcessUtil.kt` 의 `isMainProcess()` —
 *   API 28+ `Application.getProcessName()`, 그 아래는 `/proc/self/cmdline`(98자에서 잘리지만 기본 프로세스 판정에는 영향 없음)
 * - `object`·companion 도 프로세스마다 따로다 — 메인 카운터를 올려도 `:remote` 카운터는 그대로
 * - 같은 UID·다른 PID. bind 한 화면이 FOREGROUND(100) 면 `:remote` 도 FOREGROUND(100) — 서비스 프로세스는 클라이언트의 중요도를 물려받는다
 * - 첫 화면까지 나가 ViewModel 이 unbind 해도 `:remote` 는 캐시로 남아, 다시 들어오면 같은 PID·카운터·생존 시간이 이어진다.
 *   `:isolated` 는 bind 가 풀리면 곧바로 사라진다(ps 확인)
 *
 * #### 바인더 트랜잭션 한계
 * - 같은 프로세스 서비스는 Message 객체를 그대로 받아 2MB 도 통과(직렬화 없음). `:remote` 는 600KB 부터 TransactionTooLargeException
 * - 원시 `transact` 로 잰 한계: **동기 1,040,384 B = 1MB − 8KB(2페이지, 버퍼 전체)**, **oneway 520,096 B ≈ 그 절반**
 * - Messenger.send 는 oneway. 이진 탐색 경계: 페이로드 519,922 B 성공 / 519,926 B 실패. 성공 쪽 Message 파셀 520,024 B +
 *   인터페이스 토큰 등 64 B = 트랜잭션 데이터 520,088 B, 여기에 replyTo 바인더 오프셋 8 B 를 더하면 정확히 520,096 B
 * - 예외 메시지의 `data parcel size` 는 트랜잭션 데이터 크기(Message 파셀 + 64 B)다
 * - 디버그 빌드는 HotSwan 이 checked 예외를 감쌀 수 있어 실패 분류는 원인 사슬에서 찾는다(AppSecurity 예제와 같은 방식)
 *
 * #### 크래시 격리 · 재연결
 * - `Process.killProcess(원격 PID)`(같은 UID 라 허용): onServiceDisconnected → binderDied(linkToDeath) → 2~6초 뒤 onServiceConnected(새 PID, 카운터 0).
 *   15초 안에 3번 연달아 kill 해도 매번 자동 재연결 — kill 은 크래시로 세지 않는다
 * - 원격에서 예외(크래시) 1회: 대화상자 없이 1~6초 뒤 자동 재연결. 기본 프로세스(화면)는 PID·카운터 그대로
 * - **1분 안에 두 번째 크래시**: 오류 대화상자(Galaxy 는 "캐시 파일을 삭제할까요?") + onServiceDisconnected → **onBindingDied**,
 *   자동 재연결 없음. unbind 후 bind 하면 바로 새 프로세스로 복구된다
 * - 죽기 전 Messenger 로 보내면 DeadObjectException(`isBinderAlive=false`) — 재연결 뒤에도 옛 참조는 되살아나지 않는다
 * - linkToDeath 콜백은 바인더 스레드에서 오고, 이 기기에서는 7번 모두 onServiceDisconnected 보다 늦게(1~80ms) 왔다
 *
 * #### isolatedProcess
 * - bind 할 때마다 새 PID·새 UID(99016 → 99017 → 99018 …), `Process.isIsolated() = true`
 * - 프로세스 이름에 서비스 클래스명이 붙는다: `패키지:isolated:패키지.…IsolatedProcessDemoService`
 * - INTERNET 권한 DENIED(설치 시 자동 허용 권한도 못 받는다) · 루프백 소켓 `socket failed: EACCES` (일반 프로세스는 ECONNREFUSED 까지 간다)
 * - 기본 프로세스가 filesDir 에 쓴 파일: EACCES 가 아니라 **ENOENT** — 앱 데이터 경로 자체가 보이지 않는다
 * - `getRunningAppProcesses()` 는 SecurityException("Isolated process not allowed…"), 일반 프로세스가 부르면 격리 프로세스는 목록에 없다(다른 UID)
 *
 * #### 메모리(PSS, release)
 * - 설치 직후(dexopt 전): `:remote` 약 104MB — `.dex mmap` 76MB Private Dirty(dex 를 프로세스마다 메모리에 따로 풀어 둠)
 * - `cmd package compile -m speed-profile` 뒤: `:remote` 20MB 안팎, 메인 117MB. `:isolated` 는 dexopt 뒤에도 `.dex mmap` 76MB 로 약 100MB
 * - debug 빌드는 원격 프로세스에도 핫 리로드 런타임이 올라와(logcat 의 hiddenapi 접근 기록) 첫 연결 직후 136~158MB
 */
