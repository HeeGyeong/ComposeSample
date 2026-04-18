package com.example.composesample.presentation.example.component.ui.media.picker

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

/**
 * Embedded Photo Picker Guide
 *
 * 출처: https://android-developers.googleblog.com/2026/01/httpsandroid-developers.googleblog.com202506android-embedded-photo-picker.html%20.html
 *
 * Android의 Embedded Photo Picker를 앱 내에서 직접 임베드하여
 * 사진/영상을 선택하는 방법을 설명합니다.
 *
 * === 핵심 개념 ===
 *
 * Embedded Photo Picker는 기존의 전체 화면 Photo Picker와 달리,
 * 앱의 UI 안에 직접 임베드되어 사용자가 앱을 떠나지 않고도
 * 사진과 영상을 선택할 수 있게 합니다.
 *
 * === 주요 장점 ===
 *
 * 1. 심리스 UX:
 *    - 앱을 벗어나지 않고 사진/영상 선택 가능.
 *    - BottomSheet 등에 임베드하여 자연스러운 흐름 제공.
 *
 * 2. 프라이버시 강화:
 *    - 사용자가 실제로 선택한 사진/영상에만 접근 권한 부여.
 *    - READ_MEDIA_IMAGES/VIDEO 권한 불필요.
 *
 * 3. 클라우드 콘텐츠 접근:
 *    - Google Photos 등 클라우드 미디어 라이브러리 통합 제공.
 *    - 로컬 + 클라우드 통합 브라우징.
 *
 * === Jetpack Compose API ===
 *
 * 1. EmbeddedPhotoPicker Composable:
 *    - 앱 UI에 직접 임베드 가능한 포토 피커.
 *    - SurfaceView를 사용하여 시스템 UI를 호스팅.
 *
 * 2. EmbeddedPhotoPickerFeatureInfo:
 *    - Builder 패턴으로 구성.
 *    - setMaxSelectionLimit(n): 최대 선택 수 제한.
 *    - setOrderedSelection(true): 선택 순서 표시.
 *    - setAccentColor(0xFF0000): 강조색 설정.
 *
 * 3. rememberEmbeddedPhotoPickerState:
 *    - onSelectionComplete: 선택 완료 시 콜백 (Done 버튼 클릭).
 *    - onUriPermissionGranted: URI 접근 권한 부여 시 콜백 (사진 선택).
 *    - onUriPermissionRevoked: URI 접근 권한 해제 시 콜백 (사진 해제).
 *
 * 4. BottomSheetScaffold 통합:
 *    - sheetContent에 EmbeddedPhotoPicker 배치.
 *    - sheetPeekHeight로 축소 시 높이 설정.
 *    - SideEffect로 확장/축소 상태 동기화.
 *
 * === Views API (비 Compose) ===
 *
 * 1. EmbeddedPhotoPickerView:
 *    - XML 레이아웃에 추가 가능.
 *    - addEmbeddedPhotoPickerStateChangeListener로 리스너 등록.
 *
 * 2. EmbeddedPhotoPickerSession:
 *    - notifyConfigurationChanged: 설정 변경 알림.
 *    - notifyPhotoPickerExpanded: 확장 상태 알림.
 *    - notifyResized: 크기 변경 알림.
 *    - notifyVisibilityChanged: 가시성 변경 알림.
 *    - requestRevokeUriPermission: URI 권한 해제 요청.
 *
 * === 요구사항 ===
 *
 * - Android 14 (API 34) 이상 + SDK Extensions 15+.
 * - Jetpack 라이브러리: androidx.photopicker:photopicker-compose:1.0.0-alpha01
 * - 시스템이 렌더링하므로 오버레이/드로잉 불가 (광고 배너처럼 전용 영역).
 *
 * === Google Messages 적용 사례 ===
 *
 * 1. 직관적 배치: 카메라 버튼 아래에 포토 피커 배치.
 * 2. 동적 미리보기: 사진 탭 시 큰 미리보기 표시.
 * 3. 확장 기능: 초기에는 최근 사진만, 확장하면 전체 라이브러리.
 * 4. 선택 존중: 선택한 사진/영상에만 접근 권한 부여.
 *
 * === 요약 ===
 * - Embedded Photo Picker로 앱 내에서 심리스한 사진 선택 UX 제공.
 * - 프라이버시 강화: 사진 전체가 아닌 선택 항목에만 접근.
 * - BottomSheet에 임베드하여 자연스러운 UX 흐름 구현.
 * - Compose와 Views 모두 지원 (각각 별도 의존성).
 */
object EmbeddedPhotoPickerGuide {
    const val GUIDE_INFO = """
        Embedded Photo Picker - Android Jetpack
        
        이 예제는 Android의 Embedded Photo Picker 개념과
        패턴을 시뮬레이션하여 보여줍니다.
        
        실제 구현은 androidx.photopicker 라이브러리가 필요하지만,
        여기서는 동일한 UX 패턴을 직접 구현하여 개념을 시연합니다.
        
        핵심 API:
        - EmbeddedPhotoPicker: 앱 UI에 직접 임베드되는 포토 피커
        - EmbeddedPhotoPickerFeatureInfo: 최대 선택 수, 강조색 등 설정
        - rememberEmbeddedPhotoPickerState: 선택/해제 상태 관리
        - BottomSheetScaffold: 포토 피커를 바텀시트에 임베드
        
        시연 내용:
        1. BottomSheet Picker: 바텀시트에 임베드된 포토 그리드
        2. Selection Management: 사진 선택/해제 상태 관리
        3. Ordered Selection: 순서가 있는 다중 선택
        4. Max Selection Limit: 최대 선택 수 제한
        
        주요 개념:
        - 앱을 벗어나지 않는 심리스한 사진 선택 UX
        - 선택한 사진에만 접근 권한 부여 (프라이버시)
        - SideEffect로 확장/축소 상태 동기화
        - URI 권한 기반 접근 제어
    """
}

@Preview
@Composable
fun EmbeddedPhotoPickerGuidePreview() {
    // Preview for the guide content if needed
}
