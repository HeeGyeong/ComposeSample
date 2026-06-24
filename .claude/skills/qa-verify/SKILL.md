---
name: qa-verify
description: 릴리즈의 GitHub QA/bug 이슈 목록과 fix 커밋을 대조해 수정 누락·불완전·증상 불일치를 빌드 배포 전에 검출하는 감사형 멀티에이전트 하네스. 릴리즈/빌드 배포 전 "이번 버전 QA 수정 다 됐는지" 확인 시 사용.
---

# qa-verify — 릴리즈 QA 감사 하네스 (GitHub)

릴리즈의 **GitHub QA 이슈 목록 ↔ fix 커밋**을 대조해, 수정 누락(NO_FIX)·불완전(INCOMPLETE)·증상 불일치(MISMATCH)를 빌드 배포 전에 검출하는 read-only 감사 하네스입니다. 릴리즈/빌드를 올리기 직전에 돌리는 것이 자연스러운 사용 시점입니다.

**역할 경계**: 이 하네스는 "각 이슈의 수정이 **존재하고, 보고된 증상을 해결하는가**"(이슈-수정 대응 여부)만 본다.
- diff 안의 **새 버그/품질 문제 탐색은 하지 않는다** → `/code-review` 의 영역.
- **같은 패턴의 전파 탐색은 하지 않는다** → `/find-similar-bugs` 의 영역. INCOMPLETE 이슈가 나오면 보고서에서 `/find-similar-bugs #NNNN` 실행을 *권고만* 한다 (자동 연쇄 실행 금지).

호출: `/qa-verify [버전]`
- 예: `/qa-verify v1.2.0`
- 버전은 GitHub **milestone** 으로 매핑한다. 생략 시 현재 체크아웃된 git 태그/브랜치명에서 추출 → 그것도 모호하면 열린 milestone 목록을 보여주고 사용자에게 확인.

> **GitHub 매핑 규약** (ComposeSample 기준)
> - **버전 = milestone**. QA 이슈 = 해당 milestone + `qa`(없으면 `bug`) 라벨이 붙은 이슈.
> - **커밋 매핑 기준 ref = `main`**. ComposeSample 은 release 브랜치 없이 main 에 직접 커밋·푸시하므로 release 브랜치를 찾지 않는다.
> - 인증은 `gh` CLI 가 담당한다(토큰을 직접 다루지 않음). milestone/label 운영을 하지 않는 저장소라면 이 하네스는 감사할 이슈가 없어 빈 보고서를 낸다.

---

## 오케스트레이터 절차 (메인 루프가 따른다)

### 1. 버전 결정
`$ARGUMENTS` 의 버전(milestone 명) → 없으면 `git describe --tags --abbrev=0` 또는 `git branch --show-current` 에서 추출 → 그것도 모호하면 `gh api repos/{owner}/{repo}/milestones --jq '.[].title'` 로 열린 milestone 을 보여주고 사용자에게 확인.

### 2. QA 이슈 수집 (인라인)
- **인증**: `gh auth status` 로 로그인 상태를 확인한다. 인증돼 있지 않으면 **즉시 중단**하고 `gh auth login` 안내 (이슈 목록 없이는 진행 불가). `gh` CLI 가 없으면 설치 안내 후 중단.
- **조회** (읽기 전용 — 이슈 생성/수정/코멘트/라벨 변경 등 쓰기 호출 절대 금지):
  ```
  gh issue list --milestone "{version}" --label "qa" --state all \
    --json number,title,body,labels,state --limit 100
  ```
  `qa` 라벨 결과가 0건이면 라벨을 `bug` 로, 그래도 0건이면 라벨 없이 milestone 만으로 재시도하고 그 사실을 사용자에게 알린다. 100건 초과 시 `gh` 페이지네이션을 위해 `--limit` 을 올려 전부 수집한다.
- **보안**: 토큰 등 자격증명을 출력/로그/보고서에 노출하지 않는다 (gh 가 인증을 관리하므로 토큰을 직접 다루지 않지만, 혹시 노출되는 값이 있으면 마스킹).

### 3. 이슈↔커밋 매핑 (인라인)
이슈마다 `git log --grep "#NNNN" --pretty="%H %s" origin/main` (origin/main 이 없으면 현재 브랜치) 로 fix 커밋을 수집한다. (GitHub 의 squash-merge 메시지·일반 커밋 메시지의 `#NNNN` 참조를 잡는다.)
- **커밋 0건 이슈 → `NO_FIX` 로 즉시 분류** (에이전트 불필요 — 인라인 처리).
- 커밋이 있는 이슈만 Workflow 로 넘긴다: `items = [{ issue:{number,title,body,labels,state}, commits:[{hash,subject}] }]`

