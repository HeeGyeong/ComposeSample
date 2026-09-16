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
