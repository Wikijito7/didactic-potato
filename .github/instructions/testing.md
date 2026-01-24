# Testing Instructions

## Testing Strategy (Planned)

### Overview
- **Framework**: JUnit 4 + Mockk for unit testing
- **Coverage**: All non-UI classes (UseCases, Repositories, ViewModels)
- **Location**: `src/test/java/` (same package structure)

### Unit Testing Structure

#### UseCase Testing
```kotlin
class LoginUseCaseTest {

    private lateinit var useCase: LoginUseCase
    private val repository: AuthRepository = mockk()

    @Before
    fun setUp() {
        useCase = LoginUseCase(repository)
    }

    @Test
    fun `Given valid credentials When login Then returns success result`() = runTest {
        // Given
        coEvery { repository.login(any(), any()) } returns "token"

        // When
        val result = useCase("user", "pass")

        // Then
        assertTrue(result.success)
        assertEquals("token", result.token)
    }
}
```

#### Repository Testing
```kotlin
class AuthRepositoryTest {

    private lateinit var repository: AuthRepository
    private val dataSource: AuthDataSource = mockk()
    private val tokenProvider: TokenProvider = mockk()

    @Before
    fun setUp() {
        repository = AuthRepository(dataSource, tokenProvider)
    }

    @Test
    fun `Given login request When login Then calls data source and saves token`() = runTest {
        // Given
        val response = LoginResponseDTO("token")
        coEvery { dataSource.login(any(), any()) } returns response
        every { tokenProvider.saveToken(any()) } just Runs

        // When
        repository.login("user", "pass")

        // Then
        coVerify { dataSource.login("user", "pass") }
        verify { tokenProvider.saveToken("token") }
    }
}
```

#### ViewModel Testing
```kotlin
class LoginViewModelTest {

    private lateinit var viewModel: LoginViewModel
    private val useCase: LoginUseCase = mockk()

    @Before
    fun setUp() {
        viewModel = LoginViewModel(useCase)
    }

    @Test
    fun `Given valid login data When login clicked Then updates state to success`() = runTest {
        // Given
        val successResult = LoginResultBO(token = "token", success = true)
        coEvery { useCase.invoke(any(), any()) } returns successResult

        // When
        viewModel.onUsernameChanged("user")
        viewModel.onPasswordChanged("pass")
        viewModel.onLoginClicked()

        // Then
        val state = viewModel.state.value
        assertTrue(state.success)
        assertFalse(state.isLoading)
    }
}
```

### Mocking with Mockk
- **Basic Mock**: `val mock = mockk<MyClass>()`
- **Relax**: `mockk<MyClass>(relaxed = true)` for default implementations
- **Verification**: `verify { mock.method() }`
- **Stubbing**: `every { mock.method() } returns value`
- **Coroutine Mocking**: `coEvery`, `coVerify` for suspend functions

### Test Data
- Create test data factories
- Use meaningful test values
- Test edge cases and error scenarios

### Test Naming
- **BDD Style**: `Given [context] When [action] Then [expected result]`
- Example: `Given invalid credentials When login Then returns error`

### Dependencies for Testing
```kotlin
dependencies {
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
}
```

### Running Tests
```bash
./gradlew test                    # Run all tests
./gradlew testDebugUnitTest       # Run debug variant tests
./gradlew test --tests "*TestName" # Run specific test class
```

### Coverage Goals
- **UseCases**: 100% coverage
- **Repositories**: 100% coverage
- **ViewModels**: 90%+ coverage
- **DataSources**: 80%+ coverage

### Future Testing (Not Yet Implemented)
- **Integration Tests**: API calls with test server
- **UI Tests**: Compose testing with TestRule
- **End-to-End Tests**: Full user flows

### Best Practices
- Test one thing per test method
- Use descriptive test names
- Arrange-Act-Assert pattern
- Mock external dependencies
- Test error scenarios
- Keep tests fast and independent