### 4. Workflow 실행
아래 §임베드 스크립트를 `args: { version, items, noFix: [NO_FIX 이슈 목록] }` 로 호출한다.

### 5. 결과 제시
릴리즈 QA 현황 보고서를 보여준다 — 이슈별 `OK / INCOMPLETE / MISMATCH / DISPUTED / INSUFFICIENT_DESC / NO_FIX`.
- 이 보고서는 **빌드 전 스크리닝**이지 QA 의 대체가 아님을 명시한다.
- INCOMPLETE 이슈에는 `/find-similar-bugs #NNNN` 권고를 덧붙인다.

---

## 임베드 Workflow 스크립트

> `Workflow` 도구의 `script` 인자로 전달. `args` = `{ version, items, noFix }` 를 실제 JSON 값으로 넘긴다.
> 안전 규칙: `Date.now()/Math.random()/new Date()` 미사용, schema 강제 출력, `.filter(Boolean)` 로 null 방어.

```javascript
export const meta = {
  name: 'qa-verify',
  description: 'QA 이슈 ↔ fix 커밋 대조 감사 → 문제 항목만 2차 재감사 → 릴리즈 QA 현황 보고서',
  phases: [
    { title: 'Audit', detail: '이슈별 증상↔수정 대조 감사' },
    { title: 'Recheck', detail: '문제 제기 항목만 독립 재감사' },
    { title: 'Report', detail: '릴리즈 QA 현황 보고서 합성' },
  ],
}

const version = (args && args.version) || ''
const noFix = (args && args.noFix) || []
let items = (args && args.items) || []

if (!items.length && !noFix.length) {
  log('감사할 이슈가 없습니다. 메인 루프에서 이슈 수집/매핑을 확인하세요. (milestone/label 운영을 하지 않는 저장소면 정상)')
  return { report: '', results: [] }
}

const MAX = 30
if (items.length > MAX) {
  log(`이슈 ${items.length}건 중 ${MAX}건만 감사합니다. 절단: ${items.slice(MAX).map(i => '#' + i.issue.number).join(', ')}`)
  items = items.slice(0, MAX)
}

const AUDIT_SCHEMA = {
  type: 'object',
  required: ['number', 'verdict', 'reasoning', 'evidence', 'uncoveredSymptoms'],
  properties: {
    number: { type: 'integer' },
    verdict: { type: 'string', enum: ['OK', 'INCOMPLETE', 'MISMATCH', 'INSUFFICIENT_DESC'] },
    reasoning: { type: 'string' },
    evidence: { type: 'array', items: { type: 'string' }, description: '확인한 파일:라인 / 커밋' },
    uncoveredSymptoms: { type: 'array', items: { type: 'string' }, description: '이슈에 보고됐으나 diff 가 해소하지 못한 증상' },
  },
}

const auditPrompt = (item, nth) =>
  `릴리즈 ${version} QA 이슈 감사${nth ? ` (독립 재감사 #${nth} — 이전 판정을 모른 채 처음부터 감사하라)` : ''}.\n\n` +
  `이슈 #${item.issue.number}: ${item.issue.title}\n` +
  `이슈 본문(보고된 증상/재현 경로):\n${item.issue.body || '(본문 없음)'}\n\n` +
  `fix 커밋: ${item.commits.map(c => `${c.hash.slice(0, 10)} ${c.subject}`).join(' / ')}\n` +
  `각 커밋의 diff 는 git show <hash> 로 직접 확인하라.\n\n` +
  `질문은 하나로 한정한다 — **보고된 증상 각각이 이 diff 들로 해소되는가?**\n` +
  `- 전부 해소 → OK\n` +
  `- 일부 증상/경로가 미해소 → INCOMPLETE (uncoveredSymptoms 에 명시)\n` +
  `- diff 가 보고된 증상과 다른 것을 고침 → MISMATCH\n` +
  `- 이슈 본문에 증상이 기술돼 있지 않아 판정 불가 → INSUFFICIENT_DESC (추측으로 판정하지 말 것)\n\n` +
  `금지: diff 안의 새 버그/품질 문제 리뷰(다른 도구의 역할), 코드 수정. 근거(파일:라인/커밋)를 반드시 남겨라.`

