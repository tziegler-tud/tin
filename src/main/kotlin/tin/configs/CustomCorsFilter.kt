package tin.configs

import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class CustomCorsFilter : CorsFilter(
    CorsConfiguration().let { config ->

        config.allowedOrigins = listOf("http://localhost:8901", "http://localhost:3000")
        config.exposedHeaders = listOf("Content-Disposition", "Content-Length")
        config.allowedMethods = listOf("HEAD", "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
        config.allowedHeaders = listOf("Authorization", "Cache-Control", "Content-Type", "Content-Disposition", "X-Session-Seed")
        config.allowCredentials = true


        UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", config)
        }
    }
)