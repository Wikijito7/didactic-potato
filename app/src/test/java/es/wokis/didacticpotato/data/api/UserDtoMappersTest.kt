package es.wokis.didacticpotato.data.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UserDtoMappersTest {

    @Test
    fun `Given complete UserDTO When toBO Then returns UserBO with all fields`() {
        // Given
        val userDTO = UserDTO(
            username = "testUser",
            email = "test@example.com",
            emailVerified = true,
            image = "profile.jpg",
            lang = "en",
            createdOn = 1234567890L,
            totpEnabled = true
        )

        // When
        val result = userDTO.toBO()

        // Then
        assertEquals("testUser", result.username)
        assertEquals("test@example.com", result.email)
        assertTrue(result.emailVerified)
        assertEquals("profile.jpg", result.image)
        assertEquals("en", result.lang)
        assertEquals(1234567890L, result.createdOn)
        assertTrue(result.totpEnabled)
    }

    @Test
    fun `Given UserDTO with null email When toBO Then returns UserBO with null email`() {
        // Given
        val userDTO = UserDTO(
            username = "testUser",
            email = null,
            emailVerified = false
        )

        // When
        val result = userDTO.toBO()

        // Then
        assertEquals("testUser", result.username)
        assertNull(result.email)
        assertFalse(result.emailVerified)
    }

    @Test
    fun `Given UserDTO with null username When toBO Then returns UserBO with empty string`() {
        // Given
        val userDTO = UserDTO(
            username = null,
            email = "test@example.com"
        )

        // When
        val result = userDTO.toBO()

        // Then
        assertEquals("", result.username)
        assertEquals("test@example.com", result.email)
    }

    @Test
    fun `Given UserDTO with null optional fields When toBO Then returns UserBO with defaults`() {
        // Given
        val userDTO = UserDTO(
            username = "testUser",
            email = null,
            emailVerified = null,
            image = null,
            lang = null,
            createdOn = null,
            totpEnabled = null
        )

        // When
        val result = userDTO.toBO()

        // Then
        assertEquals("testUser", result.username)
        assertNull(result.email)
        assertFalse(result.emailVerified) // Default value
        assertNull(result.image)
        assertNull(result.lang)
        assertNull(result.createdOn)
        assertFalse(result.totpEnabled) // Default value
    }

    @Test
    fun `Given UserDTO with all nulls When toBO Then returns UserBO with username as empty string`() {
        // Given
        val userDTO = UserDTO(
            username = null,
            email = null,
            emailVerified = null,
            image = null,
            lang = null,
            createdOn = null,
            totpEnabled = null
        )

        // When
        val result = userDTO.toBO()

        // Then
        assertEquals("", result.username)
        assertNull(result.email)
        assertFalse(result.emailVerified)
        assertNull(result.image)
        assertNull(result.lang)
        assertNull(result.createdOn)
        assertFalse(result.totpEnabled)
    }
}
