package com.example.composesample.presentation.example.component.architecture.pattern.effect

/**
 * Side Effect Example 참고 자료
 *
 * - 공식 문서: https://developer.android.com/develop/ui/compose/side-effects
 *
 * Side Effect 함수 비교:
 *
 * LaunchedEffect(key) { ... }
 * - key가 변경될 때마다 이전 코루틴 취소 후 새로 실행
 * - 컴포지션 진입 시 1회 실행 (key = Unit)
 * - 코루틴이 필요한 작업에 사용 (API 호출, 애니메이션 등)
 *
 * SideEffect { ... }
 * - 리컴포지션 성공 후 매번 실행 (동기, 코루틴 아님)
 * - Compose 외부 시스템에 상태를 동기화할 때 사용
 * - 예: 분석 이벤트 전송, 외부 상태 업데이트
 *
 * DisposableEffect(key) { onDispose { ... } }
 * - key 변경 시 onDispose 실행 후 재실행
 * - 컴포지션 이탈 시 onDispose 실행 (리소스 해제)
 * - 리스너 등록/해제, BroadcastReceiver, 생명주기 옵저버 등
 *
 * rememberCoroutineScope()
 * - 컴포지션 이탈 시 취소되는 CoroutineScope 반환
 * - 이벤트 핸들러(onClick 등) 내부에서 코루틴 시작 시 사용
 * - LaunchedEffect와 달리 UI 이벤트에 반응해 코루틴 시작
 *
 * produceState(initialValue) { ... }
 * - 외부 데이터 소스를 State<T>로 변환
 * - 내부적으로 LaunchedEffect 사용
 *
 * 선택 기준:
 * - 초기화/정리 필요 + 코루틴 → LaunchedEffect
 * - 초기화/정리 필요 + 동기 리소스 → DisposableEffect
 * - 매 리컴포지션 동기 작업 → SideEffect
 * - onClick 등 이벤트 → rememberCoroutineScope
 */
