package jp.andpad.api.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthFilter(private val jwtService: JwtService) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        var token = extractBearer(request)
        if (token == null) {
            token = extractCookie(request, "dv_token")
        }
        if (token != null) {
            try {
                val principal = jwtService.parseToken(token)
                val authentication = UsernamePasswordAuthenticationToken(principal, null, emptyList())
                SecurityContextHolder.getContext().authentication = authentication
            } catch (_: Exception) {
                SecurityContextHolder.clearContext()
            }
        }
        filterChain.doFilter(request, response)
    }

    private fun extractBearer(request: HttpServletRequest): String? {
        val auth = request.getHeader("Authorization")
        if (auth != null && auth.startsWith("Bearer ", ignoreCase = true)) {
            return auth.substring(7).trim()
        }
        return null
    }

    private fun extractCookie(request: HttpServletRequest, name: String): String? {
        val cookies = request.cookies ?: return null
        for (cookie in cookies) {
            if (name == cookie.name && cookie.value != null && !cookie.value.isBlank()) {
                return cookie.value
            }
        }
        return null
    }
}
