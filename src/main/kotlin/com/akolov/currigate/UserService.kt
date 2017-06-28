package com.akolov.currigate

import org.springframework.stereotype.Component
import java.util.*

/**
 * Created by assen on 28/06/2017.
 */

@Component
class UserService {
    val usersByUserId: MutableMap<String, User> = HashMap()
    val usersByIdentity: MutableMap<String, User> = HashMap()
    val identities: MutableMap<String, Identity> = HashMap()

    fun findByIdentity(sub: String): User? {
        val id = identities.get(sub)
        if (id != null) {
            return usersByIdentity.get(id.sub)
        }
        return null
    }

    fun register(identity: Identity) : User {
        identities.put(identity.sub, identity)
        if( usersByIdentity.get(identity.sub) != null)
            throw Exception("Identity exists")
        var user = newUser()
        usersByIdentity.put(identity.sub, user)
        return user
    }

    fun create() : User {
        var user = newUser()
        usersByUserId.put(user.id, user)
        return user
    }

    private fun newUser() : User {
        return User(UUID.randomUUID().toString())
    }
}

data class Identity(val sub: String,
                    val name: String,
                    val givenName: String,
                    val familyName: String,
                    val profile: String,
                    val picture: String,
                    val email: String,
                    val gender: String,
                    val locale: String)