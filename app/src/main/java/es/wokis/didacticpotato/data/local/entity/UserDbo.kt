package es.wokis.didacticpotato.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserDBO(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String = "current_user",

    @ColumnInfo(name = "username")
    val username: String,

    @ColumnInfo(name = "email")
    val email: String? = null,

    @ColumnInfo(name = "image")
    val image: String? = null,

    @ColumnInfo(name = "lang")
    val lang: String? = null,

    @ColumnInfo(name = "created_on")
    val createdOn: Long? = null,

    @ColumnInfo(name = "totp_enabled")
    val totpEnabled: Boolean = false,

    @ColumnInfo(name = "email_verified")
    val emailVerified: Boolean = false,

    @ColumnInfo(name = "last_updated")
    val lastUpdated: Long = System.currentTimeMillis()
)
