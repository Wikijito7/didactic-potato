package es.wokis.didacticpotato.ui.profile.options

import es.wokis.didacticpotato.data.auth.TokenProvider
import es.wokis.didacticpotato.data.local.SettingsRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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
class OptionsViewModelTest {

    private val tokenProvider: TokenProvider = mockk()
    private val settingsRepository: SettingsRepository = mockk()
    private lateinit var viewModel: OptionsViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Default mocks for initialization
        every { settingsRepository.isPrivateScreenEnabled() } returns false

        viewModel = OptionsViewModel(
            tokenProvider = tokenProvider,
            settingsRepository = settingsRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Given initial state When viewModel created Then state has default values`() = runTest {
        // Then
        val state = viewModel.state.value
        assertFalse(state.is2FAEnabled)
        assertFalse(state.isPrivateScreenEnabled)
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertNull(state.successMessage)
    }

    @Test
    fun `Given private screen disabled When viewModel created Then loads from settings`() = runTest {
        // Given
        every { settingsRepository.isPrivateScreenEnabled() } returns false

        // When
        val newViewModel = OptionsViewModel(
            tokenProvider = tokenProvider,
            settingsRepository = settingsRepository
        )
        advanceUntilIdle()

        // Then
        assertFalse(newViewModel.state.value.isPrivateScreenEnabled)
    }

    @Test
    fun `Given private screen enabled When viewModel created Then loads from settings`() = runTest {
        // Given
        every { settingsRepository.isPrivateScreenEnabled() } returns true

        // When
        val newViewModel = OptionsViewModel(
            tokenProvider = tokenProvider,
            settingsRepository = settingsRepository
        )
        advanceUntilIdle()

        // Then
        assertTrue(newViewModel.state.value.isPrivateScreenEnabled)
    }

    @Test
    fun `When togglePrivateScreen to enabled Then updates state and saves to settings`() = runTest {
        // Given
        every { settingsRepository.setPrivateScreenEnabled(true) } returns Unit

        // When
        viewModel.togglePrivateScreen(true)

        // Then
        assertTrue(viewModel.state.value.isPrivateScreenEnabled)
        verify { settingsRepository.setPrivateScreenEnabled(true) }
    }

    @Test
    fun `When togglePrivateScreen to disabled Then updates state and saves to settings`() = runTest {
        // Given - first enable it
        every { settingsRepository.setPrivateScreenEnabled(true) } returns Unit
        every { settingsRepository.setPrivateScreenEnabled(false) } returns Unit
        viewModel.togglePrivateScreen(true)
        assertTrue(viewModel.state.value.isPrivateScreenEnabled)

        // When
        viewModel.togglePrivateScreen(false)

        // Then
        assertFalse(viewModel.state.value.isPrivateScreenEnabled)
        verify { settingsRepository.setPrivateScreenEnabled(false) }
    }

    @Test
    fun `Given token clear succeeds When closeAllSessions Then calls onSuccess callback`() = runTest {
        // Given
        every { tokenProvider.clearToken() } returns Unit
        var successCalled = false
        val onSuccess = { successCalled = true }
        val onError: (String) -> Unit = { }

        // When
        viewModel.closeAllSessions(onSuccess, onError)
        advanceUntilIdle()

        // Then
        assertTrue(successCalled)
        verify { tokenProvider.clearToken() }
    }

    @Test
    fun `Given token clear succeeds When closeAllSessions Then shows success message`() = runTest {
        // Given
        every { tokenProvider.clearToken() } returns Unit
        val onSuccess = { }
        val onError: (String) -> Unit = { }

        // When
        viewModel.closeAllSessions(onSuccess, onError)
        advanceUntilIdle()

        // Then
        assertEquals("All sessions closed successfully", viewModel.state.value.successMessage)
        assertNull(viewModel.state.value.error)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `Given token clear fails When closeAllSessions Then calls onError callback`() = runTest {
        // Given
        val errorMessage = "Token clear failed"
        every { tokenProvider.clearToken() } throws Exception(errorMessage)
        var errorCalled = false
        var receivedError = ""
        val onSuccess = { }
        val onError: (String) -> Unit = { error ->
            errorCalled = true
            receivedError = error
        }

        // When
        viewModel.closeAllSessions(onSuccess, onError)
        advanceUntilIdle()

        // Then
        assertTrue(errorCalled)
        assertEquals(errorMessage, receivedError)
    }

    @Test
    fun `Given token clear fails When closeAllSessions Then shows error message`() = runTest {
        // Given
        val errorMessage = "Token clear failed"
        every { tokenProvider.clearToken() } throws Exception(errorMessage)
        val onSuccess = { }
        val onError: (String) -> Unit = { }

        // When
        viewModel.closeAllSessions(onSuccess, onError)
        advanceUntilIdle()

        // Then
        assertEquals(errorMessage, viewModel.state.value.error)
        assertNull(viewModel.state.value.successMessage)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `When closeAllSessions Then sets loading state initially`() = runTest {
        // Given
        every { tokenProvider.clearToken() } returns Unit
        val onSuccess = { }
        val onError: (String) -> Unit = { }

        // When
        viewModel.closeAllSessions(onSuccess, onError)

        // Then - check immediate loading state
        assertTrue(viewModel.state.value.isLoading)
        assertNull(viewModel.state.value.error)
        assertNull(viewModel.state.value.successMessage)

        // Complete
        advanceUntilIdle()
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `Given token clear succeeds When deleteAccount Then calls onSuccess callback`() = runTest {
        // Given
        every { tokenProvider.clearToken() } returns Unit
        var successCalled = false
        val onSuccess = { successCalled = true }
        val onError: (String) -> Unit = { }

        // When
        viewModel.deleteAccount(onSuccess, onError)
        advanceUntilIdle()

        // Then
        assertTrue(successCalled)
        verify { tokenProvider.clearToken() }
    }

    @Test
    fun `Given token clear succeeds When deleteAccount Then shows success message`() = runTest {
        // Given
        every { tokenProvider.clearToken() } returns Unit
        val onSuccess = { }
        val onError: (String) -> Unit = { }

        // When
        viewModel.deleteAccount(onSuccess, onError)
        advanceUntilIdle()

        // Then
        assertEquals("Account deleted successfully", viewModel.state.value.successMessage)
        assertNull(viewModel.state.value.error)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `Given token clear fails When deleteAccount Then calls onError callback`() = runTest {
        // Given
        val errorMessage = "Delete failed"
        every { tokenProvider.clearToken() } throws Exception(errorMessage)
        var errorCalled = false
        var receivedError = ""
        val onSuccess = { }
        val onError: (String) -> Unit = { error ->
            errorCalled = true
            receivedError = error
        }

        // When
        viewModel.deleteAccount(onSuccess, onError)
        advanceUntilIdle()

        // Then
        assertTrue(errorCalled)
        assertEquals(errorMessage, receivedError)
    }

    @Test
    fun `Given token clear fails When deleteAccount Then shows error message`() = runTest {
        // Given
        val errorMessage = "Delete failed"
        every { tokenProvider.clearToken() } throws Exception(errorMessage)
        val onSuccess = { }
        val onError: (String) -> Unit = { }

        // When
        viewModel.deleteAccount(onSuccess, onError)
        advanceUntilIdle()

        // Then
        assertEquals(errorMessage, viewModel.state.value.error)
        assertNull(viewModel.state.value.successMessage)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `When deleteAccount Then sets loading state initially`() = runTest {
        // Given
        every { tokenProvider.clearToken() } returns Unit
        val onSuccess = { }
        val onError: (String) -> Unit = { }

        // When
        viewModel.deleteAccount(onSuccess, onError)

        // Then - check immediate loading state
        assertTrue(viewModel.state.value.isLoading)
        assertNull(viewModel.state.value.error)
        assertNull(viewModel.state.value.successMessage)

        // Complete
        advanceUntilIdle()
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `When clearMessages Then clears error and success message`() = runTest {
        // Given - first set some messages
        every { tokenProvider.clearToken() } returns Unit
        viewModel.closeAllSessions({}, {})
        advanceUntilIdle()
        assertNotNull(viewModel.state.value.successMessage)

        // When
        viewModel.clearMessages()

        // Then
        assertNull(viewModel.state.value.error)
        assertNull(viewModel.state.value.successMessage)
    }

    @Test
    fun `Given multiple operations When clearMessages Then clears all messages`() = runTest {
        // Given - trigger error
        every { tokenProvider.clearToken() } throws Exception("Error")
        viewModel.deleteAccount({}, {})
        advanceUntilIdle()
        assertNotNull(viewModel.state.value.error)

        // When
        viewModel.clearMessages()

        // Then
        assertNull(viewModel.state.value.error)
        assertNull(viewModel.state.value.successMessage)
    }

    @Test
    fun `Given default 2FA state When viewModel created Then 2FA is disabled`() = runTest {
        // Then
        assertFalse(viewModel.state.value.is2FAEnabled)
    }

    @Test
    fun `Given closeAllSessions succeeds When called multiple times Then clears token each time`() = runTest {
        // Given
        every { tokenProvider.clearToken() } returns Unit
        val onSuccess = { }
        val onError: (String) -> Unit = { }

        // When
        viewModel.closeAllSessions(onSuccess, onError)
        advanceUntilIdle()
        viewModel.closeAllSessions(onSuccess, onError)
        advanceUntilIdle()

        // Then
        verify(exactly = 2) { tokenProvider.clearToken() }
    }

    @Test
    fun `Given generic exception When closeAllSessions Then shows generic error message`() = runTest {
        // Given
        every { tokenProvider.clearToken() } throws RuntimeException()
        var receivedError = ""
        val onSuccess = { }
        val onError: (String) -> Unit = { error -> receivedError = error }

        // When
        viewModel.closeAllSessions(onSuccess, onError)
        advanceUntilIdle()

        // Then
        assertEquals("Failed to close sessions", receivedError)
    }

    @Test
    fun `Given generic exception When deleteAccount Then shows generic error message`() = runTest {
        // Given
        every { tokenProvider.clearToken() } throws RuntimeException()
        var receivedError = ""
        val onSuccess = { }
        val onError: (String) -> Unit = { error -> receivedError = error }

        // When
        viewModel.deleteAccount(onSuccess, onError)
        advanceUntilIdle()

        // Then
        assertEquals("Failed to delete account", receivedError)
    }
}
