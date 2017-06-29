package com.akolov.currigate

import org.springframework.stereotype.Component
import java.util.*

/**
 * Created by assen on 28/06/2017.
 */

@Component
class UserService {
    val usersByUserId: MutableMap<String, ThinUser> = HashMap()
    val usersByIdentity: MutableMap<String, ThinUser> = HashMap()
    val identities: MutableMap<String, Identity> = HashMap()
    val identitiesByUserId: MutableMap<String, Identity> = HashMap()

    fun findThinUserByIdentity(sub: String): ThinUser? {
        val identity = identities.get(sub)
        if (identity != null) {
            return usersByIdentity.get(identity.sub)
        }
        return null
    }

    fun findThickUserByUserId(id: String): ThickUser? {
        return ThickUser(id, identitiesByUserId.get(id))
    }


    fun register(identity: Identity): ThinUser {
        identities.put(identity.sub, identity)
        if (usersByIdentity.get(identity.sub) != null)
            throw Exception("Identity exists")
        var user = newUser()
        usersByIdentity.put(identity.sub, user)
        identitiesByUserId.put(user.id, identity)
        return user
    }

    fun create(): ThinUser {
        var user = newUser()
        usersByUserId.put(user.id, user)
        return user
    }

    private fun newUser(): ThinUser {
        return ThinUser(UUID.randomUUID().toString())
    }

}
