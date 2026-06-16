---
name: explore-codebase
description: 코드베이스를 여러 code-explorer 에이전트로 병렬 탐색해 아키텍처 맵·실행 경로로 합성하는 멀티에이전트 하네스. 사용자가 "코드 구조 파악", "이 기능 어떻게 동작하는지", "실행 경로 추적", "전체 아키텍처 이해" 등을 요청할 때 사용.
---

# explore-codebase — 코드베이스 탐색 멀티에이전트 하네스

코드베이스를 여러 `code-explorer` 에이전트로 **병렬 탐색**하고, 실행 경로를 추적해 하나의 **아키텍처 맵**으로 합성하는 read-only 하네스입니다. 단일 컨텍스트로 읽기엔 큰 서브시스템을 fan-out 으로 동시에 훑을 때 사용합니다.

호출: `/explore-codebase <대상/질문>`
- 예: `/explore-codebase 채팅방 진입 시 초기 스크롤이 결정되는 경로`
- 예: `/explore-codebase 전체 앱 아키텍처`
- 대상이 비어 있으면 "전체 앱 구조"로 간주합니다.

---

## 오케스트레이터 절차 (메인 루프가 따른다)

> 핵심: **정찰로 작업목록(서브시스템)을 먼저 확정한 뒤** Workflow 로 fan-out 한다 (하이브리드). 정찰 품질이 fan-out 품질을 좌우하므로 건너뛰지 않는다.

### 1. 대상 파악
`$ARGUMENTS` 에서 탐색 대상/질문을 추출한다. 비어 있으면 `question = "전체 앱 아키텍처"`.

### 2. 정찰 (인라인, read-only)
glob/grep 으로 패키지·진입점·모듈을 가볍게 훑어 **탐색 단위 목록**을 만든다.
- 질문이 **특정 기능/경로**면: 관련 키워드로 grep → 진입점(Activity/Fragment/ViewModel/UseCase/Repository)을 찾고, 그 경로가 지나는 레이어를 서브시스템 단위로 쪼갠다.
- 질문이 **전체 구조**면: top-level 패키지/모듈을 레이어별(presentation/domain/data 등)로 묶어 서브시스템화한다.
- 각 항목 = `{ name, hint }` — `hint` 는 "어디부터 볼지"(패키지 경로/키워드/대표 파일).
- 항목이 **12개를 넘으면** 질문과의 관련도로 상위 N개만 남기고, **잘라낸 항목은 워크플로우의 `log()` 로 사용자에게 명시한다** (silent truncation 금지).

### 3. Workflow 실행
아래 §임베드 Workflow 스크립트를 `args: { question, subsystems }` 로 호출한다.
- `subsystems` = 2단계에서 만든 `[{ name, hint }, ...]`.
- Workflow 는 백그라운드로 돌고, 완료되면 `{ map, raw }` 를 반환한다.

### 4. 결과 제시
반환된 `map`(아키텍처 맵 markdown)을 사용자에게 보여주고, 그 위에 **핵심 진입점 · 대표 실행 경로 · 열린 질문**을 3~5줄로 요약한다. 필요하면 사용자가 특정 서브시스템을 더 깊게 파달라고 후속 요청할 수 있음을 안내한다.

---

## 임베드 Workflow 스크립트

> 이 스크립트를 `Workflow` 도구의 `script` 인자로 전달한다. `args` 에는 `{ question, subsystems }` 를 **실제 JSON 값**으로 넘긴다(문자열로 인코딩하지 말 것).
> 안전 규칙: `Date.now()/Math.random()/new Date()` 미사용, schema 기반 강제 출력, `.filter(Boolean)` 로 null(스킵/실패) 방어.

