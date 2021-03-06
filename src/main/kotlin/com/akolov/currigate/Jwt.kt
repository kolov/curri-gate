package com.akolov.currigate

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm


class Jwt(val secret: String) {


    val issuer = "curri"
    val algorithm = Algorithm.HMAC256(secret)
    val verifier = JWT.require(algorithm)
            .withIssuer(issuer)
            .build()

    fun create(user: ThinUser): String {
        val token = JWT.create()
                .withIssuer(issuer)
                .withClaim("sub", user.id)
                .sign(algorithm)
        return token
    }

    fun getUser(token: String): ThinUser {
        val verified = verifier.verify(token)
        return ThinUser(verified.subject)
    }
}
