package com.example.composesample.presentation.example.component.system.platform.version

/**
 * System/Platform/Version 예제 참고 자료
 *
 * ## TargetSDK34ExampleUI (Android SDK 버전 대응 & 런타임 권한)
 * - 공식 문서(권한): https://developer.android.com/training/permissions/requesting
 * - 동작 변경: https://developer.android.com/about/versions
 * 핵심 개념:
 * - rememberLauncherForActivityResult(RequestPermission/RequestMultiplePermissions) 로 런타임 권한 요청
 * - shouldShowRequestPermissionRationale 로 거부 사유 안내 분기
 * - Build.VERSION.SDK_INT 분기로 OS 버전별 동작 변경 대응 (PermissionConstValue에 권한 상수 정의)
 * - targetSDK 상향 시 추가되는 권한(미디어 세분화, 알림 등) 처리
 */
