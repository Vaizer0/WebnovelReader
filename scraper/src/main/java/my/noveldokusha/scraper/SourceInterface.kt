package my.noveldokusha.scraper

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import my.noveldokusha.core.LanguageCode
import my.noveldokusha.core.PagedList
import my.noveldokusha.core.Response
import my.noveldokusha.scraper.domain.BookResult
import my.noveldokusha.scraper.domain.ChapterResult
import org.jsoup.nodes.Document

sealed interface SourceInterface {
    val id: String

    @get:StringRes
    val nameStrId: Int

    /**
     * Dynamic source name — used by Lua extensions which have no string resource.
     * If not null, the UI should use this instead of nameStrId.
     */
    val name: String? get() = null

    val baseUrl: String
    val isLocalSource: Boolean get() = true
    val requiresLogin: Boolean get() = false
    val charset: String get() = "UTF-8"

    fun resolveName(context: android.content.Context): String =
        name ?: if (nameStrId != 0) context.getString(nameStrId) else "Unknown"

    // Transform current url to preferred url
    suspend fun transformChapterUrl(url: String): String = url

    suspend fun getChapterTitle(doc: Document): String? = null
    suspend fun getChapterText(doc: Document): String? = null

    interface Base : SourceInterface
    interface Catalog : SourceInterface {
        val catalogUrl: String
        val language: LanguageCode?
        val languageTag: String? get() = language?.iso639_1
        val iconUrl: Any get() = "$baseUrl/favicon.ico"
        val iconResId: Int? get() = null

        suspend fun getBookCoverImageUrl(bookUrl: String): Response<String?> =
            Response.Success(null)

        suspend fun getBookDescription(bookUrl: String): Response<String?> = Response.Success(null)

        suspend fun getBookTitle(bookUrl: String): Response<String?> = Response.Success(null)

        /** Returns the list of genres/tags for the book. Implemented in Lua via getBookGenres(). */
        suspend fun getBookGenres(bookUrl: String): Response<List<String>> = Response.Success(emptyList())

        /**
         * Chapters list ordered from first one (oldest) to newest one.
         */
        suspend fun getChapterList(bookUrl: String): Response<List<ChapterResult>>
        suspend fun getCatalogList(index: Int): Response<PagedList<BookResult>>
        suspend fun getCatalogSearch(index: Int, input: String): Response<PagedList<BookResult>>

        suspend fun getChapterListHash(bookUrl: String): Response<String?> = Response.Success(null)

        /**
         * Result of parsePage — chapters from one page plus the total page count.
         */
        data class PagedChapterResult(
            val chapters: List<ChapterResult>,
            val totalPages: Int,
        )

        /**
         * Loads a single page of the chapter list. Optionally implemented by the plugin.
         *
         * If the plugin declares this function, the engine switches to paginated mode.
         * If the function is not declared (returns null), the engine uses the old
         * getChapterList() path.
         */
        suspend fun parsePage(bookUrl: String, page: Int): Response<PagedChapterResult>? = null
    }

    /**
     * A source supporting catalog filtering.
     * The plugin declares getFilterList() and getCatalogFiltered().
     */
    interface FilterableCatalog : Catalog {
        suspend fun getFilterList(): Response<List<LuaFilter>>

        suspend fun getCatalogFiltered(
            index: Int,
            filters: ActiveFilters
        ): Response<PagedList<BookResult>>
    }

    interface Configurable {
        @Composable
        fun ScreenConfig()
    }
}
