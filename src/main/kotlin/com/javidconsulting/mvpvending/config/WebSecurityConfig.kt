package com.javidconsulting.mvpvending.config

import com.javidconsulting.mvpvending.model.UserPrincipal
import com.javidconsulting.mvpvending.service.UserService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@EnableWebSecurity
class WebSecurityConfig {

    val log: Logger = LoggerFactory.getLogger(WebSecurityConfig::class.java)

    @Bean
    fun userDetailsService(userService: UserService): UserDetailsService {
        val service = UserDetailsService { username ->
            val optional = userService.loadUserByUsername(username).map(::UserPrincipal)
            if (optional.isPresent) {
                return@UserDetailsService optional.get()
            } else {
                throw UsernameNotFoundException("User not found!")
            }
        }
        return service
    }

    @Bean
    fun filterChain(http: HttpSecurity, authFilter: AuthenticationProcessingFilter): SecurityFilterChain {
        http
            .csrf().disable()
            .httpBasic().disable()
            .addFilterAt(authFilter, UsernamePasswordAuthenticationFilter::class.java)
            .authorizeRequests {
                it
                    .antMatchers(HttpMethod.GET, "/").permitAll()
                    .antMatchers(HttpMethod.POST, "/login", "/user").permitAll()
                    .anyRequest().authenticated()
            }
            .logout {
                it.permitAll()
                    .invalidateHttpSession(true)
                    .deleteCookies("SESSION")
                    .logoutSuccessHandler { _, response, _ ->
                        log.debug("Logout Success Handler")
                        response.sendRedirect("/")
                    }
            }
        val build = http.build()
        log.warn("sec:{}", build)
        return build
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun authenticationManager(authenticationConfiguration: AuthenticationConfiguration): AuthenticationManager =
        authenticationConfiguration.authenticationManager


}
