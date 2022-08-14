package com.javidconsulting.mvpvending.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.stereotype.Component
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class AuthenticationProcessingFilter(
    private val objectMapper: ObjectMapper,
    authenticationManager: AuthenticationManager,
) : AbstractAuthenticationProcessingFilter(AntPathRequestMatcher("/login", "POST"), authenticationManager) {
    private val log = LoggerFactory.getLogger(AuthenticationProcessingFilter::class.java)

    init {
        this.setAuthenticationSuccessHandler { _, response, _ ->
            log.debug("Success Handler")
            response.sendRedirect("/me")
        }
        log.debug("Current Success Handler:{}", successHandler)
    }

    override fun attemptAuthentication(request: HttpServletRequest, response: HttpServletResponse?): Authentication {
        try {
            val requestNode = objectMapper.readTree(request.inputStream)
            val username = requestNode["username"].asText()
            val password = requestNode["password"].asText()
            log.debug("Username:{}, Password:{}", username, password)
            val authRequest = UsernamePasswordAuthenticationToken(username, password)
            return this.authenticationManager.authenticate(authRequest)
        } catch (e: Exception) {
            throw AuthenticationServiceException(e.message, e)
        }
    }
}
