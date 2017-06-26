package com.akolov.currigate

import com.google.api.client.auth.oauth2.*
import com.google.api.client.http.BasicAuthentication
import com.google.api.client.http.GenericUrl
import com.google.api.client.http.HttpHeaders
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.GenericJson
import com.google.api.client.json.JsonObjectParser
import com.google.api.client.json.jackson.JacksonFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody
import java.security.Principal
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


@Controller
open class LoginControler(val google: AuthClient) {


    @RequestMapping("/")
    @ResponseBody
    open fun handleRootRequest(principal: Principal?): String {
        return principal.toString()
    }

    @RequestMapping(method = arrayOf(RequestMethod.GET), value = "/login/google")
    open fun login(req: HttpServletRequest, response: HttpServletResponse) {
        val url = AuthorizationRequestUrl(google.userAuthorizationUri, google.clientId,
                Arrays.asList("code"))
                .setScopes(google.scope)
                .setRedirectUri(rebuildUrl(req)).build()
        response.sendRedirect(url)
    }

    @RequestMapping(method = arrayOf(RequestMethod.GET), value = "/login/google", params = arrayOf("code"))
    open fun loginCallback(req: HttpServletRequest): ResponseEntity<String> {

        val authResponse = AuthorizationCodeResponseUrl(rebuildUrl(req));
        if (authResponse.getError() != null) {
            return ResponseEntity(HttpStatus.UNAUTHORIZED);
        }
        try {
            val response = AuthorizationCodeTokenRequest(NetHttpTransport(), JacksonFactory(),
                    GenericUrl(google.accessTokenUri), authResponse.getCode())
                    .setRedirectUri(req.requestURL.toString())
                    .setClientAuthentication(
                            BasicAuthentication(google.clientId, google.clientSecret))
                    .execute();
            val credential = Credential(BearerToken.authorizationHeaderAccessMethod())
            credential.setFromTokenResponse(response)
            val userInfo = getUserInfo(credential.accessToken)
            return ResponseEntity(userInfo.toString(), HttpStatus.OK);

        } catch (e: TokenResponseException) {
            return ResponseEntity(HttpStatus.UNAUTHORIZED);
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

    private fun rebuildUrl(req: HttpServletRequest): String {
        val fullUrlBuf = req.getRequestURL();
        val queryString = req.getQueryString()
        if (queryString != null && queryString.length > 0 ) {
            fullUrlBuf.append('?').append(queryString);
        }
        return fullUrlBuf.toString()
    }


}

