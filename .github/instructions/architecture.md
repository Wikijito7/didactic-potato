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
- **Location**: `data/repository/`, `data/local/`

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
- Use .orEmpty() for strings/collections instead of ?: default
- Make optional API fields nullable in DTOs/BOs/VOs
- Secure token storage with EncryptedSharedPreferences
- Use koinInject for ViewModel injection in Compose
- Add @SerialName to DTOs for R8 compatibility