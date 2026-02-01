package es.wokis.didacticpotato.ui.profile.edit

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import es.wokis.didacticpotato.data.api.TwoFactorAuthManager
import es.wokis.didacticpotato.data.repository.UserRepository
import es.wokis.didacticpotato.domain.model.UserBO
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
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
class EditProfileViewModelTest {

    private val context: Context = mockk()
    private val userRepository: UserRepository = mockk()
    private val twoFactorAuthManager: TwoFactorAuthManager = mockk()
    private lateinit var viewModel: EditProfileViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Default mocks for initialization
        coEvery { userRepository.getUser() } returns Result.success(UserBO(username = "TestUser"))

        viewModel = EditProfileViewModel(
            context = context,
            userRepository = userRepository,
            twoFactorAuthManager = twoFactorAuthManager
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
        assertEquals("", state.username)
        assertEquals("", state.email)
        assertEquals("", state.currentPassword)
        assertEquals("", state.newPassword)
        assertEquals("", state.confirmPassword)
        assertNull(state.imageUrl)
        assertFalse(state.isLoading)
        assertFalse(state.isSuccess)
        assertNull(state.error)
        assertNull(state.usernameError)
        assertNull(state.emailError)
        assertNull(state.passwordError)
        assertNull(state.confirmPasswordError)
    }

    @Test
    fun `Given user exists When viewModel created Then loads user data`() = runTest {
        // Given
        val user = UserBO(username = "LoadedUser", email = "test@example.com", image = "http://image.jpg")
        coEvery { userRepository.getUser() } returns Result.success(user)

        // When - create new viewModel
        val newViewModel = EditProfileViewModel(
            context = context,
            userRepository = userRepository,
            twoFactorAuthManager = twoFactorAuthManager
        )
        advanceUntilIdle()

        // Then
        assertEquals("LoadedUser", newViewModel.state.value.username)
    }

    @Test
    fun `Given load user fails When viewModel created Then handles gracefully`() = runTest {
        // Given
        coEvery { userRepository.getUser() } returns Result.failure(Exception("Load failed"))

        // When
        val newViewModel = EditProfileViewModel(
            context = context,
            userRepository = userRepository,
            twoFactorAuthManager = twoFactorAuthManager
        )
        advanceUntilIdle()

        // Then - state should have empty values
        assertEquals("", newViewModel.state.value.username)
    }

