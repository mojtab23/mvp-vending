package com.javidconsulting.mvpvending.controller

import com.javidconsulting.mvpvending.model.UserMe
import com.javidconsulting.mvpvending.model.UserPrincipal
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.session.FindByIndexNameSessionRepository
import org.springframework.session.Session
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletResponse

@RestController
class HomeController(
    private val sessionRepository: FindByIndexNameSessionRepository<out Session?>,
) {

    @GetMapping("/")
    fun home(): String {
        return "Ok!"
    }

    @GetMapping("/me")
    fun showMe(@AuthenticationPrincipal user: UserPrincipal?): UserMe? {
        if (user == null) return null
        val sessionMap = sessionRepository.findByPrincipalName(user.username)
            .filter { entry -> (entry.value?.isExpired?.not()) ?: false }
        val activeSessions = sessionMap
            .count()

        val sessionMessageMap = if (activeSessions < 2) {
            mapOf("message" to "There is only One active session.")
        } else {
            mapOf(
                "message" to "There are multiple active sessions. " +
                        "You can terminate all sessions by calling this url:'/logout/all'",
                "activeSessions" to activeSessions,
//                "sessionMap" to sessionMap,
            )
        }
        return UserMe(user.id, user.username, sessionMessageMap)
    }

    @PostMapping("/logout/all")
    fun logoutAll(@AuthenticationPrincipal user: UserPrincipal?, response: HttpServletResponse): ResponseEntity<Unit> {
        if (user != null) {
            sessionRepository.findByPrincipalName(user.username)
                .forEach { (sessionId, _) ->
                    sessionRepository.deleteById(sessionId)
                }
        }
        val cookie = Cookie("SESSION", null) // Not necessary, but saves bandwidth.
        cookie.path = "/"
        cookie.isHttpOnly = true
        cookie.maxAge = 0
        response.addCookie(cookie)

        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("/")).body(Unit)
    }


}
