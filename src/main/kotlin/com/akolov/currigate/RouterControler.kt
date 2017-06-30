package com.akolov.currigate

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import javax.servlet.http.HttpServletRequest


@Controller
open class RouterControler(val userService: InMemoryUserService) {


    @RequestMapping("/service/{serviceName}/**")
    @ResponseBody
    open fun route(req: HttpServletRequest, @PathVariable serviceName: String): Any? {
        if (serviceName == "user") {
            val user = req.getAttribute(IdentityFilter.ATTR_USER) as ThinUser
            return userService.userDetails(user.id)
        }
        return "OK"
    }
}

