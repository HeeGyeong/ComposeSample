package com.example.composesample.presentation.example.component.system.platform.exitinfo

/**
 * System/Platform/ExitInfo 예제 참고 자료
 *
 * ## ProcessExitAnalysisExampleUI (프로세스 종료 사후 분석)
 * - ApplicationExitInfo(getReason · getStatus · getTraceInputStream · getProcessStateSummary): https://developer.android.com/reference/android/app/ApplicationExitInfo
 * - ActivityManager(getHistoricalProcessExitReasons · setProcessStateSummary · isLowMemoryKillReportSupported): https://developer.android.com/reference/android/app/ActivityManager
 * - Android 11 기능 소개 — 앱 프로세스 종료 이유: https://developer.android.com/about/versions/11/features#app-process-exit-reasons
 * - ANR(입력 전달 시간 초과 등 ANR 종류와 진단): https://developer.android.com/topic/performance/vitals/anr
 * - Thread.UncaughtExceptionHandler: https://developer.android.com/reference/java/lang/Thread.UncaughtExceptionHandler
 * 핵심 개념:
 * - 죽는 순간에는 앱 코드가 돌지 않는다 → 다음 실행에서 getHistoricalProcessExitReasons(API 30)로 읽는다(최신 순, 이 앱의 모든 프로세스)
 * - 실험 직전에 종류·pid·시각을 commit 으로 남기고, 다음 실행에서 ApplicationExitInfo.getPid() 로 맞춘다.
 *   같은 pid 에서 뒤에 또 실험했다면 앞 실험은 끝나지 않은 것(isSuperseded)
 * - 종료 방식별 기록: ANR → REASON_ANR(설명 'user request after error: Input dispatching timed out …', 트레이스 있음) ·
 *   크래시 → REASON_CRASH(설명 'crash', 예외 클래스·메시지 없음 — 앱의 기본 처리기가 따로 남겨야 한다) ·
 *   exitProcess(0) → REASON_EXIT_SELF(status = 종료 코드) · Process.killProcess → REASON_SIGNALED(status 9 = SIGKILL)
 * - getTraceInputStream: ANR 이면 스레드 덤프 텍스트(main 스레드 블록을 잘라 본다), API 31+ 네이티브 크래시는 tombstone protobuf, 없으면 null
 * - debug(HotSwan) 빌드의 ANR 트레이스에는 앱 프레임이 보이지 않는다(MethodHandle·Transformers 프레임) → 원인 분석은 release 트레이스로
 * - setProcessStateSummary: 128바이트 초과는 IllegalArgumentException("Data size is too large"), 한글은 UTF-8 3바이트라 42자까지,
 *   마지막 호출 값 하나만 남는다, 개인 정보 금지·잦은 호출은 제한될 수 있음
 * - 기본 처리기는 이전 처리기를 받아 두고 기록 뒤 넘긴다(프로세스는 원래 처리기가 끝낸다) · 화면을 떠나면 원래 처리기로 되돌린다
 */
