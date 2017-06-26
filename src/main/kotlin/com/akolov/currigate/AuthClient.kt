package com.akolov.currigate

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*


@Component
class AuthClient {

    @Value("\${oauth2.google.client.clientId}")
    var clientId: String? = null
    @Value("\${oauth2.google.client.secret}")
    var clientSecret: String? = null
    @Value("\${oauth2.google.client.accessTokenUri}")
    var accessTokenUri: String? = null
    @Value("\${oauth2.google.client.userAuthorizationUri}")
    var userAuthorizationUri: String? = null
    @Value("\${oauth2.google.client.userInfoUri}")
    var userInfoUri: String? = null

    val scope: List<String> = Arrays.asList("openid","email", "profile")

}




