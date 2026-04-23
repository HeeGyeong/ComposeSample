package com.example.composesample.presentation.example.component.system.ai

/**
 * AI / 온디바이스 ML 예제 참고 자료
 *
 * ## Gemini Nano (ML Kit GenAI API)
 * - 공식 문서: https://developers.google.com/ml-kit/genai
 * - Summarization API: https://developers.google.com/ml-kit/genai/summarization/android
 * - Android 공식 AICore: https://developer.android.com/ai/aicore
 *
 * ### 핵심 개념
 * - Gemini Nano는 AICore 시스템 서비스를 통해 온디바이스에서 실행되는 소형 모델
 * - ML Kit GenAI는 AICore 위에 공식 Android API 레이어를 제공 (Summarization / Rewriting / Proofreading / Image Description)
 * - 모델 가중치는 기기에 다운로드되며, 네트워크 없이 추론 가능 → 프라이버시·레이턴시·비용 이점
 *
 * ### Feature Availability 플로우
 * - `FeatureStatus.AVAILABLE` — 모델 준비 완료, 즉시 호출 가능
 * - `FeatureStatus.DOWNLOADABLE` — 지원 기기지만 모델 미다운로드 → `downloadFeature()` 트리거 필요
 * - `FeatureStatus.DOWNLOADING` — 다운로드 진행 중 (진행률 콜백으로 UI 반영)
 * - `FeatureStatus.UNAVAILABLE` — 기기 미지원 (Pixel 8+, Galaxy S24+ 등 AICore 탑재 기기만 해당)
 *
 * ### 하이브리드 라우팅 패턴
 * - 가용성 확인 → AVAILABLE이면 Nano 사용, UNAVAILABLE이면 Cloud API(Gemini API) fallback
 * - 장점: 고사양 기기는 온디바이스로 프라이버시/비용 절감, 저사양 기기는 품질 유지
 * - 주의: Cloud fallback은 네트워크/API 키/과금 조건이 Nano와 달라 일관된 UX 설계 필요
 *
 * ### 제약 사항
 * - 지원 기기 제한 (AICore 탑재 모델 기준)
 * - API가 preview/alpha 단계로 버전 변화 가능성
 * - 입력 길이 제한 (Summarization은 토큰 한도 있음) 및 안전 필터로 거부될 수 있음
 * - 실기기 없이는 실제 추론 확인 불가 → 본 예제는 상태/호출 구조를 Mock으로 시뮬레이션
 */
