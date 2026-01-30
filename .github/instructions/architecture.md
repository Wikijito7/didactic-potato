# Architecture Instructions

## Clean Architecture Implementation

### Layer Structure
```
API Layer (External) ← DataSource ← Repository ← UseCase ← ViewModel ← UI Layer (Internal)
```

### API Layer
- **Purpose**: Handle external API communication
- **Components**:
  - DTOs: Data Transfer Objects with @Serializable and @SerialName
  - All DTO fields should be nullable to handle missing API fields
  - Ktor HttpClient with OkHttp engine
  - Content negotiation (JSON)
  - Logging and error handling
- **Naming**: *DTO.kt (e.g., LoginRequestDTO, SensorDataDTO)
- **Location**: `data/api/`

### Data Layer
- **Components**:
  - DataSources: Abstract API/database access
  - Repositories: Implement business data access
  - Local storage: Room entities and DAOs
- **Location**: `data/repository/`, `data/local/`, `data/remote/`

#### DataSource Organization (Critical)
Both local and remote datasources must follow the same structure:
```
data/
├── local/
│   └── datasource/
│       ├── SensorLocalDataSource.kt
│       └── UserLocalDataSource.kt
├── remote/
│   └── datasource/
│       ├── AuthRemoteDataSource.kt
│       └── SensorRemoteDataSource.kt
```

**Rule**: Never scatter remote datasources in feature folders (e.g., `data/auth/`, `data/sensor/`). Always use `data/remote/datasource/` for consistency.

### Domain Layer
- **Purpose**: Business logic independent of frameworks
- **Components**:
  - BOs: Business Objects representing domain entities
  - UseCases: Single responsibility business operations
- **Naming**: *BO.kt for models, *UseCase.kt for operations
- **Location**: `domain/model/`, `domain/usecase/`

### UI Layer
- **Components**:
  - VOs: View Objects for UI presentation
  - ViewModels: State management and UI logic
  - Compose screens with state collection
- **Naming**: *VO.kt for UI models, *State.kt for screen state
- **Location**: `ui/*/`

### Dependency Injection
- **Framework**: Koin 4.x
- **Modules**: Separate modules for data, domain, ui
- **Registration**:
  - `single {}` for singletons (HttpClient, Repositories)
  - `factory {}` for new instances (UseCases)
  - `viewModelOf(::VM)` for ViewModels
- **ViewModel Injection in Compose**: Use `koinInject<VM>()` instead of `viewModel()` for proper dependency injection with constructor parameters
- **Pattern**:
  ```kotlin
  @Composable
  fun MyScreen(viewModel: MyViewModel = koinInject()) {
      // Use viewModel
  }
  ```
- **Why koinInject**: Bypasses default factory limitations, works with parameterized ViewModels

### Data Flow
1. UI triggers action on ViewModel
2. ViewModel calls UseCase
3. UseCase orchestrates Repository calls
4. Repository uses DataSource for API/DB access
5. Data flows back through mappers: DTO → BO → VO
6. UI updates reactively via StateFlow

### Mappers
- **Location**: 
  - DTO ↔ BO: `data/api/*DtoMappers.kt` (near API layer)
  - BO ↔ VO: `domain/mappers/*VoMappers.kt` (domain layer)
- **Implementation**: Extension functions
- **Null Handling**: Provide default values in mappers for nullable DTO fields
- **UI Null Handling**: Display "Unknown" for missing battery, handle optional errors
- **Type Conversions**: Handle API format differences (e.g., Double → Int)
- **Example**:
  ```kotlin
  // DTO to BO (data/api/)
  fun SensorDTO.toBO() = SensorBO(
      name = name.orEmpty(),
      temp = data?.temp ?: 0.0,
      error = data?.error,  // nullable
      battery = data?.battery?.toBO()  // nullable
  )
  
  // BO to VO (domain/mappers/)
  fun SensorBO.toVO() = SensorVO(
      batteryPercentage = battery?.percentage  // nullable
  )
  ```

### API Integration
- **Ktor Client Setup**: OkHttp engine, JSON serialization, SLF4J logging, Bearer auth
- **DTO Design**: Nullable fields, @Serializable with @SerialName for R8
- **Response Formats**: Handle different JSON structures (nested vs flat)
- **Error Handling**: expectSuccess = false, manual error processing

### Authentication System
- **Token Management**: EncryptedSharedPreferences with AES256 encryption
- **Ktor Auth**: Bearer plugin with refreshTokens for dynamic loading
- **Security**: Automatic token cleanup, secure storage
- **UI Integration**: Conditional rendering based on auth state

### Logging Configuration
- **SLF4J Integration**: Android-compatible logging with custom Ktor logger
- **Levels**: ALL for debugging, NONE for production
- **Security**: Avoid logging sensitive data (tokens, passwords)

### Room Database & Caching
- **Version Compatibility**: Kotlin 2.3.0 + Room 2.8.4 + KSP 2.3.4
- **Naming**: Use DBO (Database Object) not Entity (e.g., `SensorDBO`)
- **Cache Pattern**: Cache-aside with expiration check
- **Timestamp**: Add `lastUpdated` field, update with `System.currentTimeMillis()`
- **Clear Before Save**: Always `deleteAll()` before inserting new data
- **Order Preservation**: Don't use `ORDER BY` in DAOs, preserve API response order
- **Column Names**: Use snake_case (`last_updated`) in SQL, not camelCase
- **Check Cache First**: Verify `hasCachedData()` before showing loading states

### Code Quality Rules
- Use .orEmpty() for strings/collections instead of ?: default
- Make optional API fields nullable in DTOs/BOs/VOs
- Secure token storage with EncryptedSharedPreferences
- Use koinInject for ViewModel injection in Compose
- Add @SerialName to DTOs for R8 compatibility
- Use DBO naming for Room entities (not Entity)
- Clear cache before saving new data to prevent accumulation
- Preserve API response order by avoiding ORDER BY in DAO queries

### File Formatting Rules
- **All files must end with a newline character** (POSIX standard)
- This is required for proper git diffs, Unix tools compatibility, and IDE consistency
- Check: `tail -c 1 filename` should return empty (means newline exists)
- Fix: `echo "" >> filename` adds newline if missing
