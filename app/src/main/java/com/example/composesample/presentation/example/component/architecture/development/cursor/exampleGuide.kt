package com.example.composesample.presentation.example.component.architecture.development.cursor

/**
 * Cursor IDE 활용 예제 참고 자료
 *
 * - Cursor 공식 사이트: https://cursor.sh
 * - Claude Code 공식 문서: https://docs.anthropic.com/en/docs/claude-code
 *
 * 핵심 개념:
 * - Cursor: VS Code 기반 AI 통합 에디터. GPT-4/Claude 모델로 코드 생성·수정·리뷰 가능
 * - Composer: 파일 전체 또는 다중 파일을 컨텍스트로 대화형 코드 작성
 * - Tab 자동완성: 단순 완성을 넘어 diff 기반 제안 (Copilot 대비 차별점)
 * - .cursorrules: 프로젝트 루트에 놓으면 AI가 참조하는 코딩 가이드라인 정의
 *
 * Android/Compose 활용 팁:
 * - Composable 함수 단위로 컨텍스트 제공 시 더 정확한 제안
 * - ViewModel/UseCase 분리 구조를 .cursorrules에 명시하면 레이어 위반 방지
 */
