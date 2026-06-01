package com.example.composesample.presentation.example.component.system.platform.file

/**
 * System/Platform/File 예제 참고 자료
 *
 * ## SafFileSelectionUI (Storage Access Framework)
 * - 공식 문서: https://developer.android.com/training/data-storage/shared/documents-files
 * 핵심 개념:
 * - SAF: 권한 선언 없이 시스템 문서 선택기로 사용자가 직접 파일 접근 허용
 * - rememberLauncherForActivityResult(OpenDocument()/CreateDocument()) 로 파일 선택/생성
 * - 반환된 content:// Uri 로 ContentResolver.openInputStream/openOutputStream 접근
 * - takePersistableUriPermission()로 재부팅 후에도 유지되는 영속 권한 획득
 */
