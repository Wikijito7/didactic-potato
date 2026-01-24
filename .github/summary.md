# Didactic Potato - Project Knowledge Summary

## Overview
This document summarizes the key knowledge and best practices established for the didactic-potato Android project, an ESP32 sensors network app built with Clean Architecture.

## Architecture & Setup
- **Framework**: Android Compose with Clean Architecture (API → DataSource → Repository → UseCase → ViewModel → UI)
- **DI**: Koin 4.1.1 for dependency injection
- **Networking**: Ktor 2.3.12 with OkHttp, JSON serialization
- **Local Storage**: Room 2.6.1 database
- **Async**: Coroutines for asynchronous operations
- **Security**: EncryptedSharedPreferences for token storage

## Model Conventions
- **API Models**: DTOs with @Serializable, named *DTO.kt
- **Domain Models**: Business objects, named *BO.kt
- **UI Models**: View objects, named *VO.kt
- **Mappers**: Extension functions for layer transformations

## Code Style
- Kotlin-first Android development
- Clean Architecture layer separation
- Dependency injection for testability
- Immutable data classes with StateFlow
- Proper error handling in use cases

## UI Patterns
- Compose screens with ViewModel injection via `koinInject<VM>()`
- State management with MutableStateFlow and collectAsState()
- MVI pattern: Intent → ViewModel → State → UI
- Conditional navigation based on auth state

## ViewModel Injection
- Use `koinInject<VM>()` for all ViewModel dependencies in Compose
- Register ViewModels with `viewModelOf(::VM)` in Koin modules
- Avoid `viewModel()` from Compose for parameterized ViewModels

## SharedPreferences Usage
- Use KTX extension: `sharedPreferences.edit { putString(key, value) }`
- For encrypted prefs, use androidx.security:security-crypto

## Testing Strategy (Planned)
- Unit tests for all non-UI classes (UseCases, Repositories, ViewModels)
- JUnit 4 + Mockk for mocking dependencies
- Integration tests for API calls (future)
- UI tests with Compose testing (future)

## Development Workflow
1. Define DTOs for API contracts
2. Create BOs for business logic
3. Implement Repository pattern
4. Add UseCases for business operations
5. Create ViewModels with State management
6. Build Compose UI with proper state handling
7. Add error handling and loading states
8. Test all layers thoroughly

## File Structure
```
app/src/main/java/es/wokis/didacticpotato/
├── data/
│   ├── api/          # DTOs, Ktor client, API services
│   ├── auth/         # Token management
│   ├── repository/   # Repository implementations
│   └── local/        # Room DAOs, entities (future)
├── domain/
│   ├── model/        # BO models
│   └── usecase/      # Business logic use cases
├── ui/
│   ├── auth/         # Authentication screens
│   ├── home/         # Home dashboard
│   ├── sensors/      # Public sensors (future)
│   └── theme/        # App theming
├── di/               # Koin dependency modules
└── MainActivity.kt   # App entry point
```

## Key Learnings
- Clean Architecture provides excellent separation of concerns
- Koin simplifies dependency injection in Kotlin/Android
- ViewModel injection in Compose requires koinInject for parameterized VMs
- EncryptedSharedPreferences essential for secure token storage
- StateFlow + collectAsState() enables reactive UI updates
- Extension functions improve code readability for mappers

## Tools & Versions
- Kotlin 2.3.0
- Compose BOM 2026.01.00
- Gradle 8.14.3
- Android Gradle Plugin 8.13.2
- KSP 2.3.4 for Room compilation

See instructions/ folder for detailed guides on each aspect.