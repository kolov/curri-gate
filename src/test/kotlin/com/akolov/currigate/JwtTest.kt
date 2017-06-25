package com.akolov.currigate

import org.junit.Assert.assertEquals
import org.junit.Test


class JwtTest {

    @Test
    fun testCreate() {
        val jwt = Jwt("secret")
        val token = jwt.create("123")
        val subject = jwt.getSubject(token)
        assertEquals(subject, "123")
    }
}