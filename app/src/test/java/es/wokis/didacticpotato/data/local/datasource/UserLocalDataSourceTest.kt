package es.wokis.didacticpotato.data.local.datasource

import es.wokis.didacticpotato.data.local.dao.UserDao
import es.wokis.didacticpotato.data.local.entity.UserDBO
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class UserLocalDataSourceTest {

    private val userDao: UserDao = mockk()
    private val userLocalDataSource = UserLocalDataSource(userDao)

    @Test
    fun `Given user exists in DB When getCurrentUser Then returns Flow with user`() = runTest {
        // Given
        val userDBO = UserDBO(
            username = "testUser",
            email = "test@example.com",
            lastUpdated = 1234567890L
        )

        every { userDao.getCurrentUser() } returns flowOf(userDBO)

        // When
        val result = userLocalDataSource.getCurrentUser()

        // Then
        result.collect { user ->
            assertNotNull(user)
            assertEquals("testUser", user?.username)
            assertEquals("test@example.com", user?.email)
        }
    }

    @Test
    fun `Given no user in DB When getCurrentUser Then returns Flow with null`() = runTest {
        // Given
        every { userDao.getCurrentUser() } returns flowOf(null)

        // When
        val result = userLocalDataSource.getCurrentUser()

        // Then
        result.collect { user ->
            assertNull(user)
        }
    }

    @Test
    fun `Given user exists When getCurrentUserSync Then returns user`() = runTest {
        // Given
        val userDBO = UserDBO(
            username = "testUser",
            email = "test@example.com",
            lastUpdated = 1234567890L
        )

        coEvery { userDao.getCurrentUserSync() } returns userDBO

        // When
        val result = userLocalDataSource.getCurrentUserSync()

        // Then
        assertNotNull(result)
        assertEquals("testUser", result?.username)
        coVerify(exactly = 1) { userDao.getCurrentUserSync() }
    }

    @Test
    fun `Given no user When getCurrentUserSync Then returns null`() = runTest {
        // Given
        coEvery { userDao.getCurrentUserSync() } returns null

        // When
        val result = userLocalDataSource.getCurrentUserSync()

        // Then
        assertNull(result)
        coVerify(exactly = 1) { userDao.getCurrentUserSync() }
    }

    @Test
    fun `Given valid user When saveUser Then inserts user to DAO`() = runTest {
        // Given
        val userDBO = UserDBO(
            username = "testUser",
            email = "test@example.com",
            lastUpdated = 1234567890L
        )

        coEvery { userDao.insertUser(any()) } returns Unit

        // When
        userLocalDataSource.saveUser(userDBO)

        // Then
        coVerify(exactly = 1) { userDao.insertUser(userDBO) }
    }

    @Test
    fun `Given user with null fields When saveUser Then inserts user with nulls`() = runTest {
        // Given
        val userDBO = UserDBO(
            username = "testUser",
            email = null,
            image = null,
            lang = null,
            createdOn = null,
            lastUpdated = 1234567890L
        )

        coEvery { userDao.insertUser(any()) } returns Unit

        // When
        userLocalDataSource.saveUser(userDBO)

        // Then
        coVerify(exactly = 1) {
            userDao.insertUser(
                match {
                    it.username == "testUser" && it.email == null
                }
            )
        }
    }

    @Test
    fun `When deleteAllUsers Then calls DAO delete`() = runTest {
        // Given
        coEvery { userDao.deleteAllUsers() } returns Unit

        // When
        userLocalDataSource.deleteAllUsers()

        // Then
        coVerify(exactly = 1) { userDao.deleteAllUsers() }
    }

    @Test
    fun `Given multiple saves When saveUser Then replaces existing user`() = runTest {
        // Given
        val user1 = UserDBO(username = "user1", lastUpdated = 1000L)
        val user2 = UserDBO(username = "user2", lastUpdated = 2000L)

        coEvery { userDao.insertUser(any()) } returns Unit

        // When
        userLocalDataSource.saveUser(user1)
        userLocalDataSource.saveUser(user2)

        // Then
        coVerify(exactly = 1) { userDao.insertUser(user1) }
        coVerify(exactly = 1) { userDao.insertUser(user2) }
    }

    @Test
    fun `Given user with all fields When saveUser Then preserves all data`() = runTest {
        // Given
        val userDBO = UserDBO(
            username = "testUser",
            email = "test@example.com",
            emailVerified = true,
            image = "profile.jpg",
            lang = "en",
            createdOn = 1234567890L,
            totpEnabled = true,
            lastUpdated = 9876543210L
        )

        coEvery { userDao.insertUser(any()) } returns Unit

        // When
        userLocalDataSource.saveUser(userDBO)

        // Then
        coVerify(exactly = 1) {
            userDao.insertUser(
                match {
                    it.username == "testUser" &&
                        it.email == "test@example.com" &&
                        it.emailVerified == true &&
                        it.image == "profile.jpg" &&
                        it.lang == "en" &&
                        it.createdOn == 1234567890L &&
                        it.totpEnabled == true &&
                        it.lastUpdated == 9876543210L
                }
            )
        }
    }
}
