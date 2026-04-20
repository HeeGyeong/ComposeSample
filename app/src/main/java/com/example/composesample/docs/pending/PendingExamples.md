# 추가 예정 예제 리스트 (Android Weekly #723 기준)

> 작성일: 2026-04-20
> 출처: https://androidweekly.net/issues/issue-723

Android Weekly #723을 검토한 결과, 아래 5개 항목을 우선순위 중간 이상으로 선정했습니다. 우선순위 순서대로 작업을 진행합니다.

---

## 🔴 우선순위 높음

### 1. Navigation 3 ViewModel Scope
- **카테고리**: `component/navigation/nav3/` (신규)
- **파일명**: `Nav3ViewModelScopeExampleUI.kt`
- **내용**:
  - Navigation 3에서 ViewModel 스코프 동작 변화 시연
  - 라이프사이클 확장(`rememberViewModelStoreOwner` 등)으로 이전 버전 동작 복원
- **출처**: medium.com/@domen.lanisnik (Issue #723)
- **제약**: Navigation 3 라이브러리 alpha 버전 가능성 → 버전 확인 후 진행
- **적합 이유**: CLAUDE.md "ViewModel 범위 규칙"과 직결, Nav3 예제 부재

### 2. Koin Compiler Plugin (Compile Safety)
- **카테고리**: `architecture/development/di/` (신규)
- **파일명**: `KoinCompilerPluginExampleUI.kt`
- **내용**:
  - `@Module`, `@Single`, `@Factory`, `@KoinViewModel` 애노테이션 기반 DI
  - 런타임 에러 → 컴파일 타임 검증 전환 사례
  - 기존 수동 모듈(`ApiModule`, `ViewModelModule`) vs 애노테이션 비교
- **출처**: blog.insert-koin.io (Issue #723)
- **관련 문서**: `docs/di/DIRules.md`

---

## 🟡 우선순위 중간

### 3. Gemini Nano On-Device AI (ML Kit GenAI)
- **카테고리**: `system/ai/` (신규 서브카테고리)
- **파일명**: `GeminiNanoExampleUI.kt`
- **내용**:
  - ML Kit GenAI API로 온디바이스 추론
  - Nano(온디바이스) ↔ Cloud 하이브리드 라우팅 패턴
  - 기기 미지원 시 fallback 처리
- **출처**: medium.com/@Y4583L (Issue #723)
- **제약**: Pixel 8+ 지원 기기 제한, API 프리뷰 단계 가능 → 에뮬레이터 대응 필요

### 4. Halogen — Runtime Material 3 Theme Generation
- **카테고리**: `ui/material3/`
- **파일명**: `HalogenThemeGenerationExampleUI.kt`
- **내용**:
  - 자연어 프롬프트 → 런타임 ColorScheme 자동 생성
  - 기존 Material 3 Expressive 예제의 확장형
- **출처**: github.com/himattm/halogen (Issue #723)
- **제약**: 외부 라이브러리 의존, AI API 키 필요 가능성 → 로컬 프리셋 fallback 고려

### 5. Airbnb Month Picker Dial (ChromaDial 기반)
- **카테고리**: `component/interaction/` 또는 `ui/canvas/`
- **파일명**: `MonthPickerDialExampleUI.kt`
- **내용**:
  - Canvas 그리기 + 드래그 제스처 조합
  - 극좌표 계산 기반 각도 → 월 매핑
  - 스냅 애니메이션
- **출처**: sinasamaki.com (Issue #723)
- **적합 이유**: 제스처 + 커스텀 측정 학습용, `ComposeLoaders`와 차별화

---

## 진행 원칙

1. 우선순위 순서대로 진행 (1 → 5)
2. 각 예제 완료 시 CLAUDE.md "새 예제 추가 방법" 4단계 준수
3. 완료 후 빌드 검증 → 커밋 → 푸시
4. 완료 항목은 `project_pending_examples.md` 메모리에서 ✅ 표시 + 커밋 해시 기록
