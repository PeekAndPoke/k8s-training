package de.peekandpoke.qaservice.web

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestClient

/**
 * Active only when SPRING_PROFILES_ACTIVE=gateway.
 *
 * The gateway is the public face (behind the Ingress). Its /hello aggregates its own version
 * with live calls to the backend, httpbin and whoami — all reached by in-cluster Service DNS
 * names that are injected as env vars (BACKEND_URL, HTTPBIN_URL, WHOAMI_URL) by the Helm chart.
 * This is the "east-west traffic" story made visible in a single JSON response.
 */
@RestController
@Profile("gateway")
class GatewayController(
    @Value("\${app.version}") private val version: String,
    @Value("\${gateway.backend-url}") private val backendUrl: String,
    @Value("\${gateway.httpbin-url}") private val httpbinUrl: String,
    @Value("\${gateway.whoami-url}") private val whoamiUrl: String,
) {
    // Boot 4 no longer auto-configures a RestClient.Builder bean, so create the client directly.
    private val http = RestClient.create()

    @GetMapping("/hello")
    fun hello(): Map<String, Any?> = mapOf(
        "role" to "gateway",
        "version" to version,
        // east-west call to our own backend service
        "backend" to call { http.get().uri("$backendUrl/hello").retrieve().body(Map::class.java) },
        // call to a ready-made REST service (httpbin)
        "external" to call { http.get().uri("$httpbinUrl/uuid").retrieve().body(Map::class.java) },
        // whoami returns plain text; we pull out just the serving pod's hostname
        "whoami" to call { hostnameOf(http.get().uri("$whoamiUrl/").retrieve().body(String::class.java)) },
    )

    /** Never let a downstream failure take the gateway down — report the error inline instead. */
    private fun <T> call(block: () -> T): Any? =
        try {
            block()
        } catch (e: Exception) {
            mapOf("error" to (e.message ?: e.javaClass.simpleName))
        }

    private fun hostnameOf(raw: String?): String? =
        raw?.lineSequence()
            ?.firstOrNull { it.startsWith("Hostname:") }
            ?.substringAfter("Hostname:")
            ?.trim()
            ?: raw
}
