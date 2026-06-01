package com.example.composesample.presentation.example.component.ui.layout.drawer

/**
 * UI/Layout/Drawer 예제 참고 자료
 *
 * ## ModalDrawerExampleUI / ScaffoldDrawerExampleUI (Navigation Drawer)
 * - 공식 문서: https://developer.android.com/develop/ui/compose/components/drawer
 * 핵심 개념:
 * - ModalNavigationDrawer + DrawerState(rememberDrawerState)로 모달 서랍 메뉴 구현
 * - drawerContent 슬롯에 메뉴 항목 배치, gesturesEnabled로 스와이프 열림 제어
 * - Scaffold와 결합 시 topBar의 메뉴 아이콘 클릭 → scope.launch { drawerState.open() }
 * - M3 ModalNavigationDrawer / DismissibleNavigationDrawer 차이(상시 표시 여부)
 */
