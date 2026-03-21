package com.example.composesample.presentation.example.component.architecture.development.rebound

/**
 * Rebound Example 참고 자료
 *
 * Rebound: Context-Aware Recomposition Monitoring for Jetpack Compose
 * - 공식 소개글: https://aditlal.dev/compose-rebound/
 *
 * 핵심 개념:
 * - 역할 기반 리컴포지션 예산 (Screen: 3/s, Leaf: 5/s, Animated: 120/s 등)
 * - Kotlin 컴파일러 플러그인 + IDE 플러그인 구조
 * - $changed 비트마스크로 파라미터 변경 여부 분석
 *
 * 설치 (build.gradle.kts):
 * plugins {
 *     id("io.github.aldefy.rebound") version "0.2.1"
 * }
 */
