package com.akolov.currigate

import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import javax.servlet.http.HttpServletResponse

class MainControllerTest {

    val google = Mockito.mock(AuthClient::class.java)
    val controller = LoginControler(google)

    @Before
    fun init() {

    }

    @Test
    fun simple() {
        val resp = mock(HttpServletResponse::class.java)
        val url = controller.login(resp)
        Mockito.verify(resp).sendRedirect(Mockito.anyString())
    }
}