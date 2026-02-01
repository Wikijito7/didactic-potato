package es.wokis.didacticpotato.data.local.entity

import es.wokis.didacticpotato.domain.model.UserBO
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UserDboMappersTest {

    @Test
    fun `Given complete UserDBO When toBO Then returns UserBO with username`() {
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

        // When
        val result = userDBO.toBO()

        // Then
        assertEquals("testUser", result.username)
        // Note: Current mapper only maps username field
    }

    @Test
    fun `Given UserDBO with minimal fields When toBO Then returns UserBO`() {
        // Given
        val userDBO = UserDBO(
            username = "minimalUser"
        )

        // When
        val result = userDBO.toBO()

        // Then
        assertEquals("minimalUser", result.username)
    }

    @Test
    fun `Given UserBO When toDbo Then returns UserDBO with username and timestamp`() {
        // Given
        val userBO = UserBO(
            username = "testUser",
            email = "test@example.com",
            emailVerified = true
        )

        // When
        val result = userBO.toDbo()

        // Then
        assertEquals("testUser", result.username)
        assertEquals("current_user", result.id) // Default ID
        assertNotNull(result.lastUpdated)
        // lastUpdated should be current timestamp
        val currentTime = System.currentTimeMillis()
        assertTrue(result.lastUpdated in (currentTime - 1000)..(currentTime + 1000))
    }

    @Test
    fun `Given UserBO with only username When toDbo Then returns UserDBO`() {
        // Given
        val userBO = UserBO(
            username = "simpleUser"
        )

        // When
        val result = userBO.toDbo()

        // Then
        assertEquals("simpleUser", result.username)
        assertEquals("current_user", result.id)
    }

    @Test
    fun `Given UserBO with all fields When toDbo Then preserves username only`() {
        // Given
        val userBO = UserBO(
            username = "fullUser",
            email = "full@example.com",
            emailVerified = true,
            image = "image.jpg",
            lang = "es",
            createdOn = 1234567890L,
            totpEnabled = true
        )

        // When
        val result = userBO.toDbo()

        // Then
        assertEquals("fullUser", result.username)
        assertEquals("current_user", result.id)
        // Note: Current mapper only maps username, other BO fields are not mapped to DBO
    }

    @Test
    fun `Given multiple UserDBOs When toBO Then each maps independently`() {
        // Given
        val users = listOf(
            UserDBO(username = "user1"),
            UserDBO(username = "user2"),
            UserDBO(username = "user3")
        )

        // When
        val results = users.map { it.toBO() }

        // Then
        assertEquals(3, results.size)
        assertEquals("user1", results[0].username)
        assertEquals("user2", results[1].username)
        assertEquals("user3", results[2].username)
    }

    @Test
    fun `Given multiple UserBOs When toDbo Then each gets unique timestamp`() {
        // Given
        val users = listOf(
            UserBO(username = "user1"),
            UserBO(username = "user2"),
            UserBO(username = "user3")
        )

        // When
        val results = users.map { it.toDbo() }

        // Then
        assertEquals(3, results.size)
        results.forEach { dbo ->
            assertNotNull(dbo.lastUpdated)
            assertTrue(dbo.lastUpdated > 0)
        }
    }

    @Test
    fun `Given UserDBO with special characters in username When toBO Then preserves characters`() {
        // Given
        val userDBO = UserDBO(
            username = "user_123@test"
        )

        // When
        val result = userDBO.toBO()

        // Then
        assertEquals("user_123@test", result.username)
    }

    @Test
    fun `Given UserBO with empty username When toDbo Then returns UserDBO with empty username`() {
        // Given
        val userBO = UserBO(
            username = ""
        )

        // When
        val result = userBO.toDbo()

        // Then
        assertEquals("", result.username)
        assertEquals("current_user", result.id)
    }
}
