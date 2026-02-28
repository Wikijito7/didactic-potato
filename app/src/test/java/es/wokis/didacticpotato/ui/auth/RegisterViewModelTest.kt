package es.wokis.didacticpotato.ui.auth

import es.wokis.didacticpotato.domain.model.RegisterResultBO
import es.wokis.didacticpotato.domain.usecase.RegisterUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RegisterViewModelTest {

    private val registerUseCase: RegisterUseCase = mockk()
    private lateinit var viewModel: RegisterViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = RegisterViewModel(registerUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Given initial state When viewModel created Then has default values`() = runTest {
        // Then
        val state = viewModel.state.value
        assertEquals("", state.email)
        assertEquals("", state.username)
        assertEquals("", state.password)
        assertEquals("", state.confirmPassword)
        assertEquals("en", state.lang)
        assertFalse(state.isLoading)
        assertFalse(state.success)
        assertNull(state.error)
    }

    @Test
    fun `When onEmailChanged Then state updates`() = runTest {
        // When
        viewModel.onEmailChanged("test@example.com")
        advanceUntilIdle()

        // Then
        assertEquals("test@example.com", viewModel.state.value.email)
        assertNull(viewModel.state.value.emailError)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `When onUsernameChanged Then state updates`() = runTest {
        // When
        viewModel.onUsernameChanged("testUser")
        advanceUntilIdle()

        // Then
        assertEquals("testUser", viewModel.state.value.username)
        assertNull(viewModel.state.value.usernameError)
    }

    @Test
    fun `When onPasswordChanged Then state updates`() = runTest {
        // When
        viewModel.onPasswordChanged("password123")
        advanceUntilIdle()

        // Then
        assertEquals("password123", viewModel.state.value.password)
        assertNull(viewModel.state.value.passwordError)
    }

    @Test
    fun `When onConfirmPasswordChanged Then state updates`() = runTest {
        // When
        viewModel.onConfirmPasswordChanged("password123")
        advanceUntilIdle()

        // Then
        assertEquals("password123", viewModel.state.value.confirmPassword)
        assertNull(viewModel.state.value.confirmPasswordError)
    }

    @Test
    fun `Given valid inputs When onRegisterClicked Then returns success`() = runTest {
        // Given
        val successResult = RegisterResultBO(success = true)
        coEvery { registerUseCase(any(), any(), any(), any()) } returns successResult

        viewModel.onEmailChanged("test@example.com")
        viewModel.onUsernameChanged("testUser")
        viewModel.onPasswordChanged("password123")
        viewModel.onConfirmPasswordChanged("password123")
        advanceUntilIdle()

        // When
        viewModel.onRegisterClicked()
        advanceUntilIdle()

        // Then
        val state = viewModel.state.value
        assertTrue(state.success)
        assertFalse(state.isLoading)
        assertNull(state.error)
        coVerify(exactly = 1) {
            registerUseCase("test@example.com", "testUser", "password123", "en")
        }
    }

    @Test
    fun `Given empty email When onRegisterClicked Then shows validation error`() = runTest {
        // Given
        viewModel.onEmailChanged("")
        viewModel.onUsernameChanged("testUser")
        viewModel.onPasswordChanged("password123")
        viewModel.onConfirmPasswordChanged("password123")
        advanceUntilIdle()

        // When
        viewModel.onRegisterClicked()
        advanceUntilIdle()

        // Then
        val state = viewModel.state.value
        assertEquals("Email is required", state.emailError)
        assertFalse(state.success)
        assertFalse(state.isLoading)
    }

    @Test
    fun `Given invalid email format When onRegisterClicked Then shows validation error`() = runTest {
        // Given
        viewModel.onEmailChanged("invalid-email")
        viewModel.onUsernameChanged("testUser")
        viewModel.onPasswordChanged("password123")
        viewModel.onConfirmPasswordChanged("password123")
        advanceUntilIdle()

        // When
        viewModel.onRegisterClicked()
        advanceUntilIdle()

        // Then
        assertEquals("Invalid email format", viewModel.state.value.emailError)
    }

    @Test
    fun `Given empty username When onRegisterClicked Then shows validation error`() = runTest {
        // Given
        viewModel.onEmailChanged("test@example.com")
        viewModel.onUsernameChanged("")
        viewModel.onPasswordChanged("password123")
        viewModel.onConfirmPasswordChanged("password123")
        advanceUntilIdle()

        // When
        viewModel.onRegisterClicked()
        advanceUntilIdle()

        // Then
        assertEquals("Username is required", viewModel.state.value.usernameError)
    }

    @Test
    fun `Given short password When onRegisterClicked Then shows validation error`() = runTest {
        // Given
        viewModel.onEmailChanged("test@example.com")
        viewModel.onUsernameChanged("testUser")
        viewModel.onPasswordChanged("short")
        viewModel.onConfirmPasswordChanged("short")
        advanceUntilIdle()

        // When
        viewModel.onRegisterClicked()
        advanceUntilIdle()

        // Then
        assertEquals("Password must be at least 8 characters", viewModel.state.value.passwordError)
    }

    @Test
    fun `Given mismatched passwords When onRegisterClicked Then shows validation error`() = runTest {
        // Given
        viewModel.onEmailChanged("test@example.com")
        viewModel.onUsernameChanged("testUser")
        viewModel.onPasswordChanged("password123")
        viewModel.onConfirmPasswordChanged("different123")
        advanceUntilIdle()

        // When
        viewModel.onRegisterClicked()
        advanceUntilIdle()

        // Then
        assertEquals("Passwords do not match", viewModel.state.value.confirmPasswordError)
    }

    @Test
    fun `Given registration fails When onRegisterClicked Then shows error`() = runTest {
        // Given
        val errorResult = RegisterResultBO(success = false, errorMessage = "Email already exists")
        coEvery { registerUseCase(any(), any(), any(), any()) } returns errorResult

        viewModel.onEmailChanged("test@example.com")
        viewModel.onUsernameChanged("testUser")
        viewModel.onPasswordChanged("password123")
        viewModel.onConfirmPasswordChanged("password123")
        advanceUntilIdle()

        // When
        viewModel.onRegisterClicked()
        advanceUntilIdle()

        // Then
        assertEquals("Email already exists", viewModel.state.value.error)
        assertFalse(viewModel.state.value.success)
    }

    @Test
    fun `When onRegisterClicked Then sets loading state`() = runTest {
        // Given
        coEvery { registerUseCase(any(), any(), any(), any()) } returns RegisterResultBO(success = true)

        viewModel.onEmailChanged("test@example.com")
        viewModel.onUsernameChanged("testUser")
        viewModel.onPasswordChanged("password123")
        viewModel.onConfirmPasswordChanged("password123")
        advanceUntilIdle()

        // When
        viewModel.onRegisterClicked()

        // Then - check immediate loading state
        assertTrue(viewModel.state.value.isLoading)
        assertNull(viewModel.state.value.error)

        // Complete
        advanceUntilIdle()
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `Given multiple validation errors When onRegisterClicked Then shows all errors`() = runTest {
        // Given - multiple invalid fields
        viewModel.onEmailChanged("")
        viewModel.onUsernameChanged("")
        viewModel.onPasswordChanged("short")
        viewModel.onConfirmPasswordChanged("different")
        advanceUntilIdle()

        // When
        viewModel.onRegisterClicked()
        advanceUntilIdle()

        // Then
        assertNotNull(viewModel.state.value.emailError)
        assertNotNull(viewModel.state.value.usernameError)
        assertNotNull(viewModel.state.value.passwordError)
        assertNotNull(viewModel.state.value.confirmPasswordError)
    }
}
