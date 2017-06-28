package com.akolov.currigate

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import org.springframework.web.filter.GenericFilterBean
import java.util.*
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


fun main(args: Array<String>) {
    SpringApplication.run(CurriGateApplication::class.java, "--debug")
}

@SpringBootApplication
open class CurriGateApplication() {


    @Bean
    open fun jwt(): Jwt {
        return Jwt("secret key here")
    }

    @Bean
    open fun registerIdentityFilter(): FilterRegistrationBean {
        val registration = FilterRegistrationBean()
        registration.filter = identityFilter
        registration.urlPatterns = Arrays.asList("/*")
        return registration
    }

    @Autowired
    var identityFilter: IdentityFilter? = null
}

@Component

class IdentityFilter(@Autowired val jwt: Jwt, @Autowired val userService: UserService) : GenericFilterBean() {


    companion object {

        val ATTR_USER: String = "curri-user"
    }

    val ATTR_COOKIE: String = "curri-cookie"
    val COOKIE_NAME: String = "curri"

    fun userChanged(request: HttpServletRequest, user: ThinUser) {
        request.setAttribute(ATTR_USER, user)
        val cookie = Cookie(COOKIE_NAME, jwt.create(user))
        cookie.maxAge = 30 * 24 * 60 * 60
        cookie.path = "/"
        request.setAttribute(ATTR_COOKIE, cookie)
    }


    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {

        val httpRequest: HttpServletRequest = request as HttpServletRequest
        val httpResponse: HttpServletResponse = response as HttpServletResponse

        val url = httpRequest.requestURL.toString()
        if (!url.startsWith("/login")) {
            val cookie: Cookie? = httpRequest.cookies?.find { c -> c.name == COOKIE_NAME }

            if (cookie == null) {
                userChanged(request, userService.create())
            } else {
                val user = jwt.getUser(cookie.value)
                httpRequest.setAttribute(ATTR_USER, user)
                httpRequest.setAttribute(ATTR_COOKIE, cookie)
            }
        }
        chain.doFilter(request, response)
        httpResponse.addCookie(httpRequest.getAttribute(ATTR_COOKIE) as Cookie?)
    }
}
