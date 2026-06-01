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
 * - currentBackStackEntryAsState()로 현재 라우트 추적 → 선택 탭 하이라이트
 * - popUpTo + launchSingleTop 으로 탭 전환 시 백스택 중복 방지
 */
