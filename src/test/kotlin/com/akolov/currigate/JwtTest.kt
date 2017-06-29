package com.akolov.currigate

import org.junit.Assert.assertEquals
import org.junit.Test


class JwtTest {

    @Test
    fun testCreateVerify() {
        val jwt = Jwt("secret")
        val token = jwt.create(ThinUser("123", false))
        val user = jwt.getUser(token)
        assertEquals(user.id, "123")
    }
}