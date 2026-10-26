package com.example.composesample.presentation.example.component.navigation

/**
 * Navigation 예제 참고 자료
 *
 * ## BottomNavigationActivity / NavigationComponent (Bottom Navigation)
 * - 공식 문서: https://developer.android.com/develop/ui/compose/navigation
 * - Material3 NavigationBar: https://developer.android.com/develop/ui/compose/components/navigation-bar
 * 핵심 개념:
 * - NavHost + NavController로 화면 그래프 구성, composable(route) 로 목적지 정의
 * - Material3 NavigationBar/NavigationBarItem 으로 하단 탭 (M2 BottomNavigation 대체)
 * - 이 예제는 선택 탭을 NavigationBar 쪽 상태(selectedItem)로 들고 탭마다 navController.navigate(route) 만 부른다 —
 *   뒤로 가기로 화면이 바뀌어도 하이라이트는 따라가지 않고, 같은 탭을 다시 누르면 백스택에 쌓인다
 * - 실무 패턴: currentBackStackEntryAsState() 의 destination 으로 선택 탭을 정하고(백스택과 항상 일치),
 *   navigate(route) { popUpTo(startDestination) { saveState = true }; launchSingleTop = true; restoreState = true } 로 중복을 막는다
 */
