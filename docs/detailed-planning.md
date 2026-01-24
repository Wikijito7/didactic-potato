# Detailed Implementation Planning - Didactic Potato

## Overview
This document outlines the detailed planning for implementing the didactic-potato Android app following Clean Architecture principles. The app connects to ESP32 sensors network with features for home dashboard, public sensors, user profile, and sensor pairing.

## Current Status
- ✅ Rules files created (.cursorrules)
- ✅ Dependencies added (Koin, Ktor, Room, etc.)
- ✅ Package structure set up
- ✅ API layer implemented (DTOs, Ktor client, AuthApi)
- ✅ Domain layer with use cases
- ✅ Home screen refactored with ViewModel

## High Priority Tasks

### 1. Authentication System
- Implement login/register screens with Compose
- Add token storage (SharedPreferences/EncryptedSharedPreferences)
- Handle 2FA authentication flow
- Add auth interceptors to Ktor client

### 2. API Integration
- Complete API layer with all endpoints
- Add authentication headers (Bearer token)
- Implement error handling and retry logic
- Add response mappers (DTO → BO)

### 3. Home Screen Enhancement
- Connect to real sensor data API
- Add refresh functionality
- Implement proper error states
- Add loading indicators

## Medium Priority Tasks

### 4. Data Layer
- Implement Room database for local storage
- Create entities for sensors, users, tokens
- Add DAOs and repositories
- Implement offline caching

### 5. Domain Layer
- Add more use cases (GetSensors, UpdateSensor, etc.)
- Create BO models for all data
- Add business logic validation
- Implement data transformation

### 6. Sensors Screen
- Create public sensors browsing screen
- Add filtering and search functionality
- Implement favorite sensors feature
- Add sensor detail views

### 7. Profile Screen
- User information display and editing
- Associated sensors management
- Image upload functionality
- Settings and preferences

### 8. Navigation
- Implement proper screen navigation
- Add deep linking support
- Handle back stack and state preservation
- Add navigation arguments

### 9. Testing Setup
- Add JUnit + Mockk for unit tests
- Test all use cases and repositories
- Add ViewModel testing
- Set up test data and mocks

### 10. Mappers
- API ↔ Domain mappers
- Domain ↔ UI mappers
- Ensure proper data transformation
- Handle null safety

## Low Priority Tasks

### 11. Sensor Pairing
- Bluetooth integration
- ESP32 device discovery
- Pairing workflow UI
- Configuration and registration

### 12. Additional Features
- Charts for sensor historical data
- Push notifications
- Background sync
- Advanced error handling

## Architecture Layers

### API Layer
- DTOs for all API responses
- Ktor client with interceptors
- Error handling and logging
- Authentication management

### Data Layer
- Room database entities
- Data sources (API, Local)
- Repositories implementing interfaces
- Data access patterns

### Domain Layer
- Business logic use cases
- BO models (business objects)
- Validation and transformation
- Interface definitions

### UI Layer
- Compose screens with ViewModels
- State management with StateFlow
- VO models (view objects)
- Event handling

## Dependencies
- Koin 4.1.1 (DI)
- Ktor 2.3.12 (Networking)
- Room 2.6.1 (Local DB)
- Compose BOM 2026.01.00 (UI)
- Kotlinx Serialization 1.7.3
- Kotlinx Coroutines 1.9.0

## Testing Strategy
- Unit tests for all non-UI classes
- JUnit 4 + Mockk for mocking
- Test coverage for use cases, repositories, ViewModels
- Integration tests for API calls

## Code Conventions
- Clean Architecture layers
- Koin for dependency injection
- MVI pattern for state management
- Extension functions for mappers
- Immutable data classes
- Proper error handling

## File Structure
```
app/src/main/java/es/wokis/didacticpotato/
├── data/
│   ├── api/          # DTOs, ApiClient, ApiServices
│   ├── auth/         # AuthDataSource
│   ├── repository/   # Repositories
│   └── local/        # Room entities, DAOs
├── domain/
│   ├── model/        # BO models
│   └── usecase/      # Use cases
├── ui/
│   ├── home/         # Home screen, ViewModel, State
│   ├── sensors/      # Sensors screen
│   ├── profile/      # Profile screen
│   └── theme/        # Theme, colors, typography
├── di/               # Koin modules
└── MainActivity.kt
```

## Next Steps
1. Implement authentication UI
2. Complete API authentication
3. Add local data storage
4. Implement remaining screens
5. Add comprehensive testing