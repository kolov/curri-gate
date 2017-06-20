package com.akolov.currigate

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.oauth2.client.OAuth2ClientContext
import org.springframework.security.oauth2.client.OAuth2RestTemplate
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import javax.servlet.Filter


fun main(args: Array<String>) {
    SpringApplication.run(CurriGateApplication::class.java, "--debug")
}

@SpringBootApplication
@EnableOAuth2Client
open class CurriGateApplication() : WebSecurityConfigurerAdapter(true) {

    @Autowired
    var oauth2ClientContext: OAuth2ClientContext? = null


    override fun configure(http: HttpSecurity) {
        http
//
               .antMatcher("/**")
.authorizeRequests()
                .anyRequest().permitAll()
  //              .antMatchers("/", "/login/**", "/**" ).permitAll()
               // .anyRequest()//.authenticated()

//                .authorizeRequests()
//                .antMatchers("/", "/login**").permitAll()
////                .anyRequest().authenticated()
//                .and().logout().logoutSuccessUrl("/").permitAll()
                .and()
                .addFilterBefore(ssoGoogleFilter(), BasicAuthenticationFilter::class.java)
            //    .addFilterBefore(ssoFacebookFilter(), BasicAuthenticationFilter::class.java)
    }

    @Bean
    open fun oauth2ClientFilterRegistration(filter: OAuth2ClientContextFilter): FilterRegistrationBean {
        val registration = FilterRegistrationBean()
        registration.filter = filter
        registration.order = -500
        return registration
    }

    fun ssoFacebookFilter(): Filter {

        val facebookTemplate = OAuth2RestTemplate(facebook(), oauth2ClientContext);
        val facebookFilter = OAuth2ClientAuthenticationProcessingFilter("/login/facebook")
        facebookFilter.setRestTemplate(facebookTemplate);
        facebookFilter.setTokenServices(
                FacebookUserInfoTokenServices(facebookResource().getUserInfoUri(),
                        facebook().getClientId()));
        return facebookFilter;
    }

    fun ssoGoogleFilter(): Filter {
        val google = google()
        val googleTemplate = OAuth2RestTemplate(google, oauth2ClientContext);
        val googleFilter = OAuth2ClientAuthenticationProcessingFilter("/login/google")
        googleFilter.setRestTemplate(googleTemplate);
        googleFilter.setTokenServices(
                UserInfoTokenServices(googleResource().getUserInfoUri(),
                        google.getClientId()));
        return googleFilter;
    }


    @Bean
    @ConfigurationProperties("oauth2.facebook.client")
    open fun facebook(): AuthorizationCodeResourceDetails = AuthorizationCodeResourceDetails();


    @Bean
    @ConfigurationProperties("oauth2.facebook.resource")
    open fun facebookResource(): ResourceServerProperties = ResourceServerProperties();


    @Bean
    @ConfigurationProperties("oauth2.google.client")
    open fun google(): AuthorizationCodeResourceDetails = AuthorizationCodeResourceDetails();


    @Bean
    @ConfigurationProperties("oauth2.google.resource")
    open fun googleResource(): ResourceServerProperties = ResourceServerProperties();


    class FacebookUserInfoTokenServices(userInfoEndpointUrl: String, clientId: String)
        : UserInfoTokenServices(userInfoEndpointUrl, clientId) {
        override fun loadAuthentication(accessToken: String): OAuth2Authentication {
            val auth = super.loadAuthentication(accessToken)
            (auth.getUserAuthentication().getDetails() as java.util.Map<String, String>)
                    .put("oauthProvider", "facebook")
            return auth
        }
    }
}