---
name: impact-analyze
description: 변경의 영향(크래시 가능성·사이드 이펙트)을 차원별 finder 로 병렬 탐지하고 adversarial 검증해 WORKFLOW.md 분석 섹션 초안을 생성하는 멀티에이전트 하네스. fix/improve 의 영향 분석, "이 변경 안전한지", "사이드 이펙트 뭐 있는지" 요청 시 사용.
---

# impact-analyze — 영향 분석 검증형 하네스

변경의 **크래시 가능성·사이드 이펙트**를 WORKFLOW.md 템플릿의 차원별로 병렬 탐지하고, 각 리스크 후보를 **적대적으로 반증(adversarial verify)** 해 살아남은 것만 분석 섹션으로 합성하는 read-only 하네스입니다. `explore-codebase`(탐색형)와 달리 **검증형** — 리스크 후보를 그대로 믿지 않고 회의론자가 반증을 시도합니다.

호출: `/impact-analyze [변경 대상/설명]`
- 인자가 비면 **현재 작업트리 `git diff`** 를 변경면으로 사용.
- 예: `/impact-analyze` (현재 diff)
- 예: `/impact-analyze ExampleRouter 라우팅 분기 변경의 영향`

---

## 오케스트레이터 절차 (메인 루프가 따른다)

### 1. 입력 파싱
`$ARGUMENTS` 에서 변경 대상/설명을 추출한다. 비어 있으면 `git diff`(+ 필요시 `git diff --staged`)를 읽어 변경면으로 삼는다.

### 2. 변경면 정찰 (인라인, read-only)
바뀌는 **파일 · 함수/심볼 · 리소스** 목록을 만들고, 핵심 심볼의 1차 호출부를 grep 해둔다.
- `changeSurface = { summary, files:[...], symbols:[...] }`
- `summary` = 변경이 무엇을 바꾸는지 1~2문장. finder 들이 공유할 컨텍스트.

### 3. Workflow 실행
아래 §임베드 Workflow 스크립트를 `args: { change: changeSurface }` 로 호출한다(실제 JSON 값으로 전달).

### 4. 결과 제시
반환된 `section`(분석 섹션 markdown)을 보여준다. 진행 중인 spec 의 step 이 있으면, 그 step 의 **"크래시 가능성 / 사이드 이펙트 분석"** 에 반영할지 사용자에게 묻는다.
- 합성 섹션은 **초안**임을 명시하고, 폐기된 후보(`dropped`)도 간단히 알려 사용자가 재검토할 수 있게 한다.

---

## 임베드 Workflow 스크립트

> `Workflow` 도구의 `script` 인자로 전달. `args` = `{ change }` 를 실제 JSON 값으로 넘긴다.
> 안전 규칙: `Date.now()/Math.random()/new Date()` 미사용, schema 강제 출력, `.filter(Boolean)` 로 null 방어.

