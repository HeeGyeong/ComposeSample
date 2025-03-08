# Data Class Implementation Guidelines

## Naming Conventions

### File Naming Pattern
```
PATTERN: {Domain}Data.kt or {Domain}ListData.kt
```

## Data Class Implementation Rules

### 1. Basic Structure
```kotlin
// PATTERN: Data class with serialization
data class ExampleData(
    @SerializedName("server_field")
    val clientField: Type? = defaultValue
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
data class MembershipData(
    @SerializedName("user_id")          // Server: snake_case
    val userId: String,                  // Client: camelCase
    @SerializedName("is_premium")       // Boolean prefix
    val isPremium: Boolean = false,
    @SerializedName("subscription_yn")  // YN flag
    val subscriptionYn: String = "N"
)
```

### 3. Nullability and Defaults
```kotlin
// RULES
NULLABLE_TYPES:
- Optional fields: Type?
- Required fields: Type
- Collections: Empty collection default

// EXAMPLE
data class ChannelData(
    val requiredField: String,           // Required - non-null
    val optionalField: String? = null,   // Optional - nullable
    val items: List<String> = emptyList() // Collection - empty default
)
```

### 4. Annotation Usage
```kotlin
// REQUIRED ANNOTATIONS
1. @SerializedName:
   - REQUIRED for all API response fields
   - MUST match server field name exactly

2. @Parcelize:
   - REQUIRED for Android component data transfer
   - MUST implement Parcelable

// EXAMPLE
@Parcelize
data class EntityData(
    @SerializedName("entity_id")
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
data class DocumentedData(
    // REQUIRED: Document complex business logic
    @SerializedName("type")
    val type: String, // Possible values: NORMAL, IMPORTANT, EMERGENCY
    
    // REQUIRED: Document state enums
    @SerializedName("status")
    val status: String // States: DRAFT, PUBLISHED, DELETED
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
```

### 2. Serialization
```
REQUIREMENTS:
1. ALL fields must have @SerializedName
2. Field names must match API spec exactly
3. Default values for nullable fields
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
   - MUST properly handle nullability
   - MUST include default values
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
// Example of a data class following the guidelines
@Parcelize
data class UserData(
    @SerializedName("user_id")
    val userId: String, // Required field
    @SerializedName("user_name")
    val userName: String? = null, // Optional field with default null
    @SerializedName("is_active")
    val isActive: Boolean = true, // Boolean flag with default value
    @SerializedName("roles")
    val roles: List<String> = emptyList() // Collection with empty list as default
) : Parcelable
```

// This example demonstrates the use of @SerializedName for all fields, proper handling of nullability, and default values for optional fields and collections.

## Additional Examples and Use Cases

### Complex Data Class Example
```kotlin
// Example of a complex data class with inheritance
@Parcelize
data class AdvancedUserData(
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("user_name")
    val userName: String? = null,
    @SerializedName("is_active")
    val isActive: Boolean = true,
    @SerializedName("roles")
    val roles: List<String> = emptyList(),
    @SerializedName("permissions")
    val permissions: Map<String, Boolean> = emptyMap()
) : UserData(userId, userName, isActive, roles), Parcelable
```

### Real-World Use Case
- **Scenario**: Managing user profiles in a social media app where users have dynamic roles and permissions.
- **Implementation**: Use `AdvancedUserData` to store and manage user information efficiently, leveraging inheritance for shared properties.