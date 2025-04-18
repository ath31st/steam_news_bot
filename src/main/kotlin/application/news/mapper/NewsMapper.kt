package sidim.doma.application.news.mapper

import sidim.doma.domain.news.entity.NewsItem
import sidim.doma.infrastructure.integration.steam.dto.SteamNewsItemDto

fun SteamNewsItemDto.toNewsItem() = NewsItem(
    gid = gid,
    title = title,
    url = url,
    author = author,
    contents = contents,
    appid = appid,
    date = date
)
