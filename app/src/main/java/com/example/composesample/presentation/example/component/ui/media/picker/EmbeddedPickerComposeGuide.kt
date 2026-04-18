package com.example.composesample.presentation.example.component.ui.media.picker

/**
 * 📚 Embedded Android Photo Picker in Jetpack Compose 학습 가이드
 *
 * 출처: https://victorbrandalise.com/embedded-android-photo-picker-in-jetpack-compose/
 *
 * Embedded Photo Picker는 시스템 포토 피커를 앱 UI 계층 안에 렌더링하는 방식입니다.
 * 내부적으로 SurfaceView.setChildSurfacePackage를 통해 시스템이 직접 그리기 때문에
 * 기존 포토 피커와 동일한 보안·프라이버시 속성을 유지합니다.
 *
 * 핵심 변화: 사용자가 '당신의 화면'에 머물면서 미디어를 탐색·선택합니다.
 * Activity가 resumed 상태를 유지하므로 선택 변경에 실시간 반응이 가능합니다.
 *
 * =================================================================================================
 * 🎯 요구 사항
 * =================================================================================================
 *
 * - Android 14 (API 34) 이상
 * - SDK Extensions 15 이상 (UPSIDE_DOWN_CAKE extension)
 * - minSdk 34 필수
 *
 * ```kotlin
 * dependencies {
 *     implementation("androidx.photopicker:photopicker-compose:1.0.0-alpha01")
 * }
 * ```
 *
 * =================================================================================================
 * 🔍 핵심 개념 1: 가용성 체크
 * =================================================================================================
 *
 * UI 진입점 근처에 배치하는 헬퍼 함수입니다:
 *
 * ```kotlin
 * import android.os.Build
 * import android.os.ext.SdkExtensions
 *
 * fun isEmbeddedPhotoPickerAvailable(): Boolean {
 *     // Embedded picker는 Android 14+ 에서만 동작
 *     if (Build.VERSION.SDK_INT < 34) return false
 *     // SDK Extension이 실제 게이트
 *     return SdkExtensions.getExtensionVersion(
 *         Build.VERSION_CODES.UPSIDE_DOWN_CAKE
 *     ) >= 15
 * }
 * ```
 *
 * =================================================================================================
 * 🔍 핵심 개념 2: Compose 통합 구조
 * =================================================================================================
 *
 * 세 가지 핵심 구성 요소:
 *
 * ```kotlin
 * // 1. 상태 홀더: 선택 콜백 등록
 * val pickerState = rememberEmbeddedPhotoPickerState(
 *     onSelectionComplete = { /* 완료 처리 */ },
 *     onUriPermissionGranted = { uris -> attachments += uris },
 *     onUriPermissionRevoked = { uris -> attachments -= uris.toSet() }
 * )
 *
 * // 2. 기능 설정: 최대 선택 수, 순서 유지 등
 * val featureInfo = remember {
 *     EmbeddedPhotoPickerFeatureInfo.Builder()
 *         .setMaxSelectionLimit(5)
 *         .setOrderedSelection(true)  // 선택 순서 보장
 *         .build()
 * }
 *
 * // 3. UI에 배치
 * EmbeddedPhotoPicker(
 *     modifier = Modifier.fillMaxWidth().heightIn(min = 240.dp),
 *     state = pickerState,
 *     embeddedPhotoPickerFeatureInfo = featureInfo
 * )
 * ```
 *
 * =================================================================================================
 * 🔍 핵심 개념 3: BottomSheet 통합 + setCurrentExpanded
 * =================================================================================================
 *
 * 피커를 BottomSheet 안에 배치하고, 피커 확장 상태와 시트 상태를 동기화합니다:
 *
 * ```kotlin
 * val sheetState = rememberBottomSheetScaffoldState(
 *     bottomSheetState = rememberStandardBottomSheetState(
 *         initialValue = SheetValue.Hidden,
 *         skipHiddenState = false
 *     )
 * )
 *
 * // 핵심: SideEffect로 피커의 expanded 상태를 시트와 동기화
 * SideEffect {
 *     val expanded = sheetState.bottomSheetState.targetValue == SheetValue.Expanded
 *     pickerState.setCurrentExpanded(expanded)
 * }
 *
 * BottomSheetScaffold(
 *     sheetContent = {
 *         EmbeddedPhotoPicker(state = pickerState, ...)
 *     }
 * ) { ... }
 * ```
 *
 * =================================================================================================
 * 🔍 핵심 개념 4: 선택 동기화 오너십 모델 (중요한 함정!)
 * =================================================================================================
 *
 * **"이것은 나중에 반드시 문제가 됩니다"** — 블로그 원문
 *
 * `deselectUri`/`deselectUris`를 호출해도 `onUriPermissionRevoked`가 자동으로
 * 트리거되지 않습니다. 앱 state를 직접 업데이트해야 합니다:
 *
 * ```kotlin
 * // ❌ 이렇게 하면 앱 state가 업데이트되지 않음
 * pickerState.deselectUri(uri)
 *
 * // ✅ 올바른 방법: deselectUri + 직접 state 업데이트
 * scope.launch {
 *     pickerState.deselectUri(uri)          // 피커에게 알림
 *     attachments = attachments - uri        // 앱 state 직접 업데이트
 * }
 * ```
 *
 * 이 동작은 의도적입니다. 오너십이 명확하게 분리됩니다:
 *  - **피커**가 owns: 선택 가능한 항목들 (selectables)
 *  - **앱**이 owns: 선택 항목의 표현과 영속화 (representation & persistence)
 *
 * =================================================================================================
 * 🔍 핵심 개념 5: URI 수명 관리
 * =================================================================================================
 *
 * 포토 피커의 URI 접근 수명:
 *  - 기본: 기기 재시작 또는 앱 종료 시 만료
 *  - 드래프트/업로드 큐가 있는 경우 더 긴 접근이 필요
 *
 * ```kotlin
 * // 더 긴 접근이 필요한 경우 (드래프트, 업로드 대기 등)
 * contentResolver.takePersistableUriPermission(
 *     uri,
 *     Intent.FLAG_GRANT_READ_URI_PERMISSION
 * )
 * ```
 *
 * ⚠️ 미디어 그랜트 제한: 앱은 동시에 최대 5,000개의 미디어 그랜트를 가질 수 있습니다.
 * 초과 시 오래된 그랜트가 자동으로 제거됩니다.
 * 일반 소비자 흐름에는 충분하지만, 피커를 파이프라인으로 사용하는 앱은 주의가 필요합니다.
 *
 * =================================================================================================
 * 🔍 핵심 개념 6: 테스트
 * =================================================================================================
 *
 * `androidx.photopicker`에는 전용 테스트 아티팩트와 `TestEmbeddedPhotoPickerProvider`가 있습니다:
 *
 * ```kotlin
 * // 테스트 의존성
 * androidTestImplementation(
 *     "androidx.photopicker:photopicker-testing:1.0.0-alpha01"
 * )
 *
 * // 테스트에서 가짜 피커 주입
 * @get:Rule
 * val photoPickerRule = TestEmbeddedPhotoPickerProvider.createRule()
 * ```
 *
 * =================================================================================================
 * 🚀 아키텍처 요약
 * =================================================================================================
 *
 * ```
 * BottomSheetScaffold
 * ├── sheetContent → EmbeddedPhotoPicker (시스템 SurfaceView)
 * │                    ↕ onUriPermissionGranted/Revoked
 * └── content → 앱 UI
 *     ├── 첨부 파일 그리드 (앱이 owns)
 *     └── 행동 버튼들
 *
 * 확장 동기화: SideEffect { pickerState.setCurrentExpanded(sheetExpanded) }
 * 선택 해제: deselectUri(uri) + attachments -= uri  (둘 다 필요!)
 * ```
 */
object EmbeddedPickerComposeGuide
