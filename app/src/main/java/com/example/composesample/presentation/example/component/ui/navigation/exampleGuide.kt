package com.example.composesample.presentation.example.component.ui.navigation

/**
 * Navigation 예제 참고 자료
 *
 * ## NestedRoutesNav3ExampleUI (Navigation 3 NavBackStack/EntryDecorator 시뮬레이션)
 * - 출처: https://proandroiddev.com/nested-routes-with-navigation-3-af0cd8223986
 * - 공식 문서: https://developer.android.com/guide/navigation/navigation-3
 * 핵심 개념:
 * - NavBackStackSimulator/EntryDecoratorSimulator/RouteComponentManagerSimulator로 실제 Navigation 3의 push/pop, 상태 저장/복원, 컴포넌트 라이프사이클을 체험
 * - @Stable 클래스 + mutableStateListOf/mutableStateMapOf로 불필요한 recomposition을 방지하고, rememberSaveable(currentRoute)로 화면 회전 시에도 입력 상태 보존
 * - 실제 API 패턴: rememberNavBackStack<Route>() + NavDisplay(backstack, entryProvider, entryDecorators), @Serializable Route로 타입 안전 인자 전달과 딥링킹 지원
 * - EntryDecorator(navigation3 1.2.0 기준): NavDisplay 의 기본 entryDecorators 는
 *   listOf(rememberSaveableStateHolderNavEntryDecorator()) — 항목별 rememberSaveable 상태 저장/복원.
 *   항목별 ViewModel 범위는 별도 아티팩트 lifecycle-viewmodel-navigation3 의 rememberViewModelStoreNavEntryDecorator() 를 더한다.
 *   장면 설정·백스택 생명주기 데코레이터(SceneSetupNavEntryDecorator 등)는 internal 이라 NavDisplay 가 알아서 붙인다
 *   (알파 시절 이름 SavedStateNavEntryDecorator 는 없다)
 */