// Audit → (문제 항목만) Recheck — 이슈 간 배리어 없음
const results = await pipeline(
  items,
  (item) => agent(auditPrompt(item), {
    label: `audit:#${item.issue.number}`, phase: 'Audit', agentType: 'code-reviewer', schema: AUDIT_SCHEMA, model: 'sonnet',
  }),
  (audit, item) => {
    if (!audit) return null
    if (audit.verdict === 'OK') return { item, audit, final: 'OK' } // OK 는 재감사 생략 (비용 절약)
    // 2차 재감사는 의도적으로 1차(sonnet)와 다른 모델(opus) — 모델 다양성이 상호 검증 효과를 높이고,
    // 두 감사의 불일치(DISPUTED) 판정 신뢰도를 높인다.
    return agent(auditPrompt(item, 2), {
      label: `recheck:#${item.issue.number}`, phase: 'Recheck', agentType: 'code-reviewer', schema: AUDIT_SCHEMA, model: 'opus',
    }).then(re => ({
      item, audit, recheck: re,
      // 두 감사 일치 → 확정, 불일치 → DISPUTED (사람 판단 필요)
      final: (re && re.verdict === audit.verdict) ? audit.verdict : 'DISPUTED',
    }))
  },
)

const clean = results.filter(Boolean)

phase('Report')
const report = await agent(
  `릴리즈 ${version} QA 감사 결과(JSON)와 fix 커밋이 없는 이슈 목록으로 **릴리즈 QA 현황 보고서(markdown)** 를 작성하라:\n` +
  `1. 요약 — 전체 N건 중 OK/INCOMPLETE/MISMATCH/DISPUTED/INSUFFICIENT_DESC/NO_FIX 집계\n` +
  `2. 이슈별 상태 표 — #number / 제목 / 판정 / 한 줄 사유\n` +
  `3. 문제 항목 상세 — INCOMPLETE(미해소 증상+근거), MISMATCH, DISPUTED(두 감사의 판정 차이), NO_FIX\n` +
  `4. 권장 조치 — NO_FIX 는 수정 필요, INCOMPLETE 는 "/find-similar-bugs #number 실행 권고" 포함, INSUFFICIENT_DESC 는 이슈 본문 보강 요청\n` +
  `5. 말미에 명시: "본 보고서는 빌드 전 스크리닝이며 QA 를 대체하지 않는다"\n\n` +
  `감사 결과:\n\`\`\`json\n${JSON.stringify(clean.map(r => ({ number: r.item.issue.number, title: r.item.issue.title, final: r.final, audit: r.audit, recheck: r.recheck || null })), null, 2)}\n\`\`\`\n\n` +
  `NO_FIX 이슈:\n\`\`\`json\n${JSON.stringify(noFix, null, 2)}\n\`\`\``,
  { label: 'report:qa-status', phase: 'Report', agentType: 'code-explorer', model: 'opus' },
)

return { report, results: clean, noFix }
```

---

## 주의
- **감사형·read-only**: GitHub 는 **읽기 전용**(이슈 생성·수정·코멘트·라벨 변경 금지, `gh issue list`/`gh api` GET 만). 에이전트는 `code-reviewer`/`code-explorer` — Edit/Write 없음. git 조회도 읽기 전용.
- **단계별 모델 차등**: 1차 감사(Audit)는 `sonnet`, 재감사(Recheck)/보고서(Report)는 `opus` 고정 — 세션 모델과 무관하게 동작. 1차·2차가 다른 모델이라 상호 검증 효과가 있음.
- **인증 보안**: `gh` CLI 가 인증을 관리하므로 토큰을 직접 다루지 않는다. 혹시라도 자격증명 값이 노출되면 출력/로그/보고서에서 마스킹한다.
- **스크리닝이지 QA 대체가 아님**: verdict 는 빌드 전 구멍 탐지용 — 최종 판단은 QA/개발자.
- **체인은 권고만**: INCOMPLETE → `/find-similar-bugs` 자동 실행 금지, 보고서에 권고 문구만.
- **ComposeSample 한정 메모**: 이 저장소는 예제 쇼케이스라 milestone/label 기반 QA 운영을 하지 않을 수 있다. 그 경우 감사할 이슈가 없어 빈 보고서가 나오는 것이 정상이며, 버그 탐지는 `/code-review`, 패턴 전파는 `/find-similar-bugs` 를 쓴다.
- **토큰 비용**: 이슈 N건(상한 30) → 감사 N + 재감사(문제 항목만) + 합성 1. 사용자 질문/상황이 트리거에 해당하면("QA 수정 다 됐나/이번 버전 수정 현황/빌드 올려도 되나" 질문, 또는 릴리즈/빌드 배포 직전) 자동 실행 가능 — 단 **반드시 "qa-verify 하네스로 릴리즈 QA 감사를 수행합니다" 고지 후** 실행하고, 사용자가 거부하면 중단. `gh` 미인증이면 에이전트를 띄우기 전에 중단·안내한다. 트리거가 불명확하면 실행하지 않는다.
- **빌드 영향 없음**.
