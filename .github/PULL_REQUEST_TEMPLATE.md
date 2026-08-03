<!-- See CONTRIBUTING.md for conventions. Commit messages are written in Korean. -->

## Summary

<!-- What does this PR change, and why? -->

## Change Type

- [ ] `feat` — new example / feature
- [ ] `fix` — bug fix
- [ ] `refactor` — code change without behavior change
- [ ] `chore` — build/config/dependency
- [ ] `docs` — documentation only

## New Example Checklist

<!-- Fill in only if this PR adds a new example; otherwise skip. -->

- [ ] Step 1: constant added to `ConstValue.kt` (section comment / `UpdateDate` updated if needed)
- [ ] Step 2: `ExampleObject` added to `Examples20XX.kt` (`blogUrl = ""`, registered in `ExampleObjectList.kt` if a new year file)
- [ ] Step 2-1: if it is a sub-category group — children added to `subCategoryList()`, parent's `exampleType` uses the group constant
- [ ] Step 3: UI file created as `*ExampleUI.kt` with `fun *ExampleUI(onBackEvent: () -> Unit)`
- [ ] Step 4: routing entry added to the `exampleUiRegistry` map in `ExampleUiRegistry.kt` (Activity-type examples: `ExampleRouter.kt` `ACTIVITY` branch instead)
- [ ] Reference URLs placed only in `exampleGuide.kt`; `blogUrl` is `""` or the `blogUrl(postId)` helper — never a raw URL string

## Verification

- [ ] `./gradlew assembleDebug` succeeds
- [ ] Layer dependency rules respected (domain → none, data → domain, app → domain/data)
- [ ] Code comments in Korean; commit message follows the convention (`feat:` / `fix:` / `refactor:` / `chore:` / `docs:`)

## Notes

<!-- Anything reviewers should know: constraints, follow-ups, screenshots, etc. -->
