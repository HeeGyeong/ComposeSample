---
name: design-options
description: 설계 결정이 필요한 문제를 관점이 다른 설계자 N명이 독립 설계하고 judge panel 이 교차 심사해 비교표·추천안을 합성하는 멀티에이전트 하네스. "방향 A/B/C 중 뭐가 나은지", 아키텍처/접근법/리팩터링 방향 결정 시 사용.
---

# design-options — 설계 비교 결정형 하네스

접근 방향이 미결정인 문제에 대해, **관점이 다른 설계자 3명이 독립 설계**하고 **judge panel(심사 3렌즈)** 이 교차 심사해 **비교표 + 추천안**을 합성하는 read-only 하네스입니다. 기존 하네스와의 역할: `explore-codebase`=탐색형, `impact-analyze`=검증형, **design-options=결정형**. 추천안이 정해지면 그 변경면을 `impact-analyze` 에 넘기는 체인이 자연스럽습니다.

호출: `/design-options <문제/대상>`
- 예: `/design-options ExampleRouter when-분기를 sealed/맵 기반으로 전환할지`
- 예: `/design-options 예제 공통 ViewModel 상태 보존(process death) 방향`

---

## 오케스트레이터 절차 (메인 루프가 따른다)

### 1. 문제 정의
`$ARGUMENTS` 에서 문제/대상을 추출한다. 제약이 모호하면(릴리즈 일정, 호환 요구, 허용 가능한 변경 범위 등) 사용자에게 1~2개만 확인하고 시작한다.

### 2. 문제면 정찰 (인라인, read-only)
관련 코드를 grep/Read 로 훑어 설계자들이 공유할 컨텍스트를 만든다:
- `problemSurface = { problem, constraints:[], currentState, keyFiles:[] }`
- `currentState` = 현재 구조와 문제 지점 요약, `keyFiles` = 설계의 출발점이 될 파일 목록.

### 3. Workflow 실행
아래 §임베드 Workflow 스크립트를 `args: { surface: problemSurface }` 로 호출한다(실제 JSON 값으로 전달).
- 문제 특성상 기본 렌즈(minimal-change/root-fix/compat-first)가 안 맞으면 `args.designerLenses` 로 오버라이드할 수 있다 (`[{key, desc}]` 3개 권장).

### 4. 결과 제시
반환된 `report`(비교표+추천안 markdown)를 보여준다.
- 비교표는 **결정 자료 초안**이며 최종 결정은 사용자가 한다는 점을 명시한다.
- 사용자가 방향을 결정하면, 그 방향으로 작업 spec(00-overview + step 분해)을 이어서 작성할지 묻는다. 채택된 설계의 `stepBreakdown` 을 초안으로 쓴다.

---

## 임베드 Workflow 스크립트

> `Workflow` 도구의 `script` 인자로 전달. `args` = `{ surface, designerLenses? }` 를 실제 JSON 값으로 넘긴다.
> 안전 규칙: `Date.now()/Math.random()/new Date()` 미사용, schema 강제 출력, `.filter(Boolean)` 로 null 방어.

