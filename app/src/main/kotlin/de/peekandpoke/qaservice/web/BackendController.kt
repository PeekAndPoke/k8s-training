package de.peekandpoke.qaservice.web

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.net.InetAddress

/**
 * Active only when SPRING_PROFILES_ACTIVE=backend.
 *
 * The backend is internal (ClusterIP only, no Ingress). It reports which pod served the request
 * (so you can watch load-balancing across replicas) and a hit counter it keeps in Redis — the
 * stateful backing service reached via a password from a Kubernetes Secret.
 */
@RestController
@Profile("backend")
class BackendController(
    private val redis: StringRedisTemplate,
    @Value("\${app.version}") private val version: String,
) {
    @GetMapping("/hello")
    fun hello(): Map<String, Any?> = mapOf(
        "role" to "backend",
        "version" to version,
        "servedBy" to hostname(),   // pod name — watch this rotate across replicas
        "hits" to hitCount(),       // proves the Redis link is live
    )

    /** Increment a counter in Redis. Degrade gracefully if Redis is unreachable. */
    private fun hitCount(): Any =
        try {
            redis.opsForValue().increment("qa:hello:count") ?: -1L
        } catch (e: Exception) {
            "redis-unavailable: ${e.message}"
        }

    /** In Kubernetes, HOSTNAME is set to the pod name. */
    private fun hostname(): String =
        System.getenv("HOSTNAME")
            ?: runCatching { InetAddress.getLocalHost().hostName }.getOrDefault("unknown")
}