```javascript
export const meta = {
  name: 'impact-analyze',
  description: '차원별 리스크 finder fan-out → adversarial verify → WORKFLOW.md 분석 섹션 합성',
  phases: [
    { title: 'Find', detail: '영향 차원별 리스크 후보 탐지' },
    { title: 'Verify', detail: '리스크마다 회의론자 3명이 반증 시도' },
    { title: 'Synthesize', detail: 'WORKFLOW.md 형식 분석 섹션 합성' },
  ],
}

const change = (args && args.change) || {}
const changeText = JSON.stringify(change, null, 2)

if (!change.summary && !(change.files && change.files.length)) {
  log('변경면(change.summary/files)이 비어 있습니다. 메인 루프에서 changeSurface 를 채워 다시 호출하세요.')
  return { section: '', survived: [], dropped: [] }
}

// 영향 차원 (Compose + Koin + Clean Architecture 기준)
const DIMENSIONS = [
  { key: 'call-sites', desc: '변경한 함수·프로퍼티·리소스의 호출부/참조부 — 깨지거나 시그니처 불일치가 생기는 곳 (ExampleRouter 분기, 다른 ExampleUI 등)' },
  { key: 'lifecycle', desc: 'Composable 생명주기·재구성과 사이드 이펙트 시점 불일치 — remember/rememberSaveable 키 누락, LaunchedEffect/DisposableEffect 의존성·정리, ViewModel 스코프, 콜백 시점' },
  { key: 'state-restore', desc: '상태 보존/복원 — rememberSaveable/Saver, process death 후 복원, Navigation 인자(Coordinator/Core) 직렬화 호환' },
  { key: 'concurrency', desc: 'Flow/coroutine 취소·재구독, 메인 스레드 밖 UI 접근, race, StateFlow 수집 시점' },
  { key: 'data-compat', desc: '서버 응답(Retrofit/Ktor)/로컬 캐시(Room)의 기존 데이터가 변경된 파싱·분기 로직을 통과하는지' },
  { key: 'shared-component', desc: '공통 Composable/extension/테마/공유 ViewModel 변경 시 이를 쓰는 다른 화면 영향' },
  { key: 'linked-flow', desc: '네비게이션 콜백(onBackEvent), ExampleRouter 라우팅, 공유 상태 등 간접 경로로 영향받는 플로우' },
]

const RISK_SCHEMA = {
  type: 'object',
  required: ['dimension', 'risks'],
  properties: {
    dimension: { type: 'string' },
    risks: {
      type: 'array',
      items: {
        type: 'object',
        required: ['scenario', 'cause', 'evidenceFiles', 'likelihood', 'defense'],
        properties: {
          scenario: { type: 'string', description: '문제가 터지는 상황' },
          cause: { type: 'string', description: '예상 예외/원인' },
          evidenceFiles: { type: 'array', items: { type: 'string' }, description: '근거 파일:라인' },
          likelihood: { type: 'string', enum: ['상', '중', '하'] },
          defense: { type: 'string', description: '대응/방어책' },
        },
      },
    },
  },
}

const VERDICT_SCHEMA = {
  type: 'object',
  required: ['refuted', 'reasoning', 'adjustedLikelihood'],
  properties: {
    refuted: { type: 'boolean', description: '이 리스크가 실제로는 성립하지 않으면 true' },
    reasoning: { type: 'string' },
    adjustedLikelihood: { type: 'string', enum: ['상', '중', '하', '해당없음'] },
  },
}

// Find → Verify 를 차원별 독립 파이프라인(차원 간 배리어 없음)
const perDim = await pipeline(
  DIMENSIONS,
  (dim) => agent(
    `변경면(JSON):\n${changeText}\n\n` +
    `위 변경에 대해 **"${dim.key}"** 차원의 영향만 분석하라: ${dim.desc}\n` +
    `해당 차원에서 발생 가능한 리스크 후보를 코드 근거(파일:라인)와 함께 반환하라. ` +
    `근거 없이 추측만으로 올리지 말 것. 없으면 빈 배열. read-only — 코드 수정 금지.`,
    { label: `find:${dim.key}`, phase: 'Find', agentType: 'code-explorer', schema: RISK_SCHEMA, model: 'sonnet' },
  ),
  (found, dim) => {
    if (!found || !found.risks || !found.risks.length) return []
    // 각 리스크를 회의론자 3명이 독립 반증 → 다수(≥2) 반증이면 폐기
    return parallel(found.risks.map(risk => () =>
      parallel([1, 2, 3].map(n => () =>
        agent(
          `변경면(JSON):\n${changeText}\n\n` +
          `다음 리스크 주장을 **반증**하라(회의론자 #${n}). 코드를 직접 확인해 실제로 성립하지 않으면 refuted=true. ` +
          `불확실하면 refuted=true 를 기본으로 한다.\n\n` +
          `- 차원: ${dim.key}\n- 시나리오: ${risk.scenario}\n- 원인: ${risk.cause}\n- 근거: ${(risk.evidenceFiles || []).join(', ')}`,
          { label: `verify:${dim.key}#${n}`, phase: 'Verify', agentType: 'code-reviewer', schema: VERDICT_SCHEMA, model: 'sonnet' },
        )
      )).then(votes => {
        const valid = votes.filter(Boolean)
        const refutes = valid.filter(v => v.refuted).length
        const survived = refutes < 2
        // 검증 통과 시 가장 보수적(높은) 가능성으로 보정
        const order = { '상': 3, '중': 2, '하': 1, '해당없음': 0 }
        const adj = valid.filter(v => !v.refuted).map(v => v.adjustedLikelihood)
        const adjusted = adj.length ? adj.reduce((a, b) => (order[b] > order[a] ? b : a)) : risk.likelihood
        return { ...risk, dimension: dim.key, survived, adjustedLikelihood: adjusted, refutes, reasoning: valid.map(v => v.reasoning) }
      })
    ))
  },
)

