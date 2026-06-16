---
name: find-similar-bugs
description: 수정된 버그 1건에서 패턴을 추상화해 코드베이스 다른 곳에 숨은 같은 버그(형제 버그)를 multi-modal 탐색 + adversarial 검증으로 찾는 멀티에이전트 하네스. "이 버그 다른 데도 있는지", QA 수정 후 전파 확인 시 사용.
---

# find-similar-bugs — 유사 버그 전파 탐지 하네스

수정된 버그 1건에서 *버그 패턴*을 추상화하고, 코드베이스의 **다른 곳에 숨은 같은 패턴(형제 버그)** 을 서로 다른 4개 각도의 finder 로 병렬 탐색한 뒤 adversarial 검증을 통과한 후보만 보고하는 read-only 하네스입니다.

**역할 경계**: 이 하네스는 "이 버그의 **사본**이 다른 곳에 있는가"만 묻는다. 수정 자체의 옳고 그름은 `/qa-verify`·`/code-review`, 변경의 영향 분석은 `/impact-analyze` 의 영역 — 여기서 하지 않는다. **탐지까지만** 수행하며, 발견된 형제 버그의 수정은 별도 작업 spec(fix-*)으로 진행한다.

호출: `/find-similar-bugs <이슈번호|커밋해시|버그설명>`
- 예: `/find-similar-bugs <커밋해시>`
- 예: `/find-similar-bugs rememberSaveable 키 누락으로 화면 회전 시 상태 유실되는 버그 — 예제 UI 렌더링`

---

## 오케스트레이터 절차 (메인 루프가 따른다)

### 1. 입력 파싱
- **이슈번호(`#NNNN`)**: `git log --grep "#NNNN" --pretty="%H %s"` 로 fix 커밋들을 찾고, 각 커밋의 `git diff-tree -p <hash>` 로 diff 를 수집한다. (이슈별 변경 파일 조회는 항상 이 방식 — 메모리 추측 금지)
- **커밋 해시**: 해당 커밋의 diff 를 직접 수집.
- **버그 설명만**: 관련 파일/증상이 특정되지 않으면 사용자에게 1~2개만 확인하고 시작한다.

### 2. 패턴 추상화 (인라인)
fix diff 와 커밋 메시지를 읽고 버그를 일반화한다:
```
bugPattern = {
  symptom: 사용자가 겪는 증상,
  rootCause: 원인 메커니즘 (왜 생기는가),
  signature: 코드 시그니처 — 잘못된 API/관용구/구조 (grep 가능한 형태로),
  fixedSites: [이미 고친 파일:라인],   // 탐색에서 제외할 곳
  category: 디폴트값|상태동기화|타이밍|생명주기|재구성/상태복원|기타,
}
```

### 3. Workflow 실행
아래 §임베드 Workflow 스크립트를 `args: { pattern: bugPattern }` 로 호출한다(실제 JSON 값으로).

### 4. 결과 제시
검증 통과 후보 목록(파일:라인 / 같은 패턴인 근거 / 예상 증상 / 신뢰도)을 보여준다.
- 후보 0건이어도 의미 있는 결과 — "4개 각도로 훑었고 전파 없음"을 어떤 각도로 봤는지와 함께 보고한다.
- 수정을 원하면 별도 fix-* 작업 spec 으로 진행한다(이 하네스에서 코드 수정 금지).

---

## 임베드 Workflow 스크립트

> `Workflow` 도구의 `script` 인자로 전달. `args` = `{ pattern }` 을 실제 JSON 값으로 넘긴다.
> 안전 규칙: `Date.now()/Math.random()/new Date()` 미사용, schema 강제 출력, `.filter(Boolean)` 로 null 방어.