```javascript
export const meta = {
  name: 'design-options',
  description: '관점별 독립 설계 fan-out → judge panel 교차 심사 → 비교표·추천안 합성',
  phases: [
    { title: 'Design', detail: '렌즈별 설계자 3명이 독립 설계' },
    { title: 'Judge', detail: '설계마다 심사 3렌즈(비용/리스크/호환) 교차 심사' },
    { title: 'Synthesize', detail: '비교표 + 추천안 + step 분해 초안 합성' },
  ],
}

const surface = (args && args.surface) || {}
const surfaceText = JSON.stringify(surface, null, 2)

if (!surface.problem) {
  log('problemSurface.problem 이 비어 있습니다. 메인 루프에서 문제 정의를 채워 다시 호출하세요.')
  return { report: '', designs: [], judgments: [] }
}

const DESIGNER_LENSES = (args && args.designerLenses) || [
  { key: 'minimal-change', desc: '최소 변경·릴리즈 안전 우선. 기존 구조를 유지하고 국소 수정으로 문제를 해결한다. 변경 파일 수와 회귀 범위를 최소화.' },
  { key: 'root-fix', desc: '근본 구조 개선 우선. 재발을 방지하고 기술 부채를 해소하는 구조적 해법을 설계한다. 변경이 커져도 올바른 구조를 지향.' },
  { key: 'compat-first', desc: '호환 우선. 네비게이션/상태복원(rememberSaveable·SavedState)/기존 로컬·서버 데이터와의 공존과 단계적 마이그레이션 경로를 최우선으로 설계한다.' },
]

const DESIGN_SCHEMA = {
  type: 'object',
  required: ['lens', 'title', 'summary', 'changes', 'pros', 'cons', 'evidenceFiles', 'stepBreakdown'],
  properties: {
    lens: { type: 'string' },
    title: { type: 'string', description: '설계안 이름 한 줄' },
    summary: { type: 'string', description: '설계 요지 3~5문장' },
    changes: {
      type: 'array',
      items: {
        type: 'object',
        required: ['file', 'what'],
        properties: { file: { type: 'string' }, what: { type: 'string' } },
      },
      description: '변경 대상 파일과 변경 내용',
    },
    pros: { type: 'array', items: { type: 'string' } },
    cons: { type: 'array', items: { type: 'string' } },
    evidenceFiles: { type: 'array', items: { type: 'string' }, description: '설계 근거로 확인한 파일:라인' },
    stepBreakdown: { type: 'array', items: { type: 'string' }, description: '채택 시 구현 step 분해 (WORKFLOW.md step 단위)' },
  },
}

const JUDGE_LENSES = [
  { key: 'effort', desc: '구현 비용·변경 범위·일정. 변경 파일 수, 난도, 검증 부담을 평가한다.' },
  { key: 'risk', desc: '크래시·사이드이펙트·회귀 리스크. 생명주기/직렬화/동시성/데이터 호환 관점에서 터질 곳을 평가한다.' },
  { key: 'compat', desc: '호환·마이그레이션 경로·롤백 가능성. 기존 네비게이션/상태복원/데이터와의 공존, 단계 출시와 되돌리기 용이성을 평가한다.' },
]

const JUDGE_SCHEMA = {
  type: 'object',
  required: ['lens', 'score', 'strengths', 'weaknesses', 'dealBreakers'],
  properties: {
    lens: { type: 'string' },
    score: { type: 'integer', minimum: 1, maximum: 5, description: '1=심각한 문제 ~ 5=우수' },
    strengths: { type: 'array', items: { type: 'string' } },
    weaknesses: { type: 'array', items: { type: 'string' } },
    dealBreakers: { type: 'array', items: { type: 'string' }, description: '치명 결격 사유 — 없으면 빈 배열' },
  },
}

// Phase 1: 렌즈별 설계자 독립 설계 (서로의 설계를 보지 못함)
const designs = (await parallel(DESIGNER_LENSES.map(lens => () =>
  agent(
    `문제면(JSON):\n${surfaceText}\n\n` +
    `당신은 "${lens.key}" 관점의 설계자다: ${lens.desc}\n` +
    `이 관점에서 위 문제의 해결 설계안 1개를 작성하라. 실제 코드를 확인(파일:라인 인용)하고 설계하라 — 추측 금지. ` +
    `변경 대상 파일/내용, 장단점, 채택 시 구현 step 분해까지 포함. read-only — 코드 수정 금지.`,
    { label: `design:${lens.key}`, phase: 'Design', agentType: 'code-explorer', schema: DESIGN_SCHEMA },
  )
))).filter(Boolean)

if (!designs.length) {
  return { report: '설계자 전원이 실패/스킵되어 비교할 설계가 없습니다.', designs: [], judgments: [] }
}
if (designs.length === 1) {
  log('설계안이 1개만 생성되어 비교가 불가합니다 — 단일 설계 기준으로 보고서를 생성합니다.')
}

// Phase 2: 설계마다 심사 3렌즈 교차 심사 (설계 간 배리어 없음)
const judged = await pipeline(
  designs,
  (design) => parallel(JUDGE_LENSES.map(jl => () =>
    agent(
      `문제면(JSON):\n${surfaceText}\n\n` +
      `심사 대상 설계안(JSON):\n${JSON.stringify(design, null, 2)}\n\n` +
      `당신은 "${jl.key}" 렌즈의 심사위원이다: ${jl.desc}\n` +
      `이 렌즈로만 설계안을 심사하라. 주장에 의심이 가면 실제 코드를 확인해 검증하라. ` +
      `점수(1~5), 강점, 약점, 치명 결격(dealBreakers — 없으면 빈 배열)을 반환하라.`,
      { label: `judge:${design.lens}/${jl.key}`, phase: 'Judge', agentType: 'code-reviewer', schema: JUDGE_SCHEMA },
    )
  )).then(verdicts => ({ design, verdicts: verdicts.filter(Boolean) })),
)

const judgments = judged.filter(Boolean)

// Phase 3: 합성 — 전체 설계·심사가 모여야 하므로 배리어 1회
phase('Synthesize')
const report = await agent(
  `문제면(JSON):\n${surfaceText}\n\n` +
  `아래는 독립 설계안들과 렌즈별 심사 결과(JSON)다. **설계 비교 보고서(markdown)** 를 작성하라. 구조:\n\n` +
  `1. **비교표** — 행=설계안, 열=심사 렌즈(effort/risk/compat) 점수 + 총점 + dealBreakers 유무\n` +
  `2. **설계안별 요약** — 각 설계의 요지·핵심 장단점·치명 결격(있다면)\n` +
  `3. **추천안** — 어느 설계를 왜 추천하는지. dealBreaker 가 있는 안은 추천 불가 사유 명시\n` +
  `4. **차점안에서 접목할 아이디어** — 추천안에 흡수하면 좋은 다른 설계의 요소\n` +
  `5. **채택 시 step 분해 초안** — 추천안(+접목 요소)의 구현 step 목록 (작업 spec 의 Step 목록 형식)\n\n` +
  `점수는 정성 판단의 보조다 — 총점만으로 기계적으로 고르지 말고 dealBreaker 와 문제의 제약(constraints)을 우선 고려하라. ` +
  `마지막에 "본 보고서는 결정 자료 초안이며 최종 결정은 사용자가 한다"를 명시하라.\n\n` +
  `데이터:\n\`\`\`json\n${JSON.stringify(judgments, null, 2)}\n\`\`\``,
  { label: 'synthesize:report', phase: 'Synthesize', agentType: 'code-explorer' },
)

