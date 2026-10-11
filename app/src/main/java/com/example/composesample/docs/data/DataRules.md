# Data Class Implementation Guidelines

## Naming Conventions

### File Naming Pattern
```
PATTERN: {Domain}Data.kt or {Domain}ListData.kt   — the model the app uses
         {Domain}Response.kt                       — the API response model that Gson parses into
```

## ⚠️ Gson does not honour Kotlin defaults or null-safety

Both converters in this project — Retrofit's `GsonConverterFactory` and Ktor's `gson()` — end in `Gson.fromJson`, so the rules below are shaped by how Gson creates objects:

- Gson calls a no-argument constructor only when one exists, and Kotlin generates one only when **every** constructor parameter has a default value. Otherwise Gson allocates the object **without running any constructor** (JDK `Unsafe`). A key missing from the JSON then keeps the JVM zero value — `null` for objects (even when the Kotlin type is non-null), `false` for `Boolean`, `0` for numbers. Kotlin default values are never applied.
- An explicit JSON `null` is written into the field even when every parameter has a default.
- Kotlin does not null-check a property read, so the `null` surfaces later, as an NPE at the first use (`roles.size`), far away from the parse.

Measured on 2026-10-12 with Gson 2.14.0 and Kotlin 2.4.20 — on the JVM, on the test device (ART, API 33), and through Retrofit 3.0.0's `GsonConverterFactory`. Input `{"user_id":"u1"}` parsed into the example this document used to recommend:

| Declared | Parsed |
|----------|--------|
| `userId: String` | `"u1"` |
| `userName: String? = null` | `null` |
| `isActive: Boolean = true` | **`false`** — the default is ignored |
| `roles: List<String> = emptyList()` | **`null`** — NPE on `roles.size` |