```javascript
export const meta = {
  name: 'find-similar-bugs',
  description: '버그 패턴 multi-modal 탐색 → adversarial 검증 → 형제 버그 목록',
  phases: [
    { title: 'Hunt', detail: '4개 각도(시그니처/구조/카테고리/데이터흐름)로 병렬 탐색' },
    { title: 'Verify', detail: '후보마다 회의론자 2명이 재현 가능성 반증 시도' },
    { title: 'Report', detail: '신뢰도순 형제 버그 목록 합성' },
  ],
}

const pattern = (args && args.pattern) || {}
const patternText = JSON.stringify(pattern, null, 2)

if (!pattern.symptom || !pattern.rootCause) {
  log('bugPattern(symptom/rootCause)이 비어 있습니다. 메인 루프에서 패턴 추상화를 채워 다시 호출하세요.')
  return { report: '', survived: [], dropped: [] }
}

const FINDERS = [
  { key: 'by-signature', desc: '같은 코드 시그니처를 쓰는 곳 — pattern.signature 의 API/관용구/구조를 grep 으로 추적해 같은 실수가 가능한 사용처를 찾는다.' },
  { key: 'by-structure', desc: '구조적 유사 지점 — fixedSites 와 같은 역할의 클래스/레이어(같은 Base 상속자, 같은 패턴의 UseCase/Repository/Fragment 형제들)에서 같은 결함을 찾는다.' },
  { key: 'by-category', desc: '같은 버그 카테고리가 잘 생기는 화면/플로우 — category(예: 상태동기화)가 동일하게 문제될 수 있는 다른 화면·갱신 경로를 찾는다.' },
  { key: 'by-data-flow', desc: '같은 데이터를 다루는 다른 경로 — 버그에 관련된 필드/키/리소스를 읽고 쓰는 다른 코드 경로에서 같은 누락/오류를 찾는다.' },
]

const CANDIDATE_SCHEMA = {
  type: 'object',
  required: ['finder', 'candidates'],
  properties: {
    finder: { type: 'string' },
    candidates: {
      type: 'array',
      items: {
        type: 'object',
        required: ['file', 'line', 'why', 'howItManifests'],
        properties: {
          file: { type: 'string' },
          line: { type: 'integer' },
          why: { type: 'string', description: '왜 같은 패턴인지 (코드 근거)' },
          howItManifests: { type: 'string', description: '여기서 증상이 어떻게 나타날지' },
        },
      },
    },
  },
}

const VERDICT_SCHEMA = {
  type: 'object',
  required: ['refuted', 'reasoning'],
  properties: {
    refuted: { type: 'boolean', description: '이 위치에서 같은 증상이 성립하지 않으면 true' },
    reasoning: { type: 'string' },
  },
}

// Phase 1: 4개 각도 병렬 탐색 — dedup 은 전체 후보가 필요하므로 여기만 배리어 정당
const found = (await parallel(FINDERS.map(f => () =>
  agent(
    `버그 패턴(JSON):\n${patternText}\n\n` +
    `당신은 "${f.key}" finder 다: ${f.desc}\n` +
    `이 각도로만 코드베이스를 탐색해, 같은 버그가 숨어 있을 후보를 코드 근거와 함께 반환하라. ` +
    `fixedSites(이미 수정된 곳)는 제외한다. 근거 없는 추측 후보는 올리지 말 것. 없으면 빈 배열. read-only — 코드 수정 금지.`,
    { label: `hunt:${f.key}`, phase: 'Hunt', agentType: 'code-explorer', schema: CANDIDATE_SCHEMA, model: 'sonnet' },
  )
))).filter(Boolean)

// 인라인 dedup (파일:라인 기준) + 상한 20
const seen = new Set()
let candidates = []
for (const r of found) {
  for (const c of (r.candidates || [])) {
    const key = `${c.file}:${c.line}`
    if (!seen.has(key)) { seen.add(key); candidates.push({ ...c, finder: r.finder }) }
  }
}
const MAX = 20
if (candidates.length > MAX) {
  log(`후보 ${candidates.length}개 중 상위 ${MAX}개만 검증합니다. 절단: ${candidates.slice(MAX).map(c => c.file + ':' + c.line).join(', ')}`)
  candidates = candidates.slice(0, MAX)
}
if (!candidates.length) {
  return {
    report: '4개 각도(시그니처/구조/카테고리/데이터흐름) 탐색 결과 형제 버그 후보가 발견되지 않았습니다.',
    survived: [], dropped: [],
  }
}
log(`형제 버그 후보 ${candidates.length}건 — 검증 시작`)

// Phase 2: 후보마다 회의론자 2명 반증 (후보 간 배리어 없음)
const verified = await pipeline(
  candidates,
  (c) => parallel([1, 2].map(n => () =>
    agent(
      `버그 패턴(JSON):\n${patternText}\n\n` +
      `다음 형제 버그 후보를 **반증**하라(회의론자 #${n}). 해당 코드를 직접 읽고, 이 위치에서 같은 증상이 실제로 성립하지 않으면 refuted=true.\n\n` +
      `- 위치: ${c.file}:${c.line}\n- 같은 패턴 근거: ${c.why}\n- 예상 증상: ${c.howItManifests}`,
      { label: `verify:${c.file.split('/').pop()}#${n}`, phase: 'Verify', agentType: 'code-reviewer', schema: VERDICT_SCHEMA, model: 'sonnet' },
    )
  )).then(votes => {
    const valid = votes.filter(Boolean)
    const refutes = valid.filter(v => v.refuted).length
    // 2명 반증=폐기, 1명=신뢰도 중, 0명=상
    return { ...c, survived: refutes < 2, confidence: refutes === 0 ? '상' : '중', refuteReasons: valid.map(v => v.reasoning) }
  }),
)