return { report, designs, judgments }
```

---

## 주의
- **결정형·read-only**: 설계자=`code-explorer`, 심사=`code-reviewer` 모두 Edit/Write 권한이 없어 코드를 변경하지 않는다. 설계는 *제안 문서*다.
- **모델: 전 단계 세션 상속** (하네스 5종 중 유일) — 설계·심사·합성 모두 판단형이라 차등하지 않는다. 중요한 아키텍처 방향 결정은 상위 모델 세션(예: Fable 5)에서 호출해 품질을 올릴 수 있다.
- **결정은 사용자가**: 비교표·추천안은 결정 자료 초안. 점수의 기계적 합산이 아니라 dealBreaker 와 제약을 우선해 읽는다.
- **토큰 비용**: 설계 3 + 심사 9 + 합성 1 ≈ 13 에이전트. 자동 실행하지 말고 사용자가 `/design-options` 로 명시 호출했을 때만 돌린다. (WORKFLOW.md §1.5 "조건부 + 호출 전 고지" 정책과 동일)
- **하네스 체인**: 방향이 결정되면 그 변경면으로 `/impact-analyze` 를 돌려 spec 의 분석 섹션을 채우는 흐름이 자연스럽다.
- **빌드 영향 없음**: 앱 코드를 건드리지 않는다.
