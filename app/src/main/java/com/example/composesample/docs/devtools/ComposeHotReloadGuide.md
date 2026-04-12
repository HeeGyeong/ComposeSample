# Compose Hot Reload (HotSwan) 가이드

## 개요

Compose Hot Reload(HotSwan)는 `.kt` 파일을 저장하면 **앱 재시작 없이 1초 내에** 실기기/에뮬레이터에 변경사항을 반영하는 개발 도구입니다.

- 네비게이션 스택, 스크롤 위치, 폼 입력값, ViewModel 데이터가 모두 보존됨
- 상수값(padding, color, string)은 컴파일 생략 후 ~50ms 내 반영 (Literal Patching)

## 동작 파이프라인 (5단계)

```
1. 파일 변경 감지 → 모듈 식별
2. 변경된 코드만 증분 컴파일
3. DEX 비교로 변경된 클래스만 추출
4. 네이티브 에이전트로 메모리 내 클래스 교체
5. 영향받는 Compose 스코프만 리컴포지션
```

## 지원 범위

| 지원 항목 | 비고 |
|-----------|------|
| Composable 함수 본문 | 텍스트, 색상, 레이아웃, 로직 |
| 비-Composable 함수 | ViewModel, 유틸리티 |
| 새 Composable 추가 및 재배치 | |
| XML 리소스 값 | |
| data class 프로퍼티 | |
| extension / suspend 함수 | |

## 버전 요구사항

> **⚠ HotSwan 1.2.1은 Kotlin 2.3.0 이상을 요구합니다.**
> 현재 프로젝트는 Kotlin 2.1.0을 사용 중이므로 Kotlin 버전 업그레이드 후 적용 가능합니다.
> Kotlin 업그레이드 시 KSP 버전도 함께 맞춰야 합니다.

## 설치 방법 (Kotlin 2.3.0+ 업그레이드 후)

### 1. IDE 플러그인 설치
`Settings → Plugins → Marketplace` → **"Compose HotSwan"** 검색 → Install → IDE 재시작

### 2. Gradle 설정

**libs.versions.toml:**
```toml
[plugins]
hotswan-compiler = { id = "com.github.skydoves.compose.hotswan.compiler", version = "1.2.1" }
```

**루트 build.gradle:**
```groovy
alias(libs.plugins.hotswan.compiler) apply false
```

**app/build.gradle:**
```groovy
alias(libs.plugins.hotswan.compiler)
```

### 3. IDE 설정
`Settings → Tools → Compose HotSwan`
- Module Path: `:app` (기본값)
- App Package Name: `com.example.composesample` (applicationId와 일치)

## 주의사항

- **Kotlin 2.3.0 이상 필수** — KSP 버전도 Kotlin 버전에 맞춰 업그레이드 필요
- **IDE와 Gradle 플러그인 버전이 반드시 일치**해야 함
- IDE 플러그인을 업데이트하면 `libs.versions.toml`의 버전도 함께 변경할 것
- 구조적 변경(클래스 계층 변경, 인터페이스 추가 등)은 앱 재시작이 필요할 수 있음
- Hot Reload는 개발 편의 도구이며, 최종 빌드에는 영향을 주지 않음

## 참고 자료

- 공식 사이트: https://hotswan.dev
- 설치 가이드: https://hotswan.dev/install
- 블로그: https://hotswan.dev/blog/compose-hot-reload
- JetBrains Marketplace: https://plugins.jetbrains.com/plugin/30551-compose-hotswan/
- 이슈 트래커: https://github.com/skydoves/compose-hotswan-issuetracker
