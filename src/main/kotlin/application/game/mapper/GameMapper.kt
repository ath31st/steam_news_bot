package sidim.doma.application.game.mapper

import sidim.doma.domain.game.entity.Game
import sidim.doma.infrastructure.integration.steam.dto.SteamAppDto
import sidim.doma.infrastructure.integration.steam.dto.SteamWishlistAppDto

fun SteamAppDto.toGame() = Game(
    appid = appid.toString(),
    name = name
)

fun SteamWishlistAppDto.toGame() = Game(
    appid = appid.toString(),
    name = null
)