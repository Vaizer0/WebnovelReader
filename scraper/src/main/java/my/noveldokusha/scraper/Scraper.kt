package my.noveldokusha.scraper

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import my.noveldokusha.core.LanguageCode
import my.noveldokusha.network.NetworkClient
import my.noveldokusha.scraper.databases.BakaUpdates
import my.noveldokusha.scraper.databases.NovelUpdates
import my.noveldokusha.scraper.sources.AT
import my.noveldokusha.scraper.sources.BacaLightnovel
import my.noveldokusha.scraper.sources.BoxNovel
import my.noveldokusha.scraper.sources.IndoWebnovel
import my.noveldokusha.scraper.sources.LocalSource
import my.noveldokusha.scraper.sources.MeioNovel
import my.noveldokusha.scraper.sources.MoreNovel
import my.noveldokusha.scraper.sources.NovelBin
import my.noveldokusha.scraper.sources.NovelHall
import my.noveldokusha.scraper.sources.Novelku
import my.noveldokusha.scraper.sources.ReadNovelFull
import my.noveldokusha.scraper.sources.Reddit
import my.noveldokusha.scraper.sources.RoyalRoad
import my.noveldokusha.scraper.sources.Saikai
import my.noveldokusha.scraper.sources.SakuraNovel
import my.noveldokusha.scraper.sources.Sousetsuka
import my.noveldokusha.scraper.sources.WbNovel
import my.noveldokusha.scraper.sources.WuxiaWorld
import my.noveldokusha.scraper.sources.ScribbleHub
import my.noveldokusha.scraper.sources.FreeWebNovel
import my.noveldokusha.scraper.sources.NovelFull
import my.noveldokusha.scraper.sources.AllNovel
import my.noveldokusha.scraper.sources.NovelBinCom
import my.noveldokusha.scraper.sources.ReadMTL
import my.noveldokusha.scraper.sources.NewNovel
import my.noveldokusha.scraper.sources.SonicMTL
import my.noveldokusha.scraper.sources.NoBadNovel
import my.noveldokusha.scraper.sources.FanMTL
import my.noveldokusha.scraper.sources.LNMTL
import my.noveldokusha.scraper.sources.WtrLab
import my.noveldokusha.scraper.sources.Shuba69
import my.noveldokusha.scraper.sources.UuKanshu
import my.noveldokusha.scraper.sources.Ddxss
import my.noveldokusha.scraper.sources.LeYueDu
import my.noveldokusha.scraper.sources.Twkan
import my.noveldokusha.scraper.sources.Ttkan
import my.noveldokusha.scraper.sources.QqBook
import my.noveldokusha.scraper.sources.WfxsTw
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Scraper @Inject constructor(
    networkClient: NetworkClient,
    localSource: LocalSource,
    private val luaSourceProvider: LuaSourceProvider
) {
    val databasesList = setOf(
        NovelUpdates(networkClient),
        BakaUpdates(networkClient)
    )

    private val scraperScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _luaSources = MutableStateFlow<Set<SourceInterface>>(emptySet())
    val luaSources: StateFlow<Set<SourceInterface>> = _luaSources.asStateFlow()

    init {
        scraperScope.launch {
            luaSourceProvider.sourcesFlow.collect { sources ->
                _luaSources.value = sources.toSet()
                Timber.d("Lua sources updated: ${sources.size}")
            }
        }
    }

    suspend fun awaitLoaded() = luaSourceProvider.awaitLoaded()

    fun clearCache() = luaSourceProvider.clearCache()

    val sourcesList = setOf(
        localSource,
        ReadNovelFull(networkClient),
        RoyalRoad(networkClient),
        my.noveldokusha.scraper.sources.NovelUpdates(networkClient),
        Reddit(),
        AT(),
        Sousetsuka(),
        Saikai(networkClient),
        BoxNovel(networkClient),
        NovelHall(networkClient),
        WuxiaWorld(networkClient),
        IndoWebnovel(networkClient),
        Shuba69(networkClient),
        UuKanshu(networkClient),
        Ddxss(networkClient),
        LeYueDu(networkClient),
        Twkan(networkClient),
        Ttkan(networkClient),
        QqBook(networkClient),
        WfxsTw(networkClient),
        BacaLightnovel(networkClient),
        SakuraNovel(networkClient),
        MeioNovel(networkClient),
        MoreNovel(networkClient),
        Novelku(networkClient),
        WbNovel(networkClient),
        NovelBin(networkClient),
        ScribbleHub(networkClient),
        FreeWebNovel(networkClient),
        NovelFull(networkClient),
        AllNovel(networkClient),
        NovelBinCom(networkClient),
        ReadMTL(networkClient),
        NewNovel(networkClient),
        SonicMTL(networkClient),
        NoBadNovel(networkClient),
        FanMTL(networkClient),
        LNMTL(networkClient),
        WtrLab(networkClient),
    )

    /** Все источники включая CachedSource заглушки (для UI) */
    val sourcesCatalogListFlow: kotlinx.coroutines.flow.Flow<List<SourceInterface.Catalog>> =
        _luaSources.map { lua ->
            (sourcesList + lua).filterIsInstance<SourceInterface.Catalog>()
        }

    val sourcesLanguagesListFlow: kotlinx.coroutines.flow.Flow<List<LanguageCode>> =
        sourcesCatalogListFlow.map { catalogs ->
            catalogs.mapNotNull { it.language }.distinct()
        }

    /** Только загруженные реальные Lua-источники (для data-операций) */
    val loadedSourcesList: Set<SourceInterface>
        get() = sourcesList + luaSourceProvider.loadedSourcesFlow.value.toSet()

    val sourcesCatalogsList = sourcesList.filterIsInstance<SourceInterface.Catalog>()
    val sourcesCatalogsLanguagesList = sourcesCatalogsList.mapNotNull { it.language }.toSet()

    private fun String.isCompatibleWithBaseUrl(baseUrl: String): Boolean {
        val normalizedUrl = if (this.endsWith("/")) this else "$this/"
        val normalizedBaseUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        return normalizedUrl.startsWith(normalizedBaseUrl)
    }

    fun getCompatibleSource(url: String): SourceInterface? =
        loadedSourcesList.find { url.isCompatibleWithBaseUrl(it.baseUrl) }

    fun getCompatibleSourceCatalog(url: String): SourceInterface.Catalog? =
        loadedSourcesList.filterIsInstance<SourceInterface.Catalog>()
            .find { url.isCompatibleWithBaseUrl(it.baseUrl) }

    fun getCompatibleDatabase(url: String): DatabaseInterface? =
        databasesList.find { url.isCompatibleWithBaseUrl(it.baseUrl) }

    fun isUrlSupported(url: String): Boolean =
        loadedSourcesList.find { url.isCompatibleWithBaseUrl(it.baseUrl) } != null

    fun getSourceId(url: String): String? =
        loadedSourcesList.find { url.isCompatibleWithBaseUrl(it.baseUrl) }?.id
}
