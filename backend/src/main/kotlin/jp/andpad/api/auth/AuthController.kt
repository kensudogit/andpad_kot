package jp.andpad.api.auth

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.time.Duration
import jp.andpad.api.auth.dto.AuthResponse
import jp.andpad.api.auth.dto.LoginRequest
import jp.andpad.api.auth.dto.RegisterRequest
import jp.andpad.api.config.AppProperties
import jp.andpad.api.repository.AuthRepository.RegisterInput
import jp.andpad.api.service.AuthService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService,
    private val appProperties: AppProperties,
) {

    @PostMapping("/login")
    fun login(
        @RequestBody request: LoginRequest,
        httpRequest: HttpServletRequest,
        response: HttpServletResponse,
    ): ResponseEntity<*> {
        return try {
            val payload = authService.login(request.email, request.password)
            setTokenCookie(httpRequest, response, payload.token)
            ResponseEntity.ok(payload)
        } catch (_: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("error" to "invalid credentials"))
        }
    }

    @PostMapping("/register")
    fun register(
        @RequestBody request: RegisterRequest,
        httpRequest: HttpServletRequest,
        response: HttpServletResponse,
    ): ResponseEntity<*> {
        return try {
            val payload = authService.register(
                RegisterInput(
                    request.clinicName,
                    request.slug,
                    request.ownerName,
                    request.email,
                    request.password,
                ),
            )
            setTokenCookie(httpRequest, response, payload.token)
            ResponseEntity.ok(payload)
        } catch (ex: Exception) {
            ResponseEntity.badRequest().body(mapOf("error" to ex.message))
        }
    }

    @PostMapping("/logout")
    fun logout(request: HttpServletRequest, response: HttpServletResponse): ResponseEntity<Map<String, Boolean>> {
        clearTokenCookie(request, response)
        return ResponseEntity.ok(mapOf("ok" to true))
    }

    private fun setTokenCookie(request: HttpServletRequest, response: HttpServletResponse, token: String) {
        val cookie = Cookie("dv_token", token).apply {
            path = "/"
            isHttpOnly = true
            secure = cookieSecure(request)
            maxAge = Duration.ofHours(appProperties.jwt.ttlHours.toLong()).seconds.toInt()
        }
        response.addCookie(cookie)
    }

    private fun clearTokenCookie(request: HttpServletRequest, response: HttpServletResponse) {
        val cookie = Cookie("dv_token", "").apply {
            path = "/"
            isHttpOnly = true
            secure = cookieSecure(request)
            maxAge = 0
        }
        response.addCookie(cookie)
    }

    private fun cookieSecure(request: HttpServletRequest): Boolean {
        if (request.isSecure) {
            return true
        }
        return "https".equals(request.getHeader("X-Forwarded-Proto"), ignoreCase = true)
    }
}
