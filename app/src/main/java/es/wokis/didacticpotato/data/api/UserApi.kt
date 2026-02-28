package es.wokis.didacticpotato.data.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType

class UserApi(private val client: HttpClient) {

    suspend fun getUser(
        totpCode: String? = null,
        timestamp: Long? = null
    ): UserDTO {
        val response = client.get("$BASE_URL/user") {
            if (totpCode != null && timestamp != null) {
                headers {
                    append(TwoFactorAuthChallenge.CODE_HEADER, totpCode)
                    append(TwoFactorAuthChallenge.TIMESTAMP_HEADER, timestamp.toString())
                }
            }
        }
        return response.body()
    }

    suspend fun updateUser(
        userDTO: UserDTO,
        totpCode: String? = null,
        timestamp: Long? = null
    ): HttpResponse {
        return client.put("$BASE_URL/user") {
            contentType(ContentType.Application.Json)
            setBody(userDTO)
            if (totpCode != null && timestamp != null) {
                headers {
                    append(TwoFactorAuthChallenge.CODE_HEADER, totpCode)
                    append(TwoFactorAuthChallenge.TIMESTAMP_HEADER, timestamp.toString())
                }
            }
        }
    }

    suspend fun uploadImage(
        imageData: ByteArray,
        fileName: String = "image.jpg",
        totpCode: String? = null,
        timestamp: Long? = null
    ): HttpResponse {
        return client.post("$BASE_URL/user/image") {
            setBody(MultiPartFormDataContent(
                formData {
                    append("image", imageData, Headers.build {
                        append(HttpHeaders.ContentType, "image/jpeg")
                        append(HttpHeaders.ContentDisposition, "filename=$fileName")
                    })
                },
                boundary = "WebAppBoundary"
            ))
            if (totpCode != null && timestamp != null) {
                headers {
                    append(TwoFactorAuthChallenge.CODE_HEADER, totpCode)
                    append(TwoFactorAuthChallenge.TIMESTAMP_HEADER, timestamp.toString())
                }
            }
        }
    }

    suspend fun deleteImage(
        totpCode: String? = null,
        timestamp: Long? = null
    ): HttpResponse {
        return client.delete("$BASE_URL/user/image") {
            if (totpCode != null && timestamp != null) {
                headers {
                    append(TwoFactorAuthChallenge.CODE_HEADER, totpCode)
                    append(TwoFactorAuthChallenge.TIMESTAMP_HEADER, timestamp.toString())
                }
            }
        }
    }

    suspend fun deleteAccount(
        totpCode: String? = null,
        timestamp: Long? = null
    ): HttpResponse {
        return client.delete("$BASE_URL/user") {
            if (totpCode != null && timestamp != null) {
                headers {
                    append(TwoFactorAuthChallenge.CODE_HEADER, totpCode)
                    append(TwoFactorAuthChallenge.TIMESTAMP_HEADER, timestamp.toString())
                }
            }
        }
    }

    /**
     * Resends the verification email.
     * POST /verify
     */
    suspend fun resendVerificationEmail(): HttpResponse {
        return client.post("$BASE_URL/verify")
    }
}
