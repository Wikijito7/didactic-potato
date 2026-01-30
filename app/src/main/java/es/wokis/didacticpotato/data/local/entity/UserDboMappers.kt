package es.wokis.didacticpotato.data.local.entity

import es.wokis.didacticpotato.domain.model.UserBO

fun UserDBO.toBO(): UserBO {
    return UserBO(
        username = username
    )
}

fun UserBO.toDbo(): UserDBO {
    return UserDBO(
        username = username,
        lastUpdated = System.currentTimeMillis() // Always update timestamp when saving
    )
}
