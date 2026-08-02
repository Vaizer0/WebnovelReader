package my.noveldokusha.network.interceptors

import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap

data class CfDomainOptions(
    val whitelist: Boolean = false,
    val ignoreMarkers: Set<String> = emptySet()
)

object LuaCfOptionsRegistry {
    private val options = ConcurrentHashMap<String, CfDomainOptions>()

    fun register(domain: String, cfOptions: CfDomainOptions) {
        val key = domain.removePrefix("https://").removePrefix("http://")
            .removePrefix("www.").trimEnd('/')
        options[key] = cfOptions
        Timber.d("CF options registered for $key: $cfOptions")
    }

    fun getForHost(host: String): CfDomainOptions? {
        val key = host.removePrefix("www.")
        return options[key]
    }

    fun clear(domain: String) {
        val key = domain.removePrefix("https://").removePrefix("http://")
            .removePrefix("www.").trimEnd('/')
        options.remove(key)
    }
}
