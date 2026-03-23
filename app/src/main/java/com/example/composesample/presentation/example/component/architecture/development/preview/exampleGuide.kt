package com.example.composesample.presentation.example.component.architecture.development.preview

/**
 * Compose Preview Internals Example 참고 자료
 *
 * - 원문: https://doveletter.dev/preview/articles/compose-preview-internals
 *
 * 핵심 개념:
 * - @Preview는 순수 메타데이터 어노테이션 (@Retention(BINARY) → 바이트코드에 유지)
 * - Studio가 PSI/UAST로 스캔 → ComposeViewAdapter → ComposableInvoker (Reflection)
 * - ComposableInvoker: $changed/$default 파라미터를 직접 구성하여 Compose 컴파일러 ABI와 일치
 * - LocalInspectionMode: Preview 환경 감지 표준 방법
 * - ViewInfo 트리: 렌더링 픽셀 ↔ 소스 줄 번호 매핑
 *
 * 내장 MultiPreview 어노테이션:
 * - @PreviewLightDark, @PreviewScreenSizes, @PreviewFontScale, @PreviewDynamicColors
 */
