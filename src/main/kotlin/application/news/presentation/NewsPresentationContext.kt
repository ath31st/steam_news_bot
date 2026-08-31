package sidim.doma.application.news.presentation

import sidim.doma.domain.news.entity.NewsItem

data class NewsPresentationContext(
    val newsItem: NewsItem,
    val gameName: String?,
    val isInWishlist: Boolean,
    val locale: String
)