    @Test
    fun `When onUsernameChange Then state updates and clears errors`() = runTest {
        // Given - first set an error
        viewModel.onUsernameChange("")
        viewModel.saveProfile()
        advanceUntilIdle()
        assertNotNull(viewModel.state.value.usernameError)

        // When
        viewModel.onUsernameChange("NewUsername")
        advanceUntilIdle()

        // Then
        assertEquals("NewUsername", viewModel.state.value.username)
        assertNull(viewModel.state.value.usernameError)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `When onEmailChange Then state updates and clears errors`() = runTest {
        // Given - set invalid email
        viewModel.onEmailChange("invalid")
        viewModel.saveProfile()
        advanceUntilIdle()
        assertNotNull(viewModel.state.value.emailError)

        // When
        viewModel.onEmailChange("valid@example.com")
        advanceUntilIdle()

        // Then
        assertEquals("valid@example.com", viewModel.state.value.email)
        assertNull(viewModel.state.value.emailError)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `When onCurrentPasswordChange Then state updates and clears errors`() = runTest {
        // When
        viewModel.onCurrentPasswordChange("current123")
        advanceUntilIdle()

        // Then
        assertEquals("current123", viewModel.state.value.currentPassword)
        assertNull(viewModel.state.value.passwordError)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `When onNewPasswordChange with short password Then shows validation error`() = runTest {
        // When
        viewModel.onNewPasswordChange("short")
        advanceUntilIdle()

        // Then
        assertEquals("short", viewModel.state.value.newPassword)
        assertEquals("Password must be at least 8 characters", viewModel.state.value.passwordError)
    }

    @Test
    fun `When onNewPasswordChange with valid password Then no validation error`() = runTest {
        // When
        viewModel.onNewPasswordChange("validpassword123")
        advanceUntilIdle()

        // Then
        assertEquals("validpassword123", viewModel.state.value.newPassword)
        assertNull(viewModel.state.value.passwordError)
    }

    @Test
    fun `When onNewPasswordChange with empty password Then no validation error`() = runTest {
        // When
        viewModel.onNewPasswordChange("")
        advanceUntilIdle()

        // Then
        assertEquals("", viewModel.state.value.newPassword)
        assertNull(viewModel.state.value.passwordError)
    }

    @Test
    fun `When onConfirmPasswordChange with mismatch Then shows validation error`() = runTest {
        // Given
        viewModel.onNewPasswordChange("password123")
        advanceUntilIdle()

        // When
        viewModel.onConfirmPasswordChange("different123")
        advanceUntilIdle()

        // Then
        assertEquals("different123", viewModel.state.value.confirmPassword)
        assertEquals("Passwords do not match", viewModel.state.value.confirmPasswordError)
    }

    @Test
    fun `When onConfirmPasswordChange with match Then no validation error`() = runTest {
        // Given
        viewModel.onNewPasswordChange("password123")
        advanceUntilIdle()

        // When
        viewModel.onConfirmPasswordChange("password123")
        advanceUntilIdle()

        // Then
        assertEquals("password123", viewModel.state.value.confirmPassword)
        assertNull(viewModel.state.value.confirmPasswordError)
    }

    @Test
    fun `When onConfirmPasswordChange with empty Then no validation error`() = runTest {
        // When
        viewModel.onConfirmPasswordChange("")
        advanceUntilIdle()

        // Then
        assertEquals("", viewModel.state.value.confirmPassword)
        assertNull(viewModel.state.value.confirmPasswordError)
    }

    @Test
    fun `Given valid inputs When saveProfile Then updates user and shows success`() = runTest {
        // Given
        val updatedUser = UserBO(username = "UpdatedUser")
        viewModel.onUsernameChange("UpdatedUser")
        viewModel.onEmailChange("test@example.com")
        advanceUntilIdle()

        coEvery { userRepository.updateUser(any(), any()) } returns Result.success(updatedUser)

        // When
        viewModel.saveProfile()
        advanceUntilIdle()

        // Then
        coVerify { userRepository.updateUser("UpdatedUser", "test@example.com") }
        assertTrue(viewModel.state.value.isSuccess)
        assertFalse(viewModel.state.value.isLoading)
        assertNull(viewModel.state.value.error)
        assertEquals("UpdatedUser", viewModel.state.value.username)
    }

    @Test
    fun `Given empty username When saveProfile Then shows validation error`() = runTest {
        // Given
        viewModel.onUsernameChange("")
        viewModel.onEmailChange("test@example.com")
        advanceUntilIdle()

        // When
        viewModel.saveProfile()
        advanceUntilIdle()

        // Then
        assertEquals("Username is required", viewModel.state.value.usernameError)
        assertFalse(viewModel.state.value.isSuccess)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `Given invalid email When saveProfile Then shows validation error`() = runTest {
        // Given
        viewModel.onUsernameChange("TestUser")
        viewModel.onEmailChange("invalid-email")
        advanceUntilIdle()

        // When
        viewModel.saveProfile()
        advanceUntilIdle()

        // Then
        assertEquals("Invalid email format", viewModel.state.value.emailError)
        assertFalse(viewModel.state.value.isSuccess)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `Given update fails When saveProfile Then shows error`() = runTest {
        // Given
        val errorMessage = "Update failed"
        viewModel.onUsernameChange("TestUser")
        viewModel.onEmailChange("test@example.com")
        advanceUntilIdle()

        coEvery { userRepository.updateUser(any(), any()) } returns Result.failure(Exception(errorMessage))

        // When
        viewModel.saveProfile()
        advanceUntilIdle()

        // Then
        assertEquals(errorMessage, viewModel.state.value.error)
        assertFalse(viewModel.state.value.isSuccess)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `When saveProfile Then sets loading state initially`() = runTest {
        // Given
        viewModel.onUsernameChange("TestUser")
        advanceUntilIdle()
        coEvery { userRepository.updateUser(any(), any()) } returns Result.success(UserBO(username = "TestUser"))

        // When
        viewModel.saveProfile()

        // Then - check immediate loading state
        assertTrue(viewModel.state.value.isLoading)
        assertNull(viewModel.state.value.error)

        // Complete
        advanceUntilIdle()
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `Given multiple validation errors When saveProfile Then shows all errors`() = runTest {
        // Given
        viewModel.onUsernameChange("")
        viewModel.onEmailChange("invalid")
        advanceUntilIdle()

        // When
        viewModel.saveProfile()
        advanceUntilIdle()

        // Then
        assertNotNull(viewModel.state.value.usernameError)
        assertNotNull(viewModel.state.value.emailError)
    }

    @Test
    fun `Given image upload succeeds When onImageSelected Then updates imageUrl`() = runTest {
        // Given
        val imageUri: Uri = mockk()
        val contentResolver: ContentResolver = mockk()
        val imageBytes = byteArrayOf(1, 2, 3, 4)

        every { context.contentResolver } returns contentResolver
        every { contentResolver.openInputStream(imageUri) } returns java.io.ByteArrayInputStream(imageBytes)
        coEvery { userRepository.uploadImage(any()) } returns Result.success("http://new-image.jpg")

        // When
        viewModel.onImageSelected(imageUri)
        advanceUntilIdle()

        // Then
        assertEquals("http://new-image.jpg", viewModel.state.value.imageUrl)
        assertTrue(viewModel.state.value.isSuccess)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `Given image read fails When onImageSelected Then shows error`() = runTest {
        // Given
        val imageUri: Uri = mockk()
        val contentResolver: ContentResolver = mockk()

        every { context.contentResolver } returns contentResolver
        every { contentResolver.openInputStream(imageUri) } returns null

        // When
        viewModel.onImageSelected(imageUri)
        advanceUntilIdle()

        // Then
        assertNotNull(viewModel.state.value.error)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `Given image upload fails When onImageSelected Then shows error`() = runTest {
        // Given
        val imageUri: Uri = mockk()
        val contentResolver: ContentResolver = mockk()
        val imageBytes = byteArrayOf(1, 2, 3, 4)
        val errorMessage = "Upload failed"

        every { context.contentResolver } returns contentResolver
        every { contentResolver.openInputStream(imageUri) } returns java.io.ByteArrayInputStream(imageBytes)
        coEvery { userRepository.uploadImage(any()) } returns Result.failure(Exception(errorMessage))

        // When
        viewModel.onImageSelected(imageUri)
        advanceUntilIdle()

        // Then
        assertEquals(errorMessage, viewModel.state.value.error)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `When onImageSelected Then sets loading state initially`() = runTest {
        // Given
        val imageUri: Uri = mockk()
        val contentResolver: ContentResolver = mockk()
        val imageBytes = byteArrayOf(1, 2, 3, 4)

        every { context.contentResolver } returns contentResolver
        every { contentResolver.openInputStream(imageUri) } returns java.io.ByteArrayInputStream(imageBytes)
        coEvery { userRepository.uploadImage(any()) } returns Result.success("http://image.jpg")

        // When
        viewModel.onImageSelected(imageUri)

        // Then - check immediate loading state
        assertTrue(viewModel.state.value.isLoading)
        assertNull(viewModel.state.value.error)

        // Complete
        advanceUntilIdle()
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `When submitTwoFactorCode Then delegates to manager`() = runTest {
        // Given
        every { twoFactorAuthManager.submitTwoFactorCode(any()) } returns Unit

        // When
        viewModel.submitTwoFactorCode("123456")

        // Then
        coVerify { twoFactorAuthManager.submitTwoFactorCode("123456") }
    }

    @Test
    fun `When cancelTwoFactorChallenge Then cancels and clears loading`() = runTest {
        // Given
        every { twoFactorAuthManager.cancelTwoFactorChallenge() } returns Unit

        // When
        viewModel.cancelTwoFactorChallenge()

        // Then
        coVerify { twoFactorAuthManager.cancelTwoFactorChallenge() }
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `Given passwords match and current provided When changePassword Then shows success`() = runTest {
        // Given
        viewModel.onCurrentPasswordChange("current123")
        viewModel.onNewPasswordChange("newpassword123")
        viewModel.onConfirmPasswordChange("newpassword123")
        advanceUntilIdle()

        // When
        viewModel.changePassword()
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.state.value.isSuccess)
        assertFalse(viewModel.state.value.isLoading)
        assertEquals("", viewModel.state.value.currentPassword)
        assertEquals("", viewModel.state.value.newPassword)
        assertEquals("", viewModel.state.value.confirmPassword)
    }

    @Test
    fun `Given passwords mismatch When changePassword Then shows error`() = runTest {
        // Given
        viewModel.onNewPasswordChange("password1")
        viewModel.onConfirmPasswordChange("password2")
        advanceUntilIdle()

        // When
        viewModel.changePassword()
        advanceUntilIdle()

        // Then
        assertEquals("Passwords do not match", viewModel.state.value.confirmPasswordError)
        assertFalse(viewModel.state.value.isSuccess)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `Given empty current password When changePassword Then shows error`() = runTest {
        // Given
        viewModel.onCurrentPasswordChange("")
        viewModel.onNewPasswordChange("newpassword123")
        viewModel.onConfirmPasswordChange("newpassword123")
        advanceUntilIdle()

        // When
        viewModel.changePassword()
        advanceUntilIdle()

        // Then
        assertEquals("Current password is required", viewModel.state.value.passwordError)
        assertFalse(viewModel.state.value.isSuccess)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `When changePassword Then sets loading state initially`() = runTest {
        // Given
        viewModel.onCurrentPasswordChange("current123")
        viewModel.onNewPasswordChange("newpassword123")
        viewModel.onConfirmPasswordChange("newpassword123")
        advanceUntilIdle()

        // When
        viewModel.changePassword()

        // Then - check immediate loading state
        assertTrue(viewModel.state.value.isLoading)
        assertNull(viewModel.state.value.error)

        // Complete
        advanceUntilIdle()
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `Given valid email with empty When saveProfile Then no email error`() = runTest {
        // Given
        viewModel.onUsernameChange("TestUser")
        viewModel.onEmailChange("") // Empty email is valid (not required)
        advanceUntilIdle()

        coEvery { userRepository.updateUser(any(), any()) } returns Result.success(UserBO(username = "TestUser"))

        // When
        viewModel.saveProfile()
        advanceUntilIdle()

        // Then
        assertNull(viewModel.state.value.emailError)
        assertTrue(viewModel.state.value.isSuccess)
    }
}
