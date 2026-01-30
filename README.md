# Didactic Potato
An Android application for the shiny-tribble server, implementing Clean Architecture with modern Android development practices.

## Architecture
This app follows Clean Architecture principles with clear separation of concerns:

- **API Layer**: Ktor client for network communication with nullable DTOs and @SerialName for R8 compatibility
- **Data Layer**: Repositories and data sources with encrypted token storage
- **Domain Layer**: Business logic with UseCases and extension function mappers
- **UI Layer**: Compose screens with ViewModel pattern and koinInject for DI

📖 **Detailed Architecture Guide**: [.github/instructions/architecture.md](.github/instructions/architecture.md)

## Project Status
- ✅ Clean Architecture foundation implemented
- ✅ Authentication system with secure token management  
- ✅ Sensor data monitoring with multiple API response formats
- ✅ Null-safe data mapping and UI handling
- ✅ Dynamic authentication token loading (fixed caching issue)
- 🚧 Local database integration (Room)
- 🚧 Unit testing implementation
- 🚧 Error message display in UI
- 🚧 Sensor pairing workflow

## Technologies & Libraries

### Core Android
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: Clean Architecture
- **Dependency Injection**: Koin 4.1.1

### Networking
- **HTTP Client**: Ktor 2.3.12 with OkHttp engine
- **Serialization**: Kotlinx Serialization
- **Authentication**: Bearer token with encrypted storage

### Database & Storage
- **Local DB**: Room (planned)
- **Secure Storage**: EncryptedSharedPreferences

### Async Programming
- **Coroutines**: Kotlinx Coroutines
- **Flow**: StateFlow for reactive UI updates

### Development Tools
- **Build Tool**: Gradle with Kotlin DSL
- **Logging**: SLF4J with Android logging
- **Code Generation**: KSP for Room

## Key Features
- User authentication with secure token management
- Sensor data monitoring and display
- Responsive UI with Compose
- Offline-capable architecture (planned)

## Project Structure
```
app/src/main/java/es/wokis/didacticpotato/
├── data/
│   ├── api/          # DTOs, API clients, mappers
│   ├── auth/         # Token management
│   ├── repository/   # Data access layer
│   └── sensor/       # Sensor data sources
├── domain/
│   ├── model/        # Business objects
│   ├── usecase/      # Business logic
│   └── mappers/      # BO to VO mappers
├── ui/               # Compose screens and ViewModels
└── di/               # Dependency injection modules
```

## Documentation & Resources

### Key Documentation
- 📋 **Architecture & Implementation**: [.github/instructions/architecture.md](.github/instructions/architecture.md)
- 🧪 **Testing Strategy**: [.github/instructions/testing.md](.github/instructions/testing.md)
- 🎨 **UI Patterns**: [.github/instructions/ui.md](.github/instructions/ui.md)

### Development Setup
1. Clone the repository
2. Open in Android Studio Arctic Fox+
3. Ensure JDK 17 and Android SDK API 24+
4. Run `./gradlew build` to verify setup
5. Connect device/emulator and run debug build

### API Integration
- **Server**: shiny-tribble
- **Auth**: JWT Bearer tokens with dynamic loading
- **Endpoints**: Login/Register, Sensor data (nested & flat formats)
- **Security**: EncryptedSharedPreferences, no token logging

### Contributing
1. Follow Clean Architecture patterns (see architecture.md)
2. Use Koin DI with koinInject in Compose
3. Handle nulls with .orEmpty() and nullable types
4. Add @SerialName to DTOs for R8 compatibility
5. Write tests for business logic
6. Update documentation for architectural changes

## Tech Stack Summary
**Core**: Kotlin, Jetpack Compose, Clean Architecture  
**Networking**: Ktor 2.3.12, Kotlinx Serialization  
**DI**: Koin 4.1.1  
**Security**: EncryptedSharedPreferences  
**Async**: Kotlin Coroutines, StateFlow  
**Build**: Gradle Kotlin DSL, KSP  

For detailed library versions and usage patterns, see the architecture guide.
