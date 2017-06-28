package com.akolov.currigate

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import javax.servlet.http.HttpServletRequest


@Controller
open class RouterControler( ) {


    @RequestMapping("/service/{serviceName}/**")
    @ResponseBody
    open fun route(@PathVariable  serviceName: String): String {
        return serviceName
    }


    private fun rebuildUrl(req: HttpServletRequest): String {
        val fullUrlBuf = req.getRequestURL();
        val queryString = req.getQueryString()
        if (queryString != null && queryString.length > 0 ) {
            fullUrlBuf.append('?').append(queryString);
        }
        return fullUrlBuf.toString()
    }


}

