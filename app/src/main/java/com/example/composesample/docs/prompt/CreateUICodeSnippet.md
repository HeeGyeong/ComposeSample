# UI Code Generation Snippet
When using generative AI, write the prompt following the guideline below.

## Basic Info
- **Task type**: UI creation/implementation/generation

## Technical Details
- **Language**: Kotlin
- **Framework**: Jetpack Compose
- **Architecture**: MVVM + Clean Architecture
- **When writing comments**: write in Korean
- **All parts of the file**: write without omitting anything
- **If there is a part that should be removed**: do not remove it; write an explanation instead
- **Color and Style usage example**:
```kotlin
Text(
    modifier = Modifier,
    text = title,
    color = Color.LightGray,
    style = getTextStyle(12)
)
```
- **When using a ViewModel**: see `app/src/main/java/com/example/composesample/docs/di/DIRules.md`
- **When using a ViewModel**: use Koin for DI

## UI Analysis
1. [UI analysis 1]
2. [UI analysis 2]
3. [UI analysis 3]

## Requirements
1. [requirement 1]
2. [requirement 2]
3. [requirement 3]

## Expected Behavior
- [description of expected behavior]
- [description of key features]

## References
- **Project rules**: [path to the relevant rule file]
- **Related files**: [path to related files]

## Desired Result
- A UI implementation that matches the image
- Keep the build working using dummy data
- Express the full context of the question
- Express what you read as a complete result based on the question's context
- If it is too long to express, continue in the next question
