package com.example.composesample.presentation.example.component.ui.media.picker

/**
 * Media/Picker 예제 참고 자료
 *
 * ## EmbeddedPhotoPickerExampleUI (Embedded Photo Picker 개념 시연)
 * - 출처: https://android-developers.googleblog.com/2026/01/httpsandroid-developers.googleblog.com202506android-embedded-photo-picker.html%20.html
 * 핵심 개념:
 * - 시스템 Photo Picker를 앱 UI 안에 직접 임베드해 화면 이탈(전체 화면 전환) 없이 사진/영상 선택
 * - EmbeddedPhotoPickerFeatureInfo(Builder)로 최대 선택 수·순서 유지·강조색 설정, rememberEmbeddedPhotoPickerState로 선택완료/권한부여/권한해제 콜백 관리
 * - 선택한 항목에만 URI 접근 권한 부여(READ_MEDIA_IMAGES/VIDEO 불필요)로 프라이버시 강화, Android 14(API 34)+ SDK Extensions 15+ 필요
 *
 * ## EmbeddedPickerComposeExampleUI (Embedded Photo Picker의 Compose 통합 심화)
 * - 출처: https://victorbrandalise.com/embedded-android-photo-picker-in-jetpack-compose/
 * 핵심 개념:
 * - SurfaceView.setChildSurfacePackage로 시스템이 직접 렌더링(기존 Photo Picker와 동일한 보안 속성 유지), Activity가 resumed 상태 유지되어 선택 변경에 실시간 반응 가능
 * - BottomSheetScaffold + SideEffect로 피커의 setCurrentExpanded와 바텀시트 확장 상태 동기화
 * - deselectUri() 호출만으로는 onUriPermissionRevoked가 자동 트리거되지 않음 → 앱 state를 직접 갱신해야 함(피커=선택 가능 항목 소유, 앱=선택 결과 표현·영속화 소유라는 오너십 분리 함정 주의)
 */