The same class with a default on every parameter gets its defaults for missing keys, but `{"roles":null}` still yields `roles == null`. `GsonBuilder().disableJdkUnsafe()` turns the silent case into an immediate `JsonIOException` (not enabled in this project's DI).

## Data Class Implementation Rules

### 1. Basic Structure
```kotlin
// API response model: every server-supplied field is nullable
data class ExampleResponse(
    @SerializedName("server_field")
    val clientField: Type?
)

// Model the app uses: non-null, built once by a mapper
data class ExampleData(
    val clientField: Type
)

fun ExampleResponse.toExampleData() = ExampleData(
    clientField = clientField ?: defaultValue // or requireNotNull(clientField) { "server_field missing" }
)
```

### 2. Field Conventions
```kotlin
// RULES
NAMING:
- Server fields: snake_case
- Client fields: camelCase
- Boolean flags: prefix with 'is' or 'has'
- YN flags: String type with "Y"/"N"

// EXAMPLE
data class MembershipResponse(
    @SerializedName("user_id")          // Server: snake_case
    val userId: String?,                 // Client: camelCase
    @SerializedName("is_premium")       // Boolean prefix
    val isPremium: Boolean?,
    @SerializedName("subscription_yn")  // YN flag
    val subscriptionYn: String?
)
```

### 3. Nullability and Defaults
```kotlin
// RULES
RESPONSE MODELS (parsed by Gson):
- Every server-supplied field: Type? — the server can omit it or send null, and Gson stops neither
- Do not rely on Kotlin default values (= false, = emptyList()) — Gson skips them (see above)
- Booleans stay Boolean? until mapping — null ("not sent") and false are different answers

APP MODELS (built by the mapper):
- Required fields: non-null, checked with requireNotNull — fails at the parse boundary with the
  field name instead of an NPE somewhere in the UI
- Optional fields: Type?, or a documented default applied in the mapper
- Collections: never null — .orEmpty() in the mapper

The mapper lives in the data layer, next to the response model (the domain module stays free of Gson).

// EXAMPLE
data class ChannelResponse(
    @SerializedName("required_field") val requiredField: String?,
    @SerializedName("optional_field") val optionalField: String?,
    @SerializedName("items") val items: List<String>?
)

data class ChannelData(
    val requiredField: String,  // Required - non-null
    val optionalField: String?, // Optional - nullable
    val items: List<String>     // Collection - never null
)

fun ChannelResponse.toChannelData() = ChannelData(
    requiredField = requireNotNull(requiredField) { "required_field missing" },
    optionalField = optionalField,
    items = items.orEmpty()
)
```

### 4. Annotation Usage
```kotlin
// REQUIRED ANNOTATIONS
1. @SerializedName:
   - REQUIRED for all API response fields (on the response model)
   - MUST match server field name exactly

2. @Parcelize:
   - REQUIRED for Android component data transfer
   - MUST implement Parcelable
   - Put it on the app model (already validated), not on the response model

// EXAMPLE
@Parcelize
data class EntityData(
    val entityId: String
) : Parcelable
```

### 5. Companion Object Patterns
```kotlin
// PATTERN: Default/Mock objects
data class DataModel(
    val field: String
) {
    companion object {
        val EMPTY = DataModel("")
        val MOCK = DataModel("test_data")
    }
}
```

### 6. Documentation Rules
```kotlin
// PATTERN: Field documentation
data class DocumentedResponse(
    // REQUIRED: Document complex business logic
    @SerializedName("type")
    val type: String?, // Possible values: NORMAL, IMPORTANT, EMERGENCY

    // REQUIRED: Document state enums
    @SerializedName("status")
    val status: String? // States: DRAFT, PUBLISHED, DELETED
)
```

## Implementation Guidelines

### 1. Field Type Selection
```
RULES:
1. String: For text and YN flags
2. Boolean: For true/false flags
3. Int/Long: For numeric IDs
4. List<T>: For collections
(Each one nullable in a response model, see "Nullability and Defaults".)
```

### 2. Serialization
```
REQUIREMENTS:
1. ALL response fields must have @SerializedName
2. Field names must match API spec exactly
3. Response fields are nullable; defaults are applied in the mapper, never through Kotlin default values
```

### 3. Legacy Code
```
RULES:
1. Legacy code MUST be in legacy package
2. New code MUST NOT follow legacy patterns
3. Migration plan MUST be documented
```

## Validation Requirements

1. **Naming Validation**
   - MUST follow defined patterns
   - MUST use correct case conventions
   - MUST maintain consistency

2. **Type Validation**
   - MUST properly handle nullability (nullable response fields, non-null app models)
   - MUST apply defaults in the mapper, not as Kotlin default values on response models
   - MUST use correct collection types

3. **Documentation Requirements**
   - MUST document business logic
   - MUST include type enumerations
   - MUST note legacy status

4. **Migration Rules**
   - MUST identify legacy code
   - MUST plan for migration
   - MUST document timeline

## Example of Data Class Implementation

```kotlin
// API response model
data class UserResponse(
    @SerializedName("user_id")
    val userId: String?,
    @SerializedName("user_name")
    val userName: String?,
    @SerializedName("is_active")
    val isActive: Boolean?,
    @SerializedName("roles")
    val roles: List<String>?
)

// Model the app uses and passes between components
@Parcelize
data class UserData(
    val userId: String,       // Required field
    val userName: String?,    // Optional field
    val isActive: Boolean,    // Default applied in the mapper
    val roles: List<String>   // Collection, never null
) : Parcelable

fun UserResponse.toUserData() = UserData(
    userId = requireNotNull(userId) { "user_id missing" },
    userName = userName,
    isActive = isActive ?: true,
    roles = roles.orEmpty()
)
```

// This example demonstrates @SerializedName on every response field, nullable response fields, and defaults/validation applied once in the mapper.

## Additional Examples and Use Cases

### Complex Data Class Example
```kotlin
// Kotlin data classes cannot extend other data classes.
// Use composition instead of inheritance.
// App model built by a mapper as above (its response model, with nullable @SerializedName fields, is omitted).
@Parcelize
data class AdvancedUserData(
    val userId: String,
    val userName: String?,
    val isActive: Boolean,
    val roles: List<String>,
    val permissions: Map<String, Boolean>
) : Parcelable
```

### Real-World Use Case
- **Scenario**: Managing user profiles in a social media app where users have dynamic roles and permissions.
- **Implementation**: Use `AdvancedUserData` as a standalone data class. Share common fields via interfaces or separate sealed class hierarchies rather than data class inheritance.

## Existing Code
- `domain/model/PostData` (the post-list sample, `PostResponse.kt`) predates this rule: Retrofit (and the Ktor call in the same example) parses straight into it, with non-null fields and no no-argument constructor. jsonplaceholder's fixed schema always sends all four keys, so nothing shows today, but a missing key would put `null` into a non-null field. It is left as is — see `KnownLimitations.md`.