const all = perDim.flat().filter(Boolean)
const survived = all.filter(r => r.survived)
const dropped = all.filter(r => !r.survived)

if (!survived.length) {
  log(`탐지된 리스크 후보 ${all.length}개가 모두 검증에서 반증되었습니다.`)
  return {
    section: '## 크래시 가능성 / 사이드 이펙트 분석\n\n검증 결과 성립하는 리스크가 발견되지 않았습니다. (탐지 후보 전부 반증됨 — 폐기 목록 참고)',
    survived: [],
    dropped,
  }
}

// 합성 — 생존 리스크 전체가 모여야 하므로 배리어 1회
phase('Synthesize')
const section = await agent(
  `변경면(JSON):\n${changeText}\n\n` +
  `아래는 adversarial 검증을 통과한 영향 리스크(JSON)다. 이를 **WORKFLOW.md 형식의 분석 섹션(markdown)** 으로 합성하라. 정확히 이 구조로:\n\n` +
  `### 크래시 가능성\n` +
  `| 시나리오 | 원인 (예상 예외) | 가능성 | 대응/방어 |\n|---|---|---|---|\n` +
  `(생명주기/재구성/상태복원/동시성/데이터호환 등 크래시성 리스크를 adjustedLikelihood 기준으로)\n\n` +
  `### 사이드 이펙트\n` +
  `- **호출부/참조부 영향**: (call-sites 차원 요약 — "다른 곳 없음"도 확인 결과로 명시)\n` +
  `- **공유 컴포넌트 영향**: (shared-component 차원 — 공통 Composable/extension/테마/공유 ViewModel)\n` +
  `- **기존 동작 변화**: (의도 외 달라지는 동작)\n` +
  `- **연계 플로우**: (linked-flow 차원 — 네비게이션 콜백/ExampleRouter/공유 상태)\n\n` +
  `### 분석 근거\n` +
  `- (검증 통과 근거 + 확인한 파일:라인 — 분석이 코드 확인에 기반했음을 남김)\n\n` +
  `중복 리스크는 합치고, 근거 파일을 반드시 인용하라. 추측은 "추정"으로 표시.\n\n` +
  `리스크 데이터:\n\`\`\`json\n${JSON.stringify(survived, null, 2)}\n\`\`\``,
  { label: 'synthesize:section', phase: 'Synthesize', agentType: 'code-explorer', model: 'opus' },
)

return { section, survived, dropped }
```

---

## 주의
- **검증형·read-only**: finder=`code-explorer`, verify=`code-reviewer` 모두 Edit/Write 권한이 없어 코드를 변경하지 않는다.
- **단계별 모델 차등**: 탐지(Find)/검증(Verify)은 `sonnet`(다수결이 개별 오차 흡수), 합성(Synthesize)은 `opus` 고정 — 세션 모델과 무관하게 동작(비용 상한·품질 하한).
- **초안으로 제시**: 검증을 거쳐도 정성적 판단 — 합성 섹션은 초안이며 사람이 최종 확인한다. 반증으로 **폐기된 후보(`dropped`)** 도 함께 알려 false negative(실제 리스크 누락)를 사용자가 재검토할 수 있게 한다.
- **토큰 비용**: 차원 7 × 검증 3 으로 explore-codebase 보다 fan-out 이 크다. 자동 실행하지 말고 사용자가 `/impact-analyze` 로 명시 호출했을 때만 돌린다. (WORKFLOW.md §1.5 "조건부 + 호출 전 고지" 정책과 동일)
- **빌드 영향 없음**: 앱 코드를 건드리지 않는다.
