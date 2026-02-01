package es.wokis.didacticpotato.data.api

import es.wokis.didacticpotato.domain.model.UserBO

fun UserDTO.toBO(): UserBO {
    return UserBO(
        username = username.orEmpty(),
        email = email,
        emailVerified = emailVerified ?: false,
        image = image,
        lang = lang,
        createdOn = createdOn,
        totpEnabled = totpEnabled ?: false
    )
}
