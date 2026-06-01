package com.example.composesample.presentation.example.component.system.background.workmanager

/**
 * System/Background/WorkManager 예제 참고 자료
 *
 * ## WorkManagerUI (백그라운드 작업 & 스케줄링)
 * - 공식 문서: https://developer.android.com/develop/background-work/background-tasks/persistent/getting-started
 * 핵심 개념:
 * - Worker.doWork() 에서 백그라운드 작업 정의 → Result.success/retry/failure 반환
 * - OneTimeWorkRequest / PeriodicWorkRequest 로 단발/주기 작업 예약
 * - Constraints(네트워크/충전/배터리)로 실행 조건 지정
 * - WorkManager.getWorkInfoByIdLiveData()로 작업 상태(ENQUEUED/RUNNING/SUCCEEDED) 관찰
 * - setInputData/outputData 로 작업 입출력 전달
 */
