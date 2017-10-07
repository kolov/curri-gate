package com.akolov.currigate

import com.google.api.client.auth.oauth2.*
import com.google.api.client.http.BasicAuthentication
import com.google.api.client.http.GenericUrl
import com.google.api.client.http.HttpHeaders
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.GenericJson
import com.google.api.client.json.JsonObjectParser
import com.google.api.client.json.jackson.JacksonFactory
import com.google.api.client.util.IOUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


@Controller
open class LoginControler(val google: AuthClient,
                          val userService: UserService,
                          val identityFilter: IdentityFilter) {


    @RequestMapping(method = arrayOf(RequestMethod.POST), value = "/logout")
    open fun logout(req: HttpServletRequest, response: HttpServletResponse) {
        identityFilter.updateUserInRequest(req, userService.createNew())
    }

    @RequestMapping(method = arrayOf(RequestMethod.GET), value = "/login/google")
    open fun login(req: HttpServletRequest, response: HttpServletResponse) {


        val url = AuthorizationRequestUrl(google.userAuthorizationUri, google.clientId,
                Arrays.asList("code"))
                .setScopes(google.scope)
                .setRedirectUri(rebuildUrl(req, false)).build()
        response.sendRedirect(url)
    }


    @RequestMapping(method = arrayOf(RequestMethod.GET), value = "/login/google", params = arrayOf("code"))
    open fun loginCallback(req: HttpServletRequest): ResponseEntity<ThinUser> {


        val authResponse = AuthorizationCodeResponseUrl(rebuildUrl(req, true));
        if (authResponse.getError() != null) {
            return ResponseEntity(HttpStatus.UNAUTHORIZED);
        }
        try {
            val response = AuthorizationCodeTokenRequest(NetHttpTransport(), JacksonFactory(),
                    GenericUrl(google.accessTokenUri), authResponse.getCode())
                    .setRedirectUri(rebuildUrl(req, false))
                    .setClientAuthentication(
                            BasicAuthentication(google.clientId, google.clientSecret))
                    .execute();
            val credential = Credential(BearerToken.authorizationHeaderAccessMethod())
            credential.setFromTokenResponse(response)
            val userInfo = getUserInfo(credential.accessToken)
            val currentUser = req.getAttribute(IdentityFilter.ATTR_USER) as ThinUser
            val user = findOrCreateUser(currentUser, userInfo!!)
            identityFilter.updateUserInRequest(req, user)
            val headers = org.springframework.http.HttpHeaders()
            headers.add("Location", "/")
            return ResponseEntity(headers, HttpStatus.FOUND)
        } catch (e: TokenResponseException) {
            return ResponseEntity(HttpStatus.UNAUTHORIZED)
        }
    }


    private fun findOrCreateUser(currentUser: ThinUser, userInfo: GenericJson): ThinUser {
        val user = userService.findByIdentity(userInfo.get("sub") as String)
        if (user != null) {
            return user
        } else {

            val newIdentity = Identity(
                    sub = userInfo.get("sub") as String,
                    name = userInfo.get("name") as String,
                    givenName = userInfo.get("given_name") as String,
                    familyName = userInfo.get("family_name") as String,
                    profile = userInfo.get("profile") as String,
                    picture = userInfo.get("picture") as String,
                    email = userInfo.get("email") as String,
                    gender = userInfo.get("gender") as String,
                    locale = userInfo.get("locale") as String
            )
            return userService.register(currentUser.id, newIdentity)
        }
    }

    private fun getUserInfo(accessToken: String): GenericJson? {
        val jsonFactory = JacksonFactory()
        val requestFactory = NetHttpTransport().createRequestFactory()
        val request = requestFactory.buildGetRequest(GenericUrl(google.userInfoUri))
        request.setParser(JsonObjectParser(jsonFactory))
        request.setThrowExceptionOnExecuteError(false)
        val headers = HttpHeaders()
        headers.setAuthorization("Bearer " + accessToken)
        request.setHeaders(headers)
        val userInfoResponse = request.execute()
        if (userInfoResponse.isSuccessStatusCode()) {
            return userInfoResponse.parseAs(GenericJson::class.java)
        } else {
            return null
        }
    }

    // Application port as seen from outside (for callback url)
    @Value("\${APPPORT:80}")
    var applicationPort: Int = 80

    fun rebuildUrl(req: HttpServletRequest, withParams: Boolean): String {
        var result = if (req.isSecure) "https" else "http"
        result += "://" + req.serverName
        if (applicationPort != 80) {
            result += ":" + applicationPort
        }
        result += req.servletPath

        if (withParams) {
            if (req.queryString != null && req.queryString.length > 0) {
                result += "?" + req.queryString;
            }
        }
        return result
    }
}


@Controller
open class RouterControler(val userService: UserService) {


    @RequestMapping("/service/{serviceName}/**")
    @ResponseBody
    open fun route(req: HttpServletRequest, resp: HttpServletResponse, @PathVariable serviceName: String): Any? {


        if (serviceName == "user") {
            val user = req.getAttribute(IdentityFilter.ATTR_USER) as ThinUser
            return userService.userDetails(user.id)
        }

        val requestFactory = NetHttpTransport().createRequestFactory()
        val request = requestFactory.buildGetRequest(GenericUrl(serviceUrl(req)))
        request.throwExceptionOnExecuteError = false
        val headers = HttpHeaders()
        val user = req.getAttribute(IdentityFilter.ATTR_USER) as ThinUser
        headers.set("x-curri-user", user.id)
        headers.set("x-curri-app", "microdocs")
        request.headers = headers
        val otherResponse = request.execute()
        resp.status = otherResponse.statusCode
        IOUtils.copy(otherResponse.content, resp.outputStream)
        return null
    }

    fun serviceUrl(req: HttpServletRequest): String {

        val u = req.servletPath.substring("/service/".length)
        val r = Regex("^([a-zA-Z0-9\\-].*)")
        val found = r.find(u)
        val serviceName = found!!.groupValues[0]
        val mapped = System.getenv().get("SERVICE_" + serviceName.toUpperCase()) ?: serviceName


        var result = "http://" + mapped + u.substring(serviceName.length)
        if (req.queryString != null && req.queryString.length > 0) {
            result += "?" + req.queryString;
        }

        return result
    }
}

