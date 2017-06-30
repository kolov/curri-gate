package com.akolov.currigate

import org.springframework.stereotype.Component
import java.util.*



@Component
class InMemoryUserService : UserService {
    val usersByUserId: MutableMap<String, ThinUser> = HashMap()
    val usersByIdentity: MutableMap<String, ThinUser> = HashMap()
    val identities: MutableMap<String, Identity> = HashMap()
    val identitiesByUserId: MutableMap<String, Identity> = HashMap()

    override fun findThinUserByIdentity(sub: String): ThinUser? {
        val identity = identities.get(sub)
        if (identity != null) {
            return usersByIdentity.get(identity.sub)
        }
        return null
    }

    override fun userDetails(id: String): ThickUser? {
        return ThickUser(id, identitiesByUserId.get(id))
    }



    override fun register(currentUser: ThinUser, identity: Identity): ThinUser {
        val existingIdentity = identitiesByUserId.get(currentUser.id)
        val user = if( existingIdentity == null) currentUser else newUser()

        identities.put(identity.sub, identity)
        if (usersByIdentity.get(identity.sub) != null)
            throw Exception("Identity exists")

        usersByIdentity.put(identity.sub, user)
        identitiesByUserId.put(currentUser.id, identity)
        return user
    }


    override fun createNew(): ThinUser {
        var user = newUser()
        usersByUserId.put(user.id, user)
        return user
    }

    private fun newUser(): ThinUser {
        return ThinUser(UUID.randomUUID().toString() )
    }

}

