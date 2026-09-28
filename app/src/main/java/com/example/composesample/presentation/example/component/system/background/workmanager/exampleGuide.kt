package com.example.composesample.presentation.example.component.system.background.workmanager

/**
 * System/Background/WorkManager 예제 참고 자료
 *
 * ## WorkManagerExampleUI (백그라운드 작업 & 스케줄링)
 * - 공식 문서: https://developer.android.com/develop/background-work/background-tasks/persistent/getting-started
 * 핵심 개념:
 * - Worker.doWork() 에서 백그라운드 작업 정의 → Result.success/retry/failure 반환
 * - OneTimeWorkRequest / PeriodicWorkRequest 로 단발/주기 작업 예약
 * - Constraints(네트워크/충전/배터리)로 실행 조건 지정
 * - WorkManager.getWorkInfoByIdLiveData()로 작업 상태(ENQUEUED/RUNNING/SUCCEEDED) 관찰
 * - setInputData/outputData 로 작업 입출력 전달
 */

/**
 * ## WorkerExceptionHandlerExampleUI (워커 예외 핸들러 — work-runtime 2.11 신규)
 * - 공식 문서(Configuration): https://developer.android.com/reference/androidx/work/Configuration.Builder
 * - 공식 문서(WorkerExceptionInfo): https://developer.android.com/reference/androidx/work/WorkerExceptionInfo
 * - 커스텀 초기화: https://developer.android.com/develop/background-work/background-tasks/persistent/configuration/custom-configuration
 * 핵심 개념:
 * - Configuration 이 받는 예외 핸들러는 4종 — Initialization / Scheduling(둘 다 Consumer<Throwable>),
 *   WorkerInitialization / WorkerExecution(둘 다 Consumer<WorkerExceptionInfo>, 2.11 신규)
 * - WorkerExceptionInfo = workerClassName(FQCN 문자열) + workerParameters(id·tags·inputData·runAttemptCount) + throwable
 * - 발화 조건은 "예외가 워커 밖으로 나갔는가" — Result.failure() 는 정상 종료라 핸들러를 부르지 않는다
 * - 생성 단계 실패는 기본 팩토리(리플렉션) 경로별로 Throwable 모양이 다르다:
 *   생성자 throw → InvocationTargetException(cause 확인 필요) / 생성자 시그니처 불일치 → NoSuchMethodException /
 *   클래스 없음 → ClassNotFoundException
 * - 두 핸들러 모두 WorkerWrapper 가 Resolution.Failed 를 만들기 직전에 호출된다 → 결과는 항상 FAILED(재시도 아님)
 * - 핸들러 호출은 워커 실행 스레드에서 일어나고, safeAccept 로 감싸져 있어 핸들러가 던진 예외는 로그만 남고 삼켜진다
 * - 등록 전제: Application 이 Configuration.Provider 를 구현 + 매니페스트에서 androidx.startup 의
 *   WorkManagerInitializer 를 tools:node="remove" 로 제거(기본 초기화가 먼저 돌면 설정이 무시된다)
 */

