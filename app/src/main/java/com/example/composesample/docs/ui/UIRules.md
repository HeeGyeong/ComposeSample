# UI System Guide

## 📋 Basic Rules
- All code must follow the coding conventions defined in the `.cursorrules.md` file at the project root.
- Separate UI code and business logic according to the rules below.

### Incorrect Example
```kotlin
@Composable
fun PlayerScreen() {
    Button(onClick = {
        // Business logic
    }) {
        Text("Button")
    }
}
```

### Correct Example
Create a ViewModel class file related to the functionality within the same folder.<br>
Once the topic of the higher-level functionality is determined, use the higher-level ViewModel class without further subdividing it.<br>
Name the ViewModel class file as FunctionName + `ViewModel`.<br>
Key Concept: Higher-Level Feature Organization.<br>
Let's use the "Live" feature as an example:

Live Feature<br>
├── Streaming<br>
├── Settings<br>
├── Chat<br>
└── Other Live-related features<br>

Rule Explanation:
DO THIS ✅

```kotlin
class LiveViewModel {
    // One ViewModel handles all Live-related features
    fun handleStreaming() { ... }
    fun handleSettings() { ... }
    fun handleChat() { ... }
}
```

DON'T DO THIS ❌

```kotlin
// Don't create separate ViewModels for sub-features
class LiveStreamViewModel { ... }
class LiveSettingViewModel { ... }
class LiveChatViewModel { ... }
```

Why This Rule Exists:
Better code organization
Prevents ViewModel fragmentation
Easier state management
Clearer dependency injection
Following single responsibility principle at the feature level
Simple Rule of Thumb:
Ask yourself: "Is this part of a larger feature?"
If yes → Use the parent feature's ViewModel
If no → Create a new ViewModel for the new feature


Use the ViewModel class with Dependency Injection and register it in the `../di/ViewModelModule.kt` file.<br>
Use the Koin Framework for Dependency Injection.<br>

```kotlin
val viewModelModule: Module = module {
    ...
    viewModel {
        PlayerViewModel()
    }
}

class PlayerViewModel() : ViewModel() {
    ...
    fun fetchPlayList() {
        // Business logic
    }
}

@Composable
fun PlayerScreen(
    playerViewModel: PlayerViewModel
) {
    Button(onClick = {
        playerViewModel.fetchPlayList()
    }) {
        Text("Button")
    }
}
```

## Separation of Business Logic and UI Code

To maintain clean architecture and improve maintainability, it's crucial to separate business logic from UI code. Here are some examples and guidelines:

### Example of Separation

**Incorrect Implementation**
```kotlin
@Composable
fun UserProfileScreen() {
    Button(onClick = {
        // Business logic directly in UI
        fetchUserProfile()
    }) {
        Text("Load Profile")
    }
}
```

**Correct Implementation**
```kotlin
class UserProfileViewModel : ViewModel() {
    fun fetchUserProfile() {
        // Business logic
    }
}

@Composable
fun UserProfileScreen(viewModel: UserProfileViewModel = viewModel()) {
    Button(onClick = {
        viewModel.fetchUserProfile()
    }) {
        Text("Load Profile")
    }
}
```

### Guidelines
- **ViewModel Usage**: Always use a ViewModel to handle business logic and state management.
- **UI Components**: Keep UI components focused on rendering and user interaction.
- **Dependency Injection**: Use Koin or another DI framework to inject ViewModels into composables.
- **State Management**: Use state hoisting to manage UI state outside of composables when necessary.

By following these guidelines, you ensure that your code is modular, testable, and easier to maintain.

## State Management Strategies

- **State Hoisting**: Lift state up to the nearest common ancestor to share state between composables.
- **ViewModel State**: Use ViewModel to manage UI-related data that survives configuration changes.

## Performance Optimization Tips

- **Recomposition**: Minimize recomposition by using `remember` and `derivedStateOf` for expensive calculations.
- **Lazy Components**: Use `LazyColumn` and `LazyRow` for lists to improve scrolling performance.