package com.akolov.currigate

import feign.FeignException
import org.springframework.cloud.netflix.feign.FeignClient
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.*


@FeignClient("service-users")
interface UserServiceClient {
    @RequestMapping(method = arrayOf(RequestMethod.POST), value = "/user/create")
    fun createNew(@RequestHeader("X-CURRI-ROLES") roles: String): ThinUser

    @RequestMapping(method = arrayOf(RequestMethod.PUT),
            value = "/user/{currentUserId}/identity", consumes = arrayOf("application/json"))
    @ResponseBody
    fun register(@RequestHeader("X-CURRI-ROLES") roles: String,
                 @PathVariable("currentUserId") currentUserId:
                 String, @RequestBody identity: Identity): ThinUser

    @RequestMapping(method = arrayOf(RequestMethod.GET), value = "/identity/{sub}")
    fun findByIdentity(@RequestHeader("X-CURRI-ROLES") roles: String,
                       @PathVariable("sub") sub: String):
            ThinUser?

    @RequestMapping(method = arrayOf(RequestMethod.GET), value = "/user/details/{id}")
    fun userDetails(@RequestHeader("X-CURRI-ROLES") roles: String,
                    @PathVariable("id") id: String): ThickUser?

}


@Component
class UserServiceDelegate(val svc: UserServiceClient) : UserService {
    val roles = "internal"

    override fun userDetails(id: String): ThickUser? {
        return svc.userDetails(roles, id)
    }

    override fun createNew(): ThinUser {
        return svc.createNew(roles)
    }

    override fun register(currentUserId: String, identity: Identity): ThinUser {
        return svc.register(roles, currentUserId, identity)
    }

    override fun findByIdentity(sub: String): ThinUser? {
        try {
            return  svc.findByIdentity(roles, sub)
        } catch(e: Exception) {
            if( e is FeignException && e.status() == 404) {
                return null
            }
            throw e
        }
    }

}
/*
class InMemoryUserService : UserService {
    val usersByUserId: MutableMap<String, ThinUser> = HashMap()
    val usersByIdentity: MutableMap<String, ThinUser> = HashMap()
    val identities: MutableMap<String, Identity> = HashMap()
    val identitiesByUserId: MutableMap<String, Identity> = HashMap()

    override fun findByIdentity(sub: String): ThinUser? {
        val identity = identities.get(sub)
        if (identity != null) {
            return usersByIdentity.get(identity.sub)
        }
        return null
    }

    override fun userDetails(id: String): ThickUser? {
        return ThickUser(id, identitiesByUserId.get(id))
    }


    override fun register(currentUserId: String, identity: Identity): ThinUser {
        val existingIdentity = identitiesByUserId.get(currentUserId)
        val user = if (existingIdentity == null) ThinUser(currentUserId) else newUser()

        identities.put(identity.sub, identity)
        if (usersByIdentity.get(identity.sub) != null)
            throw Exception("Identity exists")

        usersByIdentity.put(identity.sub, user)
        identitiesByUserId.put(currentUserId, identity)
        return user
    }


    override fun createNew(): ThinUser {
        var user = newUser()
        usersByUserId.put(user.id, user)
        return user
    }

    private fun newUser(): ThinUser {
        return ThinUser(UUID.randomUUID().toString())
    }

}
*/
