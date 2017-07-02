package com.akolov.currigate

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.cloud.netflix.feign.EnableFeignClients
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
    SpringApplication.run(CurriGateApplication::class.java)
}

@SpringBootApplication
@EnableFeignClients
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
class IdentityFilter(@Autowired val jwt: Jwt, @Autowired val userService: UserServiceDelegate) : GenericFilterBean() {


    val log = LoggerFactory.getLogger(IdentityFilter.javaClass)

    companion object {
        val ATTR_USER: String = "curri-user"
    }

    val ATTR_COOKIE: String = "curri-cookie"
    val COOKIE_NAME: String = "curri"

    fun updateUserInRequest(request: HttpServletRequest, user: ThinUser) {
        log.debug("assigning new user ${user.id}")
        request.setAttribute(ATTR_USER, user)
        request.setAttribute(ATTR_COOKIE, jwt.create(user))
    }


    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {

        val httpRequest: HttpServletRequest = request as HttpServletRequest
        val httpResponse: HttpServletResponse = response as HttpServletResponse


        val cookie: Cookie? = httpRequest.cookies?.find { c -> c.name == COOKIE_NAME }
        log.debug("processing request ${httpRequest.servletPath}")
        if (cookie == null) {
            log.debug("no cookie in request")
            val user = userService.createNew()
            updateUserInRequest(request, user)
        } else {
            val user = jwt.getUser(cookie.value)
            httpRequest.setAttribute(ATTR_USER, user)
            httpRequest.setAttribute(ATTR_COOKIE, cookie.value)
            log.debug("cookie found in request: user ${user.id}")
        }

        chain.doFilter(request, response)
        val newCookieValue = request.getAttribute(ATTR_COOKIE) as String
        if (cookie == null || cookie.value != newCookieValue) {
            log.debug("setting cookie in response ")
            val newCookie = Cookie(COOKIE_NAME, newCookieValue)
            newCookie.maxAge = 30 * 24 * 60 * 60
            newCookie.path = "/"
            httpResponse.addCookie(newCookie as Cookie?)
        }
    }
}
