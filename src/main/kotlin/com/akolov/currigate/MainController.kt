package com.akolov.currigate

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import java.security.Principal
import javax.servlet.http.HttpServletRequest

/**
 * Created by assen on 19/06/2017.
 */

@Controller
open class MailControoler {

    @RequestMapping("/")
    @ResponseBody
    open fun handleRootRequest(principal: Principal?): String {
        return principal.toString()
    }

}
