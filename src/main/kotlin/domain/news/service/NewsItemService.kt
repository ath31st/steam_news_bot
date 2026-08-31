package sidim.doma.domain.news.service

import dev.inmo.tgbotapi.types.rich.InputRichMessage
import sidim.doma.application.news.presentation.NewsFallbackFormatter
import sidim.doma.application.news.presentation.NewsPresentationBuilder
import sidim.doma.application.news.presentation.NewsPresentationContext
import sidim.doma.domain.news.entity.NewsItem

class NewsItemService(
    private val presentationBuilder: NewsPresentationBuilder,
    private val fallbackFormatter: NewsFallbackFormatter
) {
    fun buildRichNewsMessage(
        newsItem: NewsItem,
        gameName: String?,
        isInWishlist: Boolean,
        locale: String
    ): InputRichMessage = presentationBuilder.build(
        NewsPresentationContext(
            newsItem = newsItem,
            gameName = gameName,
            isInWishlist = isInWishlist,
            locale = locale
        )
    )

    fun buildFallbackHtml(
        newsItem: NewsItem,
        gameName: String?,
        isInWishlist: Boolean,
        locale: String
    ): String = fallbackFormatter.format(
        NewsPresentationContext(
            newsItem = newsItem,
            gameName = gameName,
            isInWishlist = isInWishlist,
            locale = locale
        )
    )
}
