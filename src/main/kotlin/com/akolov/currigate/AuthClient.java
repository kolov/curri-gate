package com.akolov.currigate;


import java.util.List;

public class AuthClient {
    private String clientId;

    private String clientSecret;
    private String accessTokenUri;
    private String userAuthorizationUri;
    private String userInfoUri;
    private List<String> scope;
    private String clientAuthenticationScheme;

    public String getUserInfoUri() {
        return userInfoUri;
    }

    public void setUserInfoUri(String userInfoUri) {
        this.userInfoUri = userInfoUri;
    }


    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getAccessTokenUri() {
        return accessTokenUri;
    }

    public String getUserAuthorizationUri() {
        return userAuthorizationUri;
    }

    public String getClientAuthenticationScheme() {
        return clientAuthenticationScheme;
    }

    public List<String> getScope() {
        return scope;
    }

    public void setScope(List<String> scope) {
        this.scope = scope;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public void setAccessTokenUri(String accessTokenUri) {
        this.accessTokenUri = accessTokenUri;
    }

    public void setUserAuthorizationUri(String userAuthorizationUri) {
        this.userAuthorizationUri = userAuthorizationUri;
    }

    public void setClientAuthenticationScheme(String clientAuthenticationScheme) {
        this.clientAuthenticationScheme = clientAuthenticationScheme;
    }
}
                      


