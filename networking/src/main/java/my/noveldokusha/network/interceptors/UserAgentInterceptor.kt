package my.noveldokusha.network.interceptors

import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber

const val GLOBAL_USER_AGENT = "Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro Build/UQ1A.240205.004) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.6834.83 Mobile Safari/537.36"

internal class UserAgentInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (request.header("User-Agent") != null) return chain.proceed(request)

        // 1. По тегу source:<id> (Lua http_get, bookChapter, etc.)
        val tag = request.tag(String::class.java)
        if (tag != null && tag.startsWith("source:")) {
            val sourceId = tag.removePrefix("source:")
            val presetName = PluginUARegistry.getPreset(sourceId)
            if (presetName != null) {
                val presetUA = UAPresets.resolve(presetName)
                if (presetUA != null) {
                    return chain.proceed(
                        request.newBuilder().header("User-Agent", presetUA).build()
                    )
                }
            }
        }

        // 2. По хосту (Coil-картинки, нетэгированные запросы и т.п.)
        if (tag == null) {
            val presetName = PluginUARegistry.getPresetForHost(request.url.host)
            if (presetName != null) {
                val presetUA = UAPresets.resolve(presetName)
                if (presetUA != null) {
                    return chain.proceed(
                        request.newBuilder().header("User-Agent", presetUA).build()
                    )
                }
            }
        }

        Timber.d("UserAgentInterceptor: no tag or no preset, fallback to default UA")
        return chain.proceed(
            request.newBuilder().header("User-Agent", GLOBAL_USER_AGENT).build()
        )
    }
}
