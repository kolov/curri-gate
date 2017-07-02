package com.akolov.currigate


interface UserService {
    fun userDetails(id: String): ThickUser?
    fun createNew(): ThinUser
    fun register(currentUserId: String, identity: Identity): ThinUser
    fun findByIdentity(sub: String): ThinUser?
}