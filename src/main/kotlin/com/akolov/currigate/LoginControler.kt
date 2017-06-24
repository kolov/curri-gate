package com.akolov.currigate

import com.google.api.client.auth.oauth2.*
import com.google.api.client.http.BasicAuthentication
import com.google.api.client.http.GenericUrl
import com.google.api.client.http.HttpHeaders
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.GenericJson
import com.google.api.client.json.JsonObjectParser
import com.google.api.client.json.jackson.JacksonFactory
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
                .setScopes(Arrays.asList("openid", "email", "profile"))
                .setRedirectUri(req.requestURL.toString()).build()
        println(url)
        response.sendRedirect(url)
    }

    @RequestMapping(method = arrayOf(RequestMethod.GET), value = "/login/google", params = arrayOf("code"))
    open fun loginCallback(req: HttpServletRequest) {

        val fullUrlBuf = req.getRequestURL();
        if (req.getQueryString() != null) {
            fullUrlBuf.append('?').append(req.getQueryString());
        }
        val authResponse = AuthorizationCodeResponseUrl(fullUrlBuf.toString());
        // check for user-denied error
        if (authResponse.getError() != null) {
            // authorization denied...
        } else {
            try {
                val response = AuthorizationCodeTokenRequest(NetHttpTransport(), JacksonFactory(),
                        GenericUrl(google.accessTokenUri), authResponse.getCode())
                        .setRedirectUri(req.requestURL.toString())
                        .setClientAuthentication(
                                BasicAuthentication(google.clientId, google.clientSecret))
                        .execute();
                val credential = Credential(BearerToken.authorizationHeaderAccessMethod())
                credential.setFromTokenResponse(response )




                val jsonFactory = JacksonFactory()
                val requestFactory = NetHttpTransport().createRequestFactory()
                val request = requestFactory.buildGetRequest(GenericUrl(google.userInfoUri))
                request.setParser(JsonObjectParser(jsonFactory))
                request.setThrowExceptionOnExecuteError(false)
                val headers = HttpHeaders()
                headers.setAuthorization("Bearer " + credential.accessToken)
                request.setHeaders(headers)
                val userInfoResponse = request.execute()
                if (userInfoResponse.isSuccessStatusCode()) {
                    val userInfo = userInfoResponse.parseAs(GenericJson::class.java)
                    System.out.println("Access token: " + userInfo);
                }


            } catch (e: TokenResponseException) {
                if (e.getDetails() != null) {
                    System.err.println("Error: " + e.getDetails().getError());
                    if (e.getDetails().getErrorDescription() != null) {
                        System.err.println(e.getDetails().getErrorDescription());
                    }
                    if (e.getDetails().getErrorUri() != null) {
                        System.err.println(e.getDetails().getErrorUri());
                    }
                } else {
                    System.err.println(e.message);
                }
            }


        }
    }


}