const all = verified.filter(Boolean)
const survived = all.filter(c => c.survived)
const dropped = all.filter(c => !c.survived)

// Phase 3: 합성
phase('Report')
const report = await agent(
  `버그 패턴(JSON):\n${patternText}\n\n` +
  `아래는 검증을 통과한 형제 버그 후보(JSON)다. **형제 버그 보고서(markdown)** 를 작성하라:\n` +
  `1. 한 줄 요약 (원 버그 + 전파 현황)\n` +
  `2. 후보 목록 — 신뢰도(상 우선)순으로: 파일:라인 / 같은 패턴인 근거 / 예상 증상 / 권장 조치\n` +
  `3. 탐색 커버리지 — 4개 각도로 무엇을 훑었는지\n` +
  `4. 폐기된 후보 요약 (재검토용, 폐기 사유 한 줄씩)\n\n` +
  `수정 방법 제안은 간단히만 — 실제 수정은 별도 작업 spec 으로 진행됨을 명시하라.\n\n` +
  `생존 후보:\n\`\`\`json\n${JSON.stringify(survived, null, 2)}\n\`\`\`\n\n` +
  `폐기 후보:\n\`\`\`json\n${JSON.stringify(dropped.map(d => ({ file: d.file, line: d.line, reasons: d.refuteReasons })), null, 2)}\n\`\`\``,
  { label: 'report:siblings', phase: 'Report', agentType: 'code-explorer', model: 'opus' },
)

return { report, survived, dropped }
```

---

## 주의
- **전파탐지형·read-only**: finder=`code-explorer`, verify=`code-reviewer` — Edit/Write 권한 없음. git 조회(`log --grep`/`diff-tree`)도 읽기 전용.
- **단계별 모델 차등**: 수색(Hunt)/검증(Verify)은 `sonnet`, 보고서(Report)는 `opus` 고정 — 세션 모델과 무관하게 동작(비용 상한·품질 하한).
- **탐지까지만**: 발견된 형제 버그의 수정은 WORKFLOW.md 절차대로 별도 fix-* spec 작성 → 승인 → 구현.
- **false negative 한계**: 탐지 범위는 4개 각도에 한정 — "후보 없음"이 "버그 없음"의 증명은 아니다(보고서에 명시).
- **토큰 비용**: finder 4 + 후보×2 검증(상한 20) + 합성 1. 사용자 질문/상황이 트리거에 해당하면("이 버그 다른 데도 있나/비슷한 버그/같은 패턴" 질문, 또는 QA/fix 완료 직후 전파 가능성이 높은 수정 — 공통 컴포넌트·디폴트값·다중 사용처) 자동 실행 가능 — 단 **반드시 "find-similar-bugs 하네스로 유사 버그 탐지를 수행합니다" 고지 후** 실행하고, 사용자가 거부하면 중단. 트리거가 불명확하거나 소규모 국소 수정이면 실행하지 않는다.
- **빌드 영향 없음**.
