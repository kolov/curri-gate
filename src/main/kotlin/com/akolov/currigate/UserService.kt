package com.akolov.currigate


interface UserService {
    fun createNew(): ThinUser
    fun register(currentUser: ThinUser, identity: Identity): ThinUser
    fun findThinUserByIdentity(sub: String): ThinUser?
    fun userDetails(id: String): ThickUser?
}