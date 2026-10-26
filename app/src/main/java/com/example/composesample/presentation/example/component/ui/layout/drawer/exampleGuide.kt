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
 * - M3 서랍 3종: Modal 은 스크림으로 나머지 콘텐츠를 막고 위에 뜬다 · Dismissible 은 스크림 없이 콘텐츠 옆에서 레이아웃을
 *   차지하고 열고 닫을 수 있다 · 상시 표시는 PermanentNavigationDrawer. 이 예제는 Modal 만 쓴다
 */
