package com.example.composesample.presentation.example.component.architecture.development.di

/**
 * Koin Compiler Plugin (Koin Annotations) 예제 참고 자료
 *
 * ## 공식 자료
 * - 블로그: https://blog.insert-koin.io/koin-compiler-plugin-compile-time-safety-for-dependency-injection-0a5f4a6b5e0b
 * - 공식 문서: https://insert-koin.io/docs/reference/koin-annotations/start
 * - KSP 저장소: https://github.com/InsertKoinIO/koin-annotations
 *
 * ## 핵심 개념
 * - **기존 수동 DSL (module { ... })**: 런타임에 그래프를 해석 → 누락된 의존성은 앱 실행 후에야 NoDefinitionFoundException으로 발견
 * - **Koin Annotations (KSP)**: `@Module`, `@Single`, `@Factory`, `@KoinViewModel` 애노테이션을 KSP가 스캔하여
 *   컴파일 타임에 모듈 코드를 생성하고 누락 의존성을 빌드 오류로 노출
 *
 * ## 주요 애노테이션
 * - `@Module` + `@ComponentScan("package")` — 모듈 선언 + 패키지 스캔
 * - `@Single` — `single { ... }` 싱글톤
 * - `@Factory` — `factory { ... }` 매 호출마다 새 인스턴스
 * - `@KoinViewModel` — `viewModel { ... }` ViewModel 생명주기 연동
 * - `@Named("qualifier")` — 한정자 (기존 DSL의 `named(...)`와 동일)
 * - `@Property("key")` — Koin properties 주입
 * - `@InjectedParam` — 런타임 파라미터 주입 (`parametersOf`)
 *
 * ## Gradle 설정 (참고용)
 * ```kotlin
 * plugins { id("com.google.devtools.ksp") }
 *
 * dependencies {
 *     implementation("io.insert-koin:koin-annotations:<ver>")
 *     ksp("io.insert-koin:koin-ksp-compiler:<ver>")
 * }
 * ```
 *
 * ## 전환 전략
 * - 점진적 마이그레이션 가능: 기존 수동 module과 `@Module` 클래스를 혼합해서 `startKoin`에 넘길 수 있음
 * - 복잡한 조건부 바인딩(런타임 분기)은 수동 DSL로 유지하는 것이 자연스러움
 * - API 한정자가 많은 프로젝트(`named("post")` 등)는 `@Named("post")`로 1:1 대응
 */
