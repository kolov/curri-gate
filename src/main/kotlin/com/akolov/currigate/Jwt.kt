package com.akolov.currigate

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm


class Jwt(val secret: String) {


    val issuer = "curri"
    val algorithm = Algorithm.HMAC256(secret)
    val verifier = JWT.require(algorithm)
            .withIssuer(issuer)
            .build()

    fun create(subject: String): String {
        val token = JWT.create()
                .withIssuer(issuer)
                .withClaim("sub", subject)
                .sign(algorithm)
        return token
    }

    fun getSubject(token: String): String {
        return verifier.verify(token).subject
    }
}