```javascript
export const meta = {
  name: 'explore-codebase',
  description: '서브시스템별 code-explorer fan-out → 실행경로 추적 → 아키텍처 맵 합성',
  phases: [
    { title: 'Explore', detail: '서브시스템별 책임·핵심파일·진입점 매핑' },
    { title: 'Trace', detail: '실행 경로·크로스모듈 상호작용 추적' },
    { title: 'Synthesize', detail: '아키텍처 맵으로 통합' },
  ],
}

const question = (args && args.question) || '전체 앱 아키텍처'
const subsystems = (args && args.subsystems) || []

if (!subsystems.length) {
  log('정찰된 서브시스템이 없습니다. 메인 루프에서 subsystems 를 채워 다시 호출하세요.')
  return { map: '', raw: [] }
}

// 4096 항목 상한 방어 + 과도한 fan-out 경고
const MAX = 16
let targets = subsystems
if (targets.length > MAX) {
  log(`서브시스템 ${targets.length}개 중 상위 ${MAX}개만 탐색합니다. 절단: ${targets.slice(MAX).map(s => s.name).join(', ')}`)
  targets = targets.slice(0, MAX)
}

const EXPLORE_SCHEMA = {
  type: 'object',
  required: ['subsystem', 'responsibility', 'keyFiles', 'entryPoints', 'dependencies', 'patterns', 'openQuestions'],
  properties: {
    subsystem: { type: 'string' },
    responsibility: { type: 'string', description: '이 서브시스템의 책임 한두 문장' },
    keyFiles: {
      type: 'array',
      items: {
        type: 'object',
        required: ['path', 'role'],
        properties: { path: { type: 'string' }, role: { type: 'string' } },
      },
    },
    entryPoints: { type: 'array', items: { type: 'string' }, description: '진입점(Activity/Fragment/함수 등)' },
    dependencies: { type: 'array', items: { type: 'string' }, description: '의존하는 다른 모듈/서브시스템' },
    patterns: { type: 'array', items: { type: 'string' }, description: '관찰된 컨벤션/패턴' },
    openQuestions: { type: 'array', items: { type: 'string' } },
  },
}

const TRACE_SCHEMA = {
  type: 'object',
  required: ['subsystem', 'executionPaths', 'crossModuleInteractions', 'risks'],
  properties: {
    subsystem: { type: 'string' },
    executionPaths: {
      type: 'array',
      items: {
        type: 'object',
        required: ['trigger', 'steps', 'touchedFiles'],
        properties: {
          trigger: { type: 'string', description: '경로를 시작시키는 이벤트/호출' },
          steps: { type: 'array', items: { type: 'string' }, description: '순서대로의 호출/상태 변화' },
          touchedFiles: { type: 'array', items: { type: 'string' } },
        },
      },
    },
    crossModuleInteractions: { type: 'array', items: { type: 'string' } },
    risks: { type: 'array', items: { type: 'string' }, description: '생명주기/동시성/상태 관련 주의점' },
  },
}

// EXPLORE → TRACE 를 서브시스템별로 독립 파이프라인(서브시스템 간 배리어 없음)
const results = await pipeline(
  targets,
  (sub) => agent(
    `질문: "${question}"\n` +
    `서브시스템 "${sub.name}" 를 탐색하라. 시작 지점: ${sub.hint}\n` +
    `책임, 핵심 파일(경로+역할), 진입점, 의존 모듈, 관찰된 패턴/컨벤션, 열린 질문을 구조화해 반환하라. ` +
    `read-only — 코드를 수정하지 말 것.`,
    { label: `explore:${sub.name}`, phase: 'Explore', agentType: 'code-explorer', schema: EXPLORE_SCHEMA, model: 'sonnet' },
  ),
  (explore, sub) => {
    if (!explore) return null
    const seeds = [
      ...(explore.entryPoints || []),
      ...((explore.keyFiles || []).map(f => f.path)),
    ].slice(0, 12).join(', ')
    return agent(
      `질문: "${question}"\n` +
      `서브시스템 "${sub.name}" 의 실행 경로를 추적하라.\n` +
      `진입점/핵심 파일 시드: ${seeds}\n` +
      `대표 실행 경로(트리거→단계별 호출→touched files), 크로스모듈 상호작용, 생명주기/동시성/상태 리스크를 반환하라. ` +
      `read-only.`,
      { label: `trace:${sub.name}`, phase: 'Trace', agentType: 'code-explorer', schema: TRACE_SCHEMA, model: 'sonnet' },
    ).then(trace => ({ subsystem: sub.name, hint: sub.hint, explore, trace }))
  },
)

const clean = results.filter(Boolean)

if (!clean.length) {
  return { map: '탐색 결과가 비어 있습니다(모든 에이전트 실패/스킵).', raw: [] }
}

// 합성 — 모든 결과가 모여야 하므로 배리어 1회, 합성 에이전트 1개
phase('Synthesize')
const map = await agent(
  `질문: "${question}"\n\n` +
  `아래는 서브시스템별 탐색(explore)·실행경로 추적(trace) 결과(JSON)다. ` +
  `이를 하나의 **아키텍처 맵(markdown)** 으로 합성하라. 포함할 것:\n` +
  `1. 한 줄 요약(이 코드베이스/대상이 무엇을 어떻게 하는가)\n` +
  `2. 레이어/모듈 다이어그램(텍스트) 과 모듈 간 의존 관계\n` +
  `3. 대표 실행 경로 1~3개(트리거→단계→파일)\n` +
  `4. 핵심 파일 인덱스(경로 — 역할), 질문과의 관련도 순\n` +
  `5. 관찰된 컨벤션/패턴\n` +
  `6. 열린 질문 / 더 파볼 지점\n\n` +
  `중복은 합치고, 서브시스템 간 모순은 명시하라. 추측은 "추정"으로 표시.\n\n` +
  `데이터:\n\`\`\`json\n${JSON.stringify(clean, null, 2)}\n\`\`\``,
  { label: 'synthesize:map', phase: 'Synthesize', agentType: 'code-explorer', model: 'opus' },
)

return { map, raw: clean }
```

---

## 옵션 — completeness critic 라운드 (필요 시)
합성 후 "빠진 서브시스템/관점이 있는가?"를 점검하려면, 합성 직전에 critic 에이전트를 한 번 더 돌려 누락 서브시스템을 받아 §정찰 목록에 추가하고 pipeline 을 한 라운드 더 돌린다. 기본은 생략(토큰 절약). 사용자가 "철저하게/빠짐없이"를 요청하면 활성화한다.

## 주의
- **read-only 하네스**: `code-explorer` 는 Edit/Write 권한이 없어 코드를 변경하지 않는다.
- **단계별 모델 차등**: 탐색(Explore/Trace)은 `sonnet`, 합성(Synthesize)은 `opus` 고정 — 세션 모델과 무관하게 동작(비용 상한·품질 하한).
- **토큰 비용**: 멀티 에이전트 fan-out 은 토큰 소모가 크다. 자동 실행하지 말고 사용자가 `/explore-codebase` 로 명시 호출했을 때만 Workflow 를 돌린다.
- **빌드 영향 없음**: 앱 코드를 건드리지 않는다.
