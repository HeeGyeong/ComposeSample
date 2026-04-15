package com.example.composesample.presentation.example.component.ui.material3

/**
 * Material 3 Expressive 예제 참고 자료
 *
 * ## Material3 1.4.0 릴리즈 노트
 * - 공식 릴리즈: https://developer.android.com/jetpack/androidx/releases/compose-material3#1.4.0
 *
 * ### SecureTextField / OutlinedSecureTextField
 * - TextFieldState 기반 비밀번호 입력 전용 컴포넌트
 * - TextObfuscationMode: Hidden(즉시 난독화), RevealLastTyped(마지막 문자 잠시 표시), Visible(그대로 표시)
 * - obfuscationCharacter 파라미터로 난독화 문자 커스텀 가능 (기본값 '•')
 *
 * ### HorizontalFloatingToolbar
 * - ExperimentalMaterial3ExpressiveApi 필요
 * - leadingContent / content / trailingContent 3영역 구조
 * - FloatingToolbarDefaults.floatingToolbarState()로 확장/축소 상태 관리
 * - 스크롤 연동 시 expanded 파라미터를 스크롤 상태와 바인딩
 *
 * ### VerticalDragHandle
 * - BottomSheet용 시각적 드래그 핸들
 * - DragHandleSizes, DragHandleColors, DragHandleShapes로 커스터마이징
 *
 * ### ButtonGroup 개선 (1.4.0)
 * - verticalAlignment 파라미터 추가
 * - ButtonGroupDefaults 기본 오버플로우 인디케이터 제공
 * - Modifier.align() 확장으로 개별 버튼 정렬 제어
 *
 * ### 참고: Material Icons 지원 중단
 * - androidx.compose.material:material-icons-core/extended는 1.7.8이 마지막 버전
 * - 향후 Material Symbols(Google Fonts Vector Drawable XML)로 마이그레이션 권장
 * - https://fonts.google.com/icons
 */