/**
 * WorkManager 이벤트 리스너 예제 (work 2.12 신규) 참고 자료
 *
 * - 본 예제 출처: https://msfjarvis.dev/posts/tracking-execution-events-for-your-workmanager-workers/ (AW #746)
 * - Configuration.Builder: https://developer.android.com/reference/androidx/work/Configuration.Builder
 * - WorkInfo.stopReason: https://developer.android.com/reference/androidx/work/WorkInfo#getStopReason()
 * - 작업 체이닝: https://developer.android.com/develop/background-work/background-tasks/persistent/how-to/chain-work
 *
 * 핵심 개념 요약
 *
 * 1) 두 리스너 — 보는 축이 다르다
 *  - ScheduleEventListener  : onEnqueued / onUpdated(old,new) / onUnblocked / onCancelled / onPrerequisiteFailed
 *    → "큐에서 무슨 일이 있었나". 다섯 콜백 모두 **기본 구현이 있어** 필요한 것만 재정의 가능
 *  - ExecutionEventListener : onStarted / onStopped(stopReason, WorkInfo) / onFinished(Result, WorkInfo) / onException(Throwable, WorkInfo)
 *    → "실제로 돌았나". 네 콜백 모두 **abstract 라 전부 구현해야 한다**
 *  - 둘 다 `Configuration.Builder.setExecutionEventListener` / `setScheduleEventListener` 로 등록하며
 *    **앱 전역에 하나씩만** 존재한다. 화면 단위로 붙였다 떼는 API 가 아니다
 *
 * 2) opt-in
 *  - 두 등록 메서드와 리스너 인터페이스가 `@ExperimentalEventsApi` 로 표시돼 있다
 *  - 이 어노테이션은 `@RequiresOptIn(level = WARNING)` 이라 **opt-in 없이도 컴파일은 되고 경고만** 난다
 *  - 향후 버전에서 시그니처가 바뀔 수 있다는 표시이므로 `@OptIn(ExperimentalEventsApi::class)` 로 의사를 남긴다
 *
 * 3) 전역 리스너를 예제 화면에서 쓰는 방법
 *  - 리스너는 Configuration 에만 달 수 있으므로 화면이 직접 등록할 수 없다
 *    → BaseApplication 이 등록하고, 전역 object(WorkEventRecorder)가 받아 두면 화면은 StateFlow 를 구독한다
 *    (같은 폴더 WorkerExceptionReporter 가 예외 핸들러에 대해 이미 쓰던 방식)
 *  - **리스너는 앱의 모든 작업 이벤트를 받는다.** 다른 예제(위치 스냅샷 등)의 작업이 섞이므로
 *    데모 작업에 태그를 달고 그 태그로 걸러야 타임라인을 읽을 수 있다
 *  - WorkInfo 에는 워커 클래스명이 없다 → 사람이 읽을 이름은 `name:<이름>` 태그로 실어 보낸다
 *
 * 4) 콜백이 언제 오는가 (시나리오별)
 *  - 정상 체인 A→B : A onEnqueued → A onStarted → A onFinished → B onUnblocked → B onStarted → B onFinished
 *  - 체인 실패     : A 가 Result.failure() → B 에 **onPrerequisiteFailed**. 실행 축에는 B 관련 이벤트가 하나도 없다
 *  - 실행 중 취소   : **onStopped(stopReason)** + onCancelled. CoroutineWorker 의 delay 는 취소 시 즉시 풀린다
 *  - 제약 미충족    : onEnqueued 만 오고 ENQUEUED 에서 멈춘다 — 실행 축만 보면 "사라진" 것처럼 보이는 구간
 *
 * 5) 2.11 의 Consumer 예외 핸들러와의 관계 (대체가 아니라 층이 다르다)
 *  - 호출 모델: Consumer.accept(동기) vs onException(suspend — WorkManager 가 완료를 기다린다)
 *  - 전달 정보: WorkerExceptionInfo(워커 클래스·WorkerParameters) vs Throwable + WorkInfo(상태·태그)
 *  - 범위    : 예외 전용 vs 수명주기 전체 중 한 콜백
 *  - 호출 순서: **Consumer 핸들러가 먼저, onException 이 나중**(실기기 실측 637ms → 719ms)
 *  - 2.11 핸들러는 2.12 에서도 유지되고 deprecated 도 아니다(javap 확인) → 둘을 함께 등록할 수 있고,
 *    이 예제는 실제로 둘 다 등록해 같은 실패가 양쪽으로 들어오는 것을 타임라인에 나란히 보여준다
 *
 * 본 예제의 단순화 포인트
 *  - 리스너가 전역이라 **BaseApplication 을 수정한다**(예제 파일 안에서만 끝나지 않는 몇 안 되는 예제).
 *    다른 예제의 작업 이벤트는 태그 필터로 버리므로 기존 동작에는 영향이 없다.
 *  - 이벤트는 메모리에 최근 60건만 보관하고 화면을 벗어나면 사라진다.
 *  - 경과 시간의 기준점을 필드로 두지 않고 **목록의 첫 항목 시각**에서 얻는다. 기준점을 가변 필드로 두면
 *    화면(메인 스레드)이 쓰고 리스너(WorkManager 실행기 스레드)가 읽는 공유 상태가 되는데,
 *    실제로 그렇게 만들었더니 목록은 비워지는데 시계만 안 돌아가 두 번째 시나리오가 4702ms 부터 시작했다.
 *  - 시나리오는 고정 delay 로 완료를 기다린다(작업 상태를 구독해 정확히 기다리지 않는다) — 타임라인 관찰이 목적이라
 *    그 편이 코드가 짧고, 실제 대기는 WorkInfo Flow 를 구독하는 것이 정석이다.
 */
