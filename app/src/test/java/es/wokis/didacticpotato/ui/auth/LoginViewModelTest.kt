package es.wokis.didacticpotato.ui.auth

import es.wokis.didacticpotato.domain.model.LoginResultBO
import es.wokis.didacticpotato.domain.usecase.LoginUseCase
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val loginUseCase: LoginUseCase = mockk()
    private lateinit var viewModel: LoginViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = LoginViewModel(loginUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Given initial state When viewModel created Then state has default values`() = runTest {
        // Then
        val state = viewModel.state.value
        assertEquals("", state.username)
        assertEquals("", state.password)
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertFalse(state.success)
    }

    @Test
    fun `When onUsernameChanged Then state updates with new username`() = runTest {
        // When
        viewModel.onUsernameChanged("testUser")
        advanceUntilIdle()

        // Then
        assertEquals("testUser", viewModel.state.value.username)
    }

    @Test
    fun `When onPasswordChanged Then state updates with new password`() = runTest {
        // When
        viewModel.onPasswordChanged("testPass")
        advanceUntilIdle()

        // Then
        assertEquals("testPass", viewModel.state.value.password)
    }

    @Test
    fun `Given valid credentials When onLoginClicked Then returns success`() = runTest {
        // Given
        val successResult = LoginResultBO(token = "token123", success = true)
        coEvery { loginUseCase(any(), any()) } returns successResult

        viewModel.onUsernameChanged("testUser")
        viewModel.onPasswordChanged("testPass")
        advanceUntilIdle()

        // When
        viewModel.onLoginClicked()
        advanceUntilIdle()

        // Then
        val state = viewModel.state.value
        assertTrue(state.success)
        assertFalse(state.isLoading)
        assertNull(state.error)
        coVerify(exactly = 1) { loginUseCase("testUser", "testPass") }
    }

    @Test
    fun `Given invalid credentials When onLoginClicked Then returns error`() = runTest {
        // Given
        val errorResult = LoginResultBO(token = "", success = false, errorMessage = "Invalid credentials")
        coEvery { loginUseCase(any(), any()) } returns errorResult

        viewModel.onUsernameChanged("testUser")
        viewModel.onPasswordChanged("wrongPass")
        advanceUntilIdle()

        // When
        viewModel.onLoginClicked()
        advanceUntilIdle()

        // Then
        val state = viewModel.state.value
        assertFalse(state.success)
        assertFalse(state.isLoading)
        assertEquals("Invalid credentials", state.error)
    }

    @Test
    fun `Given empty credentials When onLoginClicked Then still calls useCase`() = runTest {
        // Given
        coEvery { loginUseCase(any(), any()) } returns LoginResultBO(token = "", success = false)

        // When
        viewModel.onLoginClicked()
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { loginUseCase("", "") }
    }

    @Test
    fun `When onLoginClicked Then sets loading state initially`() = runTest {
        // Given
        coEvery { loginUseCase(any(), any()) } returns LoginResultBO(token = "", success = true)

        viewModel.onUsernameChanged("user")
        viewModel.onPasswordChanged("pass")
        advanceUntilIdle()

        // When - start login but don't wait for completion
        viewModel.onLoginClicked()

        // Then - immediately check loading is true (before advanceUntilIdle)
        assertTrue(viewModel.state.value.isLoading)
        assertNull(viewModel.state.value.error)

        // Complete the coroutine
        advanceUntilIdle()
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `Given network error When onLoginClicked Then shows error message`() = runTest {
        // Given
        val errorMessage = "Network timeout"
        coEvery { loginUseCase(any(), any()) } returns LoginResultBO(
            token = "",
            success = false,
            errorMessage = errorMessage
        )

        viewModel.onUsernameChanged("testUser")
        viewModel.onPasswordChanged("testPass")
        advanceUntilIdle()

        // When
        viewModel.onLoginClicked()
        advanceUntilIdle()

        // Then
        assertEquals(errorMessage, viewModel.state.value.error)
        assertFalse(viewModel.state.value.success)
    }

    @Test
    fun `Given multiple login attempts When onLoginClicked Then calls useCase multiple times`() = runTest {
        // Given
        coEvery { loginUseCase(any(), any()) } returns LoginResultBO(token = "", success = false)

        // When
        viewModel.onLoginClicked()
        advanceUntilIdle()
        viewModel.onLoginClicked()
        advanceUntilIdle()
        viewModel.onLoginClicked()
        advanceUntilIdle()

        // Then
        coVerify(exactly = 3) { loginUseCase(any(), any()) }
    }
}
