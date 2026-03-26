package com.example.composesample.presentation.example.component.architecture.pattern.remember

/**
 * Remember Patterns 참고 자료
 *
 * - rememberSaveable 공식 문서:
 *   https://developer.android.com/develop/ui/compose/state#restore-ui-state
 *
 * - rememberUpdatedState 공식 문서:
 *   https://developer.android.com/develop/ui/compose/side-effects#rememberupdatedstate
 *
 * - derivedStateOf 공식 문서:
 *   https://developer.android.com/develop/ui/compose/side-effects#derivedstateof
 *
 * 핵심 개념:
 * - rememberSaveable: remember와 달리 화면 회전·프로세스 재시작 후에도 상태 유지 (Bundle 직렬화)
 * - rememberUpdatedState: LaunchedEffect 등 오래 실행되는 이펙트 안에서 항상 최신 람다/값을 참조
 * - derivedStateOf: 다른 State로부터 계산된 값을 메모이제이션. key 없이 remember { derivedStateOf { } } 형태로 사용
 */
