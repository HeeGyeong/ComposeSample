# 데이터 클래스 구현 가이드라인

## 네이밍 컨벤션

### 파일 네이밍 패턴
```
패턴: {Domain}Data.kt 또는 {Domain}ListData.kt
```

## 데이터 클래스 구현 규칙

### 1. 기본 구조
```kotlin
// 패턴: 직렬화를 포함한 데이터 클래스
data class ExampleData(
    @SerializedName("server_field")
    val clientField: Type? = defaultValue
)
```

### 2. 필드 컨벤션
```kotlin
// 규칙
네이밍:
- 서버 필드: snake_case
- 클라이언트 필드: camelCase
- Boolean 플래그: 'is' 또는 'has' 접두사
- YN 플래그: "Y"/"N" 값을 갖는 String 타입

// 예시
data class MembershipData(
    @SerializedName("user_id")          // 서버: snake_case
    val userId: String,                  // 클라이언트: camelCase
    @SerializedName("is_premium")       // Boolean 접두사
    val isPremium: Boolean = false,
    @SerializedName("subscription_yn")  // YN 플래그
    val subscriptionYn: String = "N"
)
```

### 3. 널 가능성과 기본값
```kotlin
// 규칙
널 가능 타입:
- 선택적 필드: Type?
- 필수 필드: Type
- 컬렉션: 빈 컬렉션을 기본값으로

// 예시
data class ChannelData(
    val requiredField: String,           // 필수 - non-null
    val optionalField: String? = null,   // 선택 - nullable
    val items: List<String> = emptyList() // 컬렉션 - 빈 값 기본
)
```

### 4. 애노테이션 사용
```kotlin
// 필수 애노테이션
1. @SerializedName:
   - 모든 API 응답 필드에 필수
   - 서버 필드명과 정확히 일치해야 함

2. @Parcelize:
   - Android 컴포넌트 간 데이터 전달 시 필수
   - Parcelable을 반드시 구현

// 예시
@Parcelize
data class EntityData(
    @SerializedName("entity_id")
    val entityId: String
) : Parcelable
```

### 5. 컴패니언 오브젝트 패턴
```kotlin
// 패턴: 기본값/Mock 오브젝트
data class DataModel(
    val field: String
) {
    companion object {
        val EMPTY = DataModel("")
        val MOCK = DataModel("test_data")
    }
}
```

### 6. 문서화 규칙
```kotlin
// 패턴: 필드 문서화
data class DocumentedData(
    // 필수: 복잡한 비즈니스 로직을 문서화
    @SerializedName("type")
    val type: String, // 가능한 값: NORMAL, IMPORTANT, EMERGENCY
    
    // 필수: 상태 enum을 문서화
    @SerializedName("status")
    val status: String // 상태: DRAFT, PUBLISHED, DELETED
)
```

## 구현 가이드라인

### 1. 필드 타입 선택
```
규칙:
1. String: 텍스트와 YN 플래그
2. Boolean: true/false 플래그
3. Int/Long: 숫자 ID
4. List<T>: 컬렉션
```

### 2. 직렬화
```
요구사항:
1. 모든 필드에 @SerializedName이 있어야 함
2. 필드명은 API 스펙과 정확히 일치해야 함
3. 널 가능 필드에는 기본값 지정
```

### 3. 레거시 코드
```
규칙:
1. 레거시 코드는 반드시 legacy 패키지에 위치
2. 새 코드는 레거시 패턴을 따르면 안 됨
3. 마이그레이션 계획을 반드시 문서화
```

## 검증 요구사항

1. **네이밍 검증**
   - 정의된 패턴을 반드시 준수
   - 올바른 케이스 컨벤션을 반드시 사용
   - 일관성을 반드시 유지

2. **타입 검증**
   - 널 가능성을 반드시 적절히 처리
   - 기본값을 반드시 포함
   - 올바른 컬렉션 타입을 반드시 사용

3. **문서화 요구사항**
   - 비즈니스 로직을 반드시 문서화
   - 타입 열거값을 반드시 포함
   - 레거시 상태를 반드시 명시

4. **마이그레이션 규칙**
   - 레거시 코드를 반드시 식별
   - 마이그레이션을 반드시 계획
   - 일정을 반드시 문서화

## 데이터 클래스 구현 예시

```kotlin
// 가이드라인을 따르는 데이터 클래스 예시
@Parcelize
data class UserData(
    @SerializedName("user_id")
    val userId: String, // 필수 필드
    @SerializedName("user_name")
    val userName: String? = null, // 기본값 null의 선택적 필드
    @SerializedName("is_active")
    val isActive: Boolean = true, // 기본값을 가진 Boolean 플래그
    @SerializedName("roles")
    val roles: List<String> = emptyList() // 빈 리스트를 기본값으로 하는 컬렉션
) : Parcelable
```

// 이 예시는 모든 필드에 @SerializedName 사용, 올바른 널 가능성 처리, 선택적 필드와 컬렉션의 기본값 지정을 보여줍니다.

## 추가 예시 및 활용 사례

### 복합 데이터 클래스 예시
```kotlin
// Kotlin 데이터 클래스는 다른 데이터 클래스를 상속할 수 없습니다.
// 상속 대신 컴포지션을 사용하세요.
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
) : Parcelable
```

### 실제 활용 사례
- **시나리오**: 사용자가 동적인 역할과 권한을 갖는 소셜 미디어 앱에서 사용자 프로필을 관리.
- **구현**: `AdvancedUserData`를 독립적인 데이터 클래스로 사용. 공통 필드는 데이터 클래스 상속이 아니라 인터페이스나 별도의 sealed 클래스 계층으로 공유합니다.
